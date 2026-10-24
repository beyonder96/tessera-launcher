package com.tessera.launcher.ui.design.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Widget Oficial de Calendário da Apple (iOS Calendar Widget).
 * Apresenta a data hero em vermelho/acento Apple com número monumental e próximo evento da agenda.
 */
@Composable
fun AppleCalendarWidget(
    nextEvent: CalendarEventInfo?,
    hasPermission: Boolean,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val now = Date()
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.forLanguageTag("pt-BR")).format(now).uppercase()
    val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(now)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(ContinuousShape(22.dp))
            .background(colors.secondaryGrouped)
            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(22.dp))
            .clickable { onCalendarClick() }
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabeçalho Vermelho Apple
            Column {
                Text(
                    text = dayOfWeek,
                    style = typography.caption1.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFF3B30) // Vermelho Apple Calendar
                )
                Text(
                    text = dayOfMonth,
                    style = typography.largeTitle.copy(fontWeight = FontWeight.Normal),
                    color = colors.label
                )
            }

            // Próximo Evento / Status da Agenda
            Column {
                if (!hasPermission) {
                    Text(
                        text = "Toque para autorizar acesso ao Calendário",
                        style = typography.caption1,
                        color = colors.secondaryLabel
                    )
                } else if (nextEvent != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Linha indicadora colorida na lateral
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(28.dp)
                                .clip(ContinuousShape(2.dp))
                                .background(Color(0xFF007AFF))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = nextEvent.title,
                                style = typography.subheadline.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = nextEvent.timeFormatted,
                                style = typography.caption2,
                                color = colors.secondaryLabel
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Nenhum evento restante hoje",
                        style = typography.footnote,
                        color = colors.secondaryLabel
                    )
                }
            }
        }
    }
}
