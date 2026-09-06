package com.tessera.launcher.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LiquidGlassBackground
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary

data class NoteTask(val id: Long, val text: String, var isDone: Boolean = false)

@Composable
fun NotesWidgetCard(
    tasks: List<NoteTask>,
    onAddTask: (String) -> Unit,
    onToggleTask: (Long) -> Unit,
    onRemoveTask: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true
) {
    var newTaskText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val border = if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(border, CardShape)
            .clip(CardShape)
            .background(background)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B1E28)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventNote,
                            contentDescription = "Notas",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NOTAS & TAREFAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                }

                Text(
                    text = "${tasks.count { it.isDone }}/${tasks.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lista de Tarefas (até 3 visíveis no mini card)
            tasks.take(3).forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onToggleTask(task.id) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .border(1.dp, if (task.isDone) Color.White else Color(0xFF48484A), RoundedCornerShape(4.dp))
                            .background(if (task.isDone) Color.White else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isDone) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = task.text,
                        color = if (task.isDone) TextTertiary else TextPrimary,
                        fontSize = 13.sp,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remover",
                        tint = TextTertiary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onRemoveTask(task.id) }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo para Adicionar Nova Tarefa com suporte a clique amplo e ativação do teclado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(Color(0xFF161820), RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, Color(0xFF242732)), RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    )
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(TextPrimary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newTaskText.isNotBlank()) {
                                onAddTask(newTaskText.trim())
                                newTaskText = ""
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        if (newTaskText.isEmpty()) {
                            Text("Nova anotação ou tarefa...", color = TextTertiary, fontSize = 13.sp)
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )

                if (newTaskText.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Adicionar",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onAddTask(newTaskText.trim())
                                    newTaskText = ""
                                }
                            )
                    )
                }
            }
        }
    }
}
