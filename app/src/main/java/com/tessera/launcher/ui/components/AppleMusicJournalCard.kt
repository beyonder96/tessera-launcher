package com.tessera.launcher.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.service.MediaPlaybackInfo
import kotlinx.coroutines.delay
import java.util.Locale

private fun formatMediaDuration(timeMs: Long): String {
    if (timeMs <= 0) return "00:00"
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

/**
 * Mini equalizador de áudio animado de 3 barras no estilo Apple Music.
 */
@Composable
private fun MiniAudioEqualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF1C1917)
) {
    val transition = rememberInfiniteTransition(label = "miniAudioEq")
    val bar1Height by transition.animateFloat(
        initialValue = 4f,
        targetValue = 13f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by transition.animateFloat(
        initialValue = 12f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(310, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by transition.animateFloat(
        initialValue = 5f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(470, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val b1 = if (isPlaying) bar1Height.dp else 4.dp
        val b2 = if (isPlaying) bar2Height.dp else 6.dp
        val b3 = if (isPlaying) bar3Height.dp else 3.dp

        Box(modifier = Modifier.width(2.5.dp).height(b1).clip(CircleShape).background(barColor))
        Box(modifier = Modifier.width(2.5.dp).height(b2).clip(CircleShape).background(barColor))
        Box(modifier = Modifier.width(2.5.dp).height(b3).clip(CircleShape).background(barColor))
    }
}

/**
 * Card Hero Apple Music × Journal com Scrubber real, Capa viva e controles físicos táteis.
 */
@Composable
fun AppleMusicJournalCard(
    media: MediaPlaybackInfo?,
    hasNotificationAccess: Boolean,
    onRequestNotificationAccess: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onSeekTo: (Long) -> Unit,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isLightMode) Color(0xFFEFECE2) else Color(0xFF161820)
    val borderColor = if (isLightMode) Color(0xFFDDD8CB) else Color(0x25FFFFFF)
    val textPrimary = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
    val textSecondary = if (isLightMode) Color(0xFF78716C) else Color(0xFFA8A29E)

    // 1. Permissão Pendente
    if (!hasNotificationAccess) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(cardBg)
                .border(0.5.dp, borderColor, RoundedCornerShape(22.dp))
                .tesseraPressable(onClick = onRequestNotificationAccess)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                JournalMedallion(
                    icon = Icons.Outlined.Headphones,
                    contentDescription = null,
                    size = 44.dp,
                    isLightMode = isLightMode
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Conectar Player Apple Music",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Text(
                        text = "Toque para habilitar leitura e controles de mídia ao vivo.",
                        fontSize = 11.5.sp,
                        color = textSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Conectar",
                    tint = textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        return
    }

    // 2. Estado Vazio / Sem música ativa
    if (media == null || !media.hasActiveSession || media.title.isBlank()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(cardBg)
                .border(0.5.dp, borderColor, RoundedCornerShape(22.dp))
                .tesseraPressable(onClick = onOpenMusicApp)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                JournalMedallion(
                    icon = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    size = 44.dp,
                    isLightMode = isLightMode
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Player em Espera",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Text(
                        text = "Toque para abrir ${media?.appDisplayName ?: "Música"} e reproduzir.",
                        fontSize = 11.5.sp,
                        color = textSecondary
                    )
                }
            }
        }
        return
    }

    // 3. Estado Ativo com Reprodução Estilo Apple Music
    val isPlaying = media.isPlaying
    var livePositionMs by remember(media.positionMs, media.isPlaying, media.lastPositionUpdateTime) {
        mutableLongStateOf(media.getCalculatedPositionMs())
    }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(media.isPlaying, media.positionMs, media.lastPositionUpdateTime, media.durationMs) {
        if (media.isPlaying) {
            while (true) {
                if (!isScrubbing) {
                    livePositionMs = media.getCalculatedPositionMs()
                }
                delay(250L)
            }
        } else {
            if (!isScrubbing) {
                livePositionMs = media.positionMs
            }
        }
    }

    val currentFraction = if (isScrubbing) {
        scrubFraction
    } else {
        if (media.durationMs > 0L) {
            (livePositionMs.toFloat() / media.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    // Animação de escala e elevação da Capa Viva (Apple Music Now Playing)
    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.91f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f),
        label = "artworkScale"
    )
    val artworkElevation by animateDpAsState(
        targetValue = if (isPlaying) 8.dp else 2.dp,
        animationSpec = tween(300),
        label = "artworkElevation"
    )

    // Extrai leve reflexo cromático da capa
    val ambientTint = remember(media.artwork) {
        media.artwork?.let { bmp ->
            runCatching {
                val pixel = bmp.getPixel(bmp.width / 2, bmp.height / 2)
                Color(pixel).copy(alpha = if (isLightMode) 0.08f else 0.16f)
            }.getOrNull()
        } ?: Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(0.5.dp, borderColor, RoundedCornerShape(24.dp))
            .drawWithContent {
                drawContent()
                if (ambientTint != Color.Transparent) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(ambientTint, Color.Transparent),
                            center = Offset(size.width * 0.25f, size.height * 0.35f),
                            radius = size.width * 0.8f
                        )
                    )
                }
            }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Linha Superior: Capa Viva + Metadados + Selo do App
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tesseraPressable(onClick = onOpenMusicApp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Capa Viva com física Apple Music
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            scaleX = artworkScale
                            scaleY = artworkScale
                        }
                        .shadow(artworkElevation, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isLightMode) Color(0xFFDDD8CB) else Color(0xFF222634)),
                    contentAlignment = Alignment.Center
                ) {
                    if (media.artwork != null) {
                        Image(
                            bitmap = media.artwork.asImageBitmap(),
                            contentDescription = media.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Metadados
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Cápsula do App + Mini-Equalizador
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLightMode) Color(0x181C1917) else Color(0x25FFFFFF))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = media.appDisplayName.uppercase(Locale.getDefault()),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = textPrimary
                            )
                        }

                        if (isPlaying) {
                            MiniAudioEqualizer(
                                isPlaying = true,
                                barColor = textPrimary
                            )
                        }
                    }

                    Text(
                        text = media.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val subtitle = if (media.album.isNotBlank()) "${media.artist} — ${media.album}" else media.artist
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Barra de Progresso / Scrubber Interativo Apple Music
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val trackHeight by animateDpAsState(
                    targetValue = if (isScrubbing) 7.dp else 4.5.dp,
                    animationSpec = tween(150),
                    label = "scrubberHeight"
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .pointerInput(media.durationMs) {
                            detectTapGestures { offset ->
                                if (media.durationMs > 0L) {
                                    val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    val targetMs = (frac * media.durationMs).toLong()
                                    livePositionMs = targetMs
                                    onSeekTo(targetMs)
                                }
                            }
                        }
                        .pointerInput(media.durationMs) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    if (media.durationMs > 0L) {
                                        isScrubbing = true
                                        scrubFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    }
                                },
                                onDragEnd = {
                                    if (media.durationMs > 0L) {
                                        isScrubbing = false
                                        val targetMs = (scrubFraction * media.durationMs).toLong()
                                        livePositionMs = targetMs
                                        onSeekTo(targetMs)
                                    }
                                },
                                onDragCancel = {
                                    isScrubbing = false
                                },
                                onHorizontalDrag = { change, _ ->
                                    if (media.durationMs > 0L) {
                                        scrubFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(trackHeight)
                            .clip(CircleShape)
                            .background(if (isLightMode) Color(0x221C1917) else Color(0x35FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(currentFraction)
                                .clip(CircleShape)
                                .background(textPrimary)
                        )
                    }
                }

                // Timestamps (Decorrido à esquerda, Restante negativo à direita)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayCurrentMs = if (isScrubbing && media.durationMs > 0L) {
                        (scrubFraction * media.durationMs).toLong()
                    } else livePositionMs

                    Text(
                        text = formatMediaDuration(displayCurrentMs),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )

                    val remainingMs = if (media.durationMs > 0L) {
                        (media.durationMs - displayCurrentMs).coerceAtLeast(0L)
                    } else 0L

                    Text(
                        text = if (media.durationMs > 0L) "-${formatMediaDuration(remainingMs)}" else "00:00",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )
                }
            }

            // Controles Físicos Táteis Apple Music
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Anterior
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .tesseraPressable(pressedScale = 0.88f, onClick = onSkipPrevious),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = textPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Play / Pause Central em Medalhão Sólido Contrastante
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB))
                        .tesseraPressable(pressedScale = 0.90f, onClick = onTogglePlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                        tint = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF1C1917),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Próxima
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .tesseraPressable(pressedScale = 0.88f, onClick = onSkipNext),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Próxima",
                        tint = textPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
