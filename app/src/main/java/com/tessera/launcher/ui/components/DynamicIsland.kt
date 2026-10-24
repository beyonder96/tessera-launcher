package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import com.tessera.launcher.ui.theme.apple.appleSpring

enum class IslandMode {
    COMPACT,
    EXPANDED
}

/**
 * Dynamic Island oficial do Tessera 3.0.
 * Adere fisicamente ao recorte da câmera e se transforma dinamicamente
 * em pílula preta compacta, modo duplo mínimo ou cartão expandido.
 */
@Composable
fun DynamicIsland(
    mediaPlayback: MediaPlaybackInfo,
    isCharging: Boolean,
    batteryPercentage: Int,
    liveActivity: LiveActivityInfo?,
    onMediaClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLiveActivityClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val typography = LocalAppleTypography.current

    val hasMedia = mediaPlayback.isPlaying || !mediaPlayback.title.isNullOrBlank()
    val hasLiveActivity = liveActivity != null
    val hasChargingNotice = isCharging

    if (!hasMedia && !hasLiveActivity && !hasChargingNotice) {
        return
    }

    var islandMode by remember { mutableStateOf(IslandMode.COMPACT) }

    // Visualizador de onda de áudio em reprodução
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave2"
    )
    val waveHeight3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave3"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pílula Principal da Ilha
        Box(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = appleSpring(response = 0.44f, bounce = 0.16f)
                )
                .clip(if (islandMode == IslandMode.EXPANDED) ContinuousShape(34.dp) else AppleShapes.capsule)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            Taptic.lightImpact(context)
                            if (islandMode == IslandMode.COMPACT) {
                                islandMode = IslandMode.EXPANDED
                            } else {
                                islandMode = IslandMode.COMPACT
                            }
                        },
                        onLongPress = {
                            Taptic.heavyImpact(context)
                            islandMode = if (islandMode == IslandMode.COMPACT) IslandMode.EXPANDED else IslandMode.COMPACT
                        }
                    )
                }
        ) {
            if (islandMode == IslandMode.COMPACT) {
                // Modo Compacto (Pílula Horizontal Elegante)
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasMedia) {
                        if (mediaPlayback.artwork != null) {
                            Image(
                                bitmap = mediaPlayback.artwork.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFFFF2D55),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = mediaPlayback.title ?: "Reproduzindo",
                            style = typography.caption1.copy(fontWeight = FontWeight.Medium),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 140.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Waveform animado de áudio
                        if (mediaPlayback.isPlaying) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(Modifier.width(2.5.dp).height(waveHeight1.dp).background(Color(0xFF34C759), CircleShape))
                                Box(Modifier.width(2.5.dp).height(waveHeight2.dp).background(Color(0xFF34C759), CircleShape))
                                Box(Modifier.width(2.5.dp).height(waveHeight3.dp).background(Color(0xFF34C759), CircleShape))
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Pause,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    } else if (hasLiveActivity && liveActivity != null) {
                        val icon = when (liveActivity.type) {
                            LiveActivityType.RIDE -> Icons.Outlined.DirectionsCar
                            LiveActivityType.DELIVERY -> Icons.Outlined.DeliveryDining
                            LiveActivityType.NAVIGATION -> Icons.Outlined.Navigation
                            LiveActivityType.TIMER -> Icons.Outlined.Timer
                            else -> Icons.Outlined.Navigation
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF0A84FF),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val liveStatus = liveActivity.rideDetails?.statusMessage
                            ?: liveActivity.deliveryDetails?.statusMessage
                            ?: liveActivity.subtitle
                            ?: liveActivity.title
                        Text(
                            text = liveStatus,
                            style = typography.caption1.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            maxLines = 1
                        )
                    } else if (hasChargingNotice) {
                        Icon(
                            imageVector = Icons.Outlined.BatteryChargingFull,
                            contentDescription = null,
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$batteryPercentage%",
                            style = typography.caption1.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF30D158)
                        )
                    }
                }
            } else {
                // Modo Expandido (Cartão com Controles Completos)
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(20.dp)
                ) {
                    if (hasMedia) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (mediaPlayback.artwork != null) {
                                Image(
                                    bitmap = mediaPlayback.artwork.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(ContinuousShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(ContinuousShape(12.dp))
                                        .background(Color(0xFF2C2C2E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFFFF2D55),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mediaPlayback.title ?: "Mídia",
                                    style = typography.headline,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = mediaPlayback.artist ?: "Reproduzindo agora",
                                    style = typography.subheadline,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Controles de Mídia
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SkipPrevious,
                                contentDescription = "Anterior",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        Taptic.lightImpact(context)
                                        onPreviousClick()
                                    }
                            )

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        Taptic.lightImpact(context)
                                        onPlayPauseClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mediaPlayback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.SkipNext,
                                contentDescription = "Próxima",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        Taptic.lightImpact(context)
                                        onNextClick()
                                    }
                            )
                        }
                    } else if (hasLiveActivity && liveActivity != null) {
                        val liveStatus = liveActivity.rideDetails?.statusMessage
                            ?: liveActivity.deliveryDetails?.statusMessage
                            ?: liveActivity.subtitle
                            ?: liveActivity.title
                        Text(
                            text = liveStatus,
                            style = typography.headline,
                            color = Color.White
                        )
                        if (!liveActivity.subtitle.isNullOrBlank()) {
                            Text(
                                text = liveActivity.subtitle,
                                style = typography.subheadline,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Modo Mínimo (Segunda atividade satélite circular ao lado quando houver simultaneidade)
        if (islandMode == IslandMode.COMPACT && hasMedia && hasChargingNotice) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.BatteryChargingFull,
                    contentDescription = null,
                    tint = Color(0xFF30D158),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
