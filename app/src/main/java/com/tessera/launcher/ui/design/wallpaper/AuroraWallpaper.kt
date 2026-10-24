package com.tessera.launcher.ui.design.wallpaper

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Calendar

/**
 * Papel de Parede Dinâmico Aurora Apple (iOS / macOS Sonoma Mesh Gradient).
 * 3 orbes coloridos em movimento perpétuo suave cujas tonalidades se adaptam ao horário do dia.
 */
@Composable
fun AuroraWallpaper(
    modifier: Modifier = Modifier
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    // Cores dinâmicas por ciclo diurno
    val (color1, color2, color3, baseColor) = when {
        currentHour in 6..11 -> { // Manhã dourada
            listOf(Color(0xFFFF9500), Color(0xFFFF2D55), Color(0xFFFFCC00), Color(0xFF1C0D02))
        }
        currentHour in 12..17 -> { // Tarde azul da Califórnia
            listOf(Color(0xFF007AFF), Color(0xFF00C7BE), Color(0xFF5856D6), Color(0xFF051429))
        }
        else -> { // Noite índigo e roxa
            listOf(Color(0xFF5856D6), Color(0xFFAF52DE), Color(0xFF007AFF), Color(0xFF000000))
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "aurora_motion")

    val phaseX1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_x1"
    )

    val phaseY1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_y1"
    )

    val phaseX2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_x2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Fundo base
        drawRect(color = baseColor)

        // Orbe 1
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = 0.65f), Color.Transparent),
                center = Offset(w * phaseX1, h * phaseY1),
                radius = w * 0.9f
            )
        )

        // Orbe 2
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(w * phaseX2, h * (1f - phaseY1)),
                radius = w * 0.85f
            )
        )

        // Orbe 3
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(w * 0.5f, h * phaseX1),
                radius = w * 1.1f
            )
        )
    }
}
