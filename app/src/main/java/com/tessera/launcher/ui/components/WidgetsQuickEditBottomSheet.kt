package com.tessera.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.preference.WidgetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsQuickEditBottomSheet(
    userName: String,
    onSaveUserName: (String) -> Unit,
    enabledWidgets: List<WidgetType>,
    onToggleWidget: (WidgetType) -> Unit,
    onMoveWidgetUp: (WidgetType) -> Unit,
    onMoveWidgetDown: (WidgetType) -> Unit,
    isDinoEnabled: Boolean,
    onToggleDino: (Boolean) -> Unit,
    isScreenTimeEnabled: Boolean = false,
    onToggleScreenTime: (Boolean) -> Unit = {},
    isSmartDockEnabled: Boolean,
    onToggleSmartDock: (Boolean) -> Unit,
    isLightMode: Boolean,
    onOpenFullSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localName by remember(userName) { mutableStateOf(userName) }

    val bgColor = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF14161E)
    val cardColor = if (isLightMode) Color(0xFFEFECE2) else Color(0xFF1D202B)
    val borderColor = if (isLightMode) Color(0xFFDDD8CB) else Color(0x25FFFFFF)
    val textPrimary = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
    val textSecondary = if (isLightMode) Color(0xFF78716C) else Color(0xFFA8A29E)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(if (isLightMode) Color(0xFFCCC6B6) else Color(0x35FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabeçalho da Folha de Edição
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .journalFacetedSurface(shape = CircleShape, isLightMode = isLightMode),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Personalizar Central",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Ajuste a saudação e a ordem dos módulos ao vivo",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar",
                        tint = textSecondary
                    )
                }
            }

            // Bloco de Edição do Nome
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardColor)
                    .border(0.5.dp, borderColor, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SEU NOME NA SAUDAÇÃO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = localName,
                            onValueChange = {
                                localName = it
                                onSaveUserName(it)
                            },
                            placeholder = { Text("Ex: Kenned", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = textPrimary,
                                unfocusedBorderColor = borderColor,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB))
                                .clickable { onSaveUserName(localName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Salvar",
                                tint = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF1C1917),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Lista de Widgets e Reordenação
            Text(
                text = "MÓDULOS & ORDEM DA CENTRAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = textSecondary
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(enabledWidgets, key = { _, widget -> widget.name }) { index, widget ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardColor)
                            .border(0.5.dp, borderColor, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = widget.displayName,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onMoveWidgetUp(widget) },
                                enabled = index > 0,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = "Subir",
                                    tint = if (index > 0) textPrimary else textSecondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onMoveWidgetDown(widget) },
                                enabled = index < enabledWidgets.size - 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowDownward,
                                    contentDescription = "Descer",
                                    tint = if (index < enabledWidgets.size - 1) textPrimary else textSecondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Switch(
                                checked = true,
                                onCheckedChange = { onToggleWidget(widget) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF1C1917),
                                    checkedTrackColor = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
                                )
                            )
                        }
                    }
                }

                // Chaves extras opcionais
                item {
                    ExtraWidgetRow(
                        title = "Apps Sugeridos (Bandeja Contextual)",
                        checked = isSmartDockEnabled,
                        onCheckedChange = onToggleSmartDock,
                        cardColor = cardColor,
                        borderColor = borderColor,
                        textPrimary = textPrimary,
                        isLightMode = isLightMode
                    )
                }

                item {
                    ExtraWidgetRow(
                        title = "Dino Game Widget",
                        checked = isDinoEnabled,
                        onCheckedChange = onToggleDino,
                        cardColor = cardColor,
                        borderColor = borderColor,
                        textPrimary = textPrimary,
                        isLightMode = isLightMode
                    )
                }
            }

            // Botão de Rodapé para abrir configurações completas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB))
                    .tesseraPressable(onClick = {
                        onDismiss()
                        onOpenFullSettings()
                    })
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF1C1917),
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Configurações Completas da Tela −1",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF1C1917)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtraWidgetRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    cardColor: Color,
    borderColor: Color,
    textPrimary: Color,
    isLightMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF1C1917),
                checkedTrackColor = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
            )
        )
    }
}
