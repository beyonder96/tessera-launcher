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
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val SearchCardShape = RoundedCornerShape(24.dp)
private val SearchCardBackground = Color(0xFF0F0F12)
private val SearchCardBorder = Color(0xFF1D1D22)
private val SearchDividerColor = Color(0xFF18181D)
private val SearchIconBackground = Color(0xFF19191E)

@Composable
fun SearchSettingsScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    onNavigateToWidgetsCenter: () -> Unit,
    onRequestContactsPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
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
                    tint = TextPrimary,
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
                color = TextSecondary,
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
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 8.dp)
            )

            Surface(
                shape = SearchCardShape,
                color = SearchCardBackground,
                border = BorderStroke(1.dp, SearchCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SearchNavRow(
                        icon = Icons.Outlined.GridView,
                        title = "Centro de Widgets",
                        subtitle = "Ative, personalize e reordene os cartões de widgets e as ações rápidas",
                        onClick = onNavigateToWidgetsCenter
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.Keyboard,
                        title = "Abrir Teclado",
                        subtitle = "Abre o teclado assim que você entra.",
                        checked = uiState.isAutoOpenKeyboard,
                        onCheckedChange = { viewModel.setAutoOpenKeyboard(it) }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.RocketLaunch,
                        title = "Auto-Lançar",
                        subtitle = "Abre o app sozinho quando é o único resultado.",
                        checked = uiState.isAutoLaunchEnabled,
                        onCheckedChange = { viewModel.setAutoLaunchEnabled(it) }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.RadioButtonUnchecked,
                        title = "Recolher a barra",
                        subtitle = "Deixa a barra em um círculo pequeno quando parada.",
                        checked = uiState.isCollapseDockEnabled,
                        onCheckedChange = { viewModel.setCollapseDockEnabled(it) }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.VpnKey,
                        title = "Busca exata de apps",
                        subtitle = "Só mostra apps com o nome exatamente igual.",
                        checked = uiState.isExactSearchEnabled,
                        onCheckedChange = { viewModel.setExactSearchEnabled(it) }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.FlashOn,
                        title = "Atalhos de apps",
                        subtitle = "Mostra os atalhos do Android ao segurar um app.",
                        checked = uiState.isAppShortcutsEnabled,
                        onCheckedChange = { viewModel.setAppShortcutsEnabled(it) }
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
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = SearchCardShape,
                color = SearchCardBackground,
                border = BorderStroke(1.dp, SearchCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SearchToggleRow(
                        icon = Icons.Outlined.Language,
                        title = "Busca Web",
                        subtitle = "Busca na web quando nenhum app combina.",
                        checked = uiState.isWebSearchEnabled,
                        onCheckedChange = { viewModel.setWebSearchEnabled(it) }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchNavRow(
                        icon = Icons.Outlined.Tune,
                        title = "Busca em apps",
                        subtitle = "Busque dentro de apps como Spotify e YouTube.",
                        onClick = {
                            Toast.makeText(context, "Busca rápida no YouTube, Play Store e Web ativa na barra!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

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
                        }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.Chat,
                        title = "Busca de mensagens",
                        subtitle = "Search recent messages from your notifications.",
                        checked = uiState.isMessagesSearchEnabled,
                        onCheckedChange = { viewModel.setMessagesSearchEnabled(it) }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchNavRow(
                        icon = Icons.Outlined.Folder,
                        title = "Busca de arquivos",
                        subtitle = "Music • Video",
                        onClick = {
                            Toast.makeText(context, "Atalhos rápidos para arquivos e mídia disponíveis", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = SearchDividerColor, thickness = 1.dp)

                    SearchToggleRow(
                        icon = Icons.Outlined.Calculate,
                        title = "Cartão de calculadora",
                        subtitle = "Faça contas direto na barra de busca",
                        checked = uiState.isCalculatorCardEnabled,
                        onCheckedChange = { viewModel.setCalculatorCardEnabled(it) }
                    )
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
    onCheckedChange: (Boolean) -> Unit
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
                .background(SearchIconBackground),
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.White,
                checkedBorderColor = Color.White,
                uncheckedThumbColor = Color(0xFF8E8E93),
                uncheckedTrackColor = Color(0xFF1E1E24),
                uncheckedBorderColor = Color(0xFF2C2C34)
            )
        )
    }
}

@Composable
private fun SearchNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
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
                .background(SearchIconBackground),
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
