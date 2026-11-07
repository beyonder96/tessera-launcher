package com.tessera.launcher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.launcher.R
import com.tessera.launcher.ui.components.LanguageBottomSheet
import com.tessera.launcher.ui.components.ModernSearchIcon
import com.tessera.launcher.ui.components.SettingsCatalog
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.CharcoalBackground
import com.tessera.launcher.ui.theme.CharcoalCardBackground
import com.tessera.launcher.ui.theme.CharcoalCardBorder
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightDivider
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private val SettingsCardShape = RoundedCornerShape(26.dp)
private val ItemDividerColor = Color(0xFF15151A)
private val IconCircleBackground = Color(0xFF16161C)

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onPickPhoto: () -> Unit,
    onPickWallpaper: () -> Unit = {},
    onRequestCalendarPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (uiState.currentSettingsScreen) {
        SettingsSubScreen.SEARCH -> {
            SearchSettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                onNavigateToWidgetsCenter = {
                    viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER)
                },
                onRequestContactsPermission = onRequestContactsPermission,
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.WIDGETS_CENTER -> {
            LateralPanelSettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                initialTab = 0,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.FEED -> {
            LateralPanelSettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                initialTab = 1,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.EXTRAS -> {
            ExtrasScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                onNavigateToFolders = {
                    viewModel.navigateToSettingsSubScreen(SettingsSubScreen.FOLDERS)
                },
                onNavigateToCommands = {
                    viewModel.navigateToSettingsSubScreen(SettingsSubScreen.COMMANDS)
                },
                onOpenHiddenApps = {
                    viewModel.navigateToSettingsSubScreen(SettingsSubScreen.HIDDEN_APPS)
                },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.FOLDERS -> {
            FoldersScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.COMMANDS -> {
            CommandsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.DEVELOPER -> {
            DeveloperScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.TRANSPARENCY -> {
            TransparencyScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.GESTURES -> {
            GesturesScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.IN_APP_SEARCH -> {
            InAppSearchSettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.HIDDEN_APPS -> {
            HiddenAppsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.CUSTOMIZATION -> {
            CustomizationScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                onPickWallpaper = onPickWallpaper,
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.PERMISSIONS -> {
            PermissionsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.MAIN -> {
            // Exibição do Hub de Configurações Principal
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery) { SettingsCatalog.search(searchQuery) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var isAiLoading by remember { mutableStateOf(false) }
    var aiResponseText by remember { mutableStateOf<String?>(null) }
    var aiSuggestedSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }

    fun requestAiGuidance(prompt: String) {
        val queryText = prompt.trim()
        if (queryText.isBlank()) return

        scope.launch {
            isAiLoading = true
            aiResponseText = null

            val topMatch = SettingsCatalog.search(queryText).firstOrNull()
            aiSuggestedSubScreen = topMatch?.targetSubScreen ?: SettingsSubScreen.CUSTOMIZATION

            try {
                val fullPrompt = "Como assistente do Tessera Launcher, responda em no máximo 1 frase direta e diga onde o usuário deve ir para: '$queryText'."
                val response = viewModel.querySettingsAi(fullPrompt)
                aiResponseText = response?.ifBlank {
                    "Você pode configurar isso na seção ${topMatch?.sectionName ?: "Personalização"}."
                }
            } catch (e: Exception) {
                aiResponseText = "Você pode configurar isso na seção ${topMatch?.sectionName ?: "Personalização"}."
            }

            isAiLoading = false
        }
    }

    BackHandler(enabled = searchQuery.isNotEmpty()) {
        searchQuery = ""
        aiResponseText = null
        isAiLoading = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    if (uiState.isLanguageModalOpen) {
        LanguageBottomSheet(
            selectedLanguage = uiState.selectedLanguage,
            onSelectLanguage = { viewModel.setSelectedLanguage(it) },
            onDismiss = { viewModel.setLanguageModalOpen(false) },
            isAmoled = uiState.isAmoledMode,
            isLight = uiState.isLightMode
        )
    }

    val isLight = uiState.isLightMode
    val isCharcoal = uiState.themeMode == "DARK"
    val isAmoled = uiState.themeMode == "AMOLED" || uiState.isAmoledMode

    val screenBg = when {
        isLight -> LightBackground
        isCharcoal -> CharcoalBackground
        else -> AmoledBlack
    }

    val cardBg = when {
        isLight -> LightCardBackground
        isCharcoal -> CharcoalCardBackground
        else -> AmoledBlack
    }

    val cardBorder = when {
        isLight -> LightCardBorder
        isCharcoal -> CharcoalCardBorder
        else -> AmoledCardBorder
    }

    val primaryTextColor = if (isLight) LightTextPrimary else TextPrimary
    val secondaryTextColor = if (isLight) LightTextSecondary else TextSecondary

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val safeTopPadding = maxOf(statusBarTop, cutoutTop).coerceAtLeast(36.dp) + 14.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(top = safeTopPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Barra superior com Voltar e Logo "Tessera."
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                    aiResponseText = null
                                    isAiLoading = false
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                } else {
                                    viewModel.closeSettings()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = primaryTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_tessera_header_logo),
                    contentDescription = "Tessera",
                    colorFilter = ColorFilter.tint(primaryTextColor),
                    modifier = Modifier
                        .height(64.dp)
                        .align(Alignment.Center)
                )

                // Espaçador para balancear o layout sem ícone redundante de IA
                Spacer(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Barra de Busca de Configurações Rápida & Assistente IA em Primeiro Plano
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (isLight) Color(0xFFEBE8DC) else Color(0xFF14141A))
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isLight) Color(0x20000000) else Color(0x25FFFFFF)
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModernSearchIcon(
                        tint = secondaryTextColor,
                        modifier = Modifier.size(19.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Buscar recursos ou perguntar à IA...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = secondaryTextColor.copy(alpha = 0.70f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                if (it.isBlank()) {
                                    aiResponseText = null
                                    isAiLoading = false
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = primaryTextColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(if (isLight) Color.Black else Color.White),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    if (searchQuery.isNotBlank()) {
                                        requestAiGuidance(searchQuery)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        searchQuery = ""
                                        aiResponseText = null
                                        isAiLoading = false
                                        focusManager.clearFocus()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Limpar busca",
                                tint = secondaryTextColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Botão da IA na barra de pesquisa (único ícone de IA na tela)
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAiLoading) Color(0xFF67E8F9).copy(alpha = 0.25f)
                                else if (searchQuery.isNotBlank()) Color(0xFF67E8F9).copy(alpha = 0.16f)
                                else Color.Transparent
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (searchQuery.isNotBlank() || isAiLoading) Color(0xFF67E8F9).copy(alpha = 0.40f)
                                    else Color.Transparent
                                ),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    keyboardController?.hide()
                                    requestAiGuidance(searchQuery.ifBlank { "Como personalizar o launcher?" })
                                }
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "Consultar IA",
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(17.dp)
                            )
                            if (searchQuery.isNotBlank() || isAiLoading) {
                                Text(
                                    text = "IA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF67E8F9)
                                )
                            }
                        }
                    }
                }
            }

            if (searchQuery.isNotEmpty()) {
                // RESULTADOS DA BUSCA EM PRIMEIRO PLANO
                AnimatedVisibility(
                    visible = isAiLoading || aiResponseText != null,
                    enter = fadeIn(tween(250)),
                    exit = fadeOut(tween(150))
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isLight) Color(0xFFEFF6FF) else Color(0xFF0F1A28),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Assistente de Configurações",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (isAiLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Consultando catálogo de funções...",
                                        fontSize = 13.sp,
                                        color = secondaryTextColor
                                    )
                                }
                            } else {
                                aiResponseText?.let { answer ->
                                    Text(
                                        text = answer,
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp,
                                        color = primaryTextColor
                                    )

                                    aiSuggestedSubScreen?.let { targetScreen ->
                                        val targetItem = SettingsCatalog.items.firstOrNull { it.targetSubScreen == targetScreen }
                                        val buttonLabel = targetItem?.sectionName ?: "Abrir Seção"

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color(0xFF38BDF8).copy(alpha = 0.20f))
                                                .border(BorderStroke(1.dp, Color(0xFF38BDF8)), CircleShape)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = {
                                                        keyboardController?.hide()
                                                        viewModel.navigateToSettingsSubScreen(targetScreen)
                                                    }
                                                )
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Ir para $buttonLabel",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF38BDF8)
                                                )
                                                Icon(
                                                    imageVector = Icons.Outlined.ArrowForward,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (searchResults.isNotEmpty()) {
                    SectionHeader(
                        title = "RESULTADOS (${searchResults.size})",
                        color = secondaryTextColor
                    )

                    Surface(
                        shape = SettingsCardShape,
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            searchResults.forEachIndexed { index, item ->
                                SettingsItemRow(
                                    icon = item.icon,
                                    title = item.title,
                                    subtitle = "${item.sectionName} • ${item.description}",
                                    onClick = {
                                        keyboardController?.hide()
                                        viewModel.navigateToSettingsSubScreen(item.targetSubScreen)
                                    },
                                    isLight = isLight
                                )
                                if (index < searchResults.size - 1) {
                                    ItemDivider(isLight = isLight)
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = SettingsCardShape,
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Nenhuma configuração encontrada",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Text(
                                text = "Não encontramos correspondência para \"$searchQuery\". Você pode perguntar à IA onde encontrar essa opção.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = secondaryTextColor,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.18f))
                                    .border(BorderStroke(1.dp, Color(0xFF38BDF8)), CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            keyboardController?.hide()
                                            requestAiGuidance(searchQuery)
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Perguntar à IA onde encontrar",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // MODO PADRÃO: Sugestões rápidas e seções completas de configuração
                val quickSuggestions = listOf(
                    "Papel de Parede",
                    "Gestos",
                    "Ocultar Apps",
                    "Widgets",
                    "Tema AMOLED",
                    "Modo Foco Zen"
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickSuggestions) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isLight) Color(0x10000000) else Color(0x15FFFFFF))
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isLight) Color(0x15000000) else Color(0x20FFFFFF)
                                    ),
                                    CircleShape
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        searchQuery = tag
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = secondaryTextColor
                            )
                        }
                    }
                }

                // CATEGORIA 1: TELA & EXPERIÊNCIA
            SectionHeader(title = "TELA & EXPERIÊNCIA", color = secondaryTextColor)

            Surface(
                shape = SettingsCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Central & Feed (Tela −1) — Acesso unificado
                    SettingsItemRow(
                        icon = Icons.Outlined.Dashboard,
                        title = "Central & Feed (Tela −1)",
                        subtitle = "Central de widgets Bento e Feed de notícias RSS.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 2. Busca
                    SettingsItemRow(
                        icon = Icons.Outlined.Search,
                        title = "Busca & IA",
                        subtitle = "Fontes locais, web e provedor Gemini 3.8 Flash.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.SEARCH) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 3. Personalização
                    SettingsItemRow(
                        icon = Icons.Outlined.Palette,
                        title = "Personalização",
                        subtitle = "Ícones, papel de parede, relógios e acentos.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.CUSTOMIZATION) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 4. Gestos
                    SettingsItemRow(
                        icon = Icons.Outlined.TouchApp,
                        title = "Gestos",
                        subtitle = "Ações de deslizar e toque duplo na home.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.GESTURES) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 5. Tema inline
                    ThemeSettingsRow(
                        themeMode = uiState.themeMode,
                        onSelectMode = { viewModel.setThemeMode(it) },
                        isLight = isLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CATEGORIA 2: ORGANIZAÇÃO & PRIVACIDADE
            SectionHeader(title = "ORGANIZAÇÃO & PRIVACIDADE", color = secondaryTextColor)

            Surface(
                shape = SettingsCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Pastas
                    SettingsItemRow(
                        icon = Icons.Outlined.Folder,
                        title = "Pastas",
                        subtitle = "Agrupe aplicativos sob uma mesma palavra.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.FOLDERS) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 2. Comandos Rápidos
                    SettingsItemRow(
                        icon = Icons.Outlined.Code,
                        title = "Comandos Rápidos",
                        subtitle = "Prefixos dinâmicos como @con, @calc e atalhos.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.COMMANDS) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 3. Apps Ocultos
                    SettingsItemRow(
                        icon = Icons.Outlined.Visibility,
                        title = "Apps Ocultos",
                        subtitle = "Oculte apps protegidos com PIN e Biometria.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.HIDDEN_APPS) },
                        isLight = isLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CATEGORIA 3: PREFERÊNCIAS & SISTEMA
            SectionHeader(title = "PREFERÊNCIAS & SISTEMA", color = secondaryTextColor)

            Surface(
                shape = SettingsCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Geral & Backup
                    SettingsItemRow(
                        icon = Icons.Outlined.Extension,
                        title = "Geral & Backup",
                        subtitle = "Barra de status, exportação e restauração JSON.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.EXTRAS) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 2. Idioma
                    SettingsItemRow(
                        icon = Icons.Outlined.Language,
                        title = "Idioma",
                        subtitle = "Alterar o idioma do aplicativo.",
                        onClick = { viewModel.setLanguageModalOpen(true) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 3. Lançador padrão
                    SettingsItemRow(
                        icon = Icons.Outlined.Home,
                        title = "Lançador Padrão",
                        subtitle = "Definir o Tessera como início padrão do Android.",
                        onClick = { openDefaultLauncherSettings(context) },
                        isLight = isLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CATEGORIA 4: PRIVACIDADE & SOBRE
            SectionHeader(title = "TRANSPARÊNCIA & SOBRE", color = secondaryTextColor)

            Surface(
                shape = SettingsCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Permissões
                    SettingsItemRow(
                        icon = Icons.Outlined.Security,
                        title = "Permissões",
                        subtitle = "Notificações, calendário, contatos e localização.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.PERMISSIONS) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 2. Transparência
                    SettingsItemRow(
                        icon = Icons.Outlined.VisibilityOff,
                        title = "Transparência",
                        subtitle = "Sem rastreio. Sem anúncios. Sem telemetria.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.TRANSPARENCY) },
                        isLight = isLight
                    )

                    ItemDivider(isLight = isLight)

                    // 3. Desenvolvedor
                    SettingsItemRow(
                        icon = Icons.Outlined.Code,
                        title = "Desenvolvedor",
                        subtitle = "Depuração avançada, logs e informações do build.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.DEVELOPER) },
                        isLight = isLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.3.sp
        ),
        color = color,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLight: Boolean = false
) {
    val iconCircleBackground = if (isLight) Color(0xFFE8E4D8) else IconCircleBackground
    val iconTint = if (isLight) Color(0xFF1C1917) else TextPrimary
    val titleColor = if (isLight) LightTextPrimary else TextPrimary
    val subtitleColor = if (isLight) LightTextSecondary else TextSecondary
    val chevronTint = if (isLight) Color(0xFF78716C) else TextSecondary.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconCircleBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = chevronTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ThemeSettingsRow(
    themeMode: String,
    onSelectMode: (String) -> Unit,
    isLight: Boolean = false
) {
    val iconCircleBackground = if (isLight) Color(0xFFE8E4D8) else IconCircleBackground
    val iconTint = if (isLight) Color(0xFF1C1917) else TextPrimary
    val titleColor = if (isLight) LightTextPrimary else TextPrimary
    val subtitleColor = if (isLight) LightTextSecondary else TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconCircleBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = "Tema",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tema",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Trocar de estilo",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = subtitleColor
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Auto
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24))
                    .border(
                        width = if (themeMode == "AUTO") 1.dp else 0.dp,
                        color = if (themeMode == "AUTO") Color.White else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectMode("AUTO") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (themeMode == "AUTO") {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Auto Ativo",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.BrightnessAuto,
                        contentDescription = "Automático",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // 2. AMOLED (Preto Puro)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF000000))
                    .border(
                        width = 1.dp,
                        color = if (themeMode == "AMOLED") Color.White else Color(0xFF33333D),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectMode("AMOLED") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (themeMode == "AMOLED") {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "AMOLED Ativo",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // 3. Dark Charcoal
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF26262B))
                    .border(
                        width = if (themeMode == "DARK") 1.dp else 0.dp,
                        color = if (themeMode == "DARK") Color.White else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectMode("DARK") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (themeMode == "DARK") {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Dark Ativo",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // 4. Light Journal
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF7F4EB))
                    .border(
                        width = if (themeMode == "LIGHT") 1.5.dp else 1.dp,
                        color = if (themeMode == "LIGHT") Color(0xFF1C1917) else Color(0xFFDDD8CB),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectMode("LIGHT") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (themeMode == "LIGHT") {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Light Ativo",
                        tint = Color.Black,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemDivider(isLight: Boolean = false) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 74.dp, end = 16.dp),
        color = if (isLight) LightDivider else ItemDividerColor,
        thickness = 1.dp
    )
}

private fun openDefaultLauncherSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
