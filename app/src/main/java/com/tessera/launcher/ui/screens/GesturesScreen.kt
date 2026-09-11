package com.tessera.launcher.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.state.AppShortcutItem
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val GesturesCardShape = RoundedCornerShape(24.dp)
private val GesturesCardBackground = Color(0xFF0F0F12)
private val GesturesCardBorder = Color(0xFF1D1D22)
private val GesturesDividerColor = Color(0xFF18181D)
private val GesturesIconBackground = Color(0xFF19191E)

fun getActionDisplayName(actionKey: String, apps: List<AppInfo>): String {
    return when {
        actionKey == "launcher_settings" -> "Configurações do launcher"
        actionKey == "system_settings" -> "Configurações do sistema"
        actionKey == "notifications" -> "Notificações"
        actionKey == "quick_settings" -> "Configurações rápidas"
        actionKey == "lock_screen" -> "Bloquear Tela"
        actionKey == "open_keyboard" -> "Abrir teclado"
        actionKey == "open_feed" -> "Abrir Feed Social"
        actionKey.startsWith("app:") -> {
            val pkg = actionKey.removePrefix("app:")
            apps.firstOrNull { it.packageName == pkg }?.label ?: "Abrir aplicativo"
        }
        actionKey.startsWith("shortcut:") -> {
            val parts = actionKey.removePrefix("shortcut:").split(":::")
            if (parts.size >= 3) parts[2] else "Atalho"
        }
        else -> "Ação"
    }
}

enum class GestureTarget {
    DOUBLE_TAP,
    HOLD,
    SWIPE_DOWN,
    SWIPE_UP,
    SWIPE_LEFT,
    SWIPE_RIGHT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GesturesScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeGesturePicker by remember { mutableStateOf<GestureTarget?>(null) }
    var isBrowsingApps by remember { mutableStateOf(false) }

    val allApps = (uiState.appsState as? com.tessera.launcher.ui.state.AppsListState.Success)?.apps ?: emptyList()

