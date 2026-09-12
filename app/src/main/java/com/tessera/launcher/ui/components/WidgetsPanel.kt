package com.tessera.launcher.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.helper.WeatherInfo
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.state.WidgetConfigType
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import com.tessera.launcher.data.helper.SmartGlanceActionType
import com.tessera.launcher.data.helper.SmartGlanceBriefing
import java.util.Calendar

private enum class PanelCardType {
    QUICK_ACTIONS,
    BATTERY,
    CALENDAR,
    MEDIA,
    VERSE_FOCUS,
    WEATHER,
    DINO,
    NOTES,
    SMART_GLANCE
}

@Composable
fun WidgetsPanel(
    enabledWidgets: List<WidgetType>,
    batteryPercentage: Int,
    isCharging: Boolean,
    currentTime: String,
    currentDate: String,
    nextCalendarEvent: CalendarEventInfo?,
    hasCalendarPermission: Boolean,
    onRequestCalendarPermission: () -> Unit,
    onCalendarClick: () -> Unit,
    mediaPlayback: MediaPlaybackInfo,
    hasNotificationAccess: Boolean,
    onRequestNotificationAccess: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenMusicApp: () -> Unit,
    modifier: Modifier = Modifier,
    defaultWidgetCardIndex: Int = 0,
    isDinoWidgetEnabled: Boolean = false,
    isNotesWidgetEnabled: Boolean = false,
    isSwitchOnMusicPlayEnabled: Boolean = true,
    isTorchOn: Boolean = false,
    ringerMode: Int = 2,
    onToggleTorch: () -> Unit = {},
    onOpenWifi: () -> Unit = {},
    onOpenBluetooth: () -> Unit = {},
    onCycleRingerMode: () -> Unit = {},
    notesTasks: List<NoteTask> = emptyList(),
    onAddNoteTask: (String) -> Unit = {},
    onToggleNoteTask: (Long) -> Unit = {},
    onRemoveNoteTask: (Long) -> Unit = {},
    onNotesClick: () -> Unit = {},
    weatherInfo: WeatherInfo? = null,
    hasLocationPermission: Boolean = false,
    onRequestLocationPermission: () -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    isWeatherLoading: Boolean = false,
    weatherError: String? = null,
    batteryWidgetStyle: String = "cards_3",
    mediaWidgetStyle: String = "classic",
    notesWidgetFilter: String = "all",
    onWidgetLongClick: (WidgetConfigType) -> Unit = {},
    isLiquidGlass: Boolean = false,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    smartGlanceBriefing: SmartGlanceBriefing? = null,
    isSmartGlanceEnabled: Boolean = true
) {
    val activeCards = remember(isSmartGlanceEnabled, isDinoWidgetEnabled, isNotesWidgetEnabled) {
        buildList {
            add(PanelCardType.QUICK_ACTIONS)
            add(PanelCardType.BATTERY)
            add(PanelCardType.CALENDAR)
            add(PanelCardType.MEDIA)
            add(PanelCardType.VERSE_FOCUS)
            add(PanelCardType.WEATHER)
            if (isDinoWidgetEnabled) add(PanelCardType.DINO)
            if (isNotesWidgetEnabled) add(PanelCardType.NOTES)
            if (isSmartGlanceEnabled) add(PanelCardType.SMART_GLANCE)
        }
    }
    val pageCount = activeCards.size

    val targetCardType = when (defaultWidgetCardIndex) {
        0 -> PanelCardType.QUICK_ACTIONS
        1 -> PanelCardType.BATTERY
        2 -> PanelCardType.CALENDAR
        3 -> PanelCardType.MEDIA
        4 -> PanelCardType.VERSE_FOCUS
        5 -> PanelCardType.WEATHER
        6 -> PanelCardType.DINO
        7 -> PanelCardType.NOTES
        8 -> PanelCardType.SMART_GLANCE
        else -> PanelCardType.QUICK_ACTIONS
    }
    val targetPageIndex = activeCards.indexOf(targetCardType).let { if (it >= 0) it else 0 }

    val initialPage = remember {
        if (isSwitchOnMusicPlayEnabled && mediaPlayback.isPlaying) {
            val mediaIndex = activeCards.indexOf(PanelCardType.MEDIA)
            if (mediaIndex >= 0) mediaIndex else 0
        } else {
            targetPageIndex.coerceIn(0, pageCount - 1)
        }
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(mediaPlayback.isPlaying) {
        if (isSwitchOnMusicPlayEnabled && mediaPlayback.isPlaying) {
            val mediaIndex = activeCards.indexOf(PanelCardType.MEDIA)
            if (mediaIndex >= 0) {
                pagerState.animateScrollToPage(mediaIndex)
            }
        }
    }

    LaunchedEffect(defaultWidgetCardIndex) {
        if (!isSwitchOnMusicPlayEnabled || !mediaPlayback.isPlaying) {
            val target = targetPageIndex.coerceIn(0, pageCount - 1)
            if (pagerState.currentPage != target) {
                pagerState.animateScrollToPage(target)
            }
        }
    }

    // Se o usuário deslizar o widget para o lado, fecha o teclado e limpa o foco
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 16.dp
        ) { page ->
            when (activeCards.getOrNull(page)) {
                PanelCardType.QUICK_ACTIONS -> {
                    QuickActionsWidgetCard(
                        isTorchOn = isTorchOn,
                        ringerMode = ringerMode,
                        onToggleTorch = onToggleTorch,
                        onOpenWifi = onOpenWifi,
                        onOpenBluetooth = onOpenBluetooth,
                        onCycleRingerMode = onCycleRingerMode,
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.QUICK_ACTIONS) }
                    )
                }
                PanelCardType.BATTERY -> {
                    BatteryWidgetCard(
                        batteryPercentage = batteryPercentage,
                        isCharging = isCharging,
                        batteryWidgetStyle = batteryWidgetStyle,
                        onOpenWifi = onOpenWifi,
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.BATTERY) }
                    )
                }
                PanelCardType.CALENDAR -> {
                    CalendarWidgetCard(
                        currentTime = currentTime,
                        currentDate = currentDate,
                        nextCalendarEvent = nextCalendarEvent,
                        hasPermission = hasCalendarPermission,
                        onRequestPermission = onRequestCalendarPermission,
                        onCalendarClick = onCalendarClick,
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.CALENDAR) }
                    )
                }
                PanelCardType.MEDIA -> {
                    MediaWidgetCard(
                        mediaPlayback = mediaPlayback,
                        hasNotificationAccess = hasNotificationAccess,
                        onRequestAccess = onRequestNotificationAccess,
                        onTogglePlayPause = onTogglePlayPause,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onOpenMusicApp = onOpenMusicApp,
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.MEDIA) }
                    )
                }
                PanelCardType.VERSE_FOCUS -> {
                    VerseFocusWidgetCard(
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.VERSE_FOCUS) }
                    )
                }
                PanelCardType.WEATHER -> {
                    WeatherWidgetCard(
                        weatherInfo = weatherInfo,
                        hasLocationPermission = hasLocationPermission,
                        onRequestLocationPermission = onRequestLocationPermission,
                        onRefreshWeather = onRefreshWeather,
                        isWeatherLoading = isWeatherLoading,
                        weatherError = weatherError,
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.WEATHER) }
                    )
                }
                PanelCardType.DINO -> {
                    DinoWidgetCard(isDockMode = true)
                }
                PanelCardType.NOTES -> {
                    NotesWidgetCard(
                        tasks = notesTasks,
                        notesWidgetFilter = notesWidgetFilter,
                        onAddTask = onAddNoteTask,
                        onToggleTask = onToggleNoteTask,
                        onRemoveTask = onRemoveNoteTask,
                        onNotesClick = onNotesClick,
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.NOTES) }
                    )
                }
                PanelCardType.SMART_GLANCE -> {
                    SmartGlanceWidgetCard(
                        briefing = smartGlanceBriefing,
                        formattedDate = currentDate,
                        onCalendarClick = onCalendarClick,
                        onWeatherClick = onRefreshWeather,
                        onNotesClick = onNotesClick,
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.CALENDAR) }
                    )
                }
                null -> {}
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Indicadores de navegação horizontal minimalistas (Dots refinados com 180ms ease-out)
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            repeat(pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 14.dp else 4.dp,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "dot_width"
                )
                val dotColor = if (isLightMode) {
                    if (isSelected) Color(0xFF111827) else Color(0xFFCBD5E1)
                } else {
                    if (isSelected) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.22f)
                }
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

