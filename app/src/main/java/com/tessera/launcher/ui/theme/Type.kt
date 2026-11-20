package com.tessera.launcher.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tessera.launcher.R
import java.io.File

val TesseraManrope = FontFamily(
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

fun resolveTesseraFontFamily(
    fontFamilyType: String,
    customFontPath: String = ""
): FontFamily {
    return when (fontFamilyType.lowercase()) {
        "system" -> FontFamily.SansSerif
        "custom" -> {
            if (customFontPath.isNotBlank()) {
                runCatching {
                    val file = File(customFontPath)
                    if (file.exists() && file.canRead()) {
                        FontFamily(Typeface.createFromFile(file))
                    } else {
                        TesseraManrope
                    }
                }.getOrDefault(TesseraManrope)
            } else {
                TesseraManrope
            }
        }
        else -> TesseraManrope
    }
}

fun TextStyle.tabularNumerals(): TextStyle = copy(fontFeatureSettings = "tnum")

fun tesseraTypography(fontFamily: FontFamily = TesseraManrope): Typography = Typography(
    // Display Large — Relógio de Autor em Destaque
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 42.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.1).sp,
        color = TextPrimary
    ),
    // Display Medium — Relógio Numérico Secundário
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.9).sp,
        color = TextPrimary
    ),
    // Headline Medium — Títulos de Cartão & Modais
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    // Título 1 — Cabeçalho Principal / Seções
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.4).sp,
        color = TextPrimary
    ),
    // Título 2 — Nomes de App / Títulos de Item
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2).sp,
        color = TextPrimary
    ),
    // Título 3 — Subtítulos de Widgets / Seções Secundárias
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    // Corpo 1 — Texto Principal / Input da Busca
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp,
        color = TextPrimary
    ),
    // Corpo 2 — Rótulos Secundários / Status
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = TextSecondary
    ),
    // Label Small — Letras A-Z / Chips / Badges
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
        color = TextTertiary
    )
)

val Typography = tesseraTypography(TesseraManrope)
