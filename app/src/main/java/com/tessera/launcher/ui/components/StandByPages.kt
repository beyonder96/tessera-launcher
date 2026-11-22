package com.tessera.launcher.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioManager
import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.bridge.TesseraCoreBridgeClient
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.service.MediaPlaybackInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Immutable
private data class StandByQuote(val text: String, val author: String)

private val STANDBY_QUOTES_PT = listOf(
    StandByQuote("O futuro depende do que você faz hoje.", "Mahatma Gandhi"),
    StandByQuote("A simplicidade é o último grau de sofisticação.", "Leonardo da Vinci"),
    StandByQuote("Tudo posso naquele que me fortalece.", "Filipenses 4:13"),
    StandByQuote("Comece onde você está, use o que você tem, faça o que você pode.", "Arthur Ashe"),
    StandByQuote("A persistência é o caminho do êxito.", "Charles Chaplin"),
    StandByQuote("Lâmpada para os meus pés é a tua palavra e luz para o meu caminho.", "Salmos 119:105"),
    StandByQuote("A disciplina é a ponte entre metas e realizações.", "Jim Rohn")
)

@Immutable
private data class PastelClockPalette(
    val h1: Color,
    val h2: Color,
    val m1: Color,
    val m2: Color,
    val colon: Color
)

private val PASTEL_CLOCK_PALETTES = listOf(
    // Paleta exata do print 3: Rosa pastel, Rosa suave, Amarelo limão pastel, Lilás pastel
    PastelClockPalette(
        h1 = Color(0xFFFFD3DD),
        h2 = Color(0xFFFFC1D1),
        m1 = Color(0xFFFFF4A4),
        m2 = Color(0xFFD6C9F2),
        colon = Color(0xCCEEF0F5)
    ),
    // Pôr do sol quente
    PastelClockPalette(
        h1 = Color(0xFFFFC6AA),
        h2 = Color(0xFFFFAD99),
        m1 = Color(0xFFFFE59D),
        m2 = Color(0xFFF6D4C2),
        colon = Color(0xCCF5EEE8)
    ),
    // Oceano e menta
    PastelClockPalette(
        h1 = Color(0xFFBCE6FF),
        h2 = Color(0xFFA2DBFA),
        m1 = Color(0xFFB5F4D8),
        m2 = Color(0xFFD6CEFF),
        colon = Color(0xCCEAF5FA)
    ),
    // Creme minimalista
    PastelClockPalette(
        h1 = Color(0xFFF5ECE3),
        h2 = Color(0xFFE7DACD),
        m1 = Color(0xFFD9C8B8),
        m2 = Color(0xFFCBB8A6),
        colon = Color(0xCCF0E8E0)
    )
)

// ============================================================================
// TELA 1: RELÓGIO GIGANTE CONDENSADO + DATA E PÍLULA DE CLIMA
// Referência: Screenshot_20261122-000017_StandBy.png
// ============================================================================
@Composable
internal fun StandByTallClockPage(
    isLandscape: Boolean,
    formattedTime: String,
    weatherTemp: String?,
    weatherCondition: String?,
    accentColor: Color,
    activeColorPreset: StandByColorPreset,
    onCycleColorPreset: () -> Unit
) {
    val digitColor = remember(activeColorPreset, accentColor) {
        if (activeColorPreset == StandByColorPreset.WHITE) {
            Color(0xFFD6C3B3)
        } else {
            activeColorPreset.resolveColor(accentColor)
        }
    }

    val now = remember(formattedTime) { Date() }
    val weekdayShort = remember(now) { formatShortWeekdayPt(now) }
    val dayOfMonth = remember(now) {
        SimpleDateFormat("d", Locale.getDefault()).format(now)
    }

    val cleanTemp = remember(weatherTemp) {
        weatherTemp?.filter { it.isDigit() || it == '-' }?.takeIf { it.isNotEmpty() }?.let { "$it°" }
    }
    val hasWeather = cleanTemp != null

    val weatherPill: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF070707))
                .border(BorderStroke(1.dp, Color(0xFF1E1E1E)), CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasWeather) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = resolveWeatherIcon(weatherCondition),
                        contentDescription = null,
                        tint = digitColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                    val condText = weatherCondition?.takeIf { it.isNotBlank() }
                        ?.replaceFirstChar { it.uppercase() }
                    Text(
                        text = if (condText != null) "$cleanTemp • $condText" else cleanTemp.orEmpty(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA89F98),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "Desbloquear clima",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B6763),
                    maxLines = 1
                )
            }
        }
    }

    val dateHeader: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = weekdayShort,
                fontSize = if (isLandscape) 29.sp else 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
                color = digitColor
            )
            Text(
                text = dayOfMonth,
                fontSize = if (isLandscape) 29.sp else 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
                color = Color(0xFFF6EFE8)
            )
        }
    }

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Relógio gigante ocupando ~74% da largura
            Box(
                modifier = Modifier
                    .weight(0.74f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCycleColorPreset
                    ),
                contentAlignment = Alignment.Center
            ) {
                TallCapsuleClock(
                    formattedTime = formattedTime,
                    color = digitColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.80f)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Data + pílula de clima alinhadas ao topo dos dígitos à direita
            Column(
                modifier = Modifier
                    .weight(0.26f)
                    .fillMaxHeight(0.80f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                dateHeader()
                Spacer(modifier = Modifier.height(18.dp))
                weatherPill()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            dateHeader()
            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCycleColorPreset
                    ),
                contentAlignment = Alignment.Center
            ) {
                TallCapsuleClock(
                    formattedTime = formattedTime,
                    color = digitColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            weatherPill()
        }
    }
}

/**
 * Desenho vetorial de alta precisão dos dígitos condensados alongados estilo cápsula (00:00)
 * com espessura de traço uniforme, fiel ao print 1.
 */
