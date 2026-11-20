package com.tessera.launcher.ui.components

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.R
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.model.AppNotificationItem
import com.tessera.launcher.data.service.MediaPlaybackInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class StandByViewMode(val key: String, val label: String, val icon: ImageVector) {
    DUAL("DUAL", "Dupla", Icons.Outlined.GridView),
    MUSIC("MUSIC", "Música", Icons.Outlined.MusicNote),
    EVENTS("EVENTS", "Eventos", Icons.Outlined.Event),
    POMODORO("POMODORO", "Pomodoro", Icons.Outlined.Timer);

    companion object {
        fun fromKey(key: String): StandByViewMode =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DUAL
    }
}

enum class StandByClockStyle(val key: String, val label: String) {
    FLIP("FLIP", "Flip 3D"),
    EDITORIAL("EDITORIAL", "Editorial"),
    MINIMAL("MINIMAL", "Minimal");

    fun next(): StandByClockStyle {
        val list = entries
        return list[(ordinal + 1) % list.size]
    }

    companion object {
        fun fromKey(key: String): StandByClockStyle =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: FLIP
    }
}

enum class StandByColorPreset(val key: String, val label: String) {
    WHITE("WHITE", "Branco"),
    ACCENT("ACCENT", "Destaque"),
    AMBER("AMBER", "Âmbar"),
    EMERALD("EMERALD", "Esmeralda"),
    CYAN("CYAN", "Ciano");

    fun resolveColor(accentColor: Color): Color = when (this) {
        WHITE -> Color.White
        ACCENT -> accentColor
        AMBER -> Color(0xFFFFB040)
        EMERALD -> Color(0xFF34D399)
        CYAN -> Color(0xFF38BDF8)
    }

    fun next(): StandByColorPreset {
        val list = entries
        return list[(ordinal + 1) % list.size]
    }

    companion object {
        fun fromKey(key: String): StandByColorPreset =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: WHITE
    }
}

private enum class PomodoroPhase(val label: String, val durationSeconds: Int) {
    FOCUS("Foco", 25 * 60),
    SHORT_BREAK("Pausa", 5 * 60),
    LONG_BREAK("Descanso", 15 * 60)
}

/**
 * Modo StandBy Pro 2.0 (Ambient Smart Display).
 * - Logo Tessera integrado à pílula central do topo com Safe Area (DisplayCutout)
 * - Notificações em tempo real estilo StandBy (Pop-up animado de 6s + Pílula interativa + Modo Privacidade)
 * - 4 Modos dedicados: Visualização Dupla (Smart Stack Foto/Agenda/Tarefas), Música, Eventos e Pomodoro
 * - 3 Estilos de Relógio (Flip 3D, Editorial, Minimal) + 5 Presets de Cor
 * - Proteção Anti Burn-in OLED (Pixel Shift por minuto) + Keep Screen On
 */
