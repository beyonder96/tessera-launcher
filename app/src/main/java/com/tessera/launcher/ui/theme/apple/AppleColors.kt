package com.tessera.launcher.ui.theme.apple

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta semântica oficial inspirada no Human Interface Guidelines (HIG) da Apple.
 * Utiliza nomes semânticos rigorosos para adaptação visual perfeita em Light, Dark e Liquid Glass.
 */
@Immutable
data class AppleColors(
    val systemBackground: Color,
    val secondarySystemBackground: Color,
    val tertiarySystemBackground: Color,
    val groupedBackground: Color,
    val secondaryGrouped: Color,
    val tertiaryGrouped: Color,
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val quaternaryLabel: Color,
    val separator: Color,
    val opaqueSeparator: Color,
    val fill: Color,
    val secondaryFill: Color,
    val tertiaryFill: Color,
    val glassTint: Color,
    val glassRim: Color,
    val glassSurface: Color,
    val accent: Color,
    val accentMuted: Color,
    val isDark: Boolean
) {
    companion object {
        fun light(accent: Color): AppleColors = AppleColors(
            systemBackground = Color(0xFFFFFFFF),
            secondarySystemBackground = Color(0xFFF2F2F7),
            tertiarySystemBackground = Color(0xFFFFFFFF),
            groupedBackground = Color(0xFFF2F2F7),
            secondaryGrouped = Color(0xFFFFFFFF),
            tertiaryGrouped = Color(0xFFF2F2F7),
            label = Color(0xFF000000),
            secondaryLabel = Color(0x993C3C43), // 60%
            tertiaryLabel = Color(0x4D3C3C43),  // 30%
            quaternaryLabel = Color(0x2E3C3C43),// 18%
            separator = Color(0x473C3C43),      // 28%
            opaqueSeparator = Color(0xFFC6C6C8),
            fill = Color(0x1F787880),           // 12%
            secondaryFill = Color(0x14787880),  // 8%
            tertiaryFill = Color(0x0F787880),   // 6%
            glassTint = Color(0xF2FFFFFF),
            glassRim = Color(0x40FFFFFF),
            glassSurface = Color(0xCCFFFFFF),
            accent = accent,
            accentMuted = accent.copy(alpha = 0.14f),
            isDark = false
        )

        fun dark(accent: Color): AppleColors = AppleColors(
            systemBackground = Color(0xFF000000),
            secondarySystemBackground = Color(0xFF1C1C1E),
            tertiarySystemBackground = Color(0xFF2C2C2E),
            groupedBackground = Color(0xFF000000),
            secondaryGrouped = Color(0xFF1C1C1E),
            tertiaryGrouped = Color(0xFF2C2C2E),
            label = Color(0xFFFFFFFF),
            secondaryLabel = Color(0x99EBEBF5), // 60%
            tertiaryLabel = Color(0x4DEBEBF5),  // 30%
            quaternaryLabel = Color(0x29EBEBF5),// 16%
            separator = Color(0x61545458),      // 38%
            opaqueSeparator = Color(0xFF38383A),
            fill = Color(0x33787880),           // 20%
            secondaryFill = Color(0x29787880),  // 16%
            tertiaryFill = Color(0x1F787880),   // 12%
            glassTint = Color(0xD91C1C1E),
            glassRim = Color(0x26FFFFFF),
            glassSurface = Color(0x801C1C1E),
            accent = accent,
            accentMuted = accent.copy(alpha = 0.18f),
            isDark = true
        )
    }
}

/**
 * Cores de Sistema Oficiais da Apple (System Tints).
 */
enum class AppleTint(
    val id: String,
    val displayName: String,
    val light: Color,
    val dark: Color
) {
    BLUE("BLUE", "Azul", Color(0xFF007AFF), Color(0xFF0A84FF)),
    INDIGO("INDIGO", "Índigo", Color(0xFF5856D6), Color(0xFF5E5CE6)),
    PURPLE("PURPLE", "Roxo", Color(0xFFAF52DE), Color(0xFFBF5AF2)),
    PINK("PINK", "Rosa", Color(0xFFFF2D55), Color(0xFFFF375F)),
    RED("RED", "Vermelho", Color(0xFFFF3B30), Color(0xFFFF453A)),
    ORANGE("ORANGE", "Laranja", Color(0xFFFF9500), Color(0xFFFF9F0A)),
    YELLOW("YELLOW", "Amarelo", Color(0xFFFFCC00), Color(0xFFFFD60A)),
    GREEN("GREEN", "Verde", Color(0xFF34C759), Color(0xFF30D158)),
    MINT("MINT", "Menta", Color(0xFF00C7BE), Color(0xFF63E6E2)),
    GRAPHITE("GRAPHITE", "Grafite", Color(0xFF8E8E93), Color(0xFF98989D));

    fun resolve(isDark: Boolean): Color = if (isDark) dark else light

    companion object {
        fun fromId(id: String?): AppleTint {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BLUE
        }
    }
}

val LocalAppleColors = staticCompositionLocalOf {
    AppleColors.dark(AppleTint.BLUE.dark)
}