@Composable
private fun TallCapsuleClock(
    formattedTime: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val parts = remember(formattedTime) {
        val split = formattedTime.split(":")
        val hh = (split.getOrNull(0) ?: "00").filter { it.isDigit() }.padStart(2, '0').take(2)
        val mm = (split.getOrNull(1) ?: "00").filter { it.isDigit() }.padStart(2, '0').take(2)
        listOf(hh[0], hh[1], mm[0], mm[1])
    }

    Canvas(modifier = modifier) {
        val totalW = size.width
        val totalH = size.height
        if (totalW <= 0f || totalH <= 0f) return@Canvas

        // Proporção fiel ao print: 4 dígitos altos (aspect ~0.46) + 2 gaps internos + espaço central para ":"
        val digitAspect = 0.48f
        val innerGapFactor = 0.16f
        val colonColFactor = 0.36f
        val unitsWidth = 4f + 2f * innerGapFactor + colonColFactor

        val digitWFromWidth = totalW / unitsWidth
        val digitWFromHeight = totalH * digitAspect
        val digitW = min(digitWFromWidth, digitWFromHeight)
        val digitH = digitW / digitAspect

        val innerGap = digitW * innerGapFactor
        val colonColW = digitW * colonColFactor
        val contentW = 4f * digitW + 2f * innerGap + colonColW

        val startX = (totalW - contentW) / 2f
        val startY = (totalH - digitH) / 2f
        val strokeW = digitW * 0.255f

        val x0 = startX
        val x1 = x0 + digitW + innerGap
        val colonCenterX = x1 + digitW + colonColW / 2f
        val x2 = x1 + digitW + colonColW
        val x3 = x2 + digitW + innerGap

        drawTallCapsuleDigit(parts[0], x0, startY, digitW, digitH, strokeW, color)
        drawTallCapsuleDigit(parts[1], x1, startY, digitW, digitH, strokeW, color)

        // Dois pontos circulares sólidos ao centro (alinhados como no print 1)
        val dotRadius = strokeW * 0.54f
        drawCircle(
            color = color,
            radius = dotRadius,
            center = Offset(colonCenterX, startY + digitH * 0.38f)
        )
        drawCircle(
            color = color,
            radius = dotRadius,
            center = Offset(colonCenterX, startY + digitH * 0.87f)
        )

        drawTallCapsuleDigit(parts[2], x2, startY, digitW, digitH, strokeW, color)
        drawTallCapsuleDigit(parts[3], x3, startY, digitW, digitH, strokeW, color)
    }
}

private fun DrawScope.drawTallCapsuleDigit(
    digit: Char,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    strokeW: Float,
    color: Color
) {
    val halfS = strokeW / 2f
    val l = x + halfS
    val t = y + halfS
    val r = x + w - halfS
    val b = y + h - halfS
    val rw = (r - l).coerceAtLeast(1f)
    val rh = (b - t).coerceAtLeast(1f)
    val midY = t + rh * 0.50f
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (digit) {
        '0' -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(l, t),
                size = Size(rw, rh),
                cornerRadius = CornerRadius(rw / 2f, rw / 2f),
                style = stroke
            )
        }

        '1' -> {
            val stemX = l + rw * 0.62f
            val path = Path().apply {
                moveTo(l + rw * 0.14f, t + rh * 0.15f)
                lineTo(stemX, t)
                lineTo(stemX, b)
            }
            drawPath(path, color = color, style = stroke)
        }

        '2' -> {
            val topArcH = rw * 0.96f
            val path = Path().apply {
                arcTo(
                    rect = Rect(l, t, r, t + topArcH),
                    startAngleDegrees = 195f,
                    sweepAngleDegrees = 175f,
                    forceMoveTo = true
                )
                cubicTo(
                    r, t + topArcH * 0.95f,
                    l + rw * 0.18f, t + rh * 0.68f,
                    l, b
                )
                lineTo(r, b)
            }
            drawPath(path, color = color, style = stroke)
        }

        '3' -> {
            val topH = rh * 0.49f
            val botH = rh * 0.51f
            val topPath = Path().apply {
                arcTo(
                    rect = Rect(l + rw * 0.04f, t, r, t + topH),
                    startAngleDegrees = 205f,
                    sweepAngleDegrees = 245f,
                    forceMoveTo = true
                )
            }
            val botPath = Path().apply {
                arcTo(
                    rect = Rect(l, t + topH, r, t + topH + botH),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 245f,
                    forceMoveTo = true
                )
            }
            drawPath(topPath, color = color, style = stroke)
            drawPath(botPath, color = color, style = stroke)
        }

        '4' -> {
            val stemX = l + rw * 0.78f
            val barY = t + rh * 0.66f
            val path = Path().apply {
                moveTo(stemX, b)
                lineTo(stemX, t)
                moveTo(l + rw * 0.72f, t + rh * 0.02f)
                lineTo(l, barY)
                lineTo(r, barY)
            }
            drawPath(path, color = color, style = stroke)
        }

        '5' -> {
            val bellyTop = t + rh * 0.43f
            val path = Path().apply {
                moveTo(r - rw * 0.04f, t)
                lineTo(l + rw * 0.06f, t)
                lineTo(l + rw * 0.06f, bellyTop + rh * 0.04f)
                arcTo(
                    rect = Rect(l, bellyTop, r, b),
                    startAngleDegrees = 215f,
                    sweepAngleDegrees = 295f,
                    forceMoveTo = false
                )
            }
            drawPath(path, color = color, style = stroke)
        }

        '6' -> {
            val loopTop = t + rh * 0.44f
            drawRoundRect(
                color = color,
                topLeft = Offset(l, loopTop),
                size = Size(rw, b - loopTop),
                cornerRadius = CornerRadius(rw / 2f, rw / 2f),
                style = stroke
            )
            val topPath = Path().apply {
                moveTo(l, loopTop + rw * 0.45f)
                lineTo(l, t + rw * 0.50f)
                arcTo(
                    rect = Rect(l, t, r, t + rw),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 155f,
                    forceMoveTo = false
                )
            }
            drawPath(topPath, color = color, style = stroke)
        }

        '7' -> {
            val path = Path().apply {
                moveTo(l, t)
                lineTo(r, t)
                cubicTo(
                    r - rw * 0.08f, t + rh * 0.28f,
                    l + rw * 0.42f, t + rh * 0.64f,
                    l + rw * 0.30f, b
                )
            }
            drawPath(path, color = color, style = stroke)
        }

        '8' -> {
            val topInset = rw * 0.04f
            drawRoundRect(
                color = color,
                topLeft = Offset(l + topInset, t),
                size = Size(rw - topInset * 2f, midY - t),
                cornerRadius = CornerRadius((rw - topInset * 2f) / 2f, (rw - topInset * 2f) / 2f),
                style = stroke
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(l, midY),
                size = Size(rw, b - midY),
                cornerRadius = CornerRadius(rw / 2f, rw / 2f),
                style = stroke
            )
        }

        '9' -> {
            val loopBottom = t + rh * 0.56f
            drawRoundRect(
                color = color,
                topLeft = Offset(l, t),
                size = Size(rw, loopBottom - t),
                cornerRadius = CornerRadius(rw / 2f, rw / 2f),
                style = stroke
            )
            val tailPath = Path().apply {
                moveTo(r, loopBottom - rw * 0.45f)
                lineTo(r, b - rw * 0.50f)
                arcTo(
                    rect = Rect(l, b - rw, r, b),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 155f,
                    forceMoveTo = false
                )
            }
            drawPath(tailPath, color = color, style = stroke)
        }
    }
}

