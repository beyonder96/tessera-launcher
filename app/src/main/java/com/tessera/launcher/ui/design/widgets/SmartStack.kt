package com.tessera.launcher.ui.design.widgets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.helper.ScreenTimeInfo
import com.tessera.launcher.data.helper.WeatherInfo
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors

/**
 * Smart Stack Oficial Apple (iOS 26 Smart Stack).
 * Empilhamento dinâmico de widgets com snap tátil vertical, rotação inteligente baseada
 * em contexto e suporte a modo de edição Jiggle com tremor contínuo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmartStack(
    enabledWidgets: List<WidgetType>,
    batteryPercentage: Int,
    isCharging: Boolean,
    weather: WeatherInfo?,
    calendarEvent: CalendarEventInfo?,
    hasCalendarPermission: Boolean,
    mediaPlayback: MediaPlaybackInfo,
    screenTime: ScreenTimeInfo?,
    isSmartRotateEnabled: Boolean,
    isJiggleMode: Boolean,
    onEnterJiggleMode: () -> Unit,
    onRemoveWidget: (WidgetType) -> Unit,
    onCalendarClick: () -> Unit,
    onMediaClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current

    if (enabledWidgets.isEmpty()) {
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { enabledWidgets.size }
    )

    // Efeito de Rotação Inteligente Context-Aware
    LaunchedEffect(isSmartRotateEnabled, mediaPlayback.isPlaying, isCharging, calendarEvent) {
        if (isSmartRotateEnabled && !isJiggleMode) {
            val mediaIndex = enabledWidgets.indexOf(WidgetType.MEDIA)
            val calendarIndex = enabledWidgets.indexOf(WidgetType.CALENDAR)
            val batteryIndex = enabledWidgets.indexOf(WidgetType.BATTERY)

            if (mediaPlayback.isPlaying && mediaIndex >= 0) {
                pagerState.animateScrollToPage(mediaIndex)
            } else if (isCharging && batteryIndex >= 0) {
                pagerState.animateScrollToPage(batteryIndex)
            } else if (calendarEvent != null && calendarIndex >= 0) {
                pagerState.animateScrollToPage(calendarIndex)
            }
        }
    }

    // Animação de tremor Jiggle Mode
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val jiggleRotation by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiggle_angle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(168.dp)
            .graphicsLayer {
                rotationZ = if (isJiggleMode) jiggleRotation else 0f
            }
    ) {
        // Carrossel Vertical com Pager
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val widgetType = enabledWidgets.getOrNull(page) ?: return@VerticalPager
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            Taptic.heavyImpact(context)
                            onEnterJiggleMode()
                        }
                    )
            ) {
                when (widgetType) {
                    WidgetType.BATTERY -> AppleBatteryWidget(
                        batteryPercentage = batteryPercentage,
                        isCharging = isCharging
                    )
                    WidgetType.WEATHER -> AppleWeatherWidget(
                        weather = weather
                    )
                    WidgetType.CALENDAR -> AppleCalendarWidget(
                        nextEvent = calendarEvent,
                        hasPermission = hasCalendarPermission,
                        onCalendarClick = onCalendarClick
                    )
                    WidgetType.MEDIA -> AppleMediaWidget(
                        mediaPlayback = mediaPlayback,
                        onMediaClick = onMediaClick,
                        onPlayPauseClick = onPlayPauseClick,
                        onNextClick = onNextClick,
                        onPreviousClick = onPreviousClick
                    )
                    WidgetType.SCREEN_TIME -> AppleScreenTimeWidget(
                        screenTime = screenTime
                    )
                    else -> AppleBatteryWidget(
                        batteryPercentage = batteryPercentage,
                        isCharging = isCharging
                    )
                }

                // Botão "–" de remoção estilo iOS Jiggle Mode
                if (isJiggleMode) {
                    Box(
                        modifier = Modifier
                            .offset(x = (-6).dp, y = (-6).dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8E8E93))
                            .combinedClickable(
                                onClick = {
                                    Taptic.selection(context)
                                    onRemoveWidget(widgetType)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = "Remover",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Indicador de Paging Lateral Estilo Apple (Pontinhos Verticais)
        if (enabledWidgets.size > 1 && !isJiggleMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(enabledWidgets.size) { index ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 5.dp else 4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) colors.label else colors.quaternaryLabel
                            )
                    )
                }
            }
        }
    }
}
