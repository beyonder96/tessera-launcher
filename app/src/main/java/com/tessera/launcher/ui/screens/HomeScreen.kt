package com.tessera.launcher.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.components.AlphabetScroller
import com.tessera.launcher.ui.components.AppContextMenu
import com.tessera.launcher.ui.components.AppListEmptyState
import com.tessera.launcher.ui.components.AppListErrorState
import com.tessera.launcher.ui.components.AppListItem
import com.tessera.launcher.ui.components.AppListSkeleton
import com.tessera.launcher.ui.components.PhotoWidget
import com.tessera.launcher.ui.components.SearchoMorphingDock
import com.tessera.launcher.ui.components.WidgetsPanel
import com.tessera.launcher.ui.state.AppsListState
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkBackgroundTranslucent
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onPickPhoto: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationAccess(context)
                viewModel.refreshCalendarAndPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = true) {
        val handled = viewModel.handleBackPress()
        if (handled) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(uiState.isSearchExpanded) {
        if (!uiState.isSearchExpanded) {
            focusManager.clearFocus()
        }
    }

    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (uiState.isAmoledMode) DarkBackground else DarkBackgroundTranslucent)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .pointerInput(uiState.isDrawerOpen) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = { dragAccumulator = 0f },
                    onDragCancel = { dragAccumulator = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount
                        // Swipe para cima abre o drawer
                        if (!uiState.isDrawerOpen && dragAccumulator < -40f) {
                            viewModel.openDrawer()
                            dragAccumulator = 0f
                        }
                        // Swipe para baixo na home recolhe o drawer
                        if (uiState.isDrawerOpen && dragAccumulator > 60f) {
                            viewModel.closeDrawer()
                            dragAccumulator = 0f
                        }
                    }
                )
            }
    ) {
        // Centro da Tela Inicial: Moldura de Foto Minimalista (Toque fecha busca se expandida)
        if (!uiState.isDrawerOpen && uiState.searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (uiState.isSearchExpanded) {
                                viewModel.collapseSearch()
                                focusManager.clearFocus()
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isPhotoWidgetEnabled) {
                    PhotoWidget(
                        photoUriString = uiState.photoWidgetUri,
                        onPickPhoto = {
                            if (uiState.isSearchExpanded) {
                                viewModel.collapseSearch()
                                focusManager.clearFocus()
                            } else {
                                onPickPhoto()
                            }
                        },
                        onRemovePhoto = { viewModel.setPhotoWidgetUri(null) },
                        isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode
                    )
                }
            }
        }

        // Gaveta Vertical de Aplicativos (Resultados no Rodapé para Uso com Uma Mão)
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
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .padding(bottom = if (uiState.isWidgetExpanded) 310.dp else 84.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when (val state = uiState.appsState) {
                        is AppsListState.Loading -> {
                            AppListSkeleton(modifier = Modifier.padding(top = 16.dp))
                        }
                        is AppsListState.Empty -> {
                            AppListEmptyState(query = state.query)
                        }
                        is AppsListState.Error -> {
                            AppListErrorState(
                                errorMessage = state.message,
                                onRetry = { viewModel.reloadApps() }
                            )
                        }
                        is AppsListState.Success -> {
                            // Arrangement.Bottom ancora os resultados mais próximos ao polegar na busca!
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = if (uiState.searchQuery.isNotEmpty()) Arrangement.Bottom else Arrangement.Top
                            ) {
                                items(
                                    items = uiState.filteredApps,
                                    key = { it.packageName }
                                ) { app ->
                                    AppListItem(
                                        app = app,
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
                                        modifier = Modifier.animateItem()
                                    )
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
                            val targetIndex = uiState.letterIndexMap[letter]
                            if (targetIndex != null) {
                                scope.launch {
                                    listState.scrollToItem(targetIndex)
                                }
                            }
                        }
                    )
                }
            }
        }

        // Rodapé Fixo: Doca Morfológica Searcho e Painel de Widgets
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Painel Expansível de Widgets (Staggered)
            AnimatedVisibility(
                visible = uiState.isSearchExpanded && uiState.isWidgetExpanded,
                enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        ),
                exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                        slideOutVertically(
                            targetOffsetY = { it / 3 },
                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                        )
            ) {
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
                    isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Doca Morfológica Contínua (Lupa <-> Barra Tessera)
            SearchoMorphingDock(
                isExpanded = uiState.isSearchExpanded,
                searchQuery = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                isWidgetExpanded = uiState.isWidgetExpanded,
                onToggleWidgets = { viewModel.toggleWidgets() },
                onExpandClick = {
                    viewModel.expandSearch()
                    focusRequester.requestFocus()
                },
                onOpenSettings = { viewModel.openSettings() },
                focusRequester = focusRequester,
                isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode
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
                onRequestCalendarPermission = onRequestCalendarPermission
            )
        }

        // Menu de Contexto do Aplicativo (Informações & Desinstalação)
        selectedAppForMenu?.let { app ->
            AppContextMenu(
                app = app,
                onDismiss = { selectedAppForMenu = null },
                onOpenAppSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                onUninstallApp = {
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}