// ============================================================================
// TELA 2: DASHBOARD INTELIGENTE (SAUDAÇÃO, RELÓGIO, FRASE, LUZ SOLAR + 4 CARDS)
// Referência: Screenshot_20261122-000203_StandBy.png
// ============================================================================
@Composable
internal fun StandByDashboardPage(
    isLandscape: Boolean,
    formattedTime: String,
    weatherTemp: String?,
    weatherCondition: String?,
    batteryPercentage: Int,
    isCharging: Boolean,
    mediaPlayback: MediaPlaybackInfo?,
    upcomingEvents: List<CalendarEventInfo>,
    onTogglePlayPause: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onOpenCalendarApp: () -> Unit
) {
    val context = LocalContext.current
    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }

    val nowCal = remember(formattedTime) { Calendar.getInstance() }
    val hourOfDay = nowCal.get(Calendar.HOUR_OF_DAY)
    val minuteOfHour = nowCal.get(Calendar.MINUTE)
    val isDaytime = hourOfDay in 6..17

    val greeting = remember(hourOfDay) {
        when (hourOfDay) {
            in 5..11 -> "Bom dia"
            in 12..17 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    val clockDisplay = remember(formattedTime, is24Hour, hourOfDay, minuteOfHour) {
        if (is24Hour) {
            formattedTime to ""
        } else {
            val h12 = when (val h = hourOfDay % 12) {
                0 -> 12
                else -> h
            }
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            String.format(Locale.getDefault(), "%d:%02d", h12, minuteOfHour) to amPm
        }
    }

    val shortDateLabel = remember(formattedTime) {
        val locale = Locale.forLanguageTag("pt-BR")
        val raw = SimpleDateFormat("EEE, d MMM", locale).format(nowCal.time)
        raw.replace(".", "")
            .split(" ")
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase(locale) } }
    }

    // Frase/versículo do dia: prioriza ecossistema Tessera se sincronizado, senão usa coleção rotativa PT-BR
    val ecosystemSnapshot by TesseraCoreBridgeClient.snapshot.collectAsState()
    var quoteIndex by remember { mutableIntStateOf(nowCal.get(Calendar.DAY_OF_YEAR) % STANDBY_QUOTES_PT.size) }
    val activeQuote = remember(ecosystemSnapshot, quoteIndex) {
        val ecoText = ecosystemSnapshot?.dailyVerseText?.takeIf { ecosystemSnapshot?.hasSyncedData == true && it.isNotBlank() }
        if (ecoText != null) {
            StandByQuote(ecoText, ecosystemSnapshot?.dailyVerseRef ?: "Tessera")
        } else {
            STANDBY_QUOTES_PT[quoteIndex % STANDBY_QUOTES_PT.size]
        }
    }

    // Cálculo do progresso solar e luz solar restante
    val totalMinutesOfDay = hourOfDay * 60 + minuteOfHour
    val sunProgress = (totalMinutesOfDay / 1440f).coerceIn(0.02f, 0.98f)
    val daylightLabel = remember(totalMinutesOfDay) {
        val sunriseMin = 6 * 60
        val sunsetMin = 18 * 60
        if (totalMinutesOfDay in sunriseMin until sunsetMin) {
            val rem = sunsetMin - totalMinutesOfDay
            val h = rem / 60
            val m = rem % 60
            "${h}h ${m}min de luz solar restante"
        } else {
            val minsUntilSunrise = if (totalMinutesOfDay < sunriseMin) {
                sunriseMin - totalMinutesOfDay
            } else {
                (24 * 60 - totalMinutesOfDay) + sunriseMin
            }
            val h = minsUntilSunrise / 60
            val m = minsUntilSunrise % 60
            "Nascer do sol em ${h}h ${m}min"
        }
    }

    val nextEvent = upcomingEvents.firstOrNull()
    val albumBitmap = remember(mediaPlayback?.artwork) {
        runCatching {
            mediaPlayback?.artwork?.takeIf { !it.isRecycled }?.asImageBitmap()
        }.getOrNull()
    }
    val hasMusic = mediaPlayback != null && (mediaPlayback.isPlaying || (mediaPlayback.hasActiveSession && mediaPlayback.title.isNotBlank()))

    val heroCard: @Composable (Modifier) -> Unit = { mod ->
        Box(
            modifier = mod
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF231F48),
                            Color(0xFF1A1632),
                            Color(0xFF291A3C)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f)
                    )
                )
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(28.dp))
        ) {
            // Brilho diagonal sutil atmosférico igual ao print 2
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF5643A8).copy(alpha = 0.22f),
                                Color.Transparent
                            ),
                            center = Offset(120f, 180f),
                            radius = 650f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Topo: Saudação + Relógio + Data
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDaytime) Icons.Outlined.WbSunny else Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = Color(0xFFC8BEFF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = greeting,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC8BEFF)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = clockDisplay.first,
                            style = TextStyle(
                                fontSize = if (isLandscape) 72.sp else 64.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-2.5).sp,
                                color = Color.White,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        if (clockDisplay.second.isNotEmpty()) {
                            Text(
                                text = clockDisplay.second,
                                fontSize = if (isLandscape) 24.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB5AECC),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = shortDateLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFC5BBD9)
                    )
                }

                // Meio: Bloco de Frase/Versículo com barra vertical lavanda
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { quoteIndex += 1 }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(if (isLandscape) 72.dp else 64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9A8AC8))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = activeQuote.text,
                            fontSize = if (isLandscape) 18.sp else 16.sp,
                            lineHeight = if (isLandscape) 24.sp else 22.sp,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFF3EFFA),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "— ${activeQuote.author}",
                            fontSize = 12.5.sp,
                            color = Color(0xFFB3A7CF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Base: Barra de Horizonte Solar + texto de luz solar restante
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SolarHorizonProgressBar(
                        progress = sunProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                    )
                    Text(
                        text = daylightLabel,
                        fontSize = 12.sp,
                        color = Color(0xFFA098B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    val cardsList: @Composable (Modifier) -> Unit = { mod ->
        val cleanTemp = weatherTemp?.filter { it.isDigit() || it == '-' }?.takeIf { it.isNotEmpty() }?.let { "$it°" } ?: "--°"
        val cleanCond = weatherCondition?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() } ?: "Clima indisponível"

        Column(
            modifier = mod,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Clima
            DashboardInfoCard(
                modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth().height(74.dp),
                leading = {
                    DashboardCircleIconBadge(
                        icon = resolveWeatherIcon(weatherCondition),
                        background = Color(0xFF232727),
                        tint = Color(0xFFA3A8A8)
                    )
                },
                title = cleanTemp,
                titleSizeSp = 22f,
                subtitle = cleanCond
            )

            // Card 2: Próximo Evento
            val eventTitle = nextEvent?.title ?: "Agenda livre"
            val eventSub = remember(nextEvent, formattedTime) {
                formatDashboardEventSubtitle(nextEvent)
            }
            DashboardInfoCard(
                modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth().height(74.dp),
                onClick = onOpenCalendarApp,
                leading = {
                    DashboardCircleIconBadge(
                        icon = Icons.Outlined.CalendarToday,
                        background = Color(0xFF212836),
                        tint = Color(0xFF9EB7F2)
                    )
                },
                title = eventTitle,
                titleSizeSp = 17f,
                subtitle = eventSub
            )

            // Card 3: Mídia Tocando
            DashboardInfoCard(
                modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth().height(74.dp),
                onClick = onOpenMusicApp,
                leading = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF232727)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (albumBitmap != null) {
                            Image(
                                bitmap = albumBitmap,
                                contentDescription = "Capa do álbum",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFFA8B0B2),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                title = if (hasMusic) mediaPlayback.title else "Sem mídia ativa",
                titleSizeSp = 17.5f,
                subtitle = if (hasMusic) mediaPlayback.artist.ifBlank { mediaPlayback.appDisplayName } else "Toque para abrir o player",
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = if (hasMusic) onTogglePlayPause else onOpenMusicApp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (mediaPlayback?.isPlaying == true) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color(0xFFDCE5E8),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )

            // Card 4: Bateria
            DashboardInfoCard(
                modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth().height(74.dp),
                leading = {
                    DashboardCircleIconBadge(
                        icon = if (isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                        background = Color(0xFF232727),
                        tint = if (isCharging) Color(0xFF8BE2BC) else Color(0xFFDCE5E8)
                    )
                },
                title = "${batteryPercentage.coerceIn(0, 100)}%",
                titleSizeSp = 21f,
                subtitle = if (isCharging) "Carregando" else "Na bateria"
            )
        }
    }

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 22.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            heroCard(Modifier.weight(1.06f).fillMaxHeight())
            cardsList(Modifier.weight(0.94f).fillMaxHeight())
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            heroCard(Modifier.fillMaxWidth().height(340.dp))
            cardsList(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SolarHorizonProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val barH = size.height * 0.80f
        val barTop = (size.height - barH) / 2f
        val corner = CornerRadius(barH / 2f, barH / 2f)

        val skyBrush = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF1D1B3F),
                0.16f to Color(0xFF262A56),
                0.26f to Color(0xFFE59D60),
                0.39f to Color(0xFF9EA9F6),
                0.50f to Color(0xFFC5CDFF),
                0.63f to Color(0xFF8D99EE),
                0.78f to Color(0xFFE19A61),
                0.88f to Color(0xFF262A56),
                1.00f to Color(0xFF1D1B3F)
            )
        )

        drawRoundRect(
            brush = skyBrush,
            topLeft = Offset(0f, barTop),
            size = Size(size.width, barH),
            cornerRadius = corner
        )

        val sunX = (size.width * progress).coerceIn(barH / 2f, size.width - barH / 2f)
        val sunY = size.height / 2f
        val sunR = barH * 0.56f

        // Halo solar sutil
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x88FFE074), Color.Transparent),
                center = Offset(sunX, sunY),
                radius = sunR * 2.0f
            ),
            radius = sunR * 2.0f,
            center = Offset(sunX, sunY)
        )

        // Disco solar amarelo brilhante
        drawCircle(
            color = Color(0xFFFFD864),
            radius = sunR,
            center = Offset(sunX, sunY)
        )
    }
}

