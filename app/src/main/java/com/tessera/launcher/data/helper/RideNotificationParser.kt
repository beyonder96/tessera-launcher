package com.tessera.launcher.data.helper

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.model.LiveActivityAction
import com.tessera.launcher.data.model.RideActivityDetails
import com.tessera.launcher.data.model.RideStage
import java.util.regex.Pattern

/**
 * Parser inteligente de notificações de transporte e mobilidade urbana (Uber, 99, Lyft, DiDi, Bolt).
 * Extrai:
 * - Tempo estimado (ETA / minutos restantes)
 * - Nome e avaliação do motorista
 * - Modelo e cor do veículo
 * - Placa do veículo (padrão Mercosul, tradicional e internacional)
 * - Etapa da corrida (Buscando, A caminho, Chegou, Em viagem, Finalizado)
 * - Progresso visual estimado
 * - Ações interativas da notificação (PendingIntent)
 */
object RideNotificationParser {

    // Regex para extração de minutos/tempo restante
    private val ETA_PATTERN = Pattern.compile(
        """(?i)(?:chegando em|chega em|em|arriving in|until pickup|away)?\s*(\d+)\s*(?:minutos?|mins?|m)\b"""
    )

    // Regex para placas (Mercosul BRA2E19, Brasil tradicional ABC-1234, Internacional como 7NLR004)
    private val PLATE_MERCOSUL = Pattern.compile("""\b[A-Z]{3}[0-9][A-Z][0-9]{2}\b""")
    private val PLATE_TRADITIONAL = Pattern.compile("""\b[A-Z]{3}-?[0-9]{4}\b""")
    private val PLATE_INTL = Pattern.compile("""\b[0-9][A-Z]{3,4}[0-9]{3,4}\b""")

    // Palavras que não são placas
    private val EXCLUDED_WORDS = setOf(
        "UBERX", "UBERXL", "COMFORT", "BLACK", "POP99", "TAXI99", "POP", "VIP"
    )

    // Regex para avaliação (ex: 4.9★, 4,95 ★, 5.0)
    private val RATING_PATTERN = Pattern.compile("""([45][.,]\d{1,2}\s*★|★\s*[45][.,]\d{1,2})""")

    // Cores comuns de veículos em português e inglês
    private val VEHICLE_COLORS = listOf(
        "prata", "silver", "preto", "preta", "black", "branco", "branca", "white",
        "cinza", "gray", "grey", "vermelho", "vermelha", "red", "azul", "blue",
        "dourado", "dourada", "gold", "verde", "green", "amarelo", "amarela", "yellow",
        "chumbo", "marrom", "bordeaux", "vinho"
    )

    fun parse(
        sbn: StatusBarNotification,
        notification: Notification,
        context: Context,
        title: String,
        text: String?,
        subText: String?
    ): RideActivityDetails {
        val pkg = sbn.packageName
        val appName = when {
            pkg.contains("uber", ignoreCase = true) -> "Uber"
            pkg.contains("99", ignoreCase = true) -> "99"
            pkg.contains("lyft", ignoreCase = true) -> "Lyft"
            pkg.contains("didi", ignoreCase = true) -> "DiDi"
            pkg.contains("bolt", ignoreCase = true) -> "Bolt"
            else -> runCatching {
                val appInfo = context.packageManager.getApplicationInfo(pkg, 0)
                context.packageManager.getApplicationLabel(appInfo).toString()
            }.getOrDefault("Transporte")
        }

        val fullText = buildString {
            append(title)
            if (!text.isNullOrBlank()) append(" • ").append(text)
            if (!subText.isNullOrBlank()) append(" • ").append(subText)
        }

        // 1. Extração de Etapa da Corrida
        val stage = determineRideStage(fullText)

        // 2. Extração de Minutos / ETA
        val etaMinutes = extractEtaMinutes(fullText)
        val etaText = formatEtaText(etaMinutes, stage, title, text)

        // 3. Extração de Placa
        val licensePlate = extractLicensePlate(fullText)

        // 4. Extração de Avaliação e Nome do Motorista
        val driverRating = extractRating(fullText)
        val driverName = extractDriverName(fullText, title, text)

        // 5. Extração de Modelo e Cor do Veículo
        val vehicleModel = extractVehicleModel(fullText, licensePlate, driverName)

        // 6. Mensagem de Status
        val statusMessage = formatStatusMessage(stage, driverName, text, title)

        // 7. Cálculo de Progresso no Trajeto
        val progressPercent = calculateProgressPercent(sbn, notification, stage, etaMinutes)

        // 8. Extração de Ações da Notificação
        val actions = extractActions(notification)

        // 9. Extração de Imagens (Avatar e Carro)
        val (driverAvatar, vehicleImage) = extractImages(notification, context)

        return RideActivityDetails(
            appName = appName,
            stage = stage,
            etaMinutes = etaMinutes,
            etaText = etaText,
            statusMessage = statusMessage,
            driverName = driverName,
            driverRating = driverRating,
            licensePlate = licensePlate,
            vehicleModel = vehicleModel,
            driverAvatar = driverAvatar,
            vehicleImage = vehicleImage,
            progressPercent = progressPercent,
            actions = actions
        )
    }

