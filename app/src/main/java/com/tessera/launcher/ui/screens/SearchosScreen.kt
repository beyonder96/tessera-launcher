package com.tessera.launcher.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.state.SearchoItem
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val SearchoCardShape = RoundedCornerShape(24.dp)
private val SearchoCardBackground = Color(0xFF0F0F12)
private val SearchoCardBorder = Color(0xFF1D1D22)
private val SearchoDividerColor = Color(0xFF18181D)
private val SearchoIconBackground = Color(0xFF19191E)
private val SearchoChipBackground = Color(0xFF1B1C22)
private val SearchoChipBorder = Color(0xFF2C2D35)
private val DeleteRedColor = Color(0xFFCF6679)

@Composable
fun SearchosScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showSymbolDialog by remember { mutableStateOf(false) }

    val statusBarPadding = if (uiState.isShowStatusBarEnabled) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = statusBarPadding)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        // Top Bar Centrada: Botão Voltar + "SEARCHOS"
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
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "SEARCHOS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Bloco 1: SÍMBOLO DE ATIVAÇÃO
            Text(
                text = "SÍMBOLO DE ATIVAÇÃO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 8.dp)
            )

            Surface(
                shape = SearchoCardShape,
                color = SearchoCardBackground,
                border = BorderStroke(1.dp, SearchoCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showSymbolDialog = true }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SearchoIconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tag,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Símbolo de ativação",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Símbolo atual: \"${uiState.searchoActivationSymbol}\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Bloco 2: SEARCHOS ATIVOS
            Text(
                text = "SEARCHOS ATIVOS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = SearchoCardShape,
                color = SearchoCardBackground,
                border = BorderStroke(1.dp, SearchoCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    uiState.searchosList.forEachIndexed { index, item ->
                        SearchoRowItem(
                            item = item,
                            onDelete = { viewModel.removeSearcho(item.id) }
                        )
                        HorizontalDivider(color = SearchoDividerColor, thickness = 1.dp)
                    }

                    // Item: + Adicionar Searcho
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showAddDialog = true }
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SearchoIconBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Adicionar Searcho",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Veja tudo o que a barra pode fazer.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bloco 3: AÇÕES RÁPIDAS DO SISTEMA
            Text(
                text = "AÇÕES RÁPIDAS DO SISTEMA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = SearchoCardShape,
                color = SearchoCardBackground,
                border = BorderStroke(1.dp, SearchoCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                Toast.makeText(context, "Ações rápidas do sistema personalizadas.", Toast.LENGTH_SHORT).show()
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SearchoIconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Personalizar ações rápidas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Escolha quais ações do sistema aparecem.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSymbolDialog) {
        var newSymbol by remember { mutableStateOf(uiState.searchoActivationSymbol) }
        Dialog(onDismissRequest = { showSymbolDialog = false }) {
            Surface(
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Símbolo de Ativação",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("@", "/", "#", "!").forEach { sym ->
                            Surface(
                                shape = PillShape,
                                color = if (newSymbol == sym) Color.White else SearchoIconBackground,
                                border = BorderStroke(1.dp, if (newSymbol == sym) Color.White else SearchoChipBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { newSymbol = sym }
                            ) {
                                Text(
                                    text = sym,
                                    color = if (newSymbol == sym) Color.Black else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancelar",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable { showSymbolDialog = false }
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Salvar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable {
                                    viewModel.setSearchoActivationSymbol(newSymbol)
                                    showSymbolDialog = false
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var prefix by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = CardShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Novo Searcho",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(TextPrimary),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .background(Color(0xFF161820), RoundedCornerShape(10.dp))
                                    .border(BorderStroke(1.dp, Color(0xFF282B36)), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (title.isEmpty()) Text("Nome (ex: YouTube)", color = TextTertiary, fontSize = 14.sp)
                                inner()
                            }
                        }
                    )

                    BasicTextField(
                        value = prefix,
                        onValueChange = { prefix = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(TextPrimary),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .background(Color(0xFF161820), RoundedCornerShape(10.dp))
                                    .border(BorderStroke(1.dp, Color(0xFF282B36)), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (prefix.isEmpty()) Text("Prefixo (ex: @yt)", color = TextTertiary, fontSize = 14.sp)
                                inner()
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancelar",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable { showAddDialog = false }
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Adicionar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable {
                                    if (title.isNotBlank() && prefix.isNotBlank()) {
                                        viewModel.addSearcho(title.trim(), prefix.trim())
                                        showAddDialog = false
                                    }
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchoRowItem(
    item: SearchoItem,
    onDelete: () -> Unit
) {
    val icon = when (item.iconType) {
        "tasks" -> Icons.Outlined.CheckBox
        "notes" -> Icons.Outlined.Description
        "quick_actions" -> Icons.Outlined.ChevronRight
        "send_message" -> Icons.Outlined.Send
        "keep" -> Icons.Outlined.Edit
        "messages" -> Icons.Outlined.Chat
        "whatsapp" -> Icons.Outlined.Chat
        "telegram" -> Icons.Outlined.Send
        "notifications" -> Icons.Outlined.Notifications
        "contacts" -> Icons.Outlined.People
        "activity" -> Icons.Outlined.Schedule
        "obsidian" -> Icons.Outlined.MenuBook
        else -> Icons.Outlined.Tag
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SearchoIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Prefixo \"${item.prefix}\"",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }

        // Chip do Prefixo (ex: "@wa") Conforme Imagens 1 e 2
        Surface(
            shape = PillShape,
            color = SearchoChipBackground,
            border = BorderStroke(1.dp, SearchoChipBorder),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(
                text = item.prefix,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }

        // Ícone de Remoção (Lixeira Vermelha) Conforme Imagens 1 e 2
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Remover",
            tint = DeleteRedColor,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDelete
                )
        )
    }
}
