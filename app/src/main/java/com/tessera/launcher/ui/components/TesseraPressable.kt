package com.tessera.launcher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.theme.tokens.LateralSpatialMath
import kotlinx.coroutines.delay

/**
 * CompositionLocal que expõe uma lambda estável `() -> Float` com o progresso contínuo (0f..1f)
 * da Tela Lateral (-1). Por ser uma lambda lida exclusivamente dentro do `Modifier.graphicsLayer`,
 * a animação e o arraste ocorrem 100% na GPU sem disparar nenhuma recomposição de Compose!
 */
val LocalLateralPanelProgress = compositionLocalOf<() -> Float> { { 1f } }

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
 * Componente que orquestra a entrada e saída escalonadas (stagger cascade) dos blocos da página.
 * Executado inteiramente via GPU (`Modifier.graphicsLayer`) sem disparar recomposições de Compose,
 * sincronizado em tempo real tanto no arraste 1:1 com o dedo quanto na animação lenta de ~650ms.
 */
@Composable
fun JournalCascadeItem(
    index: Int,
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!visible) return
    val progressProvider = LocalLateralPanelProgress.current
    // Se o item for composto quando o painel já está aberto (ex: posts carregados via rede no Feed),
    // executa um fade suave local; caso contrário, é 100% governado pelo progresso espacial bidirecional.
    val mountedAfterOpen = remember { progressProvider() > 0.96f }
    val mountAnim = remember { Animatable(if (mountedAfterOpen) 0f else 1f) }

    LaunchedEffect(mountedAfterOpen) {
        if (mountedAfterOpen) {
            delay((index.coerceAtMost(8) * 36L))
            mountAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            val spatialProgress = LateralSpatialMath.cascadeItemProgress(progressProvider(), index)
            val effective = spatialProgress * mountAnim.value
            alpha = effective
            translationY = (1f - effective) * 24.dp.toPx()
            translationX = -(1f - effective) * 16.dp.toPx()
            val s = 0.955f + (0.045f * effective)
            scaleX = s
            scaleY = s
        }
    ) {
        content()
    }
}