    val statusBarPadding = if (uiState.isShowStatusBarEnabled) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = statusBarPadding)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        // Top Bar Centrada: Seta voltar + "GESTOS"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "GESTOS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Seção 1: GESTOS DE MOVIMENTO
            Text(
                text = "GESTOS DE MOVIMENTO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = GesturesCardShape,
                color = GesturesCardBackground,
                border = BorderStroke(1.dp, GesturesCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GesturesIconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Vibration,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gestos de Movimento",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Gire, balance ou incline para realizar ações.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção 2: GESTOS DO SISTEMA
            Text(
                text = "GESTOS DO SISTEMA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = GesturesCardShape,
                color = GesturesCardBackground,
                border = BorderStroke(1.dp, GesturesCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // 1. Toque Duplo
                    GestureToggleRow(
                        icon = Icons.Outlined.TouchApp,
                        title = "Gesto de toque duplo",
                        subtitle = "Toque duas vezes na tela inicial.",
                        checked = uiState.isDoubleTapEnabled,
                        onCheckedChange = { viewModel.setDoubleTapEnabled(it) }
                    )
                    if (uiState.isDoubleTapEnabled) {
                        HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)
                        GestureActionRow(
                            title = "Ação de toque duplo",
                            subtitle = getActionDisplayName(uiState.doubleTapAction, allApps),
                            onClick = {
                                isBrowsingApps = false
                                activeGesturePicker = GestureTarget.DOUBLE_TAP
                            }
                        )
                    }

                    HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)

                    // 2. Gesto de Segurar
                    GestureToggleRow(
                        icon = Icons.Outlined.Fingerprint,
                        title = "Gesto de Segurar",
                        subtitle = "Segure na tela inicial.",
                        checked = uiState.isHoldEnabled,
                        onCheckedChange = { viewModel.setHoldEnabled(it) }
                    )
                    if (uiState.isHoldEnabled) {
                        HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)
                        GestureActionRow(
                            title = "Ação ao segurar",
                            subtitle = getActionDisplayName(uiState.holdAction, allApps),
                            onClick = {
                                isBrowsingApps = false
                                activeGesturePicker = GestureTarget.HOLD
                            }
                        )
                    }

                    HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)

                    // 3. Deslizar para baixo
                    GestureToggleRow(
                        icon = Icons.Outlined.ArrowDownward,
                        title = "Gesto de deslizar para b...",
                        subtitle = "Deslize para baixo na tela inicial.",
                        checked = uiState.isSwipeDownEnabled,
                        onCheckedChange = { viewModel.setSwipeDownEnabled(it) }
                    )
                    if (uiState.isSwipeDownEnabled) {
                        HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)
                        GestureActionRow(
                            title = "Ação de deslizar para baixo",
                            subtitle = getActionDisplayName(uiState.swipeDownAction, allApps),
                            onClick = {
                                isBrowsingApps = false
                                activeGesturePicker = GestureTarget.SWIPE_DOWN
                            }
                        )
                    }

                    HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)

                    // 4. Deslizar para cima
                    GestureToggleRow(
                        icon = Icons.Outlined.ArrowUpward,
                        title = "Gesto de deslizar para ci...",
                        subtitle = "Deslize de baixo para cima.",
                        checked = uiState.isSwipeUpEnabled,
                        onCheckedChange = { viewModel.setSwipeUpEnabled(it) }
                    )
                    if (uiState.isSwipeUpEnabled) {
                        HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)
                        GestureActionRow(
                            title = "Ação ao deslizar para cima",
                            subtitle = getActionDisplayName(uiState.swipeUpAction, allApps),
                            onClick = {
                                isBrowsingApps = false
                                activeGesturePicker = GestureTarget.SWIPE_UP
                            }
                        )
                    }

                    HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)

                    // 5. Deslizar para a esquerda
                    GestureToggleRow(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        title = "Gesto de deslizar para a ...",
                        subtitle = "Deslize para a esquerda na tela inicial.",
                        checked = uiState.isSwipeLeftEnabled,
                        onCheckedChange = { viewModel.setSwipeLeftEnabled(it) }
                    )
                    if (uiState.isSwipeLeftEnabled) {
                        HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)
                        GestureActionRow(
                            title = "Ação ao deslizar para a esquerda",
                            subtitle = getActionDisplayName(uiState.swipeLeftAction, allApps),
                            onClick = {
                                isBrowsingApps = false
                                activeGesturePicker = GestureTarget.SWIPE_LEFT
                            }
                        )
                    }

                    HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)

                    // 6. Deslizar para a direita
                    GestureToggleRow(
                        icon = Icons.AutoMirrored.Outlined.ArrowForward,
                        title = "Gesto de deslizar para a ...",
                        subtitle = "Deslize para a direita na tela inicial.",
                        checked = uiState.isSwipeRightEnabled,
                        onCheckedChange = { viewModel.setSwipeRightEnabled(it) }
                    )
                    if (uiState.isSwipeRightEnabled) {
                        HorizontalDivider(color = GesturesDividerColor, thickness = 1.dp)
                        GestureActionRow(
                            title = "Ação ao deslizar para a direita",
                            subtitle = getActionDisplayName(uiState.swipeRightAction, allApps),
                            onClick = {
                                isBrowsingApps = false
                                activeGesturePicker = GestureTarget.SWIPE_RIGHT
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // BottomSheet: ESCOLHER AÇÃO / NAVEGAR APPS
    if (activeGesturePicker != null) {
        val currentTarget = activeGesturePicker!!
        val currentAction = when (currentTarget) {
            GestureTarget.DOUBLE_TAP -> uiState.doubleTapAction
            GestureTarget.HOLD -> uiState.holdAction
            GestureTarget.SWIPE_DOWN -> uiState.swipeDownAction
            GestureTarget.SWIPE_UP -> uiState.swipeUpAction
            GestureTarget.SWIPE_LEFT -> uiState.swipeLeftAction
            GestureTarget.SWIPE_RIGHT -> uiState.swipeRightAction
        }

        val onSelectAction: (String) -> Unit = { newAction ->
            when (currentTarget) {
                GestureTarget.DOUBLE_TAP -> viewModel.setDoubleTapAction(newAction)
                GestureTarget.HOLD -> viewModel.setHoldAction(newAction)
                GestureTarget.SWIPE_DOWN -> viewModel.setSwipeDownAction(newAction)
                GestureTarget.SWIPE_UP -> viewModel.setSwipeUpAction(newAction)
                GestureTarget.SWIPE_LEFT -> viewModel.setSwipeLeftAction(newAction)
                GestureTarget.SWIPE_RIGHT -> viewModel.setSwipeRightAction(newAction)
            }
            activeGesturePicker = null
            isBrowsingApps = false
        }

        ModalBottomSheet(
            onDismissRequest = {
                activeGesturePicker = null
                isBrowsingApps = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF0F0F12),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333338))
                )
            }
        ) {
            if (!isBrowsingApps) {
                // Vista 1: ESCOLHER AÇÃO (Imagem 2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "ESCOLHER AÇÃO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )

                    // Seção: CONFIGURAÇÕES SEARCHO
                    Text(
                        text = "CONFIGURAÇÕES SEARCHO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = TextTertiary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ActionOptionItem(
                        icon = Icons.Outlined.Tune,
                        title = "Configurações do launcher",
                        isSelected = currentAction == "launcher_settings",
                        onClick = { onSelectAction("launcher_settings") }
                    )
                    ActionOptionItem(
                        icon = Icons.Outlined.Settings,
                        title = "Configurações do sistema",
                        isSelected = currentAction == "system_settings",
                        onClick = { onSelectAction("system_settings") }
                    )
                    ActionOptionItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notificações",
                        isSelected = currentAction == "notifications",
                        onClick = { onSelectAction("notifications") }
                    )
                    ActionOptionItem(
                        icon = Icons.Outlined.Lock,
                        title = "Bloquear Tela",
                        isSelected = currentAction == "lock_screen",
                        onClick = { onSelectAction("lock_screen") }
                    )
                    ActionOptionItem(
                        icon = Icons.Outlined.Keyboard,
                        title = "Abrir teclado",
                        isSelected = currentAction == "open_keyboard",
                        onClick = { onSelectAction("open_keyboard") }
                    )
                    ActionOptionItem(
                        icon = Icons.Outlined.Dashboard,
                        title = "Abrir Feed Social",
                        isSelected = currentAction == "open_feed",
                        onClick = { onSelectAction("open_feed") }
                    )
                    ActionOptionItem(
                        icon = Icons.Outlined.SettingsApplications,
                        title = "Configurações rápidas",
                        isSelected = currentAction == "quick_settings",
                        onClick = { onSelectAction("quick_settings") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seção: APLICAÇÕES
                    Text(
                        text = "APLICAÇÕES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = TextTertiary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isBrowsingApps = true }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF19191E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GridView,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Navegar Apps",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // Vista 2: NAVEGAR APPS (Imagem 1)
                BrowseAppsSheetContent(
                    viewModel = viewModel,
                    allApps = allApps,
                    currentAction = currentAction,
                    onBack = { isBrowsingApps = false },
                    onSelectAction = onSelectAction
                )
            }
        }
    }
}

