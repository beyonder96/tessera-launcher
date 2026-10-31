package com.tessera.launcher.data.helper

import com.tessera.launcher.ui.components.MathEvaluator
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class OmniCategory(val displayName: String, val emoji: String) {
    CURRENCY("Moeda", "💵"),
    UNIT("Medida", "📐"),
    PERCENTAGE("Porcentagem", "📊"),
    MATH("Cálculo", "🧮"),
    TIMEZONE("Fuso Horário", "🌍"),
    ACTION("Ação Rápida", "⚡")
}

data class OmniResult(
    val category: OmniCategory,
    val sourceQuery: String,
    val primaryResult: String,
    val secondaryDetail: String? = null,
    val actionType: String? = null,
    val actionPayload: String? = null
)

object OmniSearchEvaluator {

    private val symbols = DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"))
    private val decimalFormat = DecimalFormat("#,##0.##", symbols)
    private val currencyFormat = DecimalFormat("#,##0.00", symbols)

    // Taxas de conversão estimadas em BRL (Base de referência offline)
    private val CURRENCY_RATES_TO_BRL = mapOf(
        "USD" to 5.65,
        "EUR" to 6.18,
        "GBP" to 7.35,
        "JPY" to 0.038,
        "CAD" to 4.15,
        "BTC" to 385000.0,
        "BRL" to 1.0
    )

    private val CURRENCY_SYMBOLS = mapOf(
        "USD" to "US$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "CAD" to "CA$",
        "BTC" to "₿",
        "BRL" to "R$"
    )

    private fun normalizeCurrencyCode(raw: String): String? {
        return when (raw.trim().lowercase()) {
            "usd", "dolar", "dolares", "dólar", "dólares" -> "USD"
            "brl", "real", "reais", "r$" -> "BRL"
            "eur", "euro", "euros" -> "EUR"
            "gbp", "libra", "libras" -> "GBP"
            "jpy", "iene", "ienes" -> "JPY"
            "cad" -> "CAD"
            "btc", "bitcoin" -> "BTC"
            else -> null
        }
    }

    fun evaluate(rawQuery: String): OmniResult? {
        val query = rawQuery.trim()
        if (query.isBlank()) return null

        // 1. Ações Rápidas (@wapp, @wa, @call)
        evaluateAction(query)?.let { return it }

        // 2. Timer e Alarme Rápido (ex: "timer 5 min", "alarme 07:30")
        evaluateTimerAndAlarm(query)?.let { return it }

        // 3. Fuso Horário e Relógio Mundial (ex: "hora em toquio", "time in london")
        evaluateTimezone(query)?.let { return it }

        // 4. Porcentagem (ex: "15% de 240", "500 + 10%")
        evaluatePercentage(query)?.let { return it }

        // 5. Conversão de Moedas (ex: "10 usd em brl", "50 eur para brl")
        evaluateCurrency(query)?.let { return it }

        // 6. Conversão de Unidades de Medida (ex: "5 km em milhas", "100 f em c", "16 gb em mb")
        evaluateUnit(query)?.let { return it }

        // 7. Cálculo Matemático Puro (ex: "12 * 45", "@calc 2^8")
        evaluateMath(query)?.let { return it }

        return null
    }

    private fun evaluateAction(query: String): OmniResult? {
        if (query.startsWith("@wapp", ignoreCase = true) || query.startsWith("@wa", ignoreCase = true)) {
            val term = query.replaceFirst(Regex("^@(wapp|wa)\\s*", RegexOption.IGNORE_CASE), "").trim()
            return OmniResult(
                category = OmniCategory.ACTION,
                sourceQuery = query,
                primaryResult = if (term.isNotBlank()) "Conversar com \"$term\"" else "Abrir WhatsApp",
                secondaryDetail = "Toque para abrir conversa direta no WhatsApp",
                actionType = "whatsapp",
                actionPayload = term
            )
        }

        if (query.startsWith("@call", ignoreCase = true) || query.startsWith("@ligar", ignoreCase = true)) {
            val term = query.replaceFirst(Regex("^@(call|ligar)\\s*", RegexOption.IGNORE_CASE), "").trim()
            return OmniResult(
                category = OmniCategory.ACTION,
                sourceQuery = query,
                primaryResult = if (term.isNotBlank()) "Ligar para \"$term\"" else "Abrir Discador",
                secondaryDetail = "Toque para discar o número ou buscar contato",
                actionType = "call",
                actionPayload = term
            )
        }

        return null
    }

