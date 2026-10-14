package com.tessera.launcher.data.helper

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.model.DeliveryActivityDetails
import com.tessera.launcher.data.model.DeliveryStage
import com.tessera.launcher.data.model.LiveActivityAction
import java.util.regex.Pattern

/**
 * Parser inteligente de notificações de delivery (iFood, Rappi, Zé Delivery, Uber Eats, DoorDash).
 * Extrai:
 * - Nome do restaurante ou loja
 * - Etapa do pedido (Confirmado, Preparando, Em Rota, Entregue)
 * - Tempo estimado de entrega (ETA em minutos ou texto)
 * - Código PIN de entrega (ex: PIN: 4821)
 * - Mensagem de status detalhada
 * - Logotipo / Artwork da loja
 * - Ações interativas (ex: Ver pedido, Chamar entregador)
 */
object DeliveryNotificationParser {

    private val PIN_PATTERN = Pattern.compile(
        """(?i)(?:código|pin|cód|code)[\s:]*([0-9]{4})\b"""
    )

    private val ETA_PATTERN = Pattern.compile(
        """(?i)(?:chega em|previsão|entrega em|em|tempo estimado)?\s*(\d+)(?:-(\d+))?\s*(?:minutos?|mins?|m)\b"""
    )

    fun parse(
        sbn: StatusBarNotification,
        notification: Notification,
        context: Context,
        title: String,
        text: String?,
        subText: String?
    ): DeliveryActivityDetails {
        val pkg = sbn.packageName
        val appName = when {
            pkg.contains("ifood", ignoreCase = true) -> "iFood"
            pkg.contains("rappi", ignoreCase = true) -> "Rappi"
            pkg.contains("zedelivery", ignoreCase = true) -> "Zé Delivery"
            pkg.contains("ubercab.eats", ignoreCase = true) || pkg.contains("eats", ignoreCase = true) -> "Uber Eats"
            pkg.contains("doordash", ignoreCase = true) -> "DoorDash"
            else -> runCatching {
                val appInfo = context.packageManager.getApplicationInfo(pkg, 0)
                context.packageManager.getApplicationLabel(appInfo).toString()
            }.getOrDefault("Delivery")
        }

        val fullText = buildString {
            append(title)
            if (!text.isNullOrBlank()) append(" • ").append(text)
            if (!subText.isNullOrBlank()) append(" • ").append(subText)
        }

        // 1. Etapa da entrega
        val stage = determineDeliveryStage(fullText)

        // 2. Extração de PIN / Código de confirmação
        val confirmationPin = extractPin(fullText)

        // 3. Extração de Minutos / ETA
        val etaMinutes = extractEtaMinutes(fullText)
        val etaText = formatEtaText(etaMinutes, stage, title, text)

        // 4. Extração do Nome do Restaurante / Loja
        val storeName = extractStoreName(title, text, subText, appName)

        // 5. Mensagem de Status
        val statusMessage = formatStatusMessage(stage, storeName, text, title)

        // 6. Cálculo de Progresso
        val progressPercent = calculateProgress(stage, etaMinutes)

        // 7. Ações da Notificação
        val actions = extractActions(notification)

        // 8. Logotipo / Imagem
        val storeLogo = extractStoreLogo(notification, context)

        return DeliveryActivityDetails(
            appName = appName,
            storeName = storeName,
            stage = stage,
            etaMinutes = etaMinutes,
            etaText = etaText,
            statusMessage = statusMessage,
            confirmationPin = confirmationPin,
            orderSummary = text,
            storeLogo = storeLogo,
            progressPercent = progressPercent,
            actions = actions
        )
    }

    internal fun determineDeliveryStage(text: String): DeliveryStage {
        val lower = text.lowercase()
        return when {
            lower.contains("entregue") || lower.contains("aproveite seu pedido") ||
                    lower.contains("delivered") || lower.contains("concluído") ||
                    lower.contains("bom apetite") -> DeliveryStage.DELIVERED

            lower.contains("saiu para entrega") || lower.contains("a caminho") ||
                    lower.contains("entregador a caminho") || lower.contains("on the way") ||
                    lower.contains("indo até você") || lower.contains("está chegando") -> DeliveryStage.ON_THE_WAY

            lower.contains("preparando") || lower.contains("na cozinha") ||
                    lower.contains("sendo preparado") || lower.contains("em preparo") ||
                    lower.contains("preparing") || lower.contains("fazendo") -> DeliveryStage.PREPARING

            lower.contains("confirmado") || lower.contains("pedido recebido") ||
                    lower.contains("confirmed") || lower.contains("recebemos seu pedido") ||
                    lower.contains("aguardando restaurante") -> DeliveryStage.CONFIRMED

            else -> DeliveryStage.ON_THE_WAY
        }
    }

