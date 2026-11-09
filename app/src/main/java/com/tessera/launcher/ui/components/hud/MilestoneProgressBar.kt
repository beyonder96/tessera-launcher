package com.tessera.launcher.ui.components.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.HudMilestone

/**
 * Barra segmentada elegante com marcos circulares (ex: Comprado -> Enviado -> Em rota -> Entregue).
 */
@Composable
fun MilestoneProgressBar(
    milestones: List<HudMilestone>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (milestones.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Linha com pontos e conectores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            milestones.forEachIndexed { index, milestone ->
                // Ponto circular
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (milestone.reached) accentColor else Color(0xFF2A2A30))
                        .then(
                            if (milestone.reached) {
                                Modifier.border(2.dp, accentColor.copy(alpha = 0.35f), CircleShape)
                            } else Modifier
                        )
                )

                // Linha conectora entre pontos
                if (index < milestones.size - 1) {
                    val nextReached = milestones[index + 1].reached
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.5.dp)
                            .clip(CircleShape)
                            .background(if (nextReached) accentColor else Color(0xFF2A2A30))
                    )
                }
            }
        }

        // Rótulos de cada marco
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            milestones.forEach { milestone ->
                Text(
                    text = milestone.label,
                    fontSize = 10.sp,
                    fontWeight = if (milestone.reached) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (milestone.reached) Color(0xFFEDEDED) else Color(0xFF71717A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
