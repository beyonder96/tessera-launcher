package com.tessera.launcher.ui.components.hud

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Visualizador de espectro sonoro com barras dinâmicas animadas em mola suave.
 */
@Composable
fun AudioWaveform(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioWaveTransition")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 13f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 440, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val bar4Height by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    Row(
        modifier = modifier
            .height(16.dp)
            .width(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val h1 = if (isPlaying) bar1Height.dp else 4.dp
        val h2 = if (isPlaying) bar2Height.dp else 8.dp
        val h3 = if (isPlaying) bar3Height.dp else 5.dp
        val h4 = if (isPlaying) bar4Height.dp else 3.dp

        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h1)
                .clip(RoundedCornerShape(1.25.dp))
                .background(accentColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h2)
                .clip(RoundedCornerShape(1.25.dp))
                .background(accentColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h3)
                .clip(RoundedCornerShape(1.25.dp))
                .background(accentColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h4)
                .clip(RoundedCornerShape(1.25.dp))
                .background(accentColor)
        )
    }
}
