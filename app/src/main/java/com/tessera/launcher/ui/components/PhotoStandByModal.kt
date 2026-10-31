package com.tessera.launcher.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    val standbyAmber = Color(0xFFFFA040)

    val now = remember(formattedTime) { Date() }
    val dayOfWeek = remember(now) {
        SimpleDateFormat("EEEE", Locale.getDefault()).format(now).replaceFirstChar { it.uppercase() }
    }
    val monthDay = remember(now) {
        SimpleDateFormat("MMMM d", Locale.getDefault()).format(now).replaceFirstChar { it.uppercase() }
    }

    val isMusicActive = mediaPlayback != null && (
        mediaPlayback.isPlaying || (mediaPlayback.hasActiveSession && mediaPlayback.title.isNotBlank())
    )

    // Ativa rotação automática por sensor enquanto o Modo Repouso estiver aberto
    DisposableEffect(isOpen) {
        val activity = context as? Activity
        if (isOpen && activity != null) {
            val previousOrientation = activity.requestedOrientation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            onDispose {
                activity.requestedOrientation = previousOrientation
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
                .background(Color(0xFF030305))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Brilho difuso ambiente suave com o tom âmbar de cabeceira ao fundo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                standbyAmber.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = 1200f
                        )
                    )
            )

            // Barra Superior de Controles e Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ícone do Tessera Launcher (canto superior esquerdo)
                StandByTesseraBadge(onDismiss = onDismiss)

                // Em modo paisagem, o chip de clima é apresentado elegantemente no topo
                if (isLandscape) {
                    StandByWeatherChip(
                        weatherTemp = weatherTemp,
                        weatherCondition = weatherCondition,
                        weatherCity = weatherCity,
                        formattedTime = formattedTime,
                        standbyAmber = standbyAmber
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Botão Fechar discreto (canto superior direito)
                Box(
                    modifier = Modifier
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
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar modo repouso",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (isLandscape) {
                // ==========================================
                // LAYOUT PAISAGEM (Horizontal)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Coluna da Esquerda: Relógio Flip e Data
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        StandByFlipClock(
                            formattedTime = formattedTime,
                            isMusicActive = isMusicActive,
                            isLandscape = true,
                            color = standbyAmber
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val dayFontSize = if (isMusicActive) 20.sp else 24.sp
                        val monthFontSize = if (isMusicActive) 17.sp else 20.sp

                        Text(
                            text = dayOfWeek,
                            fontSize = dayFontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = standbyAmber
                        )
                        Text(
                            text = monthDay,
                            fontSize = monthFontSize,
                            fontWeight = FontWeight.Normal,
                            color = standbyAmber.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Coluna da Direita: Capa do Álbum (se ativa) + Foto de Tamanho Fixo
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Capa do Álbum (aparece quando a música está ativa)
                        AnimatedVisibility(
                            visible = isMusicActive,
                            enter = fadeIn(tween(300)) + expandHorizontally(tween(350)),
                            exit = fadeOut(tween(250)) + shrinkHorizontally(tween(300))
                        ) {
                            if (mediaPlayback != null) {
                                StandByAlbumCard(
                                    mediaPlayback = mediaPlayback,
                                    cardSize = 154.dp,
                                    standbyAmber = standbyAmber,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onOpenMusicApp = onOpenMusicApp
                                )
                            }
                        }

                        // Cartão da Foto: mantém rigorosamente o mesmo tamanho (154dp)
                        StandByPhotoCard(
                            bitmap = bitmap,
                            cardSize = 154.dp
                        )
                    }
                }
            } else {
                // ==========================================
                // LAYOUT RETRATO (Vertical)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Chip de Clima Compacto no topo da coluna central
                    StandByWeatherChip(
                        weatherTemp = weatherTemp,
                        weatherCondition = weatherCondition,
                        weatherCity = weatherCity,
                        formattedTime = formattedTime,
                        standbyAmber = standbyAmber
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Relógio Flip Animado
                    StandByFlipClock(
                        formattedTime = formattedTime,
                        isMusicActive = isMusicActive,
                        isLandscape = false,
                        color = standbyAmber
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val dayFontSize = if (isMusicActive) 19.sp else 22.sp
                    val monthFontSize = if (isMusicActive) 16.sp else 19.sp

                    Text(
                        text = dayOfWeek,
                        fontSize = dayFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = standbyAmber
                    )
                    Text(
                        text = monthDay,
                        fontSize = monthFontSize,
                        fontWeight = FontWeight.Normal,
                        color = standbyAmber.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Linha de Cartões (Capa do Álbum e Foto de Tamanho Fixo)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = isMusicActive,
                            enter = fadeIn(tween(300)) + expandHorizontally(tween(350)),
                            exit = fadeOut(tween(250)) + shrinkHorizontally(tween(300))
                        ) {
                            if (mediaPlayback != null) {
                                StandByAlbumCard(
                                    mediaPlayback = mediaPlayback,
                                    cardSize = 144.dp,
                                    standbyAmber = standbyAmber,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onOpenMusicApp = onOpenMusicApp
                                )
                            }
                        }

                        // Cartão da Foto: mantém rigorosamente o mesmo tamanho (144dp)
                        StandByPhotoCard(
                            bitmap = bitmap,
                            cardSize = 144.dp
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
 * Ao tocar, fecha o Modo Repouso e retorna imediatamente ao início.
 */
@Composable
private fun StandByTesseraBadge(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher),
            contentDescription = "Tessera Launcher",
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        )
    }
}

/**
 * Chip de Clima Compacto e Translúcido.
 * Exibe ícone dinâmico lunar/solar, temperatura e condição resumida.
 */
@Composable
private fun StandByWeatherChip(
    weatherTemp: String?,
    weatherCondition: String?,
    weatherCity: String?,
    formattedTime: String,
    standbyAmber: Color,
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
                tint = standbyAmber,
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
 * Cartão de Foto do Modo Repouso.
 * Mantém fixo o tamanho especificado (cardSize) independente do estado de reprodução.
 */
@Composable
private fun StandByPhotoCard(
    bitmap: Bitmap?,
    cardSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(cardSize)
            .clip(RoundedCornerShape(26.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF2D150B),
                        Color(0xFF8B3A0E),
                        Color(0xFFD4621A),
                        Color(0xFFFAA43A),
                        Color(0xFF331606)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto em modo repouso",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Estética Pôr do Sol inspirada na referência
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE082).copy(alpha = 0.85f))
                        .align(Alignment.Center)
                )
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Cartão da Capa do Álbum com Controles Rápidos de Reprodução.
 * Mesma dimensão do cartão de foto (154dp em paisagem / 144dp em retrato).
 */
@Composable
private fun StandByAlbumCard(
    mediaPlayback: MediaPlaybackInfo,
    cardSize: Dp,
    standbyAmber: Color,
    onTogglePlayPause: () -> Unit,
    onOpenMusicApp: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        // Arte do Álbum ou Fallback Degradê
        if (mediaPlayback.artwork != null) {
            Image(
                bitmap = mediaPlayback.artwork.asImageBitmap(),
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
                                Color(0xFF2E243D),
                                Color(0xFF181520),
                                Color(0xFF0C0A10)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    tint = standbyAmber.copy(alpha = 0.6f),
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
                            Color(0xDD000000)
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
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (mediaPlayback.isPlaying) standbyAmber else Color(0xFF8E8E93))
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

                // Botão Play / Pause Flutuante
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(standbyAmber)
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
    color: Color = Color(0xFFFFA040)
) {
    // Dimensões animadas do relógio
    val targetWidth = when {
        isLandscape && !isMusicActive -> 66.dp
        isLandscape && isMusicActive -> 48.dp
        !isLandscape && !isMusicActive -> 62.dp
        else -> 46.dp
    }
    val cardWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "FlipCardWidth"
    )

    val targetHeight = when {
        isLandscape && !isMusicActive -> 104.dp
        isLandscape && isMusicActive -> 74.dp
        !isLandscape && !isMusicActive -> 96.dp
        else -> 70.dp
    }
    val cardHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "FlipCardHeight"
    )

    val targetFontSize = when {
        isLandscape && !isMusicActive -> 68f
        isLandscape && isMusicActive -> 46f
        !isLandscape && !isMusicActive -> 62f
        else -> 42f
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

    val digitSpacing = cardWidth * 0.08f
    val colonSpacing = cardWidth * 0.14f

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

        // Separador ':' pulsante
        Spacer(modifier = Modifier.width(colonSpacing))

        Text(
            text = ":",
            fontSize = (fontSizeSp * 0.8f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = color,
            modifier = Modifier.graphicsLayer { alpha = colonAlpha }
        )

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
 * Divide o número em metades superior e inferior com rotação em 3D,
 * linha de divisão realista e sombreamento progressivo.
 */
@Composable
fun SplitFlapDigit(
    digit: Char,
    cardWidth: Dp,
    cardHeight: Dp,
    fontSize: TextUnit,
    textColor: Color = Color(0xFFFFA040),
    cardColor: Color = Color(0xFF141418),
    modifier: Modifier = Modifier
) {
    var displayedDigit by remember { mutableStateOf(digit) }
    var upcomingDigit by remember { mutableStateOf(digit) }
    val flipProgress = remember { Animatable(0f) }
    val density = LocalDensity.current.density
    val cameraDist = 28f * density
    val cornerRadius = remember(cardWidth) { (cardWidth * 0.16f).coerceIn(6.dp, 12.dp) }

    LaunchedEffect(digit) {
        if (digit != displayedDigit && digit != upcomingDigit) {
            upcomingDigit = digit
            flipProgress.snapTo(0f)
            flipProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 540, easing = FastOutSlowInEasing)
            )
            displayedDigit = upcomingDigit
            flipProgress.snapTo(0f)
        }
    }

    val isAnimating = flipProgress.value > 0f && flipProgress.value < 1f
    val currentTopDigit = if (isAnimating) upcomingDigit else displayedDigit
    val currentBottomDigit = if (isAnimating && flipProgress.value >= 0.5f) upcomingDigit else displayedDigit
    val oldTopDigit = displayedDigit
    val newBottomDigit = upcomingDigit

    Box(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
    ) {
        // 1. Base Estática (Metade Superior e Metade Inferior)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HalfDigitSurface(
                digit = currentTopDigit,
                isTop = true,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                fontSize = fontSize,
                textColor = textColor,
                cardColor = cardColor,
                cornerRadius = cornerRadius
            )

            HalfDigitSurface(
                digit = currentBottomDigit,
                isTop = false,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                fontSize = fontSize,
                textColor = textColor,
                cardColor = cardColor,
                cornerRadius = cornerRadius
            )
        }

        // 2. Aba 3D Dinâmica em Movimento
        if (isAnimating) {
            val p = flipProgress.value
            if (p < 0.5f) {
                // Aba Superior Dobrando para Baixo (revelando o novo número por trás)
                val rotX = -180f * p
                Box(
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight / 2)
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            rotationX = rotX
                            cameraDistance = cameraDist
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                ) {
                    HalfDigitSurface(
                        digit = oldTopDigit,
                        isTop = true,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        fontSize = fontSize,
                        textColor = textColor,
                        cardColor = cardColor,
                        cornerRadius = cornerRadius
                    )
                    // Sombra da dobra superior
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                            .background(Color.Black.copy(alpha = (p * 1.5f).coerceIn(0f, 0.65f)))
                    )
                }
            } else {
                // Aba Inferior Caindo para a Frente (pousando sobre a base)
                val rotX = 180f * (1f - p)
                Box(
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight / 2)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            rotationX = rotX
                            cameraDistance = cameraDist
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        }
                ) {
                    HalfDigitSurface(
                        digit = newBottomDigit,
                        isTop = false,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        fontSize = fontSize,
                        textColor = textColor,
                        cardColor = cardColor,
                        cornerRadius = cornerRadius
                    )
                    // Sombra diminuindo ao pousar
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
                            .background(Color.Black.copy(alpha = ((1f - p) * 1.5f).coerceIn(0f, 0.65f)))
                    )
                }
            }
        }

        // 3. Linha de Costura / Divisor Mecânico Central e Chanfros Laterais
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.Center)
                .background(Color(0xFF070709))
        )
        // Chanfro esquerdo
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 5.dp)
                .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                .background(Color(0xFF040406))
                .align(Alignment.CenterStart)
        )
        // Chanfro direito
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 5.dp)
                .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                .background(Color(0xFF040406))
                .align(Alignment.CenterEnd)
        )
    }
}

/**
 * Superfície de Meio Dígito (Superior ou Inferior) com corte milimétrico do texto.
 */
@Composable
private fun HalfDigitSurface(
    digit: Char,
    isTop: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    fontSize: TextUnit,
    textColor: Color,
    cardColor: Color,
    cornerRadius: Dp,
    modifier: Modifier = Modifier
) {
    val halfHeight = (cardHeight - 2.dp) / 2
    val shape = if (isTop) {
        RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
    } else {
        RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
    }

    Box(
        modifier = modifier
            .width(cardWidth)
            .height(halfHeight)
            .clip(shape)
            .background(cardColor)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), shape)
            .clipToBounds(),
        contentAlignment = if (isTop) Alignment.TopCenter else Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = digit.toString(),
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = textColor,
                letterSpacing = (-1.5).sp
            )
        }
    }
}
