package com.tessera.launcher.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.DeliveryStage

/**
 * Barra de progresso visual de 4 etapas para Delivery (iFood, Rappi, etc.):
 * 1. Confirmado
 * 2. Preparando
 * 3. A caminho (Em rota)
 * 4. Entregue
 *
 * Contém linha de progresso animada, círculos de status e ícone de motinho/entrega deslizante.
 */
@Composable
fun DeliveryProgressBar(
    stage: DeliveryStage,
    accentColor: Color,
    trackColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val stageIndex = when (stage) {
        DeliveryStage.CONFIRMED -> 0
        DeliveryStage.PREPARING -> 1
        DeliveryStage.ON_THE_WAY -> 2
        DeliveryStage.DELIVERED -> 3
    }

    // Progresso alvo proporcional aos 4 nós (0.0f, 0.33f, 0.66f, 1.0f)
    val targetFraction = when (stage) {
        DeliveryStage.CONFIRMED -> 0.05f
        DeliveryStage.PREPARING -> 0.35f
        DeliveryStage.ON_THE_WAY -> 0.68f
        DeliveryStage.DELIVERED -> 0.95f
    }

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "delivery_stage_anim"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val bikeIconWidthPx = with(LocalDensity.current) { 28.dp.toPx() }
            val bikeXOffset = (totalWidthPx * animatedFraction) - (bikeIconWidthPx / 2f)
            val bikeXOffsetDp = with(LocalDensity.current) { bikeXOffset.toDp() }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                val y = size.height / 2f
                val startX = 14.dp.toPx()
                val endX = size.width - 14.dp.toPx()
                val currentX = (size.width * animatedFraction).coerceIn(startX, endX)
                val strokeWidth = 3.dp.toPx()

                // 1. Linha inativa total
                drawLine(
                    color = trackColor.copy(alpha = 0.5f),
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                // 2. Linha ativa percorrida
                if (currentX > startX) {
                    drawLine(
                        color = accentColor,
                        start = Offset(startX, y),
                        end = Offset(currentX, y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // 3. Os 4 nós das etapas
                val stepXCoords = floatArrayOf(
                    startX,
                    startX + (endX - startX) * 0.33f,
                    startX + (endX - startX) * 0.66f,
                    endX
                )

                for (i in stepXCoords.indices) {
                    val nodeX = stepXCoords[i]
                    val isPassed = i <= stageIndex

                    drawCircle(
                        color = if (isPassed) accentColor else trackColor,
                        radius = if (isPassed) 4.dp.toPx() else 3.dp.toPx(),
                        center = Offset(nodeX, y)
                    )
                }
            }

            // 4. Ícone deslizante da entrega / motinho sobreposta
            Box(
                modifier = Modifier
                    .offset(x = bikeXOffsetDp.coerceAtLeast(0.dp))
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(width = 1.2.dp, color = accentColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeliveryDining,
                    contentDescription = "Etapa de entrega",
                    tint = accentColor,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // Rótulos das 4 etapas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labels = listOf("Confirmado", "Cozinha", "A caminho", "Entregue")
            labels.forEachIndexed { index, label ->
                val isCurrent = index == stageIndex
                val isCompleted = index < stageIndex

                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isCurrent -> accentColor
                        isCompleted -> textColor
                        else -> textColor.copy(alpha = 0.45f)
                    },
                    textAlign = when (index) {
                        0 -> TextAlign.Start
                        3 -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
