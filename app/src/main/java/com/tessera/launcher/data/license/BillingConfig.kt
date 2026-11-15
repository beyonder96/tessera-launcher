package com.tessera.launcher.data.license

/**
 * Configuração centralizada da vitrine comercial, preços e checkout do Tessera Launcher.
 * Edite os links e valores abaixo a qualquer momento sem alterar a lógica do app.
 */
object BillingConfig {
    // Duração do período de testes gratuito (em dias e milissegundos)
    const val TRIAL_DAYS: Int = 3
    const val TRIAL_DURATION_MS: Long = TRIAL_DAYS * 24L * 60L * 60L * 1000L

    // Limites estritos para códigos de cortesia/promoção (TESS-GIFT-XXXX-XXXX)
    const val GIFT_MIN_DAYS: Int = 3
    const val GIFT_MAX_DAYS: Int = 7

    // Links de Checkout (Kiwify, Kirvano, Mercado Pago, Stripe, Gumroad, etc.)
    var MONTHLY_CHECKOUT_URL: String = "https://seusite.com/checkout/mensal"
    var SEMESTRAL_CHECKOUT_URL: String = "https://seusite.com/checkout/semestral"
    var ANNUAL_CHECKOUT_URL: String = "https://seusite.com/checkout/anual"
    var LIFETIME_CHECKOUT_URL: String = "https://seusite.com/checkout/vitalicio"

    // Canais de Atendimento e Venda Direta (Pix / Dúvidas)
    var SUPPORT_WHATSAPP_URL: String = "https://wa.me/55SEUNUMERO"
    var SUPPORT_EMAIL: String = "suporte@tesseradev.com"

    // Preços e selos exibidos na Vitrine Tessera Pro
    var MONTHLY_PRICE_LABEL: String = "R$ 4,90 / mês"
    var SEMESTRAL_PRICE_LABEL: String = "R$ 12,90 / 6 meses"
    var ANNUAL_PRICE_LABEL: String = "R$ 19,90 / ano"
    var ANNUAL_MONTHLY_EQUIVALENT: String = "R$ 1,65 / mês"
    var LIFETIME_PRICE_LABEL: String = "R$ 29,90 único"
    var LIFETIME_BADGE: String = "LOTE FUNDADOR"

    fun buildWhatsAppPurchaseUrl(planTitle: String, priceLabel: String): String {
        val encodedText = android.net.Uri.encode(
            "Olá! Tenho interesse no Tessera Pro ($planTitle - $priceLabel) e gostaria de adquirir minha chave de ativação."
        )
        val base = SUPPORT_WHATSAPP_URL.trimEnd('/')
        return if (base.contains("?")) "$base&text=$encodedText" else "$base?text=$encodedText"
    }

    fun resolveCheckoutUrl(configuredUrl: String, planTitle: String, priceLabel: String): String {
        val trimmed = configuredUrl.trim()
        val isPlaceholderUrl = trimmed.contains("seusite.com", ignoreCase = true)
        val isWhatsAppConfigured = !SUPPORT_WHATSAPP_URL.contains("SEUNUMERO", ignoreCase = true)
        return if (isPlaceholderUrl && isWhatsAppConfigured) {
            buildWhatsAppPurchaseUrl(planTitle, priceLabel)
        } else {
            trimmed
        }
    }
}
