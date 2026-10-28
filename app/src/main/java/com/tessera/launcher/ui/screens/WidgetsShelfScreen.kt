package com.tessera.launcher.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.helper.ScreenTimeInfo
import com.tessera.launcher.data.helper.SmartGlanceBriefing
import com.tessera.launcher.data.helper.WeatherInfo
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.model.AppNotificationItem
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.components.AppleMusicJournalCard
import com.tessera.launcher.ui.components.CalendarWidgetCard
import com.tessera.launcher.ui.components.DinoWidgetCard
import com.tessera.launcher.ui.components.JournalCascadeItem
import com.tessera.launcher.ui.components.JournalEditorialHeader
import com.tessera.launcher.ui.components.JournalTelemetryRingsRow
import com.tessera.launcher.ui.components.NoteTask
import com.tessera.launcher.ui.components.NotesWidgetCard
import com.tessera.launcher.ui.components.NotificationsWidgetCard
import com.tessera.launcher.ui.components.QuickControlBentoGrid
import com.tessera.launcher.ui.components.ScreenTimeWidgetCard
import com.tessera.launcher.ui.components.SmartGlanceWidgetCard
import com.tessera.launcher.ui.components.VerseFocusWidgetCard
import com.tessera.launcher.ui.components.WidgetsQuickEditBottomSheet
import com.tessera.launcher.ui.state.WidgetConfigType

/**
 * Página Dedicada de Widgets (Shelf — Tela −1).
 * Layout editorial Bento Journal com estética de papel quente (#F7F4EB),
 * Player estilo Apple Music, anéis de telemetria e medalhões lapidados.
 */
