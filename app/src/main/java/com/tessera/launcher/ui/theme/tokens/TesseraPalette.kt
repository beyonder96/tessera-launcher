package com.tessera.launcher.ui.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Paleta de cores semântica do Tessera Launcher.
 * Garante que nenhum componente dependa de constantes hardcoded como `Color(0x...)`.
 */
@Immutable
data class TesseraPalette(
    val background: Color,
    val backgroundTranslucent: Color,
    val scrim: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceBorder: Color,
    val surfaceBorderHighlight: Color,
    val glassFill: Color,
    val glassFillEnd: Color,
    val glassStrokeTop: Color,
    val glassStrokeBottom: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val iconTint: Color,
    val accent: Color,
    val accentMuted: Color,
    val divider: Color,
    val ripple: Color,
    val shadowAmbient: Color,
    val shadowSpot: Color,
    val destructive: Color,
    val destructiveMuted: Color,
    val isLight: Boolean
) {
    companion object {
        fun amoled(accent: Color): TesseraPalette = TesseraPalette(
            background = Color(0xFF000000),
            backgroundTranslucent = Color(0x99000000),
            scrim = Color(0xFF0A0A0D),
            surface = Color(0xFF0D0D10),
            surfaceSubtle = Color(0xFF16161B),
            surfaceBorder = Color(0x18FFFFFF),
            surfaceBorderHighlight = Color(0x33FFFFFF),
            glassFill = Color(0xCC121216),
            glassFillEnd = Color(0xE608080A),
            glassStrokeTop = Color(0x38FFFFFF),
            glassStrokeBottom = Color(0x0CFFFFFF),
            textPrimary = Color(0xFFF5F5F7),
            textSecondary = Color(0xFF8E8E93),
            textTertiary = Color(0xFF636366),
            iconTint = Color(0xFFA0A0AA),
            accent = accent,
            accentMuted = accent.copy(alpha = 0.16f),
            divider = Color(0x15FFFFFF),
            ripple = Color(0x1FFFFFFF),
            shadowAmbient = Color(0x40000000),
            shadowSpot = Color(0x73000000),
            destructive = Color(0xFFEF5350),
            destructiveMuted = Color(0x26EF5350),
            isLight = false
        )

        fun dark(accent: Color): TesseraPalette = TesseraPalette(
            background = Color(0xFF0B0B0D),
            backgroundTranslucent = Color(0xAA0B0B0D),
            scrim = Color(0xFF0F0F14),
            surface = Color(0xFF1C1C1E),
            surfaceSubtle = Color(0xFF2C2C2E),
            surfaceBorder = Color(0x1AFFFFFF),
            surfaceBorderHighlight = Color(0x35FFFFFF),
            glassFill = Color(0xBF1E1E24),
            glassFillEnd = Color(0xD9121217),
            glassStrokeTop = Color(0x40FFFFFF),
            glassStrokeBottom = Color(0x0EFFFFFF),
            textPrimary = Color(0xFFF2F2F7),
            textSecondary = Color(0xFF8E8E93),
            textTertiary = Color(0xFF636366),
            iconTint = Color(0xFFA0A0AA),
            accent = accent,
            accentMuted = accent.copy(alpha = 0.18f),
            divider = Color(0x18FFFFFF),
            ripple = Color(0x20FFFFFF),
            shadowAmbient = Color(0x40000000),
            shadowSpot = Color(0x73000000),
            destructive = Color(0xFFEF5350),
            destructiveMuted = Color(0x26EF5350),
            isLight = false
        )

        fun charcoal(accent: Color): TesseraPalette = TesseraPalette(
            background = Color(0xFF121215),
            backgroundTranslucent = Color(0xB0121215),
            scrim = Color(0xFF141419),
            surface = Color(0xFF1A1A1F),
            surfaceSubtle = Color(0xFF25252C),
            surfaceBorder = Color(0x22FFFFFF),
            surfaceBorderHighlight = Color(0x3DFFFFFF),
            glassFill = Color(0xC4202027),
            glassFillEnd = Color(0xDE14141A),
            glassStrokeTop = Color(0x44FFFFFF),
            glassStrokeBottom = Color(0x10FFFFFF),
            textPrimary = Color(0xFFF0F0F5),
            textSecondary = Color(0xFF8E8E98),
            textTertiary = Color(0xFF656570),
            iconTint = Color(0xFFA4A4B0),
            accent = accent,
            accentMuted = accent.copy(alpha = 0.18f),
            divider = Color(0x1AFFFFFF),
            ripple = Color(0x22FFFFFF),
            shadowAmbient = Color(0x40000000),
            shadowSpot = Color(0x73000000),
            destructive = Color(0xFFEF5350),
            destructiveMuted = Color(0x26EF5350),
            isLight = false
        )

        fun light(accent: Color): TesseraPalette = TesseraPalette(
            background = Color(0xFFF7F4EB),
            backgroundTranslucent = Color(0xD0F7F4EB),
            scrim = Color(0xFFE8E4D8),
            surface = Color(0xFFEFECE2),
            surfaceSubtle = Color(0xFFE8E4D8),
            surfaceBorder = Color(0xFFDDD8CB),
            surfaceBorderHighlight = Color(0x2E1C1917),
            glassFill = Color(0xCCFDFBF5),
            glassFillEnd = Color(0xE0ECE7DA),
            glassStrokeTop = Color(0xE6FFFFFF),
            glassStrokeBottom = Color(0x241C1917),
            textPrimary = Color(0xFF1C1917),
            textSecondary = Color(0xFF57534E),
            textTertiary = Color(0xFF625B55),
            iconTint = Color(0xFF374151),
            accent = if (accent == Color(0xFFFFFFFF)) Color(0xFF334155) else accent,
            accentMuted = (if (accent == Color(0xFFFFFFFF)) Color(0xFF334155) else accent).copy(alpha = 0.16f),
            divider = Color(0x181C1917),
            ripple = Color(0x141C1917),
            shadowAmbient = Color(0x14000000),
            shadowSpot = Color(0x22000000),
            destructive = Color(0xFFD32F2F),
            destructiveMuted = Color(0x22D32F2F),
            isLight = true
        )
    }
}
