package com.tessera.launcher.ui.components

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Matrizes 5x7 de pontos para dígitos de 0 a 9 e dois-pontos (:), inspiradas no design Dot-Matrix do Nothing OS.
 */
private val DIGIT_MATRICES = mapOf(
    '0' to arrayOf(
        " 111 ",
        "1   1",
        "1  11",
        "1 1 1",
        "11  1",
        "1   1",
        " 111 "
    ),
    '1' to arrayOf(
        "  1  ",
        " 11  ",
        "  1  ",
        "  1  ",
        "  1  ",
        "  1  ",
        " 111 "
    ),
    '2' to arrayOf(
        " 111 ",
        "1   1",
        "    1",
        "  11 ",
        " 1   ",
        "1    ",
        "11111"
    ),
    '3' to arrayOf(
        "1111 ",
        "    1",
        "    1",
        " 111 ",
        "    1",
        "    1",
        "1111 "
    ),
    '4' to arrayOf(
        "1   1",
        "1   1",
        "1   1",
        "11111",
        "    1",
        "    1",
        "    1"
    ),
    '5' to arrayOf(
        "11111",
        "1    ",
        "1111 ",
        "    1",
        "    1",
        "1   1",
        " 111 "
    ),
    '6' to arrayOf(
        " 111 ",
        "1    ",
        "1111 ",
        "1   1",
        "1   1",
        "1   1",
        " 111 "
    ),
    '7' to arrayOf(
        "11111",
        "    1",
        "   1 ",
        "  1  ",
        " 1   ",
        " 1   ",
        " 1   "
    ),
    '8' to arrayOf(
        " 111 ",
        "1   1",
        "1   1",
        " 111 ",
        "1   1",
        "1   1",
        " 111 "
    ),
    '9' to arrayOf(
        " 111 ",
        "1   1",
        "1   1",
        " 1111",
        "    1",
        "    1",
        " 111 "
    ),
    ':' to arrayOf(
        " ",
        "1",
        " ",
        " ",
        " ",
        "1",
        " "
    )
)

@Composable
fun NothingDotMatrixClock(
    timeString: String,
    accentColor: Color,
    isLightMode: Boolean,
    modifier: Modifier = Modifier,
    dotSize: Dp = 4.dp,
    dotSpacing: Dp = 2.dp
) {
    val inactiveDotColor = if (isLightMode) Color(0x18000000) else Color(0x22FFFFFF)
    val activeDotColor = if (isLightMode) Color(0xFF111827) else Color.White

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        timeString.forEachIndexed { charIndex, char ->
            val matrix = DIGIT_MATRICES[char] ?: DIGIT_MATRICES[' ']
            if (matrix != null) {
                val cols = matrix[0].length
                val rows = matrix.size

                val charWidth = (dotSize * cols) + (dotSpacing * (cols - 1))
                val charHeight = (dotSize * rows) + (dotSpacing * (rows - 1))

                Canvas(modifier = Modifier.size(width = charWidth, height = charHeight)) {
                    val dotPx = dotSize.toPx()
                    val spacingPx = dotSpacing.toPx()
                    val radiusPx = dotPx / 2f

                    for (r in 0 until rows) {
                        val rowStr = matrix[r]
                        for (c in 0 until cols) {
                            val isActive = rowStr.getOrNull(c) == '1'
                            val x = c * (dotPx + spacingPx) + radiusPx
                            val y = r * (dotPx + spacingPx) + radiusPx

                            // Opcional: dois-pontos ou detalhe especial pode carregar o tom accent
                            val color = when {
                                isActive && char == ':' -> accentColor
                                isActive -> activeDotColor
                                else -> inactiveDotColor
                            }

                            drawCircle(
                                color = color,
                                radius = radiusPx * 0.85f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                if (charIndex < timeString.length - 1) {
                    Spacer(modifier = Modifier.width(if (char == ':' || timeString[charIndex + 1] == ':') 5.dp else 10.dp))
                }
            }
        }
    }
}

/**
 * Componente principal de Relógios Tipográficos de Autor (Editorial Clocks)
 */
@Composable
fun EditorialClockWidget(
    clockStyle: String,
    formattedTime: String,
    formattedDate: String,
    accentColor: Color,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (clockStyle == "NONE" || clockStyle.isBlank()) return

    val context = LocalContext.current
    val onClockClick = {
        try {
            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(clockIntent)
        } catch (_: Exception) {
            try {
                val altIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(altIntent)
            } catch (_: Exception) {}
        }
    }

    val dateFormatted = remember {
        try {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("EEE, d 'de' MMMM", Locale.forLanguageTag("pt-BR"))
            sdf.format(cal.time).replaceFirstChar { it.uppercase() }
        } catch (_: Exception) {
            formattedDate
        }
    }

    val primaryTextColor = if (isLightMode) Color(0xFF111827) else TextPrimary
    val secondaryTextColor = if (isLightMode) Color(0xFF6B7280) else TextSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        when (clockStyle) {
            "NOTHING_DOT" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NothingDotMatrixClock(
                        timeString = formattedTime.ifBlank { "12:00" },
                        accentColor = accentColor,
                        isLightMode = isLightMode,
                        dotSize = 5.dp,
                        dotSpacing = 3.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormatted.uppercase(Locale.getDefault()),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = secondaryTextColor
                        )
                    }
                }
            }

            "STACKED_BOLD" -> {
                val parts = formattedTime.split(":")
                val hour = parts.getOrNull(0) ?: "12"
                val min = parts.getOrNull(1) ?: "00"

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = hour,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-3).sp,
                            lineHeight = 62.sp
                        ),
                        color = primaryTextColor
                    )
                    Text(
                        text = min,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-3).sp,
                            lineHeight = 62.sp
                        ),
                        color = if (accentColor != Color.White && accentColor != Color(0xFF111827)) accentColor else secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dateFormatted,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        color = secondaryTextColor
                    )
                }
            }

            "OVERSIZED_THIN" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val parts = formattedTime.split(":")
                    val hour = parts.getOrNull(0) ?: "12"
                    val min = parts.getOrNull(1) ?: "00"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = hour,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = (-2).sp
                            ),
                            color = primaryTextColor
                        )
                        Text(
                            text = ":",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraLight
                            ),
                            color = accentColor,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                        )
                        Text(
                            text = min,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = (-2).sp
                            ),
                            color = primaryTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLightMode) Color(0x12000000) else Color(0x18FFFFFF))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = dateFormatted,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = secondaryTextColor
                        )
                    }
                }
            }

            "CLEAN_MINIMAL" -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formattedTime,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = primaryTextColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(20.dp)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = dateFormatted,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = secondaryTextColor
                    )
                }
            }
        }
    }
}
