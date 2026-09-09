package com.tessera.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta Monocromática Ultra-Polida - Searcho & Niagara Style
val DarkBackground = Color(0xFF000000)
val DarkBackgroundTranslucent = Color.Transparent
val AmoledBlack = Color(0xFF000000)
val AmoledCardBackground = Color(0xFF000000) // 100% Preto Puro no modo AMOLED
val AmoledCardBorder = Color(0xFF18181E)
val DarkSurface = Color(0xFF0F0F12)
val DarkSurfaceVariant = Color(0xFF17171C)
val DarkSurfaceBorder = Color(0xFF1E1E24)
val DarkSurfaceBorderHover = Color(0xFF2E2E38)

// Modos Charcoal (Dark Slate) e Light
val CharcoalBackground = Color(0xFF121215)
val CharcoalCardBackground = Color(0xFF1A1A1F)
val CharcoalCardBorder = Color(0xFF26262E)
val LightBackground = Color(0xFFF6F7F9)
val LightCardBackground = Color(0xFFFFFFFF)
val LightCardBorder = Color(0xFFE5E7EB)
val LightTextPrimary = Color(0xFF111827)
val LightTextSecondary = Color(0xFF6B7280)
val LightTextTertiary = Color(0xFF9CA3AF)
val LightIconBackground = Color(0xFFF1F3F5)
val LightIconTint = Color(0xFF1F2937)
val LightDivider = Color(0xFFE5E7EB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF3F4F6)

// Liquid Glass Tokens Dinâmicos e Escalonados (Estilo Minimalista Frosted Glass)
fun liquidGlassSurfaceBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E222D).copy(alpha = 0.55f * a),
            Color(0xFF12141C).copy(alpha = 0.70f * a)
        )
    )
}

fun liquidGlassBorderBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.28f * a),
            Color.White.copy(alpha = 0.12f * a),
            Color.White.copy(alpha = 0.05f * a)
        )
    )
}

fun liquidGlassSheenBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f * a),
            Color.White.copy(alpha = 0.02f * a),
            Color.Transparent
        )
    )
}

fun lightLiquidGlassSurfaceBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.85f * a),
            Color(0xFFF1F3F7).copy(alpha = 0.72f * a)
        )
    )
}

fun lightLiquidGlassBorderBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.65f * a),
            Color.White.copy(alpha = 0.25f * a),
            Color(0x15000000).copy(alpha = 0.15f * a)
        )
    )
}

fun lightLiquidGlassSheenBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.25f * a),
            Color.Transparent
        )
    )
}

val LiquidGlassBackground = Color(0x75141720)
val LiquidGlassBackgroundHover = Color(0x901C202B)
val LiquidGlassSurfaceBrush = liquidGlassSurfaceBrush(1f)
val LiquidGlassBorderBrush = liquidGlassBorderBrush(1f)
val LiquidGlassBorder = Color(0x40FFFFFF)
val LiquidGlassSheenBrush = liquidGlassSheenBrush(1f)

val LightLiquidGlassSurfaceBrush = lightLiquidGlassSurfaceBrush(1f)
val LightLiquidGlassBorderBrush = lightLiquidGlassBorderBrush(1f)
val LightLiquidGlassSheenBrush = lightLiquidGlassSheenBrush(1f)

val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFF727275)
val TextTertiary = Color(0xFF48484A)

val AccentWhite = Color(0xFFFFFFFF)
val SkeletonBase = Color(0xFF141416)
val SkeletonHighlight = Color(0xFF222226)
