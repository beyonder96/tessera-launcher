package com.tessera.launcher.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.FeedSource
import com.tessera.launcher.ui.components.TesseraSegmentedControl
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightDivider
import com.tessera.launcher.ui.theme.LightIconBackground
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val LateralCardShape = RoundedCornerShape(24.dp)

@Composable
fun LateralPanelSettingsScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLight = uiState.isLightMode
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 1)) }

    val screenBg = when {
        isLight -> LightBackground
        uiState.isAmoledMode -> AmoledBlack
        else -> DarkBackground
    }
    val cardBg = when {
        isLight -> LightCardBackground
        uiState.isAmoledMode -> Color(0xFF0F0F12)
        else -> DarkSurfaceVariant
    }
    val cardBorder = when {
        isLight -> LightCardBorder
        uiState.isAmoledMode -> Color(0xFF1D1D22)
        else -> DarkSurfaceBorder
    }
    val dividerColor = if (isLight) LightDivider else Color(0xFF18181D)
    val iconBg = if (isLight) LightIconBackground else Color(0xFF19191E)
    val primaryTextColor = if (isLight) LightTextPrimary else TextPrimary
    val secondaryTextColor = if (isLight) LightTextSecondary else TextSecondary
    val tertiaryTextColor = if (isLight) Color(0xFF625B55) else TextTertiary

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val safeTopPadding = maxOf(statusBarTop, cutoutTop).coerceAtLeast(36.dp) + 8.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(top = safeTopPadding)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        // Barra Superior Centrada com Botão Voltar
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
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = primaryTextColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "TELA LATERAL (−1)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = secondaryTextColor,
                textAlign = TextAlign.Center
            )
        }

        // Seletor Segmentado Estilo iOS no Topo: [ Central de Widgets | Feed Social ]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            TesseraSegmentedControl(
                options = listOf("Central de Widgets", "Feed Social"),
                selectedIndex = selectedTab,
                onOptionSelected = { selectedTab = it },
                isLightMode = isLight,
                height = 42.dp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Conteúdo com rolagem
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // MODO DE ATIVAÇÃO NA TELA INICIAL
            Text(
                text = "COMPORTAMENTO DA TELA LATERAL (−1)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 8.dp)
            )

            Surface(
                shape = LateralCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    LateralModeOptionRow(
                        title = "Desativada",
                        subtitle = "Nenhuma tela ao deslizar para a direita na Home",
                        isSelected = uiState.leftScreenMode == "disabled",
                        onClick = { viewModel.setLeftScreenMode("disabled") },
                        isLight = isLight,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    LateralModeOptionRow(
                        title = "Central de Widgets",
                        subtitle = "Abre diretamente sua página dedicada de widgets e atalhos",
                        isSelected = uiState.leftScreenMode == "widgets",
                        onClick = { viewModel.setLeftScreenMode("widgets") },
                        isLight = isLight,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    LateralModeOptionRow(
                        title = "Feed Social & Notícias",
                        subtitle = "Abre sua linha do tempo de notícias com resumos por IA",
                        isSelected = uiState.leftScreenMode == "feed",
                        onClick = { viewModel.setLeftScreenMode("feed") },
                        isLight = isLight,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    LateralModeOptionRow(
                        title = "Ambos (com Seletor no topo)",
                        subtitle = "Exibe o seletor estilo iOS no topo da Tela −1 para alternar entre ambos",
                        isSelected = uiState.leftScreenMode == "both",
                        onClick = { viewModel.setLeftScreenMode("both") },
                        isLight = isLight,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 0) {
                // ==========================================
                // ABA 0: CENTRAL DE WIDGETS
                // ==========================================
                Text(
                    text = "WIDGETS DA PÁGINA LATERAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = tertiaryTextColor,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                )

                Surface(
                    shape = LateralCardShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        LateralToggleRow(
                            icon = Icons.Outlined.AutoAwesome,
                            title = "Smart Glance (Now & Next)",
                            subtitle = "Resumo contextual de agenda, clima e tarefas no topo da shelf",
                            checked = uiState.isSmartGlanceEnabled,
                            onCheckedChange = { viewModel.setSmartGlanceEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.MenuBook,
                            title = "Notas e diário",
                            subtitle = "Notas rápidas, lista de afazeres e entradas do diário",
                            checked = uiState.isNotesWidgetEnabled,
                            onCheckedChange = { viewModel.setNotesWidgetEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.SportsEsports,
                            title = "Jogo do Dino",
                            subtitle = "Jogo retrô interativo do dinossauro pulando",
                            checked = uiState.isDinoWidgetEnabled,
                            onCheckedChange = { viewModel.setDinoWidgetEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.Apps,
                            title = "Apps Sugeridos (IA)",
                            subtitle = "Cards de apps sugeridos com base em padrões de uso",
                            checked = uiState.isSmartDockEnabled,
                            onCheckedChange = { viewModel.setSmartDockEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.HourglassEmpty,
                            title = "Tempo de tela (Bem-Estar)",
                            subtitle = "Tempo diário de uso e ranking dos principais aplicativos",
                            checked = uiState.isScreenTimeWidgetEnabled,
                            onCheckedChange = { viewModel.setScreenTimeWidgetEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SOBREPOSIÇÃO DE NOTIFICAÇÕES
                Text(
                    text = "NOTIFICAÇÕES & RECONHECIMENTO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = tertiaryTextColor,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                )

                Surface(
                    shape = LateralCardShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = primaryTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acesso a notificações",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = primaryTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (uiState.hasNotificationAccess) "Autorizado • Sincroniza player e badges" else "Necessário para controle de música",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = secondaryTextColor
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = tertiaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // COMPORTAMENTO & REPOUSO
                Text(
                    text = "COMPORTAMENTO & REPOUSO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = tertiaryTextColor,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                )

                Surface(
                    shape = LateralCardShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        LateralToggleRow(
                            icon = Icons.Outlined.FlashOn,
                            title = "Trocar ao tocar música",
                            subtitle = "Muda para o player de mídia automaticamente ao iniciar uma faixa",
                            checked = uiState.isSwitchOnMusicPlayEnabled,
                            onCheckedChange = { viewModel.setSwitchOnMusicPlayEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.SwapVert,
                            title = "Smart Stacks (Rotação Inteligente)",
                            subtitle = "Gire widgets com deslize vertical no topo da shelf",
                            checked = uiState.isSmartStackRotateEnabled,
                            onCheckedChange = { viewModel.setSmartStackRotateEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.BatteryChargingFull,
                            title = "Modo Repouso ao carregar",
                            subtitle = "Abre o relógio com foto e rotação livre ao conectar o carregador",
                            checked = uiState.isPhotoStandByOnChargeEnabled,
                            onCheckedChange = { viewModel.setPhotoStandByOnChargeEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

            } else {
                // ==========================================
                // ABA 1: FEED SOCIAL & NOTÍCIAS
                // ==========================================
                Text(
                    text = "ESTADO DO FEED SOCIAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = tertiaryTextColor,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                )

                Surface(
                    shape = LateralCardShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        LateralToggleRow(
                            icon = Icons.Outlined.AutoAwesome,
                            title = "Ativar Feed Social",
                            subtitle = "Linha do tempo de notícias e redes com resumos por IA",
                            checked = uiState.isFeedEnabled,
                            onCheckedChange = { viewModel.setFeedEnabled(it) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        if (uiState.isFeedEnabled) {
                            HorizontalDivider(color = dividerColor, thickness = 1.dp)

                            LateralToggleRow(
                                icon = Icons.Outlined.AutoAwesome,
                                title = "Resumos com IA",
                                subtitle = "Gera sínteses instantâneas dos artigos mais longos",
                                checked = uiState.isFeedAiSummariesEnabled,
                                onCheckedChange = { viewModel.setFeedAiSummariesEnabled(it) },
                                isLightMode = isLight,
                                iconBgColor = iconBg,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor
                            )
                        }
                    }
                }

                if (uiState.isFeedEnabled) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "PRESETS REGIONAIS RÁPIDOS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = tertiaryTextColor,
                        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = PillShape,
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setFeedEnabledSources(setOf(FeedSource.NEWS.name, FeedSource.REDDIT.name, FeedSource.BLUESKY.name))
                                    viewModel.setFeedSubreddits(listOf("tecnologia", "brasil", "gamesEcultura"))
                                    viewModel.setFeedBlueskyHandles(listOf("g1.globo.com", "tecmundo.com.br", "canaltech.com.br"))
                                    viewModel.refreshFeed()
                                }
                        ) {
                            Text(
                                text = "🇧🇷 Brasil (PT-BR)",
                                color = primaryTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        Surface(
                            shape = PillShape,
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setFeedEnabledSources(setOf(FeedSource.REDDIT.name, FeedSource.BLUESKY.name))
                                    viewModel.setFeedSubreddits(listOf("technology", "androiddev", "worldnews"))
                                    viewModel.setFeedBlueskyHandles(listOf("theverge.com", "techcrunch.com", "bsky.app"))
                                    viewModel.refreshFeed()
                                }
                        ) {
                            Text(
                                text = "🌐 Global (EN)",
                                color = primaryTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "FONTES DE CONTEÚDO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = tertiaryTextColor,
                        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                    )

                    Surface(
                        shape = LateralCardShape,
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            FeedSource.values().forEachIndexed { index, source ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleFeedSource(source) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = source.displayName,
                                        color = primaryTextColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Switch(
                                        checked = uiState.feedEnabledSources.contains(source),
                                        onCheckedChange = { viewModel.toggleFeedSource(source) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = if (isLight) LightBackground else Color.Black,
                                            checkedTrackColor = if (isLight) LightTextPrimary else Color.White,
                                            checkedBorderColor = if (isLight) LightTextPrimary else Color.White,
                                            uncheckedThumbColor = if (isLight) Color(0xFF8E8E93) else Color(0xFF636366),
                                            uncheckedTrackColor = if (isLight) Color(0xFFE5E0D0) else Color(0xFF2C2C2E)
                                        )
                                    )
                                }
                                if (index < FeedSource.values().size - 1) {
                                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                }
                            }
                        }
                    }

                    val isRedditEnabled = uiState.feedEnabledSources.any { it.name == "REDDIT" }
                    if (isRedditEnabled) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "SUBREDDITS (separados por vírgula)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = tertiaryTextColor,
                            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                        )

                        Surface(
                            shape = LateralCardShape,
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var textValue by remember(uiState.feedSubreddits) {
                                mutableStateOf(uiState.feedSubreddits.joinToString(", "))
                            }
                            BasicTextField(
                                value = textValue,
                                onValueChange = { newValue ->
                                    textValue = newValue
                                    val list = newValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.setFeedSubreddits(list)
                                },
                                textStyle = TextStyle(
                                    color = primaryTextColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(primaryTextColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }

                    val isBlueskyEnabled = uiState.feedEnabledSources.any { it.name == "BLUESKY" }
                    if (isBlueskyEnabled) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "BLUESKY HANDLES (separados por vírgula)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = tertiaryTextColor,
                            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                        )

                        Surface(
                            shape = LateralCardShape,
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var textValue by remember(uiState.feedBlueskyHandles) {
                                mutableStateOf(uiState.feedBlueskyHandles.joinToString(", "))
                            }
                            BasicTextField(
                                value = textValue,
                                onValueChange = { newValue ->
                                    textValue = newValue
                                    val list = newValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.setFeedBlueskyHandles(list)
                                },
                                textStyle = TextStyle(
                                    color = primaryTextColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(primaryTextColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun LateralModeOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isLight: Boolean,
    primaryColor: Color,
    secondaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = secondaryColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = if (isLight) LightTextPrimary else Color.White,
                unselectedColor = secondaryColor
            )
        )
    }
}

@Composable
private fun LateralToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLightMode: Boolean,
    iconBgColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryTextColor,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = primaryTextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isLightMode) LightBackground else Color.Black,
                checkedTrackColor = if (isLightMode) LightTextPrimary else Color.White,
                checkedBorderColor = if (isLightMode) LightTextPrimary else Color.White,
                uncheckedThumbColor = if (isLightMode) Color(0xFF8E8E93) else Color(0xFF636366),
                uncheckedTrackColor = if (isLightMode) Color(0xFFE5E0D0) else Color(0xFF2C2C2E)
            )
        )
    }
}
