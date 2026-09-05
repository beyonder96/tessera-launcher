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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onPickNativeWidget: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectingAppForType by remember { mutableStateOf<String?>(null) } // "music" or "calendar"

    if (selectingAppForType != null) {
        AppSelectionDialog(
            apps = uiState.filteredApps,
            onDismiss = { selectingAppForType = null },
            onSelect = { app ->
                if (selectingAppForType == "music") {
                    viewModel.setDefaultMusicApp(app.packageName)
                } else if (selectingAppForType == "calendar") {
                    viewModel.setDefaultCalendarApp(app.packageName)
                }
                selectingAppForType = null
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configurações",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )

                Surface(
                    shape = CircleShape,
                    color = DarkSurface,
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.clickable { viewModel.closeSettings() }
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Fechar configurações",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seção 1: Widgets da Barra Searcho
            SectionHeader(title = "Widgets da Barra de Pesquisa", icon = Icons.Outlined.Widgets)
            Surface(
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val allTypes = listOf(WidgetType.CALENDAR, WidgetType.BATTERY, WidgetType.MEDIA)

                    allTypes.forEach { type ->
                        val isEnabled = uiState.enabledWidgets.contains(type)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isEnabled) TextPrimary else TextSecondary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isEnabled) {
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowUp,
                                        contentDescription = "Mover para cima",
                                        tint = TextSecondary,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clickable { viewModel.moveWidgetUp(type) }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = "Mover para baixo",
                                        tint = TextSecondary,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clickable { viewModel.moveWidgetDown(type) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { viewModel.toggleWidget(type) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TextPrimary,
                                        checkedTrackColor = DarkSurfaceVariant,
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = DarkSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção 2: Widgets Nativos da Tela Inicial
            SectionHeader(title = "Widgets Nativos do Android", icon = Icons.Outlined.Add)
            Surface(
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Adicione widgets de outros aplicativos (relógios, notas, previsão do tempo) à sua tela inicial.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = PillShape,
                        color = DarkSurfaceVariant,
                        border = BorderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier.clickable {
                            viewModel.closeSettings()
                            onPickNativeWidget()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Adicionar Widget do Sistema",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção 3: Aplicativos Padrão
            SectionHeader(title = "Serviços & Aplicativos Padrão", icon = Icons.Outlined.Today)
            Surface(
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // App de Música
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectingAppForType = "music" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "App de Música Padrão", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(
                                text = uiState.defaultMusicApp ?: "Automático (último em reprodução)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Text(text = "Alterar", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }

                    // App de Calendário
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectingAppForType = "calendar" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "App de Calendário Padrão", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(
                                text = uiState.defaultCalendarApp ?: "Google Agenda / Padrão",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Text(text = "Alterar", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção 4: Permissões
            SectionHeader(title = "Permissões do Sistema", icon = Icons.Outlined.Notifications)
            Surface(
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Notificações / Mídia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Acesso a Notificações (Mídia)", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(
                                text = if (uiState.hasNotificationAccess) "Autorizado" else "Necessário para ler faixa e pausar áudio",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.hasNotificationAccess) TextPrimary else TextTertiary
                            )
                        }
                        if (!uiState.hasNotificationAccess) {
                            Surface(
                                shape = PillShape,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                }
                            ) {
                                Text(
                                    text = "Ativar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Calendário
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Leitura do Calendário", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(
                                text = if (uiState.hasCalendarPermission) "Autorizado" else "Necessário para sincronizar compromissos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.hasCalendarPermission) TextPrimary else TextTertiary
                            )
                        }
                        if (!uiState.hasCalendarPermission) {
                            Surface(
                                shape = PillShape,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier.clickable { onRequestCalendarPermission() }
                            ) {
                                Text(
                                    text = "Permitir",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção 5: Papel de Parede
            SectionHeader(title = "Aparência e Fundo", icon = Icons.Outlined.Palette)
            Surface(
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Fundo Preto Puro AMOLED", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Text(
                            text = if (uiState.isAmoledMode) "Preto puro #000000" else "Translúcido (exibe wallpaper)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.isAmoledMode,
                        onCheckedChange = { viewModel.setAmoledMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = DarkSurfaceVariant,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = TextSecondary)
    }
}

@Composable
private fun AppSelectionDialog(
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onSelect: (AppInfo) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CardShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Selecione o Aplicativo",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    apps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(app) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
