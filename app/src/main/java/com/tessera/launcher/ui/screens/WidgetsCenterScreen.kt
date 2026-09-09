package com.tessera.launcher.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbSunny
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
import com.tessera.launcher.ui.theme.AmoledBlack
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

    val isLight = uiState.isLightMode
    val screenBg = when {
        isLight -> Color(0xFFF8FAFC)
        uiState.isAmoledMode -> AmoledBlack
        else -> DarkBackground
    }
    val cardBg = if (isLight) Color(0xFFFFFFFF) else WidgetCenterCardBackground
    val cardBorder = if (isLight) Color(0xFFCBD5E1) else WidgetCenterCardBorder
    val dividerColor = if (isLight) Color(0xFFF1F5F9) else WidgetCenterDividerColor
    val iconBg = if (isLight) Color(0xFFF1F5F9) else WidgetCenterIconBackground
    val primaryTextColor = if (isLight) Color(0xFF0F172A) else TextPrimary
    val secondaryTextColor = if (isLight) Color(0xFF64748B) else TextSecondary
    val tertiaryTextColor = if (isLight) Color(0xFF94A3B8) else TextTertiary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
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
                    tint = primaryTextColor,
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
                color = secondaryTextColor,
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
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 8.dp)
            )

            Surface(
                shape = WidgetCenterCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    WidgetCenterToggleRow(
                        icon = Icons.Outlined.SportsEsports,
                        title = "Jogo do dino",
                        subtitle = "Jogo retrô jogável do dinossauro pulando",
                        checked = uiState.isDinoWidgetEnabled,
                        onCheckedChange = { viewModel.setDinoWidgetEnabled(it) },
                        isLightMode = isLight,
                        iconBgColor = iconBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    WidgetCenterToggleRow(
                        icon = Icons.Outlined.MenuBook,
                        title = "Notas e diário",
                        subtitle = "Notas rápidas, lista de tarefas e entradas do diário",
                        checked = uiState.isNotesWidgetEnabled,
                        onCheckedChange = { viewModel.setNotesWidgetEnabled(it) },
                        isLightMode = isLight,
                        iconBgColor = iconBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
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
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = WidgetCenterCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
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
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = primaryTextColor,
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
                            color = primaryTextColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.hasNotificationAccess) "1 app permitido" else "Permissão necessária",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = secondaryTextColor
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = tertiaryTextColor,
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
                color = tertiaryTextColor,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Surface(
                shape = WidgetCenterCardShape,
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    WidgetCenterToggleRow(
                        icon = Icons.Outlined.FlashOn,
                        title = "Trocar ao tocar música",
                        subtitle = "Trocar para o player automaticamente quando o áudio começar",
                        checked = uiState.isSwitchOnMusicPlayEnabled,
                        onCheckedChange = { viewModel.setSwitchOnMusicPlayEnabled(it) },
                        isLightMode = isLight,
                        iconBgColor = iconBg,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    HorizontalDivider(
                        color = dividerColor,
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
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                tint = primaryTextColor,
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
                                color = primaryTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cartão exibido quando nada mais está acontecendo",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = secondaryTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grade dos 8 Cartões Padrão (2 Linhas x 4 Colunas Perfeitas)
                    // Linha 1: Índices 0 a 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // 0: Ações Rápidas
                        WidgetCardSelectorCircle(
                            label = "Ações\nrápidas",
                            icon = Icons.Outlined.FlashlightOn,
                            isSelected = uiState.defaultWidgetCardIndex == 0,
                            onClick = { viewModel.setDefaultWidgetCardIndex(0) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            modifier = Modifier.weight(1f)
                        )

                        // 1: Bateria e Sinais
                        WidgetCardSelectorCircle(
                            label = "Bateria e\nsinais",
                            icon = Icons.Outlined.BatteryStd,
                            isSelected = uiState.defaultWidgetCardIndex == 1,
                            onClick = { viewModel.setDefaultWidgetCardIndex(1) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            modifier = Modifier.weight(1f)
                        )

                        // 2: Agenda e Data
                        WidgetCardSelectorCircle(
                            label = "Agenda e\ndata",
                            icon = Icons.Outlined.CalendarToday,
                            isSelected = uiState.defaultWidgetCardIndex == 2,
                            onClick = { viewModel.setDefaultWidgetCardIndex(2) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            modifier = Modifier.weight(1f)
                        )

                        // 3: Player de Música
                        WidgetCardSelectorCircle(
                            label = "Player de\nmúsica",
                            icon = Icons.Outlined.MusicNote,
                            isSelected = uiState.defaultWidgetCardIndex == 3,
                            onClick = { viewModel.setDefaultWidgetCardIndex(3) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Linha 2: Índices 4 a 7
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // 4: Foco e Frase
                        WidgetCardSelectorCircle(
                            label = "Foco e frase\ndo dia",
                            icon = Icons.Outlined.FormatQuote,
                            isSelected = uiState.defaultWidgetCardIndex == 4,
                            onClick = { viewModel.setDefaultWidgetCardIndex(4) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            modifier = Modifier.weight(1f)
                        )

                        // 5: Clima & Temperatura
                        WidgetCardSelectorCircle(
                            label = "Clima e\ntempo",
                            icon = Icons.Outlined.WbSunny,
                            isSelected = uiState.defaultWidgetCardIndex == 5,
                            onClick = { viewModel.setDefaultWidgetCardIndex(5) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            modifier = Modifier.weight(1f)
                        )

                        // 6: Jogo do Dino
                        WidgetCardSelectorCircle(
                            label = "Jogo do\ndino",
                            icon = Icons.Outlined.SportsEsports,
                            isSelected = uiState.defaultWidgetCardIndex == 6,
                            onClick = { viewModel.setDefaultWidgetCardIndex(6) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            modifier = Modifier.weight(1f)
                        )

                        // 7: Notas e Diário
                        WidgetCardSelectorCircle(
                            label = "Notas e\ndiário",
                            icon = Icons.AutoMirrored.Outlined.EventNote,
                            isSelected = uiState.defaultWidgetCardIndex == 7,
                            onClick = { viewModel.setDefaultWidgetCardIndex(7) },
                            isLightMode = isLight,
                            iconBgColor = iconBg,
                            modifier = Modifier.weight(1f)
                        )
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
    isLightMode: Boolean = false,
    iconBgColor: Color = WidgetCenterIconBackground,
    modifier: Modifier = Modifier
) {
    val circleBg = when {
        isSelected -> if (isLightMode) Color(0xFF0F172A) else Color.White
        isLightMode -> Color(0xFFF1F5F9)
        else -> iconBgColor
    }
    val iconTint = when {
        isSelected -> if (isLightMode) Color.White else Color.Black
        isLightMode -> Color(0xFF64748B)
        else -> Color(0xFF8E8E93)
    }
    val labelColor = when {
        isSelected -> if (isLightMode) Color(0xFF0F172A) else Color.White
        isLightMode -> Color(0xFF64748B)
        else -> Color(0xFF727275)
    }
    val circleBorder = if (isLightMode && !isSelected) BorderStroke(1.dp, Color(0xFFCBD5E1)) else null

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
                .then(if (circleBorder != null) Modifier.border(circleBorder, CircleShape) else Modifier)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = labelColor,
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
    onCheckedChange: (Boolean) -> Unit,
    isLightMode: Boolean = false,
    iconBgColor: Color = WidgetCenterIconBackground,
    primaryTextColor: Color = TextPrimary,
    secondaryTextColor: Color = TextSecondary
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
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryTextColor,
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
                color = primaryTextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isLightMode) Color.White else Color.Black,
                checkedTrackColor = if (isLightMode) Color(0xFF0F172A) else Color.White,
                checkedBorderColor = if (isLightMode) Color(0xFF0F172A) else Color.White,
                uncheckedThumbColor = if (isLightMode) Color(0xFF94A3B8) else Color(0xFF8E8E93),
                uncheckedTrackColor = if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF1E1E24),
                uncheckedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color(0xFF2C2C34)
            )
        )
    }
}
