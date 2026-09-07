package com.tessera.launcher.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.WebAsset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.components.IconPackBottomSheet
import com.tessera.launcher.ui.components.IconShapeBottomSheet
import com.tessera.launcher.ui.components.SearchBarStyleBottomSheet
import com.tessera.launcher.ui.components.SearchBarTextBottomSheet
import com.tessera.launcher.ui.components.TypographyBottomSheet
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
fun CustomizationScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bg = if (uiState.isAmoledMode) AmoledBlack else DarkSurface

    var openModal by remember { mutableStateOf<String?>(null) }

    when (openModal) {
        "style" -> {
            SearchBarStyleBottomSheet(
                currentStyle = uiState.searchBarStyle,
                onSelectStyle = { viewModel.setSearchBarStyle(it) },
                onDismiss = { openModal = null },
                isAmoled = uiState.isAmoledMode
            )
        }
        "text" -> {
            SearchBarTextBottomSheet(
                currentTextType = uiState.searchBarTextType,
                customText = uiState.searchBarCustomText,
                onSelectTextType = { viewModel.setSearchBarTextType(it) },
                onSaveCustomText = { viewModel.setSearchBarCustomText(it) },
                onDismiss = { openModal = null },
                isAmoled = uiState.isAmoledMode
            )
        }
        "typography" -> {
            TypographyBottomSheet(
                currentFontType = uiState.fontFamilyType,
                customFontPath = uiState.customFontPath,
                onSelectFontType = { viewModel.setFontFamilyType(it) },
                onImportFont = { uri -> viewModel.importCustomFont(uri, context) },
                onDismiss = { openModal = null },
                isAmoled = uiState.isAmoledMode
            )
        }
        "shape" -> {
            IconShapeBottomSheet(
                currentShape = uiState.iconShape,
                onSelectShape = { viewModel.setIconShape(it) },
                onDismiss = { openModal = null },
                isAmoled = uiState.isAmoledMode
            )
        }
        "pack" -> {
            IconPackBottomSheet(
                selectedPack = uiState.selectedIconPack,
                installedPacks = viewModel.getInstalledIconPacks(),
                onSelectPack = { pack ->
                    viewModel.reloadAppsWithIconPack(pack)
                },
                onDismiss = { openModal = null },
                isAmoled = uiState.isAmoledMode
            )
        }
    }

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
            // Header: Voltar e Título "CUSTOMIZATION"
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
                    text = "CUSTOMIZATION",
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

            // CARD 1: Papel de Parede do Sistema & Cor sólida
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AmoledCardBackground,
                border = BorderStroke(1.dp, AmoledCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Papel de Parede do Sistema Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Papel de Parede do Sistema",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Usa cores sólidas do tema quando está desligado",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isSystemWallpaperEnabled,
                            onCheckedChange = { viewModel.setSystemWallpaperEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0xFF222228),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    CustomDivider()

                    // Alterar Papel de Parede
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_SET_WALLPAPER).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                }
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
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alterar Papel de Parede",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Abre o seletor do sistema para escolher a imagem",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
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

                    CustomDivider()

                    // Cor sólida
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cor sólida",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Usa uma cor lisa em vez de papel de parede.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = TextSecondary
                                )
                            }

                            // 3 Amostras de cores
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val colors = listOf("#000000", "#1C1C24", "#E5E5EA")
                                colors.forEach { hex ->
                                    val isSelected = uiState.solidWallpaperColor.equals(hex, ignoreCase = true)
                                    val c = Color(android.graphics.Color.parseColor(hex))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color(0xFF444450),
                                                shape = CircleShape
                                            )
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    viewModel.applySolidWallpaper(hex, uiState.solidWallpaperTarget, context)
                                                }
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Seletor Segmentado: [ INICIAL | BLOQUEIO | AMBAS ]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131318))
                                .border(1.dp, Color(0xFF22222A), RoundedCornerShape(12.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val targets = listOf(
                                "home" to "INICIAL",
                                "lock" to "BLOQUEIO",
                                "both" to "AMBAS"
                            )
                            targets.forEach { (targetKey, targetLabel) ->
                                val isSelected = uiState.solidWallpaperTarget == targetKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (isSelected) Color(0xFF282832) else Color.Transparent)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                viewModel.applySolidWallpaper(uiState.solidWallpaperColor, targetKey, context)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = targetLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            letterSpacing = 0.8.sp
                                        ),
                                        color = if (isSelected) TextPrimary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD 2: Estilos, Barra, Tipografia e Ícones
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AmoledCardBackground,
                border = BorderStroke(1.dp, AmoledCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Search Bar Style
                    CustomSettingsRow(
                        icon = Icons.Outlined.WebAsset,
                        title = "Search Bar Style",
                        subtitle = when (uiState.searchBarStyle) {
                            "split_pill" -> "Split Pill (Separate Gear)"
                            "pill" -> "Pill"
                            "split_rounded" -> "Split Rounded"
                            "rounded" -> "Rounded"
                            "split_square" -> "Split Square"
                            "square" -> "Square"
                            else -> "Split Pill (Separate Gear)"
                        },
                        onClick = { openModal = "style" }
                    )

                    CustomDivider()

                    // Texto da barra
                    CustomSettingsRow(
                        icon = Icons.Outlined.TextFields,
                        title = "Texto da barra",
                        subtitle = when (uiState.searchBarTextType) {
                            "app_name" -> "Searcho..."
                            "current_time" -> "Hora atual"
                            "greeting" -> "Saudação"
                            "custom" -> uiState.searchBarCustomText.ifBlank { "Texto próprio" }
                            else -> "Searcho..."
                        },
                        onClick = { openModal = "text" }
                    )

                    CustomDivider()

                    // Typography
                    CustomSettingsRow(
                        icon = Icons.Outlined.FontDownload,
                        title = "Typography",
                        subtitle = when (uiState.fontFamilyType) {
                            "searcho" -> "Searcho"
                            "system" -> "System"
                            "custom" -> "Custom Font"
                            else -> "Searcho"
                        },
                        onClick = { openModal = "typography" }
                    )

                    CustomDivider()

                    // Pacote de ícones
                    CustomSettingsRow(
                        icon = Icons.Outlined.GridView,
                        title = "Pacote de ícones",
                        subtitle = uiState.selectedIconPack?.substringAfterLast(".") ?: "Padrão",
                        onClick = { openModal = "pack" }
                    )

                    CustomDivider()

                    // Forma dos ícones
                    CustomSettingsRow(
                        icon = Icons.Outlined.Palette,
                        title = "Forma dos ícones",
                        subtitle = when (uiState.iconShape) {
                            "CYLINDER" -> "Cilindro"
                            "CIRCLE" -> "Círculo"
                            "LOSANGO" -> "Losango"
                            "SQUIRCLE" -> "Squircle"
                            "SQUARE" -> "Quadrado"
                            else -> "Padrão"
                        },
                        onClick = { openModal = "shape" }
                    )

                    CustomDivider()

                    // Switch Ícones com tema
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ícones com tema",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tinge os ícones na cor do seu tema.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isThemedIconsEnabled,
                            onCheckedChange = { viewModel.setThemedIconsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0xFF222228),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    CustomDivider()

                    // Switch Ocultar nomes dos apps
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ocultar nomes dos apps",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mostra só os ícones nos resultados.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isHideAppLabelsEnabled,
                            onCheckedChange = { viewModel.setHideAppLabelsEnabled(it) },
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
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun CustomSettingsRow(
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

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CustomDivider() {
    HorizontalDivider(
        color = Color(0xFF16161C),
        thickness = 1.dp,
        modifier = Modifier.padding(start = 68.dp, end = 16.dp)
    )
}
