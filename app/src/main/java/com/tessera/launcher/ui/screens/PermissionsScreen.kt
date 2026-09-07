package com.tessera.launcher.ui.screens

import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MusicNote
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

    val musicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val photosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateLocationPermission(isGranted)
        viewModel.refreshAllPermissions(context)
    }

    val bg = if (uiState.isAmoledMode) AmoledBlack else DarkSurface

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
                        tint = TextPrimary,
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
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Unificado com as 6 Permissões
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AmoledCardBackground,
                border = BorderStroke(1.dp, AmoledCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Lançador padrão
                    PermissionRow(
                        icon = Icons.Outlined.Home,
                        title = "Lançador padrão",
                        subtitle = "O Searcho é o seu launcher.",
                        isChecked = uiState.isDefaultLauncher,
                        onCheckedChange = {
                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }
                    )

                    PermissionDivider()

                    // 2. Busca de Contatos
                    PermissionRow(
                        icon = Icons.Outlined.People,
                        title = "Busca de Contatos",
                        subtitle = "Find and call contacts from search",
                        isChecked = uiState.hasContactsPermission,
                        onCheckedChange = {
                            if (!uiState.hasContactsPermission) {
                                contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    )

                    PermissionDivider()

                    // 3. Busca de mensagens
                    PermissionRow(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "Busca de mensagens",
                        subtitle = "Search messages and notifications",
                        isChecked = uiState.hasSmsPermission,
                        onCheckedChange = {
                            if (!uiState.hasSmsPermission) {
                                smsLauncher.launch(Manifest.permission.READ_SMS)
                            }
                        }
                    )

                    PermissionDivider()

                    // 4. Music
                    PermissionRow(
                        icon = Icons.Outlined.MusicNote,
                        title = "Music",
                        subtitle = "Search local music tracks",
                        isChecked = uiState.hasMusicPermission,
                        onCheckedChange = {
                            if (!uiState.hasMusicPermission) {
                                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                musicLauncher.launch(perm)
                            }
                        }
                    )

                    PermissionDivider()

                    // 5. Photos & Videos
                    PermissionRow(
                        icon = Icons.Outlined.Image,
                        title = "Photos & Videos",
                        subtitle = "Search local photos and video files",
                        isChecked = uiState.hasMediaImagesPermission,
                        onCheckedChange = {
                            if (!uiState.hasMediaImagesPermission) {
                                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_IMAGES
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                photosLauncher.launch(perm)
                            }
                        }
                    )

                    PermissionDivider()

                    // 6. Serviço de acessibilidade
                    PermissionRow(
                        icon = Icons.Outlined.AccessibilityNew,
                        title = "Serviço de acessibilidade",
                        subtitle = "Enable quick gestures and actions",
                        isChecked = uiState.hasAccessibilityService,
                        onCheckedChange = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }
                    )

                    PermissionDivider()

                    // 7. Localização e Clima
                    PermissionRow(
                        icon = Icons.Outlined.WbSunny,
                        title = "Localização e Clima",
                        subtitle = "Previsão do tempo em tempo real no widget de clima",
                        isChecked = uiState.hasLocationPermission,
                        onCheckedChange = {
                            if (!uiState.hasLocationPermission) {
                                locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            }
                        }
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
    onCheckedChange: (Boolean) -> Unit
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
                .background(Color(0xFF191920)),
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
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.White,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Color(0xFF222228),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun PermissionDivider() {
    HorizontalDivider(
        color = Color(0xFF16161C),
        thickness = 1.dp,
        modifier = Modifier.padding(start = 68.dp, end = 16.dp)
    )
}
