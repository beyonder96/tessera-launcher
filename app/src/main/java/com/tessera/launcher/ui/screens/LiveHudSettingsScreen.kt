package com.tessera.launcher.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.HudAppRule
import com.tessera.launcher.data.model.HudConfig
import com.tessera.launcher.data.model.HudDecision
import com.tessera.launcher.data.model.HudMilestone
import com.tessera.launcher.data.model.HudPeek
import com.tessera.launcher.data.model.HudReason
import com.tessera.launcher.data.model.HudSource
import com.tessera.launcher.data.model.HudState
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType
import com.tessera.launcher.data.model.ObservedApp
import com.tessera.launcher.data.model.PackageActivityDetails
import com.tessera.launcher.data.model.PackageStage
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.components.hud.TesseraIsland
import com.tessera.launcher.ui.state.AppsListState
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val SettingsCardShape = RoundedCornerShape(22.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveHudSettingsScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config = uiState.hudConfig
    val isLight = uiState.isLightMode
    val accent = uiState.activeAccentColor

    val backgroundColor = if (uiState.isAmoledMode) Color.Black else if (isLight) Color(0xFFF7F4EB) else Color(0xFF0D0D10)
    val cardBackground = if (uiState.isAmoledMode) Color(0xFF0A0A0E) else if (isLight) Color.White else Color(0xFF16161C)
    val cardBorder = if (uiState.isAmoledMode) Color(0xFF1E1E26) else if (isLight) Color(0xFFE5E0D5) else Color(0xFF242430)
    val textPrimary = if (isLight) Color(0xFF1A1A1E) else Color(0xFFEDEDED)
    val textSecondary = if (isLight) Color(0xFF767680) else Color(0xFF8E8E98)

    var previewMode by remember { mutableStateOf(0) } // 0: Compacta, 1: Split, 2: Expandida, 3: Peek
    var allAppsQuery by remember { mutableStateOf("") }
    var appForRuleConfig by remember { mutableStateOf<Pair<String, String>?>(null) } // (packageName, appName)

    // Dados simulados para a prévia ao vivo
    val mockPackageActivity = remember {
        LiveActivityInfo(
            id = "mock_pkg",
            packageName = "com.mercadolibre",
            type = LiveActivityType.PACKAGE,
            title = "Mercado Livre",
            subtitle = "Saiu para entrega",
            extraInfo = "Chega hoje até às 18h",
            progress = 85,
            appName = "Mercado Livre",
            brandColor = AndroidColor.rgb(255, 230, 0),
            criticalText = "Chega hoje até às 18h",
            milestones = listOf(
                HudMilestone("Comprado", true),
                HudMilestone("Enviado", true),
                HudMilestone("Em rota", true),
                HudMilestone("Entregue", false)
            ),
            packageDetails = PackageActivityDetails(
                appName = "Mercado Livre",
                stage = PackageStage.OUT_FOR_DELIVERY,
                headline = "Saiu para entrega",
                etaText = "Chega hoje até às 18h",
                trackingCode = "MLB389271924BR",
                itemSummary = "Fone de Ouvido Bluetooth Sem Fio",
                milestones = listOf(
                    HudMilestone("Comprado", true),
                    HudMilestone("Enviado", true),
                    HudMilestone("Em rota", true),
                    HudMilestone("Entregue", false)
                )
            ),
            source = HudSource.ADAPTER
        )
    }

    val mockRideActivity = remember {
        LiveActivityInfo(
            id = "mock_ride",
            packageName = "com.ubercab",
            type = LiveActivityType.RIDE,
            title = "Uber",
            subtitle = "Motorista chegando",
            criticalText = "2 min",
            brandColor = AndroidColor.rgb(37, 99, 235),
            appName = "Uber"
        )
    }

    val mockPeek = remember {
        HudPeek(
            key = "mock_peek",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "Mariana",
            text = "Chego em 5 minutos! Já pediu a comida?",
            brandColor = AndroidColor.rgb(0, 230, 118)
        )
    }

    val mockHudState = remember(previewMode) {
        when (previewMode) {
            0 -> HudState(primary = mockPackageActivity)
            1 -> HudState(primary = mockRideActivity, secondary = mockPackageActivity)
            2 -> HudState(primary = mockPackageActivity)
            3 -> HudState(primary = mockPackageActivity, peek = mockPeek)
            else -> HudState(primary = mockPackageActivity)
        }
    }

    val installedApps = remember(uiState.appsState, allAppsQuery) {
        val all = when (val state = uiState.appsState) {
            is AppsListState.Success -> state.apps
            else -> emptyList()
        }
        if (allAppsQuery.isBlank()) {
            all
        } else {
            all.filter { it.label.contains(allAppsQuery, ignoreCase = true) || it.packageName.contains(allAppsQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            // Barra superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = "Live HUD",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Ilha Dinâmica inteligente & Atividades ao vivo",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // ==========================================
        // 1. PRÉVIA INTERATIVA AO VIVO
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SettingsCardShape)
                    .background(cardBackground)
                    .border(0.6.dp, cardBorder, SettingsCardShape)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PRÉVIA AO VIVO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Área de simulação da Ilha
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F0F14))
                        .border(0.6.dp, Color(0xFF22222C), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    TesseraIsland(
                        hudState = mockHudState,
                        mediaPlayback = MediaPlaybackInfo(isPlaying = true, title = "Starboy", artist = "The Weeknd"),
                        isCharging = false,
                        batteryPercentage = 84,
                        defaultAccent = accent,
                        config = config,
                        onOpenApp = {},
                        onPlayPauseMedia = {},
                        onNextMedia = {},
                        onPreviousMedia = {},
                        onDismissPeek = { previewMode = 0 },
                        onDismissActivity = {}
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Seletor de estado da prévia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Compacta", "Split", "Expandida", "Alerta").forEachIndexed { index, label ->
                        FilterChip(
                            selected = previewMode == index,
                            onClick = { previewMode = index },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.2f),
                                selectedLabelColor = accent,
                                containerColor = Color.Transparent,
                                labelColor = textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = previewMode == index,
                                borderColor = if (previewMode == index) accent else cardBorder
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. CONFIGURAÇÕES GERAIS
        // ==========================================
        item {
            Text(
                text = "GERAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SettingsCardShape)
                    .background(cardBackground)
                    .border(0.6.dp, cardBorder, SettingsCardShape)
            ) {
                // Switch Ativar Live HUD
                HudToggleRow(
                    title = "Ativar Live HUD",
                    subtitle = "Exibe a pílula dinâmica no topo da tela inicial",
                    checked = config.enabled,
                    onCheckedChange = { viewModel.setLiveCapsuleEnabled(it) },
                    accent = accent,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                HorizontalDivider(color = cardBorder, thickness = 0.5.dp)

                // Switch Peek de Notificações
                HudToggleRow(
                    title = "Peek de Notificações",
                    subtitle = "A ilha expande temporariamente para mensagens e alertas importantes",
                    checked = config.peekEnabled,
                    onCheckedChange = { viewModel.updateHudConfig { c -> c.copy(peekEnabled = it) } },
                    accent = accent,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                AnimatedVisibility(visible = config.peekEnabled) {
                    Column {
                        HorizontalDivider(color = cardBorder, thickness = 0.5.dp)

                        // Duração do Peek
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Duração do Alerta",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Tempo que a pílula fica aberta",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(3000L to "3s", 4000L to "4s", 6000L to "6s").forEach { (ms, lbl) ->
                                    val isSelected = config.peekDurationMs == ms
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) accent.copy(alpha = 0.2f) else Color.Transparent)
                                            .border(1.dp, if (isSelected) accent else cardBorder, CircleShape)
                                            .clickable { viewModel.updateHudConfig { c -> c.copy(peekDurationMs = ms) } },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lbl,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) accent else textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = cardBorder, thickness = 0.5.dp)

                        // Ocultar conteúdo do peek (Privacidade)
                        HudToggleRow(
                            title = "Ocultar Conteúdo (Privacidade)",
                            subtitle = "Mostra apenas o remetente e o app, sem o corpo da mensagem",
                            checked = config.hidePeekContent,
                            onCheckedChange = { viewModel.updateHudConfig { c -> c.copy(hidePeekContent = it) } },
                            accent = accent,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }

                HorizontalDivider(color = cardBorder, thickness = 0.5.dp)

                // Switch Player de Mídia
                HudToggleRow(
                    title = "Player de Mídia",
                    subtitle = "Mostra ondas sonoras e controles de faixa musical",
                    checked = config.showMedia,
                    onCheckedChange = { viewModel.updateHudConfig { c -> c.copy(showMedia = it) } },
                    accent = accent,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                HorizontalDivider(color = cardBorder, thickness = 0.5.dp)

                // Switch Carregamento
                HudToggleRow(
                    title = "Status de Bateria & Carregamento",
                    subtitle = "Exibe pílula ao plugar o cabo no carregador",
                    checked = config.showCharging,
                    onCheckedChange = { viewModel.updateHudConfig { c -> c.copy(showCharging = it) } },
                    accent = accent,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                HorizontalDivider(color = cardBorder, thickness = 0.5.dp)

                // Switch Vibração Háptica
                HudToggleRow(
                    title = "Feedback Háptico",
                    subtitle = "Vibração tátil de precisão nos toques e gestos",
                    checked = config.haptics,
                    onCheckedChange = { viewModel.updateHudConfig { c -> c.copy(haptics = it) } },
                    accent = accent,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        }

        // ==========================================
        // 3. CATEGORIAS SUPORTADAS
        // ==========================================
        item {
            Text(
                text = "CATEGORIAS NA ILHA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SettingsCardShape)
                    .background(cardBackground)
                    .border(0.6.dp, cardBorder, SettingsCardShape)
            ) {
                val categories = listOf(
                    Triple(LiveActivityType.RIDE, "Corridas & Transporte", "Uber, 99, Didi"),
                    Triple(LiveActivityType.DELIVERY, "Delivery de Refeições", "iFood, Rappi, Zé Delivery"),
                    Triple(LiveActivityType.PACKAGE, "Encomendas & E-commerce", "Mercado Livre, Shopee, Amazon, Correios"),
                    Triple(LiveActivityType.NAVIGATION, "Navegação GPS", "Google Maps, Waze"),
                    Triple(LiveActivityType.CALL, "Chamadas VoIP & Voz", "WhatsApp, Meet, Telefone"),
                    Triple(LiveActivityType.TIMER, "Timers & Cronômetros", "Relógio, Alarme, Cronômetro"),
                    Triple(LiveActivityType.FITNESS, "Atividades & Treinos", "Strava, Samsung Health"),
                    Triple(LiveActivityType.PROGRESS, "Downloads & Progresso", "Play Store, Chrome, Live Updates")
                )

                categories.forEachIndexed { idx, (catType, title, desc) ->
                    val isCatEnabled = catType !in config.disabledTypes

                    HudToggleRow(
                        title = title,
                        subtitle = desc,
                        checked = isCatEnabled,
                        onCheckedChange = { checked ->
                            viewModel.updateHudConfig { c ->
                                val disabled = c.disabledTypes.toMutableSet()
                                if (checked) disabled.remove(catType) else disabled.add(catType)
                                c.copy(disabledTypes = disabled)
                            }
                        },
                        accent = accent,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )

                    if (idx < categories.size - 1) {
                        HorizontalDivider(color = cardBorder, thickness = 0.5.dp)
                    }
                }
            }
        }

        // ==========================================
        // 4. DETECTADOS RECENTEMENTE (TRANSPARÊNCIA)
        // ==========================================
        item {
            Text(
                text = "DETECTADOS RECENTEMENTE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Transparência do motor: entenda o que é classificado como Live, Aviso ou Ignorado",
                fontSize = 12.sp,
                color = textSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val observed = uiState.hudObservedApps
            if (observed.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SettingsCardShape)
                        .background(cardBackground)
                        .border(0.6.dp, cardBorder, SettingsCardShape)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma notificação observada desde a inicialização.",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SettingsCardShape)
                        .background(cardBackground)
                        .border(0.6.dp, cardBorder, SettingsCardShape)
                ) {
                    observed.take(8).forEachIndexed { idx, app ->
                        ObservedAppRow(
                            app = app,
                            currentRule = config.appRules[app.packageName] ?: HudAppRule.AUTO,
                            onClick = { appForRuleConfig = app.packageName to app.appName },
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                        if (idx < observed.take(8).size - 1) {
                            HorizontalDivider(color = cardBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. TODOS OS APLICATIVOS (REGRAS MANUAIS)
        // ==========================================
        item {
            Text(
                text = "REGRAS POR APLICATIVO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = allAppsQuery,
                onValueChange = { allAppsQuery = it },
                placeholder = { Text("Buscar app para personalizar...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        tint = textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = cardBorder,
                    focusedContainerColor = cardBackground,
                    unfocusedContainerColor = cardBackground,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Lista de apps instalados filtrados
        items(installedApps.take(30), key = { it.packageName }) { appInfo ->
            val rule = config.appRules[appInfo.packageName] ?: HudAppRule.AUTO

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBackground)
                    .border(0.6.dp, cardBorder, RoundedCornerShape(14.dp))
                    .tesseraPressFeedback()
                    .clickable { appForRuleConfig = appInfo.packageName to appInfo.label }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appInfo.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = appInfo.packageName,
                        fontSize = 11.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                RuleBadge(rule = rule, accent = accent)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal para seleção da regra do aplicativo
    if (appForRuleConfig != null) {
        val (pkg, name) = appForRuleConfig!!
        val currentRule = config.appRules[pkg] ?: HudAppRule.AUTO

        ModalBottomSheet(
            onDismissRequest = { appForRuleConfig = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = cardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Regra para $name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "Como a Ilha Dinâmica deve tratar este aplicativo:",
                    fontSize = 13.sp,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                listOf(
                    HudAppRule.AUTO to ("Automático" to "O classificador inteligente decide com base nos sinais do app"),
                    HudAppRule.LIVE_AND_PEEK to ("Live + Alertas" to "Permite tanto Live Activities quanto alertas pop-up"),
                    HudAppRule.LIVE_ONLY to ("Apenas Live Activity" to "Permite apenas atividades contínuas, sem alertas temporários"),
                    HudAppRule.PEEK_ONLY to ("Apenas Alertas (Peek)" to "Exibe apenas alertas rápidos, não fica fixo na ilha"),
                    HudAppRule.OFF to ("Desativado" to "Nunca exibe notificações nem atividades deste aplicativo")
                ).forEach { (r, info) ->
                    val (title, desc) = info
                    val isSelected = currentRule == r

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (isSelected) accent else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setHudAppRule(pkg, r)
                                appForRuleConfig = null
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) accent else textPrimary
                            )
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HudToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = textSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent,
                uncheckedThumbColor = textSecondary,
                uncheckedTrackColor = Color(0xFF222228),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ObservedAppRow(
    app: ObservedApp,
    currentRule: HudAppRule,
    onClick: () -> Unit,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tesseraPressFeedback()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.appName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                DecisionChip(decision = app.lastDecision)
            }

            val reasonLabel = when (app.lastReason) {
                HudReason.NATIVE_LIVE_UPDATE -> "Live Update nativa (Android 16)"
                HudReason.CALL_STYLE -> "Chamada telefônica em andamento"
                HudReason.KNOWN_ADAPTER -> "Aplicativo com adaptador dedicado"
                HudReason.PACKAGE_TRACKING -> "Rastreamento de encomenda"
                HudReason.CHRONOMETER -> "Cronômetro ou contagem regressiva"
                HudReason.PROGRESS -> "Download ou barra de progresso"
                HudReason.MESSAGING -> "Mensagem de alta prioridade"
                HudReason.BACKGROUND_SERVICE -> "Serviço persistente em segundo plano"
                HudReason.USER_RULE -> "Regra personalizada do usuário"
                HudReason.SYSTEM -> "Notificação de sistema Android"
                HudReason.MEDIA_SESSION -> "Controlado pelo Media Session"
                HudReason.GROUP_SUMMARY -> "Resumo de grupo"
                HudReason.DND -> "Silenciado pelo Não Perturbe"
                HudReason.LOW_IMPORTANCE -> "Baixa relevância em tempo real"
                else -> "Análise automática"
            }

            Text(
                text = "$reasonLabel • Regra: ${ruleDisplay(currentRule)}",
                fontSize = 11.sp,
                color = textSecondary
            )
        }
    }
}

@Composable
private fun DecisionChip(decision: HudDecision) {
    val (label, bg, fg) = when (decision) {
        HudDecision.LIVE -> Triple("Live", Color(0xFF00E676).copy(alpha = 0.15f), Color(0xFF00E676))
        HudDecision.PEEK -> Triple("Aviso", Color(0xFF38BDF8).copy(alpha = 0.15f), Color(0xFF38BDF8))
        HudDecision.IGNORE -> Triple("Ignorado", Color(0xFF27272A), Color(0xFFA1A1AA))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
private fun RuleBadge(rule: HudAppRule, accent: Color) {
    val (text, color) = when (rule) {
        HudAppRule.AUTO -> "Auto" to Color(0xFF71717A)
        HudAppRule.LIVE_AND_PEEK -> "Live + Avisos" to accent
        HudAppRule.LIVE_ONLY -> "Só Live" to Color(0xFF00E676)
        HudAppRule.PEEK_ONLY -> "Só Avisos" to Color(0xFF38BDF8)
        HudAppRule.OFF -> "Desativado" to Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.6.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private fun ruleDisplay(rule: HudAppRule): String = when (rule) {
    HudAppRule.AUTO -> "Automático"
    HudAppRule.LIVE_AND_PEEK -> "Live + Avisos"
    HudAppRule.LIVE_ONLY -> "Só Live"
    HudAppRule.PEEK_ONLY -> "Só Avisos"
    HudAppRule.OFF -> "Desativado"
}