@Composable
fun PhotoStandByModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    photoUriString: String?,
    photoUris: List<String> = emptyList(),
    activePhotoIndex: Int = 0,
    onSelectPhotoIndex: (Int) -> Unit = {},
    formattedTime: String,
    formattedDate: String,
    batteryPercentage: Int,
    isCharging: Boolean,
    weatherTemp: String? = null,
    weatherCondition: String? = null,
    weatherCity: String? = null,
    accentColor: Color = Color(0xFFFFA040),
    mediaPlayback: MediaPlaybackInfo? = null,
    recentNotifications: List<AppNotificationItem> = emptyList(),
    upcomingCalendarEvents: List<CalendarEventInfo> = emptyList(),
    notesTasks: List<NoteTask> = emptyList(),
    standByViewMode: String = "DUAL",
    standByClockStyle: String = "FLIP",
    standByColorPreset: String = "WHITE",
    isNotificationPrivacyEnabled: Boolean = false,
    onSelectViewMode: (String) -> Unit = {},
    onSelectClockStyle: (String) -> Unit = {},
    onSelectColorPreset: (String) -> Unit = {},
    onToggleNotificationPrivacy: (Boolean) -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onOpenMusicApp: () -> Unit = {},
    onOpenCalendarApp: () -> Unit = {},
    onToggleNoteTask: (Long) -> Unit = {},
    onNotificationClick: (AppNotificationItem) -> Unit = {},
    onDismissNotification: (String) -> Unit = {},
    onDismissAllNotifications: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val activeMode = StandByViewMode.fromKey(standByViewMode)
    val activeClockStyle = StandByClockStyle.fromKey(standByClockStyle)
    val activeColorPreset = StandByColorPreset.fromKey(standByColorPreset)
    val themeColor = activeColorPreset.resolveColor(accentColor)

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

    // Safe area para câmeras frontais (notch / punch-hole / ilha) em Retrato e Paisagem
    val cutoutPadding = WindowInsets.displayCutout.asPaddingValues()
    val statusPadding = WindowInsets.statusBars.asPaddingValues()
    val navPadding = WindowInsets.navigationBars.asPaddingValues()

    val safeTop = maxOf(
        cutoutPadding.calculateTopPadding(),
        statusPadding.calculateTopPadding()
    ).coerceAtLeast(14.dp) + 4.dp
    val safeStart = maxOf(
        cutoutPadding.calculateLeftPadding(layoutDirection),
        navPadding.calculateLeftPadding(layoutDirection)
    ).coerceAtLeast(16.dp)
    val safeEnd = maxOf(
        cutoutPadding.calculateRightPadding(layoutDirection),
        navPadding.calculateRightPadding(layoutDirection)
    ).coerceAtLeast(16.dp)
    val safeBottom = maxOf(
        cutoutPadding.calculateBottomPadding(),
        navPadding.calculateBottomPadding()
    ).coerceAtLeast(14.dp)

    // Proteção Anti Burn-in OLED (micro-deslocamento a cada minuto)
    val burnInOffset = remember(formattedTime) {
        val h = formattedTime.hashCode()
        val dx = ((h % 7) - 3).dp
        val dy = (((h / 7) % 7) - 3).dp
        dx to dy
    }

    // Estado do Smart Stack no Modo Visualização Dupla (0 = Foto, 1 = Agenda, 2 = Tarefas)
    var smartStackIndex by remember { mutableIntStateOf(0) }

    // Estado do Timer Pomodoro (sobrevive à troca entre abas enquanto StandBy está aberto)
    var pomodoroPhase by remember { mutableStateOf(PomodoroPhase.FOCUS) }
    var pomodoroRemainingSec by remember { mutableIntStateOf(PomodoroPhase.FOCUS.durationSeconds) }
    var pomodoroRunning by remember { mutableStateOf(false) }
    var pomodoroCompletedCycles by remember { mutableIntStateOf(0) }

    LaunchedEffect(isOpen, pomodoroRunning, pomodoroPhase) {
        while (isOpen && pomodoroRunning && pomodoroRemainingSec > 0) {
            delay(1000L)
            pomodoroRemainingSec -= 1
            if (pomodoroRemainingSec <= 0) {
                pomodoroRunning = false
                if (pomodoroPhase == PomodoroPhase.FOCUS) {
                    pomodoroCompletedCycles += 1
                    val nextPhase = if (pomodoroCompletedCycles % 4 == 0) {
                        PomodoroPhase.LONG_BREAK
                    } else {
                        PomodoroPhase.SHORT_BREAK
                    }
                    pomodoroPhase = nextPhase
                    pomodoroRemainingSec = nextPhase.durationSeconds
                } else {
                    pomodoroPhase = PomodoroPhase.FOCUS
                    pomodoroRemainingSec = PomodoroPhase.FOCUS.durationSeconds
                }
            }
        }
    }

    // Motor de Notificações Estilo StandBy (Pop-up ao receber + Painel de Leitura)
    var isNotificationOverlayOpen by remember { mutableStateOf(false) }
    var popupNotification by remember { mutableStateOf<AppNotificationItem?>(null) }
    var isPopupRevealed by remember { mutableStateOf(false) }
    var seenNotificationKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var initializedSeenKeys by remember { mutableStateOf(false) }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            seenNotificationKeys = recentNotifications.map { it.key }.toSet()
            initializedSeenKeys = true
            popupNotification = null
            isNotificationOverlayOpen = false
        } else {
            initializedSeenKeys = false
            popupNotification = null
            isNotificationOverlayOpen = false
        }
    }

    LaunchedEffect(recentNotifications, isOpen, initializedSeenKeys) {
        if (!isOpen || !initializedSeenKeys) return@LaunchedEffect
        val newestUnseen = recentNotifications
            .filterNot { it.isOngoing }
            .firstOrNull { it.key !in seenNotificationKeys }
        if (newestUnseen != null) {
            seenNotificationKeys = seenNotificationKeys + newestUnseen.key
            popupNotification = newestUnseen
            isPopupRevealed = !isNotificationPrivacyEnabled
        } else if (popupNotification != null && recentNotifications.none { it.key == popupNotification?.key }) {
            popupNotification = null
        }
    }

    LaunchedEffect(popupNotification?.key, isPopupRevealed) {
        if (popupNotification != null) {
            delay(6500L)
            popupNotification = null
        }
    }

    // Ativa rotação por sensor + FLAG_KEEP_SCREEN_ON de forma segura enquanto o Modo Stand estiver aberto
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
                activity.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            onDispose {
                runCatching {
                    activity.requestedOrientation = previousOrientation
                    activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        } else {
            onDispose {}
        }
    }

    val effectivePhotoUri = remember(photoUris, activePhotoIndex, photoUriString) {
        if (photoUris.isNotEmpty()) {
            photoUris.getOrNull(activePhotoIndex.coerceIn(0, photoUris.lastIndex)) ?: photoUriString
        } else {
            photoUriString
        }
    }

    val initialFullBitmap = remember(effectivePhotoUri, isOpen) {
        if (isOpen && !effectivePhotoUri.isNullOrBlank()) {
            com.tessera.launcher.data.helper.PhotoBitmapCache.getFull(effectivePhotoUri)
        } else {
            null
        }
    }

    // Carrega o bitmap da foto em IO thread (instantâneo quando pré-processado em disco/RAM)
    val bitmap by produceState<Bitmap?>(
        initialValue = initialFullBitmap,
        key1 = effectivePhotoUri,
        key2 = isOpen
    ) {
        value = if (isOpen && !effectivePhotoUri.isNullOrBlank()) {
            value ?: withContext(Dispatchers.IO) {
                com.tessera.launcher.data.helper.loadStandByPhotoFast(
                    context = context,
                    photoUriOrPath = effectivePhotoUri
                )
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
                    onClick = {
                        when {
                            isNotificationOverlayOpen -> isNotificationOverlayOpen = false
                            popupNotification != null -> popupNotification = null
                            else -> onDismiss()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Sutil vinheta neutra monocromática / cor selecionada ao fundo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                themeColor.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            radius = 1200f
                        )
                    )
            )

            // Conteúdo principal com Safe Area + Proteção Anti Burn-in OLED
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = safeTop,
                        start = safeStart,
                        end = safeEnd,
                        bottom = safeBottom
                    )
                    .offset(x = burnInOffset.first, y = burnInOffset.second)
            ) {
                // ==========================================
                // BARRA SUPERIOR CENTRALIZADA (COM LOGO LONGE DAS CÂMERAS)
                // ==========================================
                StandByTopHeaderBar(
                    isLandscape = isLandscape,
                    weatherTemp = weatherTemp,
                    weatherCondition = weatherCondition,
                    weatherCity = weatherCity,
                    formattedTime = formattedTime,
                    batteryPercentage = batteryPercentage,
                    isCharging = isCharging,
                    notificationCount = recentNotifications.count { !it.isOngoing },
                    recentNotifications = recentNotifications,
                    themeColor = themeColor,
                    activeClockStyle = activeClockStyle,
                    isPrivacyEnabled = isNotificationPrivacyEnabled,
                    onLogoClick = onDismiss,
                    onNotificationsClick = { isNotificationOverlayOpen = !isNotificationOverlayOpen },
                    onTogglePrivacy = { onToggleNotificationPrivacy(!isNotificationPrivacyEnabled) },
                    onCycleClockStyle = { onSelectClockStyle(activeClockStyle.next().key) },
                    onCycleColorPreset = { onSelectColorPreset(activeColorPreset.next().key) },
                    onClose = onDismiss,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // ==========================================
                // CONTEÚDO CENTRAL DOS 4 MODOS
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (isLandscape) 52.dp else 94.dp,
                            bottom = 58.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = activeMode,
                        transitionSpec = {
                            fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                        },
                        label = "StandByModeTransition"
                    ) { mode ->
                        when (mode) {
                            StandByViewMode.DUAL -> {
                                StandByDualModeContent(
                                    isLandscape = isLandscape,
                                    formattedTime = formattedTime,
                                    dayOfWeek = dayOfWeek,
                                    monthDay = monthDay,
                                    clockStyle = activeClockStyle,
                                    themeColor = themeColor,
                                    isMusicActive = isMusicActive,
                                    mediaPlayback = mediaPlayback,
                                    bitmap = bitmap,
                                    smartStackIndex = smartStackIndex,
                                    onCycleSmartStack = { smartStackIndex = (smartStackIndex + 1) % 3 },
                                    onSelectSmartStack = { smartStackIndex = it },
                                    upcomingEvents = upcomingCalendarEvents,
                                    notesTasks = notesTasks,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onSkipNext = onSkipNext,
                                    onSkipPrevious = onSkipPrevious,
                                    onOpenMusicApp = onOpenMusicApp,
                                    onOpenCalendarApp = onOpenCalendarApp,
                                    onToggleNoteTask = onToggleNoteTask,
                                    onCycleClockStyle = { onSelectClockStyle(activeClockStyle.next().key) }
                                )
                            }

                            StandByViewMode.MUSIC -> {
                                StandByMusicModeContent(
                                    isLandscape = isLandscape,
                                    formattedTime = formattedTime,
                                    monthDay = monthDay,
                                    themeColor = themeColor,
                                    mediaPlayback = mediaPlayback,
                                    isMusicActive = isMusicActive,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onSkipNext = onSkipNext,
                                    onSkipPrevious = onSkipPrevious,
                                    onOpenMusicApp = onOpenMusicApp
                                )
                            }

                            StandByViewMode.EVENTS -> {
                                StandByEventsModeContent(
                                    isLandscape = isLandscape,
                                    formattedTime = formattedTime,
                                    dayOfWeek = dayOfWeek,
                                    monthDay = monthDay,
                                    clockStyle = activeClockStyle,
                                    themeColor = themeColor,
                                    upcomingEvents = upcomingCalendarEvents,
                                    notesTasks = notesTasks,
                                    onOpenCalendarApp = onOpenCalendarApp,
                                    onToggleNoteTask = onToggleNoteTask
                                )
                            }

                            StandByViewMode.POMODORO -> {
                                StandByPomodoroModeContent(
                                    isLandscape = isLandscape,
                                    formattedTime = formattedTime,
                                    themeColor = themeColor,
                                    phase = pomodoroPhase,
                                    remainingSeconds = pomodoroRemainingSec,
                                    isRunning = pomodoroRunning,
                                    completedCycles = pomodoroCompletedCycles,
                                    onToggleRun = { pomodoroRunning = !pomodoroRunning },
                                    onReset = {
                                        pomodoroRunning = false
                                        pomodoroRemainingSec = pomodoroPhase.durationSeconds
                                    },
                                    onSelectPhase = { newPhase ->
                                        pomodoroRunning = false
                                        pomodoroPhase = newPhase
                                        pomodoroRemainingSec = newPhase.durationSeconds
                                    }
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // DOCK INFERIOR DE SELEÇÃO DE MODO (4 MODOS)
                // ==========================================
                StandByModeSelectorDock(
                    activeMode = activeMode,
                    themeColor = themeColor,
                    onSelectMode = { onSelectViewMode(it.key) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // ==========================================
                // POP-UP ANIMADO DE NOTIFICAÇÃO EM TEMPO REAL (ESTILO STANDBY)
                // ==========================================
                AnimatedVisibility(
                    visible = popupNotification != null && !isNotificationOverlayOpen,
                    enter = fadeIn(tween(240)) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ),
                    exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.92f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp)
                ) {
                    popupNotification?.let { notif ->
                        StandByLiveNotificationSpotlight(
                            item = notif,
                            themeColor = themeColor,
                            isPrivacyHidden = isNotificationPrivacyEnabled && !isPopupRevealed,
                            onReveal = { isPopupRevealed = true },
                            onOpen = {
                                onNotificationClick(notif)
                                popupNotification = null
                            },
                            onDismiss = {
                                onDismissNotification(notif.key)
                                popupNotification = null
                            }
                        )
                    }
                }

                // ==========================================
                // PAINEL INTERATIVO DE NOTIFICAÇÕES NO STANDBY
                // ==========================================
                AnimatedVisibility(
                    visible = isNotificationOverlayOpen,
                    enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.94f),
                    exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.94f),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    StandByNotificationsOverlay(
                        notifications = recentNotifications,
                        themeColor = themeColor,
                        isPrivacyEnabled = isNotificationPrivacyEnabled,
                        onTogglePrivacy = { onToggleNotificationPrivacy(!isNotificationPrivacyEnabled) },
                        onNotificationClick = {
                            onNotificationClick(it)
                            isNotificationOverlayOpen = false
                        },
                        onDismissNotification = onDismissNotification,
                        onDismissAll = onDismissAllNotifications,
                        onCloseOverlay = { isNotificationOverlayOpen = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun StandByTopHeaderBar(
    isLandscape: Boolean,
    weatherTemp: String?,
    weatherCondition: String?,
    weatherCity: String?,
    formattedTime: String,
    batteryPercentage: Int,
    isCharging: Boolean,
    notificationCount: Int,
    recentNotifications: List<AppNotificationItem>,
    themeColor: Color,
    activeClockStyle: StandByClockStyle,
    isPrivacyEnabled: Boolean,
    onLogoClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onCycleClockStyle: () -> Unit,
    onCycleColorPreset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        Box(modifier = modifier.fillMaxWidth()) {
            // Pílula Central Unificada: Logo Tessera (centralizado longe das câmeras laterais) + Clima + Bateria + Notificações
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StandByTesseraBadge(onDismiss = onLogoClick)
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
                StandByNotificationPill(
                    count = notificationCount,
                    notifications = recentNotifications,
                    themeColor = themeColor,
                    onClick = onNotificationsClick
                )
            }

            // Controles rápidos no canto direito (estilo, cor, privacidade, fechar)
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StandBySmallIconButton(
                    icon = if (isPrivacyEnabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = "Privacidade de notificações",
                    tint = if (isPrivacyEnabled) themeColor else Color.White.copy(alpha = 0.8f),
                    onClick = onTogglePrivacy
                )
                StandBySmallIconButton(
                    icon = Icons.Outlined.Schedule,
                    contentDescription = "Estilo de relógio (${activeClockStyle.label})",
                    tint = Color.White.copy(alpha = 0.85f),
                    onClick = onCycleClockStyle
                )
                StandBySmallIconButton(
                    icon = Icons.Outlined.Palette,
                    contentDescription = "Cor do relógio",
                    tint = themeColor,
                    onClick = onCycleColorPreset
                )
                StandBySmallIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = "Fechar modo StandBy",
                    tint = Color.White.copy(alpha = 0.9f),
                    onClick = onClose
                )
            }
        }
    } else {
        // Retrato: 2 linhas equilibradas com Logo centralizado no topo para nunca ser cortado pela câmera
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Pílula central com Logo + Notificações
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color(0xFF14141B).copy(alpha = 0.86f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StandByTesseraBadge(onDismiss = onLogoClick, size = 30.dp)
                    StandByNotificationPill(
                        count = notificationCount,
                        notifications = recentNotifications,
                        themeColor = themeColor,
                        onClick = onNotificationsClick,
                        compact = true
                    )
                }

                // Botão Fechar discreto no canto direito
                StandBySmallIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = "Fechar modo StandBy",
                    tint = Color.White.copy(alpha = 0.88f),
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StandByWeatherChip(
                    weatherTemp = weatherTemp,
                    weatherCondition = weatherCondition,
                    weatherCity = weatherCity,
                    formattedTime = formattedTime,
                    modifier = Modifier.weight(1f, fill = false)
                )
                StandByBatteryChip(
                    batteryPercentage = batteryPercentage,
                    isCharging = isCharging
                )
                StandBySmallIconButton(
                    icon = if (isPrivacyEnabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = "Privacidade",
                    tint = if (isPrivacyEnabled) themeColor else Color.White.copy(alpha = 0.8f),
                    onClick = onTogglePrivacy
                )
                StandBySmallIconButton(
                    icon = Icons.Outlined.Schedule,
                    contentDescription = "Estilo de relógio",
                    tint = Color.White.copy(alpha = 0.85f),
                    onClick = onCycleClockStyle
                )
                StandBySmallIconButton(
                    icon = Icons.Outlined.Palette,
                    contentDescription = "Cor",
                    tint = themeColor,
                    onClick = onCycleColorPreset
                )
            }
        }
    }
}

@Composable
private fun StandBySmallIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF16161D).copy(alpha = 0.88f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun StandByNotificationPill(
    count: Int,
    notifications: List<AppNotificationItem>,
    themeColor: Color,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    val activePackages = remember(notifications) {
        notifications.filterNot { it.isOngoing }.map { it.packageName }.distinct().take(3)
    }
    Box(
        modifier = Modifier
            .height(if (compact) 30.dp else 36.dp)
            .clip(CircleShape)
            .background(if (count > 0) themeColor.copy(alpha = 0.16f) else Color(0xFF16161D).copy(alpha = 0.88f))
            .border(
                BorderStroke(
                    1.dp,
                    if (count > 0) themeColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f)
                ),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (count > 0) Icons.Outlined.Notifications else Icons.Outlined.NotificationsNone,
                contentDescription = "Notificações",
                tint = if (count > 0) themeColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(15.dp)
            )
            if (activePackages.isNotEmpty() && !compact) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    activePackages.forEach { pkg ->
                        NotificationAppIcon(
                            packageName = pkg,
                            size = 16.dp,
                            shape = CircleShape,
                            fallbackTint = themeColor
                        )
                    }
                }
            }
            Text(
                text = if (count > 0) "$count" else "0",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) Color.White else Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun StandByModeSelectorDock(
    activeMode: StandByViewMode,
    themeColor: Color,
    onSelectMode: (StandByViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF121218).copy(alpha = 0.9f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StandByViewMode.entries.forEach { mode ->
            val selected = mode == activeMode
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) themeColor.copy(alpha = 0.22f) else Color.Transparent)
                    .border(
                        BorderStroke(
                            1.dp,
                            if (selected) themeColor.copy(alpha = 0.5f) else Color.Transparent
                        ),
                        CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectMode(mode) }
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = mode.icon,
                    contentDescription = mode.label,
                    tint = if (selected) themeColor else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = mode.label,
                    fontSize = 11.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ==========================================
// MODO 1: VISUALIZAÇÃO DUPLA (RELÓGIO + MÍDIA + SMART STACK FOTO/AGENDA/TAREFAS)
// ==========================================
@Composable
private fun StandByDualModeContent(
    isLandscape: Boolean,
    formattedTime: String,
    dayOfWeek: String,
    monthDay: String,
    clockStyle: StandByClockStyle,
    themeColor: Color,
    isMusicActive: Boolean,
    mediaPlayback: MediaPlaybackInfo?,
    bitmap: Bitmap?,
    smartStackIndex: Int,
    onCycleSmartStack: () -> Unit,
    onSelectSmartStack: (Int) -> Unit,
    upcomingEvents: List<CalendarEventInfo>,
    notesTasks: List<NoteTask>,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onOpenCalendarApp: () -> Unit,
    onToggleNoteTask: (Long) -> Unit,
    onCycleClockStyle: () -> Unit
) {
    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                StandByClockBlock(
                    formattedTime = formattedTime,
                    dayOfWeek = dayOfWeek,
                    monthDay = monthDay,
                    clockStyle = clockStyle,
                    color = themeColor,
                    isMusicActive = isMusicActive,
                    isLandscape = true,
                    onClick = onCycleClockStyle
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                val cardSize by animateDpAsState(
                    targetValue = if (isMusicActive) 156.dp else 196.dp,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioNoBouncy
                    ),
                    label = "LandscapeStackSize"
                )
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
                                cardSize = 156.dp,
                                onTogglePlayPause = onTogglePlayPause,
                                onSkipNext = onSkipNext,
                                onSkipPrevious = onSkipPrevious,
                                onOpenMusicApp = onOpenMusicApp
                            )
                        }
                    }

                    StandBySmartStackCard(
                        bitmap = bitmap,
                        cardSize = cardSize,
                        stackIndex = smartStackIndex,
                        themeColor = themeColor,
                        upcomingEvents = upcomingEvents,
                        notesTasks = notesTasks,
                        onCycleStack = onCycleSmartStack,
                        onSelectStack = onSelectSmartStack,
                        onOpenCalendarApp = onOpenCalendarApp,
                        onToggleNoteTask = onToggleNoteTask
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StandByClockBlock(
                formattedTime = formattedTime,
                dayOfWeek = dayOfWeek,
                monthDay = monthDay,
                clockStyle = clockStyle,
                color = themeColor,
                isMusicActive = isMusicActive,
                isLandscape = false,
                onClick = onCycleClockStyle
            )

            Spacer(modifier = Modifier.height(24.dp))

            val portraitCardSize by animateDpAsState(
                targetValue = if (isMusicActive) 144.dp else 184.dp,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                ),
                label = "PortraitStackSize"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                            cardSize = 144.dp,
                            onTogglePlayPause = onTogglePlayPause,
                            onSkipNext = onSkipNext,
                            onSkipPrevious = onSkipPrevious,
                            onOpenMusicApp = onOpenMusicApp
                        )
                    }
                }

                StandBySmartStackCard(
                    bitmap = bitmap,
                    cardSize = portraitCardSize,
                    stackIndex = smartStackIndex,
                    themeColor = themeColor,
                    upcomingEvents = upcomingEvents,
                    notesTasks = notesTasks,
                    onCycleStack = onCycleSmartStack,
                    onSelectStack = onSelectSmartStack,
                    onOpenCalendarApp = onOpenCalendarApp,
                    onToggleNoteTask = onToggleNoteTask
                )
            }
        }
    }
}

