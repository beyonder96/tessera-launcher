package com.tessera.launcher.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.helper.ScreenTimeInfo
import com.tessera.launcher.data.helper.SmartGlanceBriefing
import com.tessera.launcher.data.helper.WeatherInfo
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.model.AppNotificationItem
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.components.AppleMusicJournalCard
import com.tessera.launcher.ui.components.DinoWidgetCard
import com.tessera.launcher.ui.components.JournalCascadeItem
import com.tessera.launcher.ui.components.JournalEditorialHeader
import com.tessera.launcher.ui.components.JournalTelemetryRingsRow
import com.tessera.launcher.ui.components.NoteTask
import com.tessera.launcher.ui.components.NotificationsWidgetCard
import com.tessera.launcher.ui.components.QuickControlBentoGrid
import com.tessera.launcher.ui.components.SmartDockRow
import com.tessera.launcher.ui.components.VerseFocusWidgetCard
import com.tessera.launcher.ui.components.WidgetsQuickEditBottomSheet
import com.tessera.launcher.ui.components.openOrRequestDigitalWellbeing
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
    hasTopSegmentedControl: Boolean = false,
    onQuickEditVisibilityChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var isQuickEditOpen by remember { mutableStateOf(false) }

    LaunchedEffect(isQuickEditOpen) {
        onQuickEditVisibilityChange(isQuickEditOpen)
    }

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
            // Zona 1: Cabeçalho Editorial com Saudação Dinâmica e Botões do Topo (Chips exclusivos de Agenda e Tarefas)
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
                        onScreenTimeClick = {
                            openOrRequestDigitalWellbeing(context, screenTimeInfo?.hasPermission == true)
                        }
                    )
                }
            }

            // Zona 1.5: Palco Apple Music Hero Card
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

            // Zona 2.5: Trio de Anéis de Telemetria (Bateria, Clima, Foco/Tempo de Tela Clicável)
            item(key = "zone_telemetry_rings") {
                JournalCascadeItem(index = 3) {
                    JournalTelemetryRingsRow(
                        batteryPercentage = batteryPercentage,
                        isCharging = isCharging,
                        weatherInfo = weatherInfo,
                        screenTimeInfo = screenTimeInfo,
                        isLightMode = isLightMode,
                        onWeatherRefresh = onRefreshWeather,
                        onScreenTimeClick = {
                            openOrRequestDigitalWellbeing(context, screenTimeInfo?.hasPermission == true)
                        }
                    )
                }
            }

            // Zona 3: Módulos Exclusivos em Molduras Bento Journal Alinhadas (Zero Redundância)
            // 1. Central de Alertas e Notificações (Deduplica música ativa)
            if (hasNotificationAccess && recentNotifications.isNotEmpty()) {
                val activeMusicPkg = mediaPlayback?.packageName
                val filteredNotifications = recentNotifications.filterNot { item ->
                    item.isOngoing && activeMusicPkg != null && item.packageName == activeMusicPkg
                }

                if (filteredNotifications.isNotEmpty()) {
                    item(key = "shelf_notifications") {
                        JournalCascadeItem(index = 4) {
                            JournalBentoCard(isLightMode = isLightMode) {
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
            }

            // 2. Apps Sugeridos pela IA (Bandeja Contextual em Card Bento)
            if (isPredictedAppsWidgetEnabled && predictedApps.isNotEmpty()) {
                item(key = "shelf_predicted_apps") {
                    JournalCascadeItem(index = 5) {
                        JournalBentoCard(isLightMode = isLightMode) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SUGESTÕES DA IA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = if (isLightMode) Color(0xFF78716C) else Color(0xFFA8A29E)
                                )
                                SmartDockRow(
                                    apps = predictedApps,
                                    onAppClick = onAppClick,
                                    onAppLongClick = onAppLongClick,
                                    iconShape = iconShape,
                                    isThemedIcons = isThemedIcons,
                                    isLightMode = isLightMode,
                                    isAmoledMode = isAmoledMode,
                                    isEmbedded = true,
                                    notificationCounts = notificationCounts,
                                    accentColor = accentColor
                                )
                            }
                        }
                    }
                }
            }

            // 3. Versículo / Foco Espiritual (Configurável e moldado em Bento)
            item(key = "shelf_verse") {
                JournalCascadeItem(index = 6) {
                    JournalBentoCard(isLightMode = isLightMode) {
                        VerseFocusWidgetCard(
                            isAmoledMode = isAmoledMode,
                            isLightMode = isLightMode,
                            isLiquidGlass = isLiquidGlass,
                            onLongClick = { onWidgetLongClick(WidgetConfigType.VERSE_FOCUS) }
                        )
                    }
                }
            }

            // 4. Dino Game Widget (Card Bento dedicado com altura e contraste corrigidos)
            if (isDinoWidgetEnabled) {
                item(key = "shelf_dino") {
                    JournalCascadeItem(index = 7) {
                        JournalBentoCard(isLightMode = isLightMode) {
                            DinoWidgetCard(
                                isDockMode = true,
                                isLightMode = isLightMode,
                                isLiquidGlass = isLiquidGlass
                            )
                        }
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

/**
 * Moldura Bento Journal consistente com as outras seções da página.
 * Aplica acabamento lapidado, borda sutil e fundo adaptativo (#EFECE2 no claro / #161820 no escuro).
 */
@Composable
private fun JournalBentoCard(
    isLightMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cardBg = if (isLightMode) Color(0xFFEFECE2) else Color(0xFF161820)
    val cardBorder = if (isLightMode) Color(0xFFDDD8CB) else Color(0x25FFFFFF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        content()
    }
}
