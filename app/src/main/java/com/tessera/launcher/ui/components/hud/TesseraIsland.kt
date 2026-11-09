package com.tessera.launcher.ui.components.hud

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.HudConfig
import com.tessera.launcher.data.model.HudState
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.theme.tokens.Tessera

/**
 * Live HUD 2.0 — "Ilha Dinâmica Tessera"
 *
 * Pílula preta sólida profunda com morphing fluido em mola física:
 * - 4 Estados: Compacta, Split (bolha satélite), Expandida e Peek temporário (~4s).
 * - Suporte completo a Mídia, Corridas, Delivery, Navegação GPS, Encomendas de E-commerce,
 *   Chamadas com duração ao vivo, Timers e status de Bateria.
 * - Gestos inteligentes: deslize horizontal para trocar de faixa, deslize para cima para fechar/dispensar.
 */
@Composable
fun TesseraIsland(
    hudState: HudState,
    mediaPlayback: MediaPlaybackInfo,
    isCharging: Boolean,
    batteryPercentage: Int,
    defaultAccent: Color,
    config: HudConfig,
    onOpenApp: (String) -> Unit,
    onPlayPauseMedia: () -> Unit,
    onNextMedia: () -> Unit,
    onPreviousMedia: () -> Unit,
    onDismissPeek: () -> Unit,
    onDismissActivity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!config.enabled) return

    val isMediaActive = config.showMedia && (mediaPlayback.isPlaying || (mediaPlayback.hasActiveSession && mediaPlayback.title.isNotBlank()))
    val isChargingActive = config.showCharging && isCharging

    // Determina quem é o primário e quem é o secundário da ilha
    var swappedPrimaryActivity by remember { mutableStateOf<LiveActivityInfo?>(null) }
    var showMediaAsPrimary by remember { mutableStateOf(false) }

    val rawPrimary = hudState.primary
    val rawSecondary = hudState.secondary

    val effectivePrimary: LiveActivityInfo? = when {
        showMediaAsPrimary -> null
        swappedPrimaryActivity != null -> swappedPrimaryActivity
        rawPrimary != null -> rawPrimary
        else -> null
    }

    val effectiveSecondary: LiveActivityInfo? = when {
        showMediaAsPrimary -> rawPrimary
        swappedPrimaryActivity != null -> rawPrimary
        rawSecondary != null -> rawSecondary
        else -> null
    }

    val hasSecondaryMediaBubble = isMediaActive && effectivePrimary != null && !showMediaAsPrimary

    val shouldShow = hudState.peek != null ||
            effectivePrimary != null ||
            isMediaActive ||
            isChargingActive

    var isExpanded by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedDragX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = Tessera.motion.springPress,
        label = "island_drag_x"
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
        val gestureModifier = Modifier
            .pointerInput(isExpanded, hudState.peek, isMediaActive) {
                if (hudState.peek != null) {
                    // Swipe para cima dispensa o Peek
                    detectVerticalDragGestures { change, dragAmount ->
                        if (dragAmount < -15f) {
                            if (config.haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismissPeek()
                            change.consume()
                        }
                    }
                } else if (!isExpanded && isMediaActive && effectivePrimary == null) {
                    // Swipe horizontal para trocar de faixa musical
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragOffsetX > 45f) {
                                if (config.haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNextMedia()
                            } else if (dragOffsetX < -45f) {
                                if (config.haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPreviousMedia()
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
            }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer { translationX = animatedDragX }
        ) {
            // ==========================================
            // SUPERFÍCIE PRINCIPAL DA ILHA
            // ==========================================
            IslandSurface(
                isExpanded = isExpanded,
                onClick = {
                    if (hudState.peek != null) {
                        try {
                            hudState.peek.contentIntent?.send()
                        } catch (_: Exception) {}
                        onDismissPeek()
                    } else {
                        if (config.haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isExpanded = !isExpanded
                    }
                },
                modifier = gestureModifier
            ) {
                when {
                    // 1. PEEK TEMPORÁRIO DE NOTIFICAÇÃO
                    hudState.peek != null -> {
                        IslandPeekContent(
                            peek = hudState.peek,
                            hideContent = config.hidePeekContent,
                            defaultAccent = defaultAccent,
                            onClick = {
                                try {
                                    hudState.peek.contentIntent?.send()
                                } catch (_: Exception) {}
                                onDismissPeek()
                            }
                        )
                    }

                    // 2. MODO EXPANDIDO RICO
                    isExpanded -> {
                        IslandExpandedContent(
                            liveActivity = effectivePrimary,
                            mediaPlayback = mediaPlayback,
                            defaultAccent = defaultAccent,
                            onOpenApp = onOpenApp,
                            onCollapse = { isExpanded = false },
                            onPlayPauseMedia = onPlayPauseMedia,
                            onNextMedia = onNextMedia,
                            onPreviousMedia = onPreviousMedia
                        )
                    }

                    // 3. MODO COMPACTO COM LIVE ACTIVITY ATIVA
                    effectivePrimary != null -> {
                        IslandCompactContent(
                            activity = effectivePrimary,
                            defaultAccent = defaultAccent
                        )
                    }

                    // 4. MODO COMPACTO DE MÍDIA ATIVA
                    isMediaActive -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 280.dp)
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            AudioWaveform(
                                isPlaying = mediaPlayback.isPlaying,
                                accentColor = defaultAccent
                            )

                            Spacer(modifier = Modifier.width(9.dp))

                            AnimatedContent(
                                targetState = mediaPlayback.title,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "island_media_title"
                            ) { titleText ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = titleText.ifBlank { "Reproduzindo" },
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    if (mediaPlayback.artist.isNotBlank() && mediaPlayback.artist != "Toque para abrir e reproduzir") {
                                        Text(
                                            text = " • ${mediaPlayback.artist}",
                                            color = Color(0xFFA1A1AA),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. STATUS DE CARREGAMENTO / BATERIA
                    isChargingActive -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BatteryChargingFull,
                                contentDescription = "Carregando",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Carregando • $batteryPercentage%",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ==========================================
            // BOLHA SECUNDÁRIA DO MODO SPLIT
            // ==========================================
            val showSplitBubble = !isExpanded && hudState.peek == null &&
                    (effectiveSecondary != null || hasSecondaryMediaBubble)

            if (showSplitBubble) {
                Spacer(modifier = Modifier.width(8.dp))
                IslandSecondaryBubble(
                    activity = effectiveSecondary,
                    mediaPlayback = if (hasSecondaryMediaBubble) mediaPlayback else null,
                    defaultAccent = defaultAccent,
                    onSwap = {
                        if (config.haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (hasSecondaryMediaBubble && effectiveSecondary == null) {
                            showMediaAsPrimary = !showMediaAsPrimary
                        } else if (effectiveSecondary != null) {
                            swappedPrimaryActivity = if (swappedPrimaryActivity == null) effectiveSecondary else null
                        }
                    }
                )
            }
        }
    }
}
