package com.tessera.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.AppCategory
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceBorderHover
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightSurfaceVariant
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary

/**
 * Barra horizontal de filtros por categorias na gaveta de aplicativos.
 * Estritamente minimalista, monocromática e rápida.
 */
@Composable
fun AppCategoriesBar(
    selectedCategory: AppCategory,
    onCategorySelected: (AppCategory) -> Unit,
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    categories: List<AppCategory> = AppCategory.entries
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(categories, key = { it.name }) { category ->
            val isSelected = category == selectedCategory

            val bgColor by animateColorAsState(
                targetValue = when {
                    isSelected && isLightMode -> LightSurfaceVariant
                    isSelected -> DarkSurfaceVariant
                    else -> Color.Transparent
                },
                animationSpec = tween(150),
                label = "cat_bg"
            )

            val borderColor by animateColorAsState(
                targetValue = when {
                    isSelected && isLightMode -> LightCardBorder
                    isSelected -> DarkSurfaceBorderHover
                    isLightMode -> LightCardBorder.copy(alpha = 0.6f)
                    else -> DarkSurfaceBorder
                },
                animationSpec = tween(150),
                label = "cat_border"
            )

            val textColor by animateColorAsState(
                targetValue = when {
                    isSelected && isLightMode -> LightTextPrimary
                    isSelected -> TextPrimary
                    isLightMode -> LightTextSecondary
                    else -> TextSecondary
                },
                animationSpec = tween(150),
                label = "cat_text"
            )

            Surface(
                shape = PillShape,
                color = bgColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .height(32.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onCategorySelected(category) }
                    )
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}
