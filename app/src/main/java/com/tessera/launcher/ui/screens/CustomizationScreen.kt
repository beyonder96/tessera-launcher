package com.tessera.launcher.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
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
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightDivider
import com.tessera.launcher.ui.theme.LightIconBackground
import com.tessera.launcher.ui.theme.LightIconTint
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
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
    val isLight = uiState.isLightMode
    val bg = if (isLight) LightBackground else if (uiState.isAmoledMode) AmoledBlack else DarkSurface
    val cardBg = if (isLight) LightCardBackground else if (uiState.isAmoledMode) AmoledCardBackground else DarkSurface
    val cardBorder = if (isLight) LightCardBorder else if (uiState.isAmoledMode) AmoledCardBorder else DarkSurfaceBorder
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary

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
                        tint = textPrimary,
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
                    color = textSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CARD 1: Papel de Parede do Sistema & Cor sólida
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
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
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Usa cores sólidas do tema quando está desligado",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = textSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isSystemWallpaperEnabled,
                            onCheckedChange = { viewModel.setSystemWallpaperEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = textSecondary,
                                uncheckedTrackColor = Color(0xFF222228),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    // Menos brilho para dar mais contraste (Pill Slider)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        PillPercentSlider(
                            title = "Escurecimento Home",
                            subtitle = "Menos brilho para aumentar o contraste",
                            percent = uiState.homeWallpaperDimming,
                            onPercentChange = { viewModel.setHomeWallpaperDimming(it) },
                            isLight = isLight
                        )
                    }

                    CustomDivider(isLight = isLight)

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

            // CARD 2: Vidro na Gaveta
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vidro na Gaveta",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mostra o papel de parede atrás dos resultados",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = textSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isDrawerGlassEnabled,
                            onCheckedChange = { viewModel.setDrawerGlassEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = textSecondary,
                                uncheckedTrackColor = Color(0xFF222228),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        PillPercentSlider(
                            title = "Desfoque do Vidro",
                            subtitle = "Ajuste a intensidade do efeito de blur do fundo",
                            percent = uiState.drawerGlassOpacity,
                            onPercentChange = { viewModel.setDrawerGlassOpacity(it) },
                            isLight = isLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD SMART DOCK: Previsão Contextual de Apps
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Smart Dock (Previsão Contextual)",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sugere apps contextuais no topo da lista com base no momento",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = textSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isSmartDockEnabled,
                            onCheckedChange = { viewModel.setSmartDockEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = textSecondary,
                                uncheckedTrackColor = Color(0xFF222228),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    if (uiState.isSmartDockEnabled) {
                        HorizontalDivider(
                            color = if (isLight) LightCardBorder else Color(0xFF1A1A22),
                            thickness = 1.dp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Quantidade de Apps",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = textPrimary
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isLight) Color(0xFFEFEFF2) else Color(0xFF131318))
                                    .border(1.dp, if (isLight) LightCardBorder else Color(0xFF22222A), RoundedCornerShape(10.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(3, 4, 5).forEach { count ->
                                    val isSelected = uiState.smartDockAppCount == count
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) {
                                                    if (isLight) Color.White else Color(0xFF282832)
                                                } else Color.Transparent
                                            )
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { viewModel.setSmartDockAppCount(count) }
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) textPrimary else textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD CATEGORIAS: Categorias Automáticas na Gaveta
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Categorias na Gaveta",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Agrupa apps por Produtividade, Social, Mídia, Utilitários e Jogos",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = textSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isAppCategoriesEnabled,
                            onCheckedChange = { viewModel.setAppCategoriesEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = textSecondary,
                                uncheckedTrackColor = Color(0xFF222228),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD 3: Estilos, Barra, Tipografia e Ícones
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
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
                        onClick = { openModal = "style" },
                        isLight = isLight
                    )

                    CustomDivider(isLight = isLight)

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
                        onClick = { openModal = "text" },
                        isLight = isLight
                    )

                    CustomDivider(isLight = isLight)

                    // Opacidade da Barra de Pesquisa (Liquid Design)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        PillPercentSlider(
                            title = "Opacidade da Barra de Pesquisa",
                            subtitle = "Transparência do vidro com acabamento Liquid Design",
                            percent = uiState.searchBarOpacity,
                            onPercentChange = { viewModel.setSearchBarOpacity(it) },
                            isLight = isLight
                        )
                    }

                    CustomDivider(isLight = isLight)

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
                        onClick = { openModal = "typography" },
                        isLight = isLight
                    )

                    CustomDivider(isLight = isLight)

                    // Pacote de ícones
                    CustomSettingsRow(
                        icon = Icons.Outlined.GridView,
                        title = "Pacote de ícones",
                        subtitle = uiState.selectedIconPack?.substringAfterLast(".") ?: "Padrão",
                        onClick = { openModal = "pack" },
                        isLight = isLight
                    )

                    CustomDivider(isLight = isLight)

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
                        onClick = { openModal = "shape" },
                        isLight = isLight
                    )

                    CustomDivider(isLight = isLight)

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
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tinge os ícones na cor do seu tema.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = textSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isThemedIconsEnabled,
                            onCheckedChange = { viewModel.setThemedIconsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = textSecondary,
                                uncheckedTrackColor = Color(0xFF222228),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    CustomDivider(isLight = isLight)

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
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mostra só os ícones nos resultados.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = textSecondary
                            )
                        }

                        Switch(
                            checked = uiState.isHideAppLabelsEnabled,
                            onCheckedChange = { viewModel.setHideAppLabelsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = textSecondary,
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
    onClick: () -> Unit,
    isLight: Boolean = false
) {
    val iconBg = if (isLight) Color(0xFFF1F3F5) else Color(0xFF191920)
    val iconTint = if (isLight) Color(0xFF111827) else TextPrimary
    val titleColor = if (isLight) LightTextPrimary else TextPrimary
    val subtitleColor = if (isLight) LightTextSecondary else TextSecondary
    val chevronTint = if (isLight) Color(0xFF9CA3AF) else TextTertiary

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
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
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
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = subtitleColor
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = chevronTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CustomDivider(isLight: Boolean = false) {
    HorizontalDivider(
        color = if (isLight) LightDivider else Color(0xFF16161C),
        thickness = 1.dp,
        modifier = Modifier.padding(start = 68.dp, end = 16.dp)
    )
}

@Composable
private fun PillPercentSlider(
    title: String,
    subtitle: String? = null,
    percent: Int,
    onPercentChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isLight: Boolean = false
) {
    val trackBg = if (isLight) Color(0xFFE5E7EB) else Color(0xFF14141A)
    val trackBorder = if (isLight) Color(0xFFD1D5DB) else Color(0xFF24242E)
    val handleBg = if (isLight) Color(0xFF111827) else Color.White
    val handleGrip = if (isLight) Color(0xFF6B7280) else Color(0xFF7E7E88)
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val badgeBg = if (isLight) Color(0xFFE5E7EB) else Color(0xFF1E1E28)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = textPrimary
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = textSecondary
                    )
                }
            }

            // Badge de Porcentagem
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Trilho deslizante limpo e contínuo com rastreamento a 120Hz
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(trackBg)
                .border(BorderStroke(1.dp, trackBorder), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val totalWidth = size.width.toFloat()
                        if (totalWidth > 0) {
                            val fraction = (down.position.x / totalWidth).coerceIn(0f, 1f)
                            val raw = (fraction * 100).toInt()
                            val snapped = (Math.round(raw / 5.0) * 5).toInt().coerceIn(0, 100)
                            onPercentChange(snapped)
                        }
                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val dragChange = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (dragChange.pressed) {
                                val width = size.width.toFloat()
                                if (width > 0) {
                                    val fraction = (dragChange.position.x / width).coerceIn(0f, 1f)
                                    val raw = (fraction * 100).toInt()
                                    val snapped = (Math.round(raw / 5.0) * 5).toInt().coerceIn(0, 100)
                                    onPercentChange(snapped)
                                }
                                dragChange.consume()
                            } else {
                                break
                            }
                        }
                    }
                }
                .padding(4.dp)
        ) {
            val handleWidth = 46.dp
            val fraction = (percent.coerceIn(0, 100) / 100f)

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val totalW = maxWidth
                val maxOffset = (totalW - handleWidth).coerceAtLeast(0.dp)
                val handleOffset = maxOffset * fraction

                // Pílula / Manopla deslizante sem texto sobreposto
                Box(
                    modifier = Modifier
                        .offset(x = handleOffset)
                        .width(handleWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .background(handleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(handleGrip)
                    )
                }
            }
        }
    }
}