/**
 * Screenshot 1: Ações Rápidas (4 squircle buttons: Sol, Lanterna, Bluetooth, Wi-Fi com estado ativo branco)
 */
@Composable
private fun QuickActionsWidgetCard(
    isTorchOn: Boolean,
    ringerMode: Int,
    onToggleTorch: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onCycleRingerMode: () -> Unit,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Brilho / Display
        QuickActionButton(
            icon = Icons.Outlined.WbSunny,
            isActive = false,
            contentDescription = "Brilho",
            isAmoledMode = isAmoledMode,
            isLightMode = isLightMode,
            isLiquidGlass = isLiquidGlass,
            onClick = {
                try {
                    context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (_: Exception) {}
            },
            onLongClick = onLongClick
        )

        // 2. Lanterna (Destaque ativo quando ligada)
        QuickActionButton(
            icon = Icons.Outlined.FlashlightOn,
            isActive = isTorchOn,
            contentDescription = "Lanterna",
            isAmoledMode = isAmoledMode,
            isLightMode = isLightMode,
            isLiquidGlass = isLiquidGlass,
            onClick = onToggleTorch,
            onLongClick = onLongClick
        )

        // 3. Bluetooth (Destaque ativo)
        QuickActionButton(
            icon = Icons.Outlined.Bluetooth,
            isActive = true,
            contentDescription = "Bluetooth",
            isAmoledMode = isAmoledMode,
            isLightMode = isLightMode,
            isLiquidGlass = isLiquidGlass,
            onClick = onOpenBluetooth,
            onLongClick = onLongClick
        )

        // 4. Wi-Fi (Destaque ativo)
        QuickActionButton(
            icon = Icons.Outlined.Wifi,
            isActive = true,
            contentDescription = "Wi-Fi",
            isAmoledMode = isAmoledMode,
            isLightMode = isLightMode,
            isLiquidGlass = isLiquidGlass,
            onClick = onOpenWifi,
            onLongClick = onLongClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    isActive: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val bgColor = when {
        isActive -> if (isLightMode) Color(0xFF0F172A) else Color.White.copy(alpha = 0.92f)
        isLightMode -> Color(0x14000000)
        isLiquidGlass -> Color.White.copy(alpha = 0.08f)
        isAmoledMode -> Color.White.copy(alpha = 0.05f)
        else -> Color(0xFF1E1E26)
    }
    val iconTint = when {
        isActive -> if (isLightMode) Color.White else Color(0xFF0F172A)
        isLightMode -> Color(0xFF0F172A)
        else -> Color.White
    }
    val borderStroke = when {
        isActive -> BorderStroke(1.dp, if (isLightMode) Color(0xFF0F172A) else Color.White.copy(alpha = 0.40f))
        isLightMode -> BorderStroke(1.dp, Color(0x20000000))
        isLiquidGlass -> BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        isAmoledMode -> BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
        else -> BorderStroke(1.dp, Color(0xFF2C2C38))
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .background(bgColor)
            .border(
                border = borderStroke,
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Screenshot 2: Conectividade & Bateria (3 pílulas: Bateria %, Wi-Fi, Celular)
 */
@Composable
private fun BatteryWidgetCard(
    batteryPercentage: Int,
    isCharging: Boolean,
    batteryWidgetStyle: String = "cards_3",
    onOpenWifi: () -> Unit = {},
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current

    if (batteryWidgetStyle == "bar") {
        // Estilo Barra Padrão
        val boxBg = if (isLightMode) Color(0x14000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.08f) else if (isAmoledMode) Color.White.copy(alpha = 0.05f) else Color(0xFF1E1E26)
        val boxBorder = if (isLightMode) Color(0x20000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.12f) else if (isAmoledMode) Color.White.copy(alpha = 0.10f) else Color(0xFF2C2C38)
        val contentColor = if (isLightMode) Color(0xFF0F172A) else Color.White

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        } catch (_: Exception) {}
                    },
                    onLongClick = onLongClick
                )
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(boxBg)
                    .border(BorderStroke(1.dp, boxBorder), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCharging) "Carregando • $batteryPercentage%" else "Bateria • $batteryPercentage%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { batteryPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = contentColor,
                    trackColor = if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF2C2C36)
                )
            }
        }
    } else {
        // Estilo 3 Cartões (Pills) — Padrão conforme Screenshot 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill 1: Bateria
            StatusPill(
                icon = if (isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                label = "$batteryPercentage%",
                isAmoledMode = isAmoledMode,
                isLightMode = isLightMode,
                isLiquidGlass = isLiquidGlass,
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (_: Exception) {}
                },
                onLongClick = onLongClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Pill 2: Wi-Fi
            StatusPill(
                icon = Icons.Outlined.Wifi,
                label = "Wi-Fi",
                isAmoledMode = isAmoledMode,
                isLightMode = isLightMode,
                isLiquidGlass = isLiquidGlass,
                onClick = onOpenWifi,
                onLongClick = onLongClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Pill 3: Celular
            StatusPill(
                icon = Icons.Outlined.SignalCellularAlt,
                label = "Cell",
                isAmoledMode = isAmoledMode,
                isLightMode = isLightMode,
                isLiquidGlass = isLiquidGlass,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (_: Exception) {}
                },
                onLongClick = onLongClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StatusPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(20.dp)
    val pillBg = if (isLightMode) Color(0x14000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.08f) else if (isAmoledMode) Color.White.copy(alpha = 0.05f) else Color(0xFF1C1C24)
    val pillBorder = if (isLightMode) Color(0x20000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.12f) else if (isAmoledMode) Color.White.copy(alpha = 0.10f) else Color(0xFF2A2A36)
    val contentColor = if (isLightMode) Color(0xFF0F172A) else Color.White

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(pillShape)
            .background(pillBg)
            .border(BorderStroke(1.dp, pillBorder), pillShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Screenshot 3: Calendário, Data & Horário
 */
@Composable
private fun CalendarWidgetCard(
    currentTime: String,
    currentDate: String,
    nextCalendarEvent: CalendarEventInfo?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onCalendarClick: () -> Unit,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val cal = remember { Calendar.getInstance() }
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val monthNames = listOf("JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ")
    val monthStr = monthNames.getOrElse(cal.get(Calendar.MONTH)) { "SEP" }
    val dayNames = listOf("DOMINGO", "SEGUNDA", "TERÇA", "QUARTA", "QUINTA", "SEXTA", "SÁBADO")
    val dayOfWeek = dayNames.getOrElse(cal.get(Calendar.DAY_OF_WEEK) - 1) { "MONDAY" }

    val currentMinuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val dayProgress = (currentMinuteOfDay / 1440f).coerceIn(0.05f, 1f)

    val badgeBg = if (isLightMode) Color(0x14000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.08f) else if (isAmoledMode) Color.White.copy(alpha = 0.05f) else Color(0xFF1C1C24)
    val badgeBorder = if (isLightMode) Color(0x20000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.12f) else if (isAmoledMode) Color.White.copy(alpha = 0.10f) else Color(0xFF2A2A36)
    val dayColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val monthColor = if (isLightMode) Color(0xFF475569) else Color(0xFFAAAAAA)
    val primaryText = if (isLightMode) Color(0xFF0F172A) else Color.White
    val secondaryText = if (isLightMode) Color(0xFF475569) else Color(0xFF8E8E98)
    val linkColor = if (isLightMode) Color(0xFF334155) else Color(0xFFAAAAAA)
    val progressColor = if (isLightMode) Color(0xFF0F172A) else Color(0xFFCCCCCC)
    val progressTrack = if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF262630)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (hasPermission) onCalendarClick() else onRequestPermission() },
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emblema Esquerdo (Dia / Mês)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(badgeBg)
                .border(BorderStroke(1.dp, badgeBorder), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayOfMonth,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = dayColor
                )
                Text(
                    text = monthStr,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = monthColor
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Conteúdo Direito
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Linha Superior: Dia da semana + Horário
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = primaryText
                )

                Text(
                    text = currentTime.ifBlank { "07:22" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = primaryText
                )
            }

            // Linha Central: Subtítulo do evento / data + Atalho "Calendar ↗"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (nextCalendarEvent != null) nextCalendarEvent.title else currentDate,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Calendar ↗",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = linkColor,
                    modifier = Modifier.clickable { onCalendarClick() }
                )
            }

            // Linha Inferior: Progresso fino do dia
            LinearProgressIndicator(
                progress = { dayProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = progressTrack
            )
        }
    }
}

