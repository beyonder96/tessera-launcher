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

// Liquid Glass Tokens (Estilo iOS 27 / VisionOS - Frosted Glass Translúcido com Difração de Luz)
val LiquidGlassBackground = Color(0x75141720)
val LiquidGlassBackgroundHover = Color(0x901C202B)
val LiquidGlassSurfaceBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x66222836),
        Color(0x8511131A)
    )
)
val LiquidGlassBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x99FFFFFF),
        Color(0x33FFFFFF),
        Color(0x0AFFFFFF)
    )
)
val LiquidGlassBorder = Color(0x40FFFFFF)
val LiquidGlassSheenBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x28FFFFFF),
        Color(0x00FFFFFF)
    )
)

// Light Liquid Glass Tokens (Vidro Leitoso Translúcido com Brilho Especular Claro)
val LightLiquidGlassSurfaceBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xD9FFFFFF),
        Color(0xB3F0F2F5)
    )
)
val LightLiquidGlassBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xE6FFFFFF),
        Color(0x40FFFFFF),
        Color(0x20000000)
    )
)
val LightLiquidGlassSheenBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x55FFFFFF),
        Color(0x00FFFFFF)
    )
)

val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFF727275)
val TextTertiary = Color(0xFF48484A)

val AccentWhite = Color(0xFFFFFFFF)
val SkeletonBase = Color(0xFF141416)
val SkeletonHighlight = Color(0xFF222226)