@Composable
private fun DashboardCircleIconBadge(
    icon: ImageVector,
    background: Color,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun DashboardInfoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit,
    title: String,
    titleSizeSp: Float,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null
) {
    val clickMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF101212))
            .border(BorderStroke(1.dp, Color(0xFF252727)), RoundedCornerShape(22.dp))
            .then(clickMod)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        leading()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = titleSizeSp.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF3F4F4),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF8E9393),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing?.invoke()
    }
}

// ============================================================================
// TELA 3: RELÓGIO PASTEL SOBREPOSTO + CARD DE MÚSICA COM CAPA AO FUNDO
// Referência: Screenshot_20261122-000259_StandBy.png
// ============================================================================
@Composable
internal fun StandByPastelMusicPage(
    isLandscape: Boolean,
    formattedTime: String,
    mediaPlayback: MediaPlaybackInfo?,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onOpenMusicApp: () -> Unit
) {
    val context = LocalContext.current
    var paletteIndex by remember { mutableIntStateOf(0) }
    val palette = PASTEL_CLOCK_PALETTES[paletteIndex % PASTEL_CLOCK_PALETTES.size]

    val isMusicActive = mediaPlayback != null && (
        mediaPlayback.isPlaying || (mediaPlayback.hasActiveSession && mediaPlayback.title.isNotBlank())
    )
    var calculatedPos by remember { mutableLongStateOf(mediaPlayback?.getCalculatedPositionMs() ?: 0L) }
    LaunchedEffect(mediaPlayback, isMusicActive) {
        while (true) {
            calculatedPos = mediaPlayback?.getCalculatedPositionMs() ?: 0L
            if (mediaPlayback?.isPlaying != true) break
            delay(500L)
        }
    }

    val albumBitmap = remember(mediaPlayback?.artwork) {
        runCatching {
            mediaPlayback?.artwork?.takeIf { !it.isRecycled }?.asImageBitmap()
        }.getOrNull()
    }

    val musicBackdropCard: @Composable (Modifier) -> Unit = { mod ->
        Box(
            modifier = mod
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF25201D))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenMusicApp
                )
        ) {
            // Capa do álbum preenchendo todo o fundo do card
            if (albumBitmap != null) {
                Image(
                    bitmap = albumBitmap,
                    contentDescription = "Capa do álbum",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xAA2A2420),
                                    Color(0x991F1A17),
                                    Color(0xCC1A1614)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3A312B), Color(0xFF211C19), Color(0xFF161311))
                            )
                        )
                )
            }

            // Badge circular superior direito (ícone de fila + micro-badge)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.52f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenMusicApp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Abrir fila",
                        tint = Color(0xFFF3ECE6),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }

            // Conteúdo central de controles de música
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Título e Artista
                Column(
                    modifier = Modifier.padding(top = 12.dp, start = 10.dp, end = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isMusicActive) mediaPlayback.title else "Nenhuma mídia ativa",
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8F0E8),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isMusicActive) mediaPlayback.artist else "Toque para abrir o player",
                        fontSize = 14.5.sp,
                        color = Color(0xFFD5CCC5),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controles Principais << ▶ >>
                StandByMusicTransportRow(
                    isPlaying = mediaPlayback?.isPlaying == true,
                    isMusicActive = isMusicActive,
                    buttonSize = 42.dp,
                    playSize = 52.dp,
                    spacing = 30.dp,
                    tint = Color(0xFFFFF4EC),
                    onSkipPrevious = onSkipPrevious,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipNext = onSkipNext,
                    onOpenMusicApp = onOpenMusicApp
                )

                // Seekbar + Tempos + Ações Secundárias + Rodapé
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StandByMusicProgressSection(
                        positionMs = calculatedPos,
                        durationMs = mediaPlayback?.durationMs ?: 0L,
                        barHeight = 6.dp,
                        onSeekTo = onSeekTo
                    )

                    StandByMusicSecondaryActionsRow(
                        iconSize = 21.dp,
                        spacing = 24.dp,
                        onActionClick = onOpenMusicApp
                    )

                    // Rodapé: Volume à esquerda + ^ SPOTIFY ao centro
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Volume de mídia",
                            tint = Color(0xFFC9C0B9),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(21.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { showSystemMediaVolumeUi(context) }
                                )
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onOpenMusicApp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowUp,
                                contentDescription = null,
                                tint = Color(0xFFBDB4AD),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = (mediaPlayback?.appDisplayName?.ifBlank { "PLAYER" } ?: "PLAYER").uppercase(Locale.getDefault()),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                color = Color(0xFFBDB4AD)
                            )
                        }
                    }
                }
            }
        }
    }

    if (isLandscape) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.49f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    PastelBubbleClock(
                        formattedTime = formattedTime,
                        palette = palette,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(172.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { paletteIndex += 1 }
                            )
                    )
                }

                musicBackdropCard(
                    Modifier
                        .weight(0.51f)
                        .fillMaxHeight()
                )
            }

            // Botão circular discreto de lápis (troca paleta do relógio pastel, igual ao print 3)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(end = 36.dp, bottom = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF232327).copy(alpha = 0.88f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { paletteIndex += 1 }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Alternar cores do relógio",
                    tint = Color(0xFFE8E2DC),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            PastelBubbleClock(
                formattedTime = formattedTime,
                palette = palette,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { paletteIndex += 1 }
                    )
            )
            musicBackdropCard(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

/**
 * Relógio gordinho arredondado e sobreposto (00:02) com paleta pastel e dois-pontos translúcidos,
 * fiel ao print 3.
 */
@Composable
private fun PastelBubbleClock(
    formattedTime: String,
    palette: PastelClockPalette,
    modifier: Modifier = Modifier
) {
    val digits = remember(formattedTime) {
        val split = formattedTime.split(":")
        val hh = (split.getOrNull(0) ?: "00").filter { it.isDigit() }.padStart(2, '0').take(2)
        val mm = (split.getOrNull(1) ?: "00").filter { it.isDigit() }.padStart(2, '0').take(2)
        listOf(hh[0].toString(), hh[1].toString(), mm[0].toString(), mm[1].toString())
    }

    val roundedBoldTypeface = remember {
        Typeface.create("sans-serif-black", Typeface.BOLD)
    }

    val shadowPaint = remember(roundedBoldTypeface) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = roundedBoldTypeface
            style = Paint.Style.FILL_AND_STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            textAlign = Paint.Align.CENTER
        }
    }

    val digitPaint = remember(roundedBoldTypeface) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = roundedBoldTypeface
            style = Paint.Style.FILL_AND_STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            textAlign = Paint.Align.CENTER
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // Calcula tamanho ideal para 4 dígitos gordinhos levemente sobrepostos
        val textSizePx = min(w * 0.34f, h * 0.88f)
        val extraStroke = textSizePx * 0.135f
        shadowPaint.textSize = textSizePx
        shadowPaint.strokeWidth = extraStroke
        digitPaint.textSize = textSizePx
        digitPaint.strokeWidth = extraStroke

        val fm = digitPaint.fontMetrics
        val baselineY = h / 2f - (fm.ascent + fm.descent) / 2f

        val step = textSizePx * 0.62f
        val centerX = w / 2f
        val colonGap = textSizePx * 0.16f

        val positions = listOf(
            centerX - step * 1.5f - colonGap,
            centerX - step * 0.5f - colonGap * 0.55f,
            centerX + step * 0.5f + colonGap * 0.55f,
            centerX + step * 1.5f + colonGap
        )
        val colors = listOf(palette.h1, palette.h2, palette.m1, palette.m2)

        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas

            // Desenha os dois dígitos das horas (com leve sombra separando sobreposição)
            for (i in 0..1) {
                shadowPaint.color = Color.Black.copy(alpha = 0.42f).toArgb()
                native.drawText(digits[i], positions[i] + textSizePx * 0.03f, baselineY + textSizePx * 0.03f, shadowPaint)
                digitPaint.color = colors[i].toArgb()
                native.drawText(digits[i], positions[i], baselineY, digitPaint)
            }

            // Desenha os dois dígitos dos minutos (m2 atrás ou levemente sobreposto)
            for (i in 2..3) {
                shadowPaint.color = Color.Black.copy(alpha = 0.42f).toArgb()
                native.drawText(digits[i], positions[i] + textSizePx * 0.03f, baselineY + textSizePx * 0.03f, shadowPaint)
                digitPaint.color = colors[i].toArgb()
                native.drawText(digits[i], positions[i], baselineY, digitPaint)
            }
        }

        // Dois pontos translúcidos foscos flutuando exatamente entre hora e minuto (igual ao print 3)
        val colonR = textSizePx * 0.125f
        val colonSpread = textSizePx * 0.21f
        drawCircle(
            color = palette.colon,
            radius = colonR,
            center = Offset(centerX, h / 2f - colonSpread)
        )
        drawCircle(
            color = palette.colon,
            radius = colonR,
            center = Offset(centerX, h / 2f + colonSpread)
        )
    }
}

