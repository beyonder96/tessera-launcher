package com.tessera.launcher.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Modo Repouso (StandBy / Ambient Smart Display com Relógio Âmbar e Widgets Modulares).
 * Inspirado fielmente no design do StandBy Mode:
 * - Cabeçalho CONNECTED GEEK
 * - Relógio digital âmbar em negrito com odômetro suave
 * - Data estilizada (Dia da semana e Mês/Dia)
 * - Widget 1: Cartão de Clima com temperatura, condição, ícone lunar/solar e localização
 * - Widget 2: Cartão de Foto arredondado com efeito pôr do sol ou imagem customizada
 * - Rotação livre por sensor (orientação paisagem/cabeceira ou vertical)
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
    accentColor: Color = Color(0xFFFFA040)
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
                                standbyAmber.copy(alpha = 0.09f),
                                Color.Transparent
                            ),
                            radius = 1200f
                        )
                    )
            )

            // Botão Fechar discreto no topo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 28.dp, end = 24.dp),
                contentAlignment = Alignment.TopEnd
            ) {
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
                // Layout Paisagem Idêntico à Foto de Referência (iOS StandBy Smart Display)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Coluna da Esquerda: Header, Relógio Gigante Âmbar e Data
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "CONNECTED GEEK",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp,
                            color = standbyAmber.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        StandByAnimatedTimeClock(
                            formattedTime = formattedTime,
                            fontSize = 86.sp,
                            color = standbyAmber,
                            accentColor = standbyAmber,
                            showSecondsBar = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = dayOfWeek,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = standbyAmber
                        )
                        Text(
                            text = monthDay,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = standbyAmber.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    // Coluna da Direita: Os 2 Widgets Modulares (Clima e Foto)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandByWeatherCard(
                            weatherTemp = weatherTemp,
                            weatherCondition = weatherCondition,
                            weatherCity = weatherCity,
                            formattedTime = formattedTime,
                            standbyAmber = standbyAmber,
                            cardSize = 154.dp
                        )

                        StandByPhotoCard(
                            bitmap = bitmap,
                            cardSize = 154.dp
                        )
                    }
                }
            } else {
                // Layout Retrato (Vertical) com o mesmo padrão visual
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header, Relógio Âmbar e Data
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "CONNECTED GEEK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp,
                            color = standbyAmber.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        StandByAnimatedTimeClock(
                            formattedTime = formattedTime,
                            fontSize = 76.sp,
                            color = standbyAmber,
                            accentColor = standbyAmber,
                            showSecondsBar = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = dayOfWeek,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = standbyAmber
                        )
                        Text(
                            text = monthDay,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = standbyAmber.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Os 2 Widgets Modulares lado a lado
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandByWeatherCard(
                            weatherTemp = weatherTemp,
                            weatherCondition = weatherCondition,
                            weatherCity = weatherCity,
                            formattedTime = formattedTime,
                            standbyAmber = standbyAmber,
                            cardSize = 144.dp
                        )

                        StandByPhotoCard(
                            bitmap = bitmap,
                            cardSize = 144.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Toque em qualquer lugar para sair",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StandByWeatherCard(
    weatherTemp: String?,
    weatherCondition: String?,
    weatherCity: String?,
    formattedTime: String,
    standbyAmber: Color,
    cardSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val tempDigits = weatherTemp?.filter { it.isDigit() || it == '-' }?.ifEmpty { "62" } ?: "62"
    val condDisplay = (weatherCondition?.ifBlank { "clear" } ?: "clear").lowercase()
    val cityDisplay = (weatherCity?.ifBlank { "SAN FRANCISCO" } ?: "SAN FRANCISCO").uppercase()
    val isNightTime = remember(formattedTime) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        hour < 6 || hour >= 18
    }

    Box(
        modifier = modifier
            .size(cardSize)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF16161C))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(26.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Column {
                Text(
                    text = "$tempDigits°",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = condDisplay,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFA0A0A8)
                )
            }

            Icon(
                imageVector = if (isNightTime) Icons.Outlined.DarkMode else Icons.Outlined.WbSunny,
                contentDescription = null,
                tint = standbyAmber,
                modifier = Modifier.size(34.dp)
            )

            Text(
                text = cityDisplay,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFFA0A0A8),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StandByPhotoCard(
    bitmap: Bitmap?,
    cardSize: androidx.compose.ui.unit.Dp,
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
            // Estética Pôr do Sol inspirada na foto de referência
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE082).copy(alpha = 0.85f))
                        .align(Alignment.Center)
                )
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Relógio Digital Animado no Formato Tradicional (ex: 16:44).
 * Apresenta odômetro vertical elástico por dígito ao virar o minuto,
 * pulso contínuo nos dois pontos ':' a cada segundo e micro-barra luminosa de 60s.
 */
