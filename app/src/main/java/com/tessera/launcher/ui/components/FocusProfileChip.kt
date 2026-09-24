package com.tessera.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.FocusProfile
import com.tessera.launcher.ui.theme.PillShape

/**
 * Chip minimalista de status e alternância de Perfil de Foco.
 * Permite alternar rapidamente entre Padrão, Trabalho e Desconexão na gaveta de aplicativos.
 */
@Composable
fun FocusProfileChip(
    currentProfile: FocusProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    accentColor: Color = Color.White
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "focus_chip_scale"
    )

    val isActive = currentProfile != FocusProfile.OFF
    val effectiveAccent = if (accentColor != Color.White && accentColor != Color(0xFF111827)) accentColor else Color.White

    val icon = when (currentProfile) {
        FocusProfile.WORK -> Icons.Outlined.WorkOutline
        FocusProfile.MINDFUL -> Icons.Outlined.SelfImprovement
        FocusProfile.OFF -> Icons.Outlined.GridView
    }

    val label = when (currentProfile) {
        FocusProfile.WORK -> "Trabalho"
        FocusProfile.MINDFUL -> "Desconexão"
        FocusProfile.OFF -> "Foco"
    }

    val bgColor by animateColorAsState(
        targetValue = when {
            isActive && isLightMode -> effectiveAccent.copy(alpha = 0.16f)
            isActive -> effectiveAccent.copy(alpha = 0.22f)
            isLightMode -> Color(0xFFFFFFFF).copy(alpha = 0.90f)
            else -> Color(0xFF1C1E26).copy(alpha = 0.85f)
        },
        animationSpec = tween(160),
        label = "focus_chip_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> effectiveAccent.copy(alpha = 0.80f)
            isLightMode -> Color(0xFFD1D5DB)
            else -> Color.White.copy(alpha = 0.16f)
        },
        animationSpec = tween(160),
        label = "focus_chip_border"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isActive && isLightMode -> effectiveAccent
            isActive -> effectiveAccent
            isLightMode -> Color(0xFF374151)
            else -> Color(0xFFD4D4D8)
        },
        animationSpec = tween(160),
        label = "focus_chip_content"
    )

    Surface(
        shape = PillShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(32.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )

            if (isActive) {
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(effectiveAccent, shape = CircleShape)
                )
            }
        }
    }
}
