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
    categories: List<AppCategory> = AppCategory.entries,
    accentColor: Color = Color.White,
    leadingContent: (@Composable () -> Unit)? = null
) {
    val hasCustomAccent = accentColor != Color.White && accentColor != Color(0xFF111827)

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingContent != null) {
            item(key = "leading_focus_content") {
                leadingContent()
            }
        }

        items(categories, key = { it.name }) { category ->
            val isSelected = category == selectedCategory

            val bgColor by animateColorAsState(
                targetValue = when {
                    isSelected && hasCustomAccent -> accentColor.copy(alpha = if (isLightMode) 0.16f else 0.24f)
                    isSelected && isLightMode -> Color(0xFF111827)
                    isSelected -> Color(0xFFF2F2F5)
                    isLightMode -> Color(0xFFFFFFFF).copy(alpha = 0.90f)
                    else -> Color(0xFF1C1E26).copy(alpha = 0.85f)
                },
                animationSpec = tween(150),
                label = "cat_bg"
            )

            val borderColor by animateColorAsState(
                targetValue = when {
                    isSelected && hasCustomAccent -> accentColor
                    isSelected && isLightMode -> Color(0xFF111827)
                    isSelected -> Color.White
                    isLightMode -> Color(0xFFD1D5DB)
                    else -> Color.White.copy(alpha = 0.16f)
                },
                animationSpec = tween(150),
                label = "cat_border"
            )

            val textColor by animateColorAsState(
                targetValue = when {
                    isSelected && hasCustomAccent -> if (isLightMode) accentColor else Color.White
                    isSelected && isLightMode -> Color.White
                    isSelected -> Color(0xFF101014)
                    isLightMode -> Color(0xFF374151)
                    else -> Color(0xFFD4D4D8)
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