@Composable
private fun StandBySmartStackCard(
    bitmap: Bitmap?,
    cardSize: Dp,
    stackIndex: Int,
    themeColor: Color,
    upcomingEvents: List<CalendarEventInfo>,
    notesTasks: List<NoteTask>,
    onCycleStack: () -> Unit,
    onSelectStack: (Int) -> Unit,
    onOpenCalendarApp: () -> Unit,
    onToggleNoteTask: (Long) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(cardSize)
                .clip(RoundedCornerShape(26.dp))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF22222A), Color(0xFF141418), Color(0xFF0C0C0E))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCycleStack
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = stackIndex,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "SmartStackSlide"
            ) { idx ->
                when (idx) {
                    0 -> StandByPhotoCardContent(bitmap = bitmap)
                    1 -> StandByStackCalendarContent(
                        upcomingEvents = upcomingEvents,
                        themeColor = themeColor,
                        onOpenCalendarApp = onOpenCalendarApp
                    )
                    else -> StandByStackTasksContent(
                        notesTasks = notesTasks,
                        themeColor = themeColor,
                        onToggleNoteTask = onToggleNoteTask
                    )
                }
            }
        }

        // Indicadores interativos do Smart Stack (Foto • Agenda • Tarefas)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val labels = listOf("Foto", "Agenda", "Tarefas")
            labels.forEachIndexed { idx, label ->
                val active = stackIndex == idx
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (active) themeColor.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectStack(idx) }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 9.5.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) Color.White else Color.White.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StandByPhotoCardContent(bitmap: Bitmap?) {
    val safeImageBitmap = remember(bitmap) {
        runCatching {
            bitmap?.takeIf { !it.isRecycled }?.asImageBitmap()
        }.getOrNull()
    }
    if (safeImageBitmap != null) {
        Image(
            bitmap = safeImageBitmap,
            contentDescription = "Foto em modo StandBy",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toque para alternar",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun StandByStackCalendarContent(
    upcomingEvents: List<CalendarEventInfo>,
    themeColor: Color,
    onOpenCalendarApp: () -> Unit
) {
    val nextEvent = upcomingEvents.firstOrNull()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = themeColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "AGENDA",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = themeColor
            )
        }

        if (nextEvent != null) {
            Column {
                Text(
                    text = nextEvent.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (nextEvent.isOngoing) "Em andamento • ${nextEvent.timeFormatted}" else nextEvent.timeFormatted,
                    fontSize = 11.5.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
        } else {
            Text(
                text = "Nenhum evento próximo hoje",
                fontSize = 12.5.sp,
                color = Color(0xFFA1A1AA)
            )
        }

        Text(
            text = "Abrir agenda →",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = themeColor,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpenCalendarApp
            )
        )
    }
}

