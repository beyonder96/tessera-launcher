package com.tessera.launcher.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.theme.apple.ContinuousShape

/**
 * Ícone estilizado no padrão Apple Squircle com cantos contínuos e fundo vibrante.
 */
@Composable
fun SquircleIcon(
    imageVector: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    size: Dp = 29.dp,
    iconSize: Dp = 18.dp
) {
    val cornerRadius = size * 0.24f
    Box(
        modifier = modifier
            .size(size)
            .clip(ContinuousShape(cornerRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
