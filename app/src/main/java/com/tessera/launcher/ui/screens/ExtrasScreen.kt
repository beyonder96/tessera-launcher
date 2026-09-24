package com.tessera.launcher.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Visibility
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
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val ExtrasCardShape = RoundedCornerShape(24.dp)
private val ExtrasCardBackground = Color(0xFF0F0F12)
private val ExtrasCardBorder = Color(0xFF1D1D22)
private val ExtrasDividerColor = Color(0xFF18181D)
private val ExtrasIconBackground = Color(0xFF19191E)

@Composable
fun ExtrasScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToCommands: () -> Unit,
    onOpenHiddenApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarPadding = if (uiState.isShowStatusBarEnabled) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val success = viewModel.exportBackup(context, uri)
            Toast.makeText(
                context,
                if (success) "Backup exportado com sucesso!" else "Erro ao exportar backup",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val success = viewModel.importBackup(context, uri)
            Toast.makeText(
                context,
                if (success) "Configurações restauradas com sucesso!" else "Erro ao restaurar backup (arquivo inválido)",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = statusBarPadding)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar Centrada: Botão Voltar + "EXTRAS"
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
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "EXTRAS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Único de Extras (Conforme Imagem 5)
        Surface(
            shape = ExtrasCardShape,
            color = ExtrasCardBackground,
            border = BorderStroke(1.dp, ExtrasCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                // 1. Pastas
                ExtrasRowItem(
                    icon = Icons.Outlined.Folder,
                    title = "Pastas",
                    subtitle = "Agrupe apps sob uma mesma palavra.",
                    onClick = onNavigateToFolders
                )

                HorizontalDivider(color = ExtrasDividerColor, thickness = 1.dp)

                // 2. Apps ocultos
                ExtrasRowItem(
                    icon = Icons.Outlined.Visibility,
                    title = "Apps ocultos",
                    subtitle = "Oculte apps e proteja com um PIN.",
                    onClick = onOpenHiddenApps
                )

                HorizontalDivider(color = ExtrasDividerColor, thickness = 1.dp)

                // 3. Comandos
                ExtrasRowItem(
                    icon = Icons.Outlined.Code,
                    title = "Comandos",
                    subtitle = "Prefixos rápidos de ação — como @con para contatos.",
                    onClick = onNavigateToCommands
                )

                HorizontalDivider(color = ExtrasDividerColor, thickness = 1.dp)

                // 4. Perfis de Foco
                ExtrasRowItem(
                    icon = Icons.Outlined.SelfImprovement,
                    title = "Perfis de foco",
                    subtitle = "Modos Trabalho, Desconexão e agendamento automático.",
                    onClick = { viewModel.openFocusModal() }
                )

                HorizontalDivider(color = ExtrasDividerColor, thickness = 1.dp)

                // 4. Mostrar barra de status (Toggle com Switch)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                viewModel.setShowStatusBarEnabled(!uiState.isShowStatusBarEnabled)
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ExtrasIconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhoneAndroid,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mostrar barra de status",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mostra a barra de status do Android no topo.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Switch(
                        checked = uiState.isShowStatusBarEnabled,
                        onCheckedChange = { viewModel.setShowStatusBarEnabled(it) },
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "BACKUP E RESTAURAÇÃO",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        // Card de Backup & Restauração
        Surface(
            shape = ExtrasCardShape,
            color = ExtrasCardBackground,
            border = BorderStroke(1.dp, ExtrasCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                // 1. Exportar backup
                ExtrasRowItem(
                    icon = Icons.Outlined.FileDownload,
                    title = "Exportar backup",
                    subtitle = "Salva suas preferências, pastas e atalhos em um arquivo JSON.",
                    onClick = {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                        exportLauncher.launch("tessera_backup_$timestamp.json")
                    }
                )

                HorizontalDivider(color = ExtrasDividerColor, thickness = 1.dp)

                // 2. Restaurar backup
                ExtrasRowItem(
                    icon = Icons.Outlined.FileUpload,
                    title = "Restaurar backup",
                    subtitle = "Restaura todas as configurações a partir de um backup JSON salvo.",
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ExtrasRowItem(
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
                .background(ExtrasIconBackground),
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
            tint = Color(0xFF48484A),
            modifier = Modifier.size(20.dp)
        )
    }
}
