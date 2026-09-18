package com.tessera.launcher.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.launcher.data.helper.ContactInfo
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.components.AiResponseCard
import com.tessera.launcher.ui.components.Alphabet
import com.tessera.launcher.ui.components.AlphabetScroller
import com.tessera.launcher.ui.components.AppCategoriesBar
import com.tessera.launcher.ui.components.AppContextMenu
import com.tessera.launcher.ui.components.AppListEmptyState
import com.tessera.launcher.ui.components.AppListErrorState
import com.tessera.launcher.ui.components.AppListItem
import com.tessera.launcher.ui.components.AppListSkeleton
import com.tessera.launcher.ui.components.CalculatorCard
import com.tessera.launcher.ui.components.FolderSearchCard
import com.tessera.launcher.ui.components.BatteryConfigBottomSheet
import com.tessera.launcher.ui.components.CalendarConfigBottomSheet
import com.tessera.launcher.ui.components.FolderViewBottomSheet
import com.tessera.launcher.ui.components.MediaConfigBottomSheet
import com.tessera.launcher.ui.components.NotesConfigBottomSheet
import com.tessera.launcher.ui.components.NotesTasksManagerBottomSheet
import com.tessera.launcher.ui.components.PhotoWidget
import com.tessera.launcher.ui.components.SearchExternalActions
import com.tessera.launcher.ui.components.SearchMorphingDock
import com.tessera.launcher.ui.components.CommandsChipsRow
import com.tessera.launcher.ui.components.SmartDockRow
import com.tessera.launcher.ui.components.WeatherConfigBottomSheet
import com.tessera.launcher.ui.components.WidgetsPanel
import com.tessera.launcher.ui.state.AppFolder
import com.tessera.launcher.ui.state.AppsListState
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.state.WidgetConfigType
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.CharcoalBackground
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkBackgroundTranslucent
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.tessera.launcher.ui.state.FeedState

