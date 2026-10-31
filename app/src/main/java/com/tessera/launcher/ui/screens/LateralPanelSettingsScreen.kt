package com.tessera.launcher.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.ui.components.TesseraSegmentedControl
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightDivider
import com.tessera.launcher.ui.theme.LightIconBackground
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
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

        // Seletor Segmentado Estilo iOS no Topo: [ Central de Widgets | Feed de Notícias ]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            TesseraSegmentedControl(
                options = listOf("Central de Widgets", "Feed de Notícias"),
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
                        title = "Feed de Notícias (RSS)",
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
                    text = "WIDGETS DA CENTRAL BENTO",
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
                            icon = Icons.Outlined.MusicNote,
                            title = "Palco Apple Music",
                            subtitle = "Player de mídia hero com capa e controles musicais",
                            checked = uiState.enabledWidgets.contains(WidgetType.MEDIA),
                            onCheckedChange = { viewModel.toggleWidget(WidgetType.MEDIA) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.WbSunny,
                            title = "Clima e Previsão",
                            subtitle = "Temperatura local e condição meteorológica em tempo real",
                            checked = uiState.enabledWidgets.contains(WidgetType.WEATHER),
                            onCheckedChange = { viewModel.toggleWidget(WidgetType.WEATHER) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.CalendarToday,
                            title = "Agenda e Calendário",
                            subtitle = "Próximo compromisso e data editorial sincronizada",
                            checked = uiState.enabledWidgets.contains(WidgetType.CALENDAR),
                            onCheckedChange = { viewModel.toggleWidget(WidgetType.CALENDAR) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.BatteryStd,
                            title = "Nível de Bateria",
                            subtitle = "Percentual de bateria e status do carregamento",
                            checked = uiState.enabledWidgets.contains(WidgetType.BATTERY),
                            onCheckedChange = { viewModel.toggleWidget(WidgetType.BATTERY) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor
                        )

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        LateralToggleRow(
                            icon = Icons.Outlined.SportsEsports,
                            title = "Jogo do Dino (Chrome Retro)",
                            subtitle = "Minigame retrô autêntico em pixel-art com dinossauro pulando",
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
                            title = "Dock de Apps Sugeridos",
                            subtitle = "Atalhos preditivos para os aplicativos mais utilizados",
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
                            title = "Tempo de Tela (Bem-Estar)",
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
                                text = if (uiState.hasNotificationAccess) "Autorizado • Sincroniza player e deck estilo iOS" else "Necessário para controle de música e notificações",
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

                // WIDGETS DO ANDROID (TERCEIROS)
                Text(
                    text = "WIDGETS DO ANDROID (TERCEIROS)",
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
                                imageVector = Icons.Outlined.Widgets,
                                contentDescription = null,
                                tint = primaryTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Widgets Hospedados",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = primaryTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${uiState.hostedWidgetIds.size} widget(s) de terceiros adicionados. Gerencie e adicione mais diretamente no rodapé da Central de Widgets.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = secondaryTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

            } else {
                // ==========================================
                // ABA 1: FEED DE NOTÍCIAS (RSS)
                // ==========================================
                Text(
                    text = "ESTADO DO FEED DE NOTÍCIAS",
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
                            icon = Icons.Outlined.RssFeed,
                            title = "Ativar Feed de Notícias",
                            subtitle = "Linha do tempo de notícias dos seus feeds RSS cadastrados",
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

                    // SEÇÃO DE CADASTRO DE NOVA FONTE RSS
                    Text(
                        text = "ADICIONAR NOVA FONTE RSS",
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
                        var newTitle by remember { mutableStateOf("") }
                        var newUrl by remember { mutableStateOf("") }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Nome da Fonte (ex: G1 Tecnologia)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryTextColor,
                                    unfocusedBorderColor = cardBorder,
                                    focusedLabelColor = primaryTextColor,
                                    unfocusedLabelColor = secondaryTextColor
                                )
                            )

                            OutlinedTextField(
                                value = newUrl,
                                onValueChange = { newUrl = it },
                                label = { Text("URL do Feed RSS / Atom") },
                                placeholder = { Text("https://exemplo.com/rss") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryTextColor,
                                    unfocusedBorderColor = cardBorder,
                                    focusedLabelColor = primaryTextColor,
                                    unfocusedLabelColor = secondaryTextColor
                                )
                            )

                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank() && newUrl.isNotBlank()) {
                                        viewModel.addCustomRssSource(newTitle.trim(), newUrl.trim())
                                        newTitle = ""
                                        newUrl = ""
                                    }
                                },
                                enabled = newTitle.isNotBlank() && newUrl.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLight) Color(0xFF1C1917) else Color(0xFFF7F4EB),
                                    contentColor = if (isLight) Color(0xFFF7F4EB) else Color(0xFF1C1917)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cadastrar Fonte",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // LISTA DE FONTES RSS CADASTRADAS
                    Text(
                        text = "FONTES CADASTRADAS (${uiState.customRssSources.size})",
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
                        if (uiState.customRssSources.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RssFeed,
                                    contentDescription = null,
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nenhuma fonte RSS cadastrada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryTextColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cadastre URLs de feeds RSS dos seus portais de notícias favoritos no formulário acima.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                uiState.customRssSources.forEachIndexed { index, source ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = source.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.5.sp
                                                ),
                                                color = primaryTextColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = source.url,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.5.sp
                                                ),
                                                color = secondaryTextColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Switch(
                                            checked = source.isEnabled,
                                            onCheckedChange = { viewModel.toggleCustomRssSource(source.id) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = if (isLight) LightBackground else Color.Black,
                                                checkedTrackColor = if (isLight) LightTextPrimary else Color.White,
                                                checkedBorderColor = if (isLight) LightTextPrimary else Color.White,
                                                uncheckedThumbColor = if (isLight) Color(0xFF8E8E93) else Color(0xFF636366),
                                                uncheckedTrackColor = if (isLight) Color(0xFFE5E0D0) else Color(0xFF2C2C2E)
                                            )
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        IconButton(
                                            onClick = { viewModel.removeCustomRssSource(source.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Excluir fonte",
                                                tint = if (isLight) Color(0xFFBA1A1A) else Color(0xFFFFB4AB),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    if (index < uiState.customRssSources.size - 1) {
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                    }
                                }
                            }
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
