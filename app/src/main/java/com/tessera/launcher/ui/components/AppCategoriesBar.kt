package com.tessera.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.AppCategory
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.rememberTesseraHaptics

/**
 * Barra horizontal de filtros por categorias na gaveta de aplicativos.
 * Utiliza indicador em pílula deslizante único acelerado por mola e háptica de tick.
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
    val motion = Tessera.motion
    val palette = Tessera.palette
    val pillShape = Tessera.shapes.pill
    val haptics = rememberTesseraHaptics()
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    val hasCustomAccent = accentColor != Color.White && accentColor != Color(0xFF111827) && accentColor != Color(0xFF334155)
    val chipBounds = remember { mutableStateMapOf<String, Pair<Float, Float>>() }
    val selectedBounds = chipBounds[selectedCategory.name]

    val indicatorXState = animateFloatAsState(
        targetValue = selectedBounds?.first ?: 0f,
        animationSpec = motion.springStandard,
        label = "cat_pill_x"
    )
    val indicatorWidthState = animateFloatAsState(
        targetValue = selectedBounds?.second ?: 0f,
        animationSpec = motion.springStandard,
        label = "cat_pill_w"
    )

    LaunchedEffect(selectedCategory, selectedBounds) {
        selectedBounds?.let { (x, w) ->
            val targetScroll = (x - with(density) { 48.dp.toPx() }).toInt().coerceAtLeast(0)
            if (x < scrollState.value || x + w > scrollState.value + scrollState.viewportSize) {
                scrollState.animateScrollTo(targetScroll)
            }
        }
    }

    val pillFill = when {
        hasCustomAccent -> accentColor.copy(alpha = if (palette.isLight) 0.18f else 0.24f)
        palette.isLight -> palette.textPrimary
        else -> palette.surfaceSubtle
    }
    val pillBorder = when {
        hasCustomAccent -> accentColor
        palette.isLight -> palette.textPrimary
        else -> palette.surfaceBorderHighlight
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (leadingContent != null) {
            leadingContent()
        }

        Box(modifier = Modifier.height(32.dp)) {
            if (selectedBounds != null) {
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .graphicsLayer {
                            translationX = indicatorXState.value
                        }
                        .width(with(density) { indicatorWidthState.value.coerceAtLeast(0f).toDp() })
                        .clip(pillShape)
                        .background(pillFill, pillShape)
                        .border(BorderStroke(0.75.dp, pillBorder), pillShape)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    val textColor by animateColorAsState(
                        targetValue = when {
                            isSelected && hasCustomAccent -> if (palette.isLight) accentColor else Color.White
                            isSelected && palette.isLight -> palette.background
                            isSelected -> palette.textPrimary
                            else -> palette.textSecondary
                        },
                        animationSpec = tween(durationMillis = 150, easing = motion.emphasizedEasing),
                        label = "cat_text_${category.name}"
                    )

                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .onGloballyPositioned { coords ->
                                val x = coords.positionInParent().x
                                val w = coords.size.width.toFloat()
                                val existing = chipBounds[category.name]
                                if (existing == null || existing.first != x || existing.second != w) {
                                    chipBounds[category.name] = x to w
                                }
                            }
                            .clip(pillShape)
                            .then(
                                if (!isSelected) {
                                    Modifier
                                        .background(palette.surface.copy(alpha = 0.55f), pillShape)
                                        .border(BorderStroke(0.5.dp, palette.surfaceBorder), pillShape)
                                } else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = palette.ripple),
                                onClick = {
                                    if (!isSelected) {
                                        haptics.tick()
                                        onCategorySelected(category)
                                    }
                                }
                            )
                            .padding(horizontal = 14.dp),
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
}
