package com.tessera.launcher.ui.components

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.R
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.sin
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Modo Repouso (StandBy / Ambient Smart Display).
 * - Relógio Mecânico Flip 3D (estilo placar de virar dígitos)
 * - Widget de Clima compacto em formato de chip translúcido
 * - Espaço dinâmico para a capa do álbum quando houver música ativa,
 *   diminuindo o tamanho do relógio de forma animada e mantendo a foto no tamanho original
 * - Ícone da marca Tessera Launcher no cabeçalho com retorno à tela inicial
 * - Rotação livre por sensor (orientação paisagem ou vertical)
 */
@Composable
fun PhotoStandByModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    photoUriString: String?,
    formattedTime: String,
    formattedDate: String,
    batteryPercentage: Int,
    isCharging: Boolean,
    weatherTemp: String? = null,
    weatherCondition: String? = null,
    weatherCity: String? = null,
    accentColor: Color = Color(0xFFFFA040),
    mediaPlayback: MediaPlaybackInfo? = null,
    onTogglePlayPause: () -> Unit = {},
    onOpenMusicApp: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val now = remember(formattedTime) { Date() }
    val dayOfWeek = remember(now) {
        SimpleDateFormat("EEEE", Locale.getDefault()).format(now).replaceFirstChar { it.uppercase() }
    }
    val monthDay = remember(now) {
        val locale = Locale.getDefault()
        if (locale.language == "pt") {
            SimpleDateFormat("d 'de' MMMM", locale).format(now)
        } else {
            SimpleDateFormat("MMMM d", locale).format(now)
        }.replaceFirstChar { it.uppercase() }
    }

    val isMusicActive = mediaPlayback != null && (
        mediaPlayback.isPlaying || (mediaPlayback.hasActiveSession && mediaPlayback.title.isNotBlank())
    )

    // Ativa rotação automática por sensor enquanto o Modo Repouso estiver aberto de forma segura
    DisposableEffect(isOpen) {
        var act: Activity? = null
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                act = ctx
                break
            }
            ctx = ctx.baseContext
        }
        val activity = act
        if (isOpen && activity != null) {
            val previousOrientation = activity.requestedOrientation
            runCatching {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
            onDispose {
                runCatching {
                    activity.requestedOrientation = previousOrientation
                }
            }
        } else {
            onDispose {}
        }
    }

    // Carrega o bitmap da foto em IO thread
    val bitmap by produceState<Bitmap?>(initialValue = null, photoUriString) {
        value = if (!photoUriString.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(photoUriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
        } else {
            null
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(260)) + scaleIn(
            animationSpec = tween(320, easing = FastOutSlowInEasing),
            initialScale = 0.88f
        ),
        exit = fadeOut(tween(200)) + scaleOut(
            animationSpec = tween(200),
            targetScale = 0.92f
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Sutil vinheta neutra monocromática ao fundo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            ),
                            radius = 1100f
                        )
                    )
            )

            // Barra Superior de Controles e Status (perfeitamente centralizada e espaçada)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp, start = 24.dp, end = 24.dp)
            ) {
                // Ícone do Tessera Launcher (canto superior esquerdo)
                StandByTesseraBadge(
                    onDismiss = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Em modo paisagem, os chips de clima e bateria são apresentados centralizados no topo
                if (isLandscape) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandByWeatherChip(
                            weatherTemp = weatherTemp,
                            weatherCondition = weatherCondition,
                            weatherCity = weatherCity,
                            formattedTime = formattedTime
                        )
                        StandByBatteryChip(
                            batteryPercentage = batteryPercentage,
                            isCharging = isCharging
                        )
                    }
                }

                // Botão Fechar discreto (canto superior direito)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        )
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar modo repouso",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (isLandscape) {
                // ==========================================
                // LAYOUT PAISAGEM (Horizontal) — 2 Quadrantes Balanceados
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, bottom = 20.dp, start = 28.dp, end = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quadrante Esquerdo (50%): Relógio Mecânico Flip + Data
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            StandByFlipClock(
                                formattedTime = formattedTime,
                                isMusicActive = isMusicActive,
                                isLandscape = true,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val dayFontSize = if (isMusicActive) 21.sp else 24.sp
                            val monthFontSize = if (isMusicActive) 16.sp else 18.5.sp

                            Text(
                                text = dayOfWeek,
                                fontSize = dayFontSize,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.3).sp,
                                color = Color.White
                            )
                            Text(
                                text = monthDay,
                                fontSize = monthFontSize,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }

                    // Quadrante Direito (50%): Cards (Mídia + Foto)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val photoCardSize by animateDpAsState(
                            targetValue = if (isMusicActive) 152.dp else 192.dp,
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                dampingRatio = Spring.DampingRatioNoBouncy
                            ),
                            label = "PhotoCardSize"
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Capa do Álbum (quando houver música ativa)
                            AnimatedVisibility(
                                visible = isMusicActive,
                                enter = fadeIn(tween(260)) + expandHorizontally(tween(300)),
                                exit = fadeOut(tween(200)) + shrinkHorizontally(tween(260))
                            ) {
                                if (mediaPlayback != null) {
                                    StandByAlbumCard(
                                        mediaPlayback = mediaPlayback,
                                        cardSize = 152.dp,
                                        onTogglePlayPause = onTogglePlayPause,
                                        onOpenMusicApp = onOpenMusicApp
                                    )
                                }
                            }

                            // Card de Foto
                            StandByPhotoCard(
                                bitmap = bitmap,
                                cardSize = photoCardSize
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // LAYOUT RETRATO (Vertical)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Chips de Clima e Bateria no topo do fluxo vertical
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandByWeatherChip(
                            weatherTemp = weatherTemp,
                            weatherCondition = weatherCondition,
                            weatherCity = weatherCity,
                            formattedTime = formattedTime
                        )
                        StandByBatteryChip(
                            batteryPercentage = batteryPercentage,
                            isCharging = isCharging
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // Relógio Flip Animado
                    StandByFlipClock(
                        formattedTime = formattedTime,
                        isMusicActive = isMusicActive,
                        isLandscape = false,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val dayFontSize = if (isMusicActive) 20.sp else 23.sp
                    val monthFontSize = if (isMusicActive) 15.sp else 18.sp

                    Text(
                        text = dayOfWeek,
                        fontSize = dayFontSize,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    )
                    Text(
                        text = monthDay,
                        fontSize = monthFontSize,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFA1A1AA)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    val portraitPhotoSize by animateDpAsState(
                        targetValue = if (isMusicActive) 140.dp else 176.dp,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        ),
                        label = "PortraitPhotoSize"
                    )

                    // Linha de Cartões (Capa do Álbum e Foto)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = isMusicActive,
                            enter = fadeIn(tween(260)) + expandHorizontally(tween(300)),
                            exit = fadeOut(tween(200)) + shrinkHorizontally(tween(260))
                        ) {
                            if (mediaPlayback != null) {
                                StandByAlbumCard(
                                    mediaPlayback = mediaPlayback,
                                    cardSize = 140.dp,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onOpenMusicApp = onOpenMusicApp
                                )
                            }
                        }

                        StandByPhotoCard(
                            bitmap = bitmap,
                            cardSize = portraitPhotoSize
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Toque em qualquer lugar para sair",
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

/**
 * Badge do Ícone do Tessera Launcher no cabeçalho.
 * Renderiza o ícone do aplicativo de forma segura (sem usar painterResource em adaptive-icon)
 * e, ao tocar, fecha o Modo Repouso retornando imediatamente à tela inicial.
 */
@Composable
private fun StandByTesseraBadge(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcherIconBitmap = remember(context) {
        runCatching {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(context.packageName)
            val size = 96
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
            bmp
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        if (launcherIconBitmap != null && !launcherIconBitmap.isRecycled) {
            Image(
                bitmap = launcherIconBitmap.asImageBitmap(),
                contentDescription = "Tessera Launcher",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_tessera_header_logo),
                contentDescription = "Tessera Launcher",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
        }
    }
}

/**
 * Chip de Clima Compacto e Translúcido.
 * Exibe ícone dinâmico lunar/solar, temperatura e condição resumida em tom monocromático.
 */
@Composable
private fun StandByWeatherChip(
    weatherTemp: String?,
    weatherCondition: String?,
    weatherCity: String?,
    formattedTime: String,
    modifier: Modifier = Modifier
) {
    val tempDigits = weatherTemp?.filter { it.isDigit() || it == '-' }?.ifEmpty { "24" } ?: "24"
    val condDisplay = (weatherCondition?.ifBlank { "Limpo" } ?: "Limpo").replaceFirstChar { it.uppercase() }
    val isNightTime = remember(formattedTime) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        hour < 6 || hour >= 18
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF16161D).copy(alpha = 0.88f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isNightTime) Icons.Outlined.DarkMode else Icons.Outlined.WbSunny,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$tempDigits°",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "•",
                fontSize = 11.sp,
                color = Color(0xFF707078)
            )
            Text(
                text = condDisplay,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA0A0A8),
                maxLines = 1
            )
        }
    }
}

