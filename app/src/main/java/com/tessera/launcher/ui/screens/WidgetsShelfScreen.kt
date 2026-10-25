package com.tessera.launcher.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.tessera.launcher.ui.components.BatteryWidgetCard
import com.tessera.launcher.ui.components.CalendarWidgetCard
import com.tessera.launcher.ui.components.DinoWidgetCard
import com.tessera.launcher.ui.components.MediaWidgetCard
import com.tessera.launcher.ui.components.NoteTask
import com.tessera.launcher.ui.components.NotesWidgetCard
import com.tessera.launcher.ui.components.NotificationsWidgetCard
import com.tessera.launcher.ui.components.QuickActionsWidgetCard
import com.tessera.launcher.ui.components.ScreenTimeWidgetCard
import com.tessera.launcher.ui.components.SmartGlanceWidgetCard
import com.tessera.launcher.ui.components.VerseFocusWidgetCard
import com.tessera.launcher.ui.components.WeatherWidgetCard
import com.tessera.launcher.ui.state.WidgetConfigType
import java.util.Locale

/**
 * Página Dedicada de Widgets (Shelf — Tela −1).
 * Layout editorial em mosaico no estilo Journal, com fundo translúcido (papel vegetal / frosted glass)
 * sobreposto organicamente ao papel de parede do usuário.
 */
@Composable
fun WidgetsShelfScreen(
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
    onOpenMusicApp: () -> Unit,
    isDinoWidgetEnabled: Boolean,
    isNotesWidgetEnabled: Boolean,
    isTorchOn: Boolean,
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
    isLiquidGlass: Boolean,
    isAmoledMode: Boolean,
    isLightMode: Boolean,
    smartGlanceBriefing: SmartGlanceBriefing?,
    isSmartGlanceEnabled: Boolean,
    predictedApps: List<AppInfo>,
    isPredictedAppsWidgetEnabled: Boolean,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    iconShape: String,
    isThemedIcons: Boolean,
    accentColor: Color,
    screenTimeInfo: ScreenTimeInfo?,
    isScreenTimeWidgetEnabled: Boolean,
    recentNotifications: List<AppNotificationItem>,
    notificationCounts: Map<String, Int>,
    onDismissNotification: (String) -> Unit,
    onOpenNotificationsHub: () -> Unit,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    BackHandler(onBack = onClose)

    // Fundo Translúcido com Desfoque (Papel Vegetal no Claro / Frosted Glass no Escuro)
    val shelfBg = when {
        isLightMode -> Color(0xDDF7F4EB) // Papel vegetal Journal quente
        isAmoledMode -> Color(0xE8000000)
        else -> Color(0xD90B0B0D)
    }

    val primaryTextColor = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF2F2F7)
    val secondaryTextColor = if (isLightMode) Color(0xFF57534E) else Color(0xFF8E8E93)
    val iconBtnBg = if (isLightMode) Color(0xFFE8E4D8) else Color(0x22FFFFFF)
    val iconBtnBorder = if (isLightMode) Color(0xFFDDD8CB) else Color(0x20FFFFFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(shelfBg)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Editorial da Shelf
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HOJE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        color = if (accentColor != Color.White && accentColor != Color(0xFF111827) && accentColor != Color(0xFF334155)) {
                            accentColor
                        } else {
                            secondaryTextColor
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryTextColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Botão Customizar Widgets
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconBtnBg)
                            .border(1.dp, iconBtnBorder, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenSettings
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Configurar Widgets",
                            tint = primaryTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Botão Fechar Shelf / Voltar à Home
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconBtnBg)
                            .border(1.dp, iconBtnBorder, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClose
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                            tint = primaryTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Mosaico Editorial de Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
            ) {
                // 1. Mídia / Player
                if (enabledWidgets.contains(WidgetType.MEDIA)) {
                    item(key = "shelf_media") {
                        val activePlayback = mediaPlayback ?: MediaPlaybackInfo()
                        Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
                            MediaWidgetCard(
                                mediaPlayback = activePlayback,
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
                    }
                }

                // 2. Clima Detalhado
                if (enabledWidgets.contains(WidgetType.WEATHER)) {
                    item(key = "shelf_weather") {
                        Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
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
                    }
                }

                // 3. Ações Rápidas (Quick Toggles)
                item(key = "shelf_quick_actions") {
                    Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
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
                }

                // 4. Agenda & Calendário
                if (enabledWidgets.contains(WidgetType.CALENDAR)) {
                    item(key = "shelf_calendar") {
                        Box(modifier = Modifier.fillMaxWidth().height(105.dp)) {
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

                // 5. Bateria & Status
                if (enabledWidgets.contains(WidgetType.BATTERY)) {
                    item(key = "shelf_battery") {
                        Box(modifier = Modifier.fillMaxWidth().height(85.dp)) {
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
                    }
                }

                // 6. Anotações & Tarefas Rápidas
                if (isNotesWidgetEnabled) {
                    item(key = "shelf_notes") {
                        Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
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

                // 7. Tempo de Uso de Tela
                if (isScreenTimeWidgetEnabled || enabledWidgets.contains(WidgetType.SCREEN_TIME)) {
                    item(key = "shelf_screen_time") {
                        Box(modifier = Modifier.fillMaxWidth().height(105.dp)) {
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

                // 8. Versículo / Frase Inspiradora
                item(key = "shelf_verse") {
                    Box(modifier = Modifier.fillMaxWidth().height(105.dp)) {
                        VerseFocusWidgetCard(
                            isAmoledMode = isAmoledMode,
                            isLightMode = isLightMode,
                            isLiquidGlass = isLiquidGlass,
                            onLongClick = { onWidgetLongClick(WidgetConfigType.VERSE_FOCUS) }
                        )
                    }
                }

                // 9. Smart Glance
                if (isSmartGlanceEnabled && smartGlanceBriefing != null) {
                    item(key = "shelf_smart_glance") {
                        Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
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

                // 10. Dino Runner (se ativo)
                if (isDinoWidgetEnabled) {
                    item(key = "shelf_dino") {
                        Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
                            DinoWidgetCard(isDockMode = true)
                        }
                    }
                }

                // 11. Hub de Notificações
                if (hasNotificationAccess && recentNotifications.isNotEmpty()) {
                    item(key = "shelf_notifications") {
                        Box(modifier = Modifier.fillMaxWidth().height(95.dp)) {
                            NotificationsWidgetCard(
                                recentNotifications = recentNotifications,
                                onOpenHub = onOpenNotificationsHub,
                                onDismissTop = {
                                    recentNotifications.firstOrNull()?.let { top ->
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
    }
}