@Composable
fun WidgetsShelfScreen(
    userName: String = "Kenned",
    onSaveUserName: (String) -> Unit = {},
    enabledWidgets: List<WidgetType>,
    batteryPercentage: Int,
    isCharging: Boolean,
    currentTime: String,
    formattedDate: String,
    nextCalendarEvent: CalendarEventInfo?,
    hasCalendarPermission: Boolean,
    onRequestCalendarPermission: () -> Unit,
    onCalendarClick: () -> Unit,
    mediaPlayback: MediaPlaybackInfo?,
    hasNotificationAccess: Boolean,
    onRequestNotificationAccess: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit = {},
    onOpenMusicApp: () -> Unit,
    isDinoWidgetEnabled: Boolean,
    onToggleDino: (Boolean) -> Unit = {},
    isNotesWidgetEnabled: Boolean,
    isTorchOn: Boolean,
    isWifiActive: Boolean = true,
    isBluetoothActive: Boolean = false,
    ringerMode: Int,
    onToggleTorch: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onCycleRingerMode: () -> Unit,
    notesTasks: List<NoteTask>,
    onAddNoteTask: (String) -> Unit,
    onToggleNoteTask: (Long) -> Unit,
    onRemoveNoteTask: (Long) -> Unit,
    onNotesClick: () -> Unit,
    weatherInfo: WeatherInfo?,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onRefreshWeather: () -> Unit,
    isWeatherLoading: Boolean,
    weatherError: String?,
    batteryWidgetStyle: String,
    mediaWidgetStyle: String,
    notesWidgetFilter: String,
    onWidgetLongClick: (WidgetConfigType) -> Unit,
    onToggleWidget: (WidgetType) -> Unit = {},
    onMoveWidgetUp: (WidgetType) -> Unit = {},
    onMoveWidgetDown: (WidgetType) -> Unit = {},
    isLiquidGlass: Boolean,
    isAmoledMode: Boolean,
    isLightMode: Boolean,
    smartGlanceBriefing: SmartGlanceBriefing?,
    isSmartGlanceEnabled: Boolean,
    predictedApps: List<AppInfo>,
    isPredictedAppsWidgetEnabled: Boolean,
    onTogglePredictedApps: (Boolean) -> Unit = {},
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    iconShape: String,
    isThemedIcons: Boolean,
    accentColor: Color,
    screenTimeInfo: ScreenTimeInfo?,
    isScreenTimeWidgetEnabled: Boolean,
    onToggleScreenTime: (Boolean) -> Unit = {},
    recentNotifications: List<AppNotificationItem>,
    notificationCounts: Map<String, Int>,
    onDismissNotification: (String) -> Unit,
    onOpenNotificationsHub: () -> Unit,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
    hasTopSegmentedControl: Boolean = false
) {
    var isQuickEditOpen by remember { mutableStateOf(false) }

    // Gating no BackHandler: não fecha a central se o Bottom Sheet estiver aberto
    BackHandler(enabled = !isQuickEditOpen, onBack = onClose)

    val shelfBg = when {
        isLightMode -> Color(0xFFF7F4EB)
        isAmoledMode -> Color.Black
        else -> Color(0xFF0F1014)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(shelfBg)
            .then(if (!hasTopSegmentedControl) Modifier.statusBarsPadding() else Modifier)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 48.dp)
        ) {
            // Zona 1: Cabeçalho Editorial com Saudação Dinâmica e Botões do Topo
            item(key = "zone_editorial_header") {
                JournalCascadeItem(index = 0) {
                    JournalEditorialHeader(
                        userName = userName,
                        formattedDate = formattedDate,
                        weatherInfo = weatherInfo,
                        nextCalendarEvent = nextCalendarEvent,
                        pendingNotesCount = notesTasks.count { !it.isDone },
                        screenTimeInfo = screenTimeInfo,
                        isLightMode = isLightMode,
                        onOpenQuickEdit = { isQuickEditOpen = true },
                        onBackToHome = onClose,
                        onWeatherClick = onRefreshWeather,
                        onCalendarClick = onCalendarClick,
                        onNotesClick = onNotesClick,
                        onScreenTimeClick = {}
                    )
                }
            }

            // Zona 1: Palco Apple Music Hero Card
            if (enabledWidgets.contains(WidgetType.MEDIA)) {
                item(key = "zone_apple_music_hero") {
                    JournalCascadeItem(index = 1) {
                        AppleMusicJournalCard(
                            media = mediaPlayback,
                            hasNotificationAccess = hasNotificationAccess,
                            onRequestNotificationAccess = onRequestNotificationAccess,
                            onTogglePlayPause = onTogglePlayPause,
                            onSkipNext = onSkipNext,
                            onSkipPrevious = onSkipPrevious,
                            onOpenMusicApp = onOpenMusicApp,
                            onSeekTo = onSeekTo,
                            isLightMode = isLightMode
                        )
                    }
                }
            }

            // Zona 2: Grade Bento 2x2 de Controles Táteis (Wi-Fi, BT, Lanterna, Som)
            item(key = "zone_bento_controls") {
                JournalCascadeItem(index = 2) {
                    QuickControlBentoGrid(
                        isWifiActive = isWifiActive,
                        isBluetoothActive = isBluetoothActive,
                        isTorchOn = isTorchOn,
                        ringerMode = ringerMode,
                        onOpenWifi = onOpenWifi,
                        onOpenBluetooth = onOpenBluetooth,
                        onToggleTorch = onToggleTorch,
                        onCycleRingerMode = onCycleRingerMode,
                        isLightMode = isLightMode
                    )
                }
            }

            // Zona 2: Trio de Anéis de Telemetria (Bateria, Clima, Tempo de Tela)
            item(key = "zone_telemetry_rings") {
                JournalCascadeItem(index = 3) {
                    JournalTelemetryRingsRow(
                        batteryPercentage = batteryPercentage,
                        isCharging = isCharging,
                        weatherInfo = weatherInfo,
                        screenTimeInfo = screenTimeInfo,
                        isLightMode = isLightMode,
                        onWeatherRefresh = onRefreshWeather,
                        onScreenTimeClick = {}
                    )
                }
            }

            // Zona 3: Capítulos Journal Abertos na sequência configurada
            // 1. Now & Next (Smart Glance)
            if (isSmartGlanceEnabled && smartGlanceBriefing != null) {
                item(key = "shelf_smart_glance") {
                    JournalCascadeItem(index = 4) {
                        SmartGlanceWidgetCard(
                            briefing = smartGlanceBriefing,
                            formattedDate = formattedDate,
                            onCalendarClick = onCalendarClick,
                            onWeatherClick = onRefreshWeather,
                            onNotesClick = onNotesClick,
                            isAmoledMode = isAmoledMode,
                            isLightMode = isLightMode,
                            isLiquidGlass = isLiquidGlass,
                            onLongClick = { onWidgetLongClick(WidgetConfigType.CALENDAR) }
                        )
                    }
                }
            }

            // 2. Agenda & Calendário
            if (enabledWidgets.contains(WidgetType.CALENDAR)) {
                item(key = "shelf_calendar") {
                    JournalCascadeItem(index = 5) {
                        CalendarWidgetCard(
                            currentTime = currentTime,
                            currentDate = formattedDate,
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
                }
            }

            // 3. Central de Alertas e Notificações (Deduplica mídia ativa)
            if (hasNotificationAccess && recentNotifications.isNotEmpty()) {
                val activeMusicPkg = mediaPlayback?.packageName
                val filteredNotifications = recentNotifications.filterNot { item ->
                    item.isOngoing && activeMusicPkg != null && item.packageName == activeMusicPkg
                }

                if (filteredNotifications.isNotEmpty()) {
                    item(key = "shelf_notifications") {
                        JournalCascadeItem(index = 6) {
                            NotificationsWidgetCard(
                                recentNotifications = filteredNotifications,
                                onOpenHub = onOpenNotificationsHub,
                                onDismissTop = {
                                    filteredNotifications.firstOrNull()?.let { top ->
                                        onDismissNotification(top.key)
                                    }
                                },
                                accentColor = accentColor,
                                isAmoledMode = isAmoledMode,
                                isLightMode = isLightMode,
                                isLiquidGlass = isLiquidGlass
                            )
                        }
                    }
                }
            }

            // 4. Anotações & Tarefas Rápidas
            if (isNotesWidgetEnabled) {
                item(key = "shelf_notes") {
                    JournalCascadeItem(index = 7) {
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
                }
            }

            // 5. Tempo de Tela & Bem-Estar Detalhado
            if (isScreenTimeWidgetEnabled || enabledWidgets.contains(WidgetType.SCREEN_TIME)) {
                item(key = "shelf_screen_time") {
                    JournalCascadeItem(index = 8) {
                        ScreenTimeWidgetCard(
                            screenTimeInfo = screenTimeInfo,
                            isLightMode = isLightMode,
                            isLiquidGlass = isLiquidGlass,
                            isAmoledMode = isAmoledMode,
                            accentColor = accentColor,
                            onCardClick = {},
                            onLongClick = { onWidgetLongClick(WidgetConfigType.CALENDAR) }
                        )
                    }
                }
            }

            // 6. Versículo / Foco Espiritual
            item(key = "shelf_verse") {
                JournalCascadeItem(index = 9) {
                    VerseFocusWidgetCard(
                        isAmoledMode = isAmoledMode,
                        isLightMode = isLightMode,
                        isLiquidGlass = isLiquidGlass,
                        onLongClick = { onWidgetLongClick(WidgetConfigType.VERSE_FOCUS) }
                    )
                }
            }

            // 7. Dino Game Widget (se ativo)
            if (isDinoWidgetEnabled) {
                item(key = "shelf_dino") {
                    JournalCascadeItem(index = 10) {
                        DinoWidgetCard(
                            isDockMode = true,
                            isLightMode = isLightMode,
                            isLiquidGlass = isLiquidGlass
                        )
                    }
                }
            }
        }

        // Bottom Sheet Journal de Personalização Rápida Ao Vivo
        if (isQuickEditOpen) {
            WidgetsQuickEditBottomSheet(
                userName = userName,
                onSaveUserName = onSaveUserName,
                enabledWidgets = enabledWidgets,
                onToggleWidget = onToggleWidget,
                onMoveWidgetUp = onMoveWidgetUp,
                onMoveWidgetDown = onMoveWidgetDown,
                isDinoEnabled = isDinoWidgetEnabled,
                onToggleDino = onToggleDino,
                isScreenTimeEnabled = isScreenTimeWidgetEnabled,
                onToggleScreenTime = onToggleScreenTime,
                isSmartDockEnabled = isPredictedAppsWidgetEnabled,
                onToggleSmartDock = onTogglePredictedApps,
                isLightMode = isLightMode,
                onOpenFullSettings = {
                    isQuickEditOpen = false
                    onOpenSettings()
                },
                onDismiss = { isQuickEditOpen = false }
            )
        }
    }
}