    private fun evaluatePercentage(query: String): OmniResult? {
        // Padrão A: "15% de 240" ou "15% of 240"
        val percentOfRegex = Regex("""^(\d+(?:[.,]\d+)?)\s*%\s*(?:de|of)\s*(\d+(?:[.,]\d+)?)$""", RegexOption.IGNORE_CASE)
        val matchA = percentOfRegex.find(query)
        if (matchA != null) {
            val pct = matchA.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return null
            val total = matchA.groupValues[2].replace(",", ".").toDoubleOrNull() ?: return null
            val result = (pct / 100.0) * total
            val plus = total + result
            val minus = total - result

            return OmniResult(
                category = OmniCategory.PERCENTAGE,
                sourceQuery = query,
                primaryResult = decimalFormat.format(result),
                secondaryDetail = "${decimalFormat.format(total)} + ${decimalFormat.format(pct)}% = ${decimalFormat.format(plus)} | - ${decimalFormat.format(pct)}% = ${decimalFormat.format(minus)}"
            )
        }

        // Padrão B: "500 + 20%" ou "1200 - 15%"
        val percentAddSubRegex = Regex("""^(\d+(?:[.,]\d+)?)\s*([+-])\s*(\d+(?:[.,]\d+)?)\s*%$""")
        val matchB = percentAddSubRegex.find(query)
        if (matchB != null) {
            val total = matchB.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return null
            val op = matchB.groupValues[2]
            val pct = matchB.groupValues[3].replace(",", ".").toDoubleOrNull() ?: return null
            val delta = (pct / 100.0) * total
            val finalVal = if (op == "+") total + delta else total - delta

            return OmniResult(
                category = OmniCategory.PERCENTAGE,
                sourceQuery = query,
                primaryResult = decimalFormat.format(finalVal),
                secondaryDetail = "Variação de ${decimalFormat.format(delta)} (${decimalFormat.format(pct)}% de ${decimalFormat.format(total)})"
            )
        }

        return null
    }

    private fun evaluateCurrency(query: String): OmniResult? {
        val currencyRegex = Regex(
            """^(\d+(?:[.,]\d+)?)\s*([a-zA-Z$€£¥]+)\s*(?:em|para|to|in|\->)?\s*([a-zA-Z$€£¥]+)$""",
            RegexOption.IGNORE_CASE
        )
        val match = currencyRegex.find(query) ?: return null

        val amount = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return null
        val fromCode = normalizeCurrencyCode(match.groupValues[2]) ?: return null
        val toCode = normalizeCurrencyCode(match.groupValues[3]) ?: return null

        if (fromCode == toCode) return null

        val fromRateInBrl = CURRENCY_RATES_TO_BRL[fromCode] ?: return null
        val toRateInBrl = CURRENCY_RATES_TO_BRL[toCode] ?: return null

        // Valor em BRL, depois converte para a moeda de destino
        val valueInBrl = amount * fromRateInBrl
        val convertedValue = valueInBrl / toRateInBrl

        val toSymbol = CURRENCY_SYMBOLS[toCode] ?: toCode
        val fromSymbol = CURRENCY_SYMBOLS[fromCode] ?: fromCode
        val parity = fromRateInBrl / toRateInBrl

        return OmniResult(
            category = OmniCategory.CURRENCY,
            sourceQuery = "$fromSymbol ${decimalFormat.format(amount)} $fromCode → $toCode",
            primaryResult = "$toSymbol ${currencyFormat.format(convertedValue)}",
            secondaryDetail = "Cotação estimada • 1 $fromCode = ${currencyFormat.format(parity)} $toCode"
        )
    }

