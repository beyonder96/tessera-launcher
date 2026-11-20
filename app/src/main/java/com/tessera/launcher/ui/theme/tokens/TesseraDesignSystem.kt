package com.tessera.launcher.ui.theme.tokens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

val LocalTesseraPalette = staticCompositionLocalOf {
    TesseraPalette.amoled(Color.White)
}

val LocalTesseraShapes = staticCompositionLocalOf {
    TesseraShapes()
}

val LocalTesseraSpacing = staticCompositionLocalOf {
    TesseraSpacing()
}

val LocalTesseraMotion = staticCompositionLocalOf {
    TesseraMotion()
}

val LocalVisagismProfile = staticCompositionLocalOf {
    VisagismProfile.SERENE
}

/**
 * Ponto de acesso centralizado aos Design Tokens do Tessera Launcher.
 * Uso: `Tessera.palette.textPrimary`, `Tessera.spacing.m`, `Tessera.shapes.card`, etc.
 */
object Tessera {
    val palette: TesseraPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalTesseraPalette.current

    val shapes: TesseraShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalTesseraShapes.current

    val spacing: TesseraSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalTesseraSpacing.current

    val motion: TesseraMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalTesseraMotion.current

    val profile: VisagismProfile
        @Composable
        @ReadOnlyComposable
        get() = LocalVisagismProfile.current
}

/**
 * Modifier de feedback tátil e físico para toques interativos.
 * Utiliza graphicsLayer com spring do Design System para latência zero de recomposição.
 */
fun Modifier.tesseraPressFeedback(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    interactionSource: MutableInteractionSource? = null,
    onHaptic: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val motion = Tessera.motion
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = motion.springPress,
        label = "tessera_press_scale"
    )

    androidx.compose.runtime.LaunchedEffect(isPressed) {
        if (isPressed && onHaptic) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
