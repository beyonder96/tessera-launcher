package com.tessera.launcher.ui.design.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.helper.ScreenTimeInfo
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

/**
 * Widget Oficial de Tempo de Uso da Apple (iOS Screen Time Widget).
 * Total de horas diárias com barras categorizadas e gráfico de barras.
 */
@Composable
fun AppleScreenTimeWidget(
    screenTime: ScreenTimeInfo?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val totalMinutes = screenTime?.totalTimeMillis?.let { it / (1000 * 60) } ?: 135
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(ContinuousShape(22.dp))
            .background(colors.secondaryGrouped)
            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(22.dp))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(ContinuousShape(6.dp))
                            .background(Color(0xFF5856D6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tempo de Uso",
                        style = typography.caption1.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.secondaryLabel
                    )
                }
            }

            // Tempo Total Hero
            Column {
                Text(
                    text = "${hours}h ${minutes}m",
                    style = typography.title1.copy(fontWeight = FontWeight.Bold),
                    color = colors.label
                )
                Text(
                    text = "Diário • 12m a menos que ontem",
                    style = typography.caption2,
                    color = Color(0xFF34C759)
                )
            }

            // Barras Coloridas Categorizadas Estilo Apple
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(colors.tertiaryFill)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(8.dp)
                        .background(Color(0xFF007AFF)) // Social
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .height(8.dp)
                        .background(Color(0xFF5856D6)) // Produtividade
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .height(8.dp)
                        .background(Color(0xFFFF9500)) // Entretenimento
                )
            }
        }
    }
}
