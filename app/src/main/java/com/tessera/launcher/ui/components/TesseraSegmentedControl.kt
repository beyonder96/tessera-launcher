package com.tessera.launcher.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary

/**
 * Seletor segmentado estilo iOS com transição fluida de pílula deslizante.
 * Suporta perfeitamente Modo Claro (Journal) e Modo Escuro / AMOLED.
 */
@Composable
fun TesseraSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    height: Dp = 44.dp
) {
    if (options.isEmpty()) return

    val containerBg = if (isLightMode) Color(0xFFE8E4D8) else Color(0xFF16161C)
    val containerBorder = if (isLightMode) Color(0xFFDDD8CB) else Color(0xFF262630)

    val pillBg = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF282834)
    val pillBorder = if (isLightMode) Color(0xFFDDD8CB) else Color(0xFF383844)

    val activeTextColor = if (isLightMode) LightTextPrimary else Color.White
    val inactiveTextColor = if (isLightMode) LightTextSecondary else Color(0xFF8E8E93)

    val shape = RoundedCornerShape(14.dp)
    val pillShape = RoundedCornerShape(11.dp)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(containerBg)
            .border(BorderStroke(1.dp, containerBorder), shape)
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val segmentWidth = totalWidth / options.size

        val animatedOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "segmented_indicator_offset"
        )

        // Pílula deslizante ativa
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(elevation = if (isLightMode) 2.dp else 0.dp, shape = pillShape)
                .clip(pillShape)
                .background(pillBg)
                .border(BorderStroke(1.dp, pillBorder), pillShape)
        )

        // Textos das opções
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(pillShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onOptionSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isSelected) activeTextColor else inactiveTextColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
