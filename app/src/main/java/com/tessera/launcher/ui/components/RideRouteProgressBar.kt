package com.tessera.launcher.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Barra de progresso de trajeto e rota para corridas de transporte (estilo At-a-Glance / Live Activity).
 * Contém:
 * - Ponto de partida (origem)
 * - Linha de progresso sólida percorrida
 * - Ícone de veículo animado deslizando sobre a linha conforme o progresso avança
 * - Ponto intermediário de embarque (waypoint com anel pulsante)
 * - Linha tracejada indicando o caminho restante até o destino final
 * - Ponto de chegada (alvo com anel concêntrico)
 */
@Composable
fun RideRouteProgressBar(
    progressPercent: Float,
    accentColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    isArrivedOrNear: Boolean = false
) {
    // Animação fluida e amortecida da posição do veículo ao longo da linha
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0.04f, 0.96f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ride_car_progress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val carIconWidthPx = with(LocalDensity.current) { 28.dp.toPx() }
        val carXOffset = (totalWidthPx * animatedProgress) - (carIconWidthPx / 2f)
        val carXOffsetDp = with(LocalDensity.current) { carXOffset.toDp() }

        // Canvas desenha a rota: linha sólida até o carro, linha tracejada no trajeto restante, e os pontos
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            val y = size.height / 2f
            val startX = 14.dp.toPx()
            val endX = size.width - 14.dp.toPx()
            val currentX = (size.width * animatedProgress).coerceIn(startX, endX)
            val waypointX = size.width * 0.52f
            val strokeWidth = 3.2.dp.toPx()

            // 1. Linha sólida percorrida (da origem até o veículo)
            if (currentX > startX) {
                drawLine(
                    color = accentColor,
                    start = Offset(startX, y),
                    end = Offset(currentX, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 2. Linha tracejada para o restante da rota (do veículo até o destino)
            if (currentX < endX) {
                val dashLength = 8.dp.toPx()
                val gapLength = 6.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)

                drawLine(
                    color = trackColor.copy(alpha = 0.55f),
                    start = Offset(currentX, y),
                    end = Offset(endX, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = pathEffect
                )
            }

            // 3. Ponto de Início (Origem)
            drawCircle(
                color = accentColor,
                radius = 3.5.dp.toPx(),
                center = Offset(startX, y)
            )

            // 4. Ponto Intermediário (Waypoint / Embarque)
            if (waypointX > currentX + 16.dp.toPx() || waypointX < currentX - 16.dp.toPx()) {
                // Anel externo translúcido
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
                    radius = 7.dp.toPx(),
                    center = Offset(waypointX, y)
                )
                // Ponto central
                drawCircle(
                    color = accentColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(waypointX, y)
                )
            }

            // 5. Ponto de Destino (Alvo Concêntrico)
            // Anel externo
            drawCircle(
                color = trackColor.copy(alpha = 0.7f),
                radius = 7.dp.toPx(),
                center = Offset(endX, y),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
            )
            // Ponto interno
            drawCircle(
                color = if (isArrivedOrNear) accentColor else trackColor.copy(alpha = 0.85f),
                radius = 3.dp.toPx(),
                center = Offset(endX, y)
            )
        }

        // 6. Miniatura do Veículo sobreposta exatamente na posição atual da rota
        Box(
            modifier = Modifier
                .offset(x = carXOffsetDp.coerceAtLeast(0.dp))
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(width = 1.dp, color = accentColor.copy(alpha = 0.6f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsCar,
                contentDescription = "Posição do carro",
                tint = Color(0xFF1E293B),
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
