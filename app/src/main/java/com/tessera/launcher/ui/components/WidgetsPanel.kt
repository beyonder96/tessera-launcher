package com.tessera.launcher.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import java.util.Calendar

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
    weatherInfo: WeatherInfo? = null,
    hasLocationPermission: Boolean = false,
    onRequestLocationPermission: () -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    batteryWidgetStyle: String = "cards_3",
    mediaWidgetStyle: String = "classic",
    notesWidgetFilter: String = "all",
    onWidgetLongClick: (WidgetConfigType) -> Unit = {},
    isLiquidGlass: Boolean = false
) {
    // Ordem das páginas:
    // 0: Ações Rápidas (Screenshot 1)
    // 1: Bateria & Conectividade (Screenshot 2)
    // 2: Calendário & Horário (Screenshot 3)
    // 3: Mídia (Screenshot 4)
    // 4: Foco / Citação do dia (Screenshot 5)
    // 5: Clima & Temperatura
    // 6: Dino Run (se habilitado)
    // 7: Notas / Tarefas (se habilitado)
    val pageCount = 6 + (if (isDinoWidgetEnabled) 1 else 0) + (if (isNotesWidgetEnabled) 1 else 0)

    val initialPage = remember {
        if (isSwitchOnMusicPlayEnabled && mediaPlayback.isPlaying) 3
        else defaultWidgetCardIndex.coerceIn(0, pageCount - 1)
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(mediaPlayback.isPlaying) {
        if (isSwitchOnMusicPlayEnabled && mediaPlayback.isPlaying) {
            pagerState.animateScrollToPage(3)
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
            when (page) {
                0 -> {
                    QuickActionsWidgetCard(
                        isTorchOn = isTorchOn,
                        ringerMode = ringerMode,
                        onToggleTorch = onToggleTorch,
                        onOpenWifi = onOpenWifi,
                        onOpenBluetooth = onOpenBluetooth,
                        onCycleRingerMode = onCycleRingerMode,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.QUICK_ACTIONS) }
                    )
                }
                1 -> {
                    BatteryWidgetCard(
                        batteryPercentage = batteryPercentage,
                        isCharging = isCharging,
                        batteryWidgetStyle = batteryWidgetStyle,
                        onOpenWifi = onOpenWifi,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.BATTERY) }
                    )
                }
                2 -> {
                    CalendarWidgetCard(
                        currentTime = currentTime,
                        currentDate = currentDate,
                        nextCalendarEvent = nextCalendarEvent,
                        hasPermission = hasCalendarPermission,
                        onRequestPermission = onRequestCalendarPermission,
                        onCalendarClick = onCalendarClick,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.CALENDAR) }
                    )
                }
                3 -> {
                    MediaWidgetCard(
                        mediaPlayback = mediaPlayback,
                        hasNotificationAccess = hasNotificationAccess,
                        onRequestAccess = onRequestNotificationAccess,
                        onTogglePlayPause = onTogglePlayPause,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onOpenMusicApp = onOpenMusicApp,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.MEDIA) }
                    )
                }
                4 -> {
                    VerseFocusWidgetCard(
                        onLongClick = { onWidgetLongClick(WidgetConfigType.VERSE_FOCUS) }
                    )
                }
                5 -> {
                    WeatherWidgetCard(
                        weatherInfo = weatherInfo,
                        hasLocationPermission = hasLocationPermission,
                        onRequestLocationPermission = onRequestLocationPermission,
                        onRefreshWeather = onRefreshWeather,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.WEATHER) }
                    )
                }
                6 -> {
                    if (isDinoWidgetEnabled) {
                        DinoWidgetCard()
                    } else if (isNotesWidgetEnabled) {
                        NotesWidgetCard(
                            tasks = notesTasks,
                            notesWidgetFilter = notesWidgetFilter,
                            onAddTask = onAddNoteTask,
                            onToggleTask = onToggleNoteTask,
                            onRemoveTask = onRemoveNoteTask,
                            onLongClick = { onWidgetLongClick(WidgetConfigType.NOTES) }
                        )
                    }
                }
                7 -> {
                    if (isNotesWidgetEnabled) {
                        NotesWidgetCard(
                            tasks = notesTasks,
                            notesWidgetFilter = notesWidgetFilter,
                            onAddTask = onAddNoteTask,
                            onToggleTask = onToggleNoteTask,
                            onRemoveTask = onRemoveNoteTask,
                            onLongClick = { onWidgetLongClick(WidgetConfigType.NOTES) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Indicadores de navegação horizontal minimalistas (Dots conforme prints do usuário)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            repeat(pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 16.dp else 4.dp,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "dot_width"
                )
                Box(
                    modifier = Modifier
                        .height(3.5.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0xFF383844))
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
    onLongClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Brilho / Display
        QuickActionButton(
            icon = Icons.Outlined.WbSunny,
            isActive = false,
            contentDescription = "Brilho",
            onClick = {
                try {
                    context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (_: Exception) {}
            },
            onLongClick = onLongClick
        )

        // 2. Lanterna (Destaque branco quando ligada)
        QuickActionButton(
            icon = Icons.Outlined.FlashlightOn,
            isActive = isTorchOn,
            contentDescription = "Lanterna",
            onClick = onToggleTorch,
            onLongClick = onLongClick
        )

        // 3. Bluetooth (Destaque ativo conforme print)
        QuickActionButton(
            icon = Icons.Outlined.Bluetooth,
            isActive = true,
            contentDescription = "Bluetooth",
            onClick = onOpenBluetooth,
            onLongClick = onLongClick
        )

        // 4. Wi-Fi (Destaque ativo conforme print)
        QuickActionButton(
            icon = Icons.Outlined.Wifi,
            isActive = true,
            contentDescription = "Wi-Fi",
            onClick = onOpenWifi,
            onLongClick = onLongClick
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    isActive: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val bgColor = if (isActive) Color.White else Color(0xFF1E1E26)
    val iconTint = if (isActive) Color.Black else Color.White

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .border(
                border = if (isActive) BorderStroke(1.dp, Color.White) else BorderStroke(1.dp, Color(0xFF2C2C38)),
                shape = shape
            )
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
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
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current

    if (batteryWidgetStyle == "bar") {
        // Estilo Barra Padrão
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick() }
                    )
                }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E26)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCharging) "Carregando • $batteryPercentage%" else "Bateria • $batteryPercentage%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { batteryPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color(0xFF2C2C36)
                )
            }
        }
    } else {
        // Estilo 3 Cartões (Pills) — Padrão conforme Screenshot 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick() }
                    )
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill 1: Bateria
            StatusPill(
                icon = if (isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                label = "$batteryPercentage%",
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
                onClick = onOpenWifi,
                onLongClick = onLongClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Pill 3: Celular
            StatusPill(
                icon = Icons.Outlined.SignalCellularAlt,
                label = "Cell",
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

@Composable
private fun StatusPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(pillShape)
            .border(BorderStroke(1.dp, Color(0xFF2A2A36)), pillShape)
            .background(Color(0xFF1C1C24))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
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
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = Color.White
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (hasPermission) onCalendarClick() else onRequestPermission() },
                    onLongPress = { onLongClick() }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emblema Esquerdo (Dia / Mês)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .border(BorderStroke(1.dp, Color(0xFF2A2A36)), RoundedCornerShape(13.dp))
                .background(Color(0xFF1C1C24)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayOfMonth,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = monthStr,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = Color(0xFFAAAAAA)
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
                    color = Color.White
                )

                Text(
                    text = currentTime.ifBlank { "07:22" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = Color.White
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
                    color = Color(0xFF8E8E98),
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
                    color = Color(0xFFAAAAAA),
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
                color = Color(0xFFCCCCCC),
                trackColor = Color(0xFF262630)
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
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Capa do Álbum
        if (mediaPlayback.artwork != null) {
            Image(
                bitmap = mediaPlayback.artwork.asImageBitmap(),
                contentDescription = "Capa do álbum",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                    .clip(RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, Color(0xFF2A2A36)), RoundedCornerShape(10.dp))
                    .background(Color(0xFF1C1C24))
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
                    tint = Color(0xFFAAAAAA),
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
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = mediaPlayback.artist.ifBlank { "Toque para abrir música" },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF8E8E98),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Controles de Reprodução Compactos
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.SkipPrevious,
                contentDescription = "Anterior",
                tint = Color(0xFFAAAAAA),
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSkipPrevious
                    )
            )

            // Play / Pause em Squircle Branco com Ícone Preto
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.White)
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
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }

            Icon(
                imageVector = Icons.Outlined.SkipNext,
                contentDescription = "Próxima",
                tint = Color(0xFFAAAAAA),
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSkipNext
                    )
            )
        }
    }
}