    private fun evaluateUnit(query: String): OmniResult? {
        val unitRegex = Regex(
            """^(\d+(?:[.,]\d+)?)\s*([a-zA-Z°]+)\s*(?:em|para|to|in|\->)?\s*([a-zA-Z°]+)$""",
            RegexOption.IGNORE_CASE
        )
        val match = unitRegex.find(query) ?: return null

        val value = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return null
        val from = match.groupValues[2].lowercase()
        val to = match.groupValues[3].lowercase()

        // 1. Distância
        if ((from == "km" || from == "quilometro" || from == "quilometros") &&
            (to == "mi" || to == "milha" || to == "milhas")) {
            val res = value * 0.621371
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} mi",
                "1 km = 0,6214 milhas"
            )
        }
        if ((from == "mi" || from == "milha" || from == "milhas") &&
            (to == "km" || to == "quilometro" || to == "quilometros")) {
            val res = value * 1.60934
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} km",
                "1 milha = 1,6093 km"
            )
        }
        if ((from == "m" || from == "metro" || from == "metros") &&
            (to == "ft" || to == "pe" || to == "pes" || to == "pés")) {
            val res = value * 3.28084
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} ft",
                "1 metro = 3,2808 pés"
            )
        }
        if ((from == "ft" || from == "pe" || from == "pes" || from == "pés") &&
            (to == "m" || to == "metro" || to == "metros")) {
            val res = value * 0.3048
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} m",
                "1 pé = 0,3048 metros"
            )
        }
        if ((from == "cm" || from == "centimetro" || from == "centimetros") &&
            (to == "in" || to == "pol" || to == "polegada" || to == "polegadas")) {
            val res = value * 0.393701
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} in",
                "1 cm = 0,3937 polegadas"
            )
        }

        // 2. Massa
        if ((from == "kg" || from == "quilo" || from == "quilos") &&
            (to == "lbs" || to == "lb" || to == "libra" || to == "libras")) {
            val res = value * 2.20462
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} lbs",
                "1 kg = 2,2046 libras"
            )
        }
        if ((from == "lbs" || from == "lb" || from == "libra" || to == "libras") &&
            (to == "kg" || to == "quilo" || to == "quilos")) {
            val res = value * 0.453592
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} kg",
                "1 libra = 0,4536 kg"
            )
        }

        // 3. Temperatura
        if ((from == "f" || from == "fahrenheit" || from == "°f") &&
            (to == "c" || to == "celsius" || to == "°c")) {
            val res = (value - 32.0) * (5.0 / 9.0)
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} °C",
                "Fórmula: (°F - 32) × 5/9"
            )
        }
        if ((from == "c" || from == "celsius" || from == "°c") &&
            (to == "f" || to == "fahrenheit" || to == "°f")) {
            val res = (value * (9.0 / 5.0)) + 32.0
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} °F",
                "Fórmula: (°C × 9/5) + 32"
            )
        }

        // 4. Volume
        if ((from == "l" || from == "litro" || from == "litros") &&
            (to == "gal" || to == "galao" || to == "galoes" || to == "galão")) {
            val res = value * 0.264172
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} gal",
                "1 litro = 0,2642 galões"
            )
        }
        if ((from == "gal" || from == "galao" || from == "galoes" || from == "galão") &&
            (to == "l" || to == "litro" || to == "litros")) {
            val res = value * 3.78541
            return OmniResult(
                OmniCategory.UNIT,
                query,
                "${decimalFormat.format(res)} L",
                "1 galão = 3,7854 litros"
            )
        }

        // 5. Armazenamento Digital
        if ((from == "gb" || from == "gigabyte" || from == "gigabytes") &&
            (to == "mb" || to == "megabyte" || to == "megabytes")) {
            val res = value * 1024.0
            return OmniResult(OmniCategory.UNIT, query, "${decimalFormat.format(res)} MB", "1 GB = 1024 MB")
        }
        if ((from == "mb" || from == "megabyte" || from == "megabytes") &&
            (to == "gb" || to == "gigabyte" || to == "gigabytes")) {
            val res = value / 1024.0
            return OmniResult(OmniCategory.UNIT, query, "${decimalFormat.format(res)} GB", "1 MB = 0,000976 GB")
        }
        if ((from == "tb" || from == "terabyte" || from == "terabytes") &&
            (to == "gb" || to == "gigabyte" || to == "gigabytes")) {
            val res = value * 1024.0
            return OmniResult(OmniCategory.UNIT, query, "${decimalFormat.format(res)} GB", "1 TB = 1024 GB")
        }
        if ((from == "mb" || from == "megabyte" || from == "megabytes") &&
            (to == "kb" || to == "kilobyte" || to == "kilobytes")) {
            val res = value * 1024.0
            return OmniResult(OmniCategory.UNIT, query, "${decimalFormat.format(res)} KB", "1 MB = 1024 KB")
        }

        return null
    }

    private val TIMEZONE_CITIES = mapOf(
        "toquio" to Pair("Asia/Tokyo", "Tóquio, Japão"),
        "tokyo" to Pair("Asia/Tokyo", "Tóquio, Japão"),
        "nova york" to Pair("America/New_York", "Nova York, EUA"),
        "new york" to Pair("America/New_York", "Nova York, EUA"),
        "ny" to Pair("America/New_York", "Nova York, EUA"),
        "londres" to Pair("Europe/London", "Londres, Reino Unido"),
        "london" to Pair("Europe/London", "Londres, Reino Unido"),
        "paris" to Pair("Europe/Paris", "Paris, França"),
        "lisboa" to Pair("Europe/Lisbon", "Lisboa, Portugal"),
        "lisbon" to Pair("Europe/Lisbon", "Lisboa, Portugal"),
        "berlim" to Pair("Europe/Berlin", "Berlim, Alemanha"),
        "berlin" to Pair("Europe/Berlin", "Berlim, Alemanha"),
        "roma" to Pair("Europe/Rome", "Roma, Itália"),
        "rome" to Pair("Europe/Rome", "Roma, Itália"),
        "madrid" to Pair("Europe/Madrid", "Madri, Espanha"),
        "sydney" to Pair("Australia/Sydney", "Sydney, Austrália"),
        "los angeles" to Pair("America/Los_Angeles", "Los Angeles, EUA"),
        "dubai" to Pair("Asia/Dubai", "Dubai, Emirados Árabes"),
        "sao paulo" to Pair("America/Sao_Paulo", "São Paulo, Brasil"),
        "brasilia" to Pair("America/Sao_Paulo", "Brasília, Brasil"),
        "toronto" to Pair("America/Toronto", "Toronto, Canadá"),
        "seul" to Pair("Asia/Seoul", "Seul, Coreia do Sul"),
        "seoul" to Pair("Asia/Seoul", "Seul, Coreia do Sul")
    )

    private fun evaluateTimezone(query: String): OmniResult? {
        val pattern = Regex("""^(?:hora|horas|que horas s[aã]o|hor[aá]rio|time)\s*(?:em|in|de|at)?\s*(.+)$""", RegexOption.IGNORE_CASE)
        val match = pattern.find(query) ?: return null
        val cityQuery = match.groupValues[1].trim().lowercase()
            .replace("?", "").replace("!", "").trim()

        val entry = TIMEZONE_CITIES[cityQuery]
            ?: TIMEZONE_CITIES.entries.firstOrNull { cityQuery.contains(it.key) }?.value
            ?: return null

        return try {
            val zoneId = ZoneId.of(entry.first)
            val nowInZone = ZonedDateTime.now(zoneId)
            val localNow = ZonedDateTime.now()

            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val formattedTime = nowInZone.format(timeFormatter)

            val dayDiff = nowInZone.dayOfYear - localNow.dayOfYear
            val dayDesc = when (dayDiff) {
                1, -364, -365 -> "Amanhã"
                -1, 364, 365 -> "Ontem"
                else -> "Hoje"
            }

            val offsetHours = (nowInZone.offset.totalSeconds - localNow.offset.totalSeconds) / 3600
            val offsetDesc = if (offsetHours >= 0) "+${offsetHours}h" else "${offsetHours}h"

            OmniResult(
                category = OmniCategory.TIMEZONE,
                sourceQuery = query,
                primaryResult = "$formattedTime ($dayDesc)",
                secondaryDetail = "${entry.second} • Fuso $offsetDesc em relação a você"
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun evaluateTimerAndAlarm(query: String): OmniResult? {
        // Timer: ex: "timer 5 min", "timer 10", "temporizador 30s"
        val timerPattern = Regex("""^(?:timer|temporizador)\s*(\d+)\s*(m|min|minutos?|s|seg|segundos?)?$""", RegexOption.IGNORE_CASE)
        val timerMatch = timerPattern.find(query)
        if (timerMatch != null) {
            val amount = timerMatch.groupValues[1].toIntOrNull() ?: return null
            val unit = timerMatch.groupValues[2].lowercase()
            val totalSeconds = if (unit.startsWith("s")) amount else amount * 60
            val displayDesc = if (unit.startsWith("s")) "$amount seg" else "$amount min"

            return OmniResult(
                category = OmniCategory.ACTION,
                sourceQuery = query,
                primaryResult = "Iniciar Timer de $displayDesc",
                secondaryDetail = "Toque para abrir e iniciar o temporizador no relógio",
                actionType = "timer",
                actionPayload = totalSeconds.toString()
            )
        }

        // Alarme: ex: "alarme 07:30", "alarme 8h", "despertador 6:00"
        val alarmPattern = Regex("""^(?:alarme|alarm|despertador)\s*(?:para|as|às|at)?\s*(\d{1,2})(?:[:hH](\d{2}))?$""", RegexOption.IGNORE_CASE)
        val alarmMatch = alarmPattern.find(query)
        if (alarmMatch != null) {
            val hour = alarmMatch.groupValues[1].toIntOrNull() ?: return null
            val minute = alarmMatch.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
            if (hour in 0..23 && minute in 0..59) {
                val formattedTime = String.format("%02d:%02d", hour, minute)
                return OmniResult(
                    category = OmniCategory.ACTION,
                    sourceQuery = query,
                    primaryResult = "Definir Alarme para $formattedTime",
                    secondaryDetail = "Toque para agendar no aplicativo de relógio",
                    actionType = "alarm",
                    actionPayload = "$hour:$minute"
                )
            }
        }

        // Modo Foco Zen: "zen", "foco", "modo zen", "modo foco", "zen 25", "foco 45 min"
        val zenPattern = Regex("""^(?:zen|foco|modo\s+zen|modo\s+foco)(?:\s+(\d+)\s*(?:m|min|minutos?)?)?$""", RegexOption.IGNORE_CASE)
        val zenMatch = zenPattern.find(query)
        if (zenMatch != null) {
            val minutes = zenMatch.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 30
            return OmniResult(
                category = OmniCategory.ACTION,
                sourceQuery = query,
                primaryResult = "Ativar Modo Foco Zen ($minutes min)",
                secondaryDetail = "Bloqueia distrações e restringe apps permitidos",
                actionType = "zen",
                actionPayload = minutes.toString()
            )
        }

        return null
    }

    private fun evaluateMath(query: String): OmniResult? {
        val expr = if (query.startsWith("@calc", ignoreCase = true)) {
            query.substringAfter("calc", "").trim()
        } else {
            query
        }
        if (expr.isBlank()) return null

        val result = MathEvaluator.evaluate(expr)
        if (result != null) {
            return OmniResult(
                category = OmniCategory.MATH,
                sourceQuery = expr,
                primaryResult = result,
                secondaryDetail = "Cálculo aritmético instantâneo"
            )
        }
        return null
    }
}