    internal fun determineRideStage(text: String): RideStage {
        val lower = text.lowercase()
        return when {
            lower.contains("procurando") || lower.contains("buscando") ||
                    lower.contains("finding") || lower.contains("searching") ||
                    lower.contains("aguarde") || lower.contains("confirmando") -> RideStage.SEARCHING

            lower.contains("chegou") || lower.contains("no local") ||
                    lower.contains("arrived") || lower.contains("esperando") ||
                    lower.contains("aguardando você") -> RideStage.ARRIVED

            lower.contains("em viagem") || lower.contains("viagem em andamento") ||
                    lower.contains("on your way") || lower.contains("em rota para") ||
                    lower.contains("indo para") || lower.contains("dropoff") -> RideStage.IN_TRIP

            lower.contains("finalizada") || lower.contains("concluída") ||
                    lower.contains("completed") -> RideStage.COMPLETED

            else -> RideStage.ARRIVING
        }
    }

    internal fun extractEtaMinutes(text: String): Int? {
        val matcher = ETA_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toIntOrNull()
        } else null
    }

    internal fun formatEtaText(etaMinutes: Int?, stage: RideStage, title: String, text: String?): String {
        return when {
            etaMinutes != null && etaMinutes == 1 -> "1 min para o embarque"
            etaMinutes != null && etaMinutes > 1 -> "$etaMinutes min para o embarque"
            stage == RideStage.ARRIVED -> "Motorista no local"
            stage == RideStage.SEARCHING -> "Buscando motorista"
            stage == RideStage.IN_TRIP -> "Viagem em andamento"
            stage == RideStage.COMPLETED -> "Viagem finalizada"
            !text.isNullOrBlank() && text.length <= 25 -> text
            else -> title
        }
    }

    internal fun extractLicensePlate(text: String): String? {
        // Tenta padrão Mercosul
        val mercosul = PLATE_MERCOSUL.matcher(text)
        while (mercosul.find()) {
            val candidate = mercosul.group()
            if (candidate !in EXCLUDED_WORDS) return candidate
        }

        // Tenta padrão Tradicional (ABC-1234 ou ABC1234)
        val traditional = PLATE_TRADITIONAL.matcher(text)
        while (traditional.find()) {
            val candidate = traditional.group()
            if (candidate !in EXCLUDED_WORDS) return candidate
        }

        // Tenta padrão Internacional (como 7NLR004 na foto de referência)
        val intl = PLATE_INTL.matcher(text)
        while (intl.find()) {
            val candidate = intl.group()
            if (candidate !in EXCLUDED_WORDS) return candidate
        }

        return null
    }

    internal fun extractRating(text: String): String? {
        val matcher = RATING_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.trim()
        } else null
    }

    internal fun extractDriverName(fullText: String, title: String, text: String?): String? {
        // Padrões como: "Anderson is arriving soon", "Anderson está a caminho", "Motorista Anderson"
        val arrivingMatch = Regex("""(?i)([A-ZÁÉÍÓÚÂÊÔÃÕÇ][a-záéíóúâêôãõç]+)\s+(?:is arriving|está a caminho|está chegando|vem aí)""").find(fullText)
        if (arrivingMatch != null) {
            return arrivingMatch.groupValues[1]
        }

        val driverPrefixMatch = Regex("""(?i)motorista:\s*([A-ZÁÉÍÓÚÂÊÔÃÕÇ][a-záéíóúâêôãõç]+)""").find(fullText)
        if (driverPrefixMatch != null) {
            return driverPrefixMatch.groupValues[1]
        }

        // Analisa tokens separados por marcadores como " • ", " - ", ","
        val segments = listOfNotNull(title, text).flatMap { it.split("•", "-", ",", "|") }.map { it.trim() }
        for (seg in segments) {
            val words = seg.split(" ")
            if (words.size in 1..2 && words[0].firstOrNull()?.isUpperCase() == true) {
                val candidate = words[0]
                if (candidate.lowercase() !in listOf("uber", "99", "chegando", "motorista", "carro", "viagem", "pop", "comfort")) {
                    return seg
                }
            }
        }
        return null
    }

    internal fun extractVehicleModel(fullText: String, plate: String?, driverName: String?): String? {
        val segments = fullText.split("•", "-", "|", ",").map { it.trim() }
        for (seg in segments) {
            val lower = seg.lowercase()
            val containsColor = VEHICLE_COLORS.any { lower.contains(it) }
            val isPlate = plate != null && seg.contains(plate, ignoreCase = true)
            val isDriver = driverName != null && seg.contains(driverName, ignoreCase = true)
            val isApp = lower.contains("uber") || lower.contains("99") || lower.contains("pop")

            if ((containsColor || seg.matches(Regex(""".*\b(Civic|Corolla|Onix|HB20|Gol|Ka|Argo|Kwid|Polo|Cruze|Compass|Renegade|Etios|Logan|Sandero|Prisma|Cobalt|Versa|Fit|City|Yaris|Mobi|Compass|Creta|Tracker|T-Cross|Kicks)\b.*""", RegexOption.IGNORE_CASE))) && !isPlate && !isDriver && !isApp) {
                return seg
            }
        }

        // Se encontrou cor no texto, tenta extrair a frase com a cor
        for (color in VEHICLE_COLORS) {
            val match = Regex("""(?i)\b([A-ZÁÉÍÓÚÂÊÔÃÕÇ][a-záéíóúâêôãõç]+\s+)?($color)(\s+[A-ZÁÉÍÓÚÂÊÔÃÕÇ][a-záéíóúâêôãõç]+)?\b""").find(fullText)
            if (match != null && match.value.length > 3) {
                return match.value.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }

        return null
    }

    private fun formatStatusMessage(stage: RideStage, driverName: String?, text: String?, title: String): String {
        return when (stage) {
            RideStage.SEARCHING -> "Procurando motoristas próximos..."
            RideStage.ARRIVING -> {
                if (driverName != null) {
                    "$driverName está a caminho"
                } else {
                    "Motorista está a caminho"
                }
            }
            RideStage.ARRIVED -> {
                if (driverName != null) {
                    "$driverName está aguardando no ponto"
                } else {
                    "Veículo no ponto de embarque"
                }
            }
            RideStage.IN_TRIP -> "Viagem em andamento até o destino"
            RideStage.COMPLETED -> "Você chegou ao seu destino!"
        }
    }

    private fun calculateProgressPercent(
        sbn: StatusBarNotification,
        notification: Notification,
        stage: RideStage,
        etaMinutes: Int?
    ): Float {
        // Se a notificação do Android possuir progresso explícito
        val extras = notification.extras
        if (extras != null) {
            val max = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
            val current = extras.getInt(Notification.EXTRA_PROGRESS, 0)
            if (max > 0) {
                return (current.toFloat() / max).coerceIn(0.05f, 0.95f)
            }
        }

        return when (stage) {
            RideStage.SEARCHING -> 0.10f
            RideStage.ARRIVING -> {
                if (etaMinutes != null) {
                    when {
                        etaMinutes <= 1 -> 0.85f
                        etaMinutes <= 2 -> 0.72f
                        etaMinutes <= 4 -> 0.55f
                        etaMinutes <= 7 -> 0.38f
                        else -> 0.25f
                    }
                } else {
                    0.50f
                }
            }
            RideStage.ARRIVED -> 0.90f
            RideStage.IN_TRIP -> 0.60f
            RideStage.COMPLETED -> 1.0f
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

    private fun extractImages(notification: Notification, context: Context): Pair<Bitmap?, Bitmap?> {
        val largeIcon = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notification.getLargeIcon()?.loadDrawable(context)?.let { drawableToBitmap(it) }
            } else {
                @Suppress("DEPRECATION")
                notification.largeIcon
            }
        }.getOrNull()

        // Verifica imagem extra no BigPictureStyle se existir
        val extras = notification.extras
        val extraPicture = runCatching {
            @Suppress("DEPRECATION")
            extras?.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)
        }.getOrNull()

        return Pair(largeIcon, extraPicture)
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
