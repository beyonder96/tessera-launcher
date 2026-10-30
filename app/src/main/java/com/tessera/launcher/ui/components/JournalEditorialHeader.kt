package com.tessera.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.helper.ScreenTimeInfo
import com.tessera.launcher.data.helper.WeatherInfo
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalEditorialHeader(
    userName: String,
    formattedDate: String,
    weatherInfo: WeatherInfo?,
    nextCalendarEvent: CalendarEventInfo?,
    pendingNotesCount: Int,
    screenTimeInfo: ScreenTimeInfo?,
    isLightMode: Boolean,
    onOpenQuickEdit: () -> Unit,
    onBackToHome: () -> Unit,
    onWeatherClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onNotesClick: () -> Unit,
    onScreenTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = remember { Calendar.getInstance() }
    val hour = remember { cal.get(Calendar.HOUR_OF_DAY) }

    val dayAbbrev = remember {
        val raw = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale("pt", "BR")) ?: "HOJE"
        raw.replace(".", "").uppercase(Locale("pt", "BR")).take(3)
    }

    val greeting = remember(hour) {
        when (hour) {
            in 5..11 -> "Bom dia"
            in 12..17 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    val primaryTextColor = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
    val mutedTextColor = if (isLightMode) Color(0xFF78716C) else Color(0xFFA8A29E)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Linha Superior: Dia em destaque ("QUA •") + Data, e Ações à direita (Editar + Voltar →)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = dayAbbrev,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryTextColor,
                    letterSpacing = (-1.5).sp
                )
                // Assinatura Coral Dot (estilo Tessera)
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formattedDate,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = mutedTextColor
                )
            }

            // Medalhões do Topo: Editar + Voltar (→)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JournalMedallion(
                    icon = Icons.Outlined.Tune,
                    contentDescription = "Personalizar Central",
                    size = 38.dp,
                    iconSize = 18.dp,
                    isLightMode = isLightMode,
                    onClick = onOpenQuickEdit
                )

                JournalMedallion(
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "Voltar para Início",
                    size = 38.dp,
                    iconSize = 18.dp,
                    isLightMode = isLightMode,
                    onClick = onBackToHome
                )
            }
        }

        // 2. Narrativa Editorial em FlowRow com micro-chips exclusivos (Agenda & Tarefas)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$greeting,",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = mutedTextColor
            )

            // Nome do usuário (ou 'Kenned') com toque para personalizar
            Text(
                text = if (userName.isNotBlank()) "$userName." else "você.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                modifier = Modifier.tesseraPressable(
                    pressedScale = 0.94f,
                    onClick = onOpenQuickEdit
                )
            )

            // Chip Agenda
            if (nextCalendarEvent != null) {
                val title = nextCalendarEvent.title.take(18)
                val timeSuffix = if (nextCalendarEvent.timeFormatted.isNotBlank()) " · ${nextCalendarEvent.timeFormatted}" else ""
                EditorialChip(
                    icon = Icons.Outlined.CalendarToday,
                    text = "$title$timeSuffix",
                    accentColor = Color(0xFF3B82F6),
                    isLightMode = isLightMode,
                    onClick = onCalendarClick
                )
            } else {
                EditorialChip(
                    icon = Icons.Outlined.Schedule,
                    text = "Agenda livre",
                    accentColor = Color(0xFF10B981),
                    isLightMode = isLightMode,
                    onClick = onCalendarClick
                )
            }

            // Chip Notas & Tarefas
            if (pendingNotesCount > 0) {
                EditorialChip(
                    icon = Icons.Outlined.Edit,
                    text = "$pendingNotesCount notas",
                    accentColor = Color(0xFF8B5CF6),
                    isLightMode = isLightMode,
                    onClick = onNotesClick
                )
            } else {
                EditorialChip(
                    icon = Icons.Outlined.CheckCircle,
                    text = "Notas em dia",
                    accentColor = Color(0xFF10B981),
                    isLightMode = isLightMode,
                    onClick = onNotesClick
                )
            }
        }

        // 3. Filete divisório editorial em gradiente suave
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (isLightMode) Color(0x301C1917) else Color(0x25FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun EditorialChip(
    icon: ImageVector,
    text: String,
    accentColor: Color,
    isLightMode: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isLightMode) Color(0xFFEFECE2) else Color(0xFF1F222B)
    val border = if (isLightMode) Color(0xFFDDD8CB) else Color(0x25FFFFFF)
    val textColor = if (isLightMode) Color(0xFF1C1917) else Color(0xFFE5E7EB)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(10.dp))
            .tesseraPressable(
                pressedScale = 0.94f,
                onClick = onClick
            )
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(13.5.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
