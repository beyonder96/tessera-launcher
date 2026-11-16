package com.tessera.launcher.data.helper

import com.tessera.launcher.data.model.CheckoutSession
import com.tessera.launcher.data.model.DEFAULT_PRO_PLANS
import com.tessera.launcher.data.model.LicenseInfo
import com.tessera.launcher.data.model.PaymentStatusResult
import com.tessera.launcher.data.model.PlanOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Cliente HTTP leve (sem dependências externas) para conectar o Tessera Launcher
 * ao backend serverless gratuito em Cloudflare Workers + D1 (Mercado Pago).
 *
 * Nenhuma chave secreta do Mercado Pago fica neste cliente — apenas a URL pública do Worker.
 */
class PaymentServiceClient(
    private val baseUrl: String = BACKEND_BASE_URL
) {

    companion object {
        // URL pública do Cloudflare Worker publicado em backend/
        const val BACKEND_BASE_URL = "https://tessera-pay.tessera-kenned.workers.dev"

        private const val PLACEHOLDER_HOST = "seu-usuario.workers.dev"
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000

        internal fun isBackendConfigured(url: String = BACKEND_BASE_URL): Boolean {
            val clean = url.trim().trimEnd('/')
            return clean.startsWith("https://") && !clean.contains(PLACEHOLDER_HOST)
        }

        internal fun buildCreateCheckoutBody(planId: String, email: String, method: String): String {
            return buildString {
                append('{')
                append("\"planId\":\"").append(escapeJson(planId.trim())).append("\",")
                append("\"email\":\"").append(escapeJson(email.trim().lowercase())).append("\",")
                append("\"method\":\"").append(escapeJson(method.trim().lowercase())).append('"')
                append('}')
            }
        }

        internal fun parsePlansResponse(jsonStr: String): List<PlanOption> {
            val root = SimpleJson.parse(jsonStr) as? Map<*, *> ?: return DEFAULT_PRO_PLANS
            val plansArray = root["plans"] as? List<*> ?: return DEFAULT_PRO_PLANS
            val parsed = plansArray.mapNotNull { entry ->
                val obj = entry as? Map<*, *> ?: return@mapNotNull null
                val id = obj.str("id") ?: return@mapNotNull null
                val title = obj.str("title") ?: return@mapNotNull null
                val subtitle = obj.str("subtitle").orEmpty()
                val priceFormatted = obj.str("priceFormatted") ?: return@mapNotNull null
                val badge = obj.str("badge")?.takeIf { it.isNotBlank() }
                val durationDays = obj.numLong("durationDays")?.toInt()
                PlanOption(
                    id = id,
                    title = title,
                    subtitle = subtitle,
                    priceFormatted = priceFormatted,
                    badge = badge,
                    durationDays = durationDays
                )
            }
            return parsed.ifEmpty { DEFAULT_PRO_PLANS }
        }

        internal fun parseCheckoutSession(jsonStr: String): CheckoutSession {
            val root = SimpleJson.parse(jsonStr) as? Map<*, *>
                ?: throw IllegalArgumentException("Resposta JSON inválida do servidor.")
            val errorMsg = root.str("error")
            if (!errorMsg.isNullOrBlank()) {
                throw IllegalStateException(errorMsg)
            }
            val paymentId = root.str("paymentId")
                ?: throw IllegalStateException("Servidor não retornou paymentId.")
            return CheckoutSession(
                paymentId = paymentId,
                externalReference = root.str("externalReference"),
                planId = root.str("planId") ?: "pro_lifetime",
                method = root.str("method") ?: "pix",
                pixCopyPaste = root.str("pixCopyPaste")?.takeIf { it.isNotBlank() },
                qrCodeBase64 = root.str("qrCodeBase64")?.takeIf { it.isNotBlank() },
                checkoutUrl = root.str("checkoutUrl")?.takeIf { it.isNotBlank() },
                status = root.str("status") ?: "pending"
            )
        }

        internal fun parseStatusResponse(jsonStr: String): PaymentStatusResult {
            val root = SimpleJson.parse(jsonStr) as? Map<*, *>
                ?: throw IllegalArgumentException("Resposta JSON inválida do servidor.")
            val errorMsg = root.str("error")
            if (!errorMsg.isNullOrBlank()) {
                throw IllegalStateException(errorMsg)
            }

            val status = root.str("status") ?: "pending"
            val isProActive = root["isProActive"] as? Boolean ?: false
            val message = root.str("message")

            val license = if (isProActive) {
                LicenseInfo(
                    isProActive = true,
                    activePlanId = root.str("activePlanId"),
                    licensedEmail = root.str("licensedEmail"),
                    licenseToken = root.str("licenseToken"),
                    activatedAt = root.numLong("activatedAt") ?: 0L,
                    expiresAt = root.numLong("expiresAt") ?: 0L
                )
            } else {
                null
            }

            return PaymentStatusResult(
                status = status,
                licenseInfo = license,
                message = message
            )
        }

        private fun escapeJson(value: String): String = buildString(value.length) {
            for (ch in value) {
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(ch)
                }
            }
        }
    }

    private val cleanBaseUrl: String get() = baseUrl.trim().trimEnd('/')

    suspend fun fetchPlans(): Result<List<PlanOption>> = withContext(Dispatchers.IO) {
        if (!isBackendConfigured(cleanBaseUrl)) {
            return@withContext Result.success(DEFAULT_PRO_PLANS)
        }
        runCatching {
            val body = executeGet("$cleanBaseUrl/api/plans")
            parsePlansResponse(body)
        }.recover {
            DEFAULT_PRO_PLANS
        }
    }

    suspend fun createCheckout(
        planId: String,
        email: String,
        method: String
    ): Result<CheckoutSession> = withContext(Dispatchers.IO) {
        if (!isBackendConfigured(cleanBaseUrl)) {
            return@withContext Result.failure(
                IllegalStateException(
                    "Backend não configurado. Publique a pasta backend/ na Cloudflare e atualize BACKEND_BASE_URL em PaymentServiceClient.kt."
                )
            )
        }
        runCatching {
            val payload = buildCreateCheckoutBody(planId, email, method)
            val responseBody = executePost("$cleanBaseUrl/api/checkout/create", payload)
            parseCheckoutSession(responseBody)
        }
    }

    suspend fun checkPaymentStatus(paymentId: String): Result<PaymentStatusResult> = withContext(Dispatchers.IO) {
        if (!isBackendConfigured(cleanBaseUrl)) {
            return@withContext Result.failure(
                IllegalStateException("Backend de pagamentos ainda não configurado.")
            )
        }
        runCatching {
            val encodedId = URLEncoder.encode(paymentId.trim(), "UTF-8")
            val responseBody = executeGet("$cleanBaseUrl/api/checkout/status?paymentId=$encodedId")
            parseStatusResponse(responseBody)
        }
    }

    suspend fun restorePurchasesByEmail(email: String): Result<PaymentStatusResult> = withContext(Dispatchers.IO) {
        if (!isBackendConfigured(cleanBaseUrl)) {
            return@withContext Result.failure(
                IllegalStateException("Backend de pagamentos ainda não configurado.")
            )
        }
        runCatching {
            val encodedEmail = URLEncoder.encode(email.trim().lowercase(), "UTF-8")
            val responseBody = executeGet("$cleanBaseUrl/api/checkout/status?email=$encodedEmail")
            parseStatusResponse(responseBody)
        }
    }

    private fun executeGet(urlString: String): String {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
            }
            return readHttpResponse(connection)
        } finally {
            connection?.disconnect()
        }
    }

    private fun executePost(urlString: String, jsonBody: String): String {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }
            return readHttpResponse(connection)
        } finally {
            connection?.disconnect()
        }
    }

    private fun readHttpResponse(connection: HttpURLConnection): String {
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            val serverError = runCatching {
                (SimpleJson.parse(text) as? Map<*, *>)?.str("error")
            }.getOrNull()
            throw IllegalStateException(
                serverError?.takeIf { it.isNotBlank() } ?: "Erro HTTP $code ao conectar ao serviço de pagamento."
            )
        }
        return text
    }
}