@Composable
private fun StandByStackTasksContent(
    notesTasks: List<NoteTask>,
    themeColor: Color,
    onToggleNoteTask: (Long) -> Unit
) {
    val topTasks = remember(notesTasks) {
        notesTasks.sortedBy { it.isDone }.take(3)
    }
    val pendingCount = notesTasks.count { !it.isDone }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TAREFAS",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = themeColor
            )
            Text(
                text = "$pendingCount pend.",
                fontSize = 10.sp,
                color = Color(0xFFA1A1AA)
            )
        }

        if (topTasks.isEmpty()) {
            Text(
                text = "Nenhuma tarefa pendente",
                fontSize = 12.sp,
                color = Color(0xFFA1A1AA)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                topTasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onToggleNoteTask(task.id) }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isDone) themeColor else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = task.text,
                            fontSize = 11.5.sp,
                            color = if (task.isDone) Color.White.copy(alpha = 0.4f) else Color.White,
                            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

// ==========================================
// MODO 2: MODO MÚSICA DEDICADO
// ==========================================
@Composable
private fun StandByMusicModeContent(
    isLandscape: Boolean,
    formattedTime: String,
    monthDay: String,
    themeColor: Color,
    mediaPlayback: MediaPlaybackInfo?,
    isMusicActive: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenMusicApp: () -> Unit
) {
    var calculatedPos by remember { mutableLongStateOf(0L) }
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

    val artworkSize = if (isLandscape) 186.dp else 210.dp

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            StandByLargeArtworkBox(
                albumBitmap = albumBitmap,
                size = artworkSize,
                themeColor = themeColor,
                onClick = onOpenMusicApp
            )

            StandByMusicDetailsAndControls(
                formattedTime = formattedTime,
                monthDay = monthDay,
                themeColor = themeColor,
                mediaPlayback = mediaPlayback,
                isMusicActive = isMusicActive,
                calculatedPos = calculatedPos,
                onTogglePlayPause = onTogglePlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onOpenMusicApp = onOpenMusicApp,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$formattedTime • $monthDay",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            StandByLargeArtworkBox(
                albumBitmap = albumBitmap,
                size = artworkSize,
                themeColor = themeColor,
                onClick = onOpenMusicApp
            )
            Spacer(modifier = Modifier.height(20.dp))
            StandByMusicDetailsAndControls(
                formattedTime = formattedTime,
                monthDay = monthDay,
                themeColor = themeColor,
                mediaPlayback = mediaPlayback,
                isMusicActive = isMusicActive,
                calculatedPos = calculatedPos,
                onTogglePlayPause = onTogglePlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onOpenMusicApp = onOpenMusicApp,
                centered = true,
                showHeaderTime = false
            )
        }
    }
}