/**
 * Screenshot 4: Player de Mídia
 */
@Composable
private fun MediaWidgetCard(
    mediaPlayback: MediaPlaybackInfo,
    hasNotificationAccess: Boolean,
    onRequestAccess: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenMusicApp: () -> Unit,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val placeholderBg = if (isLightMode) Color(0x14000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.08f) else if (isAmoledMode) Color.White.copy(alpha = 0.05f) else Color(0xFF1C1C24)
    val placeholderBorder = if (isLightMode) Color(0x20000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.12f) else if (isAmoledMode) Color.White.copy(alpha = 0.10f) else Color(0xFF2A2A36)
    val placeholderTint = if (isLightMode) Color(0xFF475569) else Color(0xFFAAAAAA)
    val primaryText = if (isLightMode) Color(0xFF0F172A) else Color.White
    val secondaryText = if (isLightMode) Color(0xFF475569) else Color(0xFF8E8E98)
    val controlTint = if (isLightMode) Color(0xFF334155) else Color.White.copy(alpha = 0.88f)
    val playBg = if (isLightMode) Color(0xFF0F172A) else Color.White.copy(alpha = 0.92f)
    val playTint = if (isLightMode) Color.White else Color(0xFF0F172A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpenMusicApp,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Capa do Álbum com cantos arredondados e borda sutil
        if (mediaPlayback.artwork != null) {
            Image(
                bitmap = mediaPlayback.artwork.asImageBitmap(),
                contentDescription = "Capa do álbum",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenMusicApp
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(placeholderBg)
                    .border(BorderStroke(1.dp, placeholderBorder), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenMusicApp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    tint = placeholderTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Título e Artista
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenMusicApp
                )
        ) {
            Text(
                text = mediaPlayback.title.ifBlank { "Sem reprodução" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = mediaPlayback.artist.ifBlank { "Toque para abrir música" },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Controles de Reprodução Compactos com alvos de toque aprimorados
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSkipPrevious
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = controlTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Play / Pause
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(playBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTogglePlayPause
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mediaPlayback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (mediaPlayback.isPlaying) "Pausar" else "Reproduzir",
                    tint = playTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSkipNext
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = "Próxima",
                    tint = controlTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Screenshot 5: Versículo / Foco do Dia
 */
@Composable
private fun VerseFocusWidgetCard(
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val quotes = remember {
        listOf(
            "“NOTHING HEARD, NOTHING SAID”",
            "“O Senhor é o meu pastor; de nada terei falta.”",
            "“Foco no essencial, simplifique tudo o resto.”",
            "“Tudo posso naquele que me fortalece.”",
            "“A simplicidade é o último grau de sofisticação.”"
        )
    }
    var currentQuoteIndex by remember { mutableIntStateOf(0) }

    val badgeBg = if (isLightMode) Color(0x14000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.08f) else if (isAmoledMode) Color.White.copy(alpha = 0.05f) else Color(0xFF1C1C24)
    val badgeBorder = if (isLightMode) Color(0x20000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.12f) else if (isAmoledMode) Color.White.copy(alpha = 0.10f) else Color(0xFF2A2A36)
    val quoteIconColor = if (isLightMode) Color(0xFF0F172A) else Color(0xFFAAAAAA)
    val headerColor = if (isLightMode) Color(0xFF475569) else Color(0xFF8E8E98)
    val quoteTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size },
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emblema de Aspas
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(badgeBg)
                .border(BorderStroke(1.dp, badgeBorder), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "”",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = quoteIconColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Coluna com Cabeçalho e Citação
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Foco do dia",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = headerColor
                )

                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Alternar",
                    tint = headerColor,
                    modifier = Modifier
                        .size(15.dp)
                        .clickable {
                            currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
                        }
                )
            }

            Text(
                text = quotes[currentQuoteIndex],
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp
                ),
                color = quoteTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Novo Widget de Clima & Temperatura (Open-Meteo API)
 */
@Composable
private fun WeatherWidgetCard(
    weatherInfo: WeatherInfo?,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onRefreshWeather: () -> Unit,
    isWeatherLoading: Boolean = false,
    weatherError: String? = null,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val badgeBg = if (isLightMode) Color(0x14000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.08f) else if (isAmoledMode) Color.White.copy(alpha = 0.05f) else Color(0xFF1C1C24)
    val badgeBorder = if (isLightMode) Color(0x20000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.12f) else if (isAmoledMode) Color.White.copy(alpha = 0.10f) else Color(0xFF2A2A36)
    val iconTint = if (isLightMode) Color(0xFF0F172A) else Color.White
    val primaryText = if (isLightMode) Color(0xFF0F172A) else Color.White
    val secondaryText = if (isLightMode) Color(0xFF475569) else Color(0xFF8E8E98)
    val subtleText = if (isLightMode) Color(0xFF334155) else Color(0xFFAAAAAA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!hasLocationPermission) {
                        onRequestLocationPermission()
                    }
                    onRefreshWeather()
                },
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weatherIcon = when {
            weatherInfo != null -> when (weatherInfo.weatherCode) {
                0, 1 -> Icons.Outlined.WbSunny
                2, 3, 45, 48 -> Icons.Outlined.Cloud
                51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Outlined.WaterDrop
                71, 73, 75, 77, 85, 86 -> Icons.Outlined.AcUnit
                95, 96, 99 -> Icons.Outlined.Thunderstorm
                else -> Icons.Outlined.WbSunny
            }
            !hasLocationPermission -> Icons.Outlined.LocationOn
            weatherError != null -> Icons.Outlined.Cloud
            else -> Icons.Outlined.Cloud
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(badgeBg)
                .border(BorderStroke(1.dp, badgeBorder), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = weatherIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        when {
            weatherInfo != null -> {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = weatherInfo.displayTemperature,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = primaryText
                        )

                        Text(
                            text = weatherInfo.cityName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = subtleText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = weatherInfo.condition,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = if (isWeatherLoading) "Atualizando..." else "Sensação ${weatherInfo.displayApparent}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = subtleText
                        )
                    }
                }
            }
            isWeatherLoading -> {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Carregando clima...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Atualizando dados meteorológicos",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = secondaryText
                    )
                }
            }
            weatherError != null -> {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Falha ao carregar clima",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Toque para tentar novamente",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = secondaryText
                        )
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            tint = secondaryText,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
            !hasLocationPermission -> {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ativar clima",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Toque para conceder localização",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = secondaryText
                    )
                }
            }
            else -> {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Obtendo previsão...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Toque para atualizar",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = secondaryText
                    )
                }
            }
        }
    }
}

