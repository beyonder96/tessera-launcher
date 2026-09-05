package com.tessera.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.GraphicEq
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.state.MediaState
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary

@Composable
fun WidgetsPanel(
    batteryPercentage: Int,
    isCharging: Boolean,
    currentTime: String,
    currentDate: String,
    mediaState: MediaState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Linha 1: Cartão de Data/Relógio e Cartão de Bateria
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mini-card Data e Relógio (60% da largura)
            Surface(
                modifier = Modifier
                    .weight(1.3f)
                    .height(96.dp),
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentTime,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Mini-card Bateria e Sinais (40% da largura)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$batteryPercentage%",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
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

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isCharging) "Carregando" else "Bateria",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
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

        // Linha 2: Mini-card de Mídia Interativo
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            shape = CardShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone / Capa monocromática
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CardShape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GraphicEq,
                        contentDescription = "Mídia",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info da Faixa
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = mediaState.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mediaState.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controles de Reprodução
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
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
                                onClick = onTogglePlayPause
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (mediaState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (mediaState.isPlaying) "Pausar" else "Reproduzir",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = "Próximo",
                        tint = TextSecondary,
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
    }
}
