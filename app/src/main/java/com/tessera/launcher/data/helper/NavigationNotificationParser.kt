package com.tessera.launcher.data.helper

import android.app.Notification
import android.content.Context
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.model.LiveActivityAction
import com.tessera.launcher.data.model.ManeuverType
import com.tessera.launcher.data.model.NavigationActivityDetails
import java.util.regex.Pattern

/**
 * Parser inteligente de notificações de navegação GPS curva a curva (Google Maps e Waze).
 * Extrai:
 * - Tipo de manobra (curva à esquerda/direita, rotatória, retorno, seguir em frente)
 * - Distância imediata até a manobra (ex: "em 200 m")
 * - Nome da próxima via / rua
 * - Tempo restante de viagem (ex: "18 min")
 * - Distância total restante (ex: "15 km")
 * - Horário estimado de chegada (ex: "14:35")
 * - Progresso do percurso
 * - Ações da notificação (ex: Encerrar rota, Mutar áudio)
 */
object NavigationNotificationParser {

    private val DISTANCE_MANEUVER_PATTERN = Pattern.compile(
        """(?i)(?:em\s*)?(\d+(?:[.,]\d+)?\s*(?:m|km|metros?|quilômetros?|ft|mi))\b"""
    )

    private val ETA_PATTERN = Pattern.compile(
        """(?i)(\d+)\s*(?:minutos?|mins?|m|h(?:\s*\d+\s*min)?)\b"""
    )

    private val REMAINING_DIST_PATTERN = Pattern.compile(
        """(?i)(\d+(?:[.,]\d+)?\s*(?:km|mi))\b"""
    )

    private val ARRIVAL_TIME_PATTERN = Pattern.compile(
        """\b(\d{1,2}:\d{2})\b"""
    )

    fun parse(
        sbn: StatusBarNotification,
        notification: Notification,
        context: Context,
        title: String,
        text: String?,
        subText: String?
    ): NavigationActivityDetails {
        val pkg = sbn.packageName
        val appName = when {
            pkg.contains("maps", ignoreCase = true) -> "Google Maps"
            pkg.contains("waze", ignoreCase = true) -> "Waze"
            else -> "Navegação"
        }

        val fullText = buildString {
            append(title)
            if (!text.isNullOrBlank()) append(" • ").append(text)
            if (!subText.isNullOrBlank()) append(" • ").append(subText)
        }

        // 1. Manobra e texto descritivo
        val maneuver = determineManeuver(fullText)
        val maneuverText = formatManeuverText(maneuver, title)

        // 2. Distância até a manobra imediata
        val distanceToManeuver = extractDistanceToManeuver(fullText, title)

        // 3. Nome da próxima via
        val nextStreetName = extractNextStreet(fullText, title, text)

        // 4. Métricas de viagem (ETA, Distância total, Chegada)
        val etaRemaining = extractEta(fullText)
        val distanceRemaining = extractDistanceRemaining(fullText)
        val estimatedArrival = extractArrivalTime(fullText)

        // 5. Progresso da rota
        val progressPercent = calculateProgress(notification)

        // 6. Ações da Notificação
        val actions = extractActions(notification)

        return NavigationActivityDetails(
            appName = appName,
            maneuver = maneuver,
            maneuverText = maneuverText,
            distanceToManeuver = distanceToManeuver,
            nextStreetName = nextStreetName,
            etaRemaining = etaRemaining,
            distanceRemaining = distanceRemaining,
            estimatedArrival = estimatedArrival,
            progressPercent = progressPercent,
            actions = actions
        )
    }

    internal fun determineManeuver(text: String): ManeuverType {
        val lower = text.lowercase()
        return when {
            lower.contains("destino") || lower.contains("destination") || lower.contains("chegada") -> ManeuverType.DESTINATION
            lower.contains("rotatória") || lower.contains("roundabout") -> ManeuverType.ROUNDABOUT
            lower.contains("retorno") || lower.contains("u-turn") -> ManeuverType.U_TURN
            lower.contains("suave à esquerda") || lower.contains("slight left") -> ManeuverType.TURN_SLIGHT_LEFT
            lower.contains("suave à direita") || lower.contains("slight right") -> ManeuverType.TURN_SLIGHT_RIGHT
            lower.contains("fechada à esquerda") || lower.contains("sharp left") -> ManeuverType.TURN_SHARP_LEFT
            lower.contains("fechada à direita") || lower.contains("sharp right") -> ManeuverType.TURN_SHARP_RIGHT
            lower.contains("vire à esquerda") || lower.contains("à esquerda") || lower.contains("turn left") -> ManeuverType.TURN_LEFT
            lower.contains("vire à direita") || lower.contains("à direita") || lower.contains("turn right") -> ManeuverType.TURN_RIGHT
            lower.contains("bifurcação") || lower.contains("fork") -> ManeuverType.FORK
            lower.contains("mantenha-se") || lower.contains("continue") || lower.contains("siga em frente") || lower.contains("straight") -> ManeuverType.STRAIGHT
            else -> ManeuverType.STRAIGHT
        }
    }