@Composable
fun StandByAnimatedTimeClock(
    formattedTime: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 82.sp,
    color: Color = Color(0xFFFFA040),
    accentColor: Color = Color(0xFFFFA040),
    showSecondsBar: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StandbyClockTransition")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ColonAlpha"
    )

    var secondFraction by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            val sec = cal.get(Calendar.SECOND)
            val ms = cal.get(Calendar.MILLISECOND)
            secondFraction = (sec * 1000f + ms) / 60000f
            delay(60)
        }
    }

    // Separa formattedTime nas partes de Horas e Minutos (ex: "16:44" -> "16", "44")
    val parts = formattedTime.split(":")
    val hoursPart = parts.getOrNull(0) ?: formattedTime
    val minutesPart = parts.getOrNull(1)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Dígitos da Hora
            hoursPart.forEachIndexed { index, char ->
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        (slideInVertically(animationSpec = tween(560, easing = FastOutSlowInEasing)) { height -> height * 3 / 4 } +
                                fadeIn(animationSpec = tween(400)) +
                                scaleIn(animationSpec = tween(560), initialScale = 0.88f)) togetherWith
                                (slideOutVertically(animationSpec = tween(560, easing = FastOutSlowInEasing)) { height -> -height * 3 / 4 } +
                                        fadeOut(animationSpec = tween(300)) +
                                        scaleOut(animationSpec = tween(560), targetScale = 0.88f))
                    },
                    label = "StandbyHourDigit_$index"
                ) { targetChar ->
                    Text(
                        text = targetChar.toString(),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = color,
                        letterSpacing = (-1.5).sp
                    )
                }
            }

            // Separador ':' com pulso suave contínuo a cada segundo
            if (minutesPart != null) {
                Text(
                    text = ":",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = color,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.graphicsLayer { alpha = colonAlpha }
                )

                // Dígitos do Minuto com Odômetro Vertical Elástico
                minutesPart.forEachIndexed { index, char ->
                    AnimatedContent(
                        targetState = char,
                        transitionSpec = {
                            (slideInVertically(animationSpec = tween(560, easing = FastOutSlowInEasing)) { height -> height * 3 / 4 } +
                                    fadeIn(animationSpec = tween(400)) +
                                    scaleIn(animationSpec = tween(560), initialScale = 0.88f)) togetherWith
                                    (slideOutVertically(animationSpec = tween(560, easing = FastOutSlowInEasing)) { height -> -height * 3 / 4 } +
                                            fadeOut(animationSpec = tween(300)) +
                                            scaleOut(animationSpec = tween(560), targetScale = 0.88f))
                        },
                        label = "StandbyMinuteDigit_$index"
                    ) { targetChar ->
                        Text(
                            text = targetChar.toString(),
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = color,
                            letterSpacing = (-1.5).sp
                        )
                    }
                }
            }
        }

        if (showSecondsBar) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(secondFraction.coerceIn(0.02f, 1f))
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.45f),
                                    accentColor
                                )
                            )
                        )
                )
            }
        }
    }
}