/**
 * Notes & Tasks Widget
 */
@Composable
private fun NotesWidgetCard(
    tasks: List<NoteTask>,
    notesWidgetFilter: String,
    onAddTask: (String) -> Unit,
    onToggleTask: (Long) -> Unit,
    onRemoveTask: (Long) -> Unit,
    onNotesClick: () -> Unit = {},
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val topTask = tasks.firstOrNull()
    val badgeBg = if (isLightMode) Color(0x14000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.08f) else if (isAmoledMode) Color.White.copy(alpha = 0.05f) else Color(0xFF1C1C24)
    val badgeBorder = if (isLightMode) Color(0x20000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.12f) else if (isAmoledMode) Color.White.copy(alpha = 0.10f) else Color(0xFF2A2A36)
    val iconTint = if (isLightMode) Color(0xFF0F172A) else Color.White
    val primaryText = if (isLightMode) Color(0xFF0F172A) else Color.White
    val secondaryText = if (isLightMode) Color(0xFF475569) else Color(0xFF8E8E98)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onNotesClick,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(badgeBg)
                .border(BorderStroke(1.dp, badgeBorder), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.EventNote,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topTask?.text ?: "Nenhuma tarefa pendente",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val pendingCount = tasks.count { !it.isDone }
            Text(
                text = if (pendingCount > 0) "$pendingCount pendente${if (pendingCount > 1) "s" else ""} • Toque para ver" else "Diário e Tarefas • Toque para abrir",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = secondaryText
            )
        }
    }
}

