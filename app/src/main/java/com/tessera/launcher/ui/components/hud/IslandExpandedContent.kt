package com.tessera.launcher.ui.components.hud

import android.app.PendingIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.components.DeliveryLiveActivityCard
import com.tessera.launcher.ui.components.NavigationLiveActivityCard
import com.tessera.launcher.ui.components.RideLiveActivityCard
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Modo expandido unificado da Ilha Dinâmica Tessera.
 */
@Composable
fun IslandExpandedContent(
    liveActivity: LiveActivityInfo?,
    mediaPlayback: MediaPlaybackInfo,
    defaultAccent: Color,
    onOpenApp: (String) -> Unit,
    onCollapse: () -> Unit,
    onPlayPauseMedia: () -> Unit,
    onNextMedia: () -> Unit,
    onPreviousMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (liveActivity != null) {
        when {
            liveActivity.packageDetails != null -> {
                PackageExpandedContent(
                    details = liveActivity.packageDetails,
                    accentColor = liveActivity.brandColor?.let { Color(it) } ?: defaultAccent,
                    onOpenAppClick = { onOpenApp(liveActivity.packageName) },
                    modifier = modifier
                )
            }
            liveActivity.rideDetails != null -> {
                RideLiveActivityCard(
                    rideDetails = liveActivity.rideDetails,
                    isLiquidGlass = false,
                    isAmoledMode = true,
                    isLightMode = false,
                    accentColor = liveActivity.brandColor?.let { Color(it) } ?: defaultAccent,
                    onCardClick = { onOpenApp(liveActivity.packageName) },
                    onActionClick = { intent ->
                        try {
                            intent?.send()
                        } catch (_: Exception) {}
                    },
                    onCollapseClick = onCollapse,
                    modifier = modifier
                )
            }
            liveActivity.deliveryDetails != null -> {
                DeliveryLiveActivityCard(
                    deliveryDetails = liveActivity.deliveryDetails,
                    isLiquidGlass = false,
                    isAmoledMode = true,
                    isLightMode = false,
                    accentColor = liveActivity.brandColor?.let { Color(it) } ?: defaultAccent,
                    onCardClick = { onOpenApp(liveActivity.packageName) },
                    onActionClick = { intent ->
                        try {
                            intent?.send()
                        } catch (_: Exception) {}
                    },
                    onCollapseClick = onCollapse,
                    modifier = modifier
                )
            }
            liveActivity.navigationDetails != null -> {
                NavigationLiveActivityCard(
                    navigationDetails = liveActivity.navigationDetails,
                    isLiquidGlass = false,
                    isAmoledMode = true,
                    isLightMode = false,
                    accentColor = liveActivity.brandColor?.let { Color(it) } ?: defaultAccent,
                    onCardClick = { onOpenApp(liveActivity.packageName) },
                    onActionClick = { intent ->
                        try {
                            intent?.send()
                        } catch (_: Exception) {}
                    },
                    onCollapseClick = onCollapse,
                    modifier = modifier
                )
            }
            else -> {
                // Expandido genérico de alta qualidade para Call, Timer, Fitness, Core, Progress
                GenericActivityExpandedContent(
                    activity = liveActivity,
                    defaultAccent = defaultAccent,
                    onOpenApp = { onOpenApp(liveActivity.packageName) },
                    modifier = modifier
                )
            }
        }
    } else {
        // Modo Expandido de Mídia Ativa (Spotify / YouTube Music / Apple Music)
        MediaExpandedContent(
            mediaPlayback = mediaPlayback,
            accentColor = defaultAccent,
            onOpenMediaApp = {
                mediaPlayback.packageName?.let { onOpenApp(it) }
            },
            onPlayPauseMedia = onPlayPauseMedia,
            onNextMedia = onNextMedia,
            onPreviousMedia = onPreviousMedia,
            modifier = modifier
        )
    }
}

@Composable
private fun GenericActivityExpandedContent(
    activity: LiveActivityInfo,
    defaultAccent: Color,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presentation = remember(activity, defaultAccent) {
        HudPresentationMapper.map(activity, defaultAccent)
    }

    Column(
        modifier = modifier
            .widthIn(min = 280.dp, max = 330.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val safeArtwork = remember(activity.artwork) {
                runCatching {
                    activity.artwork?.takeIf { !it.isRecycled }?.asImageBitmap()
                }.getOrNull()
            }

            if (safeArtwork != null) {
                Image(
                    bitmap = safeArtwork,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(presentation.accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = presentation.icon,
                        contentDescription = null,
                        tint = presentation.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = presentation.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!presentation.subtitle.isNullOrBlank()) {
                    Text(
                        text = presentation.subtitle,
                        color = Color(0xFFA1A1AA),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24))
                    .tesseraPressFeedback()
                    .clickable { onOpenApp() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = "Abrir aplicativo",
                    tint = Color(0xFFA1A1AA),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Cronômetro ou Barra de Progresso
        if (presentation.isChronometer && presentation.chronometerBaseMs != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                LiveChronometer(
                    baseMs = presentation.chronometerBaseMs,
                    isCountDown = presentation.isCountDown,
                    color = presentation.accentColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (presentation.progressPercent != null && presentation.progressPercent > 0f) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { presentation.progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    color = presentation.accentColor,
                    trackColor = Color(0xFF27272A)
                )
                if (!presentation.criticalText.isNullOrBlank()) {
                    Text(
                        text = presentation.criticalText,
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Botões de Ação
        if (activity.actions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activity.actions.take(2).forEach { action ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(presentation.accentColor.copy(alpha = 0.15f))
                            .border(1.dp, presentation.accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .tesseraPressFeedback()
                            .clickable {
                                try {
                                    action.intent?.send()
                                } catch (_: Exception) {}
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = action.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = presentation.accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaExpandedContent(
    mediaPlayback: MediaPlaybackInfo,
    accentColor: Color,
    onOpenMediaApp: () -> Unit,
    onPlayPauseMedia: () -> Unit,
    onNextMedia: () -> Unit,
    onPreviousMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(min = 280.dp, max = 320.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val safeArtwork = remember(mediaPlayback.artwork) {
                runCatching {
                    mediaPlayback.artwork?.takeIf { !it.isRecycled }?.asImageBitmap()
                }.getOrNull()
            }

            if (safeArtwork != null) {
                Image(
                    bitmap = safeArtwork,
                    contentDescription = "Capa do Álbum",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E1E24)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MusicNote,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mediaPlayback.title.ifBlank { "Reproduzindo" },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mediaPlayback.artist.ifBlank { mediaPlayback.appDisplayName },
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24))
                    .tesseraPressFeedback()
                    .clickable { onOpenMediaApp() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = "Abrir player",
                    tint = Color(0xFFA1A1AA),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Controles de Mídia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24))
                    .tesseraPressFeedback()
                    .clickable { onPreviousMedia() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .tesseraPressFeedback()
                    .clickable { onPlayPauseMedia() },
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
                    .background(Color(0xFF1E1E24))
                    .tesseraPressFeedback()
                    .clickable { onNextMedia() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = "Próxima",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
