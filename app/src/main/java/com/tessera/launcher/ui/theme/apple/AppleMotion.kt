package com.tessera.launcher.ui.theme.apple

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.PI
import kotlin.math.pow

/**
 * Converte a parametrização de mola nativa do SwiftUI / iOS (response, bounce)
 * em constantes físicas de Stiffness e DampingRatio para o Jetpack Compose.
 */
fun <T> appleSpring(response: Float = 0.5f, bounce: Float = 0f): SpringSpec<T> {
    val stiffness = (2f * PI.toFloat() / response.coerceAtLeast(0.01f)).pow(2)
    val dampingRatio = (1f - bounce).coerceIn(0.1f, 1.5f)
    return spring(stiffness = stiffness, dampingRatio = dampingRatio)
}

@Immutable
data class AppleMotion(
    val isReducedMotion: Boolean = false,

    // Smooth — Transições espaciais fluidas de tela, modais, sheets (response: 0.50, bounce: 0.0)
    val smoothFloat: AnimationSpec<Float> = if (isReducedMotion) tween(200, easing = FastOutSlowInEasing) else appleSpring(0.50f, 0.0f),
    val smoothDp: AnimationSpec<Dp> = if (isReducedMotion) tween(200, easing = FastOutSlowInEasing) else appleSpring(0.50f, 0.0f),
    val smoothOffset: AnimationSpec<IntOffset> = if (isReducedMotion) tween(200, easing = FastOutSlowInEasing) else appleSpring(0.50f, 0.0f),

    // Snappy — Toggles, chips, Dynamic Island expand/collapse (response: 0.35, bounce: 0.15)
    val snappyFloat: AnimationSpec<Float> = if (isReducedMotion) tween(150, easing = FastOutSlowInEasing) else appleSpring(0.35f, 0.15f),
    val snappyDp: AnimationSpec<Dp> = if (isReducedMotion) tween(150, easing = FastOutSlowInEasing) else appleSpring(0.35f, 0.15f),
    val snappyOffset: AnimationSpec<IntOffset> = if (isReducedMotion) tween(150, easing = FastOutSlowInEasing) else appleSpring(0.35f, 0.15f),

    // Bouncy — Toque longo, menus flutuantes Haptic Touch, modo jiggle (response: 0.45, bounce: 0.28)
    val bouncyFloat: AnimationSpec<Float> = if (isReducedMotion) tween(150, easing = FastOutSlowInEasing) else appleSpring(0.45f, 0.28f),
    val bouncyDp: AnimationSpec<Dp> = if (isReducedMotion) tween(150, easing = FastOutSlowInEasing) else appleSpring(0.45f, 0.28f),

    // Interactive — Feedback de toque imediato sem latência (response: 0.16, bounce: 0.0)
    val interactiveFloat: AnimationSpec<Float> = if (isReducedMotion) tween(80) else appleSpring(0.16f, 0.0f),

    // Fade padrão
    val fadeQuick: FiniteAnimationSpec<Float> = tween(140, easing = FastOutSlowInEasing),
    val fadeStandard: FiniteAnimationSpec<Float> = tween(220, easing = FastOutSlowInEasing)
)

val LocalAppleMotion = staticCompositionLocalOf {
    AppleMotion()
}
