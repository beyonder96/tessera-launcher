package com.tessera.launcher.data.helper

import com.tessera.launcher.data.model.FeedPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Helper class for AI features in the Feed.
 * Currently simulates an AI summarization engine.
 * In the future, this can be integrated with Gemini Nano (AICore) or a remote API.
 */
class AiSummaryHelper {
    
    suspend fun summarizePost(post: FeedPost): String = withContext(Dispatchers.Default) {
        // Simulate network/inference delay
        delay(500)
        
        val textLength = post.body.length
        
        if (textLength < 50) {
            return@withContext "Resumo: Texto curto."
        }
        
        val keywords = listOf("Android", "Google", "Kotlin", "Compose", "App", "AI", "Update")
        val found = keywords.filter { post.body.contains(it, ignoreCase = true) }
        
        if (found.isNotEmpty()) {
            return@withContext "IA: Aborda tópicos sobre ${found.joinToString(", ")}."
        }
        
        // Generic fallback summary
        "IA: Discussão geral sobre ${post.subreddit ?: post.authorHandle}."
    }
}
