package com.tessera.launcher.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.launcher.ui.components.HeroClockCustomizerSheet
import com.tessera.launcher.ui.components.LanguageBottomSheet
import com.tessera.launcher.ui.design.components.AppleProfileHeader
import com.tessera.launcher.ui.design.components.AppleSearchBar
import com.tessera.launcher.ui.design.components.AppleSwitch
import com.tessera.launcher.ui.design.components.AppleTintPickerSheet
import com.tessera.launcher.ui.design.components.CapsuleButton
import com.tessera.launcher.ui.design.components.GroupedRow
import com.tessera.launcher.ui.design.components.GroupedSection
import com.tessera.launcher.ui.design.components.LargeTitleScaffold
import com.tessera.launcher.ui.design.components.SegmentedControl
import com.tessera.launcher.ui.design.components.SquircleIcon
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.theme.apple.AppleTint
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleMotion
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import com.tessera.launcher.ui.theme.apple.appleSpring
import com.tessera.launcher.ui.viewmodel.MainViewModel

/**
 * Ajustes Oficiais do Tessera 3.0 — Padrão iOS Settings (Designed in Cupertino).
 * Estrutura com LargeTitle colapsável, pesquisa com filtro dinâmico, seções Inset Grouped,
 * ícones squircle em cores semânticas da Apple, toggles táteis e navegação push/pop horizontal.
 */
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
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current
    val motion = LocalAppleMotion.current

    var searchQuery by remember { mutableStateOf("") }
    var showTintPicker by remember { mutableStateOf(false) }
    var showClockCustomizer by remember { mutableStateOf(false) }

    // Intercepta botão voltar do Android: se sub-tela, volta para tela anterior; se raiz, fecha Ajustes
    BackHandler(enabled = true) {
        if (uiState.currentSettingsScreen != SettingsSubScreen.MAIN) {
            viewModel.navigateBackSettings()
        } else {
            viewModel.closeSettings()
        }
    }

    // Modal de Cores da Apple
    if (showTintPicker) {
        AppleTintPickerSheet(
            visible = showTintPicker,
            selectedTintId = uiState.accentColorName,
            onSelectTint = { tint ->
                viewModel.setAccentColor(tint.id)
            },
            onDismissRequest = { showTintPicker = false }
        )
    }

    // Modal do Relógio Hero
    if (showClockCustomizer) {
        HeroClockCustomizerSheet(
            visible = showClockCustomizer,
            currentWeight = 400,
            onWeightChange = { weight ->
                viewModel.setHomeClockStyle("WEIGHT_$weight")
            },
            onDismissRequest = { showClockCustomizer = false }
        )
    }

    // Modal de Idioma
    if (uiState.isLanguageModalOpen) {
        LanguageBottomSheet(
            selectedLanguage = uiState.selectedLanguage,
            onSelectLanguage = { viewModel.setSelectedLanguage(it) },
            onDismiss = { viewModel.setLanguageModalOpen(false) },
            isAmoled = uiState.isAmoledMode
        )
    }

    AnimatedContent(
        targetState = uiState.currentSettingsScreen,
        transitionSpec = {
            if (targetState != SettingsSubScreen.MAIN) {
                // Push forward suave estilo iOS Navigation Controller
                (slideInHorizontally(initialOffsetX = { it }, animationSpec = appleSpring()) +
                        fadeIn(animationSpec = motion.fadeStandard))
                    .togetherWith(
                        slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = appleSpring()) +
                                fadeOut(animationSpec = motion.fadeQuick)
                    )
            } else {
                // Pop back suave
                (slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = appleSpring()) +
                        fadeIn(animationSpec = motion.fadeStandard))
                    .togetherWith(
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = appleSpring()) +
                                fadeOut(animationSpec = motion.fadeQuick)
                    )
            }
        },
        label = "settings_navigation"
    ) { currentScreen ->
        when (currentScreen) {
            SettingsSubScreen.MAIN -> {
                LargeTitleScaffold(
                    title = "Ajustes",
                    actions = {
                        CapsuleButton(
                            text = "OK",
                            onClick = {
                                Taptic.lightImpact(context)
                                viewModel.closeSettings()
                            }
                        )
                    },
                    modifier = modifier
                ) {
                    item(key = "search_and_profile") {
                        Column {
                            // Barra de Pesquisa dos Ajustes
                            AppleSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Buscar nos Ajustes"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Cartão de Perfil Tessera 3.0
                            if (searchQuery.isBlank()) {
                                AppleProfileHeader(
                                    onClick = {
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.DEVELOPER)
                                    }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }

                    // Seções dos Ajustes Filtradas
                    val q = searchQuery.trim().lowercase()

                    // Seção 1: Aparência & Estilo
                    if (q.isEmpty() || "aparência tema cores ícones vidro transparência".contains(q)) {
                        item(key = "sec_appearance") {
                            GroupedSection(header = "Aparência") {
                                // Seletor de Tema Segmentado
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Aparência do Sistema",
                                        style = typography.caption1,
                                        color = colors.secondaryLabel
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val currentModeIndex = when (uiState.themeMode) {
                                        "LIGHT" -> 0
                                        "DARK" -> 1
                                        else -> 2
                                    }
                                    SegmentedControl(
                                        items = listOf("Claro", "Escuro", "Auto"),
                                        selectedIndex = currentModeIndex,
                                        onItemSelected = { index ->
                                            val mode = when (index) {
                                                0 -> "LIGHT"
                                                1 -> "DARK"
                                                else -> "AMOLED"
                                            }
                                            viewModel.setThemeMode(mode)
                                        }
                                    )
                                }

                                // Cor de Destaque Oficial Apple
                                val tint = AppleTint.fromId(uiState.accentColorName)
                                GroupedRow(
                                    title = "Cor de Destaque",
                                    subtitle = tint.displayName,
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.ColorLens,
                                            backgroundColor = colors.accent
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        showTintPicker = true
                                    }
                                )

                                // Personalização de Ícones
                                GroupedRow(
                                    title = "Pacote de Ícones",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Dashboard,
                                            backgroundColor = Color(0xFF5856D6)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.CUSTOMIZATION)
                                    }
                                )

                                // Reduzir Transparência (Fallback Apple para Liquid Glass)
                                GroupedRow(
                                    title = "Reduzir Transparência",
                                    subtitle = "Substitui o vidro por superfícies sólidas de alto contraste",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Visibility,
                                            backgroundColor = Color(0xFF8E8E93)
                                        )
                                    },
                                    trailingContent = {
                                        AppleSwitch(
                                            checked = uiState.isReduceTransparency,
                                            onCheckedChange = { enabled ->
                                                viewModel.setReduceTransparency(enabled)
                                                viewModel.setLiquidGlassEnabled(!enabled)
                                            }
                                        )
                                    }
                                )

                                // Reduzir Movimento
                                GroupedRow(
                                    title = "Reduzir Movimento",
                                    subtitle = "Desativa efeitos de rolagem e molas elásticas",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.TouchApp,
                                            backgroundColor = Color(0xFFFF9500)
                                        )
                                    },
                                    trailingContent = {
                                        AppleSwitch(
                                            checked = uiState.isReduceMotion,
                                            onCheckedChange = { enabled ->
                                                viewModel.setReduceMotion(enabled)
                                            }
                                        )
                                    },
                                    showDivider = false
                                )
                            }
                        }
                    }

                    // Seção 2: Tela Inicial & HUD
                    if (q.isEmpty() || "tela inicial relógio dock dynamic island stack widgets".contains(q)) {
                        item(key = "sec_home_and_hud") {
                            GroupedSection(header = "Tela Inicial & HUD") {
                                // Relógio Hero Monumental
                                GroupedRow(
                                    title = "Relógio Monumental",
                                    subtitle = "Tipografia, estilo e números tabulares",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Schedule,
                                            backgroundColor = Color(0xFFFF2D55)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        showClockCustomizer = true
                                    }
                                )

                                // Dynamic Island
                                GroupedRow(
                                    title = "Dynamic Island",
                                    subtitle = "Ilha interativa de mídia, bateria e live activities",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Notifications,
                                            backgroundColor = Color(0xFF007AFF)
                                        )
                                    },
                                    trailingContent = {
                                        AppleSwitch(
                                            checked = uiState.isLiveCapsuleEnabled,
                                            onCheckedChange = { viewModel.setLiveCapsuleEnabled(it) }
                                        )
                                    }
                                )

                                // Doca de Vidro (Smart Dock)
                                GroupedRow(
                                    title = "Doca Inteligente",
                                    subtitle = "Barra flutuante com previsão rápida de aplicativos",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Apps,
                                            backgroundColor = Color(0xFF34C759)
                                        )
                                    },
                                    trailingContent = {
                                        AppleSwitch(
                                            checked = uiState.isSmartDockEnabled,
                                            onCheckedChange = { viewModel.setSmartDockEnabled(it) }
                                        )
                                    }
                                )

                                // Smart Stack
                                GroupedRow(
                                    title = "Smart Stack",
                                    subtitle = "Empilhamento de widgets com rotação automática",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Layers,
                                            backgroundColor = Color(0xFFAF52DE)
                                        )
                                    },
                                    trailingContent = {
                                        AppleSwitch(
                                            checked = uiState.isSmartStackRotateEnabled,
                                            onCheckedChange = { viewModel.setSmartStackRotateEnabled(it) }
                                        )
                                    },
                                    showDivider = false
                                )
                            }
                        }
                    }

                    // Seção 3: Busca & Spotlight
                    if (q.isEmpty() || "busca spotlight pesquisa comandos atalhos".contains(q)) {
                        item(key = "sec_search_spotlight") {
                            GroupedSection(header = "Busca & Spotlight") {
                                GroupedRow(
                                    title = "Spotlight Search",
                                    subtitle = "Resultados unificados, contatos e IA",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Search,
                                            backgroundColor = Color(0xFF007AFF)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.SEARCH)
                                    }
                                )

                                GroupedRow(
                                    title = "Comandos & Atalhos Rápidos",
                                    subtitle = "@ai, @calc, @t, @msg e ações velozes",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Code,
                                            backgroundColor = Color(0xFF30D158)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.COMMANDS)
                                    }
                                )

                                GroupedRow(
                                    title = "Pesquisa no Conteúdo de Apps",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Language,
                                            backgroundColor = Color(0xFF5E5CE6)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.IN_APP_SEARCH)
                                    },
                                    showDivider = false
                                )
                            }
                        }
                    }

                    // Seção 4: Recursos & Produtividade
                    if (q.isEmpty() || "widgets feed notícias extras módulos".contains(q)) {
                        item(key = "sec_features") {
                            GroupedSection(header = "Recursos & Módulos") {
                                GroupedRow(
                                    title = "Galeria de Widgets",
                                    subtitle = "Gerencie widgets Small, Medium e Large",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Widgets,
                                            backgroundColor = Color(0xFFFF9500)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER)
                                    }
                                )

                                GroupedRow(
                                    title = "Feed Tessera News",
                                    subtitle = "Painel de notícias no padrão Apple News Today",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Feed,
                                            backgroundColor = Color(0xFFFF2D55)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.FEED)
                                    }
                                )

                                GroupedRow(
                                    title = "Módulos Extras",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Extension,
                                            backgroundColor = Color(0xFF00C7BE)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.EXTRAS)
                                    },
                                    showDivider = false
                                )
                            }
                        }
                    }

                    // Seção 5: Organização & Gestos
                    if (q.isEmpty() || "pastas ocultos gaveta gestos toque".contains(q)) {
                        item(key = "sec_organization") {
                            GroupedSection(header = "Organização & Gestos") {
                                GroupedRow(
                                    title = "Pastas de Aplicativos",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Folder,
                                            backgroundColor = Color(0xFF007AFF)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.FOLDERS)
                                    }
                                )

                                GroupedRow(
                                    title = "Aplicativos Ocultos",
                                    subtitle = "Protegidos por PIN e biometria",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Lock,
                                            backgroundColor = Color(0xFF8E8E93)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.HIDDEN_APPS)
                                    }
                                )

                                GroupedRow(
                                    title = "Gestos de Toque",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.TouchApp,
                                            backgroundColor = Color(0xFFFF9F0A)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.GESTURES)
                                    },
                                    showDivider = false
                                )
                            }
                        }
                    }

                    // Seção 6: Sistema & Privacidade
                    if (q.isEmpty() || "sistema privacidade permissões idioma desenvolvedor sobre".contains(q)) {
                        item(key = "sec_system") {
                            GroupedSection(header = "Sistema & Privacidade") {
                                GroupedRow(
                                    title = "Idioma",
                                    subtitle = if (uiState.selectedLanguage.isNotBlank()) uiState.selectedLanguage else "Padrão do Sistema",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Translate,
                                            backgroundColor = Color(0xFF007AFF)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.setLanguageModalOpen(true)
                                    }
                                )

                                GroupedRow(
                                    title = "Privacidade & Permissões",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Security,
                                            backgroundColor = Color(0xFF34C759)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.PERMISSIONS)
                                    }
                                )

                                GroupedRow(
                                    title = "Sobre o Tessera & Desenvolvedor",
                                    subtitle = "Versão 3.0 • Compilação 40",
                                    leadingIcon = {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Info,
                                            backgroundColor = Color(0xFF8E8E93)
                                        )
                                    },
                                    onClick = {
                                        Taptic.lightImpact(context)
                                        viewModel.navigateToSettingsSubScreen(SettingsSubScreen.DEVELOPER)
                                    },
                                    showDivider = false
                                )
                            }
                        }
                    }

                    item(key = "footer_info") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tessera Launcher 3.0",
                                style = typography.footnote.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                color = colors.secondaryLabel
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Designed in Cupertino • Made for Android",
                                style = typography.caption2,
                                color = colors.tertiaryLabel
                            )
                        }
                    }
                }
            }

            SettingsSubScreen.SEARCH -> SearchSettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                onNavigateToWidgetsCenter = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.WIDGETS_CENTER) },
                onRequestContactsPermission = onRequestContactsPermission,
                modifier = modifier
            )

            SettingsSubScreen.WIDGETS_CENTER -> WidgetsCenterScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.EXTRAS -> ExtrasScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                onNavigateToFolders = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.FOLDERS) },
                onNavigateToCommands = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.COMMANDS) },
                onOpenHiddenApps = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.HIDDEN_APPS) },
                modifier = modifier
            )

            SettingsSubScreen.FOLDERS -> FoldersScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.COMMANDS -> CommandsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.DEVELOPER -> DeveloperScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.TRANSPARENCY -> TransparencyScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.GESTURES -> GesturesScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.IN_APP_SEARCH -> InAppSearchSettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.HIDDEN_APPS -> HiddenAppsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.CUSTOMIZATION -> CustomizationScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                onPickWallpaper = onPickWallpaper,
                modifier = modifier
            )

            SettingsSubScreen.PERMISSIONS -> PermissionsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )

            SettingsSubScreen.FEED -> FeedSettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
        }
    }
}
