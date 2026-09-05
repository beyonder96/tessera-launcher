package com.tessera.launcher.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.service.TesseraMediaService
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
    onPickPhoto: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectingAppForType by remember { mutableStateOf<String?>(null) }

    if (selectingAppForType != null) {
        AppSelectionDialog(
            apps = uiState.filteredApps,
            filterType = selectingAppForType!!,
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

            // Seção 1: Moldura de Foto Central
            SectionHeader(title = "Moldura de Foto na Tela Inicial", icon = Icons.Outlined.Image)
            Surface(
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Exibir Moldura de Foto", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(
                                text = if (uiState.photoWidgetUri != null) "Foto configurada" else "Nenhuma foto selecionada",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = uiState.isPhotoWidgetEnabled,
                            onCheckedChange = { viewModel.setPhotoWidgetEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = DarkSurfaceVariant,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = DarkSurface
                            )
                        )
                    }

                    if (uiState.isPhotoWidgetEnabled) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = PillShape,
                                color = DarkSurfaceVariant,
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier.clickable { onPickPhoto() }
                            ) {
                                Text(
                                    text = if (uiState.photoWidgetUri == null) "Escolher Foto" else "Trocar Foto",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            if (uiState.photoWidgetUri != null) {
                                Surface(
                                    shape = PillShape,
                                    color = Color.Transparent,
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.clickable { viewModel.setPhotoWidgetUri(null) }
                                ) {
                                    Text(
                                        text = "Remover Foto",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção 2: Widgets da Barra Searcho
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
                                text = uiState.defaultMusicApp ?: "Spotify (Padrão)",
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

            // Seção 4: Permissões e Desbloqueio Android 13+
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
                                text = if (uiState.hasNotificationAccess) "Autorizado" else "Necessário para ler o que está tocando",
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

                    // Atalho para Configurações Restritas (Android 13+)
                    if (!uiState.hasNotificationAccess) {
                        Surface(
                            shape = PillShape,
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.clickable {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Security, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Desbloquear Configurações Restritas (3 Pontinhos)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
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
                                text = if (uiState.hasCalendarPermission) "Autorizado" else "Sincroniza eventos do Google Agenda",
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
                            text = if (uiState.isAmoledMode) "Preto puro #050505" else "Translúcido (exibe wallpaper)",
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
    filterType: String,
    onDismiss: () -> Unit,
    onSelect: (AppInfo) -> Unit
) {
    val sortedApps = remember(apps, filterType) {
        if (filterType == "music") {
            apps.sortedByDescending { TesseraMediaService.KNOWN_MUSIC_PACKAGES.contains(it.packageName) }
        } else {
            apps
        }
    }

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
                    text = if (filterType == "music") "Selecione o App de Música" else "Selecione o App de Calendário",
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
                    sortedApps.forEach { app ->
                        val isPriority = filterType == "music" && TesseraMediaService.KNOWN_MUSIC_PACKAGES.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(app) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isPriority) TextPrimary else TextSecondary
                            )
                            if (isPriority) {
                                Text(
                                    text = "Música",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
