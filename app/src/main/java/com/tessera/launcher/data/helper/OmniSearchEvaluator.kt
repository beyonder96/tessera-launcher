package com.tessera.launcher.data.helper

import com.tessera.launcher.ui.components.MathEvaluator
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class OmniCategory(val displayName: String, val emoji: String) {
    CURRENCY("Moeda", "💵"),
    UNIT("Medida", "📐"),
    PERCENTAGE("Porcentagem", "📊"),
    MATH("Cálculo", "🧮"),
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

        // 2. Porcentagem (ex: "15% de 240", "500 + 10%")
        evaluatePercentage(query)?.let { return it }

        // 3. Conversão de Moedas (ex: "10 usd em brl", "50 eur para brl")
        evaluateCurrency(query)?.let { return it }

        // 4. Conversão de Unidades de Medida (ex: "5 km em milhas", "100 f em c")
        evaluateUnit(query)?.let { return it }

        // 5. Cálculo Matemático Puro (ex: "12 * 45", "@calc 2^8")
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
