package com.tessera.launcher.ui.components.hud

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Cronômetro ou Timer vivo que atualiza por segundo de forma reativa e eficiente.
 */
@Composable
fun LiveChronometer(
    baseMs: Long,
    isCountDown: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.sp,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    val formattedTime = produceState(initialValue = formatTime(baseMs, isCountDown), key1 = baseMs, key2 = isCountDown) {
        while (true) {
            value = formatTime(baseMs, isCountDown)
            delay(1000L)
        }
    }

    Text(
        text = formattedTime.value,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
    )
}

private fun formatTime(baseMs: Long, isCountDown: Boolean): String {
    val now = System.currentTimeMillis()
    val diffSeconds = if (isCountDown) {
        ((baseMs - now) / 1000L).coerceAtLeast(0L)
    } else {
        ((now - baseMs) / 1000L).coerceAtLeast(0L)
    }

    val hours = diffSeconds / 3600L
    val minutes = (diffSeconds % 3600L) / 60L
    val seconds = diffSeconds % 60L

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
