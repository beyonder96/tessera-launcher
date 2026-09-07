package com.tessera.launcher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val HiddenCardShape = RoundedCornerShape(24.dp)
private val HiddenCardBackground = Color(0xFF0F0F12)
private val HiddenCardBorder = Color(0xFF1D1D22)
private val HiddenDividerColor = Color(0xFF18181D)

@Composable
fun HiddenAppsScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isUnlocked by remember { mutableStateOf(uiState.hiddenAppsPin.isEmpty() || uiState.isPinUnlocked) }
    var enteredPin by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var showSetPinDialog by remember { mutableStateOf(false) }

    val allApps = (uiState.appsState as? com.tessera.launcher.ui.state.AppsListState.Success)?.apps ?: emptyList()

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
        // Top Bar
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
                text = "APPS OCULTOS",
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

        if (!isUnlocked) {
            // Tela de Autenticação por PIN
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Digite seu PIN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Acesso protegido a aplicativos ocultos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                BasicTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 6) {
                            enteredPin = it
                            if (it == uiState.hiddenAppsPin) {
                                isUnlocked = true
                                viewModel.unlockHiddenAppsWithPin(it)
                            }
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (enteredPin == uiState.hiddenAppsPin) {
                                isUnlocked = true
                                viewModel.unlockHiddenAppsWithPin(enteredPin)
                            } else {
                                Toast.makeText(context, "PIN incorreto", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                    textStyle = TextStyle(color = TextPrimary, fontSize = 24.sp, textAlign = TextAlign.Center, letterSpacing = 8.sp),
                    cursorBrush = SolidColor(TextPrimary),
                    modifier = Modifier
                        .width(180.dp)
                        .background(Color(0xFF19191E), RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }
        } else {
            // Lista de Apps para Ocultar / Desocultar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OCULTAR DA GAVETA & BUSCA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = TextTertiary
                )
                Text(
                    text = if (uiState.hiddenAppsPin.isEmpty()) "Definir PIN" else "Alterar PIN",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSetPinDialog = true }
                        .padding(4.dp)
                )
            }

            Surface(
                shape = HiddenCardShape,
                color = HiddenCardBackground,
                border = BorderStroke(1.dp, HiddenCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    items(allApps, key = { it.packageName }) { app ->
                        val isHidden = uiState.hiddenAppsPackages.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF19191E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.firstLetter.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isHidden) "Oculto (visível apenas com PIN)" else app.packageName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = if (isHidden) Color(0xFFEF5350) else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                checked = isHidden,
                                onCheckedChange = { viewModel.toggleHiddenApp(app.packageName) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Color.White,
                                    uncheckedThumbColor = Color(0xFF636366),
                                    uncheckedTrackColor = Color(0xFF2C2C2E)
                                )
                            )
                        }
                        HorizontalDivider(color = HiddenDividerColor, thickness = 1.dp)
                    }
                }
            }
        }
    }

    if (showSetPinDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSetPinDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Configurar PIN de Acesso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Digite um PIN numérico (4 a 6 dígitos). Digitar esse PIN na barra de busca revelará os apps ocultos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BasicTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) newPinInput = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 22.sp, textAlign = TextAlign.Center, letterSpacing = 6.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        cursorBrush = SolidColor(TextPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161820), RoundedCornerShape(10.dp))
                            .padding(vertical = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancelar",
                            color = TextSecondary,
                            modifier = Modifier.clickable { showSetPinDialog = false }.padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Salvar",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.clickable {
                                if (newPinInput.length >= 4) {
                                    viewModel.setHiddenAppsPin(newPinInput)
                                    showSetPinDialog = false
                                    Toast.makeText(context, "PIN salvo com sucesso!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "O PIN deve ter pelo menos 4 números", Toast.LENGTH_SHORT).show()
                                }
                            }.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
