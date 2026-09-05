package com.tessera.launcher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary

@Composable
fun SearchoFloatingButton(
    isGeminiAnimating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = PillShape,
        color = DarkSurface,
        modifier = modifier
            .size(56.dp)
            .geminiBorder(isAnimating = isGeminiAnimating, shape = PillShape, borderWidth = 2.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Expandir busca Searcho",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