// ============================================================================
// TELA 4: PLAYER DE MÚSICA FULLSCREEN COM FUNDO AMBIENTE
// Referência: Screenshot_20261122-000138_StandBy.png
// ============================================================================
@Composable
internal fun StandByFullMusicPage(
    isLandscape: Boolean,
    mediaPlayback: MediaPlaybackInfo?,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onOpenMusicApp: () -> Unit
) {
    val context = LocalContext.current
    val isMusicActive = mediaPlayback != null && (
        mediaPlayback.isPlaying || (mediaPlayback.hasActiveSession && mediaPlayback.title.isNotBlank())
    )

    var calculatedPos by remember { mutableLongStateOf(mediaPlayback?.getCalculatedPositionMs() ?: 0L) }
    LaunchedEffect(mediaPlayback, isMusicActive) {
        while (true) {
            calculatedPos = mediaPlayback?.getCalculatedPositionMs() ?: 0L
            if (mediaPlayback?.isPlaying != true) break
            delay(500L)
        }
    }

    val albumBitmap = remember(mediaPlayback?.artwork) {
        runCatching {
            mediaPlayback?.artwork?.takeIf { !it.isRecycled }?.asImageBitmap()
        }.getOrNull()
    }

    // Extrai cor ambiente escura e elegante da capa em Dispatchers.Default
    val ambientPalette by produceState(
        initialValue = Pair(Color(0xFF3E352F), Color(0xFF211C19)),
        key1 = mediaPlayback?.artwork
    ) {
        value = withContext(Dispatchers.Default) {
            extractAmbientColorsFromBitmap(mediaPlayback?.artwork)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        ambientPalette.first,
                        ambientPalette.second,
                        Color(0xFF1A1614)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1400f, 900f)
                )
            )
    ) {
        // Glow suave atrás da capa
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ambientPalette.first.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(320f, 360f),
                        radius = 780f
                    )
                )
        )

        val artworkBlock: @Composable (Modifier) -> Unit = { mod ->
            Box(
                modifier = mod
                    .aspectRatio(1f)
                    .shadow(18.dp, RoundedCornerShape(18.dp), clip = false)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF1E1A18))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenMusicApp
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (albumBitmap != null) {
                    Image(
                        bitmap = albumBitmap,
                        contentDescription = "Capa do álbum",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFFCBBFB6),
                        modifier = Modifier.size(72.dp)
                    )
                }

                // Badge circular no canto inferior direito igual ao print 4
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = "Fila",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }
        }

        val controlsColumn: @Composable (Modifier) -> Unit = { mod ->
            Column(
                modifier = mod,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isMusicActive) mediaPlayback.title else "Nenhuma mídia ativa",
                    fontSize = if (isLandscape) 26.sp else 23.sp,
                    lineHeight = if (isLandscape) 32.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF6ECE2),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isMusicActive) mediaPlayback.artist else "Toque para abrir seu player",
                    fontSize = 16.sp,
                    color = Color(0xFFCBBFB6),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 26.dp else 22.dp))

                StandByMusicTransportRow(
                    isPlaying = mediaPlayback?.isPlaying == true,
                    isMusicActive = isMusicActive,
                    buttonSize = 48.dp,
                    playSize = 62.dp,
                    spacing = 36.dp,
                    tint = Color.White,
                    onSkipPrevious = onSkipPrevious,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipNext = onSkipNext,
                    onOpenMusicApp = onOpenMusicApp
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 20.dp))

                StandByMusicProgressSection(
                    positionMs = calculatedPos,
                    durationMs = mediaPlayback?.durationMs ?: 0L,
                    barHeight = 7.dp,
                    onSeekTo = onSeekTo
                )

                Spacer(modifier = Modifier.height(20.dp))

                StandByMusicSecondaryActionsRow(
                    iconSize = 24.dp,
                    spacing = 28.dp,
                    onActionClick = onOpenMusicApp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Rodapé: ^ SPOTIFY + Volume (igual ao print 4)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenMusicApp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color(0xFFB8AEA6),
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = (mediaPlayback?.appDisplayName?.ifBlank { "PLAYER" } ?: "PLAYER").uppercase(Locale.getDefault()),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = Color(0xFFB8AEA6)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Volume de mídia",
                        tint = Color(0xFFCBBFB6),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showSystemMediaVolumeUi(context) }
                            )
                    )
                }
            }
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 42.dp, vertical = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(42.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    artworkBlock(Modifier.fillMaxHeight(0.86f))
                }
                controlsColumn(
                    Modifier
                        .weight(0.55f)
                        .fillMaxHeight()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                artworkBlock(Modifier.fillMaxWidth(0.72f))
                Spacer(modifier = Modifier.height(28.dp))
                controlsColumn(Modifier.fillMaxWidth())
            }
        }
    }
}

