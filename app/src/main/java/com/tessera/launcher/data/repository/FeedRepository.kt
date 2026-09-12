package com.tessera.launcher.data.repository

import android.content.Context
import android.util.Log
import com.tessera.launcher.data.model.FeedPost
import com.tessera.launcher.data.model.FeedSource
import com.tessera.launcher.data.preference.LauncherPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

class FeedRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {

    companion object {
        private const val TAG = "FeedRepository"
        private const val REDDIT_BASE = "https://www.reddit.com/r/"
        private const val BLUESKY_BASE = "https://public.api.bsky.app/xrpc/"
        private const val CACHE_TTL_MS = 15 * 60 * 1000L
        private const val DEFAULT_LIMIT = 20
        private const val CONNECT_TIMEOUT = 8_000
        private const val READ_TIMEOUT = 10_000
        private const val USER_AGENT = "android:com.tessera.launcher:v1.8.3 (by /u/tessera_launcher)"
    }

    private var lastFetchTime: Long = 0L

    suspend fun fetchFeed(forceRefresh: Boolean = false): FeedResult {
        val cachedJson = preferences.getFeedCachedJson()
        val cacheValid = !forceRefresh &&
            cachedJson != null &&
            (System.currentTimeMillis() - lastFetchTime) < CACHE_TTL_MS

        if (cacheValid && cachedJson != null) {
            val cached = parseCachedPosts(cachedJson)
            if (cached.isNotEmpty()) return FeedResult.Success(cached)
        }

        return try {
            val enabledSources = preferences.getFeedEnabledSources()
            val posts = mutableListOf<FeedPost>()

            if (FeedSource.REDDIT.name in enabledSources) {
                val subreddits = preferences.getFeedSubreddits()
                for (sub in subreddits) {
                    val cleanSub = sub.trim().removePrefix("/r/").removePrefix("r/").removePrefix("/").trim()
                    if (cleanSub.isNotBlank()) {
                        runCatching {
                            val redditPosts = fetchRedditSubreddit(cleanSub)
                            posts.addAll(redditPosts)
                        }.onFailure { Log.w(TAG, "Falha ao buscar r/$cleanSub: ${it.message}") }
                    }
                }
            }

            if (FeedSource.BLUESKY.name in enabledSources) {
                val handles = preferences.getFeedBlueskyHandles()
                for (handle in handles) {
                    val cleanHandle = handle.trim().removePrefix("@").trim()
                    if (cleanHandle.isNotBlank()) {
                        runCatching {
                            val bskyPosts = fetchBlueskyAuthor(cleanHandle)
                            posts.addAll(bskyPosts)
                        }.onFailure { Log.w(TAG, "Falha ao buscar @$cleanHandle: ${it.message}") }
                    }
                }
            }

            if (posts.isEmpty()) {
                val fallback = cachedJson?.let { parseCachedPosts(it) } ?: emptyList()
                if (fallback.isNotEmpty()) {
                    FeedResult.Success(fallback)
                } else {
                    FeedResult.Empty
                }
            } else {
                var sorted = posts.distinctBy { it.id }.sortedByDescending { it.createdAt }

                // Aplicar resumos de IA se habilitado
                if (preferences.isFeedAiSummariesEnabled()) {
                    val aiHelper = com.tessera.launcher.data.helper.AiSummaryHelper()
                    sorted = sorted.map { post ->
                        val summary = aiHelper.summarizePost(post)
                        post.copy(aiSummary = summary)
                    }
                }

                lastFetchTime = System.currentTimeMillis()
                cachePosts(sorted)
                FeedResult.Success(sorted)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Feed fetch failed", e)
            val fallback = cachedJson?.let { parseCachedPosts(it) } ?: emptyList()
            if (fallback.isNotEmpty()) {
                FeedResult.Success(fallback)
            } else {
                FeedResult.Error(e.localizedMessage ?: "Falha ao carregar o feed")
            }
        }
    }

    private suspend fun fetchRedditSubreddit(subreddit: String): List<FeedPost> =
        withContext(Dispatchers.IO) {
            // 1. Tentar endpoint JSON oficial do Reddit primeiro (mais rico em campos e score)
            val jsonUrl = "${REDDIT_BASE}${subreddit}/hot.json?limit=$DEFAULT_LIMIT&raw_json=1"
            val json = httpGet(jsonUrl)
            if (json != null && json.startsWith("{") && json.contains("\"children\"")) {
                try {
                    val root = JSONObject(json)
                    val children = root.optJSONObject("data")?.optJSONArray("children")
                    if (children != null && children.length() > 0) {
                        val jsonPosts = parseRedditChildren(children, subreddit)
                        if (jsonPosts.isNotEmpty()) return@withContext jsonPosts
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Reddit JSON parse error for r/$subreddit, tentando RSS", e)
                }
            }

            // 2. Fallback para feed RSS caso JSON seja bloqueado ou falhe
            val rssUrl = "${REDDIT_BASE}${subreddit}/.rss?limit=$DEFAULT_LIMIT"
            val rssXml = httpGet(rssUrl)
            if (rssXml != null && rssXml.contains("<entry")) {
                val rssPosts = parseRedditRss(rssXml, subreddit)
                if (rssPosts.isNotEmpty()) return@withContext rssPosts
            }

            emptyList()
        }

    private fun parseRedditRss(xml: String, subreddit: String): List<FeedPost> {
        val posts = mutableListOf<FeedPost>()
        val entryRegex = Regex("<entry[\\s\\S]*?</entry>")
        val titleRegex = Regex("<title(?:[^>]*)>([\\s\\S]*?)</title>")
        val authorRegex = Regex("<author>\\s*<name>([^<]+)</name>")
        val linkRegex = Regex("<link\\s+href=\"([^\"]+)\"")
        val idRegex = Regex("<id>([^<]+)</id>")
        val updatedRegex = Regex("<updated>([^<]+)</updated>")
        val thumbRegex = Regex("<media:thumbnail\\s+url=\"([^\"]+)\"")
        val contentRegex = Regex("<content(?:[^>]*)>([\\s\\S]*?)</content>")

        for (match in entryRegex.findAll(xml)) {
            val entry = match.value
            val rawTitle = titleRegex.find(entry)?.groupValues?.getOrNull(1) ?: continue
            val title = unescapeHtml(rawTitle)
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            if (title.isBlank()) continue

            val author = authorRegex.find(entry)?.groupValues?.getOrNull(1)?.removePrefix("/u/") ?: "reddit"
            val link = linkRegex.find(entry)?.groupValues?.getOrNull(1) ?: ""
            val id = idRegex.find(entry)?.groupValues?.getOrNull(1) ?: link.hashCode().toString()
            val thumb = thumbRegex.find(entry)?.groupValues?.getOrNull(1)?.replace("&amp;", "&")
            val updatedStr = updatedRegex.find(entry)?.groupValues?.getOrNull(1)
            val created = try {
                if (updatedStr != null) java.time.Instant.parse(updatedStr).toEpochMilli()
                else System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }

            val rawContent = contentRegex.find(entry)?.groupValues?.getOrNull(1) ?: ""
            val unescapedContent = unescapeHtml(rawContent)
            val bodyText = unescapedContent
                .replace(Regex("<!--[\\s\\S]*?-->"), "")
                .replace(Regex("<table[\\s\\S]*?</table>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\[link\\]|\\[comments\\]", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\s+"), " ")
                .trim()
                .take(500)

            posts.add(
                FeedPost(
                    id = "reddit_$id",
                    source = FeedSource.REDDIT,
                    author = author,
                    authorHandle = "u/$author",
                    title = title,
                    body = bodyText,
                    subreddit = subreddit,
                    score = 0,
                    commentCount = 0,
                    url = link,
                    thumbnailUrl = thumb,
                    createdAt = created
                )
            )
        }
        return posts
    }

    private fun parseRedditChildren(children: JSONArray, subreddit: String): List<FeedPost> {
        val posts = mutableListOf<FeedPost>()
        for (i in 0 until children.length()) {
            val child = children.getJSONObject(i)
            if (child.optString("kind") != "t3") continue
            val data = child.getJSONObject("data")

            if (data.optBoolean("stickied", false)) continue

            val id = data.optString("id", "reddit_$i")
            val author = data.optString("author", "[deleted]")
            val title = data.optString("title", "")
            val selftext = data.optString("selftext", "")
            val score = data.optInt("score", 0)
            val numComments = data.optInt("num_comments", 0)
            val permalink = data.optString("permalink", "")
            val created = (data.optDouble("created_utc", 0.0) * 1000).toLong()
            val thumbnail = data.optString("thumbnail", "")
                .takeIf { it.startsWith("http") }

            val cleanTitle = unescapeHtml(title).trim()
            val cleanBody = unescapeHtml(selftext).trim().take(500)

            posts.add(
                FeedPost(
                    id = "reddit_$id",
                    source = FeedSource.REDDIT,
                    author = author,
                    authorHandle = "u/$author",
                    title = cleanTitle.ifBlank { null },
                    body = cleanBody,
                    subreddit = subreddit,
                    score = score,
                    commentCount = numComments,
                    url = if (permalink.startsWith("http")) permalink else "https://www.reddit.com$permalink",
                    thumbnailUrl = thumbnail,
                    createdAt = if (created > 0L) created else System.currentTimeMillis()
                )
            )
        }
        return posts
    }

    private suspend fun fetchBlueskyAuthor(handle: String): List<FeedPost> =
        withContext(Dispatchers.IO) {
            val cleanHandle = handle.trim()
                .removePrefix("@")
                .let { if (!it.contains(".")) "$it.bsky.social" else it }

            val url = "${BLUESKY_BASE}app.bsky.feed.getAuthorFeed?actor=$cleanHandle&limit=$DEFAULT_LIMIT"
            val json = httpGet(url) ?: return@withContext emptyList()

            try {
                val root = JSONObject(json)
                val feed = root.optJSONArray("feed") ?: return@withContext emptyList()
                parseBlueskyFeed(feed, cleanHandle)
            } catch (e: Exception) {
                Log.e(TAG, "Bluesky parse error for @$cleanHandle", e)
                emptyList()
            }
        }

    private fun parseBlueskyFeed(feed: JSONArray, handle: String): List<FeedPost> {
        val posts = mutableListOf<FeedPost>()
        for (i in 0 until feed.length()) {
            val item = feed.getJSONObject(i)
            val post = item.getJSONObject("post")
            val record = post.optJSONObject("record") ?: continue

            val uri = post.optString("uri", "bsky_$i")
            val author = post.optJSONObject("author")
            val displayName = author?.optString("displayName", handle)?.ifBlank { handle } ?: handle
            val authorHandle = author?.optString("handle", handle)?.ifBlank { handle } ?: handle
            val text = record.optString("text", "")
            val createdAt = record.optString("createdAt", "")
            val likeCount = post.optInt("likeCount", 0)
            val replyCount = post.optInt("replyCount", 0)

            val timestamp = try {
                if (createdAt.isNotBlank()) java.time.Instant.parse(createdAt).toEpochMilli()
                else System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }

            val postId = uri.substringAfterLast("/")
            val webUrl = "https://bsky.app/profile/$authorHandle/post/$postId"

            // Tentar extrair imagem thumbnail se houver embed
            val embed = post.optJSONObject("embed")
            val thumb = embed?.optJSONArray("images")?.optJSONObject(0)?.optString("thumb")
                ?: embed?.optJSONObject("media")?.optJSONArray("images")?.optJSONObject(0)?.optString("thumb")

            posts.add(
                FeedPost(
                    id = "bsky_$postId",
                    source = FeedSource.BLUESKY,
                    author = displayName,
                    authorHandle = authorHandle,
                    title = null,
                    body = text.take(500),
                    score = likeCount,
                    commentCount = replyCount,
                    url = webUrl,
                    thumbnailUrl = thumb,
                    createdAt = timestamp
                )
            )
        }
        return posts
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .replace("&#32;", " ")
    }

    private fun httpGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Accept", "*/*")
                setRequestProperty("Accept-Encoding", "gzip")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            if (connection.responseCode !in 200..299) return null

            val inputStream = if (connection.contentEncoding?.equals("gzip", true) == true) {
                GZIPInputStream(connection.inputStream)
            } else {
                connection.inputStream
            }

            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "HTTP GET failed: $urlString", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun cachePosts(posts: List<FeedPost>) {
        val arr = JSONArray()
        for (post in posts) {
            val obj = JSONObject().apply {
                put("id", post.id)
                put("source", post.source.name)
                put("author", post.author)
                put("authorHandle", post.authorHandle)
                put("title", post.title ?: JSONObject.NULL)
                put("body", post.body)
                put("subreddit", post.subreddit ?: JSONObject.NULL)
                put("score", post.score)
                put("commentCount", post.commentCount)
                put("url", post.url)
                put("thumbnailUrl", post.thumbnailUrl ?: JSONObject.NULL)
                put("createdAt", post.createdAt)
                put("aiSummary", post.aiSummary ?: JSONObject.NULL)
            }
            arr.put(obj)
        }
        preferences.setFeedCachedJson(arr.toString())
    }

    private fun parseCachedPosts(json: String): List<FeedPost> {
        return try {
            val arr = JSONArray(json)
            val posts = mutableListOf<FeedPost>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val sourceName = obj.optString("source", "REDDIT")
                val source = try {
                    FeedSource.valueOf(sourceName)
                } catch (_: Exception) {
                    FeedSource.REDDIT
                }

                posts.add(
                    FeedPost(
                        id = obj.optString("id", "cached_$i"),
                        source = source,
                        author = obj.optString("author", ""),
                        authorHandle = obj.optString("authorHandle", ""),
                        title = obj.optString("title").takeIf { it != "null" && it.isNotBlank() },
                        body = obj.optString("body", ""),
                        subreddit = obj.optString("subreddit").takeIf { it != "null" && it.isNotBlank() },
                        score = obj.optInt("score", 0),
                        commentCount = obj.optInt("commentCount", 0),
                        url = obj.optString("url", ""),
                        thumbnailUrl = obj.optString("thumbnailUrl").takeIf { it != "null" && it.isNotBlank() },
                        createdAt = obj.optLong("createdAt", 0L),
                        aiSummary = obj.optString("aiSummary").takeIf { it != "null" && it.isNotBlank() }
                    )
                )
            }
            posts
        } catch (e: Exception) {
            Log.e(TAG, "Cache parse failed", e)
            emptyList()
        }
    }
}

sealed interface FeedResult {
    data class Success(val posts: List<FeedPost>) : FeedResult
    data object Empty : FeedResult
    data class Error(val message: String) : FeedResult
}
