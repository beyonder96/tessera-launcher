package com.tessera.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta Monocromática Ultra-Polida — Padrão Cupertino & Minimalismo Calmo
val DarkBackground = Color(0xFF0B0B0D) // Cinza escuro profundo para evitar black smear em OLED 120Hz
val DarkBackgroundTranslucent = Color.Transparent
val AmoledBlack = Color(0xFF000000)
val AmoledCardBackground = Color(0xFF000000)
val AmoledCardBorder = Color(0x15FFFFFF) // Micro-borda de luz especular a 8%
val DarkSurface = Color(0xFF1C1C1E) // Superfície com elevação de profundidade
val DarkSurfaceVariant = Color(0xFF2C2C2E)
val DarkSurfaceBorder = Color(0x1AFFFFFF) // Contorno vítreo sutil a 10%
val DarkSurfaceBorderHover = Color(0x30FFFFFF)

// Modos Charcoal (Dark Slate) e Light
val CharcoalBackground = Color(0xFF121215)
val CharcoalCardBackground = Color(0xFF1A1A1F)
val CharcoalCardBorder = Color(0x22FFFFFF)
val LightBackground = Color(0xFFF2F2F7) // iOS System Grouped Background
val LightCardBackground = Color(0xFFFFFFFF)
val LightCardBorder = Color(0x15000000)
val LightTextPrimary = Color(0xFF1C1C1E)
val LightTextSecondary = Color(0xFF8E8E93)
val LightTextTertiary = Color(0xFFAEAEB2)
val LightIconBackground = Color(0xFFE5E5EA)
val LightIconTint = Color(0xFF1C1C1E)
val LightDivider = Color(0x1A000000)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF2F2F7)

// Tokens de Vidro Translúcido Cupertino (Apple Vibrancy & Frosted Lens)
fun liquidGlassSurfaceBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1C1C1E).copy(alpha = 0.68f * a),
            Color(0xFF121214).copy(alpha = 0.82f * a)
        )
    )
}

fun liquidGlassBorderBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.14f * a), // Micro-borda de luz superior (rim light)
            Color.White.copy(alpha = 0.05f * a),
            Color.Transparent
        )
    )
}

fun liquidGlassSheenBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.05f * a),
            Color.White.copy(alpha = 0.01f * a),
            Color.Transparent
        )
    )
}

fun lightLiquidGlassSurfaceBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.82f * a),
            Color(0xFFF2F2F7).copy(alpha = 0.72f * a)
        )
    )
}

fun lightLiquidGlassBorderBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.50f * a),
            Color.White.copy(alpha = 0.18f * a),
            Color(0x10000000).copy(alpha = 0.08f * a)
        )
    )
}

fun lightLiquidGlassSheenBrush(alphaFactor: Float = 1f): Brush {
    val a = alphaFactor.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.20f * a),
            Color.Transparent
        )
    )
}

val LiquidGlassBackground = Color(0x801C1C1E)
val LiquidGlassBackgroundHover = Color(0x992C2C2E)
val LiquidGlassSurfaceBrush = liquidGlassSurfaceBrush(1f)
val LiquidGlassBorderBrush = liquidGlassBorderBrush(1f)
val LiquidGlassBorder = Color(0x22FFFFFF)
val LiquidGlassSheenBrush = liquidGlassSheenBrush(1f)

val LightLiquidGlassSurfaceBrush = lightLiquidGlassSurfaceBrush(1f)
val LightLiquidGlassBorderBrush = lightLiquidGlassBorderBrush(1f)
val LightLiquidGlassSheenBrush = lightLiquidGlassSheenBrush(1f)

// Tipografia Harmônica — Padrão San Francisco
val TextPrimary = Color(0xFFF2F2F7) // Off-white padrão Apple
val TextSecondary = Color(0xFF8E8E93) // Cinza secundário equilibrado
val TextTertiary = Color(0xFF636366) // Cinza terciário discreto

val AccentWhite = Color(0xFFFFFFFF)
val SkeletonBase = Color(0xFF1C1C1E)
val SkeletonHighlight = Color(0xFF2C2C2E)

// Efeitos de Luz & Reflexo Especular Neutro (Substituição de Neons por Crystalline Sheen)
val GeminiCyan = Color(0xFF5AC8FA)
val GeminiBlue = Color(0xFF007AFF)
val GeminiPurple = Color(0xFF5856D6)
val GeminiMagenta = Color(0xFFAF52DE)
val GeminiAmber = Color(0xFFFF9500)

val PhoneAiWhiteGlowColors = listOf(
    Color(0x10FFFFFF),
    Color(0x40FFFFFF),
    Color(0x80FFFFFF),
    Color(0x80FFFFFF),
    Color(0x40FFFFFF),
    Color(0x10FFFFFF)
)

val PhoneAiLightGlowColors = listOf(
    Color(0x0C1C1C1E),
    Color(0x251C1C1E),
    Color(0x401C1C1E),
    Color(0x401C1C1E),
    Color(0x251C1C1E),
    Color(0x0C1C1C1E)
)

fun phoneAiWhiteBrush(alpha: Float = 1f): Brush {
    val a = alpha.coerceIn(0f, 1f)
    return Brush.linearGradient(
        colors = PhoneAiWhiteGlowColors.map { it.copy(alpha = it.alpha * a) }
    )
}

// Luz Perimétrica Cristalina Subtil (sem anéis de circo multicoloridos)
val GeminiGlowColors = listOf(
    Color(0x18FFFFFF),
    Color(0x35FFFFFF),
    Color(0x60FFFFFF),
    Color(0x35FFFFFF),
    Color(0x18FFFFFF),
    Color(0x35FFFFFF)
)

fun geminiSweepBrush(alpha: Float = 1f): Brush {
    val a = alpha.coerceIn(0f, 1f)
    return Brush.sweepGradient(
        colors = GeminiGlowColors.map { it.copy(alpha = it.alpha * a) }
    )
}

fun geminiLinearBrush(alpha: Float = 1f): Brush {
    val a = alpha.coerceIn(0f, 1f)
    return Brush.linearGradient(
        colors = GeminiGlowColors.map { it.copy(alpha = it.alpha * a) }
    )
}

// Accent Tints (Cores de Destaque Pop)
val AccentMonochrome = Color(0xFFFFFFFF)
val AccentNothingRed = Color(0xFFFF2E2E)
val AccentNeonGreen = Color(0xFF00E676)
val AccentElectricAmber = Color(0xFFFF9100)
val AccentCyanPulse = Color(0xFF00E5FF)
val AccentPurpleHaze = Color(0xFFB388FF)

data class AccentColorOption(
    val id: String,
    val displayName: String,
    val color: Color
)

val ACCENT_COLOR_OPTIONS = listOf(
    AccentColorOption("MONOCHROME", "Monocromático", AccentMonochrome),
    AccentColorOption("NOTHING_RED", "Nothing Red", AccentNothingRed),
    AccentColorOption("NEON_GREEN", "Neon Green", AccentNeonGreen),
    AccentColorOption("ELECTRIC_AMBER", "Electric Amber", AccentElectricAmber),
    AccentColorOption("CYAN_PULSE", "Cyan Pulse", AccentCyanPulse),
    AccentColorOption("PURPLE_HAZE", "Purple Haze", AccentPurpleHaze)
)

fun getAccentColor(name: String, isLightMode: Boolean = false): Color {
    val option = ACCENT_COLOR_OPTIONS.firstOrNull { it.id.equals(name, ignoreCase = true) }
    val base = option?.color ?: AccentMonochrome
    return if (isLightMode && base == AccentMonochrome) Color(0xFF111827) else base
}

