package com.tessera.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Modificador universal de física tátil elástica com micro-reflexo luminoso e háptica.
 * Aplicado em botões, cartões, ícones e itens do Feed para garantir que cada toque tenha feedback premium.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.tesseraPressable(
    pressedScale: Float = 0.95f,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    showPressSheen: Boolean = true
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 480f),
        label = "tesseraPressScale"
    )

    val sheenAlpha by animateFloatAsState(
        targetValue = if (isPressed && enabled && showPressSheen) 0.12f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "tesseraPressSheen"
    )

    val baseModifier = this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            drawContent()
            if (sheenAlpha > 0f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = sheenAlpha), Color.Transparent),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.width.coerceAtLeast(size.height)
                    )
                )
            }
        }

    if (onClick != null || onLongClick != null) {
        baseModifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick?.invoke()
            },
            onLongClick = if (onLongClick != null) {
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick.invoke()
                }
            } else null
        )
    } else {
        baseModifier
    }
}

/**
 * Componente que orquestra a entrada escalonada e suave (stagger cascade) dos blocos da página.
 */
@Composable
fun JournalCascadeItem(
    index: Int,
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val delayMs = (index * 35).coerceAtMost(280)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 320, delayMillis = delayMs)) +
                slideInVertically(
                    initialOffsetY = { 24 },
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f)
                ),
        modifier = modifier
    ) {
        content()
    }
}
