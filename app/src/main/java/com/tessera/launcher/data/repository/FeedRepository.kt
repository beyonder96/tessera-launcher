package com.tessera.launcher.data.repository

import android.content.Context
import android.util.Log
import com.tessera.launcher.data.model.FeedPost
import com.tessera.launcher.data.model.FeedSource
import com.tessera.launcher.data.preference.LauncherPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
        private const val CACHE_TTL_MS = 15 * 60 * 1000L
        private const val CONNECT_TIMEOUT = 4_000
        private const val READ_TIMEOUT = 5_000
        private const val USER_AGENT = "android:com.tessera.launcher:v1.8.8 (RSS Reader)"

        /**
         * Sanitiza e formata profundamente textos de matérias RSS:
         * - Remove marcadores CDATA residuais (<![CDATA[ e ]]>)
         * - Remove tags HTML, scripts, estilos, comentários e tabelas
         * - Decodifica entidades HTML nomeadas e numéricas (&quot;, &amp;, &ndash;, etc.)
         * - Remove links e marcações markdown
         * - Remove repetições do título no início do corpo da matéria
         * - Normaliza espaçamentos e quebras
         */
        fun formatFeedText(raw: String?, titleToDeduplicate: String? = null): String {
            if (raw.isNullOrBlank()) return ""

            var text = raw
                .replace("<![CDATA[", "")
                .replace("]]>", "")
                .replace("]]", "")
                .replace("]>", "")
                .replace(Regex("<!--[\\s\\S]*?-->"), " ")
                .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
                .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
                .replace(Regex("<table[\\s\\S]*?</table>", RegexOption.IGNORE_CASE), " ")

            text = text
                .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), " ")
                .replace(Regex("</?(?:p|div|li|tr|h[1-6]|blockquote)[^>]*>", RegexOption.IGNORE_CASE), " ")
                .replace(Regex("<[^>]+>"), " ")

            if (text.contains("&amp;")) {
                text = text.replace("&amp;", "&")
            }
            text = try {
                android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
            } catch (_: Throwable) {
                text
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'")
                    .replace("&apos;", "'")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&nbsp;", " ")
                    .replace("&ndash;", "–")
                    .replace("&mdash;", "—")
                    .replace("&hellip;", "…")
                    .replace("&ldquo;", "“")
                    .replace("&rdquo;", "”")
                    .replace("&lsquo;", "‘")
                    .replace("&rsquo;", "’")
                    .replace("&aacute;", "á")
                    .replace("&eacute;", "é")
                    .replace("&iacute;", "í")
                    .replace("&oacute;", "ó")
                    .replace("&uacute;", "ú")
                    .replace("&atilde;", "ã")
                    .replace("&otilde;", "õ")
                    .replace("&ccedil;", "ç")
                    .replace("&Aacute;", "Á")
                    .replace("&Eacute;", "É")
                    .replace("&Iacute;", "Í")
                    .replace("&Oacute;", "Ó")
                    .replace("&Uacute;", "Ú")
                    .replace("&Atilde;", "Ã")
                    .replace("&Otilde;", "Õ")
                    .replace("&Ccedil;", "Ç")
                    .replace("&ecirc;", "ê")
                    .replace("&ocirc;", "ô")
                    .replace("&Ecirc;", "Ê")
                    .replace("&Ocirc;", "Ô")
            }

            text = text
                .replace('\u00A0', ' ')
                .replace("\u200B", "")
                .replace(Regex("\\[link\\]|\\[comments\\]", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")
                .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
                .replace(Regex("\\*([^*]+)\\*"), "$1")

            text = text.replace(Regex("\\s+"), " ").trim()

            if (!titleToDeduplicate.isNullOrBlank()) {
                val cleanTitle = titleToDeduplicate.trim()
                if (text.startsWith(cleanTitle, ignoreCase = true)) {
                    text = text.substring(cleanTitle.length).trim()
                }
            }

            text = text.replace(Regex("^[\\s\\-–—:•·|/]+"), "").trim()
            return text
        }
    }

    private var lastFetchTime: Long = 0L

    suspend fun fetchFeed(forceRefresh: Boolean = false): FeedResult {
        val sources = preferences.getCustomRssSources().filter { it.isEnabled }
        if (sources.isEmpty()) {
            return FeedResult.Empty
        }

        val cachedJson = preferences.getFeedCachedJson()
        val cacheValid = !forceRefresh &&
            cachedJson != null &&
            (System.currentTimeMillis() - lastFetchTime) < CACHE_TTL_MS

        if (cacheValid && cachedJson != null) {
            val cached = parseCachedPosts(cachedJson)
            if (cached.isNotEmpty()) return FeedResult.Success(cached)
        }

        return try {
            val posts = mutableListOf<FeedPost>()

            coroutineScope {
                val deferredList = sources.map { source ->
                    async(Dispatchers.IO) {
                        runCatching {
                            val xml = httpGet(source.url)
                            if (xml != null && (xml.contains("<entry") || xml.contains("<item"))) {
                                parseStandardRss(xml, source.name, FeedSource.NEWS)
                            } else {
                                emptyList()
                            }
                        }.getOrDefault(emptyList())
                    }
                }
                deferredList.awaitAll().forEach { posts.addAll(it) }
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

    private fun httpGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json, application/xml, text/xml, application/atom+xml, */*")
                setRequestProperty("Accept-Encoding", "gzip")
                setRequestProperty("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.w(TAG, "HTTP $responseCode from $urlString")
                return null
            }

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

    private fun parseStandardRss(xml: String, defaultAuthor: String, source: FeedSource): List<FeedPost> {
        val posts = mutableListOf<FeedPost>()
        val isAtom = xml.contains("<feed") || xml.contains("<entry")
        val itemRegex = if (isAtom) Regex("<entry[\\s\\S]*?</entry>") else Regex("<item[\\s\\S]*?</item>")
        val titleRegex = Regex("<title(?:[^>]*)>([\\s\\S]*?)</title>")
        val linkRegex = if (isAtom) Regex("<link[^>]+href=[\"']([^\"']+)[\"']") else Regex("<link>([^<]+)</link>")
        val descRegex = Regex("<(?:description|summary|content:encoded|content)(?:[^>]*)>([\\s\\S]*?)</(?:description|summary|content:encoded|content)>")
        val pubDateRegex = Regex("<(?:pubDate|updated|dc:date)>([^<]+)</(?:pubDate|updated|dc:date)>")
        val authorRegex = Regex("<(?:author|dc:creator)>\\s*(?:<name>)?([^<]+)(?:</name>)?</(?:author|dc:creator)>")
        val thumbRegex = Regex("(?:url=[\"']([^\"']+\\.(?:jpg|jpeg|png|webp)[^\"']*)[\"']|<media:content[^>]+url=[\"']([^\"']+)[\"']|<img[^>]+src=[\"']([^\"']+)[\"'])")

        for (match in itemRegex.findAll(xml)) {
            val item = match.value
            val rawTitle = titleRegex.find(item)?.groupValues?.getOrNull(1) ?: continue
            val title = formatFeedText(rawTitle)
            if (title.isBlank()) continue

            val link = linkRegex.find(item)?.groupValues?.getOrNull(1)?.trim() ?: ""
            val rawDesc = descRegex.find(item)?.groupValues?.getOrNull(1) ?: ""
            val body = formatFeedText(rawDesc, titleToDeduplicate = title).take(450)
            val author = authorRegex.find(item)?.groupValues?.getOrNull(1)?.trim()?.let { formatFeedText(it) }?.ifBlank { defaultAuthor } ?: defaultAuthor

            val dateStr = pubDateRegex.find(item)?.groupValues?.getOrNull(1)?.trim() ?: ""
            val timestamp = parseRssDate(dateStr)

            val thumb = thumbRegex.find(item)?.let { m ->
                m.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() }
                    ?: m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }
                    ?: m.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }
            }

            val id = (link.ifBlank { title }).hashCode().toString()

            posts.add(
                FeedPost(
                    id = "rss_$id",
                    source = source,
                    author = author,
                    authorHandle = "",
                    title = title,
                    body = body,
                    subreddit = null,
                    score = 0,
                    commentCount = 0,
                    url = link,
                    thumbnailUrl = thumb,
                    createdAt = timestamp
                )
            )
        }
        return posts
    }

    private fun parseRssDate(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        val formats = listOf(
            java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US),
            java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        )
        for (format in formats) {
            try {
                val parsed = format.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
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
                val sourceName = obj.optString("source", "NEWS")
                val source = try {
                    FeedSource.valueOf(sourceName)
                } catch (_: Exception) {
                    FeedSource.NEWS
                }

                val rawTitle = obj.optString("title").takeIf { it != "null" && it.isNotBlank() }
                val rawBody = obj.optString("body", "")
                val cleanTitle = rawTitle?.let { formatFeedText(it) }?.takeIf { it.isNotBlank() }
                val cleanBody = formatFeedText(rawBody, cleanTitle)

                posts.add(
                    FeedPost(
                        id = obj.optString("id", "cached_$i"),
                        source = source,
                        author = obj.optString("author", ""),
                        authorHandle = obj.optString("authorHandle", ""),
                        title = cleanTitle,
                        body = cleanBody,
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