private fun Map<*, *>.str(key: String): String? = this[key] as? String

private fun Map<*, *>.numLong(key: String): Long? = (this[key] as? Number)?.toLong()

/**
 * Parser JSON minimalista em Kotlin puro para funcionar de forma idêntica no Android
 * e em testes unitários JVM sem depender de mocks da biblioteca org.json.
 */
internal class SimpleJson private constructor(private val input: String) {
    private var index = 0

    companion object {
        fun parse(json: String): Any? {
            val parser = SimpleJson(json)
            val value = parser.readValue()
            parser.skipWhitespace()
            return value
        }
    }

    private fun readValue(): Any? {
        skipWhitespace()
        if (index >= input.length) throw IllegalArgumentException("JSON inesperado no final.")
        return when (val ch = input[index]) {
            '{' -> readObject()
            '[' -> readArray()
            '"' -> readString()
            't' -> readLiteral("true", true)
            'f' -> readLiteral("false", false)
            'n' -> readLiteral("null", null)
            '-', in '0'..'9' -> readNumber()
            else -> throw IllegalArgumentException("Token JSON inválido '$ch' na posição $index.")
        }
    }

    private fun readObject(): Map<String, Any?> {
        index++ // '{'
        skipWhitespace()
        val map = LinkedHashMap<String, Any?>()
        if (index < input.length && input[index] == '}') {
            index++
            return map
        }
        while (index < input.length) {
            skipWhitespace()
            if (input[index] != '"') {
                throw IllegalArgumentException("Chave JSON esperada na posição $index.")
            }
            val key = readString()
            skipWhitespace()
            if (index >= input.length || input[index] != ':') {
                throw IllegalArgumentException("':' esperado após chave JSON na posição $index.")
            }
            index++
            val value = readValue()
            map[key] = value
            skipWhitespace()
            if (index < input.length && input[index] == ',') {
                index++
            } else if (index < input.length && input[index] == '}') {
                index++
                return map
            } else {
                throw IllegalArgumentException("',' ou '}' esperado na posição $index.")
            }
        }
        throw IllegalArgumentException("Objeto JSON não fechado.")
    }