// ============================================================================
// TELA 5: AGENDA MULTI-DIAS MINIMALISTA
// Referência: Screenshot_20261122-000043_StandBy.png
// ============================================================================
@Composable
internal fun StandByAgendaPage(
    isLandscape: Boolean,
    formattedTime: String,
    upcomingEvents: List<CalendarEventInfo>,
    onOpenCalendarApp: () -> Unit,
    onOpenStandBySettings: () -> Unit
) {
    val todayCal = remember(formattedTime) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayDayStr = remember(todayCal) {
        todayCal.get(Calendar.DAY_OF_MONTH).toString()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (upcomingEvents.isEmpty()) {
            // Estado vazio fiel à estética da Agenda do print 5
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isLandscape) 42.dp else 28.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenCalendarApp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.width(88.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = todayDayStr,
                            style = TextStyle(
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF5EAE0),
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HOJE",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = Color(0xFF9A908A)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF06E65))
                    )

                    Spacer(modifier = Modifier.width(22.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Agenda livre",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF7EFE8)
                        )
                        Text(
                            text = "Nenhum evento nos próximos dias • Toque para abrir",
                            fontSize = 15.sp,
                            color = Color(0xFF9D958F)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isLandscape) 42.dp else 24.dp),
                contentPadding = PaddingValues(
                    top = if (isLandscape) 34.dp else 44.dp,
                    bottom = 84.dp
                ),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 36.dp else 28.dp)
            ) {
                itemsIndexed(upcomingEvents) { index, event ->
                    val eventMeta = remember(event, todayCal.timeInMillis) {
                        computeAgendaEventDisplay(event, todayCal)
                    }
                    val accentBarColor = if (eventMeta.daysFromToday == 0 || (index == 0 && event.isAllDay)) {
                        Color(0xFFF06E65) // Coral / Salmão do print 5
                    } else {
                        Color(0xFF7CA844) // Verde oliva do print 5
                    }
                    val subtitleColor = if (eventMeta.daysFromToday == 0 && event.isAllDay) {
                        Color(0xFFE5746A)
                    } else {
                        Color(0xFF9E9690)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenCalendarApp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Coluna da data: Dia grande + HOJE / QUA. / QUI.
                        Column(
                            modifier = Modifier.width(if (isLandscape) 104.dp else 84.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = eventMeta.dayNumber,
                                style = TextStyle(
                                    fontSize = if (isLandscape) 46.sp else 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF5EAE0),
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = eventMeta.dayLabel,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp,
                                color = Color(0xFF9A908A)
                            )
                        }

                        // Barra vertical colorida
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(54.dp)
                                .clip(CircleShape)
                                .background(accentBarColor)
                        )

                        Spacer(modifier = Modifier.width(if (isLandscape) 24.dp else 18.dp))

                        // Título e horário do compromisso
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = event.title,
                                fontSize = if (isLandscape) 22.sp else 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF7EFE8),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = eventMeta.subtitle,
                                fontSize = if (isLandscape) 15.5.sp else 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = subtitleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Botão flutuante de configurações (canto inferior direito, igual ao print 5)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 22.dp)
                .size(58.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF212121))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)), RoundedCornerShape(20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenStandBySettings
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Configurações do StandBy",
                tint = Color(0xFFE7E2DC),
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

// ============================================================================
// TELA 6: PORTA-RETRATOS DE FOTO (FULLSCREEN COM RELÓGIO EDITORIAL)
// ============================================================================
@Composable
internal fun StandByPhotoPage(
    isLandscape: Boolean,
    formattedTime: String,
    bitmap: Bitmap?,
    accentColor: Color,
    activeColorPreset: StandByColorPreset
) {
    val safeImageBitmap = remember(bitmap) {
        runCatching { bitmap?.takeIf { !it.isRecycled }?.asImageBitmap() }.getOrNull()
    }
    var fitMode by remember { mutableStateOf(false) }

    val now = remember(formattedTime) { Date() }
    val dateShort = remember(now) {
        val weekday = formatShortWeekdayPt(now)
        val day = SimpleDateFormat("d", Locale.getDefault()).format(now)
        "$weekday $day"
    }
    val clockColor = remember(activeColorPreset, accentColor) {
        if (activeColorPreset == StandByColorPreset.WHITE) Color(0xFFFAF3EB)
        else activeColorPreset.resolveColor(accentColor)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (safeImageBitmap != null) fitMode = !fitMode }
            )
    ) {
        if (safeImageBitmap != null) {
            Image(
                bitmap = safeImageBitmap,
                contentDescription = "Foto em Modo StandBy",
                contentScale = if (fitMode) ContentScale.Fit else ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Vinheta suave nas bordas para legibilidade cristalina do relógio sobre qualquer foto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.52f)
                            )
                        )
                    )
            )

            // Relógio + Data editorial no canto superior direito (paisagem) ou superior esquerdo (retrato)
            Column(
                modifier = Modifier
                    .align(if (isLandscape) Alignment.TopEnd else Alignment.TopStart)
                    .padding(horizontal = 28.dp, vertical = 22.dp),
                horizontalAlignment = if (isLandscape) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = formattedTime,
                    style = TextStyle(
                        fontSize = if (isLandscape) 64.sp else 58.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-2.5).sp,
                        color = clockColor,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Text(
                    text = dateShort,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        } else {
            // Estado elegante quando o usuário ainda não escolheu uma foto
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedTime,
                    style = TextStyle(
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-2.5).sp,
                        color = clockColor,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dateShort,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9E9690)
                )
                Spacer(modifier = Modifier.height(26.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF141417))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), RoundedCornerShape(22.dp))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Color(0xFFD6C3B3),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Escolha uma foto no widget de Fotos da tela inicial",
                        fontSize = 13.5.sp,
                        color = Color(0xFFA8A09A)
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPONENTES REUTILIZÁVEIS DE MÚSICA E UTILITÁRIOS
// ============================================================================
@Composable
private fun StandByMusicTransportRow(
    isPlaying: Boolean,
    isMusicActive: Boolean,
    buttonSize: Dp,
    playSize: Dp,
    spacing: Dp,
    tint: Color,
    onSkipPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onOpenMusicApp: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.FastRewind,
            contentDescription = "Faixa anterior",
            tint = tint,
            modifier = Modifier
                .size(buttonSize)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSkipPrevious
                )
        )
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
            tint = tint,
            modifier = Modifier
                .size(playSize)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = if (isMusicActive) onTogglePlayPause else onOpenMusicApp
                )
        )
        Icon(
            imageVector = Icons.Rounded.FastForward,
            contentDescription = "Próxima faixa",
            tint = tint,
            modifier = Modifier
                .size(buttonSize)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSkipNext
                )
        )
    }
}

