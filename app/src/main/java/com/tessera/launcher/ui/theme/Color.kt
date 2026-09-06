package com.tessera.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta Monocromática Ultra-Polida - Searcho & Niagara Style
val DarkBackground = Color(0xFF050505)
val DarkBackgroundTranslucent = Color.Transparent
val DarkSurface = Color(0xFF121214)
val DarkSurfaceVariant = Color(0xFF18181A)
val DarkSurfaceBorder = Color(0xFF222224)
val DarkSurfaceBorderHover = Color(0xFF333336)

// Liquid Glass Tokens (Estilo iOS 27 / VisionOS)
val LiquidGlassBackground = Color(0x381A1C22)
val LiquidGlassBackgroundHover = Color(0x4D20242E)
val LiquidGlassSurfaceBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x522A2E38),
        Color(0x2E161820)
    )
)
val LiquidGlassBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x80FFFFFF),
        Color(0x24FFFFFF),
        Color(0x0AFFFFFF)
    )
)
val LiquidGlassBorder = Color(0x38FFFFFF)

val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFF727275)
val TextTertiary = Color(0xFF48484A)

val AccentWhite = Color(0xFFFFFFFF)
val SkeletonBase = Color(0xFF141416)
val SkeletonHighlight = Color(0xFF222226)