/**
 * Now & Next (Smart Glance) Widget Card para o painel de widgets da doca.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SmartGlanceWidgetCard(
    briefing: SmartGlanceBriefing?,
    formattedDate: String,
    onCalendarClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onNotesClick: () -> Unit,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val activeBriefing = briefing ?: SmartGlanceBriefing(
        primaryText = "Tudo tranquilo hoje",
        secondaryText = null,
        iconType = "INFO",
        actionType = SmartGlanceActionType.CALENDAR,
        isUrgent = false
    )

    val badgeBg = if (isLightMode) Color(0x14000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.08f) else if (isAmoledMode) Color.White.copy(alpha = 0.05f) else Color(0xFF1C1C24)
    val badgeBorder = if (isLightMode) Color(0x20000000) else if (isLiquidGlass) Color.White.copy(alpha = 0.12f) else if (isAmoledMode) Color.White.copy(alpha = 0.10f) else Color(0xFF2A2A36)
    val primaryText = if (isLightMode) Color(0xFF0F172A) else Color.White
    val secondaryText = if (isLightMode) Color(0xFF475569) else Color(0xFF8E8E98)

    val icon: ImageVector = when (activeBriefing.iconType) {
        "ALERT" -> Icons.Outlined.Warning
        "CALENDAR" -> Icons.AutoMirrored.Outlined.EventNote
        "RAIN" -> Icons.Outlined.Cloud
        "SUN" -> Icons.Outlined.WbSunny
        "CHECK" -> Icons.Outlined.CheckCircle
        else -> Icons.Outlined.Info
    }

    val iconTint = if (activeBriefing.isUrgent) Color(0xFFEF4444) else primaryText

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    when (activeBriefing.actionType) {
                        SmartGlanceActionType.CALENDAR -> onCalendarClick()
                        SmartGlanceActionType.WEATHER -> onWeatherClick()
                        SmartGlanceActionType.NOTES -> onNotesClick()
                        SmartGlanceActionType.NONE -> onCalendarClick()
                    }
                },
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(badgeBg)
                .border(BorderStroke(1.dp, badgeBorder), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = activeBriefing.iconType,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = activeBriefing.primaryText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = activeBriefing.secondaryText ?: formattedDate.ifEmpty { "Resumo do dia" },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp
                ),
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