/**
 * Screenshot 5: Versículo / Foco do Dia
 */
@Composable
private fun VerseFocusWidgetCard(
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size },
                    onLongPress = { onLongClick() }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emblema de Aspas
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .border(BorderStroke(1.dp, Color(0xFF2A2A36)), RoundedCornerShape(13.dp))
                .background(Color(0xFF1C1C24)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "”",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = Color(0xFFAAAAAA)
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
                    color = Color(0xFF8E8E98)
                )

                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Alternar",
                    tint = Color(0xFF8E8E98),
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
                color = Color.White,
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
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (!hasLocationPermission) onRequestLocationPermission()
                        else {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=clima+tempo"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    },
                    onLongPress = { onLongClick() }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weatherIcon = when (weatherInfo?.weatherCode) {
            0, 1 -> Icons.Outlined.WbSunny
            2, 3, 45, 48 -> Icons.Outlined.Cloud
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Outlined.WaterDrop
            71, 73, 75, 77, 85, 86 -> Icons.Outlined.AcUnit
            95, 96, 99 -> Icons.Outlined.Thunderstorm
            else -> Icons.Outlined.WbSunny
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .border(BorderStroke(1.dp, Color(0xFF2A2A36)), RoundedCornerShape(13.dp))
                .background(Color(0xFF1C1C24)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = weatherIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (!hasLocationPermission) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ativar previsão do tempo",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Text(
                    text = "Toque para conceder localização",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF8E8E98)
                )
            }
        } else if (weatherInfo != null) {
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
                        color = Color.White
                    )

                    Text(
                        text = weatherInfo.cityName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = Color(0xFFAAAAAA)
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
                        color = Color(0xFF8E8E98),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Clima ↗",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFFAAAAAA)
                    )
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Obtendo previsão...",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Text(
                    text = "Aguarde atualização de satélite",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF8E8E98)
                )
            }
        }
    }
}

/**
 * Dino Run Mini Widget
 */
@Composable
private fun DinoWidgetCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DINO RUNNER",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Toque para jogar",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextSecondary
        )
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
    onLongClick: () -> Unit = {}
) {
    val topTask = tasks.firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .border(BorderStroke(1.dp, Color(0xFF2A2A36)), RoundedCornerShape(13.dp))
                .background(Color(0xFF1C1C24)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topTask?.text ?: "Nenhuma tarefa pendente",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Diário e Tarefas",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF8E8E98)
            )
        }
    }
}