private fun Modifier.fadingEdges(
    topFade: Dp = 32.dp,
    bottomFade: Dp = 44.dp
): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val topFadePx = topFade.toPx()
        val bottomFadePx = bottomFade.toPx()
        if (topFadePx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topFadePx
                ),
                blendMode = BlendMode.DstIn
            )
        }
        if (bottomFadePx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - bottomFadePx,
                    endY = size.height
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onPickPhoto: () -> Unit,
    onPickWallpaper: () -> Unit = {},
    onRequestCalendarPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wallpaperBitmap by viewModel.wallpaperBitmap.collectAsStateWithLifecycle()

    val isDrawerOrSearchOpen = uiState.isDrawerOpen || uiState.isSearchExpanded || uiState.searchQuery.isNotEmpty()
    val isOtherOpen = uiState.isSettingsOpen || uiState.isFeedOpen

    val targetBlurDp = when {
        isDrawerOrSearchOpen -> {
            val base = if (uiState.homeWallpaperBlur > 0) {
                ((uiState.homeWallpaperBlur / 100f) * 20f + 25f).dp
            } else {
                28.dp
            }
            val extra = if (uiState.isDrawerGlassEnabled && uiState.drawerGlassOpacity > 0) {
                ((uiState.drawerGlassOpacity / 100f) * 20f).dp
            } else {
                12.dp
            }
            (base + extra).coerceIn(25.dp, 65.dp)
        }
        isOtherOpen -> 25.dp
        else -> {
            if (uiState.homeWallpaperBlur <= 0) 0.dp
            else ((uiState.homeWallpaperBlur / 100f) * 40f).dp
        }
    }

    val animatedBlurDp by animateDpAsState(
        targetValue = targetBlurDp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "wallpaperBlurAnimation"
    )

    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }
    var isNotesTasksOpen by remember { mutableStateOf(false) }
    var folderToView by remember { mutableStateOf<AppFolder?>(null) }
    val haptic = LocalHapticFeedback.current

    val isRestingHomeScreen = !uiState.isDrawerOpen &&
        !uiState.isSettingsOpen &&
        !uiState.isFeedOpen &&
        !uiState.isSearchExpanded &&
        uiState.searchQuery.isEmpty() &&
        folderToView == null &&
        selectedAppForMenu == null

    val allApps = (uiState.appsState as? AppsListState.Success)?.apps ?: emptyList()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationAccess(context)
                viewModel.refreshCalendarAndPermissions()
                viewModel.refreshWeather()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = true) {
        if (viewModel.handleFeedBackPress()) {
            return@BackHandler
        }
        val handled = viewModel.handleBackPress()
        if (handled) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // Abertura automática e confiável do teclado ao abrir busca ou drawer
    LaunchedEffect(uiState.isSearchExpanded, uiState.isDrawerOpen) {
        if ((uiState.isSearchExpanded || uiState.isDrawerOpen) && uiState.isAutoOpenKeyboard) {
            delay(90)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {
            }
        } else if (!uiState.isSearchExpanded && !uiState.isDrawerOpen) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // Redefinição de scroll para o início da lista (letra A) ao abrir a gaveta ou limpar a busca
    val isAppsReady = uiState.appsState is AppsListState.Success
    LaunchedEffect(uiState.isDrawerOpen, uiState.searchQuery.isEmpty(), isAppsReady) {
        if (uiState.isDrawerOpen && uiState.searchQuery.isEmpty() && isAppsReady) {
            val targetIndex = uiState.letterIndexMap['A'] ?: 0
            listState.scrollToItem(targetIndex)
        }
    }

    // Redefinição imediata do scroll para o topo ao digitar qualquer termo na pesquisa
    LaunchedEffect(uiState.searchQuery) {
        if (uiState.searchQuery.isNotBlank()) {
            listState.scrollToItem(0)
        }
    }

    val statusBarTopPadding = if (uiState.isShowStatusBarEnabled) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    // Pasta correspondente à busca (se houver)
    val matchingFolder = remember(uiState.searchQuery, uiState.appFolders) {
        val query = uiState.searchQuery.trim().lowercase()
        if (query.isNotEmpty()) {
            uiState.appFolders.firstOrNull { it.name.lowercase().contains(query) }
        } else {
            null
        }
    }

    val baseBackgroundColor = if (uiState.isSystemWallpaperEnabled) {
        Color.Transparent
    } else {
        when (uiState.themeMode) {
            "amoled" -> AmoledBlack
            "charcoal" -> CharcoalBackground
            "light" -> LightBackground
            else -> if (uiState.isAmoledMode) DarkBackground else DarkBackgroundTranslucent
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackgroundColor)
    ) {
        // Camada 0: Papel de Parede da Tela Inicial com Borrão Dinâmico Acelerado por GPU
        if (uiState.isSystemWallpaperEnabled) {
            if (wallpaperBitmap != null) {
                Image(
                    bitmap = wallpaperBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (animatedBlurDp > 0.dp) {
                                Modifier
                                    .graphicsLayer {
                                        scaleX = 1.10f
                                        scaleY = 1.10f
                                    }
                                    .blur(radius = animatedBlurDp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            } else {
                                Modifier
                            }
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            // Camada de Escurecimento / Tint Suave para Legibilidade dos Widgets e Textos
            val restingDimAlpha = ((uiState.homeWallpaperBlur / 100f) * 0.40f).coerceIn(0f, 0.45f)
            val effectiveAlpha = if (wallpaperBitmap != null) {
                restingDimAlpha
            } else {
                ((uiState.homeWallpaperBlur / 100f) * 0.50f).coerceIn(0f, 0.65f)
            }
            if (effectiveAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (uiState.isLightMode) Color.White.copy(alpha = effectiveAlpha * 0.5f)
                            else Color.Black.copy(alpha = effectiveAlpha)
                        )
                )
            }
        }

        // Camada Principal da Launcher (com padding da barra de status e gestos)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarTopPadding)
                // Gestos de Toque: Toque duplo e Manter pressionado (Restritos à Tela Inicial Limpa)
                .pointerInput(
                isRestingHomeScreen,
                uiState.isDoubleTapEnabled,
                uiState.doubleTapAction,
                uiState.isHoldEnabled,
                uiState.holdAction
            ) {
                if (!isRestingHomeScreen) return@pointerInput
                detectTapGestures(
                    onDoubleTap = {
                        if (isRestingHomeScreen && uiState.isDoubleTapEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.executeGestureAction(uiState.doubleTapAction, context)
                        }
                    },
                    onLongPress = {
                        if (isRestingHomeScreen && uiState.isHoldEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.executeGestureAction(uiState.holdAction, context)
                        }
                    }
                )
            }
            // Gestos com Dois Dedos (Swipe com 2 dedos, Pinça para dentro/fora e Toque com 2 dedos)
            .pointerInput(
                isRestingHomeScreen,
                uiState.isSwipeDownTwoFingersEnabled,
                uiState.swipeDownTwoFingersAction,
                uiState.isSwipeUpTwoFingersEnabled,
                uiState.swipeUpTwoFingersAction,
                uiState.isPinchInEnabled,
                uiState.pinchInAction,
                uiState.isPinchOutEnabled,
                uiState.pinchOutAction,
                uiState.isDoubleFingerTapEnabled,
                uiState.doubleFingerTapAction
            ) {
                if (!isRestingHomeScreen) return@pointerInput
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    if (!isRestingHomeScreen) return@awaitEachGesture
                    var hadTwoPointers = false
                    var initialDistance = 0f
                    var lastDistance = 0f
                    var initialCenterY = 0f
                    var lastCenterY = 0f
                    val startTime = System.currentTimeMillis()

                    do {
                        val event = awaitPointerEvent()
                        val activePointers = event.changes.filter { it.pressed }
                        if (activePointers.size >= 2) {
                            hadTwoPointers = true
                            val p1 = activePointers[0].position
                            val p2 = activePointers[1].position
                            val currentDist = kotlin.math.hypot(p1.x - p2.x, p1.y - p2.y)
                            val currentCenter = (p1.y + p2.y) / 2f

                            if (initialDistance == 0f) {
                                initialDistance = currentDist
                                initialCenterY = currentCenter
                            }
                            lastDistance = currentDist
                            lastCenterY = currentCenter
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    if (hadTwoPointers && initialDistance > 0f) {
                        val duration = System.currentTimeMillis() - startTime
                        val deltaY = lastCenterY - initialCenterY
                        val distanceDiff = lastDistance - initialDistance
                        val distanceRatio = if (initialDistance > 0f) lastDistance / initialDistance else 1f

                        when {
                            // Pinçar para dentro (Pinch in)
                            distanceRatio < 0.75f && distanceDiff < -40f && uiState.isPinchInEnabled -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.executeGestureAction(uiState.pinchInAction, context)
                            }
                            // Pinçar para fora (Pinch out)
                            distanceRatio > 1.30f && distanceDiff > 40f && uiState.isPinchOutEnabled -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.executeGestureAction(uiState.pinchOutAction, context)
                            }
                            // Deslizar 2 dedos para cima
                            deltaY < -50f && kotlin.math.abs(deltaY) > kotlin.math.abs(distanceDiff) && uiState.isSwipeUpTwoFingersEnabled -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.executeGestureAction(uiState.swipeUpTwoFingersAction, context)
                            }
                            // Deslizar 2 dedos para baixo
                            deltaY > 50f && kotlin.math.abs(deltaY) > kotlin.math.abs(distanceDiff) && uiState.isSwipeDownTwoFingersEnabled -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.executeGestureAction(uiState.swipeDownTwoFingersAction, context)
                            }
                            // Toque com dois dedos (rápido e sem grande deslocamento)
                            duration < 350 && kotlin.math.abs(deltaY) < 25f && kotlin.math.abs(distanceDiff) < 25f && uiState.isDoubleFingerTapEnabled -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.executeGestureAction(uiState.doubleFingerTapAction, context)
                            }
                        }
                    }
                }
            }
            // Gestos de Deslizar: Cima, Baixo, Esquerda, Direita (Restritos à Tela Inicial Limpa)
            .pointerInput(
                isRestingHomeScreen,
                uiState.isSwipeDownEnabled,
                uiState.swipeDownAction,
                uiState.isSwipeUpEnabled,
                uiState.swipeUpAction,
                uiState.isSwipeLeftEnabled,
                uiState.swipeLeftAction,
                uiState.isSwipeRightEnabled,
                uiState.swipeRightAction
            ) {
                if (!isRestingHomeScreen) return@pointerInput
                var totalDragX = 0f
                var totalDragY = 0f
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        if (!isRestingHomeScreen) return@detectDragGestures
                        val absX = abs(totalDragX)
                        val absY = abs(totalDragY)
                        if (absY > absX && absY > 55f) {
                            if (totalDragY < 0) { // Deslizar para CIMA
                                if (uiState.isSwipeUpEnabled &&
                                    uiState.swipeUpAction != "open_drawer" && uiState.swipeUpAction != "open_keyboard"
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.executeGestureAction(uiState.swipeUpAction, context)
                                } else {
                                    viewModel.expandSearch()
                                    viewModel.openDrawer()
                                    try {
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    } catch (_: Exception) {}
                                }
                            } else { // Deslizar para BAIXO
                                if (uiState.isSwipeDownEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.executeGestureAction(uiState.swipeDownAction, context)
                                }
                            }
                        } else if (absX > absY && absX > 55f) {
                            if (totalDragX < 0) { // Deslizar para a ESQUERDA
                                if (uiState.isSwipeLeftEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.executeGestureAction(uiState.swipeLeftAction, context)
                                }
                            } else { // Deslizar para a DIREITA
                                if (uiState.isSwipeRightEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.executeGestureAction(uiState.swipeRightAction, context)
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    }
                )
            }
    ) {

        // Toque de Descarte Suave: Quando a busca estiver expandida na home, toque fora recolhe a busca e esconde o teclado
        if (uiState.isSearchExpanded && !uiState.isDrawerOpen && uiState.searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.collapseSearch()
                        }
                    )
            )
        }

        // Centro da Tela Inicial: Moldura de Foto Minimalista
        if (!uiState.isSearchExpanded && !uiState.isDrawerOpen && uiState.searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                if (uiState.isPhotoWidgetEnabled) {
                    PhotoWidget(
                        photoUriString = uiState.photoWidgetUri,
                        onPickPhoto = { onPickPhoto() },
                        onRemovePhoto = { viewModel.setPhotoWidgetUri(null) },
                        isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 88.dp)
                    )
                }
            }
        }

        // Camada de Foco e Desfoque de Fundo da Gaveta (Frosted Glass / Atmospheric Scrim)
        val drawerBackdropAlpha = if (uiState.isDrawerGlassEnabled) {
            val baseMin = if (uiState.isLightMode) 0.45f else (if (uiState.isSystemWallpaperEnabled) 0.48f else 0.68f)
            val baseMax = if (uiState.isLightMode) 0.75f else (if (uiState.isSystemWallpaperEnabled) 0.74f else 0.88f)
            val factor = (uiState.drawerGlassOpacity.coerceIn(0, 100) / 100f)
            baseMin + (baseMax - baseMin) * factor
        } else if (uiState.isAmoledMode) {
            1f
        } else {
            if (uiState.isLightMode) 0.76f else 0.82f
        }

        val drawerBackdropBrush = remember(uiState.isLightMode, drawerBackdropAlpha) {
            if (uiState.isLightMode) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF2F2F7).copy(alpha = (drawerBackdropAlpha * 0.88f).coerceAtMost(1f)),
                        Color(0xFFE5E5EA).copy(alpha = drawerBackdropAlpha)
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F14).copy(alpha = (drawerBackdropAlpha * 0.92f).coerceAtMost(1f)),
                        Color.Black.copy(alpha = drawerBackdropAlpha)
                    )
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isSearchExpanded || uiState.isDrawerOpen || uiState.searchQuery.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(drawerBackdropBrush)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            viewModel.collapseSearch()
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
            )
        }

        // Gaveta Vertical de Aplicativos & Resultados
        AnimatedVisibility(
            visible = uiState.isDrawerOpen || uiState.searchQuery.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { it / 6 },
                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                    slideOutVertically(
                        targetOffsetY = { it / 6 },
                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                    ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .padding(bottom = if (uiState.isWidgetExpanded && !uiState.isDrawerOpen) 310.dp else 84.dp)
            ) {
                val drawerTopPadding = if (uiState.isShowStatusBarEnabled) 8.dp else 32.dp
                Spacer(modifier = Modifier.height(drawerTopPadding))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        when (val state = uiState.appsState) {
                            is AppsListState.Loading -> AppListSkeleton()
                            is AppsListState.Empty -> {
                                val isAiSearchActive = uiState.isAiSearchEnabled && (
                                    uiState.isAiSearchLoading ||
                                    uiState.aiSearchResponse != null ||
                                    uiState.aiSearchError != null ||
                                    uiState.searchQuery.startsWith("@ai", ignoreCase = true) ||
                                    uiState.searchQuery.startsWith("@gemini", ignoreCase = true) ||
                                    uiState.searchQuery.startsWith("@groq", ignoreCase = true)
                                )

                                // Se a busca tiver IA, contatos, arquivos, calculadora ou pasta, exibe esses resultados
                                if (isAiSearchActive ||
                                    uiState.calculatorResult != null ||
                                    uiState.matchingContacts.isNotEmpty() ||
                                    matchingFolder != null
                                ) {
                                     LazyColumn(
                                         state = listState,
                                         modifier = Modifier.fillMaxSize().fadingEdges(topFade = 24.dp, bottomFade = 36.dp),
                                         verticalArrangement = Arrangement.Bottom
                                     ) {
                                        // Resposta IA (Gemini / Groq)
                                        if (isAiSearchActive) {
                                            item(key = "ai_response_card") {
                                                AiResponseCard(
                                                    query = uiState.searchQuery,
                                                    response = uiState.aiSearchResponse,
                                                    isLoading = uiState.isAiSearchLoading,
                                                    error = uiState.aiSearchError,
                                                    provider = uiState.aiProvider,
                                                    onRetry = { viewModel.executeAiSearch() },
                                                    onOpenSettings = {
                                                        viewModel.openSettings()
                                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.SEARCH)
                                                    },
                                                    isLightMode = uiState.isLightMode,
                                                    isAmoledMode = uiState.isAmoledMode,
                                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                                )
                                            }
                                        }

                                        // Calculadora (@calc)
                                        uiState.calculatorResult?.let { result ->
                                            item(key = "calc_result") {
                                                CalculatorCard(
                                                    query = uiState.searchQuery,
                                                    result = result,
                                                    isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode,
                                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                                )
                                            }
                                        }

                                        // Contatos
                                        if (uiState.calculatorResult == null && !isAiSearchActive && uiState.matchingContacts.isNotEmpty()) {
                                            item(key = "contacts_header") {
                                                Text(
                                                    text = "CONTATOS",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.sp
                                                    ),
                                                    color = TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                                )
                                            }
                                            items(
                                                items = uiState.matchingContacts,
                                                key = { "contact_${it.name}_${it.phoneNumber}" }
                                            ) { contact ->
                                                ContactListItem(
                                                    contact = contact,
                                                    onClick = { viewModel.callContact(contact.phoneNumber) }
                                                )
                                            }
                                        }

                                        // Cartão de Pasta encontrada na busca
                                        matchingFolder?.let { folder ->
                                            item(key = "folder_card_${folder.id}") {
                                                FolderSearchCard(
                                                    folder = folder,
                                                    allApps = allApps,
                                                    onAppClick = { app -> viewModel.launchApp(app.packageName) },
                                                    onAppLongClick = { app -> selectedAppForMenu = app },
                                                    onFolderClick = { f -> folderToView = f },
                                                    isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode
                                                )
                                            }
                                        }

                                        // Barra externa de busca
                                        if (uiState.searchQuery.isNotEmpty() && uiState.isWebSearchEnabled && !isAiSearchActive) {
                                            item {
                                                SearchExternalActions(
                                                    query = uiState.searchQuery,
                                                    inAppSearchPackages = uiState.inAppSearchPackages,
                                                    isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode,
                                                    onAskAi = { viewModel.executeAiSearch(it) },
                                                    modifier = Modifier.padding(top = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    AppListEmptyState(
                                        query = state.query,
                                        onAskAi = { viewModel.executeAiSearch(it) }
                                    )
                                }
                            }
                            is AppsListState.Error -> {
                                AppListErrorState(
                                    errorMessage = state.message,
                                    onRetry = { viewModel.reloadApps() }
                                )
                            }
                            is AppsListState.Success -> {
                                 LazyColumn(
                                     state = listState,
                                     modifier = Modifier.fillMaxSize().fadingEdges(topFade = 32.dp, bottomFade = 44.dp),
                                     verticalArrangement = if (uiState.searchQuery.isNotEmpty()) Arrangement.Bottom else Arrangement.Top
                                 ) {
                                    // Modo Calculadora isolado (@calc)
                                    val isCalcMode = uiState.calculatorResult != null || uiState.searchQuery.startsWith("@calc", ignoreCase = true)
                                    val isContactsMode = uiState.searchQuery.startsWith("@con", ignoreCase = true)
                                    val isAiCommand = uiState.searchQuery.startsWith("@ai", ignoreCase = true) ||
                                            uiState.searchQuery.startsWith("@gemini", ignoreCase = true) ||
                                            uiState.searchQuery.startsWith("@groq", ignoreCase = true)

                                    // Resposta IA (Gemini / Groq)
                                    val isAiSearchActive = uiState.isAiSearchEnabled && (
                                        uiState.isAiSearchLoading ||
                                        uiState.aiSearchResponse != null ||
                                        uiState.aiSearchError != null ||
                                        isAiCommand
                                    )

                                    if (isAiSearchActive) {
                                        item(key = "ai_response_card") {
                                            AiResponseCard(
                                                query = uiState.searchQuery,
                                                response = uiState.aiSearchResponse,
                                                isLoading = uiState.isAiSearchLoading,
                                                error = uiState.aiSearchError,
                                                provider = uiState.aiProvider,
                                                onRetry = { viewModel.executeAiSearch() },
                                                onOpenSettings = {
                                                    viewModel.openSettings()
                                                    viewModel.navigateToSettingsSubScreen(SettingsSubScreen.SEARCH)
                                                },
                                                isLightMode = uiState.isLightMode,
                                                isAmoledMode = uiState.isAmoledMode,
                                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    // Chips de Comandos quando digitar @ isolado ou prefixo inicial sem espaço
                                    val showCommandsChips = !isCalcMode && !isContactsMode && !isAiCommand &&
                                            (uiState.searchQuery == "@" || (uiState.searchQuery.startsWith("@") && !uiState.searchQuery.contains(" ")))

                                    if (showCommandsChips) {
                                        item(key = "commands_chips") {
                                            CommandsChipsRow(
                                                commandsList = uiState.commandsList,
                                                onChipClick = { prefix ->
                                                    viewModel.onSearchQueryChange(prefix)
                                                }
                                            )
                                        }
                                    }

                                    // Cartão da Calculadora (@calc)
                                    uiState.calculatorResult?.let { result ->
                                        item(key = "calc_result") {
                                            CalculatorCard(
                                                query = uiState.searchQuery,
                                                result = result,
                                                isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode,
                                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                // Contatos encontrados (oculto em modo calculadora)
                                if (!isCalcMode && uiState.matchingContacts.isNotEmpty()) {
                                    item(key = "contacts_header") {
                                        Text(
                                            text = "CONTATOS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                        )
                                    }
                                    items(
                                        items = uiState.matchingContacts,
                                        key = { "contact_${it.name}_${it.phoneNumber}" }
                                    ) { contact ->
                                        ContactListItem(
                                            contact = contact,
                                            onClick = { viewModel.callContact(contact.phoneNumber) }
                                        )
                                    }
                                }

                                // Cartão de Pasta encontrada na busca (Screenshot media_1788774600763.png)
                                if (!isCalcMode) {
                                    matchingFolder?.let { folder ->
                                         item(key = "folder_card_${folder.id}") {
                                             FolderSearchCard(
                                                 folder = folder,
                                                 allApps = allApps,
                                                 onAppClick = { app -> viewModel.launchApp(app.packageName) },
                                                 onAppLongClick = { app -> selectedAppForMenu = app },
                                                 onFolderClick = { f -> folderToView = f },
                                                 isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode
                                             )
                                         }
                                     }
                                 }

                                  // Aplicativos filtrados (ocultos em modo calculadora/contatos)
                                  if (!isCalcMode && !isContactsMode) {
                                      // Sugestões da IA no Topo da Gaveta (Estilo Niagara)
                                      if (uiState.searchQuery.isEmpty() &&
                                          uiState.selectedAppCategory == com.tessera.launcher.data.model.AppCategory.ALL &&
                                          uiState.isSmartDockEnabled &&
                                          uiState.predictedApps.isNotEmpty()
                                      ) {
                                          item(key = "ai_suggested_header") {
                                              Text(
                                                  text = "SUGESTÕES DA IA",
                                                  style = MaterialTheme.typography.labelSmall.copy(
                                                      fontSize = 11.sp,
                                                      fontWeight = FontWeight.Bold,
                                                      letterSpacing = 1.sp
                                                  ),
                                                  color = TextSecondary,
                                                  modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                              )
                                          }
                                          item(key = "ai_suggested_row") {
                                              SmartDockRow(
                                                  apps = uiState.predictedApps,
                                                  onAppClick = { app -> viewModel.launchApp(app.packageName) },
                                                  onAppLongClick = { app -> selectedAppForMenu = app },
                                                  iconShape = uiState.iconShape,
                                                  isThemedIcons = uiState.isThemedIconsEnabled,
                                                  isLightMode = uiState.isLightMode,
                                                  isAmoledMode = uiState.isAmoledMode,
                                                  isEmbedded = true,
                                                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                                              )
                                          }
                                          item(key = "ai_suggested_divider") {
                                              Spacer(modifier = Modifier.height(10.dp))
                                          }
                                      }

                                      items(
                                         items = uiState.filteredApps,
                                         key = { it.packageName }
                                     ) { app ->
                                     AppListItem(
                                         app = app,
                                         iconShape = uiState.iconShape,
                                         isThemedIcons = uiState.isThemedIconsEnabled,
                                         isHideAppLabels = uiState.isHideAppLabelsEnabled,
                                         isRightAligned = uiState.isDrawerRightAligned,
                                         onClick = {
                                             viewModel.launchApp(app.packageName).onFailure { error ->
                                                 Toast.makeText(
                                                     context,
                                                     error.message ?: "Erro ao abrir aplicativo",
                                                     Toast.LENGTH_SHORT
                                                 ).show()
                                             }
                                         },
                                         onLongClick = {
                                             selectedAppForMenu = app
                                         },
                                         onSwipeRight = {
                                             selectedAppForMenu = app
                                         }
                                     )
                                 }
                             }

                            // Pílula externa de pesquisa (Google + Apps)
                                if (uiState.searchQuery.isNotEmpty() && uiState.isWebSearchEnabled) {
                                    item {
                                        SearchExternalActions(
                                            query = uiState.searchQuery,
                                            inAppSearchPackages = uiState.inAppSearchPackages,
                                            isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode,
                                            onAskAi = { viewModel.executeAiSearch(it) },
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Indexador A-Z Niagara
                if (uiState.appsState is AppsListState.Success && uiState.searchQuery.isEmpty()) {
                    AlphabetScroller(
                        availableLetters = uiState.availableLetters.toSet(),
                        onLetterSelected = { letter ->
                            val targetIndex = uiState.letterIndexMap[letter] ?: run {
                                val alphabetIndex = Alphabet.indexOf(letter)
                                if (alphabetIndex >= 0) {
                                    val nextLetter = Alphabet.drop(alphabetIndex + 1).firstOrNull { uiState.letterIndexMap.containsKey(it) }
                                    if (nextLetter != null) {
                                        uiState.letterIndexMap[nextLetter]
                                    } else {
                                        Alphabet.take(alphabetIndex).reversed().firstOrNull { uiState.letterIndexMap.containsKey(it) }?.let {
                                            uiState.letterIndexMap[it]
                                        }
                                    }
                                } else null
                            }
                            if (targetIndex != null) {
                                scope.launch {
                                    listState.scrollToItem(targetIndex)
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            // Barra de Categorias de Aplicativos (na parte de baixo, em cima da barra de pesquisa)
            if (uiState.isDrawerOpen && uiState.searchQuery.isEmpty() && uiState.isAppCategoriesEnabled) {
                AppCategoriesBar(
                    selectedCategory = uiState.selectedAppCategory,
                    onCategorySelected = { category -> viewModel.selectAppCategory(category) },
                    isLightMode = uiState.isLightMode,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }

        // Doca Inferior de Pesquisa & Controles Unificada
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val showWidgets = !uiState.isDrawerOpen && (uiState.isSearchExpanded || !uiState.isCollapseDockEnabled) && uiState.isWidgetExpanded && uiState.searchQuery.isEmpty()

            SearchMorphingDock(
                isExpanded = if (!uiState.isCollapseDockEnabled) true else uiState.isSearchExpanded,
                searchQuery = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onExpandClick = {
                    viewModel.expandSearch()
                },
                onOpenSettings = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.openSettings()
                },
                focusRequester = focusRequester,
                searchBarStyle = uiState.searchBarStyle,
                searchBarTextType = uiState.searchBarTextType,
                searchBarCustomText = uiState.searchBarCustomText,
                currentTime = uiState.formattedTime,
                isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode,
                isAmoledMode = uiState.isAmoledMode,
                isLightMode = uiState.isLightMode,
                searchBarOpacity = uiState.searchBarOpacity,
                isGeminiGlowEnabled = uiState.isGeminiGlowEnabled,
                onAiSearchClick = { viewModel.executeAiSearch() },
                onSearchSubmit = { viewModel.submitSearch(it) },
                widgetContent = if (showWidgets) {
                    {
                        WidgetsPanel(
                            enabledWidgets = uiState.enabledWidgets,
                            batteryPercentage = uiState.batteryPercentage,
                            isCharging = uiState.isCharging,
                            currentTime = uiState.formattedTime,
                            currentDate = uiState.formattedDate,
                            nextCalendarEvent = uiState.nextCalendarEvent,
                            hasCalendarPermission = uiState.hasCalendarPermission,
                            onRequestCalendarPermission = onRequestCalendarPermission,
                            onCalendarClick = { viewModel.launchCalendarApp() },
                            mediaPlayback = uiState.mediaPlayback,
                            hasNotificationAccess = uiState.hasNotificationAccess,
                            onRequestNotificationAccess = {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            },
                            onTogglePlayPause = { viewModel.togglePlayPauseMedia() },
                            onSkipNext = { viewModel.skipNextMedia() },
                            onSkipPrevious = { viewModel.skipPreviousMedia() },
                            onOpenMusicApp = { viewModel.launchDefaultMusicApp() },
                            defaultWidgetCardIndex = uiState.defaultWidgetCardIndex,
                            isDinoWidgetEnabled = uiState.isDinoWidgetEnabled,
                            isNotesWidgetEnabled = uiState.isNotesWidgetEnabled,
                            isSwitchOnMusicPlayEnabled = uiState.isSwitchOnMusicPlayEnabled,
                            isTorchOn = uiState.isTorchOn,
                            ringerMode = uiState.ringerMode,
                            onToggleTorch = { viewModel.toggleTorch() },
                            onOpenWifi = { viewModel.openWifiSettings() },
                            onOpenBluetooth = { viewModel.openBluetoothSettings() },
                            onCycleRingerMode = { viewModel.cycleRingerMode() },
                            notesTasks = uiState.notesTasks,
                            onAddNoteTask = { viewModel.addNoteTask(it) },
                            onToggleNoteTask = { viewModel.toggleNoteTask(it) },
                            onRemoveNoteTask = { viewModel.removeNoteTask(it) },
                            onNotesClick = { isNotesTasksOpen = true },
                            weatherInfo = uiState.weatherInfo,
                            hasLocationPermission = uiState.hasLocationPermission,
                            onRequestLocationPermission = onRequestLocationPermission,
                            onRefreshWeather = { viewModel.refreshWeather() },
                            isWeatherLoading = uiState.isWeatherLoading,
                            weatherError = uiState.weatherError,
                            batteryWidgetStyle = uiState.batteryWidgetStyle,
                            mediaWidgetStyle = uiState.mediaWidgetStyle,
                            notesWidgetFilter = uiState.notesWidgetFilter,
                            onWidgetLongClick = { configType -> viewModel.openWidgetConfig(configType) },
                            isLiquidGlass = (uiState.isLiquidGlassEnabled && !uiState.isAmoledMode) || uiState.searchBarOpacity < 98,
                            isAmoledMode = uiState.isAmoledMode,
                            isLightMode = uiState.isLightMode,
                            smartGlanceBriefing = uiState.smartGlanceBriefing,
                            isSmartGlanceEnabled = uiState.isSmartGlanceEnabled,
                            predictedApps = uiState.predictedApps,
                            isPredictedAppsWidgetEnabled = uiState.isSmartDockEnabled,
                            onAppClick = { app -> viewModel.launchApp(app.packageName) },
                            onAppLongClick = { app -> selectedAppForMenu = app },
                            iconShape = uiState.iconShape,
                            isThemedIcons = uiState.isThemedIconsEnabled
                        )
                    }
                } else null
            )
        }

        // Painel de Configurações da Launcher
        AnimatedVisibility(
            visible = uiState.isSettingsOpen,
            enter = fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing))
        ) {
            SettingsScreen(
                viewModel = viewModel,
                onPickPhoto = onPickPhoto,
                onPickWallpaper = onPickWallpaper,
                onRequestCalendarPermission = onRequestCalendarPermission,
                onRequestContactsPermission = onRequestContactsPermission
            )
        }

        // Menu de Contexto do Aplicativo com Atalhos & Troca de Ícones
        selectedAppForMenu?.let { app ->
            AppContextMenu(
                app = app,
                shortcuts = if (uiState.isAppShortcutsEnabled) viewModel.getAppShortcuts(app.packageName) else emptyList(),
                installedIconPacks = viewModel.getInstalledIconPacks(),
                currentCustomIconPack = uiState.customAppIcons[app.packageName],
                onShortcutClick = { shortcut ->
                    viewModel.launchShortcut(shortcut.packageName, shortcut.id)
                },
                onChangeIconPack = { packPkg ->
                    viewModel.setCustomAppIcon(app.packageName, packPkg)
                },
                onHideApp = {
                    viewModel.toggleHiddenApp(app.packageName)
                    Toast.makeText(context, "${app.label} ocultado", Toast.LENGTH_SHORT).show()
                    selectedAppForMenu = null
                },
                onDismiss = { selectedAppForMenu = null },
                onOpenAppSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                onUninstallApp = {
                    try {
                        val intent = Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.parse("package:${app.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Não foi possível iniciar a desinstalação", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Modais de Configuração dos Widgets por Toque Longo
        when (uiState.activeWidgetConfigModal) {
            WidgetConfigType.NOTES -> {
                NotesConfigBottomSheet(
                    currentFilter = uiState.notesWidgetFilter,
                    onFilterSelect = { viewModel.setNotesWidgetFilter(it) },
                    onOpenWidgetsCenter = {
                        viewModel.closeWidgetConfig()
                        viewModel.openSettings()
                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER)
                    },
                    onDismiss = { viewModel.closeWidgetConfig() }
                )
            }
            WidgetConfigType.MEDIA -> {
                MediaConfigBottomSheet(
                    currentStyle = uiState.mediaWidgetStyle,
                    onStyleSelect = { viewModel.setMediaWidgetStyle(it) },
                    preferredMusicApp = uiState.defaultMusicApp,
                    onOpenWidgetsCenter = {
                        viewModel.closeWidgetConfig()
                        viewModel.openSettings()
                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER)
                    },
                    onDismiss = { viewModel.closeWidgetConfig() }
                )
            }
            WidgetConfigType.BATTERY -> {
                BatteryConfigBottomSheet(
                    currentStyle = uiState.batteryWidgetStyle,
                    onStyleSelect = { viewModel.setBatteryWidgetStyle(it) },
                    onOpenWidgetsCenter = {
                        viewModel.closeWidgetConfig()
                        viewModel.openSettings()
                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER)
                    },
                    onDismiss = { viewModel.closeWidgetConfig() }
                )
            }
            WidgetConfigType.CALENDAR -> {
                CalendarConfigBottomSheet(
                    hasCalendarPermission = uiState.hasCalendarPermission,
                    onRequestCalendarPermission = onRequestCalendarPermission,
                    howFarAhead = uiState.calendarHowFarAhead,
                    onHowFarAheadChange = { viewModel.setCalendarHowFarAhead(it) },
                    hideFinishedEvents = uiState.calendarHideFinished,
                    onHideFinishedEventsChange = { viewModel.setCalendarHideFinished(it) },
                    is24hFormat = uiState.calendarIs24hFormat,
                    onIs24hFormatChange = { viewModel.setCalendarIs24hFormat(it) },
                    onOpenWidgetsCenter = {
                        viewModel.closeWidgetConfig()
                        viewModel.openSettings()
                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER)
                    },
                    onDismiss = { viewModel.closeWidgetConfig() }
                )
            }
            WidgetConfigType.WEATHER -> {
                WeatherConfigBottomSheet(
                    weatherInfo = uiState.weatherInfo,
                    isCelsius = uiState.isWeatherCelsius,
                    hasLocationPermission = uiState.hasLocationPermission,
                    onCelsiusChange = { viewModel.setWeatherCelsius(it) },
                    onRequestLocationPermission = onRequestLocationPermission,
                    onRefreshWeather = { viewModel.refreshWeather() },
                    onOpenWidgetsCenter = {
                        viewModel.closeWidgetConfig()
                        viewModel.openSettings()
                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER)
                    },
                    onDismiss = { viewModel.closeWidgetConfig() },
                    isLightMode = uiState.isLightMode,
                    isAmoledMode = uiState.isAmoledMode,
                    isAutoLocation = uiState.isWeatherAutoLocation,
                    customCityName = uiState.customWeatherCity,
                    searchResults = uiState.weatherCitySearchResults,
                    isSearchingCities = uiState.isSearchingCities,
                    onSearchCityQuery = { viewModel.searchWeatherCities(it) },
                    onSelectCity = { viewModel.selectCustomWeatherCity(it) },
                    onToggleAutoLocation = { viewModel.setWeatherAutoLocation(it) }
                )
            }
            WidgetConfigType.QUICK_ACTIONS -> {
                LaunchedEffect(Unit) {
                    viewModel.closeWidgetConfig()
                    viewModel.openSettings()
                    viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER)
                }
            }
            WidgetConfigType.VERSE_FOCUS, null -> {}
        }

        if (isNotesTasksOpen) {
            NotesTasksManagerBottomSheet(
                tasks = uiState.notesTasks,
                onAddTask = { viewModel.addNoteTask(it) },
                onToggleTask = { viewModel.toggleNoteTask(it) },
                onRemoveTask = { viewModel.removeNoteTask(it) },
                onDismiss = { isNotesTasksOpen = false }
            )
        }

        folderToView?.let { folder ->
            FolderViewBottomSheet(
                folder = folder,
                allApps = allApps,
                onAppClick = { app -> viewModel.launchApp(app.packageName) },
                onAppLongClick = { app -> selectedAppForMenu = app },
                onDismiss = { folderToView = null },
                isAmoledMode = uiState.isAmoledMode
            )
        }

        // Central Nothing OS ou Feed Social — Tela −1 (Slide Horizontal)
        AnimatedVisibility(
            visible = uiState.isFeedOpen,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(180, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(180))
        ) {
            if (uiState.leftScreenMode == "feed") {
                FeedScreen(
                    feedState = uiState.feedState,
                    activeSource = null,
                    enabledSources = uiState.feedEnabledSources,
                    isLightMode = uiState.isLightMode,
                    onSourceSelected = { },
                    onPostClick = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching { context.startActivity(intent) }
                    },
                    onRefresh = { viewModel.refreshFeed() },
                    onClose = { viewModel.closeFeed() }
                )
            } else {
                NothingHubScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onClose = { viewModel.closeFeed() }
                )
            }
        }
    }
    }
}

@Composable
private fun ContactListItem(
    contact: ContactInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF222632)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1
            )
        }

        Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = "Ligar",
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

