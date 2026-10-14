package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.DeliveryStage
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Live Capsule Pro: Mini HUD & Cápsula Dinâmica inteligente no topo.
 * - Suporta dois modos: Pílula Compacta e Card Expandido com Controles Completos.
 * - Integração total com Live Activities (Uber, 99, iFood, Timer, Maps, Chamadas) e Reprodução de Mídia.
 * - Gestos rápidos na pílula: deslize para a direita (próxima faixa) e para a esquerda (anterior).
 * - Transições espaciais fluidas em mola física e visualizador de ondas sonoras adaptativo.
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
    modifier: Modifier = Modifier,
    onNextClick: (() -> Unit)? = null,
    onPreviousClick: (() -> Unit)? = null,
    liveActivity: LiveActivityInfo? = null,
    onLiveActivityClick: ((String) -> Unit)? = null
) {
    val isMediaActive = mediaPlayback.isPlaying || (mediaPlayback.hasActiveSession && mediaPlayback.title.isNotBlank())
    val shouldShow = liveActivity != null || isMediaActive || isCharging

    var isExpanded by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedDragX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = Tessera.motion.springPress,
        label = "capsule_drag_x"
    )

    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = shouldShow,
        enter = fadeIn(animationSpec = Tessera.motion.springSpatial) +
                slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = Tessera.motion.springSpatialOffset
                ),
        exit = fadeOut(animationSpec = Tessera.motion.springSpatial) +
                slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = Tessera.motion.springSpatialOffset
                ),
        modifier = modifier
    ) {
        if (liveActivity?.rideDetails != null && isExpanded) {
            RideLiveActivityCard(
                rideDetails = liveActivity.rideDetails,
                isLiquidGlass = isLiquidGlass,
                isAmoledMode = isAmoledMode,
                isLightMode = isLightMode,
                accentColor = accentColor,
                onCardClick = {
                    onLiveActivityClick?.invoke(liveActivity.packageName)
                },
                onActionClick = { intent ->
                    try {
                        intent?.send()
                    } catch (_: Exception) {}
                },
                onCollapseClick = {
                    isExpanded = false
                }
            )
        } else if (liveActivity?.deliveryDetails != null && isExpanded) {
            DeliveryLiveActivityCard(
                deliveryDetails = liveActivity.deliveryDetails,
                isLiquidGlass = isLiquidGlass,
                isAmoledMode = isAmoledMode,
                isLightMode = isLightMode,
                accentColor = accentColor,
                onCardClick = {
                    onLiveActivityClick?.invoke(liveActivity.packageName)
                },
                onActionClick = { intent ->
                    try {
                        intent?.send()
                    } catch (_: Exception) {}
                },
                onCollapseClick = {
                    isExpanded = false
                }
            )
        } else if (liveActivity?.navigationDetails != null && isExpanded) {
            NavigationLiveActivityCard(
                navigationDetails = liveActivity.navigationDetails,
                isLiquidGlass = isLiquidGlass,
                isAmoledMode = isAmoledMode,
                isLightMode = isLightMode,
                accentColor = accentColor,
                onCardClick = {
                    onLiveActivityClick?.invoke(liveActivity.packageName)
                },
                onActionClick = { intent ->
                    try {
                        intent?.send()
                    } catch (_: Exception) {}
                },
                onCollapseClick = {
                    isExpanded = false
                }
            )
        } else {
            val capsuleShape = if (isExpanded) Tessera.shapes.card else Tessera.shapes.pill

            val capsuleBackground = if (isLiquidGlass && !isAmoledMode) {
                Tessera.palette.surface.copy(alpha = 0.88f)
            } else {
                Tessera.palette.surface
            }

            val borderColor = Tessera.palette.surfaceBorder
            val primaryTextColor = Tessera.palette.textPrimary
            val secondaryTextColor = Tessera.palette.textSecondary

            val gestureModifier = if (!isExpanded && liveActivity == null && isMediaActive) {
                Modifier.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragOffsetX > 45f && onNextClick != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNextClick()
                            } else if (dragOffsetX < -45f && onPreviousClick != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPreviousClick()
                            }
                            dragOffsetX = 0f
                        },
                        onDragCancel = { dragOffsetX = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            dragOffsetX = (dragOffsetX + dragAmount * 0.45f).coerceIn(-65f, 65f)
                            change.consume()
                        }
                    )
                }
            } else Modifier

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = animatedDragX
                    }
                    .then(gestureModifier)
                    .clip(capsuleShape)
                    .background(capsuleBackground)
                    .border(width = 0.6.dp, color = borderColor, shape = capsuleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            isExpanded = !isExpanded
                        }
                    )
                    .padding(
                        horizontal = if (isExpanded) 16.dp else 14.dp,
                        vertical = if (isExpanded) 12.dp else 7.dp
                    )
            ) {
                if (liveActivity != null) {
                // ==========================================
                // 1. LIVE ACTIVITY (Uber / iFood / Timer / etc)
                // ==========================================
                val activityIcon = when (liveActivity.type) {
                    LiveActivityType.RIDE -> Icons.Outlined.DirectionsCar
                    LiveActivityType.DELIVERY -> Icons.Outlined.DeliveryDining
                    LiveActivityType.TIMER -> Icons.Outlined.Timer
                    LiveActivityType.NAVIGATION -> Icons.Outlined.Navigation
                    LiveActivityType.CALL -> Icons.Outlined.Call
                    LiveActivityType.FITNESS -> Icons.AutoMirrored.Outlined.DirectionsRun
                    LiveActivityType.OTHER -> Icons.Outlined.NotificationsActive
                }

                val activityAccent = when (liveActivity.type) {
                    LiveActivityType.CALL -> Color(0xFF00E676)
                    LiveActivityType.RIDE -> Color(0xFF00E5FF)
                    LiveActivityType.DELIVERY -> Color(0xFFFF2E2E)
                    LiveActivityType.TIMER -> Color(0xFFFF9100)
                    else -> accentColor
                }

                if (isExpanded) {
                    // Modo Expandido da Live Activity
                    Column(
                        modifier = Modifier.widthIn(min = 280.dp, max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val artwork = liveActivity.artwork
                            if (artwork != null) {
                                Image(
                                    bitmap = artwork.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(Tessera.shapes.small)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(Tessera.shapes.small)
                                        .background(activityAccent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = activityIcon,
                                        contentDescription = null,
                                        tint = activityAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = liveActivity.title,
                                    color = primaryTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!liveActivity.subtitle.isNullOrBlank()) {
                                    Text(
                                        text = liveActivity.subtitle,
                                        color = secondaryTextColor,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Botão para Abrir Aplicativo
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Tessera.palette.surfaceSubtle)
                                    .tesseraPressFeedback()
                                    .clickable {
                                        onLiveActivityClick?.invoke(liveActivity.packageName)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Abrir aplicativo",
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Barra de Progresso se houver
                        if (liveActivity.progress != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Tessera.palette.surfaceSubtle)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(liveActivity.progress / 100f)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(activityAccent)
                                )
                            }
                        }
                    }
                } else {
                    // Modo Compacto da Live Activity
                    val ride = liveActivity.rideDetails
                    val delivery = liveActivity.deliveryDetails
                    val nav = liveActivity.navigationDetails

                    if (ride != null) {
                        // Modo Compacto Especializado de Transporte (Uber / 99)
                        val compactAccent = if (ride.appName.equals("99", ignoreCase = true)) Color(0xFFFFB300) else Color(0xFF2563EB)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.widthIn(min = 120.dp, max = 290.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(compactAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DirectionsCar,
                                    contentDescription = null,
                                    tint = compactAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(7.dp))

                            Text(
                                text = ride.appName,
                                color = primaryTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            val compactInfo = ride.etaMinutes?.let { "$it min" } ?: ride.etaText ?: ride.statusMessage
                            if (!compactInfo.isNullOrBlank()) {
                                Text(
                                    text = " • $compactInfo",
                                    color = if (ride.etaMinutes != null && ride.etaMinutes <= 2) compactAccent else secondaryTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (ride.etaMinutes != null && ride.etaMinutes <= 2) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(compactAccent)
                            )
                        }
                    } else if (delivery != null) {
                        // Modo Compacto Especializado de Delivery (iFood / Rappi)
                        val deliveryAccent = when {
                            delivery.appName.contains("ifood", ignoreCase = true) -> Color(0xFFEA1D2C)
                            delivery.appName.contains("rappi", ignoreCase = true) -> Color(0xFFFF441F)
                            delivery.appName.contains("ze", ignoreCase = true) -> Color(0xFFFFB300)
                            else -> Color(0xFFEA1D2C)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.widthIn(min = 120.dp, max = 290.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(deliveryAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeliveryDining,
                                    contentDescription = null,
                                    tint = deliveryAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(7.dp))

                            Text(
                                text = delivery.appName,
                                color = primaryTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            val compactInfo = delivery.etaMinutes?.let { "$it min" } ?: delivery.etaText ?: delivery.statusMessage
                            if (!compactInfo.isNullOrBlank()) {
                                Text(
                                    text = " • $compactInfo",
                                    color = if (delivery.stage == DeliveryStage.ON_THE_WAY) deliveryAccent else secondaryTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (delivery.stage == DeliveryStage.ON_THE_WAY) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(deliveryAccent)
                            )
                        }
                    } else if (nav != null) {
                        // Modo Compacto Especializado de Navegação (Google Maps / Waze)
                        val navAccent = if (nav.appName.contains("waze", ignoreCase = true)) Color(0xFF33CCFF) else Color(0xFF10B981)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.widthIn(min = 120.dp, max = 290.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(navAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Navigation,
                                    contentDescription = null,
                                    tint = navAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(7.dp))

                            Text(
                                text = nav.appName,
                                color = primaryTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            val compactInfo = nav.distanceToManeuver ?: nav.etaRemaining ?: nav.nextStreetName
                            if (!compactInfo.isNullOrBlank()) {
                                Text(
                                    text = " • $compactInfo",
                                    color = navAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(navAccent)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.widthIn(min = 120.dp, max = 290.dp)
                        ) {
                            Icon(
                                imageVector = activityIcon,
                                contentDescription = null,
                                tint = activityAccent,
                                modifier = Modifier.size(15.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = liveActivity.title,
                                    color = primaryTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!liveActivity.subtitle.isNullOrBlank()) {
                                    Text(
                                        text = " • ${liveActivity.subtitle}",
                                        color = secondaryTextColor,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(activityAccent)
                            )
                        }
                    }
                }
            } else if (isMediaActive) {
                // ==========================================
                // 2. MÍDIA ATIVA (Spotify / YouTube Music)
                // ==========================================
                if (isExpanded) {
                    // Modo Expandido de Mídia: Controles Completos & Capa
                    Column(
                        modifier = Modifier.widthIn(min = 280.dp, max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val artwork = mediaPlayback.artwork
                            if (artwork != null) {
                                Image(
                                    bitmap = artwork.asImageBitmap(),
                                    contentDescription = "Capa do Álbum",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(Tessera.shapes.small)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(Tessera.shapes.small)
                                        .background(Tessera.palette.surfaceSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MusicNote,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mediaPlayback.title.ifBlank { "Reproduzindo" },
                                    color = primaryTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = mediaPlayback.artist.ifBlank { "Mídia ativa" },
                                    color = secondaryTextColor,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Botão para Abrir Aplicativo
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Tessera.palette.surfaceSubtle)
                                    .tesseraPressFeedback()
                                    .clickable { onMediaClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Abrir aplicativo",
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Barra de Ações: Faixa Anterior, Play/Pause, Próxima Faixa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Tessera.palette.surfaceSubtle)
                                    .tesseraPressFeedback()
                                    .clickable { onPreviousClick?.invoke() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SkipPrevious,
                                    contentDescription = "Faixa Anterior",
                                    tint = primaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                                    .tesseraPressFeedback()
                                    .clickable { onPlayPauseClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mediaPlayback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = if (mediaPlayback.isPlaying) "Pausar" else "Reproduzir",
                                    tint = if (accentColor == Color.White) Color.Black else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Tessera.palette.surfaceSubtle)
                                    .tesseraPressFeedback()
                                    .clickable { onNextClick?.invoke() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SkipNext,
                                    contentDescription = "Próxima Faixa",
                                    tint = primaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Modo Compacto de Mídia
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.widthIn(min = 120.dp, max = 280.dp)
                    ) {
                        AudioWaveformVisualizer(
                            isPlaying = mediaPlayback.isPlaying,
                            accentColor = accentColor,
                            isLightMode = isLightMode
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        AnimatedContent(
                            targetState = mediaPlayback.title,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(150)) togetherWith
                                        fadeOut(animationSpec = tween(120))
                            },
                            label = "media_title_anim",
                            modifier = Modifier.weight(1f, fill = false)
                        ) { titleText ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = titleText.ifBlank { "Reproduzindo" },
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
                        }

                        Spacer(modifier = Modifier.width(9.dp))

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Tessera.palette.surfaceSubtle)
                                .tesseraPressFeedback()
                                .clickable { onPlayPauseClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (mediaPlayback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (mediaPlayback.isPlaying) "Pausar" else "Reproduzir",
                                tint = primaryTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            } else if (isCharging) {
                // ==========================================
                // 3. STATUS DE CARREGAMENTO
                // ==========================================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
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
