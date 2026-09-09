package com.tessera.launcher.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

@Composable
fun PermissionsScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAllPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val contactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val smsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val calendarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val openAppSettings = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateLocationPermission(isGranted)
        viewModel.refreshAllPermissions(context)
    }

    val isLight = uiState.isLightMode
    val bg = when {
        isLight -> Color(0xFFF8FAFC)
        uiState.isAmoledMode -> AmoledBlack
        else -> DarkSurface
    }
    val cardBg = if (isLight) Color(0xFFFFFFFF) else AmoledCardBackground
    val cardBorder = if (isLight) Color(0xFFCBD5E1) else AmoledCardBorder
    val iconBoxBg = if (isLight) Color(0xFFF1F5F9) else Color(0xFF191920)
    val primaryTextColor = if (isLight) Color(0xFF0F172A) else TextPrimary
    val secondaryTextColor = if (isLight) Color(0xFF64748B) else TextSecondary
    val tertiaryTextColor = if (isLight) Color(0xFF94A3B8) else TextTertiary
    val dividerColor = if (isLight) Color(0xFFF1F5F9) else Color(0xFF16161C)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Voltar e Título "PERMISSÕES"
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
                            onClick = onBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = primaryTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "PERMISSÕES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    ),
                    color = secondaryTextColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SEÇÃO 1: PERMISSÕES DO APP (ACESSO DIRETO)
            Text(
                text = "PERMISSÕES DO APP (ACESSO DIRETO)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )
            Text(
                text = "Solicitações acionadas diretamente na tela via diálogo nativo do sistema",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = secondaryTextColor,
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Localização (Clima)
                    PermissionRow(
                        icon = Icons.Outlined.LocationOn,
                        title = "Localização",
                        subtitle = "Previsão do tempo precisa em tempo real no widget",
                        isChecked = uiState.hasLocationPermission,
                        onCheckedChange = {
                            if (uiState.hasLocationPermission) {
                                viewModel.refreshWeather()
                                Toast.makeText(context, "Localização ativa. Atualizando previsão...", Toast.LENGTH_SHORT).show()
                            } else {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        isLightMode = isLight,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    PermissionDivider(dividerColor)

                    // 2. Calendário & Agenda
                    PermissionRow(
                        icon = Icons.Outlined.CalendarToday,
                        title = "Calendário & Agenda",
                        subtitle = "Exibe seus próximos compromissos no widget",
                        isChecked = uiState.hasCalendarPermission,
                        onCheckedChange = {
                            if (uiState.hasCalendarPermission) {
                                openAppSettings()
                            } else {
                                calendarLauncher.launch(Manifest.permission.READ_CALENDAR)
                            }
                        },
                        isLightMode = isLight,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    PermissionDivider(dividerColor)

                    // 3. Busca de Contatos
                    PermissionRow(
                        icon = Icons.Outlined.People,
                        title = "Busca de contatos",
                        subtitle = "Localizar contatos e discar diretamente pela barra",
                        isChecked = uiState.hasContactsPermission,
                        onCheckedChange = {
                            if (uiState.hasContactsPermission) {
                                openAppSettings()
                            } else {
                                contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        },
                        isLightMode = isLight,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    PermissionDivider(dividerColor)

                    // 4. Busca de Mensagens
                    PermissionRow(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "Busca de mensagens",
                        subtitle = "Encontrar mensagens SMS pela pesquisa do launcher",
                        isChecked = uiState.hasSmsPermission,
                        onCheckedChange = {
                            if (uiState.hasSmsPermission) {
                                openAppSettings()
                            } else {
                                smsLauncher.launch(Manifest.permission.READ_SMS)
                            }
                        },
                        isLightMode = isLight,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SEÇÃO 2: ACESSOS DO SISTEMA (CONFIGURAÇÕES DO ANDROID)
            Text(
                text = "ACESSOS DO SISTEMA (CONFIGURAÇÕES DO ANDROID)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )
            Text(
                text = "Exigência de segurança do Android: permissões de sistema requerem autorização nas telas oficiais de configuração.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = secondaryTextColor,
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Lançador Padrão
                    PermissionRow(
                        icon = Icons.Outlined.Home,
                        title = "Lançador padrão",
                        subtitle = "Define o Tessera como sua tela inicial principal",
                        isChecked = uiState.isDefaultLauncher,
                        onCheckedChange = {
                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        },
                        isLightMode = isLight,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    PermissionDivider(dividerColor)

                    // 2. Acesso a Notificações (Música)
                    PermissionRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Acesso a notificações",
                        subtitle = "Permite ao widget de música ler e controlar reprodução",
                        isChecked = uiState.hasNotificationAccess,
                        onCheckedChange = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        },
                        isLightMode = isLight,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    PermissionDivider(dividerColor)

                    // 3. Serviço de Acessibilidade
                    PermissionRow(
                        icon = Icons.Outlined.AccessibilityNew,
                        title = "Serviço de acessibilidade",
                        subtitle = "Gestos de toque duplo para bloquear a tela e ações rápidas",
                        isChecked = uiState.hasAccessibilityService,
                        onCheckedChange = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        },
                        isLightMode = isLight,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLightMode: Boolean = false,
    iconBoxBg: Color = Color(0xFF191920),
    primaryTextColor: Color = TextPrimary,
    secondaryTextColor: Color = TextSecondary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!isChecked) }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBoxBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryTextColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = primaryTextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = secondaryTextColor
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isLightMode) Color.White else Color.Black,
                checkedTrackColor = if (isLightMode) Color(0xFF0F172A) else Color.White,
                checkedBorderColor = if (isLightMode) Color(0xFF0F172A) else Color.White,
                uncheckedThumbColor = if (isLightMode) Color(0xFF94A3B8) else TextSecondary,
                uncheckedTrackColor = if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF222228),
                uncheckedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Transparent
            )
        )
    }
}

@Composable
private fun PermissionDivider(color: Color = Color(0xFF16161C)) {
    HorizontalDivider(
        color = color,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 68.dp, end = 16.dp)
    )
}
