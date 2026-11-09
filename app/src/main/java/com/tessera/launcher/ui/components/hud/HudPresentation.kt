package com.tessera.launcher.ui.components.hud

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType

@Immutable
data class HudPresentation(
    val icon: ImageVector,
    val accentColor: Color,
    val title: String,
    val subtitle: String?,
    val criticalText: String?,
    val progressPercent: Float?,
    val isChronometer: Boolean = false,
    val chronometerBaseMs: Long? = null,
    val isCountDown: Boolean = false
)

object HudPresentationMapper {

    fun map(activity: LiveActivityInfo, defaultAccent: Color): HudPresentation {
        val baseAccent = activity.brandColor?.let { Color(it) } ?: when (activity.type) {
            LiveActivityType.CALL -> Color(0xFF00E676)
            LiveActivityType.RIDE -> Color(0xFF00E5FF)
            LiveActivityType.DELIVERY -> Color(0xFFFF2E2E)
            LiveActivityType.PACKAGE -> Color(0xFFFFE600)
            LiveActivityType.TIMER -> Color(0xFFFF9100)
            LiveActivityType.MARKET -> Color(0xFF00E676)
            LiveActivityType.TIME_CLOCK -> Color(0xFF3D5AFE)
            LiveActivityType.FOCUS -> Color(0xFFFFAB00)
            LiveActivityType.NAVIGATION -> Color(0xFF10B981)
            LiveActivityType.PROGRESS -> Color(0xFF00E5FF)
            LiveActivityType.FITNESS -> Color(0xFFFC4C02)
            LiveActivityType.OTHER -> defaultAccent
        }

        // Garante contraste: se a cor for muito escura, clareia para visibilidade no fundo preto
        val accentColor = adjustLuminanceForBlackSurface(baseAccent)

        val icon = when (activity.type) {
            LiveActivityType.RIDE -> Icons.Outlined.DirectionsCar
            LiveActivityType.DELIVERY -> Icons.Outlined.DeliveryDining
            LiveActivityType.PACKAGE -> Icons.Outlined.LocalShipping
            LiveActivityType.TIMER -> Icons.Outlined.Timer
            LiveActivityType.NAVIGATION -> Icons.Outlined.Navigation
            LiveActivityType.CALL -> Icons.Outlined.Call
            LiveActivityType.FITNESS -> Icons.AutoMirrored.Outlined.DirectionsRun
            LiveActivityType.MARKET -> Icons.Outlined.ShoppingCart
            LiveActivityType.TIME_CLOCK -> Icons.Outlined.Schedule
            LiveActivityType.FOCUS -> Icons.Outlined.SelfImprovement
            LiveActivityType.PROGRESS -> Icons.Outlined.Sync
            LiveActivityType.OTHER -> Icons.Outlined.NotificationsActive
        }

        val progressPercent = activity.progress?.let { (it.toFloat() / 100f).coerceIn(0f, 1f) }

        return HudPresentation(
            icon = icon,
            accentColor = accentColor,
            title = activity.appName.ifBlank { activity.title },
            subtitle = activity.subtitle,
            criticalText = activity.criticalText ?: activity.extraInfo,
            progressPercent = progressPercent,
            isChronometer = activity.chronometerBaseMs != null,
            chronometerBaseMs = activity.chronometerBaseMs,
            isCountDown = activity.isChronometerCountDown
        )
    }

    private fun adjustLuminanceForBlackSurface(color: Color): Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b

        return if (luminance < 0.25f) {
            // Aumenta o brilho mantendo a matiz
            Color(
                red = (r + 0.4f).coerceAtMost(1f),
                green = (g + 0.4f).coerceAtMost(1f),
                blue = (b + 0.4f).coerceAtMost(1f),
                alpha = 1f
            )
        } else {
            color
        }
    }
}