@Composable
private fun StandByLargeArtworkBox(
    albumBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    size: Dp,
    themeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(30.dp))
            .border(BorderStroke(1.2.dp, themeColor.copy(alpha = 0.35f)), RoundedCornerShape(30.dp))
            .background(Color(0xFF14141A))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
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
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = null,
                tint = themeColor.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
private fun StandByMusicDetailsAndControls(
    formattedTime: String,
    monthDay: String,
    themeColor: Color,
    mediaPlayback: MediaPlaybackInfo?,
    isMusicActive: Boolean,
    calculatedPos: Long,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenMusicApp: () -> Unit,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    showHeaderTime: Boolean = true
) {
    val alignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.Center
    ) {
        if (showHeaderTime) {
            Text(
                text = "$formattedTime • $monthDay",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColor
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        val appLabel = mediaPlayback?.appDisplayName?.takeIf { it.isNotBlank() } ?: "Player"
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (mediaPlayback?.isPlaying == true) "$appLabel • TOCANDO" else appLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isMusicActive) (mediaPlayback?.title ?: "Música") else "Nenhuma mídia ativa",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        )
        Text(
            text = if (isMusicActive) (mediaPlayback?.artist ?: "") else "Toque para abrir seu player favorito",
            fontSize = 14.sp,
            color = Color(0xFFA1A1AA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        val duration = mediaPlayback?.durationMs ?: 0L
        val progress = if (duration > 0L) (calculatedPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = themeColor,
            trackColor = Color.White.copy(alpha = 0.14f)
        )

        if (duration > 0L) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDurationMs(calculatedPos), fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.55f))
                Text(formatDurationMs(duration), fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.55f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSkipPrevious
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(22.dp))
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(themeColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = if (isMusicActive) onTogglePlayPause else onOpenMusicApp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mediaPlayback?.isPlaying == true) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSkipNext
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.SkipNext, contentDescription = "Próxima", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val min = totalSec / 60L
    val sec = totalSec % 60L
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}

