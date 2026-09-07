package com.tessera.launcher.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.tessera.launcher.data.helper.IconPackInfo
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary

// -------------------------------------------------------------
// 1. MODAL: SEARCH BAR STYLE
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarStyleBottomSheet(
    currentStyle: String,
    onSelectStyle: (String) -> Unit,
    onDismiss: () -> Unit,
    isAmoled: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val containerBg = if (isAmoled) AmoledBlack else DarkSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        dragHandle = { DragHandleIndicator() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "SEARCH BAR STYLE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            val styles = listOf(
                "pill" to "Pill",
                "split_pill" to "Split Pill",
                "rounded" to "Rounded",
                "split_rounded" to "Split Rounded",
                "square" to "Square",
                "split_square" to "Split Square"
            )

            styles.forEach { (styleKey, styleLabel) ->
                val isSelected = currentStyle.equals(styleKey, ignoreCase = true)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onSelectStyle(styleKey)
                                onDismiss()
                            }
                        )
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ilustração geométrica
                    SearchBarStyleGlyph(styleKey = styleKey)

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(
                        text = styleLabel,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 16.sp
                        ),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Selecionado",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SearchBarStyleGlyph(styleKey: String) {
    val glyphColor = Color(0xFF282830)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(76.dp)
    ) {
        when (styleKey) {
            "pill" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(glyphColor)
                )
            }
            "split_pill" -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(glyphColor)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(glyphColor)
                )
            }
            "rounded" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(glyphColor)
                )
            }
            "split_rounded" -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(glyphColor)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(glyphColor)
                )
            }
            "square" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(glyphColor)
                )
            }
            "split_square" -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .background(glyphColor)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(glyphColor)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2. MODAL: TEXTO DA BARRA
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarTextBottomSheet(
    currentTextType: String,
    customText: String,
    onSelectTextType: (String) -> Unit,
    onSaveCustomText: (String) -> Unit,
    onDismiss: () -> Unit,
    isAmoled: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val containerBg = if (isAmoled) AmoledBlack else DarkSurface
    var editingCustomText by remember { mutableStateOf(customText) }

    val previewText = when (currentTextType) {
        "app_name" -> "Searcho..."
        "current_time" -> "09:41"
        "greeting" -> "Bom dia!"
        "custom" -> editingCustomText.ifBlank { "Tessera..." }
        else -> "Searcho..."
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        dragHandle = { DragHandleIndicator() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "TEXTO DA BARRA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PRÉVIA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                ),
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Prévia da Barra
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF131317))
                    .border(1.dp, Color(0xFF22222A), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val options = listOf(
                Triple("app_name", "Nome do app", Icons.Outlined.AutoAwesome),
                Triple("current_time", "Hora atual", Icons.Outlined.AccessTime),
                Triple("greeting", "Saudação", Icons.Outlined.WbSunny),
                Triple("custom", "Texto próprio", Icons.Outlined.Edit)
            )

            options.forEach { (typeKey, typeLabel, icon) ->
                val isSelected = currentTextType == typeKey
                val cardBorder = if (isSelected) BorderStroke(1.5.dp, Color.White) else BorderStroke(1.dp, AmoledCardBorder)
                val cardBg = if (isSelected) Color(0xFF111116) else AmoledCardBackground

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(cardBorder, RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectTextType(typeKey) }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1C1C24)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Selecionado",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (currentTextType == "custom") {
                Spacer(modifier = Modifier.height(10.dp))
                BasicTextField(
                    value = editingCustomText,
                    onValueChange = {
                        editingCustomText = it
                        onSaveCustomText(it)
                    },
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(TextPrimary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131318))
                                .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (editingCustomText.isEmpty()) {
                                Text("Digite seu texto próprio...", color = TextTertiary, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. MODAL: TYPOGRAPHY
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypographyBottomSheet(
    currentFontType: String,
    customFontPath: String,
    onSelectFontType: (String) -> Unit,
    onImportFont: (Uri) -> Unit,
    onDismiss: () -> Unit,
    isAmoled: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val containerBg = if (isAmoled) AmoledBlack else DarkSurface

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportFont(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        dragHandle = { DragHandleIndicator() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "TYPOGRAPHY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "APP FONT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Card Searcho Font
            FontCard(
                title = "Searcho",
                sample = "Aa Bb Cc 0123",
                subtitle = "Space Grotesk — the built-in face",
                fontFamily = FontFamily.Monospace,
                isSelected = currentFontType == "searcho",
                onClick = { onSelectFontType("searcho") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Card System Font
            FontCard(
                title = "System",
                sample = "Aa Bb Cc 0123",
                subtitle = "Whatever your phone uses",
                fontFamily = FontFamily.SansSerif,
                isSelected = currentFontType == "system",
                onClick = { onSelectFontType("system") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "YOUR FONTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            if (customFontPath.isNotBlank() && currentFontType == "custom") {
                FontCard(
                    title = "Custom Font",
                    sample = "Aa Bb Cc 0123",
                    subtitle = customFontPath.substringAfterLast("/"),
                    fontFamily = FontFamily.Default,
                    isSelected = true,
                    onClick = { onSelectFontType("custom") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AmoledCardBackground)
                        .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 20.dp, horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No fonts added yet. Bring your own .ttf or .otf and Searcho will use it everywhere.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Botão [+] ADD A FONT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF16161D))
                    .border(1.dp, Color(0xFF262632), RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PostAdd,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADD A FONT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Fonts are copied into Searcho and stay on your device. Only .ttf and .otf files work.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                color = TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FontCard(
    title: String,
    sample: String,
    subtitle: String,
    fontFamily: FontFamily,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardBorder = if (isSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else BorderStroke(1.dp, AmoledCardBorder)
    val cardBg = if (isSelected) Color(0xFF121217) else AmoledCardBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(cardBorder, RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sample,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 18.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Selecionado",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. MODAL: FORMA DOS ÍCONES (CARROSSEL HORIZONTAL)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconShapeBottomSheet(
    currentShape: String,
    onSelectShape: (String) -> Unit,
    onDismiss: () -> Unit,
    isAmoled: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val containerBg = if (isAmoled) AmoledBlack else DarkSurface

    val shapes = listOf(
        "CIRCLE" to "Círculo",
        "CYLINDER" to "Cilindro",
        "LOSANGO" to "Losango",
        "SQUIRCLE" to "Squircle",
        "SQUARE" to "Quadrado",
        "DEFAULT" to "Padrão"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        dragHandle = { DragHandleIndicator() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "FORMA DOS ÍCONES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                shapes.forEach { (shapeKey, shapeLabel) ->
                    val isSelected = currentShape.equals(shapeKey, ignoreCase = true)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onSelectShape(shapeKey)
                                    onDismiss()
                                }
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(
                                    when (shapeKey) {
                                        "CIRCLE" -> CircleShape
                                        "CYLINDER" -> RoundedCornerShape(28.dp)
                                        "SQUIRCLE" -> RoundedCornerShape(22.dp)
                                        "SQUARE" -> RoundedCornerShape(6.dp)
                                        else -> RoundedCornerShape(20.dp)
                                    }
                                )
                                .border(
                                    border = if (isSelected) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, AmoledCardBorder),
                                    shape = when (shapeKey) {
                                        "CIRCLE" -> CircleShape
                                        "CYLINDER" -> RoundedCornerShape(28.dp)
                                        "SQUIRCLE" -> RoundedCornerShape(22.dp)
                                        "SQUARE" -> RoundedCornerShape(6.dp)
                                        else -> RoundedCornerShape(20.dp)
                                    }
                                )
                                .background(if (isSelected) Color(0xFF26262E) else Color(0xFF16161B)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Glifo representativo
                            ShapeGlyphVisual(shapeKey = shapeKey)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = shapeLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeGlyphVisual(shapeKey: String) {
    when (shapeKey) {
        "CIRCLE" -> {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            )
        }
        "CYLINDER" -> {
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(13.dp))
            )
        }
        "LOSANGO" -> {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .rotate(45f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            )
        }
        "SQUIRCLE" -> {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            )
        }
        "SQUARE" -> {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            )
        }
    }
}

// -------------------------------------------------------------
// 5. MODAL: PACOTE DE ÍCONES (CARROSSEL HORIZONTAL)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPackBottomSheet(
    selectedPack: String?,
    installedPacks: List<IconPackInfo>,
    onSelectPack: (String?) -> Unit,
    onDismiss: () -> Unit,
    isAmoled: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val containerBg = if (isAmoled) AmoledBlack else DarkSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        dragHandle = { DragHandleIndicator() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "PACOTE DE ÍCONES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Opção Padrão (Sem pacote)
                val isDefaultSelected = selectedPack == null
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onSelectPack(null)
                            onDismiss()
                        }
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                border = if (isDefaultSelected) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, AmoledCardBorder),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .background(if (isDefaultSelected) Color(0xFF26262E) else Color(0xFF141418)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Padrão",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Padrão",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isDefaultSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        ),
                        color = if (isDefaultSelected) TextPrimary else TextSecondary
                    )
                }

                // Pacotes Instalados
                installedPacks.forEach { pack ->
                    val isSelected = selectedPack == pack.packageName
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(84.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onSelectPack(pack.packageName)
                                    onDismiss()
                                }
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .border(
                                    border = if (isSelected) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, AmoledCardBorder),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .background(if (isSelected) Color(0xFF26262E) else Color(0xFF141418)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (pack.icon != null) {
                                Image(
                                    bitmap = pack.icon.toBitmap().asImageBitmap(),
                                    contentDescription = pack.label,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = pack.label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) TextPrimary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DragHandleIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF383842))
        )
    }
}
