package com.tessera.launcher.ui.components.hud

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Superfície estética da Ilha Dinâmica Tessera.
 * Pílula preta sólida profunda com borda nano-fina translúcida e morphing fluido por mola física.
 */
@Composable
fun IslandSurface(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 32.dp else 28.dp,
        animationSpec = Tessera.motion.springSpatialDp,
        label = "island_corner_radius"
    )

    val islandShape = RoundedCornerShape(cornerRadius)
    val islandBlack = Color(0xFF000000)
    val subtleBorder = Color.White.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isExpanded) 18.dp else 10.dp,
                shape = islandShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.7f)
            )
            .clip(islandShape)
            .background(islandBlack)
            .border(width = 0.7.dp, color = subtleBorder, shape = islandShape)
            .tesseraPressFeedback()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .animateContentSize(animationSpec = Tessera.motion.springSpatialIntSize),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