@Composable
private fun StandByMusicProgressSection(
    positionMs: Long,
    durationMs: Long,
    barHeight: Dp,
    onSeekTo: (Long) -> Unit
) {
    val safeDuration = durationMs.coerceAtLeast(0L)
    val clampedPos = if (safeDuration > 0L) positionMs.coerceIn(0L, safeDuration) else 0L
    val liveProgress = if (safeDuration > 0L) (clampedPos.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f) else 0f

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(liveProgress) }
    var barWidthPx by remember { mutableIntStateOf(1) }

    val displayedFraction = if (isDragging) dragFraction else liveProgress
    val displayedElapsed = if (isDragging && safeDuration > 0L) (dragFraction * safeDuration).toLong() else clampedPos
    val displayedRemaining = (safeDuration - displayedElapsed).coerceAtLeast(0L)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .onSizeChanged { barWidthPx = it.width.coerceAtLeast(1) }
                .pointerInput(safeDuration) {
                    if (safeDuration <= 0L) return@pointerInput
                    detectTapGestures { offset ->
                        val frac = (offset.x / barWidthPx.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((frac * safeDuration).toLong())
                    }
                }
                .pointerInput(safeDuration) {
                    if (safeDuration <= 0L) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / barWidthPx.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            if (isDragging) {
                                onSeekTo((dragFraction * safeDuration).toLong())
                                isDragging = false
                            }
                        },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / barWidthPx.toFloat()).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(displayedFraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatStandByDurationMs(displayedElapsed),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFCBBFB6)
            )
            Text(
                text = if (safeDuration > 0L) "-${formatStandByDurationMs(displayedRemaining)}" else "-0:00",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFCBBFB6)
            )
        }
    }
}

@Composable
private fun StandByMusicSecondaryActionsRow(
    iconSize: Dp,
    spacing: Dp,
    onActionClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            Icons.Rounded.Shuffle to "Aleatório",
            Icons.Rounded.CheckCircle to "Favorito",
            Icons.Rounded.Sensors to "Dispositivos",
            Icons.Rounded.RepeatOne to "Repetir"
        ).forEach { (icon, desc) ->
            Icon(
                imageVector = icon,
                contentDescription = desc,
                tint = Color(0xFFC9BEB6),
                modifier = Modifier
                    .size(iconSize)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onActionClick
                    )
            )
        }
    }
}

@Immutable
private data class AgendaEventDisplay(
    val dayNumber: String,
    val dayLabel: String,
    val subtitle: String,
    val daysFromToday: Int
)

