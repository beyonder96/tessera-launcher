package com.tessera.launcher.ui.screens

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tessera.launcher.ui.components.AnimatedPermissionsProgressCard
import com.tessera.launcher.ui.components.DefaultLauncherAnimatedCard
import com.tessera.launcher.ui.components.RestrictedSettingsAnimatedCard
import com.tessera.launcher.ui.components.RestrictedSettingsGuideDialog
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

private data class RestrictedPermissionPrompt(
    val title: String,
    val openSettingsAction: () -> Unit
)

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

    val calendarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val openAppSettings = {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    val requestDefaultLauncher = {
        val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
        } else {
            null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            roleManager != null &&
            roleManager.isRoleAvailable(RoleManager.ROLE_HOME)
        ) {
            roleRequestLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
        } else {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }.onFailure {
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateLocationPermission(isGranted)
        viewModel.refreshAllPermissions(context)
    }

    var restrictedPrompt by remember { mutableStateOf<RestrictedPermissionPrompt?>(null) }

    val isLight = uiState.isLightMode
    val accent = uiState.activeAccentColor
    val bg = when {
        isLight -> Color(0xFFF7F4EB)
        uiState.isAmoledMode -> AmoledBlack
        else -> DarkSurface
    }
    val cardBg = if (isLight) Color(0xFFEFECE2) else AmoledCardBackground
    val cardBorder = if (isLight) Color(0xFFDDD8CB) else AmoledCardBorder
    val iconBoxBg = if (isLight) Color(0xFFE8E4D8) else Color(0xFF191920)
    val primaryTextColor = if (isLight) Color(0xFF1C1917) else TextPrimary
    val secondaryTextColor = if (isLight) Color(0xFF57534E) else TextSecondary
    val tertiaryTextColor = if (isLight) Color(0xFF625B55) else TextTertiary
    val dividerColor = if (isLight) Color(0x181C1917) else Color(0xFF16161C)

    val grantedCount = remember(
        uiState.hasLocationPermission,
        uiState.hasCalendarPermission,
        uiState.hasContactsPermission,
        uiState.isDefaultLauncher,
        uiState.hasNotificationAccess,
        uiState.hasAccessibilityService,
        uiState.hasUsageAccessPermission
    ) {
        listOf(
            uiState.hasLocationPermission,
            uiState.hasCalendarPermission,
            uiState.hasContactsPermission,
            uiState.isDefaultLauncher,
            uiState.hasNotificationAccess,
            uiState.hasAccessibilityService,
            uiState.hasUsageAccessPermission
        ).count { it }
    }

    restrictedPrompt?.let { prompt ->
        RestrictedSettingsGuideDialog(
            permissionName = prompt.title,
            accent = accent,
            isLight = isLight,
            onOpenAppInfo = { openAppSettings() },
            onOpenPermissionSettings = prompt.openSettingsAction,
            onDismiss = { restrictedPrompt = null }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
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

            // Resumo Animado de Progresso
            AnimatedPermissionsProgressCard(
                grantedCount = grantedCount,
                totalCount = 7,
                accent = accent,
                isLight = isLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cartão Animado de Lançador Padrão
            DefaultLauncherAnimatedCard(
                isDefaultLauncher = uiState.isDefaultLauncher,
                accent = accent,
                isLight = isLight,
                onRequestDefault = { requestDefaultLauncher() }
            )

            Spacer(modifier = Modifier.height(22.dp))

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
                        accentColor = accent,
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
                        accentColor = accent,
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
                        accentColor = accent,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RestrictedSettingsAnimatedCard(
                    accent = accent,
                    isLight = isLight,
                    onOpenAppInfo = { openAppSettings() },
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

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
                        onCheckedChange = { requestDefaultLauncher() },
                        isLightMode = isLight,
                        accentColor = accent,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    PermissionDivider(dividerColor)

                    // 2. Acesso a Notificações (Música & Hub)
                    val openNotificationListener = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }
                    }
                    PermissionRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Acesso a notificações",
                        subtitle = "Permite ao widget de música controlar reprodução e exibir badges",
                        isChecked = uiState.hasNotificationAccess,
                        onCheckedChange = {
                            if (!uiState.hasNotificationAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                restrictedPrompt = RestrictedPermissionPrompt(
                                    title = "Acesso a Notificações",
                                    openSettingsAction = { openNotificationListener() }
                                )
                            } else {
                                openNotificationListener()
                            }
                        },
                        isLightMode = isLight,
                        accentColor = accent,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    PermissionDivider(dividerColor)

                    // 3. Serviço de Acessibilidade
                    val openAccessibilitySettings = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }
                    }
                    PermissionRow(
                        icon = Icons.Outlined.AccessibilityNew,
                        title = "Serviço de acessibilidade",
                        subtitle = "Gestos de toque duplo para bloquear a tela e ações rápidas",
                        isChecked = uiState.hasAccessibilityService,
                        onCheckedChange = {
                            if (!uiState.hasAccessibilityService && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                restrictedPrompt = RestrictedPermissionPrompt(
                                    title = "Serviço de Acessibilidade",
                                    openSettingsAction = { openAccessibilitySettings() }
                                )
                            } else {
                                openAccessibilitySettings()
                            }
                        },
                        isLightMode = isLight,
                        accentColor = accent,
                        iconBoxBg = iconBoxBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    PermissionDivider(dividerColor)

                    // 4. Acesso a Dados de Uso (Tempo de tela / Bem-Estar)
                    PermissionRow(
                        icon = Icons.Outlined.HourglassEmpty,
                        title = "Acesso a dados de uso",
                        subtitle = "Permite calcular o tempo de uso de tela e apps do Bem-Estar Digital",
                        isChecked = uiState.hasUsageAccessPermission,
                        onCheckedChange = {
                            val packageUriIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(packageUriIntent)
                            } catch (_: Exception) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                } catch (_: Exception) {}
                            }
                        },
                        isLightMode = isLight,
                        accentColor = accent,
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
    accentColor: Color = Color.White,
    iconBoxBg: Color = Color(0xFF191920),
    primaryTextColor: Color = TextPrimary,
    secondaryTextColor: Color = TextSecondary
) {
    val successGreen = Color(0xFF10B981)
    val pulseScale = remember { Animatable(1f) }
    var previousChecked by remember { mutableStateOf(isChecked) }

    LaunchedEffect(isChecked) {
        if (isChecked && !previousChecked) {
            pulseScale.snapTo(0.84f)
            pulseScale.animateTo(
                targetValue = 1.18f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            pulseScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        previousChecked = isChecked
    }

    val animatedIconBg by animateColorAsState(
        targetValue = if (isChecked) successGreen.copy(alpha = if (isLightMode) 0.16f else 0.18f) else iconBoxBg,
        animationSpec = tween(320),
        label = "perm_icon_bg"
    )
    val animatedIconTint by animateColorAsState(
        targetValue = if (isChecked) successGreen else primaryTextColor,
        animationSpec = tween(320),
        label = "perm_icon_tint"
    )
    val animatedIconBorder by animateColorAsState(
        targetValue = if (isChecked) successGreen.copy(alpha = 0.45f) else Color.Transparent,
        animationSpec = tween(320),
        label = "perm_icon_border"
    )
    val animatedRowTint by animateColorAsState(
        targetValue = if (isChecked) successGreen.copy(alpha = if (isLightMode) 0.04f else 0.05f) else Color.Transparent,
        animationSpec = tween(320),
        label = "perm_row_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(animatedRowTint)
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
                .size(40.dp)
                .scale(pulseScale.value),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(animatedIconBg)
                    .border(BorderStroke(1.dp, animatedIconBorder), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedIconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Mini selo de check animado no canto inferior direito do ícone ao ativar
            androidx.compose.animation.AnimatedVisibility(
                visible = isChecked,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(tween(180)),
                exit = scaleOut(tween(150)) + fadeOut(tween(150)),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(successGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Ativa",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = primaryTextColor
                )
                AnimatedVisibility(
                    visible = isChecked,
                    enter = fadeIn(tween(240)) + scaleIn(initialScale = 0.85f),
                    exit = fadeOut(tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(successGreen.copy(alpha = 0.14f))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = "ATIVA",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            color = successGreen
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = successGreen,
                checkedBorderColor = successGreen,
                uncheckedThumbColor = if (isLightMode) Color(0xFF625B55) else TextSecondary,
                uncheckedTrackColor = if (isLightMode) Color(0xFFE8E4D8) else Color(0xFF222228),
                uncheckedBorderColor = if (isLightMode) Color(0xFFDDD8CB) else Color.Transparent
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
