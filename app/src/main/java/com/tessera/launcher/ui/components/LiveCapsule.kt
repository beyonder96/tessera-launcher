package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.theme.AmoledBlack

/**
 * Live Capsule (Mini HUD / Dynamic Island flutuante no topo).
 * Exibe status em tempo real de reprodução de mídia ativa (com visualizador de ondas sonoras animado
 * e play/pause instantâneo) ou status de carregamento de bateria.
 */
@Composable
fun LiveCapsule(
    mediaPlayback: MediaPlaybackInfo,
    isCharging: Boolean,
    batteryPercentage: Int,
    isLiquidGlass: Boolean,
    isAmoledMode: Boolean,
    isLightMode: Boolean,
    accentColor: Color,
    onMediaClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMediaActive = mediaPlayback.isPlaying || (mediaPlayback.hasActiveSession && mediaPlayback.title.isNotBlank())
    val shouldShow = isMediaActive || isCharging

    AnimatedVisibility(
        visible = shouldShow,
        enter = fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ),
        exit = fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ),
        modifier = modifier
    ) {
        val capsuleShape = RoundedCornerShape(24.dp)

        val capsuleBackground = when {
            isAmoledMode -> AmoledBlack.copy(alpha = 0.94f)
            isLightMode -> Color(0xFFEFEFEF).copy(alpha = 0.92f)
            isLiquidGlass -> Color(0xFF1E1E24).copy(alpha = 0.82f)
            else -> Color(0xFF141418).copy(alpha = 0.90f)
        }

        val borderColor = when {
            isLightMode -> Color.Black.copy(alpha = 0.08f)
            isLiquidGlass -> Color.White.copy(alpha = 0.18f)
            else -> Color.White.copy(alpha = 0.12f)
        }

        val primaryTextColor = if (isLightMode) Color(0xFF1C1C1E) else Color.White
        val secondaryTextColor = if (isLightMode) Color(0xFF6C6C70) else Color(0xFF8E8E93)

        Box(
            modifier = Modifier
                .clip(capsuleShape)
                .background(capsuleBackground)
                .border(width = 0.8.dp, color = borderColor, shape = capsuleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (isMediaActive) {
                            onMediaClick()
                        }
                    }
                )
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.widthIn(min = 120.dp, max = 290.dp)
            ) {
                if (isMediaActive) {
                    // Mini Visualizador de Espectro Sonoro
                    AudioWaveformVisualizer(
                        isPlaying = mediaPlayback.isPlaying,
                        accentColor = accentColor,
                        isLightMode = isLightMode
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Título e Artista
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mediaPlayback.title.ifBlank { "Reproduzindo" },
                            color = primaryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (mediaPlayback.artist.isNotBlank() && mediaPlayback.artist != "Toque para abrir e reproduzir") {
                            Text(
                                text = " • ${mediaPlayback.artist}",
                                color = secondaryTextColor,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Botão Play / Pause Compacto
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLightMode) Color.Black.copy(alpha = 0.08f)
                                else Color.White.copy(alpha = 0.12f)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onPlayPauseClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (mediaPlayback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            contentDescription = if (mediaPlayback.isPlaying) "Pausar" else "Reproduzir",
                            tint = primaryTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else if (isCharging) {
                    // Indicador de Carregamento
                    Icon(
                        imageVector = Icons.Outlined.BatteryChargingFull,
                        contentDescription = "Carregando",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Carregando • $batteryPercentage%",
                        color = primaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Visualizador de 3 barras pulsantes simulando espectro de frequências sonoras.
 */
@Composable
private fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val barColor = if (accentColor != Color.White) {
        accentColor
    } else {
        if (isLightMode) Color.Black else Color.White
    }

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
        initialValue = 14f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier
            .height(16.dp)
            .width(15.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val h1 = if (isPlaying) bar1Height.dp else 4.dp
        val h2 = if (isPlaying) bar2Height.dp else 7.dp
        val h3 = if (isPlaying) bar3Height.dp else 4.dp

        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h1)
                .clip(RoundedCornerShape(1.5.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h2)
                .clip(RoundedCornerShape(1.5.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h3)
                .clip(RoundedCornerShape(1.5.dp))
                .background(barColor)
        )
    }
}
