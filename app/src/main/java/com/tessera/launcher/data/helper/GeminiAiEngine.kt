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
    data class Success(val answer: String, val model: String = "Gemini Flash") : AiAnswerResult
    data class Error(val message: String, val needsKey: Boolean = false) : AiAnswerResult
}

/**
 * Motor de respostas rápidas via Google Gemini REST API.
 * 100% nativo, sem bibliotecas pesadas de terceiros, com timeout seguro e cache em memória.
 */
class GeminiAiEngine {

    companion object {
        private const val TAG = "GeminiAiEngine"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    }

    private val answerCache = ConcurrentHashMap<String, String>()

    suspend fun query(prompt: String, apiKey: String): AiAnswerResult = withContext(Dispatchers.IO) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) {
            return@withContext AiAnswerResult.Error("Pergunta vazia.")
        }

        // 1. Verificar cache local em memória
        val cached = answerCache[cleanPrompt.lowercase()]
        if (cached != null) {
            return@withContext AiAnswerResult.Success(cached)
        }

        // 2. Validação da chave de API
        if (apiKey.isBlank()) {
            return@withContext AiAnswerResult.Error(
                message = "Adicione sua chave gratuita do Gemini em Configurações para ativar respostas com IA diretamente na busca.",
                needsKey = true
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val endpoint = "$BASE_URL?key=$apiKey"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            // Instrução de sistema para manter a resposta ultra concisa e minimalista
            val systemInstruction = "Responda de forma extremamente concisa, direta e objetiva em no máximo 2 ou 3 frases em Português do Brasil. Sem introduções vazias ou saudações."
            val fullPrompt = "$systemInstruction\n\nPergunta: $cleanPrompt"

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
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseText)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val answerText = parts?.optJSONObject(0)?.optString("text")?.trim()

                    if (!answerText.isNullOrBlank()) {
                        answerCache[cleanPrompt.lowercase()] = answerText
                        return@withContext AiAnswerResult.Success(answerText)
                    }
                }
                return@withContext AiAnswerResult.Error("Não foi possível processar a resposta do Gemini.")
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.w(TAG, "Erro na API Gemini (HTTP $responseCode): $errorStream")
                val isInvalidKey = responseCode == 400 || responseCode == 403
                return@withContext AiAnswerResult.Error(
                    message = if (isInvalidKey) "Chave de API do Gemini inválida ou sem permissão." else "Erro de conexão com o Gemini (HTTP $responseCode).",
                    needsKey = isInvalidKey
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao consultar Gemini: ${e.message}", e)
            return@withContext AiAnswerResult.Error("Falha de rede ao consultar o Gemini: ${e.localizedMessage ?: "Tempo esgotado"}")
        } finally {
            connection?.disconnect()
        }
    }
}
