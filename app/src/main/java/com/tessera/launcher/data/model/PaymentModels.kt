package com.tessera.launcher.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class PlanOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceFormatted: String,
    val badge: String? = null,
    val durationDays: Int? = null
)

val DEFAULT_PRO_PLANS: List<PlanOption> = listOf(
    PlanOption(
        id = "pro_monthly",
        title = "Tessera Pro Mensal",
        subtitle = "Acesso completo aos recursos Pro com renovação flexível.",
        priceFormatted = "R$ 4,90 / mês",
        badge = null,
        durationDays = 30
    ),
    PlanOption(
        id = "pro_lifetime",
        title = "Tessera Pro Vitalício",
        subtitle = "Pague uma única vez e tenha acesso permanente.",
        priceFormatted = "R$ 24,90",
        badge = "Mais Popular",
        durationDays = null
    )
)

@Immutable
data class CheckoutSession(
    val paymentId: String,
    val externalReference: String? = null,
    val planId: String,
    val method: String, // "pix" | "card"
    val pixCopyPaste: String? = null,
    val qrCodeBase64: String? = null,
    val checkoutUrl: String? = null,
    val status: String = "pending" // "pending" | "approved"
)

@Immutable
data class LicenseInfo(
    val isProActive: Boolean = false,
    val activePlanId: String? = null,
    val licensedEmail: String? = null,
    val licenseToken: String? = null,
    val activatedAt: Long = 0L,
    val expiresAt: Long = 0L
) {
    fun isCurrentlyValid(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (!isProActive) return false
        return expiresAt <= 0L || expiresAt > nowMs
    }

    fun planDisplayName(): String = when (activePlanId) {
        "pro_monthly" -> "Tessera Pro Mensal"
        "pro_lifetime" -> "Tessera Pro Vitalício"
        else -> "Tessera Pro"
    }
}

@Immutable
data class PaymentStatusResult(
    val status: String,
    val licenseInfo: LicenseInfo?,
    val message: String? = null
)
