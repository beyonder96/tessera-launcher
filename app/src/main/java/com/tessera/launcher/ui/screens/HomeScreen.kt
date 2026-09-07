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
import androidx.compose.material.icons.outlined.InsertDriveFile
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import com.tessera.launcher.ui.components.AlphabetScroller
import com.tessera.launcher.ui.components.AppContextMenu
import com.tessera.launcher.ui.components.AppListEmptyState
import com.tessera.launcher.ui.components.AppListErrorState
import com.tessera.launcher.ui.components.AppListItem
import com.tessera.launcher.ui.components.AppListSkeleton
import com.tessera.launcher.ui.components.CalculatorCard
import com.tessera.launcher.ui.components.FolderSearchCard
import com.tessera.launcher.ui.components.BatteryConfigBottomSheet
import com.tessera.launcher.ui.components.CalendarConfigBottomSheet
import com.tessera.launcher.ui.components.MediaConfigBottomSheet
import com.tessera.launcher.ui.components.NotesConfigBottomSheet
import com.tessera.launcher.ui.components.PhotoWidget
import com.tessera.launcher.ui.components.SearchExternalActions
import com.tessera.launcher.ui.components.SearchoMorphingDock
import com.tessera.launcher.ui.components.SearchosChipsRow
import com.tessera.launcher.ui.components.WeatherConfigBottomSheet
import com.tessera.launcher.ui.components.WidgetsPanel
import com.tessera.launcher.ui.state.AppsListState
import com.tessera.launcher.ui.state.FileSearchResult
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.state.WidgetConfigType
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkBackgroundTranslucent
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onPickPhoto: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (uiState.isAmoledMode) DarkBackground else DarkBackgroundTranslucent)
            .padding(top = statusBarTopPadding)
            // Gestos de Toque: Toque duplo e Manter pressionado
            .pointerInput(uiState.isDoubleTapEnabled, uiState.doubleTapAction, uiState.isHoldEnabled, uiState.holdAction, uiState.isDrawerOpen) {
                detectTapGestures(
                    onDoubleTap = {
                        if (uiState.isDoubleTapEnabled && !uiState.isDrawerOpen) {
                            viewModel.executeGestureAction(uiState.doubleTapAction, context)
                        }
                    },
                    onLongPress = {
                        if (uiState.isHoldEnabled && !uiState.isDrawerOpen) {
                            viewModel.executeGestureAction(uiState.holdAction, context)
                        }
                    }
                )
            }
            // Gestos de Deslizar: Cima, Baixo, Esquerda, Direita
            .pointerInput(uiState.isDrawerOpen, uiState.isSwipeDownEnabled, uiState.swipeDownAction, uiState.isSwipeUpEnabled, uiState.swipeUpAction) {
                var totalDragX = 0f
                var totalDragY = 0f
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        val absX = abs(totalDragX)
                        val absY = abs(totalDragY)
                        if (absY > absX && absY > 55f) {
                            if (totalDragY < 0) { // Deslizar para CIMA
                                if (!uiState.isDrawerOpen) {
                                    if (uiState.isSwipeUpEnabled &&
                                        uiState.swipeUpAction != "open_drawer" && uiState.swipeUpAction != "open_keyboard"
                                    ) {
                                        viewModel.executeGestureAction(uiState.swipeUpAction, context)
                                    } else {
                                        viewModel.expandSearch()
                                        viewModel.openDrawer()
                                        try {
                                            focusRequester.requestFocus()
                                            keyboardController?.show()
                                        } catch (_: Exception) {}
                                    }
                                }
                            } else { // Deslizar para BAIXO
                                if (uiState.isDrawerOpen) {
                                    viewModel.closeDrawer()
                                } else if (uiState.isSwipeDownEnabled) {
                                    viewModel.executeGestureAction(uiState.swipeDownAction, context)
                                }
                            }
                        } else if (absX > absY && absX > 55f && !uiState.isDrawerOpen) {
                            if (totalDragX < 0) { // Deslizar para a ESQUERDA
                                if (uiState.isSwipeLeftEnabled) {
                                    viewModel.executeGestureAction(uiState.swipeLeftAction, context)
                                }
                            } else { // Deslizar para a DIREITA
                                if (uiState.isSwipeRightEnabled) {
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

        // Camada de Foco e Desfoque de Fundo
        AnimatedVisibility(
            visible = uiState.isSearchExpanded || uiState.isDrawerOpen || uiState.searchQuery.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.58f))
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
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .padding(bottom = if (uiState.isWidgetExpanded) 310.dp else 84.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    when (val state = uiState.appsState) {
                        is AppsListState.Loading -> AppListSkeleton()
                        is AppsListState.Empty -> {
                            // Se a busca tiver contatos, arquivos, calculadora ou pasta, exibe esses resultados
                            if (uiState.calculatorResult != null ||
                                uiState.matchingContacts.isNotEmpty() ||
                                uiState.matchingFiles.isNotEmpty() ||
                                matchingFolder != null
                            ) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Bottom
                                ) {
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
                                    if (uiState.calculatorResult == null && uiState.matchingContacts.isNotEmpty()) {
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

                                    // Arquivos
                                    if (uiState.calculatorResult == null && uiState.matchingFiles.isNotEmpty()) {
                                        item(key = "files_header") {
                                            Text(
                                                text = "ARQUIVOS",
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
                                            items = uiState.matchingFiles,
                                            key = { "file_${it.uriString}" }
                                        ) { file ->
                                            FileListItem(
                                                file = file,
                                                onClick = { viewModel.openFile(file.uriString, file.mimeType) }
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
                                                isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode
                                            )
                                        }
                                    }

                                    // Barra externa de busca
                                    if (uiState.searchQuery.isNotEmpty() && uiState.isWebSearchEnabled) {
                                        item {
                                            SearchExternalActions(
                                                query = uiState.searchQuery,
                                                inAppSearchPackages = uiState.inAppSearchPackages,
                                                isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                AppListEmptyState(query = state.query)
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
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = if (uiState.searchQuery.isNotEmpty()) Arrangement.Bottom else Arrangement.Top
                            ) {
                                // Modo Calculadora isolado (@calc)
                                val isCalcMode = uiState.calculatorResult != null || uiState.searchQuery.startsWith("@calc", ignoreCase = true)
                                val isContactsMode = uiState.searchQuery.startsWith("@con", ignoreCase = true)
                                val isFilesMode = uiState.searchQuery.startsWith("@files", ignoreCase = true)

                                // Chips do SearchOS quando digitar @ isolado ou prefixo inicial sem espaço
                                val showSearchosChips = !isCalcMode && !isContactsMode && !isFilesMode &&
                                        (uiState.searchQuery == "@" || (uiState.searchQuery.startsWith("@") && !uiState.searchQuery.contains(" ")))

                                if (showSearchosChips) {
                                    item(key = "searchos_chips") {
                                        SearchosChipsRow(
                                            searchosList = uiState.searchosList,
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

                                // Contatos encontrados (oculto em modo calculadora ou arquivos)
                                if (!isCalcMode && !isFilesMode && uiState.matchingContacts.isNotEmpty()) {
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

                                // Arquivos encontrados (oculto em modo calculadora ou contatos)
                                if (!isCalcMode && !isContactsMode && uiState.matchingFiles.isNotEmpty()) {
                                    item(key = "files_header") {
                                        Text(
                                            text = "ARQUIVOS",
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
                                        items = uiState.matchingFiles,
                                        key = { "file_${it.uriString}" }
                                    ) { file ->
                                        FileListItem(
                                            file = file,
                                            onClick = { viewModel.openFile(file.uriString, file.mimeType) }
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
                                                isLiquidGlass = uiState.isLiquidGlassEnabled && !uiState.isAmoledMode
                                            )
                                        }
                                    }
                                }

                                // Aplicativos filtrados (ocultos em modo calculadora/contatos/arquivos)
                                if (!isCalcMode && !isContactsMode && !isFilesMode) {
                                    items(
                                        items = uiState.filteredApps,
                                        key = { it.packageName }
                                    ) { app ->
                                    AppListItem(
                                        app = app,
                                        iconShape = uiState.iconShape,
                                        isThemedIcons = uiState.isThemedIconsEnabled,
                                        isHideAppLabels = uiState.isHideAppLabelsEnabled,
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
                            val targetIndex = uiState.letterIndexMap[letter]
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
        }

        // Doca Inferior de Pesquisa & Controles Unificada (Widget + Dots + Busca)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
            contentAlignment = Alignment.BottomCenter
        ) {
            val showWidgets = (uiState.isSearchExpanded || !uiState.isCollapseDockEnabled) && uiState.isWidgetExpanded && uiState.searchQuery.isEmpty()

            SearchoMorphingDock(
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
                            weatherInfo = uiState.weatherInfo,
                            hasLocationPermission = uiState.hasLocationPermission,
                            onRequestLocationPermission = onRequestLocationPermission,
                            onRefreshWeather = { viewModel.refreshWeather() },
                            batteryWidgetStyle = uiState.batteryWidgetStyle,
                            mediaWidgetStyle = uiState.mediaWidgetStyle,
                            notesWidgetFilter = uiState.notesWidgetFilter,
                            onWidgetLongClick = { configType -> viewModel.openWidgetConfig(configType) },
                            isLiquidGlass = false
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
                    onDismiss = { viewModel.closeWidgetConfig() }
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

@Composable
private fun FileListItem(
    file: FileSearchResult,
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
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E202B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.InsertDriveFile,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val sizeMb = file.sizeBytes / (1024 * 1024.0)
            val sizeFormatted = if (sizeMb >= 1.0) String.format("%.1f MB", sizeMb) else "${file.sizeBytes / 1024} KB"
            Text(
                text = "${file.mimeType ?: "Arquivo"} • $sizeFormatted",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}
