package com.tessera.launcher.data.helper

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.model.HudMilestone
import com.tessera.launcher.data.model.LiveActivityAction
import com.tessera.launcher.data.model.PackageActivityDetails
import com.tessera.launcher.data.model.PackageStage
import java.util.regex.Pattern

/**
 * Parser inteligente de notificações de encomendas e e-commerce.
 * Suporta Mercado Livre, Shopee, Amazon, AliExpress, Correios, Loggi, Magalu, Shein, Temu
 * e qualquer aplicativo com código de rastreio postal ou avisos de remessa.
 */
object PackageNotificationParser {

    private val TRACKING_CODE_PATTERN = Pattern.compile(
        """\b([A-Za-z]{2}\d{9}[A-Za-z]{2})\b"""
    )

    private val ETA_PATTERN = Pattern.compile(
        """(?i)(?:chega|previsão|entrega)?\s*(hoje\b.*?(?:às\s*\d{1,2}h|\d{1,2}:\d{2})?|amanhã\b.*?(?:às\s*\d{1,2}h)?|entre\s*\d{1,2}\s*e\s*\d{1,2}\s*de\s*[a-zç]+|\d{1,2}\s*de\s*[a-zç]+|\d{1,2}/\d{1,2}(?:/\d{2,4})?)"""
    )

    fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): PackageActivityDetails {
        val appName = when {
            snapshot.packageName.contains("mercadolibre", ignoreCase = true) -> "Mercado Livre"
            snapshot.packageName.contains("shopee", ignoreCase = true) -> "Shopee"
            snapshot.packageName.contains("amazon", ignoreCase = true) -> "Amazon"
            snapshot.packageName.contains("aliexpress", ignoreCase = true) -> "AliExpress"
            snapshot.packageName.contains("correios", ignoreCase = true) -> "Correios"
            snapshot.packageName.contains("loggi", ignoreCase = true) -> "Loggi"
            snapshot.packageName.contains("luizalabs", ignoreCase = true) -> "Magalu"
            snapshot.packageName.contains("shein", ignoreCase = true) || snapshot.packageName.contains("zzkko", ignoreCase = true) -> "Shein"
            snapshot.packageName.contains("temu", ignoreCase = true) -> "Temu"
            else -> snapshot.appName.ifBlank { "Encomenda" }
        }

        val fullText = snapshot.fullText
        val stage = determineStage(fullText)
        val trackingCode = extractTrackingCode(fullText)
        val etaText = extractEtaText(fullText, stage)
        val headline = formatHeadline(stage, fullText)
        val milestones = buildMilestones(stage)
        val actions = extractActions(sbn.notification)
        val logo = extractLogo(sbn.notification, context)

        val itemSummary = when {
            snapshot.text.isNotBlank() && snapshot.text != headline -> snapshot.text
            snapshot.bigText.isNotBlank() && snapshot.bigText != headline -> snapshot.bigText
            else -> null
        }

        return PackageActivityDetails(
            appName = appName,
            stage = stage,
            headline = headline,
            etaText = etaText,
            trackingCode = trackingCode,
            itemSummary = itemSummary,
            packageLogo = logo,
            milestones = milestones,
            actions = actions
        )
    }

    internal fun determineStage(text: String): PackageStage {
        val lower = text.lowercase()
        return when {
            lower.contains("entregue") || lower.contains("delivered") || lower.contains("entregamos") -> {
                PackageStage.DELIVERED
            }
            lower.contains("tentativa") || lower.contains("destinatário ausente") ||
                    lower.contains("tributação") || lower.contains("aguardando pagamento") ||
                    lower.contains("endereço incorreto") || lower.contains("falha na entrega") -> {
                PackageStage.ISSUE
            }
            lower.contains("disponível para retirada") || lower.contains("aguardando retirada") ||
                    lower.contains("retirar na agência") || lower.contains("pickup") -> {
                PackageStage.PICKUP_READY
            }
            lower.contains("saiu para entrega") || lower.contains("chega hoje") ||
                    lower.contains("out for delivery") || lower.contains("a caminho do seu endereço") ||
                    lower.contains("en caminho") || lower.contains("motorista a caminho") -> {
                PackageStage.OUT_FOR_DELIVERY
            }
            lower.contains("em trânsito") || lower.contains("em transporte") ||
                    lower.contains("in transit") || lower.contains("a caminho") ||
                    lower.contains("transferência") -> {
                PackageStage.IN_TRANSIT
            }
            lower.contains("enviado") || lower.contains("objeto postado") ||
                    lower.contains("postado") || lower.contains("despachado") ||
                    lower.contains("shipped") || lower.contains("a caminho da transportadora") -> {
                PackageStage.SHIPPED
            }
            lower.contains("compra aprovada") || lower.contains("pagamento confirmado") ||
                    lower.contains("pedido confirmado") || lower.contains("pedido realizado") ||
                    lower.contains("order confirmed") -> {
                PackageStage.ORDERED
            }
            else -> PackageStage.OUT_FOR_DELIVERY
        }
    }

    internal fun extractTrackingCode(text: String): String? {
        val matcher = TRACKING_CODE_PATTERN.matcher(text)
        return if (matcher.find()) matcher.group(1)?.uppercase() else null
    }

    internal fun extractEtaText(text: String, stage: PackageStage): String? {
        if (stage == PackageStage.DELIVERED) return "Pedido entregue"
        if (stage == PackageStage.PICKUP_READY) return "Disponível para retirada"

        val lower = text.lowercase()
        if (lower.contains("hoje")) {
            val hourMatch = Regex("""(?:até\s*(?:às\s*)?|às\s*)(\d{1,2}(?:h|:\d{2}))""").find(lower)
            return if (hourMatch != null) "Chega hoje até ${hourMatch.groupValues[1]}" else "Chega hoje"
        }
        if (lower.contains("amanhã")) {
            return "Chega amanhã"
        }

        val matcher = ETA_PATTERN.matcher(text)
        if (matcher.find()) {
            val match = matcher.group(1)?.trim()
            if (!match.isNullOrBlank()) {
                return if (match.startsWith("Chega", ignoreCase = true) || match.startsWith("Entre", ignoreCase = true)) {
                    match
                } else {
                    "Chega $match"
                }
            }
        }
        return null
    }

    private fun formatHeadline(stage: PackageStage, fullText: String): String {
        return when (stage) {
            PackageStage.OUT_FOR_DELIVERY -> "Saiu para entrega"
            PackageStage.DELIVERED -> "Pedido entregue"
            PackageStage.PICKUP_READY -> "Aguardando retirada"
            PackageStage.ISSUE -> "Ação necessária na entrega"
            PackageStage.IN_TRANSIT -> "Em trânsito"
            PackageStage.SHIPPED -> "Pedido enviado"
            PackageStage.ORDERED -> "Compra aprovada"
        }
    }

    private fun buildMilestones(stage: PackageStage): List<HudMilestone> {
        val isOrdered = true
        val isShipped = stage in listOf(
            PackageStage.SHIPPED,
            PackageStage.IN_TRANSIT,
            PackageStage.OUT_FOR_DELIVERY,
            PackageStage.PICKUP_READY,
            PackageStage.DELIVERED
        )
        val isOut = stage in listOf(
            PackageStage.OUT_FOR_DELIVERY,
            PackageStage.PICKUP_READY,
            PackageStage.DELIVERED
        )
        val isDelivered = stage == PackageStage.DELIVERED

        return listOf(
            HudMilestone("Comprado", isOrdered),
            HudMilestone("Enviado", isShipped),
            HudMilestone(if (stage == PackageStage.PICKUP_READY) "Retirada" else "Em rota", isOut),
            HudMilestone("Entregue", isDelivered)
        )
    }

    private fun extractActions(notification: Notification?): List<LiveActivityAction> {
        return notification?.actions?.mapNotNull { act ->
            val title = act.title?.toString()?.trim() ?: return@mapNotNull null
            LiveActivityAction(title = title, intent = act.actionIntent)
        } ?: emptyList()
    }

    private fun extractLogo(notification: Notification?, context: Context): Bitmap? {
        val notif = notification ?: return null
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notif.getLargeIcon()?.loadDrawable(context)?.let { drawableToBitmap(it) }
            } else {
                @Suppress("DEPRECATION")
                notif.largeIcon
            }
        }.getOrNull()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(1, 128) else 64
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(1, 128) else 64
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
