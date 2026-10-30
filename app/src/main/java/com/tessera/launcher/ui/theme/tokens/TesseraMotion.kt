package com.tessera.launcher.ui.theme.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * Sistema unificado de Física e Animações (Motion Tokens) do Tessera Launcher.
 * Elimina durações arbitrárias soltas em favor de modelos baseados em molas responsivas e curvas expressivas.
 */
@Immutable
data class TesseraMotion(
    // Press & Micro-interações táteis rápidas
    val springPress: SpringSpec<Float> = spring(
        stiffness = 1400f,
        dampingRatio = 0.9f
    ),
    // Transições de estado locais (expandir chips, toggle, filtros rápidos)
    val springQuick: SpringSpec<Float> = spring(
        stiffness = 900f,
        dampingRatio = 0.88f
    ),
    // Movimento padrão de interface (troca de seções, reorganização de listas)
    val springStandard: SpringSpec<Float> = spring(
        stiffness = 600f,
        dampingRatio = 0.85f
    ),
    // Movimentos espaciais amplos (Sheets, Gaveta de apps, Dock morphing)
    val springSpatial: SpringSpec<Float> = spring(
        stiffness = 380f,
        dampingRatio = 0.82f
    ),
    val springSpatialDp: SpringSpec<Dp> = spring(
        stiffness = 380f,
        dampingRatio = 0.82f
    ),
    val springSpatialOffset: SpringSpec<IntOffset> = spring(
        stiffness = 380f,
        dampingRatio = 0.82f
    ),
    // Curva com leve overshoot expressivo (para perfis VIBRANT)
    val springBouncy: SpringSpec<Float> = spring(
        stiffness = 450f,
        dampingRatio = 0.72f
    ),
    // Transições de opacidade / fade rápido
    val fadeQuick: FiniteAnimationSpec<Float> = tween(
        durationMillis = 150,
        easing = FastOutSlowInEasing
    ),
    // Fade padrão com curva refinada
    val fadeStandard: FiniteAnimationSpec<Float> = tween(
        durationMillis = 220,
        easing = FastOutSlowInEasing
    ),
    // Easing de desaceleração enfatizada (material curve para entradas suaves)
    val emphasizedEasing: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
    // Transição Espacial Cinematográfica para a Tela Lateral (-1): ~650ms de desaceleração aveludada sem quique
    val springSpatialCinematic: SpringSpec<Float> = LateralSpatialMath.springSpatialCinematic,
    // Curva espacial para troca entre abas da Tela Lateral (Central <-> Feed)
    val spatialTabEasing: Easing = LateralSpatialMath.spatialTabEasing,
    val spatialTabDurationMillis: Int = LateralSpatialMath.SPATIAL_TAB_DURATION_MILLIS
)

/**
 * Funções matemáticas puras e constantes de física espacial da Tela Lateral (-1).
 * Totalmente desacopladas de ciclo de vida para testes unitários precisos e sem alocação por frame.
 */
object LateralSpatialMath {
    const val MAX_NORMALIZED_VELOCITY = 1.55f
    const val OPEN_PROGRESS_THRESHOLD = 0.30f
    const val CLOSE_PROGRESS_THRESHOLD = 0.70f
    const val FLING_VELOCITY_THRESHOLD_PX = 420f
    const val SPATIAL_TAB_DURATION_MILLIS = 520

    val springSpatialCinematic: SpringSpec<Float> = spring(
        stiffness = 115f,
        dampingRatio = 0.94f,
        visibilityThreshold = 0.001f
    )

    val spatialTabEasing: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    fun clampNormalizedVelocity(velocityPxPerSec: Float, widthPx: Float): Float {
        if (widthPx <= 0f) return 0f
        return (velocityPxPerSec / widthPx).coerceIn(-MAX_NORMALIZED_VELOCITY, MAX_NORMALIZED_VELOCITY)
    }

    fun homeScale(progress: Float): Float = 1f - 0.085f * progress.coerceIn(0f, 1f)
    fun homeParallaxX(progress: Float, widthPx: Float): Float = widthPx * 0.24f * progress.coerceIn(0f, 1f)
    fun homeAlpha(progress: Float): Float = (1f - 0.82f * progress.coerceIn(0f, 1f)).coerceIn(0f, 1f)

    fun panelScale(progress: Float): Float = 0.945f + 0.055f * progress.coerceIn(0f, 1f)
    fun panelAlpha(progress: Float): Float = (0.38f + 0.62f * progress.coerceIn(0f, 1f)).coerceIn(0f, 1f)
    fun panelCornerFraction(progress: Float): Float =
        ((1f - progress.coerceIn(0f, 1f)) / 0.22f).coerceIn(0f, 1f)

    fun cascadeItemProgress(panelProgress: Float, index: Int): Float {
        val clampedIdx = index.coerceIn(0, 8)
        val start = 0.05f + clampedIdx * 0.045f // 0.05 .. 0.41
        val span = 0.56f
        val t = ((panelProgress.coerceIn(0f, 1f) - start) / span).coerceIn(0f, 1f)
        val inv = 1f - t
        return 1f - inv * inv * inv // Cubic ease-out simétrico para entrada e saída
    }
}