    internal fun formatManeuverText(maneuver: ManeuverType, title: String): String {
        return when (maneuver) {
            ManeuverType.TURN_LEFT -> "Vire à esquerda"
            ManeuverType.TURN_RIGHT -> "Vire à direita"
            ManeuverType.TURN_SLIGHT_LEFT -> "Mantenha-se à esquerda"
            ManeuverType.TURN_SLIGHT_RIGHT -> "Mantenha-se à direita"
            ManeuverType.TURN_SHARP_LEFT -> "Curva acentuada à esquerda"
            ManeuverType.TURN_SHARP_RIGHT -> "Curva acentuada à direita"
            ManeuverType.ROUNDABOUT -> "Entre na rotatória"
            ManeuverType.U_TURN -> "Faça o retorno"
            ManeuverType.DESTINATION -> "Você está chegando ao destino"
            ManeuverType.STRAIGHT -> if (title.isNotBlank() && title.length <= 25) title else "Siga em frente"
            else -> title
        }
    }

    internal fun extractDistanceToManeuver(fullText: String, title: String): String? {
        val matcher = DISTANCE_MANEUVER_PATTERN.matcher(title)
        if (matcher.find()) {
            val dist = matcher.group(1)
            return if (!dist.isNullOrBlank()) "em $dist" else null
        }

        val fullMatcher = DISTANCE_MANEUVER_PATTERN.matcher(fullText)
        if (fullMatcher.find()) {
            val dist = fullMatcher.group(1)
            return if (!dist.isNullOrBlank()) "em $dist" else null
        }
        return null
    }

    internal fun extractNextStreet(fullText: String, title: String, text: String?): String? {
        // Padrões como "na Av. Paulista", "na R. Vergueiro", "em direção a", "na Rua", "on Main St"
        val match = Regex("""(?i)(?:na|no|em direção a|onto|on)\s+([A-ZÁÉÍÓÚÂÊÔÃÕÇ0-9][a-záéíóúâêôãõçA-Z0-9\.\s\-]+)""").find(fullText)
        if (match != null) {
            val street = match.groupValues[1].split("•", "-", "|")[0].trim()
            if (street.length in 3..40) return street
        }

        // Tenta encontrar siglas conhecidas (Av., R., Rod., Alameda)
        val streetMatch = Regex("""(?i)\b((?:Av\.|Avenida|R\.|Rua|Rod\.|Rodovia|Al\.|Alameda|Estrada)\s+[A-ZÁÉÍÓÚÂÊÔÃÕÇ][a-záéíóúâêôãõçA-Z0-9\.\s\-]+)""").find(fullText)
        if (streetMatch != null) {
            return streetMatch.groupValues[1].split("•", "-", "|")[0].trim()
        }

        // Fallback: se o título não for apenas uma distância, usa ele
        if (title.length > 3 && !title.matches(Regex("""(?i)\d+\s*(m|km)"""))) {
            return title.replace(Regex("""(?i)(vire|curva|siga|em|mantenha-se|à direita|à esquerda)"""), "").trim(' ', '•', '-')
        }

        return text?.split("•", "-")?.firstOrNull()?.trim()
    }

    internal fun extractEta(text: String): String? {
        val matcher = ETA_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }

    internal fun extractDistanceRemaining(text: String): String? {
        val matcher = REMAINING_DIST_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    internal fun extractArrivalTime(text: String): String? {
        val matcher = ARRIVAL_TIME_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    private fun calculateProgress(notification: Notification): Float {
        val extras = notification.extras
        if (extras != null) {
            val max = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
            val current = extras.getInt(Notification.EXTRA_PROGRESS, 0)
            if (max > 0) {
                return (current.toFloat() / max).coerceIn(0.05f, 0.95f)
            }
        }
        return 0.50f
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
}
