package com.tessera.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta Monocromática Ultra-Polida - Searcho & Niagara Style
val DarkBackground = Color(0xFF000000)
val DarkBackgroundTranslucent = Color.Transparent
val AmoledBlack = Color(0xFF000000)
val AmoledCardBackground = Color(0xFF0C0C0F)
val AmoledCardBorder = Color(0xFF1C1C24)
val DarkSurface = Color(0xFF0F0F12)
val DarkSurfaceVariant = Color(0xFF17171C)
val DarkSurfaceBorder = Color(0xFF1E1E24)
val DarkSurfaceBorderHover = Color(0xFF2E2E38)

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

val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFF727275)
val TextTertiary = Color(0xFF48484A)

val AccentWhite = Color(0xFFFFFFFF)
val SkeletonBase = Color(0xFF141416)
val SkeletonHighlight = Color(0xFF222226)
