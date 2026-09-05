package com.tessera.launcher.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.theme.DarkSurfaceBorder

fun Modifier.geminiBorder(
    isAnimating: Boolean,
    shape: Shape,
    borderWidth: Dp = 1.5.dp
): Modifier = composed {
    if (!isAnimating) {
        return@composed this.border(1.dp, DarkSurfaceBorder, shape)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "gemini_sweep")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gemini_angle"
    )

    val geminiBrush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFF7A7A7A),
            Color(0xFF262626),
            Color(0xFF8E8E93),
            Color(0xFFFFFFFF)
        )
    )

    this.drawWithContent {
        drawContent()
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(
            outline = outline,
            brush = geminiBrush,
            style = Stroke(width = borderWidth.toPx())
        )
    }
}
