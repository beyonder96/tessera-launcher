package com.tessera.launcher.ui.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleMotion
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

@Composable
fun CapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalAppleColors.current.accent,
    contentColor: Color = Color.White,
    paddingValues: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
) {
    val context = LocalContext.current
    val typography = LocalAppleTypography.current
    val motion = LocalAppleMotion.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = motion.interactiveFloat,
        label = "capsule_button_press"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            Taptic.lightImpact(context)
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(AppleShapes.capsule)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.headline.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )
    }
}

@Composable
fun TintedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tintColor: Color = LocalAppleColors.current.accent,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    val context = LocalContext.current
    val typography = LocalAppleTypography.current
    val motion = LocalAppleMotion.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = motion.interactiveFloat,
        label = "tinted_button_press"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            Taptic.lightImpact(context)
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(AppleShapes.capsule)
            .background(tintColor.copy(alpha = 0.16f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.subheadlineBold,
            color = tintColor
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalAppleColors.current.label,
    paddingValues: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
) {
    val context = LocalContext.current
    val typography = LocalAppleTypography.current
    val motion = LocalAppleMotion.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = motion.interactiveFloat,
        label = "glass_button_press"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            Taptic.lightImpact(context)
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .liquidGlass(shape = AppleShapes.capsule, style = GlassStyle.Thin)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.headline.copy(fontWeight = FontWeight.Medium),
            color = contentColor
        )
    }
}
