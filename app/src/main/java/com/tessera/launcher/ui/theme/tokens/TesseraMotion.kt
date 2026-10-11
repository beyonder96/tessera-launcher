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
    val emphasizedEasing: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
)