    private fun readArray(): List<Any?> {
        index++ // '['
        skipWhitespace()
        val list = ArrayList<Any?>()
        if (index < input.length && input[index] == ']') {
            index++
            return list
        }
        while (index < input.length) {
            list.add(readValue())
            skipWhitespace()
            if (index < input.length && input[index] == ',') {
                index++
            } else if (index < input.length && input[index] == ']') {
                index++
                return list
            } else {
                throw IllegalArgumentException("',' ou ']' esperado na posição $index.")
            }
        }
        throw IllegalArgumentException("Array JSON não fechado.")
    }

    private fun readString(): String {
        index++ // '"'
        val sb = StringBuilder()
        while (index < input.length) {
            val ch = input[index++]
            when (ch) {
                '"' -> return sb.toString()
                '\\' -> {
                    if (index >= input.length) throw IllegalArgumentException("Escape JSON incompleto.")
                    when (val esc = input[index++]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (index + 4 > input.length) throw IllegalArgumentException("Unicode JSON inválido.")
                            val hex = input.substring(index, index + 4)
                            sb.append(hex.toInt(16).toChar())
                            index += 4
                        }
                        else -> throw IllegalArgumentException("Escape JSON desconhecido: \\$esc")
                    }
                }
                else -> sb.append(ch)
            }
        }
        throw IllegalArgumentException("String JSON não fechada.")
    }

    private fun readNumber(): Number {
        val start = index
        if (input[index] == '-') index++
        while (index < input.length && input[index] in '0'..'9') index++
        var isFloat = false
        if (index < input.length && input[index] == '.') {
            isFloat = true
            index++
            while (index < input.length && input[index] in '0'..'9') index++
        }
        if (index < input.length && (input[index] == 'e' || input[index] == 'E')) {
            isFloat = true
            index++
            if (index < input.length && (input[index] == '+' || input[index] == '-')) index++
            while (index < input.length && input[index] in '0'..'9') index++
        }
        val numStr = input.substring(start, index)
        return if (isFloat) numStr.toDouble() else numStr.toLong()
    }

    private fun readLiteral(expected: String, value: Any?): Any? {
        if (!input.startsWith(expected, index)) {
            throw IllegalArgumentException("Literal JSON esperado '$expected' na posição $index.")
        }
        index += expected.length
        return value
    }

    private fun skipWhitespace() {
        while (index < input.length && input[index].isWhitespace()) {
            index++
        }
    }
}
