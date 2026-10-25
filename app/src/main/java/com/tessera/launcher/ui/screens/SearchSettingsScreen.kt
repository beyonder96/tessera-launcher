package com.tessera.launcher.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.theme.DarkBackground
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

private val SearchCardShape = RoundedCornerShape(24.dp)

@Composable
fun SearchSettingsScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    onNavigateToWidgetsCenter: () -> Unit = {},
    onRequestContactsPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isLight = uiState.isLightMode

    val screenBg = if (isLight) LightBackground else DarkBackground
    val cardBg = if (isLight) LightCardBackground else Color(0xFF0F0F12)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF1D1D22)
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
        // Top Bar Centrada: Botão Voltar + "BUSCA"
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
                text = "BUSCA",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = secondaryTextColor,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Bloco 1: BUSCA E NAVEGAÇÃO
            Text(
                text = "BUSCA E NAVEGAÇÃO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 8.dp)
            )

            Surface(
                shape = SearchCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SearchToggleRow(
                        icon = Icons.Outlined.Keyboard,
                        title = "Abrir Teclado",
                        subtitle = "Abre o teclado assim que você entra.",
                        checked = uiState.isAutoOpenKeyboard,
                        onCheckedChange = { viewModel.setAutoOpenKeyboard(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.TouchApp,
                        title = "Abrir Primeiro App",
                        subtitle = "Ao pressionar Enter, abre o primeiro app.",
                        checked = uiState.isAutoLaunchEnabled,
                        onCheckedChange = { viewModel.setAutoLaunchEnabled(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.VpnKey,
                        title = "Busca exata de apps",
                        subtitle = "Só mostra apps com o nome exatamente igual.",
                        checked = uiState.isExactSearchEnabled,
                        onCheckedChange = { viewModel.setExactSearchEnabled(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.FlashOn,
                        title = "Atalhos de apps",
                        subtitle = "Mostra os atalhos do Android ao segurar um app.",
                        checked = uiState.isAppShortcutsEnabled,
                        onCheckedChange = { viewModel.setAppShortcutsEnabled(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bloco 2: BUSCA EM APPS
            Text(
                text = "BUSCA EM APPS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = SearchCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SearchToggleRow(
                        icon = Icons.Outlined.Language,
                        title = "Busca Web",
                        subtitle = "Busca na web quando nenhum app combina.",
                        checked = uiState.isWebSearchEnabled,
                        onCheckedChange = { viewModel.setWebSearchEnabled(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    SearchNavRow(
                        icon = Icons.Outlined.Tune,
                        title = "Busca em apps",
                        subtitle = "Busque dentro de apps como Spotify e YouTube.",
                        onClick = {
                            viewModel.navigateToSettingsSubScreen(SettingsSubScreen.IN_APP_SEARCH)
                        },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor,
                        tertiaryColor = tertiaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.People,
                        title = "Busca de Contatos",
                        subtitle = if (uiState.hasContactsPermission) "Encontre e ligue para contatos pela barra de busca." else "Toque para autorizar e buscar contatos na barra",
                        checked = uiState.isContactsSearchEnabled,
                        onCheckedChange = { enable ->
                            viewModel.setContactsSearchEnabled(enable)
                            if (enable && !uiState.hasContactsPermission) {
                                onRequestContactsPermission()
                            }
                        },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.Chat,
                        title = "Busca de mensagens",
                        subtitle = "Busca mensagens recentes das suas notificações.",
                        checked = uiState.isMessagesSearchEnabled,
                        onCheckedChange = { viewModel.setMessagesSearchEnabled(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.Calculate,
                        title = "Cartão de calculadora",
                        subtitle = "Faça contas direto na barra de busca",
                        checked = uiState.isCalculatorCardEnabled,
                        onCheckedChange = { viewModel.setCalculatorCardEnabled(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )
                }
            }

            // Bloco 3: INTELIGÊNCIA ARTIFICIAL (GROQ & GEMINI)
            Text(
                text = "INTELIGÊNCIA ARTIFICIAL (GROQ & GEMINI)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, top = 20.dp, bottom = 8.dp)
            )

            Surface(
                shape = SearchCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SearchToggleRow(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "Respostas com IA na Busca",
                        subtitle = "Respostas instantâneas do Groq/Gemini ao pesquisar ou com @groq/@ai",
                        checked = uiState.isAiSearchEnabled,
                        onCheckedChange = { viewModel.setAiSearchEnabled(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "Luz de IA ao Expandir",
                        subtitle = "Borda de luz animada estilo ativação de IA",
                        checked = uiState.isGeminiGlowEnabled,
                        onCheckedChange = { viewModel.setGeminiGlowEnabled(it) },
                        isLight = isLight,
                        iconBg = iconBg,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )

                    if (uiState.isAiSearchEnabled) {
                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        // Seletor de Provedor de IA
                        var showProviderDialog by remember { mutableStateOf(false) }
                        SearchNavRow(
                            icon = Icons.Outlined.RocketLaunch,
                            title = "Provedor de IA",
                            subtitle = if (uiState.aiProvider.equals("GEMINI", true)) "Google Gemini 3.8 Flash" else "Groq Cloud (Gratuito e Ultra-rápido)",
                            onClick = { showProviderDialog = true },
                            isLight = isLight,
                            iconBg = iconBg,
                            primaryColor = primaryTextColor,
                            secondaryColor = secondaryTextColor,
                            tertiaryColor = tertiaryTextColor
                        )

                        if (showProviderDialog) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showProviderDialog = false },
                                containerColor = cardBg,
                                title = {
                                    Text("Escolha o Provedor de IA", color = primaryTextColor, fontWeight = FontWeight.Bold)
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val groqSelected = uiState.aiProvider.equals("GROQ", true)
                                        val geminiSelected = uiState.aiProvider.equals("GEMINI", true)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (groqSelected) (if (isLight) Color(0xFFE8E4D8) else Color(0xFF1E1E26)) else Color.Transparent)
                                                .clickable {
                                                    viewModel.setAiProvider("GROQ")
                                                    showProviderDialog = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Groq Cloud IA", fontWeight = FontWeight.SemiBold, color = primaryTextColor)
                                                Text("Inferência ultra-rápida, tier 100% gratuito (Recomendado)", fontSize = 12.sp, color = secondaryTextColor)
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (geminiSelected) (if (isLight) Color(0xFFE8E4D8) else Color(0xFF1E1E26)) else Color.Transparent)
                                                .clickable {
                                                    viewModel.setAiProvider("GEMINI")
                                                    showProviderDialog = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Google Gemini 3.8 Flash", fontWeight = FontWeight.SemiBold, color = primaryTextColor)
                                                Text("Modelo mais recente do Google via AI Studio", fontSize = 12.sp, color = secondaryTextColor)
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showProviderDialog = false }) {
                                        Text("Fechar", color = primaryTextColor)
                                    }
                                }
                            )
                        }

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        // Configuração da Chave Groq
                        var showGroqKeyDialog by remember { mutableStateOf(false) }
                        SearchNavRow(
                            icon = Icons.Outlined.VpnKey,
                            title = "Chave de API Groq (Gratuita)",
                            subtitle = if (uiState.groqApiKey.isNotBlank()) "Chave configurada (toque para alterar)" else "Toque para inserir sua chave gratuita do Groq",
                            onClick = { showGroqKeyDialog = true },
                            isLight = isLight,
                            iconBg = iconBg,
                            primaryColor = primaryTextColor,
                            secondaryColor = secondaryTextColor,
                            tertiaryColor = tertiaryTextColor
                        )

                        if (showGroqKeyDialog) {
                            var tempKey by remember { mutableStateOf(uiState.groqApiKey) }
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showGroqKeyDialog = false },
                                containerColor = cardBg,
                                title = {
                                    Text("Chave de API do Groq", color = primaryTextColor, fontWeight = FontWeight.Bold)
                                },
                                text = {
                                    Column {
                                        Text(
                                            text = "Obtenha uma chave gratuita instantaneamente em console.groq.com/keys e cole abaixo:",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = secondaryTextColor
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedTextField(
                                            value = tempKey,
                                            onValueChange = { tempKey = it },
                                            placeholder = { Text("gsk_...", color = tertiaryTextColor, fontSize = 13.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.setGroqApiKey(tempKey)
                                            showGroqKeyDialog = false
                                            Toast.makeText(context, "Chave Groq salva", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Salvar", color = primaryTextColor, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showGroqKeyDialog = false }) {
                                        Text("Cancelar", color = secondaryTextColor)
                                    }
                                }
                            )
                        }

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        // Configuração da Chave Gemini
                        var showGeminiKeyDialog by remember { mutableStateOf(false) }
                        SearchNavRow(
                            icon = Icons.Outlined.VpnKey,
                            title = "Chave de API do Gemini",
                            subtitle = if (uiState.geminiApiKey.isNotBlank()) "Chave configurada (toque para alterar)" else "Toque para inserir sua chave do Google Gemini",
                            onClick = { showGeminiKeyDialog = true },
                            isLight = isLight,
                            iconBg = iconBg,
                            primaryColor = primaryTextColor,
                            secondaryColor = secondaryTextColor,
                            tertiaryColor = tertiaryTextColor
                        )

                        if (showGeminiKeyDialog) {
                            var tempKey by remember { mutableStateOf(uiState.geminiApiKey) }
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showGeminiKeyDialog = false },
                                containerColor = cardBg,
                                title = {
                                    Text("Chave de API do Gemini", color = primaryTextColor, fontWeight = FontWeight.Bold)
                                },
                                text = {
                                    Column {
                                        Text(
                                            text = "Obtenha gratuitamente em aistudio.google.com e cole abaixo:",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = secondaryTextColor
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedTextField(
                                            value = tempKey,
                                            onValueChange = { tempKey = it },
                                            placeholder = { Text("AIzaSy...", color = tertiaryTextColor, fontSize = 13.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.setGeminiApiKey(tempKey)
                                            showGeminiKeyDialog = false
                                            Toast.makeText(context, "Chave Gemini salva", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Salvar", color = primaryTextColor, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showGeminiKeyDialog = false }) {
                                        Text("Cancelar", color = secondaryTextColor)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SearchToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLight: Boolean = false,
    iconBg: Color = Color(0xFF19191E),
    primaryColor: Color = TextPrimary,
    secondaryColor: Color = TextSecondary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
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
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = secondaryColor
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isLight) LightBackground else Color.Black,
                checkedTrackColor = if (isLight) LightTextPrimary else Color.White,
                checkedBorderColor = if (isLight) LightTextPrimary else Color.White,
                uncheckedThumbColor = if (isLight) Color(0xFF8E8E93) else Color(0xFF8E8E93),
                uncheckedTrackColor = if (isLight) Color(0xFFE5E0D0) else Color(0xFF1E1E24),
                uncheckedBorderColor = if (isLight) Color(0xFFDDD8CB) else Color(0xFF2C2C34)
            )
        )
    }
}

@Composable
private fun SearchNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLight: Boolean = false,
    iconBg: Color = Color(0xFF19191E),
    primaryColor: Color = TextPrimary,
    secondaryColor: Color = TextSecondary,
    tertiaryColor: Color = TextTertiary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
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
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = secondaryColor
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = tertiaryColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