    internal fun extractPin(text: String): String? {
        val matcher = PIN_PATTERN.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)
        }

        // Tenta padrão "código 1234"
        val codeMatch = Regex("""(?i)\b(?:código|pin|senha)\s*(?:de\s*entrega)?[\s:]*([0-9]{4})\b""").find(text)
        if (codeMatch != null) {
            return codeMatch.groupValues[1]
        }
        return null
    }

    internal fun extractEtaMinutes(text: String): Int? {
        val matcher = ETA_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toIntOrNull()
        } else null
    }

    internal fun formatEtaText(etaMinutes: Int?, stage: DeliveryStage, title: String, text: String?): String {
        return when {
            stage == DeliveryStage.DELIVERED -> "Pedido entregue!"
            stage == DeliveryStage.CONFIRMED -> "Pedido confirmado"
            stage == DeliveryStage.PREPARING -> "Sendo preparado"
            etaMinutes != null && etaMinutes > 0 -> "$etaMinutes min até a entrega"
            !text.isNullOrBlank() && (text.contains("min", ignoreCase = true) || text.contains("previsão", ignoreCase = true)) -> text
            else -> "A caminho de você"
        }
    }

    internal fun extractStoreName(title: String, text: String?, subText: String?, appName: String): String {
        // Se o título contém o nome da loja separado por "•" ou "-"
        val cleanTitle = title.replace(appName, "", ignoreCase = true).trim(' ', '•', '-', ':')
        if (cleanTitle.isNotBlank() && cleanTitle.length > 2 && !cleanTitle.contains("pedido", ignoreCase = true)) {
            return cleanTitle
        }

        // Tenta extrair do subText se existir
        if (!subText.isNullOrBlank() && !subText.equals(appName, ignoreCase = true) && !subText.contains("pedido", ignoreCase = true)) {
            return subText
        }

        // Fallback para partes do texto
        val parts = listOfNotNull(title, text).flatMap { it.split("•", "-", "|") }.map { it.trim() }
        for (part in parts) {
            val lower = part.lowercase()
            if (lower != appName.lowercase() && !lower.contains("previsão") && !lower.contains("entrega") && !lower.contains("min") && !lower.contains("código") && part.length in 3..25) {
                return part
            }
        }

        return appName
    }

    internal fun formatStatusMessage(stage: DeliveryStage, storeName: String, text: String?, title: String): String {
        return when (stage) {
            DeliveryStage.CONFIRMED -> "$storeName recebeu seu pedido"
            DeliveryStage.PREPARING -> "$storeName está preparando seu pedido"
            DeliveryStage.ON_THE_WAY -> "Entregador a caminho do seu endereço"
            DeliveryStage.DELIVERED -> "Seu pedido foi entregue. Bom apetite!"
        }
    }

    private fun calculateProgress(stage: DeliveryStage, etaMinutes: Int?): Float {
        return when (stage) {
            DeliveryStage.CONFIRMED -> 0.15f
            DeliveryStage.PREPARING -> 0.45f
            DeliveryStage.ON_THE_WAY -> {
                if (etaMinutes != null && etaMinutes <= 5) 0.88f else 0.75f
            }
            DeliveryStage.DELIVERED -> 1.0f
        }
    }

    private fun extractActions(notification: Notification): List<LiveActivityAction> {
        val actions = notification.actions ?: return emptyList()
        return actions.mapNotNull { act ->
            val title = act.title?.toString()?.trim()
            if (!title.isNullOrBlank()) {
                LiveActivityAction(title = title, intent = act.actionIntent)
            } else null
        }.take(3)
    }

    private fun extractStoreLogo(notification: Notification, context: Context): Bitmap? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notification.getLargeIcon()?.loadDrawable(context)?.let { drawableToBitmap(it) }
            } else {
                @Suppress("DEPRECATION")
                notification.largeIcon
            }
        }.getOrNull()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
