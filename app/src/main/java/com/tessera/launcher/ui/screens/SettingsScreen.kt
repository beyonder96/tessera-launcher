package com.tessera.launcher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.tessera.launcher.data.helper.IconPackInfo
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.service.TesseraMediaService
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val SettingsCardShape = RoundedCornerShape(26.dp)
private val SettingsCardBackground = Color(0xFF0F0F12)
private val SettingsCardBorder = Color(0xFF1D1D22)
private val ItemDividerColor = Color(0xFF18181D)
private val IconCircleBackground = Color(0xFF19191E)

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onPickPhoto: () -> Unit,
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
            WidgetsCenterScreen(
                viewModel = viewModel,
                uiState = uiState,
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
                onNavigateToSearchos = {
                    viewModel.navigateToSettingsSubScreen(SettingsSubScreen.SEARCHOS)
                },
                onOpenHiddenApps = {
                    Toast.makeText(context, "Apps ocultos: recurso de proteção de aplicativos.", Toast.LENGTH_SHORT).show()
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
        SettingsSubScreen.SEARCHOS -> {
            SearchosScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateBackSettings() },
                modifier = modifier
            )
            return
        }
        SettingsSubScreen.MAIN -> {
            // Continua exibição principal
        }
    }

    var activeDialog by remember { mutableStateOf<String?>(null) }
    var selectingAppForType by remember { mutableStateOf<String?>(null) }

    when (activeDialog) {
        "extras" -> ExtrasDialog(
            viewModel = viewModel,
            enabledWidgets = uiState.enabledWidgets,
            isPhotoEnabled = uiState.isPhotoWidgetEnabled,
            onTogglePhoto = { viewModel.setPhotoWidgetEnabled(it) },
            onPickPhoto = onPickPhoto,
            hasPhoto = uiState.photoWidgetUri != null,
            onClearPhoto = { viewModel.setPhotoWidgetUri(null) },
            onDismiss = { activeDialog = null }
        )
        "gestures" -> GesturesDialog(onDismiss = { activeDialog = null })
        "customization" -> CustomizationDialog(
            viewModel = viewModel,
            uiState = uiState,
            onPickPhoto = onPickPhoto,
            onSelectMusicApp = { selectingAppForType = "music" },
            onSelectCalendarApp = { selectingAppForType = "calendar" },
            onDismiss = { activeDialog = null }
        )
        "language" -> LanguageDialog(onDismiss = { activeDialog = null })
        "permissions" -> PermissionsDialog(
            hasNotificationAccess = uiState.hasNotificationAccess,
            hasCalendarPermission = uiState.hasCalendarPermission,
            onRequestCalendarPermission = onRequestCalendarPermission,
            onOpenNotificationSettings = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            },
            onOpenAppSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            onDismiss = { activeDialog = null }
        )
        "transparency" -> TransparencyDialog(onDismiss = { activeDialog = null })
        "developer" -> DeveloperDialog(
            appsCount = uiState.filteredApps.size,
            hasNotificationAccess = uiState.hasNotificationAccess,
            isLiquidGlass = uiState.isLiquidGlassEnabled,
            onDismiss = { activeDialog = null }
        )
    }

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
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Barra superior com Voltar e Título "Tessera"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.closeSettings() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Tessera",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CARD 1: Configurações Principais (7 itens)
            Surface(
                shape = SettingsCardShape,
                color = SettingsCardBackground,
                border = BorderStroke(1.dp, SettingsCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Busca
                    SettingsItemRow(
                        icon = Icons.Outlined.Search,
                        title = "Busca",
                        subtitle = "Escolha o que aparece quando você busca.",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.SEARCH) }
                    )

                    ItemDivider()

                    // 2. Extras
                    SettingsItemRow(
                        icon = Icons.Outlined.Extension,
                        title = "Extras",
                        subtitle = "Pastas, apps ocultos e Tesseras",
                        onClick = { viewModel.navigateToSettingsSubScreen(SettingsSubScreen.EXTRAS) }
                    )

                    ItemDivider()

                    // 3. Gestos
                    SettingsItemRow(
                        icon = Icons.Outlined.TouchApp,
                        title = "Gestos",
                        subtitle = "Ações de deslizar e toque duplo.",
                        onClick = { activeDialog = "gestures" }
                    )

                    ItemDivider()

                    // 4. Customization
                    SettingsItemRow(
                        icon = Icons.Outlined.Palette,
                        title = "Customization",
                        subtitle = "Ícones, papel de parede e estilo",
                        onClick = { activeDialog = "customization" }
                    )

                    ItemDivider()

                    // 5. Idioma
                    SettingsItemRow(
                        icon = Icons.Outlined.Language,
                        title = "Idioma",
                        subtitle = "Alterar o idioma do aplicativo.",
                        onClick = { activeDialog = "language" }
                    )

                    ItemDivider()

                    // 6. Tema com 4 botões circulares inline
                    ThemeSettingsRow(
                        isAmoled = uiState.isAmoledMode,
                        onSelectAuto = { viewModel.setAmoledMode(false) },
                        onSelectAmoled = { viewModel.setAmoledMode(true) },
                        onSelectDarkGray = { viewModel.setAmoledMode(false) },
                        onSelectLight = { viewModel.setAmoledMode(false) }
                    )

                    ItemDivider()

                    // 7. Lançador padrão
                    SettingsItemRow(
                        icon = Icons.Outlined.Home,
                        title = "Lançador padrão",
                        subtitle = "O Tessera é o seu launcher.",
                        onClick = { openDefaultLauncherSettings(context) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CARD 2: Sistema e Privacidade (3 itens)
            Surface(
                shape = SettingsCardShape,
                color = SettingsCardBackground,
                border = BorderStroke(1.dp, SettingsCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 8. Permissões
                    SettingsItemRow(
                        icon = Icons.Outlined.Security,
                        title = "Permissões",
                        subtitle = "Escolha o que o Tessera pode acessar",
                        onClick = { activeDialog = "permissions" }
                    )

                    ItemDivider()

                    // 9. Transparência
                    SettingsItemRow(
                        icon = Icons.Outlined.Visibility,
                        title = "Transparência",
                        subtitle = "Sem rastreio. Sem anúncios. Sem coleta de dados.",
                        onClick = { activeDialog = "transparency" }
                    )

                    ItemDivider()

                    // 10. Desenvolvedor
                    SettingsItemRow(
                        icon = Icons.Outlined.Code,
                        title = "Desenvolvedor",
                        subtitle = "Opções avançadas e depuração.",
                        onClick = { activeDialog = "developer" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsItemRow(
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
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(IconCircleBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ThemeSettingsRow(
    isAmoled: Boolean,
    onSelectAuto: () -> Unit,
    onSelectAmoled: () -> Unit,
    onSelectDarkGray: () -> Unit,
    onSelectLight: () -> Unit
) {
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
                .background(IconCircleBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = "Tema",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tema",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Trocar de estilo",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSelectAuto
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.BrightnessAuto,
                    contentDescription = "Automático",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF000000))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSelectAmoled
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isAmoled) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "AMOLED Selecionado",
                        tint = TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF26262B))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSelectDarkGray
                    )
            )

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E5EA))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSelectLight
                    )
            )
        }
    }
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 74.dp, end = 16.dp),
        color = ItemDividerColor,
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

