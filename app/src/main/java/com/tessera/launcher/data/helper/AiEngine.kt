package com.tessera.launcher.data.helper

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

sealed interface AiAnswerResult {
    data class Success(val answer: String, val provider: String = "Groq (Llama 3.3)") : AiAnswerResult
    data class Error(val message: String, val needsKey: Boolean = false) : AiAnswerResult
}

/**
 * Motor de inteligência artificial nativo e ultra-rápido.
 * Suporta Groq (100% gratuito, inferência instantânea com Llama 3.3 70B) e Google Gemini.
 */
class AiEngine {

    companion object {
        private const val TAG = "AiEngine"
        private const val GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
        private const val GROQ_MODEL = "llama-3.3-70b-versatile"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
        private const val SYSTEM_PROMPT = "Responda de forma extremamente concisa, direta e objetiva em no máximo 2 ou 3 frases em Português do Brasil. Sem introduções vazias ou saudações."
    }

    private val answerCache = ConcurrentHashMap<String, String>()

    suspend fun query(
        prompt: String,
        provider: String = "GROQ",
        apiKey: String
    ): AiAnswerResult = withContext(Dispatchers.IO) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) {
            return@withContext AiAnswerResult.Error("Pergunta vazia.")
        }

        // Cache em memória
        val cacheKey = "${provider}_${cleanPrompt.lowercase()}"
        val cached = answerCache[cacheKey]
        if (cached != null) {
            return@withContext AiAnswerResult.Success(cached, if (provider.equals("GEMINI", true)) "Gemini Flash" else "Groq Llama 3.3")
        }

        if (apiKey.isBlank()) {
            val providerName = if (provider.equals("GEMINI", true)) "Gemini" else "Groq"
            return@withContext AiAnswerResult.Error(
                message = "Adicione sua chave gratuita do $providerName em Configurações > Busca para ativar respostas inteligentes.",
                needsKey = true
            )
        }

        if (provider.equals("GEMINI", true)) {
            queryGemini(cleanPrompt, apiKey, cacheKey)
        } else {
            queryGroq(cleanPrompt, apiKey, cacheKey)
        }
    }

    private fun queryGroq(prompt: String, apiKey: String, cacheKey: String): AiAnswerResult {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GROQ_ENDPOINT)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 7000
                readTimeout = 7000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                setRequestProperty("Accept", "application/json")
            }

            val body = JSONObject().apply {
                put("model", GROQ_MODEL)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messages)
                put("temperature", 0.5)
                put("max_tokens", 220)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseText)
                val choices = rootJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.optJSONObject("message")
                    val answerText = message?.optString("content")?.trim()

                    if (!answerText.isNullOrBlank()) {
                        answerCache[cacheKey] = answerText
                        return AiAnswerResult.Success(answerText, "Groq Llama 3.3")
                    }
                }
                return AiAnswerResult.Error("Não foi possível processar a resposta do Groq.")
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.w(TAG, "Erro na API Groq (HTTP $responseCode): $errorStream")
                val isAuthError = responseCode == 401 || responseCode == 403
                return AiAnswerResult.Error(
                    message = if (isAuthError) "Chave de API do Groq inválida ou não autorizada." else "Erro de conexão com o Groq (HTTP $responseCode).",
                    needsKey = isAuthError
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao consultar Groq: ${e.message}", e)
            return AiAnswerResult.Error("Falha de rede ao consultar o Groq: ${e.localizedMessage ?: "Tempo esgotado"}")
        } finally {
            connection?.disconnect()
        }
    }

    private fun queryGemini(prompt: String, apiKey: String, cacheKey: String): AiAnswerResult {
        var connection: HttpURLConnection? = null
        try {
            val endpoint = "$GEMINI_BASE_URL?key=${apiKey.trim()}"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 7000
                readTimeout = 7000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val fullPrompt = "$SYSTEM_PROMPT\n\nPergunta: $prompt"

            val body = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", fullPrompt)
                            }
                            put(partObj)
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseText)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val answerText = parts?.optJSONObject(0)?.optString("text")?.trim()

                    if (!answerText.isNullOrBlank()) {
                        answerCache[cacheKey] = answerText
                        return AiAnswerResult.Success(answerText, "Gemini Flash")
                    }
                }
                return AiAnswerResult.Error("Não foi possível processar a resposta do Gemini.")
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.w(TAG, "Erro na API Gemini (HTTP $responseCode): $errorStream")
                val isInvalidKey = responseCode == 400 || responseCode == 403
                return AiAnswerResult.Error(
                    message = if (isInvalidKey) "Chave de API do Gemini inválida ou sem permissão." else "Erro de conexão com o Gemini (HTTP $responseCode).",
                    needsKey = isInvalidKey
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao consultar Gemini: ${e.message}", e)
            return AiAnswerResult.Error("Falha de rede ao consultar o Gemini: ${e.localizedMessage ?: "Tempo esgotado"}")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun summarizePost(
        title: String,
        body: String,
        provider: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        val textToSummarize = "$title\n$body".trim()
        if (textToSummarize.length < 50) return@withContext "Resumo: Conteúdo breve."
        if (apiKey.isBlank()) {
            return@withContext "IA: ${title.ifBlank { body.take(60) }}..."
        }

        val prompt = "Resuma o seguinte post em uma única frase ultra concisa em português:\n\n$textToSummarize"
        val result = query(prompt, provider, apiKey)
        if (result is AiAnswerResult.Success) {
            result.answer
        } else {
            "IA: ${title.ifBlank { body.take(60) }}"
        }
    }
}
