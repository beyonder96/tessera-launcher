package com.tessera.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.LiquidGlassSurfaceBrush
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary

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
    isLiquidGlass: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        enabledWidgets.forEach { widgetType ->
            when (widgetType) {
                WidgetType.BATTERY -> {
                    BatteryWidgetCard(
                        batteryPercentage = batteryPercentage,
                        isCharging = isCharging,
                        isLiquidGlass = isLiquidGlass
                    )
                }
                WidgetType.CALENDAR -> {
                    CalendarWidgetCard(
                        currentTime = currentTime,
                        currentDate = currentDate,
                        nextCalendarEvent = nextCalendarEvent,
                        hasPermission = hasCalendarPermission,
                        onRequestPermission = onRequestCalendarPermission,
                        onCalendarClick = onCalendarClick,
                        isLiquidGlass = isLiquidGlass
                    )
                }
                WidgetType.MEDIA -> {
                    MediaWidgetCard(
                        mediaPlayback = mediaPlayback,
                        hasNotificationAccess = hasNotificationAccess,
                        onRequestAccess = onRequestNotificationAccess,
                        onTogglePlayPause = onTogglePlayPause,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onOpenMusicApp = onOpenMusicApp,
                        isLiquidGlass = isLiquidGlass
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryWidgetCard(
    batteryPercentage: Int,
    isCharging: Boolean,
    isLiquidGlass: Boolean = true
) {
    val cardBorder = if (isLiquidGlass) BorderStroke(1.dp, LiquidGlassBorderBrush) else BorderStroke(1.dp, DarkSurfaceBorder)
    val cardBg = if (isLiquidGlass) Modifier.background(LiquidGlassSurfaceBrush) else Modifier.background(DarkSurface)
    val iconBg = if (isLiquidGlass) Color(0x33FFFFFF) else DarkSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(
                elevation = if (isLiquidGlass) 10.dp else 4.dp,
                shape = CardShape,
                ambientColor = if (isLiquidGlass) Color(0x33000000) else Color.Black.copy(alpha = 0.4f),
                spotColor = if (isLiquidGlass) Color(0x4D000000) else Color.Black.copy(alpha = 0.4f)
            )
            .border(cardBorder, CardShape)
            .clip(CardShape)
            .then(cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isCharging -> Icons.Outlined.BatteryChargingFull
                        batteryPercentage <= 15 -> Icons.Outlined.BatteryAlert
                        else -> Icons.Outlined.BatteryStd
                    },
                    contentDescription = "Bateria",
                    tint = if (isCharging) TextPrimary else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCharging) "Bateria Carregando" else "Nível de Bateria",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "$batteryPercentage%",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { batteryPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = TextPrimary,
                    trackColor = DarkSurfaceBorder
                )
            }
        }
    }
}

@Composable
private fun CalendarWidgetCard(
    currentTime: String,
    currentDate: String,
    nextCalendarEvent: CalendarEventInfo?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onCalendarClick: () -> Unit,
    isLiquidGlass: Boolean = true
) {
    val cardBorder = if (isLiquidGlass) BorderStroke(1.dp, LiquidGlassBorderBrush) else BorderStroke(1.dp, DarkSurfaceBorder)
    val cardBg = if (isLiquidGlass) Modifier.background(LiquidGlassSurfaceBrush) else Modifier.background(DarkSurface)
    val iconBg = if (isLiquidGlass) Color(0x33FFFFFF) else DarkSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isLiquidGlass) 10.dp else 4.dp,
                shape = CardShape,
                ambientColor = if (isLiquidGlass) Color(0x33000000) else Color.Black.copy(alpha = 0.4f),
                spotColor = if (isLiquidGlass) Color(0x4D000000) else Color.Black.copy(alpha = 0.4f)
            )
            .border(cardBorder, CardShape)
            .clip(CardShape)
            .then(cardBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (hasPermission) onCalendarClick() else onRequestPermission()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Calendário",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (!hasPermission) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Toque para sincronizar Google Agenda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else if (nextCalendarEvent != null) {
                    Text(
                        text = "Próximo: ${nextCalendarEvent.title} (${nextCalendarEvent.timeFormatted})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Sem eventos restantes hoje",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaWidgetCard(
    mediaPlayback: MediaPlaybackInfo,
    hasNotificationAccess: Boolean,
    onRequestAccess: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenMusicApp: () -> Unit,
    isLiquidGlass: Boolean = true
) {
    val cardBorder = if (isLiquidGlass) BorderStroke(1.dp, LiquidGlassBorderBrush) else BorderStroke(1.dp, DarkSurfaceBorder)
    val cardBg = if (isLiquidGlass) Modifier.background(LiquidGlassSurfaceBrush) else Modifier.background(DarkSurface)
    val iconBg = if (isLiquidGlass) Color(0x33FFFFFF) else DarkSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isLiquidGlass) 10.dp else 4.dp,
                shape = CardShape,
                ambientColor = if (isLiquidGlass) Color(0x33000000) else Color.Black.copy(alpha = 0.4f),
                spotColor = if (isLiquidGlass) Color(0x4D000000) else Color.Black.copy(alpha = 0.4f)
            )
            .border(cardBorder, CardShape)
            .clip(CardShape)
            .then(cardBg)
    ) {
        if (!hasNotificationAccess) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRequestAccess
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Acesso a Notificações",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Controle de Mídia do Sistema",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Toque para conceder acesso às notificações",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = PillShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Text(
                        text = "Conceder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Capa do álbum ou ícone do app de música
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
                            .background(DarkSurfaceVariant)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenMusicApp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = "Abrir app de música",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

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
                        text = mediaPlayback.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mediaPlayback.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controles de Reprodução Reais
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = if (mediaPlayback.hasActiveSession) TextSecondary else TextTertiary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                enabled = mediaPlayback.hasActiveSession,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSkipPrevious
                            )
                    )

                    Surface(
                        shape = CircleShape,
                        color = DarkSurfaceBorder,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (mediaPlayback.hasActiveSession) {
                                        onTogglePlayPause()
                                    } else {
                                        onOpenMusicApp()
                                    }
                                }
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (mediaPlayback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (mediaPlayback.isPlaying) "Pausar" else "Reproduzir",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = "Próximo",
                        tint = if (mediaPlayback.hasActiveSession) TextSecondary else TextTertiary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                enabled = mediaPlayback.hasActiveSession,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSkipNext
                            )
                    )
                }
            }
        }
    }
}