/**
 * Chip de Bateria Compacto e Translúcido do Modo Repouso.
 * Exibe ícone dinâmico (carregando/normal/baixa), percentual e acabamento de vidro idêntico ao chip de clima.
 */
@Composable
private fun StandByBatteryChip(
    batteryPercentage: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val clampedPercentage = batteryPercentage.coerceIn(0, 100)
    val isLowBattery = clampedPercentage <= 20 && !isCharging

    val batteryTint = when {
        isCharging -> Color(0xFF34D399) // Verde esmeralda brilhante
        isLowBattery -> Color(0xFFF87171) // Alerta de bateria fraca
        else -> Color.White
    }

    val borderColor = if (isCharging) {
        Color(0xFF34D399).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF16161D).copy(alpha = 0.88f))
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isCharging) {
                    Icons.Outlined.BatteryChargingFull
                } else {
                    Icons.Outlined.BatteryStd
                },
                contentDescription = if (isCharging) "Carregando" else "Bateria",
                tint = batteryTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$clampedPercentage%",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isLowBattery) batteryTint else Color.White
            )
        }
    }
}

/**
 * Cartão de Foto do Modo Repouso.
 * Mantém fixo o tamanho animado especificado (cardSize) com borda de vidro sutil.
 */
@Composable
private fun StandByPhotoCard(
    bitmap: Bitmap?,
    cardSize: Dp,
    modifier: Modifier = Modifier
) {
    val safeImageBitmap = remember(bitmap) {
        runCatching {
            bitmap?.takeIf { !it.isRecycled }?.asImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .size(cardSize)
            .clip(RoundedCornerShape(26.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF222228),
                        Color(0xFF141416),
                        Color(0xFF0C0C0E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (safeImageBitmap != null) {
            Image(
                bitmap = safeImageBitmap,
                contentDescription = "Foto em modo repouso",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Estética Minimalista Monocromática (Fallback quando nenhuma foto for escolhida)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Cartão da Capa do Álbum com Controles Rápidos de Reprodução em Padrão Monocromático.
 */
@Composable
private fun StandByAlbumCard(
    mediaPlayback: MediaPlaybackInfo,
    cardSize: Dp,
    onTogglePlayPause: () -> Unit,
    onOpenMusicApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val albumBitmap = remember(mediaPlayback.artwork) {
        runCatching {
            mediaPlayback.artwork?.takeIf { !it.isRecycled }?.asImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .size(cardSize)
            .clip(RoundedCornerShape(26.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(26.dp))
            .background(Color(0xFF14141A))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpenMusicApp
            ),
        contentAlignment = Alignment.Center
    ) {
        // Arte do Álbum ou Fallback Degradê Obsidiana
        if (albumBitmap != null) {
            Image(
                bitmap = albumBitmap,
                contentDescription = "Capa do álbum",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF24242A),
                                Color(0xFF141418),
                                Color(0xFF0A0A0D)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        // Gradiente escurecedor na parte inferior para contraste do texto
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0x33000000),
                            Color(0xEE000000)
                        )
                    )
                )
        )

        // Conteúdo da Faixa e Botão de Play/Pause
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Badge indicador de mídia no topo
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)), CircleShape)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (mediaPlayback.isPlaying) Color.White else Color(0xFF707078))
                    )
                    Text(
                        text = if (mediaPlayback.isPlaying) "TOCANDO" else "PAUSADO",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.White
                    )
                }
            }

            // Título, Artista e Botão de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = mediaPlayback.title.ifBlank { "Música" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mediaPlayback.artist.ifBlank { "Reproduzindo" },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFA0A0A8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Botão Play / Pause Flutuante em Branco Sólido com Ícone Preto de Alto Contraste
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
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
            }
        }
    }
}

