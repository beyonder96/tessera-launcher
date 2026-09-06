package com.tessera.launcher.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel

private val WidgetCenterCardShape = RoundedCornerShape(24.dp)
private val WidgetCenterCardBackground = Color(0xFF0F0F12)
private val WidgetCenterCardBorder = Color(0xFF1D1D22)
private val WidgetCenterDividerColor = Color(0xFF18181D)
private val WidgetCenterIconBackground = Color(0xFF19191E)

@Composable
fun WidgetsCenterScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        // Top Bar Centrada: Botão Voltar + "CENTRAL DE WIDGETS"
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
                text = "CENTRAL DE WIDGETS",
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Bloco 1: UTILITÁRIOS
            Text(
                text = "UTILITÁRIOS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 8.dp)
            )

            Surface(
                shape = WidgetCenterCardShape,
                color = WidgetCenterCardBackground,
                border = BorderStroke(1.dp, WidgetCenterCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    WidgetCenterToggleRow(
                        icon = Icons.Outlined.SportsEsports,
                        title = "Jogo do dino",
                        subtitle = "Jogo retrô jogável do dinossauro pulando",
                        checked = uiState.isDinoWidgetEnabled,
                        onCheckedChange = { viewModel.setDinoWidgetEnabled(it) }
                    )

                    HorizontalDivider(color = WidgetCenterDividerColor, thickness = 1.dp)

                    WidgetCenterToggleRow(
                        icon = Icons.Outlined.MenuBook,
                        title = "Notas e diário",
                        subtitle = "Notas rápidas, lista de tarefas e entradas do diário",
                        checked = uiState.isNotesWidgetEnabled,
                        onCheckedChange = { viewModel.setNotesWidgetEnabled(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bloco 2: SOBREPOSIÇÃO DE NOTIFICAÇÕES
            Text(
                text = "SOBREPOSIÇÃO DE NOTIFICAÇÕES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = WidgetCenterCardShape,
                color = WidgetCenterCardBackground,
                border = BorderStroke(1.dp, WidgetCenterCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WidgetCenterIconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Apps permitidos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.hasNotificationAccess) "1 app permitido" else "Permissão necessária",
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

            Spacer(modifier = Modifier.height(24.dp))

            // Bloco 3: COMPORTAMENTO E PADRÃO
            Text(
                text = "COMPORTAMENTO E PADRÃO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextTertiary,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = WidgetCenterCardShape,
                color = WidgetCenterCardBackground,
                border = BorderStroke(1.dp, WidgetCenterCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    WidgetCenterToggleRow(
                        icon = Icons.Outlined.FlashOn,
                        title = "Trocar ao tocar música",
                        subtitle = "Trocar para o player automaticamente quando o áudio começar",
                        checked = uiState.isSwitchOnMusicPlayEnabled,
                        onCheckedChange = { viewModel.setSwitchOnMusicPlayEnabled(it) }
                    )

                    HorizontalDivider(
                        color = WidgetCenterDividerColor,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // Header do Cartão Padrão
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WidgetCenterIconBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "CARTÃO PADRÃO",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cartão exibido quando nada mais está acontecendo",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grade de Cartões Padrão (7 Chips Circulares Conforme Imagem 5)
                    // Linha 1: 4 itens
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        WidgetCardSelectorCircle(
                            label = "Agenda e\ndata",
                            icon = Icons.Outlined.CalendarToday,
                            isSelected = uiState.defaultWidgetCardIndex == 0,
                            onClick = { viewModel.setDefaultWidgetCardIndex(0) },
                            modifier = Modifier.weight(1f)
                        )

                        WidgetCardSelectorCircle(
                            label = "Bateria e\nsinais",
                            icon = Icons.Outlined.BatteryStd,
                            isSelected = uiState.defaultWidgetCardIndex == 1,
                            onClick = { viewModel.setDefaultWidgetCardIndex(1) },
                            modifier = Modifier.weight(1f)
                        )

                        WidgetCardSelectorCircle(
                            label = "Player de\nmúsica",
                            icon = Icons.Outlined.MusicNote,
                            isSelected = uiState.defaultWidgetCardIndex == 2,
                            onClick = { viewModel.setDefaultWidgetCardIndex(2) },
                            modifier = Modifier.weight(1f)
                        )

                        WidgetCardSelectorCircle(
                            label = "Barra de\nações rápid...",
                            icon = Icons.Outlined.Tune,
                            isSelected = uiState.defaultWidgetCardIndex == 3,
                            onClick = { viewModel.setDefaultWidgetCardIndex(3) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Linha 2: 3 itens
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        WidgetCardSelectorCircle(
                            label = "Foco e frase\ndo dia",
                            icon = Icons.Outlined.FormatQuote,
                            isSelected = uiState.defaultWidgetCardIndex == 4,
                            onClick = { viewModel.setDefaultWidgetCardIndex(4) },
                            modifier = Modifier.weight(1f)
                        )

                        WidgetCardSelectorCircle(
                            label = "Jogo do dino",
                            icon = Icons.Outlined.SportsEsports,
                            isSelected = uiState.defaultWidgetCardIndex == 5,
                            onClick = { viewModel.setDefaultWidgetCardIndex(5) },
                            modifier = Modifier.weight(1f)
                        )

                        WidgetCardSelectorCircle(
                            label = "Notas e\ndiário",
                            icon = Icons.Outlined.MenuBook,
                            isSelected = uiState.defaultWidgetCardIndex == 6,
                            onClick = { viewModel.setDefaultWidgetCardIndex(6) },
                            modifier = Modifier.weight(1f)
                        )

                        // Espaço vazio para manter alinhamento estrito de 4 colunas
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WidgetCardSelectorCircle(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color.White else WidgetCenterIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else Color(0xFF8E8E93),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF727275),
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WidgetCenterToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WidgetCenterIconBackground),
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

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
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
