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
    data class Success(val answer: String, val provider: String = "Groq IA") : AiAnswerResult
    data class Error(val message: String, val needsKey: Boolean = false) : AiAnswerResult
}

/**
 * Motor de inteligência artificial nativo e ultra-rápido.
 * Suporta Groq (100% gratuito, inferência instantânea com Llama 3.3 70B) e Google Gemini.
 */
class AiEngine {

    companion object {
        private const val TAG = "AiEngine"
        private const val GROQ_CHAT_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
        private const val GROQ_MODELS_ENDPOINT = "https://api.groq.com/openai/v1/models"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
        private const val SYSTEM_PROMPT = "Responda de forma extremamente concisa, direta e objetiva em no máximo 2 ou 3 frases em Português do Brasil. Sem introduções vazias ou saudações."

        // Modelos candidatos ordenados por prioridade (desempenho + disponibilidade em tiers gratuitos/developer)
        private val CANDIDATE_GROQ_MODELS = listOf(
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "llama-3.1-8b-instant",
            "llama-3.3-70b-versatile",
            "meta-llama/llama-4-scout-17b-16e-instruct",
            "qwen/qwen3.6-27b",
            "llama-3.2-3b-preview",
            "llama-3.2-1b-preview",
            "llama3-8b-8192"
        )
    }

    private val answerCache = ConcurrentHashMap<String, String>()

    @Volatile
    private var resolvedGroqModel: String? = null

