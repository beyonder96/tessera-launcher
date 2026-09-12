package com.tessera.launcher.data.model

/**
 * Modelo unificado de post para o Feed Social.
 * Suporta Reddit, Bluesky e Twitter/X.
 */
data class FeedPost(
    val id: String,
    val source: FeedSource,
    val author: String,
    val authorHandle: String,
    val title: String?,
    val body: String,
    val subreddit: String? = null,
    val score: Int = 0,
    val commentCount: Int = 0,
    val url: String,
    val thumbnailUrl: String? = null,
    val createdAt: Long,
    val aiSummary: String? = null,
    val aiCategory: String? = null
) {
    val relativeTime: String
        get() {
            val diff = System.currentTimeMillis() - createdAt
            val minutes = diff / 60_000
            val hours = minutes / 60
            val days = hours / 24
            return when {
                minutes < 1 -> "agora"
                minutes < 60 -> "${minutes}min"
                hours < 24 -> "${hours}h"
                days < 7 -> "${days}d"
                else -> "${days / 7}sem"
            }
        }

    val sourceLabel: String
        get() = when (source) {
            FeedSource.REDDIT -> "r/${subreddit?.removePrefix("/r/")?.removePrefix("r/") ?: "reddit"}"
            FeedSource.BLUESKY -> "@${authorHandle.removePrefix("@")}"
        }
}

enum class FeedSource(val displayName: String) {
    REDDIT("Reddit"),
    BLUESKY("Bluesky")
}