// ==========================================
// MODO 3: MODO EVENTOS & TAREFAS
// ==========================================
@Composable
private fun StandByEventsModeContent(
    isLandscape: Boolean,
    formattedTime: String,
    dayOfWeek: String,
    monthDay: String,
    clockStyle: StandByClockStyle,
    themeColor: Color,
    upcomingEvents: List<CalendarEventInfo>,
    notesTasks: List<NoteTask>,
    onOpenCalendarApp: () -> Unit,
    onToggleNoteTask: (Long) -> Unit
) {
    val eventsCard: @Composable (Modifier) -> Unit = { mod ->
        Surface(
            modifier = mod
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF14141A),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRÓXIMOS EVENTOS",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = themeColor
                    )
                    Text(
                        text = "Abrir Agenda →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { onOpenCalendarApp() }
                    )
                }

                if (upcomingEvents.isEmpty()) {
                    Text(
                        text = "Sem compromissos para as próximas 24 horas.",
                        fontSize = 13.sp,
                        color = Color(0xFFA1A1AA)
                    )
                } else {
                    upcomingEvents.take(4).forEach { ev ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .clip(CircleShape)
                                    .background(if (ev.isOngoing) Color(0xFF34D399) else themeColor)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (ev.isOngoing) "Agora • ${ev.timeFormatted}" else ev.timeFormatted,
                                    fontSize = 11.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                Text(
                    text = "TAREFAS (${notesTasks.count { !it.isDone }} pendentes)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = themeColor
                )

                if (notesTasks.isEmpty()) {
                    Text(
                        text = "Nenhuma tarefa cadastrada no widget de notas.",
                        fontSize = 12.5.sp,
                        color = Color(0xFFA1A1AA)
                    )
                } else {
                    notesTasks.take(5).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onToggleNoteTask(task.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (task.isDone) themeColor else Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = task.text,
                                fontSize = 13.sp,
                                color = if (task.isDone) Color.White.copy(alpha = 0.45f) else Color.White,
                                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                StandByClockBlock(
                    formattedTime = formattedTime,
                    dayOfWeek = dayOfWeek,
                    monthDay = monthDay,
                    clockStyle = clockStyle,
                    color = themeColor,
                    isMusicActive = true,
                    isLandscape = true,
                    onClick = {}
                )
            }
            eventsCard(
                Modifier
                    .weight(1.1f)
                    .fillMaxHeight(0.9f)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StandByClockBlock(
                formattedTime = formattedTime,
                dayOfWeek = dayOfWeek,
                monthDay = monthDay,
                clockStyle = clockStyle,
                color = themeColor,
                isMusicActive = true,
                isLandscape = false,
                onClick = {}
            )
            eventsCard(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

// ==========================================
// MODO 4: MODO POMODORO (TIMER DE FOCO)
// ==========================================
@Composable
private fun StandByPomodoroModeContent(
    isLandscape: Boolean,
    formattedTime: String,
    themeColor: Color,
    phase: PomodoroPhase,
    remainingSeconds: Int,
    isRunning: Boolean,
    completedCycles: Int,
    onToggleRun: () -> Unit,
    onReset: () -> Unit,
    onSelectPhase: (PomodoroPhase) -> Unit
) {
    val progress = (remainingSeconds.toFloat() / phase.durationSeconds.toFloat()).coerceIn(0f, 1f)
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timerText = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

    val ringSize = if (isLandscape) 184.dp else 210.dp

    val ringBlock: @Composable () -> Unit = {
        Box(
            modifier = Modifier.size(ringSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                drawArc(
                    color = Color.White.copy(alpha = 0.10f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = themeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = phase.label.uppercase(Locale.getDefault()),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = themeColor
                )
                Text(
                    text = timerText,
                    fontSize = if (isLandscape) 42.sp else 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                Text(
                    text = "Agora: $formattedTime",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }

    val controlsBlock: @Composable () -> Unit = {
        Column(
            horizontalAlignment = if (isLandscape) Alignment.Start else Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Seletor de Fase (Foco / Pausa / Descanso)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PomodoroPhase.entries.forEach { p ->
                    val selected = p == phase
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) themeColor.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f))
                            .border(
                                BorderStroke(1.dp, if (selected) themeColor.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.1f)),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelectPhase(p) }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = p.label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Text(
                text = "Ciclos concluídos hoje: $completedCycles",
                fontSize = 13.sp,
                color = Color(0xFFA1A1AA)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(themeColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleRun
                        )
                        .padding(horizontal = 22.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isRunning) "Pausar" else "Iniciar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onReset
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Reiniciar", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ringBlock()
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                controlsBlock()
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ringBlock()
            Spacer(modifier = Modifier.height(24.dp))
            controlsBlock()
        }
    }
}

// ==========================================
// COMPONENTES DE NOTIFICAÇÃO PERSONALIZADA STANDBY
// ==========================================
@Composable
private fun StandByLiveNotificationSpotlight(
    item: AppNotificationItem,
    themeColor: Color,
    isPrivacyHidden: Boolean,
    onReveal: () -> Unit,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF13131A).copy(alpha = 0.96f),
        border = BorderStroke(1.2.dp, themeColor.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (isPrivacyHidden) onReveal() else onOpen()
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                NotificationAppIcon(
                    packageName = item.packageName,
                    largeIcon = item.largeIcon,
                    size = 52.dp,
                    shape = RoundedCornerShape(14.dp),
                    fallbackTint = themeColor
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.appName.uppercase(Locale.getDefault()),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = themeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("•", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                        Text("Agora", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isPrivacyHidden) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Outlined.Lock, null, tint = themeColor, modifier = Modifier.size(13.dp))
                            Text(
                                text = "Conteúdo oculto • Toque para revelar",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    } else if (item.text.isNotBlank()) {
                        Text(
                            text = item.text,
                            fontSize = 13.5.sp,
                            color = Color(0xFFD4D4D8),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text("Dispensar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(themeColor)
                        .clickable { if (isPrivacyHidden) onReveal() else onOpen() }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (isPrivacyHidden) Icons.Outlined.Visibility else Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isPrivacyHidden) "Revelar" else "Abrir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StandByNotificationsOverlay(
    notifications: List<AppNotificationItem>,
    themeColor: Color,
    isPrivacyEnabled: Boolean,
    onTogglePrivacy: () -> Unit,
    onNotificationClick: (AppNotificationItem) -> Unit,
    onDismissNotification: (String) -> Unit,
    onDismissAll: () -> Unit,
    onCloseOverlay: () -> Unit
) {
    var revealedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF111117).copy(alpha = 0.96f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.76f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Notifications, null, tint = themeColor, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Notificações no StandBy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isPrivacyEnabled) themeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                            .clickable { onTogglePrivacy() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPrivacyEnabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = if (isPrivacyEnabled) themeColor else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isPrivacyEnabled) "Privado" else "Visível",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    if (notifications.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { onDismissAll() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = "Limpar", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(15.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onCloseOverlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.NotificationsNone, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhuma notificação pendente", fontSize = 13.5.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(notifications, key = { it.key }) { item ->
                        val isHidden = isPrivacyEnabled && item.key !in revealedKeys
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1A1A24),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isHidden) {
                                        revealedKeys = revealedKeys + item.key
                                    } else {
                                        onNotificationClick(item)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NotificationAppIcon(
                                    packageName = item.packageName,
                                    largeIcon = item.largeIcon,
                                    size = 38.dp,
                                    fallbackTint = themeColor
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.appName} • ${item.title}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isHidden) "Toque para revelar conteúdo" else item.text.ifBlank { "Nova notificação" },
                                        fontSize = 11.5.sp,
                                        color = if (isHidden) themeColor else Color(0xFFA1A1AA),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (!item.isOngoing) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .clickable { onDismissNotification(item.key) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Dispensar", tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES DE RELÓGIO (3 ESTILOS) + CHIPS + LOGO
// ==========================================
@Composable
private fun StandByClockBlock(
    formattedTime: String,
    dayOfWeek: String,
    monthDay: String,
    clockStyle: StandByClockStyle,
    color: Color,
    isMusicActive: Boolean,
    isLandscape: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        when (clockStyle) {
            StandByClockStyle.FLIP -> {
                StandByFlipClock(
                    formattedTime = formattedTime,
                    isMusicActive = isMusicActive,
                    isLandscape = isLandscape,
                    color = color
                )
            }

            StandByClockStyle.EDITORIAL -> {
                StandByEditorialClock(
                    formattedTime = formattedTime,
                    isMusicActive = isMusicActive,
                    isLandscape = isLandscape,
                    color = color
                )
            }

            StandByClockStyle.MINIMAL -> {
                StandByMinimalClock(
                    formattedTime = formattedTime,
                    isMusicActive = isMusicActive,
                    isLandscape = isLandscape,
                    color = color
                )
            }
        }

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
    }
}

@Composable
private fun StandByEditorialClock(
    formattedTime: String,
    isMusicActive: Boolean,
    isLandscape: Boolean,
    color: Color
) {
    val sizeSp = when {
        isLandscape && !isMusicActive -> 88.sp
        isLandscape && isMusicActive -> 70.sp
        !isLandscape && !isMusicActive -> 82.sp
        else -> 64.sp
    }
    Text(
        text = formattedTime,
        style = TextStyle(
            fontSize = sizeSp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-3.5).sp,
            color = color,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    )
}

@Composable
private fun StandByMinimalClock(
    formattedTime: String,
    isMusicActive: Boolean,
    isLandscape: Boolean,
    color: Color
) {
    val sizeSp = when {
        isLandscape && !isMusicActive -> 80.sp
        isLandscape && isMusicActive -> 64.sp
        !isLandscape && !isMusicActive -> 74.sp
        else -> 58.sp
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.25f)), RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formattedTime,
            style = TextStyle(
                fontSize = sizeSp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1.5).sp,
                color = color,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

/**
 * Badge do Ícone do Tessera Launcher no cabeçalho.
 */
@Composable
private fun StandByTesseraBadge(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val context = LocalContext.current
    val launcherIconBitmap = remember(context) {
        runCatching {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(context.packageName)
            val px = 96
            val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, px, px)
            drawable.draw(canvas)
            bmp
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        val iconSize = size * 0.64f
        if (launcherIconBitmap != null && !launcherIconBitmap.isRecycled) {
            Image(
                bitmap = launcherIconBitmap.asImageBitmap(),
                contentDescription = "Tessera Launcher",
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_tessera_header_logo),
                contentDescription = "Tessera Launcher",
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
            )
        }
    }
}

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
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isNightTime) Icons.Outlined.DarkMode else Icons.Outlined.WbSunny,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "$tempDigits°",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "•",
                fontSize = 10.sp,
                color = Color(0xFF707078)
            )
            Text(
                text = condDisplay,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA0A0A8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StandByBatteryChip(
    batteryPercentage: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val clampedPercentage = batteryPercentage.coerceIn(0, 100)
    val isLowBattery = clampedPercentage <= 20 && !isCharging

    val batteryTint = when {
        isCharging -> Color(0xFF34D399)
        isLowBattery -> Color(0xFFF87171)
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
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (isCharging) {
                    Icons.Outlined.BatteryChargingFull
                } else {
                    Icons.Outlined.BatteryStd
                },
                contentDescription = if (isCharging) "Carregando" else "Bateria",
                tint = batteryTint,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "$clampedPercentage%",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isLowBattery) batteryTint else Color.White
            )
        }
    }
}

@Composable
private fun StandByAlbumCard(
    mediaPlayback: MediaPlaybackInfo,
    cardSize: Dp,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
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
                            listOf(Color(0xFF24242A), Color(0xFF141418), Color(0xFF0A0A0D))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x44000000), Color(0xF0000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
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
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.White
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Column {
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
                        color = Color(0xFFA0A0A8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSkipPrevious
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
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

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSkipNext
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.SkipNext, contentDescription = "Próxima", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Relógio Mecânico Flip 3D (Estilo Placar / Split-Flap).
 */
@Composable
fun StandByFlipClock(
    formattedTime: String,
    isMusicActive: Boolean,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
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

    val rotX = if (isAnimating) {
        val p = flipProgress.value
        if (p < 0.5f) -90f * (p * 2f) else 90f * (1f - (p - 0.5f) * 2f)
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
                    listOf(Color(0xFF202026), Color(0xFF16161A), Color(0xFF0F0F12))
                )
            )
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), shape),
        contentAlignment = Alignment.Center
    ) {
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent))
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.12f), Color.Transparent))
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.Center)
                .background(Color(0xFF08080B))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.6.dp)
                .align(Alignment.Center)
                .offset(y = 1.dp)
                .background(Color.White.copy(alpha = 0.07f))
        )

        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 6.dp)
                .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                .background(Color(0xFF060608))
                .align(Alignment.CenterStart)
        )

        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 6.dp)
                .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                .background(Color(0xFF060608))
                .align(Alignment.CenterEnd)
        )

        if (shadowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = shadowAlpha))
            )
        }
    }
}