    fun resetGroqModel() {
        resolvedGroqModel = null
        answerCache.clear()
    }

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
            return@withContext AiAnswerResult.Success(cached, if (provider.equals("GEMINI", true)) "Gemini Flash" else "Groq IA")
        }

        val cleanKey = apiKey.trim().removeSurrounding("\"").removeSurrounding("'")
        if (cleanKey.isBlank()) {
            val providerName = if (provider.equals("GEMINI", true)) "Gemini" else "Groq"
            return@withContext AiAnswerResult.Error(
                message = "Adicione sua chave gratuita do $providerName em Configurações > Busca para ativar respostas inteligentes.",
                needsKey = true
            )
        }

        if (provider.equals("GEMINI", true)) {
            queryGemini(cleanPrompt, cleanKey, cacheKey)
        } else {
            queryGroq(cleanPrompt, cleanKey, cacheKey)
        }
    }

    private fun discoverBestGroqModel(apiKey: String): String? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GROQ_MODELS_ENDPOINT)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                instanceFollowRedirects = true
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode in 200..299) {
                val text = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val root = JSONObject(text)
                val data = root.optJSONArray("data")
                if (data != null && data.length() > 0) {
                    val availableIds = mutableSetOf<String>()
                    for (i in 0 until data.length()) {
                        val item = data.optJSONObject(i)
                        val id = item?.optString("id")
                        if (!id.isNullOrBlank()) availableIds.add(id)
                    }
                    // Escolhe o melhor candidato presente na conta
                    for (candidate in CANDIDATE_GROQ_MODELS) {
                        if (availableIds.contains(candidate)) {
                            Log.i(TAG, "Modelo Groq selecionado dinamicamente: $candidate")
                            return candidate
                        }
                    }
                    // Se nenhum dos candidatos exatos estiver presente, busca qualquer modelo de chat disponível
                    val fallback = availableIds.firstOrNull { id ->
                        val lower = id.lowercase()
                        !lower.contains("whisper") &&
                        !lower.contains("tts") &&
                        !lower.contains("orpheus") &&
                        !lower.contains("guard") &&
                        !lower.contains("embed") &&
                        !lower.contains("moderation")
                    }
                    if (fallback != null) {
                        Log.i(TAG, "Modelo Groq fallback genérico: $fallback")
                        return fallback
                    }
                }
            } else {
                val errStream = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                Log.w(TAG, "Falha ao listar modelos do Groq (HTTP ${connection.responseCode}): $errStream")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exceção ao listar modelos do Groq: ${e.message}")
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private sealed interface GroqCallResult {
        data class Success(val answer: String) : GroqCallResult
        data class AuthError(val message: String) : GroqCallResult
        data class ModelUnavailable(val message: String) : GroqCallResult
        data class OtherError(val message: String, val needsKey: Boolean = false) : GroqCallResult
    }

    private fun queryGroq(prompt: String, apiKey: String, cacheKey: String): AiAnswerResult {
        // 1. Resolve dinamicamente se necessário
        if (resolvedGroqModel == null) {
            resolvedGroqModel = discoverBestGroqModel(apiKey)
        }

        // 2. Monta lista de tentativas
        val modelsToTry = LinkedHashSet<String>().apply {
            resolvedGroqModel?.let { add(it) }
            addAll(CANDIDATE_GROQ_MODELS)
        }.toList()

        var lastErrorMessage: String? = null
        var lastNeedsKey = false

        for (model in modelsToTry) {
            val result = executeGroqChat(prompt, apiKey, model)
            when (result) {
                is GroqCallResult.Success -> {
                    resolvedGroqModel = model
                    answerCache[cacheKey] = result.answer
                    return AiAnswerResult.Success(result.answer, "Groq IA")
                }
                is GroqCallResult.AuthError -> {
                    resolvedGroqModel = null
                    return AiAnswerResult.Error(result.message, needsKey = true)
                }
                is GroqCallResult.ModelUnavailable -> {
                    Log.w(TAG, "Modelo Groq '$model' indisponível (${result.message}). Tentando próximo modelo...")
                    if (resolvedGroqModel == model) {
                        resolvedGroqModel = null
                    }
                    lastErrorMessage = result.message
                }
                is GroqCallResult.OtherError -> {
                    lastErrorMessage = result.message
                    lastNeedsKey = result.needsKey
                }
            }
        }

        return AiAnswerResult.Error(
            message = lastErrorMessage ?: "Erro de conexão com o Groq.",
            needsKey = lastNeedsKey
        )
    }

    private fun executeGroqChat(prompt: String, apiKey: String, model: String): GroqCallResult {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GROQ_CHAT_ENDPOINT)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 7000
                readTimeout = 7000
                doOutput = true
                instanceFollowRedirects = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Accept", "application/json")
            }

            val body = JSONObject().apply {
                put("model", model)
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
                val responseText = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val rootJson = JSONObject(responseText)
                val choices = rootJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.optJSONObject("message")
                    val answerText = message?.optString("content")?.trim()

                    if (!answerText.isNullOrBlank()) {
                        return GroqCallResult.Success(answerText)
                    }
                }
                return GroqCallResult.OtherError("Não foi possível processar a resposta do Groq.")
            } else {
                val errorStream = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                Log.w(TAG, "Erro na API Groq ($model, HTTP $responseCode): $errorStream")
                val apiErrorMsg = parseGroqErrorMessage(errorStream)

                val isAuthError = responseCode == 401 || responseCode == 403
                if (isAuthError) {
                    return GroqCallResult.AuthError(
                        apiErrorMsg ?: "Chave de API do Groq inválida ou não autorizada."
                    )
                }

                val isModelIssue = responseCode == 404 ||
                    (apiErrorMsg != null && (
                        apiErrorMsg.contains("model", ignoreCase = true) ||
                        apiErrorMsg.contains("decommissioned", ignoreCase = true) ||
                        apiErrorMsg.contains("not found", ignoreCase = true)
                    ))

                if (isModelIssue) {
                    return GroqCallResult.ModelUnavailable(
                        apiErrorMsg ?: "Modelo $model não encontrado (HTTP 404)"
                    )
                }

                if (responseCode == 429) {
                    return GroqCallResult.OtherError("Limite de requisições do Groq atingido. Tente novamente em instantes.")
                }

                return GroqCallResult.OtherError(
                    apiErrorMsg ?: "Erro de conexão com o Groq (HTTP $responseCode)."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao consultar Groq com modelo $model: ${e.message}", e)
            return GroqCallResult.OtherError("Falha de rede ao consultar o Groq: ${e.localizedMessage ?: "Tempo esgotado"}")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseGroqErrorMessage(errorJson: String?): String? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            val root = JSONObject(errorJson)
            val err = root.optJSONObject("error")
            err?.optString("message")?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseGeminiErrorMessage(errorJson: String?): String? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            val root = JSONObject(errorJson)
            val err = root.optJSONObject("error")
            err?.optString("message")?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
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
                val responseText = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
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
                val errorStream = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                Log.w(TAG, "Erro na API Gemini (HTTP $responseCode): $errorStream")
                val isInvalidKey = responseCode == 400 || responseCode == 403
                val apiErrorMsg = parseGeminiErrorMessage(errorStream)
                return AiAnswerResult.Error(
                    message = if (isInvalidKey) {
                        apiErrorMsg ?: "Chave de API do Gemini inválida ou sem permissão."
                    } else {
                        apiErrorMsg ?: "Erro de conexão com o Gemini (HTTP $responseCode)."
                    },
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
