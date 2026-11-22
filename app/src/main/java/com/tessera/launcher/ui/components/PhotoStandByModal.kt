package com.tessera.launcher.ui.components

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.model.AppNotificationItem
import com.tessera.launcher.data.service.MediaPlaybackInfo
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class StandByViewMode(val key: String, val label: String, val icon: ImageVector) {
    CLOCK_TALL("CLOCK_TALL", "Relógio", Icons.Outlined.Schedule),
    DASHBOARD("DASHBOARD", "Painel", Icons.Outlined.GridView),
    PASTEL_MUSIC("PASTEL_MUSIC", "Relógio & Música", Icons.Outlined.Palette),
    FULL_MUSIC("FULL_MUSIC", "Música", Icons.Outlined.MusicNote),
    AGENDA("AGENDA", "Agenda", Icons.Outlined.Event),
    PHOTO("PHOTO", "Foto", Icons.Outlined.Photo);

    companion object {
        fun fromKey(key: String): StandByViewMode {
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.let { return it }
            return when (key.uppercase(Locale.ROOT)) {
                "DUAL" -> DASHBOARD
                "MUSIC" -> FULL_MUSIC
                "EVENTS" -> AGENDA
                "POMODORO" -> CLOCK_TALL
                else -> CLOCK_TALL
            }
        }
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

/**
 * Modo StandBy Pro (Ambient Smart Display — Navegação por Swipe Horizontal).
 * - 6 telas limpas acessadas via swipe horizontal:
 *   1. Relógio Gigante Condensado + Data e Pílula de Clima
 *   2. Dashboard Inteligente (Saudação, Relógio, Frase, Barra Solar + 4 Cards)
 *   3. Relógio Pastel Sobreposto + Card de Música com Capa
 *   4. Player de Música Fullscreen com Fundo Ambiente
 *   5. Agenda Multi-dias Minimalista
 *   6. Porta-Retratos de Foto
 * - Notificações em Tela Cheia exibindo o conteúdo completo da mensagem ao chegar
 * - Suporte automático ao sensor de orientação (Paisagem / Retrato) + Imersivo + Anti Burn-in OLED
 * - Saída via gesto/botão Voltar ou duplo toque no fundo
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
    recentNotifications: List<AppNotificationItem> = emptyList(),
    upcomingCalendarEvents: List<CalendarEventInfo> = emptyList(),
    notesTasks: List<NoteTask> = emptyList(),
    standByViewMode: String = "CLOCK_TALL",
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
    onSeekTo: (Long) -> Unit = {},
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
    val coroutineScope = rememberCoroutineScope()
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val activeMode = StandByViewMode.fromKey(standByViewMode)
    val activeColorPreset = StandByColorPreset.fromKey(standByColorPreset)
    val themeColor = activeColorPreset.resolveColor(accentColor)

    // Safe area mínima para câmera frontal (display cutout) mantendo o visual 100% limpo e imersivo
    val cutoutPadding = WindowInsets.displayCutout.asPaddingValues()
    val safeTop = cutoutPadding.calculateTopPadding()
    val safeStart = cutoutPadding.calculateLeftPadding(layoutDirection)
    val safeEnd = cutoutPadding.calculateRightPadding(layoutDirection)
    val safeBottom = cutoutPadding.calculateBottomPadding()

    // Proteção Anti Burn-in OLED (micro-deslocamento sutil a cada minuto)
    val burnInOffset = remember(formattedTime) {
        val h = formattedTime.hashCode()
        val dx = ((h % 5) - 2).dp
        val dy = (((h / 5) % 5) - 2).dp
        dx to dy
    }

    // Motor de Notificação em Tela Cheia (detecta novas notificações e atualizações de mensagens)
    var fullscreenNotification by remember { mutableStateOf<AppNotificationItem?>(null) }
    var isFullscreenNotifRevealed by remember { mutableStateOf(false) }
    var seenNotificationSignatures by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var initializedSnapshots by remember { mutableStateOf(false) }

    // Painel discreto de Configurações / Histórico de Notificações (via engrenagem na tela Agenda)
    var isQuickSettingsOpen by remember { mutableStateOf(false) }
    var isNotificationHistoryOpen by remember { mutableStateOf(false) }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            seenNotificationSignatures = recentNotifications.associate { it.key to notificationSignature(it) }
            initializedSnapshots = true
            fullscreenNotification = null
            isQuickSettingsOpen = false
            isNotificationHistoryOpen = false
        } else {
            initializedSnapshots = false
            fullscreenNotification = null
            isQuickSettingsOpen = false
            isNotificationHistoryOpen = false
        }
    }

    LaunchedEffect(recentNotifications, isOpen, initializedSnapshots) {
        if (!isOpen || !initializedSnapshots) return@LaunchedEffect
        val incoming = recentNotifications
            .filterNot { it.isOngoing }
            .firstOrNull { item ->
                val previousSig = seenNotificationSignatures[item.key]
                previousSig == null || previousSig != notificationSignature(item)
            }

        // Atualiza assinaturas conhecidas para todas as notificações atuais
        val updatedMap = seenNotificationSignatures.toMutableMap()
        recentNotifications.forEach { updatedMap[it.key] = notificationSignature(it) }
        seenNotificationSignatures = updatedMap

        if (incoming != null) {
            fullscreenNotification = incoming
            isFullscreenNotifRevealed = !isNotificationPrivacyEnabled
        } else if (fullscreenNotification != null && recentNotifications.none { it.key == fullscreenNotification?.key }) {
            fullscreenNotification = null
        }
    }

    // Intercepta o botão/gesto Voltar para fechar alertas/configurações antes de sair do StandBy
    BackHandler(enabled = isOpen) {
        when {
            fullscreenNotification != null -> fullscreenNotification = null
            isNotificationHistoryOpen -> isNotificationHistoryOpen = false
            isQuickSettingsOpen -> isQuickSettingsOpen = false
            else -> onDismiss()
        }
    }

    // Rotação automática por sensor + Keep Screen On + Barras do sistema ocultas (modo imersivo)
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
            val window = activity.window
            val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
            runCatching {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller?.hide(WindowInsetsCompat.Type.systemBars())
            }
            onDispose {
                runCatching {
                    activity.requestedOrientation = previousOrientation
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    controller?.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        } else {
            onDispose {}
        }
    }

    // Carrega o bitmap da foto do usuário em Dispatchers.IO
    val photoBitmap by produceState<Bitmap?>(initialValue = null, photoUriString, isOpen) {
        value = if (isOpen && !photoUriString.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                com.tessera.launcher.data.helper.loadSampledPhotoBitmap(
                    context = context,
                    photoUriOrPath = photoUriString,
                    maxDimensionPx = 1600
                )
            }
        } else {
            null
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(240)) + scaleIn(
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            initialScale = 0.92f
        ),
        exit = fadeOut(tween(180)) + scaleOut(
            animationSpec = tween(180),
            targetScale = 0.94f
        )
    ) {
        val pages = StandByViewMode.entries
        val pagerState = rememberPagerState(
            initialPage = activeMode.ordinal,
            pageCount = { pages.size }
        )

        // Sincroniza mudança de página via swipe horizontal com a preferência persistida
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }
                .distinctUntilChanged()
                .collect { pageIndex ->
                    val mode = pages.getOrNull(pageIndex) ?: return@collect
                    if (!mode.key.equals(standByViewMode, ignoreCase = true)) {
                        onSelectViewMode(mode.key)
                    }
                }
        }

        // Sincroniza caso o modo mude externamente
        LaunchedEffect(activeMode) {
            if (pagerState.currentPage != activeMode.ordinal && !pagerState.isScrollInProgress) {
                pagerState.scrollToPage(activeMode.ordinal)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            when {
                                fullscreenNotification != null -> fullscreenNotification = null
                                isNotificationHistoryOpen -> isNotificationHistoryOpen = false
                                isQuickSettingsOpen -> isQuickSettingsOpen = false
                                else -> onDismiss()
                            }
                        }
                    )
                }
        ) {
            // =================================================================
            // NAVEGAÇÃO POR SWIPE HORIZONTAL ENTRE AS 6 TELAS DO STANDBY
            // =================================================================
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = burnInOffset.first, y = burnInOffset.second)
            ) { pageIndex ->
                val pageMode = pages[pageIndex]
                val needsCutoutPadding = pageMode != StandByViewMode.FULL_MUSIC && pageMode != StandByViewMode.PHOTO
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (needsCutoutPadding) {
                                Modifier.padding(
                                    top = safeTop,
                                    start = safeStart,
                                    end = safeEnd,
                                    bottom = safeBottom
                                )
                            } else {
                                Modifier
                            }
                        )
                ) {
                    when (pageMode) {
                        StandByViewMode.CLOCK_TALL -> {
                            StandByTallClockPage(
                                isLandscape = isLandscape,
                                formattedTime = formattedTime,
                                weatherTemp = weatherTemp,
                                weatherCondition = weatherCondition,
                                accentColor = accentColor,
                                activeColorPreset = activeColorPreset,
                                onCycleColorPreset = { onSelectColorPreset(activeColorPreset.next().key) }
                            )
                        }

                        StandByViewMode.DASHBOARD -> {
                            StandByDashboardPage(
                                isLandscape = isLandscape,
                                formattedTime = formattedTime,
                                weatherTemp = weatherTemp,
                                weatherCondition = weatherCondition,
                                batteryPercentage = batteryPercentage,
                                isCharging = isCharging,
                                mediaPlayback = mediaPlayback,
                                upcomingEvents = upcomingCalendarEvents,
                                onTogglePlayPause = onTogglePlayPause,
                                onOpenMusicApp = onOpenMusicApp,
                                onOpenCalendarApp = onOpenCalendarApp
                            )
                        }

                        StandByViewMode.PASTEL_MUSIC -> {
                            StandByPastelMusicPage(
                                isLandscape = isLandscape,
                                formattedTime = formattedTime,
                                mediaPlayback = mediaPlayback,
                                onTogglePlayPause = onTogglePlayPause,
                                onSkipNext = onSkipNext,
                                onSkipPrevious = onSkipPrevious,
                                onSeekTo = onSeekTo,
                                onOpenMusicApp = onOpenMusicApp
                            )
                        }

                        StandByViewMode.FULL_MUSIC -> {
                            StandByFullMusicPage(
                                isLandscape = isLandscape,
                                mediaPlayback = mediaPlayback,
                                onTogglePlayPause = onTogglePlayPause,
                                onSkipNext = onSkipNext,
                                onSkipPrevious = onSkipPrevious,
                                onSeekTo = onSeekTo,
                                onOpenMusicApp = onOpenMusicApp
                            )
                        }

                        StandByViewMode.AGENDA -> {
                            StandByAgendaPage(
                                isLandscape = isLandscape,
                                formattedTime = formattedTime,
                                upcomingEvents = upcomingCalendarEvents,
                                onOpenCalendarApp = onOpenCalendarApp,
                                onOpenStandBySettings = { isQuickSettingsOpen = true }
                            )
                        }

                        StandByViewMode.PHOTO -> {
                            StandByPhotoPage(
                                isLandscape = isLandscape,
                                formattedTime = formattedTime,
                                bitmap = photoBitmap,
                                accentColor = accentColor,
                                activeColorPreset = activeColorPreset
                            )
                        }
                    }
                }
            }

            // =================================================================
            // INDICADORES DISCRETOS NA BORDA DIREITA (IGUAL AOS PRINTS 2 E 5)
            // =================================================================
            if (fullscreenNotification == null && !isQuickSettingsOpen && !isNotificationHistoryOpen) {
                StandByEdgeDotsIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    onSelectPage = { target ->
                        coroutineScope.launch { pagerState.animateScrollToPage(target) }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = safeEnd + 6.dp)
                )
            }

            // =================================================================
            // NOTIFICAÇÃO EM TELA CHEIA COM O CONTEÚDO COMPLETO DA MENSAGEM
            // =================================================================
            AnimatedVisibility(
                visible = fullscreenNotification != null,
                enter = fadeIn(tween(240)) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.96f)
            ) {
                fullscreenNotification?.let { notif ->
                    StandByFullscreenNotificationAlert(
                        item = notif,
                        formattedTime = formattedTime,
                        themeColor = if (activeColorPreset == StandByColorPreset.WHITE) Color(0xFFD6C3B3) else themeColor,
                        isLandscape = isLandscape,
                        isPrivacyHidden = isNotificationPrivacyEnabled && !isFullscreenNotifRevealed,
                        onReveal = { isFullscreenNotifRevealed = true },
                        onOpen = {
                            onNotificationClick(notif)
                            fullscreenNotification = null
                        },
                        onDismissNotification = {
                            onDismissNotification(notif.key)
                            fullscreenNotification = null
                        },
                        onCloseAlert = { fullscreenNotification = null }
                    )
                }
            }

            // =================================================================
            // MENU RÁPIDO DISCRETO (ABERTO PELA ENGRENAGEM DA TELA DE AGENDA)
            // =================================================================
            AnimatedVisibility(
                visible = isQuickSettingsOpen && !isNotificationHistoryOpen && fullscreenNotification == null,
                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.94f),
                exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.94f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.72f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isQuickSettingsOpen = false }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    StandByQuickSettingsDialog(
                        activeColorPreset = activeColorPreset,
                        themeColor = themeColor,
                        isPrivacyEnabled = isNotificationPrivacyEnabled,
                        notificationCount = recentNotifications.count { !it.isOngoing },
                        onCycleColorPreset = { onSelectColorPreset(activeColorPreset.next().key) },
                        onTogglePrivacy = { onToggleNotificationPrivacy(!isNotificationPrivacyEnabled) },
                        onOpenNotifications = {
                            isQuickSettingsOpen = false
                            isNotificationHistoryOpen = true
                        },
                        onOpenCalendar = {
                            isQuickSettingsOpen = false
                            onOpenCalendarApp()
                        },
                        onExitStandBy = {
                            isQuickSettingsOpen = false
                            onDismiss()
                        },
                        onClose = { isQuickSettingsOpen = false }
                    )
                }
            }

            // =================================================================
            // HISTÓRICO DE NOTIFICAÇÕES (ACESSÍVEL PELO MENU DA ENGRENAGEM)
            // =================================================================
            AnimatedVisibility(
                visible = isNotificationHistoryOpen && fullscreenNotification == null,
                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.94f),
                exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.94f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.80f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isNotificationHistoryOpen = false }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    StandByNotificationsHistorySheet(
                        notifications = recentNotifications,
                        themeColor = if (activeColorPreset == StandByColorPreset.WHITE) Color(0xFFD6C3B3) else themeColor,
                        isPrivacyEnabled = isNotificationPrivacyEnabled,
                        onTogglePrivacy = { onToggleNotificationPrivacy(!isNotificationPrivacyEnabled) },
                        onNotificationClick = {
                            onNotificationClick(it)
                            isNotificationHistoryOpen = false
                        },
                        onDismissNotification = onDismissNotification,
                        onDismissAll = onDismissAllNotifications,
                        onCloseOverlay = { isNotificationHistoryOpen = false }
                    )
                }
            }
        }
    }
}

private fun notificationSignature(item: AppNotificationItem): String =
    "${item.timestamp}|${item.title}|${item.text}"

@Composable
private fun StandByEdgeDotsIndicator(
    pageCount: Int,
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(pageCount) { idx ->
            val selected = idx == currentPage
            val dotSize by animateDpAsState(
                targetValue = if (selected) 7.dp else 5.5.dp,
                animationSpec = tween(200),
                label = "StandByDotSize"
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(
                        if (selected) Color(0xFFF5EBE1) else Color(0xFF7B736C).copy(alpha = 0.65f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectPage(idx) }
                    )
            )
        }
    }
}

/**
 * Exibição de Notificação em Tela Cheia no Modo StandBy.
 * Mostra o aplicativo, remetente/título e o conteúdo completo da mensagem em tipografia ampla
 * para leitura instantânea à distância assim que a notificação chega.
 */
@Composable
private fun StandByFullscreenNotificationAlert(
    item: AppNotificationItem,
    formattedTime: String,
    themeColor: Color,
    isLandscape: Boolean,
    isPrivacyHidden: Boolean,
    onReveal: () -> Unit,
    onOpen: () -> Unit,
    onDismissNotification: () -> Unit,
    onCloseAlert: () -> Unit
) {
    val autoCloseDurationMs = 10_000
    val progressAnim = remember(item.key, item.timestamp, isPrivacyHidden) { Animatable(1f) }

    LaunchedEffect(item.key, item.timestamp, isPrivacyHidden) {
        progressAnim.snapTo(1f)
        progressAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = autoCloseDurationMs, easing = LinearEasing)
        )
        onCloseAlert()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040406))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (isPrivacyHidden) onReveal() else onCloseAlert()
                }
            )
    ) {
        // Sutil halo ambiente no topo da tela cheia
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            themeColor.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        // Barra ultrafina no topo indicando o tempo restante do alerta
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(progressAnim.value.coerceIn(0f, 1f))
                .height(3.dp)
                .background(themeColor.copy(alpha = 0.78f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isLandscape) 48.dp else 28.dp,
                    vertical = if (isLandscape) 26.dp else 36.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabeçalho: Ícone grande do App + Nome do App + AGORA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NotificationAppIcon(
                        packageName = item.packageName,
                        largeIcon = item.largeIcon,
                        size = if (isLandscape) 56.dp else 52.dp,
                        shape = RoundedCornerShape(16.dp),
                        fallbackTint = themeColor
                    )
                    Column {
                        Text(
                            text = item.appName.uppercase(Locale.getDefault()),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp,
                            color = themeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "AGORA • $formattedTime",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9E9690)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCloseAlert
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar aviso",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Corpo Principal em Tela Cheia: Título/Remetente + Conteúdo Completo da Mensagem
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = if (isLandscape) 16.dp else 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    fontSize = if (isLandscape) 34.sp else 28.sp,
                    lineHeight = if (isLandscape) 40.sp else 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isPrivacyHidden) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, themeColor.copy(alpha = 0.35f)), RoundedCornerShape(18.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onReveal
                            )
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Conteúdo oculto • Toque para ler a mensagem",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEBE4DD)
                        )
                    }
                } else {
                    val bodyText = item.text.ifBlank { "Nova notificação de ${item.appName}" }
                    Text(
                        text = bodyText,
                        fontSize = if (isLandscape) 24.sp else 21.sp,
                        lineHeight = if (isLandscape) 33.sp else 29.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFEBE4DD)
                    )
                }
            }

            // Rodapé de Ações Rápidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.09f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismissNotification
                        )
                        .padding(horizontal = 20.dp, vertical = 11.dp)
                ) {
                    Text(
                        text = "Dispensar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD6CEC8)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(themeColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = if (isPrivacyHidden) onReveal else onOpen
                        )
                        .padding(horizontal = 22.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = if (isPrivacyHidden) Icons.Outlined.Visibility else Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = if (isPrivacyHidden) "Revelar mensagem" else "Abrir",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StandByQuickSettingsDialog(
    activeColorPreset: StandByColorPreset,
    themeColor: Color,
    isPrivacyEnabled: Boolean,
    notificationCount: Int,
    onCycleColorPreset: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenCalendar: () -> Unit,
    onExitStandBy: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF141418),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth(0.84f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modo StandBy",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            StandByQuickSettingRow(
                icon = Icons.Outlined.Palette,
                title = "Cor do relógio",
                subtitle = activeColorPreset.label,
                tint = themeColor,
                onClick = onCycleColorPreset
            )

            StandByQuickSettingRow(
                icon = if (isPrivacyEnabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                title = "Privacidade nas notificações",
                subtitle = if (isPrivacyEnabled) "Ocultar conteúdo até tocar" else "Mostrar mensagem completa em tela cheia",
                tint = if (isPrivacyEnabled) themeColor else Color.White,
                onClick = onTogglePrivacy
            )

            StandByQuickSettingRow(
                icon = Icons.Outlined.Notifications,
                title = "Notificações recentes",
                subtitle = if (notificationCount > 0) "$notificationCount ativas" else "Nenhuma pendente",
                tint = Color(0xFFD6C3B3),
                onClick = onOpenNotifications
            )

            StandByQuickSettingRow(
                icon = Icons.Outlined.CalendarToday,
                title = "Abrir aplicativo de Agenda",
                subtitle = "Ver ou criar compromissos",
                tint = Color(0xFF9EB7F2),
                onClick = onOpenCalendar
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable(onClick = onExitStandBy)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sair do Modo StandBy",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF06E65)
                )
            }
        }
    }
}

@Composable
private fun StandByQuickSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF9E9690), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun StandByNotificationsHistorySheet(
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
            .fillMaxWidth(0.90f)
            .fillMaxHeight(0.78f)
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
                        text = "Notificações",
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
                            .clickable(onClick = onTogglePrivacy)
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
                                .clickable(onClick = onDismissAll)
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
                            .clickable(onClick = onCloseOverlay),
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
                                        fontSize = 12.sp,
                                        color = if (isHidden) themeColor else Color(0xFFA1A1AA),
                                        maxLines = 3,
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