/**
 * Relógio Mecânico Flip 3D (Estilo Placar / Split-Flap).
 * Possui animação elástica de escala quando a música toca ("diminuindo o relógio")
 * e animação física de dobra 3D em cada dígito ao virar o minuto/hora.
 */
@Composable
fun StandByFlipClock(
    formattedTime: String,
    isMusicActive: Boolean,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    // Dimensões animadas do relógio
    val targetWidth = when {
        isLandscape && !isMusicActive -> 68.dp
        isLandscape && isMusicActive -> 54.dp
        !isLandscape && !isMusicActive -> 64.dp
        else -> 50.dp
    }
    val cardWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "FlipCardWidth"
    )

    val targetHeight = when {
        isLandscape && !isMusicActive -> 100.dp
        isLandscape && isMusicActive -> 80.dp
        !isLandscape && !isMusicActive -> 94.dp
        else -> 74.dp
    }
    val cardHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "FlipCardHeight"
    )

    val targetFontSize = when {
        isLandscape && !isMusicActive -> 66f
        isLandscape && isMusicActive -> 52f
        !isLandscape && !isMusicActive -> 62f
        else -> 48f
    }
    val fontSizeSp by animateFloatAsState(
        targetValue = targetFontSize,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "FlipCardFontSize"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ColonPulse")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ColonAlpha"
    )

    // Separa formattedTime nas partes de Horas e Minutos (ex: "19:48" -> "19", "48")
    val parts = formattedTime.split(":")
    val hoursStr = (parts.getOrNull(0) ?: "12").padStart(2, '0')
    val minutesStr = (parts.getOrNull(1) ?: "00").padStart(2, '0')

    val h1 = hoursStr.getOrElse(0) { '1' }
    val h2 = hoursStr.getOrElse(1) { '2' }
    val m1 = minutesStr.getOrElse(0) { '0' }
    val m2 = minutesStr.getOrElse(1) { '0' }

    val digitSpacing = 6.dp
    val colonSpacing = 10.dp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Horas
        SplitFlapDigit(
            digit = h1,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            fontSize = fontSizeSp.sp,
            textColor = color
        )

        Spacer(modifier = Modifier.width(digitSpacing))

        SplitFlapDigit(
            digit = h2,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            fontSize = fontSizeSp.sp,
            textColor = color
        )

        // Separador ':' pulsante com dois círculos mecânicos
        Spacer(modifier = Modifier.width(colonSpacing))

        Column(
            verticalArrangement = Arrangement.spacedBy(cardHeight * 0.22f),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { alpha = colonAlpha }
        ) {
            Box(
                modifier = Modifier
                    .size(if (isMusicActive) 5.dp else 6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Box(
                modifier = Modifier
                    .size(if (isMusicActive) 5.dp else 6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.width(colonSpacing))

        // Minutos
        SplitFlapDigit(
            digit = m1,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            fontSize = fontSizeSp.sp,
            textColor = color
        )

        Spacer(modifier = Modifier.width(digitSpacing))

        SplitFlapDigit(
            digit = m2,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            fontSize = fontSizeSp.sp,
            textColor = color
        )
    }
}

/**
 * Dígito Individual com Efeito Físico de Placar Mecânico (Split-Flap 3D).
 * Renderiza um cartão unificado com linha de divisão mecânica central, chanfros nos pinos laterais,
 * iluminação física e rotação suave em 3D ao virar o minuto/hora sem duplicação de dígitos.
 */
@Composable
fun SplitFlapDigit(
    digit: Char,
    cardWidth: Dp,
    cardHeight: Dp,
    fontSize: TextUnit,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    var displayedDigit by remember { mutableStateOf(digit) }
    var upcomingDigit by remember { mutableStateOf(digit) }
    val flipProgress = remember { Animatable(0f) }
    val density = LocalDensity.current.density
    val cameraDist = 32f * density
    val cornerRadius = remember(cardWidth) { (cardWidth * 0.18f).coerceIn(8.dp, 14.dp) }

    LaunchedEffect(digit) {
        if (digit != displayedDigit && digit != upcomingDigit) {
            upcomingDigit = digit
            flipProgress.snapTo(0f)
            flipProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
            displayedDigit = upcomingDigit
            flipProgress.snapTo(0f)
        }
    }

    val isAnimating = flipProgress.value > 0f && flipProgress.value < 1f
    val currentDigit = if (isAnimating && flipProgress.value >= 0.5f) upcomingDigit else displayedDigit

    // Rotação 3D ao virar a placa mecânica
    val rotX = if (isAnimating) {
        val p = flipProgress.value
        if (p < 0.5f) {
            -90f * (p * 2f)
        } else {
            90f * (1f - (p - 0.5f) * 2f)
        }
    } else 0f

    val shadowAlpha = if (isAnimating) {
        val p = flipProgress.value
        (sin(p * Math.PI.toFloat()) * 0.40f).coerceIn(0f, 0.40f)
    } else 0f

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .graphicsLayer {
                rotationX = rotX
                cameraDistance = cameraDist
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF202026),
                        Color(0xFF16161A),
                        Color(0xFF0F0F12)
                    )
                )
            )
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Dígito Centralizado Único (Sem duplicações ou distorções)
        Text(
            text = currentDigit.toString(),
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = textColor,
                letterSpacing = (-1.5).sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )

        // Brilho suave da metade superior (Sheen / Acabamento físico)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Sombra suave na metade inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Linha de Costura / Divisão Mecânica Central (Split seam)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.Center)
                .background(Color(0xFF08080B))
        )
        // Destaque especular sutil na borda inferior da costura
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.6.dp)
                .align(Alignment.Center)
                .offset(y = 1.dp)
                .background(Color.White.copy(alpha = 0.07f))
        )

        // Chanfro mecânico lateral esquerdo (Axle notch)
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 6.dp)
                .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                .background(Color(0xFF060608))
                .align(Alignment.CenterStart)
        )

        // Chanfro mecânico lateral direito (Axle notch)
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 6.dp)
                .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                .background(Color(0xFF060608))
                .align(Alignment.CenterEnd)
        )

        // Sombra física dinâmica durante a rotação 3D
        if (shadowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = shadowAlpha))
            )
        }
    }
}
