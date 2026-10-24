package com.tessera.launcher.ui.theme.apple

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.tessera.launcher.R
import kotlin.math.exp

/**
 * Fórmula de tracking dinâmico da família Inter (equivalente ao Tracking Óptico do San Francisco).
 * Valores expressos em 'em' para escalonamento automático de Dynamic Type.
 */
fun interTracking(sizeSp: Float): TextUnit {
    val emValue = -0.0223f + 0.185f * exp(-0.1745f * sizeSp)
    return emValue.em
}

/**
 * Criação de FontFamily com suporte a Variação de Peso (wght) e Tamanho Óptico (opsz).
 * Tamanhos maiores (>= 32sp) ativam o optical sizing de display para contraste e elegância.
 */
fun interFont(weight: Int, opsz: Float = 14f): FontFamily {
    val clampedOpsz = opsz.coerceIn(14f, 32f)
    return FontFamily(
        Font(
            resId = R.font.inter_variable,
            weight = FontWeight(weight),
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight),
                FontVariation.Setting("opsz", clampedOpsz)
            )
        )
    )
}

private fun style(
    sizeSp: Float,
    lineHeightSp: Float,
    weight: Int,
    opsz: Float = sizeSp
): TextStyle {
    return TextStyle(
        fontFamily = interFont(weight, opsz),
        fontWeight = FontWeight(weight),
        fontSize = sizeSp.sp,
        lineHeight = lineHeightSp.sp,
        letterSpacing = interTracking(sizeSp)
    )
}

@Immutable
data class AppleTypography(
    // Relógio Hero da Tela Inicial (Display Extra Large com números tabulares)
    val heroClock: TextStyle = TextStyle(
        fontFamily = interFont(400, opsz = 32f),
        fontWeight = FontWeight.Normal,
        fontSize = 112.sp,
        lineHeight = 112.sp,
        letterSpacing = (-0.03).em
    ),
    // Títulos da Hierarquia Apple HIG
    val largeTitle: TextStyle = style(34f, 41f, 700, opsz = 32f),
    val title1: TextStyle = style(28f, 34f, 700, opsz = 28f),
    val title2: TextStyle = style(22f, 28f, 700, opsz = 22f),
    val title3: TextStyle = style(20f, 25f, 600, opsz = 20f),
    val headline: TextStyle = style(17f, 22f, 600, opsz = 17f),
    val body: TextStyle = style(17f, 22f, 400, opsz = 17f),
    val bodyMedium: TextStyle = style(17f, 22f, 500, opsz = 17f),
    val drawerApp: TextStyle = style(26f, 32f, 500, opsz = 26f),
    val callout: TextStyle = style(16f, 21f, 400, opsz = 16f),
    val subheadline: TextStyle = style(15f, 20f, 400, opsz = 15f),
    val subheadlineBold: TextStyle = style(15f, 20f, 600, opsz = 15f),
    val footnote: TextStyle = style(13f, 18f, 400, opsz = 14f),
    val footnoteBold: TextStyle = style(13f, 18f, 600, opsz = 14f),
    val caption1: TextStyle = style(12f, 16f, 400, opsz = 14f),
    val caption2: TextStyle = style(11f, 13f, 500, opsz = 14f)
)

val LocalAppleTypography = staticCompositionLocalOf {
    AppleTypography()
}
