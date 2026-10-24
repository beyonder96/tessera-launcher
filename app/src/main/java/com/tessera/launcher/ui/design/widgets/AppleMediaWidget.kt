package com.tessera.launcher.ui.design.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

/**
 * Widget Oficial de Mídia da Apple (iOS Music Widget).
 * Capa do álbum em squircle, controles de reprodução táteis e visualização tipográfica.
 */
@Composable
fun AppleMediaWidget(
    mediaPlayback: MediaPlaybackInfo,
    onMediaClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(ContinuousShape(22.dp))
            .background(colors.secondaryGrouped)
            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(22.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Topo: Capa e Detalhes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMediaClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mediaPlayback.artwork != null) {
                    Image(
                        bitmap = mediaPlayback.artwork.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(ContinuousShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(ContinuousShape(14.dp))
                            .background(Color(0xFFFF2D55)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaPlayback.title.ifBlank { "Nenhuma Reprodução" },
                        style = typography.subheadline.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mediaPlayback.artist.ifBlank { "Toque para abrir player" },
                        style = typography.caption1,
                        color = colors.secondaryLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Controles de Reprodução Táteis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = colors.label,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            Taptic.lightImpact(context)
                            onPreviousClick()
                        }
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .clickable {
                            Taptic.lightImpact(context)
                            onPlayPauseClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (mediaPlayback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = "Próximo",
                    tint = colors.label,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            Taptic.lightImpact(context)
                            onNextClick()
                        }
                )
            }
        }
    }
}