@Composable
private fun BrowseAppsSheetContent(
    viewModel: MainViewModel,
    allApps: List<AppInfo>,
    currentAction: String,
    onBack: () -> Unit,
    onSelectAction: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedPackage by remember { mutableStateOf<String?>(null) }
    var appShortcutsMap by remember { mutableStateOf<Map<String, List<AppShortcutItem>>>(emptyMap()) }

    val filtered = remember(searchQuery, allApps) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter { it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(horizontal = 20.dp)
    ) {
        // Header com seta voltar e "NAVEGAR APPS"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NAVEGAR APPS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = TextTertiary
            )
        }

        // Barra de Busca "Searcho..."
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xFF16161A), RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(TextPrimary),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text("Searcho...", color = TextTertiary, fontSize = 14.sp)
                        }
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Lista de Apps com expansão e atalhos
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                val isExpanded = expandedPackage == app.packageName
                val shortcuts = appShortcutsMap[app.packageName] ?: emptyList()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Linha do Aplicativo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (isExpanded) {
                                    expandedPackage = null
                                } else {
                                    expandedPackage = app.packageName
                                    if (!appShortcutsMap.containsKey(app.packageName)) {
                                        val list = viewModel.getAppShortcuts(app.packageName)
                                        appShortcutsMap = appShortcutsMap + (app.packageName to list)
                                    }
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF19191E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.firstLetter.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Se expandido: mostra "Abrir <App>" e os atalhos dinâmicos
                    if (isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 50.dp, bottom = 8.dp)
                        ) {
                            // Ação principal: Abrir App
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectAction("app:${app.packageName}") }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Abrir ${app.label}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Atalhos do aplicativo (Shortcuts)
                            shortcuts.forEach { shortcut ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onSelectAction("shortcut:${app.packageName}:::${shortcut.id}:::${shortcut.shortLabel}")
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = shortcut.shortLabel.ifBlank { shortcut.longLabel ?: "Atalho" },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = TextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionOptionItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF1B1C22) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF19191E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun GestureToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GesturesIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.White,
                uncheckedThumbColor = Color(0xFF636366),
                uncheckedTrackColor = Color(0xFF2C2C2E)
            )
        )
    }
}

@Composable
private fun GestureActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GesturesIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