private fun computeAgendaEventDisplay(
    event: CalendarEventInfo,
    todayMidnight: Calendar
): AgendaEventDisplay {
    val eventCal = Calendar.getInstance().apply {
        timeInMillis = if (event.beginTimestamp > 0L) event.beginTimestamp else todayMidnight.timeInMillis
    }
    val eventMidnight = Calendar.getInstance().apply {
        set(eventCal.get(Calendar.YEAR), eventCal.get(Calendar.MONTH), eventCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffDays = ((eventMidnight.timeInMillis - todayMidnight.timeInMillis) / (24L * 60L * 60L * 1000L))
        .toInt()
        .coerceAtLeast(0)

    val dayNumber = eventCal.get(Calendar.DAY_OF_MONTH).toString()
    val dayLabel = when (diffDays) {
        0 -> "HOJE"
        1 -> "AMANHÃ"
        else -> formatShortWeekdayPt(eventCal.time)
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeRange = if (event.isAllDay) {
        "Dia inteiro"
    } else if (event.beginTimestamp > 0L && event.endTimestamp > event.beginTimestamp) {
        "${timeFormat.format(Date(event.beginTimestamp))} - ${timeFormat.format(Date(event.endTimestamp))}"
    } else {
        event.timeFormatted
    }

    val subtitle = when {
        diffDays == 0 && event.isAllDay -> "Dia inteiro"
        diffDays == 0 && event.isOngoing -> "Agora • $timeRange"
        diffDays == 0 -> timeRange
        diffDays == 1 -> "$timeRange • Amanhã"
        else -> "$timeRange • Em $diffDays dias"
    }

    return AgendaEventDisplay(
        dayNumber = dayNumber,
        dayLabel = dayLabel,
        subtitle = subtitle,
        daysFromToday = diffDays
    )
}

private fun formatDashboardEventSubtitle(event: CalendarEventInfo?): String {
    if (event == null) return "Sem eventos próximos"
    if (event.isOngoing) return "Em andamento • ${event.timeFormatted}"

    val todayMidnight = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val eventCal = Calendar.getInstance().apply {
        timeInMillis = if (event.beginTimestamp > 0L) event.beginTimestamp else System.currentTimeMillis()
    }
    val eventMidnight = Calendar.getInstance().apply {
        set(eventCal.get(Calendar.YEAR), eventCal.get(Calendar.MONTH), eventCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffDays = ((eventMidnight.timeInMillis - todayMidnight.timeInMillis) / (24L * 60L * 60L * 1000L)).toInt()

    if (event.isAllDay) {
        return when {
            diffDays <= 0 -> "Dia inteiro"
            diffDays == 1 -> "Amanhã • Dia inteiro"
            else -> "Em $diffDays dias"
        }
    }

    val mins = event.minutesUntil
    return when {
        diffDays > 1 -> "${event.timeFormatted} • em $diffDays dias"
        diffDays == 1 -> "Amanhã às ${event.timeFormatted}"
        mins < 60 -> "em ${mins.coerceAtLeast(1)} min"
        mins < 180 -> {
            val h = mins / 60
            val m = mins % 60
            if (m > 0) "em ${h}h ${m}min" else "em ${h}h"
        }
        else -> "Hoje às ${event.timeFormatted}"
    }
}

private fun formatShortWeekdayPt(date: Date): String {
    val cal = Calendar.getInstance().apply { time = date }
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "DOM."
        Calendar.MONDAY -> "SEG."
        Calendar.TUESDAY -> "TER."
        Calendar.WEDNESDAY -> "QUA."
        Calendar.THURSDAY -> "QUI."
        Calendar.FRIDAY -> "SEX."
        Calendar.SATURDAY -> "SÁB."
        else -> {
            val ptBr = Locale.forLanguageTag("pt-BR")
            SimpleDateFormat("EEE", ptBr).format(date).uppercase(ptBr)
        }
    }
}

private fun resolveWeatherIcon(condition: String?): ImageVector {
    val c = condition.orEmpty().lowercase(Locale.getDefault())
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isNight = hour < 6 || hour >= 18
    return when {
        c.contains("chuva") || c.contains("rain") || c.contains("garoa") || c.contains("tempest") -> Icons.Rounded.WaterDrop
        c.contains("nubl") || c.contains("nuv") || c.contains("cloud") || c.contains("encob") -> Icons.Rounded.Cloud
        isNight -> Icons.Outlined.DarkMode
        else -> Icons.Outlined.WbSunny
    }
}

private fun formatStandByDurationMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val min = totalSec / 60L
    val sec = totalSec % 60L
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}

private fun showSystemMediaVolumeUi(context: Context) {
    runCatching {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI
        )
    }
}

/**
 * Amostragem ultraleve em background da cor ambiente dominante da capa do álbum para o Player Fullscreen.
 */
private fun extractAmbientColorsFromBitmap(bitmap: Bitmap?): Pair<Color, Color> {
    val fallback = Color(0xFF3E352F) to Color(0xFF211C19)
    if (bitmap == null || bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return fallback
    return runCatching {
        val sampleCols = 12
        val sampleRows = 12
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0L

        val stepX = (bitmap.width / sampleCols).coerceAtLeast(1)
        val stepY = (bitmap.height / sampleRows).coerceAtLeast(1)

        var y = stepY / 2
        while (y < bitmap.height) {
            var x = stepX / 2
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                // Ignora pixels totalmente pretos ou quase brancos estourados
                val maxCh = maxOf(r, g, b)
                val minCh = minOf(r, g, b)
                val sat = maxCh - minCh
                val weight = if (sat > 16) 2L else 1L
                rSum += r * weight
                gSum += g * weight
                bSum += b * weight
                count += weight
                x += stepX
            }
            y += stepY
        }

        if (count == 0L) return@runCatching fallback

        val avgR = (rSum / count).toFloat() / 255f
        val avgG = (gSum / count).toFloat() / 255f
        val avgB = (bSum / count).toFloat() / 255f

        // Escurece suavemente mantendo o matiz quente/atmosférico igual ao print 4
        val topColor = Color(
            red = (avgR * 0.42f + 0.06f).coerceIn(0.10f, 0.34f),
            green = (avgG * 0.42f + 0.05f).coerceIn(0.09f, 0.30f),
            blue = (avgB * 0.42f + 0.05f).coerceIn(0.09f, 0.30f),
            alpha = 1f
        )
        val bottomColor = Color(
            red = (avgR * 0.22f + 0.04f).coerceIn(0.06f, 0.20f),
            green = (avgG * 0.22f + 0.03f).coerceIn(0.05f, 0.18f),
            blue = (avgB * 0.22f + 0.03f).coerceIn(0.05f, 0.18f),
            alpha = 1f
        )
        topColor to bottomColor
    }.getOrDefault(fallback)
}
