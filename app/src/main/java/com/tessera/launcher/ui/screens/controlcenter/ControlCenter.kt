package com.tessera.launcher.ui.screens.controlcenter

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DoNotDisturb
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NetworkCell
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import com.tessera.launcher.ui.theme.apple.appleSpring

/**
 * Central de Controle Oficial Apple (iOS Control Center).
 * Módulos em Liquid Glass com grid 4x4, sliders verticais fluidos de volume e brilho,
 * controle de mídia e atalhos rápidos com feedback tátil.
 */
@Composable
fun ControlCenter(
    visible: Boolean,
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    mediaPlayback: MediaPlaybackInfo,
    onPlayPauseClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    var volumeLevel by remember {
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
        mutableFloatStateOf(current.toFloat() / max.toFloat())
    }

    var brightnessLevel by remember { mutableFloatStateOf(0.7f) }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = appleSpring()) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = appleSpring()) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .liquidGlass(style = GlassStyle.Chrome)
                .statusBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Linha 1: Bloco de Conectividade + Bloco de Mídia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Módulo de Conectividade (2x2 interno)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .clip(ContinuousShape(22.dp))
                            .background(colors.secondaryGrouped)
                            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(22.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ControlIconButton(
                                    icon = Icons.Outlined.AirplanemodeActive,
                                    isActive = false,
                                    activeColor = Color(0xFFFF9500),
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        }
                                    }
                                )
                                ControlIconButton(
                                    icon = Icons.Outlined.NetworkCell,
                                    isActive = true,
                                    activeColor = Color(0xFF34C759),
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        }
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ControlIconButton(
                                    icon = Icons.Outlined.Wifi,
                                    isActive = true,
                                    activeColor = Color(0xFF007AFF),
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        }
                                    }
                                )
                                ControlIconButton(
                                    icon = Icons.Outlined.Bluetooth,
                                    isActive = true,
                                    activeColor = Color(0xFF007AFF),
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Módulo de Mídia
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .clip(ContinuousShape(22.dp))
                            .background(colors.secondaryGrouped)
                            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(22.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MusicNote,
                                    contentDescription = null,
                                    tint = Color(0xFFFF2D55),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mediaPlayback.title.ifBlank { "Mídia" },
                                    style = typography.caption1.copy(fontWeight = FontWeight.Bold),
                                    color = colors.label,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = mediaPlayback.artist.ifBlank { "Nenhuma reprodução" },
                                style = typography.caption2,
                                color = colors.secondaryLabel,
                                maxLines = 1
                            )

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(CircleShape)
                                    .background(colors.fill)
                                    .clickable {
                                        Taptic.lightImpact(context)
                                        onPlayPauseClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mediaPlayback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = colors.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Linha 2: Sliders Verticais (Brilho + Volume) e Ações Rápidas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Slider Vertical de Brilho
                    AppleVerticalSlider(
                        value = brightnessLevel,
                        icon = Icons.Outlined.WbSunny,
                        onValueChange = { brightnessLevel = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                    )

                    // Slider Vertical de Volume
                    AppleVerticalSlider(
                        value = volumeLevel,
                        icon = Icons.Outlined.VolumeUp,
                        onValueChange = { newVol ->
                            volumeLevel = newVol
                            audioManager?.let { am ->
                                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (newVol * max).toInt(), 0)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                    )

                    // Grade 2x1 de Botões Utilitários (Lanterna e Calculadora)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Botão Lanterna
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(ContinuousShape(18.dp))
                                .background(if (isTorchOn) Color.White else colors.secondaryGrouped)
                                .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(18.dp))
                                .clickable {
                                    Taptic.selection(context)
                                    onToggleTorch()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FlashlightOn,
                                contentDescription = "Lanterna",
                                tint = if (isTorchOn) Color.Black else colors.label,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Botão Não Perturbe
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(ContinuousShape(18.dp))
                                .background(colors.secondaryGrouped)
                                .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(18.dp))
                                .clickable {
                                    Taptic.lightImpact(context)
                                    runCatching {
                                        context.startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        })
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DoNotDisturb,
                                contentDescription = "Não Perturbe",
                                tint = Color(0xFF5856D6),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlIconButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else colors.fill)
            .clickable {
                Taptic.selection(context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color.White else colors.label,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Slider Vertical Padrão Apple Control Center.
 */
@Composable
private fun AppleVerticalSlider(
    value: Float,
    icon: ImageVector,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current

    Box(
        modifier = modifier
            .clip(ContinuousShape(22.dp))
            .background(colors.secondaryGrouped)
            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(22.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = -dragAmount / size.height.toFloat()
                    val newValue = (value + delta).coerceIn(0f, 1f)
                    onValueChange(newValue)
                }
            }
    ) {
        // Nível de preenchimento fluido
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(value.coerceIn(0f, 1f))
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.95f))
        )

        // Ícone indicador no fundo inferior
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (value > 0.35f) Color.Black else colors.label,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(24.dp)
        )
    }
}
