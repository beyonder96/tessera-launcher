package com.tessera.launcher.ui.design.glass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

val LocalHazeState = staticCompositionLocalOf {
    HazeState()
}

val LocalReduceTransparency = staticCompositionLocalOf {
    false
}

enum class GlassStyle {
    Regular,
    Thin,
    UltraThin,
    Chrome // Dock flutuante e barras de destaque
}

@Immutable
data class GlassMetrics(
    val blurRadius: Dp,
    val surfaceAlpha: Float,
    val borderAlpha: Float,
    val borderWidth: Dp
)

fun resolveGlassMetrics(style: GlassStyle, isDark: Boolean): GlassMetrics {
    return when (style) {
        GlassStyle.UltraThin -> GlassMetrics(
            blurRadius = 18.dp,
            surfaceAlpha = if (isDark) 0.35f else 0.45f,
            borderAlpha = if (isDark) 0.12f else 0.25f,
            borderWidth = 0.5.dp
        )
        GlassStyle.Thin -> GlassMetrics(
            blurRadius = 24.dp,
            surfaceAlpha = if (isDark) 0.50f else 0.62f,
            borderAlpha = if (isDark) 0.16f else 0.35f,
            borderWidth = 0.6.dp
        )
        GlassStyle.Regular -> GlassMetrics(
            blurRadius = 32.dp,
            surfaceAlpha = if (isDark) 0.68f else 0.78f,
            borderAlpha = if (isDark) 0.22f else 0.45f,
            borderWidth = 0.75.dp
        )
        GlassStyle.Chrome -> GlassMetrics(
            blurRadius = 40.dp,
            surfaceAlpha = if (isDark) 0.76f else 0.86f,
            borderAlpha = if (isDark) 0.28f else 0.55f,
            borderWidth = 1.dp
        )
    }
}

/**
 * Modifier Liquid Glass em 3 camadas com suporte a Haze 2.x e Fallback Adaptativo.
 */
fun Modifier.liquidGlass(
    state: HazeState? = null,
    shape: Shape = AppleShapes.card,
    style: GlassStyle = GlassStyle.Regular
): Modifier = composed {
    val haze = state ?: LocalHazeState.current
    val colors = LocalAppleColors.current
    val reduceTransparency = LocalReduceTransparency.current
    val metrics = resolveGlassMetrics(style, colors.isDark)

    val surfaceColor = if (colors.isDark) {
        Color(0xFF1C1C1E).copy(alpha = metrics.surfaceAlpha)
    } else {
        Color(0xFFFFFFFF).copy(alpha = metrics.surfaceAlpha)
    }

    val rimBorderBrush = Brush.verticalGradient(
        colors = if (colors.isDark) {
            listOf(
                Color.White.copy(alpha = metrics.borderAlpha),
                Color.White.copy(alpha = metrics.borderAlpha * 0.4f),
                Color.Transparent
            )
        } else {
            listOf(
                Color.White.copy(alpha = metrics.borderAlpha),
                Color(0x33000000).copy(alpha = metrics.borderAlpha * 0.3f),
                Color.Transparent
            )
        }
    )

    if (reduceTransparency || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        // Fallback rápido sem cálculo de blur de backdrop
        val fallbackSurface = if (colors.isDark) {
            Color(0xFF1C1C1E)
        } else {
            Color(0xFFFFFFFF)
        }
        this
            .clip(shape)
            .background(fallbackSurface)
            .border(metrics.borderWidth, colors.separator, shape)
    } else {
        // L1: Haze Backdrop Blur + L2: Tint + L3: Rim light especular
        this
            .clip(shape)
            .hazeBlur(
                input = HazeInput.Sources(haze),
                style = HazeBlurStyle {
                    blurRadius(metrics.blurRadius)
                }
            )
            .background(surfaceColor)
            .border(metrics.borderWidth, rimBorderBrush, shape)
    }
}
