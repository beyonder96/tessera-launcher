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
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceBorder: Color,
    val surfaceBorderHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentMuted: Color,
    val divider: Color,
    val ripple: Color,
    val isLight: Boolean
) {
    companion object {
        fun amoled(accent: Color): TesseraPalette = TesseraPalette(
            background = Color(0xFF000000),
            backgroundTranslucent = Color(0x99000000),
            surface = Color(0xFF0D0D10),
            surfaceSubtle = Color(0xFF16161B),
            surfaceBorder = Color(0x18FFFFFF),
            surfaceBorderHighlight = Color(0x33FFFFFF),
            textPrimary = Color(0xFFF5F5F7),
            textSecondary = Color(0xFF8E8E93),
            textTertiary = Color(0xFF636366),
            accent = accent,
            accentMuted = accent.copy(alpha = 0.16f),
            divider = Color(0x15FFFFFF),
            ripple = Color(0x1FFFFFFF),
            isLight = false
        )

        fun dark(accent: Color): TesseraPalette = TesseraPalette(
            background = Color(0xFF0B0B0D),
            backgroundTranslucent = Color(0xAA0B0B0D),
            surface = Color(0xFF1C1C1E),
            surfaceSubtle = Color(0xFF2C2C2E),
            surfaceBorder = Color(0x1AFFFFFF),
            surfaceBorderHighlight = Color(0x35FFFFFF),
            textPrimary = Color(0xFFF2F2F7),
            textSecondary = Color(0xFF8E8E93),
            textTertiary = Color(0xFF636366),
            accent = accent,
            accentMuted = accent.copy(alpha = 0.18f),
            divider = Color(0x18FFFFFF),
            ripple = Color(0x20FFFFFF),
            isLight = false
        )

        fun charcoal(accent: Color): TesseraPalette = TesseraPalette(
            background = Color(0xFF121215),
            backgroundTranslucent = Color(0xB0121215),
            surface = Color(0xFF1A1A1F),
            surfaceSubtle = Color(0xFF25252C),
            surfaceBorder = Color(0x22FFFFFF),
            surfaceBorderHighlight = Color(0x3DFFFFFF),
            textPrimary = Color(0xFFF0F0F5),
            textSecondary = Color(0xFF8E8E98),
            textTertiary = Color(0xFF656570),
            accent = accent,
            accentMuted = accent.copy(alpha = 0.18f),
            divider = Color(0x1AFFFFFF),
            ripple = Color(0x22FFFFFF),
            isLight = false
        )

        fun light(accent: Color): TesseraPalette = TesseraPalette(
            background = Color(0xFFF7F4EB),
            backgroundTranslucent = Color(0xD0F7F4EB),
            surface = Color(0xFFEFECE2),
            surfaceSubtle = Color(0xFFE8E4D8),
            surfaceBorder = Color(0xFFDDD8CB),
            surfaceBorderHighlight = Color(0x2E1C1917),
            textPrimary = Color(0xFF1C1917),
            textSecondary = Color(0xFF57534E),
            textTertiary = Color(0xFF625B55),
            accent = if (accent == Color(0xFFFFFFFF)) Color(0xFF334155) else accent,
            accentMuted = (if (accent == Color(0xFFFFFFFF)) Color(0xFF334155) else accent).copy(alpha = 0.16f),
            divider = Color(0x181C1917),
            ripple = Color(0x141C1917),
            isLight = true
        )
    }
}
