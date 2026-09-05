package com.tessera.launcher.ui.screens

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.tessera.launcher.ui.components.AlphabetScroller
import com.tessera.launcher.ui.components.AppListEmptyState
import com.tessera.launcher.ui.components.AppListErrorState
import com.tessera.launcher.ui.components.AppListItem
import com.tessera.launcher.ui.components.AppListSkeleton
import com.tessera.launcher.ui.components.NativeWidgetHostContainer
import com.tessera.launcher.ui.components.SearchoDock
import com.tessera.launcher.ui.components.SearchoFloatingButton
import com.tessera.launcher.ui.components.WidgetsPanel
import com.tessera.launcher.ui.state.AppsListState
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkBackgroundTranslucent
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    appWidgetHost: AppWidgetHost?,
    appWidgetManager: AppWidgetManager?,
    onPickNativeWidget: () -> Unit,
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

    // Observa retorno ao app para atualizar permissões de notificação e calendário
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

    // BackHandler estrito de Launcher: nunca encerra a aplicação
    BackHandler(enabled = true) {
        val handled = viewModel.handleBackPress()
        if (handled) {
            focusManager.clearFocus()
        }
    }

    // Fecha o teclado se a busca recolher
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
        // Área Central da Tela Inicial (Widgets Nativos do Android ou limpo)
        if (!uiState.isDrawerOpen && uiState.searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                NativeWidgetHostContainer(
                    appWidgetHost = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    hostedWidgetIds = uiState.hostedWidgetIds,
                    onRemoveWidget = { id -> viewModel.onNativeWidgetRemoved(id) }
                )
            }
        }

        // Gaveta Vertical de Aplicativos (Niagara Style)
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
                    .padding(bottom = if (uiState.isWidgetExpanded) 280.dp else 96.dp)
            ) {
                // Lista de Aplicativos
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
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
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
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Seletor Vertical A-Z (Indexação O(1) rápida)
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

        // Rodapé Flutuante: Lupa (FAB) ou Barra Searcho Expandida
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(WindowInsets.navigationBars.asPaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Painel Expansível de Widgets (Acima da Barra)
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
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Transição entre Botão de Lupa (FAB) e Barra de Pesquisa Completa
            if (!uiState.isSearchExpanded) {
                SearchoFloatingButton(
                    isGeminiAnimating = uiState.isGeminiAnimating,
                    onClick = {
                        viewModel.expandSearch()
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                SearchoDock(
                    searchQuery = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    isWidgetExpanded = uiState.isWidgetExpanded,
                    isGeminiAnimating = uiState.isGeminiAnimating,
                    onToggleWidgets = { viewModel.toggleWidgets() },
                    onSearchFocused = {
                        viewModel.openDrawer()
                        viewModel.setWidgetsExpanded(true)
                    },
                    onCollapseSearch = { viewModel.collapseSearch() },
                    onOpenSettings = { viewModel.openSettings() },
                    focusRequester = focusRequester
                )
            }
        }

        // Painel / Tela de Configurações da Launcher
        AnimatedVisibility(
            visible = uiState.isSettingsOpen,
            enter = fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing))
        ) {
            SettingsScreen(
                viewModel = viewModel,
                onPickNativeWidget = onPickNativeWidget,
                onRequestCalendarPermission = onRequestCalendarPermission
            )
        }
    }
}
