package com.tessera.launcher.ui.design.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleMotion
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

/**
 * Widget Oficial de Bateria da Apple (iOS Battery Widget).
 * Apresenta o anel circular de carga animado com cor semântica (Verde > 20%, Amarelo modo econômico, Vermelho < 20%)
 * e porcentagem hero com números tabulares.
 */
@Composable
fun AppleBatteryWidget(
    batteryPercentage: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
    deviceName: String = "Tessera"
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current
    val motion = LocalAppleMotion.current

    val animatedProgress by animateFloatAsState(
        targetValue = batteryPercentage / 100f,
        animationSpec = motion.smoothFloat,
        label = "battery_ring_progress"
    )

    val ringColor = when {
        isCharging -> Color(0xFF34C759) // Verde carregando
        batteryPercentage <= 20 -> Color(0xFFFF3B30) // Vermelho crítico
        batteryPercentage <= 35 -> Color(0xFFFFCC00) // Amarelo alerta
        else -> Color(0xFF34C759) // Verde normal
    }

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
            // Topo: Ícone e Nome do Dispositivo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize().weight(1f)
            ) {
                // Anel de Carga Circular Estilo Apple Watch / iOS
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = colors.tertiaryFill
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        // Trilho de fundo
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = strokeWidth)
                        )
                        // Arco de progresso
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Icon(
                        imageVector = if (isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                        contentDescription = null,
                        tint = ringColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Informações textuais à direita
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "$batteryPercentage%",
                        style = typography.title1.copy(fontWeight = FontWeight.Bold),
                        color = colors.label
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isCharging) "Carregando" else "Nível da Bateria",
                        style = typography.footnote,
                        color = colors.secondaryLabel
                    )
                    Text(
                        text = deviceName,
                        style = typography.caption2,
                        color = colors.tertiaryLabel
                    )
                }
            }
        }
    }
}