private fun getInstalledIconPacks(context: Context): List<Pair<String, String>> {
    val pm = context.packageManager
    val iconPacks = mutableListOf<Pair<String, String>>()
    val intentActions = listOf(
        "com.novalauncher.THEME",
        "org.adw.launcher.THEMES",
        "com.gau.go.launcherex.theme"
    )
    for (action in intentActions) {
        val intent = Intent(action)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            val label = info.loadLabel(pm).toString()
            if (!iconPacks.any { it.first == pkg }) {
                iconPacks.add(pkg to label)
            }
        }
    }
    return iconPacks
}

@Composable
private fun PermissionsDialog(
    hasNotificationAccess: Boolean,
    hasCalendarPermission: Boolean,
    onRequestCalendarPermission: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = SettingsCardShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Permissões do Sistema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Acesso a Notificações (Mídia)", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Text(
                            text = if (hasNotificationAccess) "Autorizado" else "Detecta o player e a capa da música",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasNotificationAccess) TextPrimary else TextSecondary
                        )
                    }
                    if (!hasNotificationAccess) {
                        Surface(
                            shape = PillShape,
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.clickable { onOpenNotificationSettings() }
                        ) {
                            Text(
                                text = "Ativar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (!hasNotificationAccess) {
                    Surface(
                        shape = PillShape,
                        color = Color(0xFF18181D),
                        border = BorderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier.clickable { onOpenAppSettings() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Security, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Desbloquear Restritas (3 Pontinhos)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(color = ItemDividerColor, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Acesso ao Calendário", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Text(
                            text = if (hasCalendarPermission) "Autorizado" else "Sincroniza eventos da sua agenda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasCalendarPermission) TextPrimary else TextSecondary
                        )
                    }
                    if (!hasCalendarPermission) {
                        Surface(
                            shape = PillShape,
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.clickable { onRequestCalendarPermission() }
                        ) {
                            Text(
                                text = "Permitir",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = PillShape,
                    color = DarkSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                ) {
                    Text(
                        text = "Concluído",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtrasDialog(
    viewModel: MainViewModel,
    enabledWidgets: List<WidgetType>,
    isPhotoEnabled: Boolean,
    onTogglePhoto: (Boolean) -> Unit,
    onPickPhoto: () -> Unit,
    hasPhoto: Boolean,
    onClearPhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = SettingsCardShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Extras & Widgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Moldura de Foto na Home", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Text(text = if (hasPhoto) "Foto ativa" else "Sem foto", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Switch(
                        checked = isPhotoEnabled,
                        onCheckedChange = onTogglePhoto,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = DarkSurfaceVariant,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkSurface
                        )
                    )
                }

                if (isPhotoEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = PillShape,
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.clickable { onPickPhoto() }
                        ) {
                            Text(
                                text = if (!hasPhoto) "Escolher Foto" else "Trocar Foto",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                        if (hasPhoto) {
                            Surface(
                                shape = PillShape,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier.clickable { onClearPhoto() }
                            ) {
                                Text(
                                    text = "Remover",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = ItemDividerColor, thickness = 1.dp)

                Text(
                    text = "Widgets da Barra de Pesquisa",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                val allTypes = listOf(WidgetType.CALENDAR, WidgetType.BATTERY, WidgetType.MEDIA)
                allTypes.forEach { type ->
                    val isEnabled = enabledWidgets.contains(type)
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
                                    contentDescription = "Subir",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable { viewModel.moveWidgetUp(type) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Descer",
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

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = PillShape,
                    color = DarkSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                ) {
                    Text(
                        text = "Fechar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomizationDialog(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onPickPhoto: () -> Unit,
    onSelectMusicApp: () -> Unit,
    onSelectCalendarApp: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = SettingsCardShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Customization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectMusicApp() },
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCalendarApp() },
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

                HorizontalDivider(color = ItemDividerColor, thickness = 1.dp)

                // Modo Liquid Glass (iOS 27 Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo Liquid Glass (iOS 27)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = if (uiState.isLiquidGlassEnabled) "Ativo (vidro translúcido & reflexo de luz)" else "Desativado (estilo opaco sólido)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.isLiquidGlassEnabled,
                        onCheckedChange = { viewModel.setLiquidGlassEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = DarkSurfaceVariant,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkSurface
                        )
                    )
                }

                HorizontalDivider(color = ItemDividerColor, thickness = 1.dp)

                // Estilo dos Ícones
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Estilo dos Ícones",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Formato e acabamento dos ícones da lista de apps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val shapes = listOf(
                            "DEFAULT" to "Padrão",
                            "MONOCHROME" to "Mono",
                            "CIRCLE" to "Círculo",
                            "SQUIRCLE" to "Squircle"
                        )
                        shapes.forEach { (shapeKey, label) ->
                            val isSelected = uiState.iconShape == shapeKey
                            Surface(
                                shape = PillShape,
                                color = if (isSelected) Color.White else DarkSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setIconShape(shapeKey) }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = ItemDividerColor, thickness = 1.dp)

                // Pacote de Ícones
                var showIconPackSelector by remember { mutableStateOf(false) }
                val installedPacks = remember { viewModel.getInstalledIconPacks() }
                val currentPackName = remember(uiState.selectedIconPack, installedPacks) {
                    installedPacks.find { it.packageName == uiState.selectedIconPack }?.label
                        ?: uiState.selectedIconPack
                        ?: "Padrão do Sistema"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIconPackSelector = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Pacote de Ícones", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Text(
                            text = currentPackName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Text(text = "Alterar", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }

                if (showIconPackSelector) {
                    IconPackSelectionDialog(
                        installedPacks = installedPacks,
                        selectedPackage = uiState.selectedIconPack,
                        onSelectPack = { viewModel.setSelectedIconPack(it) },
                        onDismiss = { showIconPackSelector = false }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = PillShape,
                    color = DarkSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                ) {
                    Text(
                        text = "Concluído",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GesturesDialog(onDismiss: () -> Unit) {
    SimpleInfoDialog(
        title = "Gestos",
        message = "• Toque no meio da tela: recolhe a barra de pesquisa.\n• Toque longo no app: menu rápido com atalho para desinstalar.\n• Deslize lateral: barra alfabética para navegação rápida.",
        onDismiss = onDismiss
    )
}

@Composable
private fun LanguageDialog(onDismiss: () -> Unit) {
    SimpleInfoDialog(
        title = "Idioma",
        message = "Idioma ativo: Português (Brasil).\nO Tessera Launcher segue a preferência regional definida nas configurações do seu sistema.",
        onDismiss = onDismiss
    )
}

@Composable
private fun TransparencyDialog(onDismiss: () -> Unit) {
    SimpleInfoDialog(
        title = "Transparência & Privacidade",
        message = "Sem rastreio. Sem anúncios. Sem telemetria.\n\nO Tessera Launcher funciona 100% offline no seu dispositivo. Todas as suas permissões são utilizadas única e exclusivamente para as funções locais de widgets e navegação.",
        onDismiss = onDismiss
    )
}

@Composable
private fun DeveloperDialog(
    appsCount: Int,
    hasNotificationAccess: Boolean,
    isLiquidGlass: Boolean,
    onDismiss: () -> Unit
) {
    SimpleInfoDialog(
        title = "Desenvolvedor",
        message = "Tessera Launcher v1.5.0\nCompilação: Release/Debug Estável\nApps indexados: $appsCount\nServiço de Mídia: ${if (hasNotificationAccess) "Ativo" else "Inativo"}\nModo Liquid Glass: ${if (isLiquidGlass) "Ativo" else "Inativo"}\nArquitetura: Jetpack Compose + Kotlin Coroutines",
        onDismiss = onDismiss
    )
}

@Composable
private fun SimpleInfoDialog(title: String, message: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = SettingsCardShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = PillShape,
                    color = DarkSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                ) {
                    Text(
                        text = "Entendido",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
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
            shape = SettingsCardShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (filterType == "music") "Selecione o App de Música" else "Selecione o App de Calendário",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sortedApps.forEach { app ->
                        val isPriority = filterType == "music" && TesseraMediaService.KNOWN_MUSIC_PACKAGES.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(app) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
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

@Composable
private fun IconPackSelectionDialog(
    installedPacks: List<IconPackInfo>,
    selectedPackage: String?,
    onSelectPack: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = SettingsCardShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Pacote de Ícones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Escolha o pacote de ícones a ser aplicado a todos os aplicativos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Opção 1: Padrão do Sistema
                val isDefaultSelected = selectedPackage.isNullOrBlank()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDefaultSelected) Color(0xFF1E202A) else Color.Transparent)
                        .clickable {
                            onSelectPack(null)
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF282B36)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Padrão do Sistema",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isDefaultSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    if (isDefaultSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Selecionado",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = ItemDividerColor, thickness = 1.dp)

                if (installedPacks.isEmpty()) {
                    Text(
                        text = "Nenhum pacote de ícones adicional instalado no dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    installedPacks.forEach { pack ->
                        val isPackSelected = selectedPackage == pack.packageName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPackSelected) Color(0xFF1E202A) else Color.Transparent)
                                .clickable {
                                    onSelectPack(pack.packageName)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (pack.icon != null) {
                                Image(
                                    bitmap = pack.icon.toBitmap(64, 64).asImageBitmap(),
                                    contentDescription = pack.label,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF282B36)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Palette,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = pack.label,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isPackSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            if (isPackSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selecionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = PillShape,
                    color = DarkSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                ) {
                    Text(
                        text = "Fechar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}
