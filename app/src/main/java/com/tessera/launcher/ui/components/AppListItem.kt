package com.tessera.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.IconShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.tokens.Tessera

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 80
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 80
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    iconShape: String = "DEFAULT",
    isThemedIcons: Boolean = false,
    isHideAppLabels: Boolean = false,
    isRightAligned: Boolean = false,
    hasLinkedWidget: Boolean = false,
    notificationCount: Int = 0,
    accentColor: Color = Color(0xFF3B82F6),
    isLightMode: Boolean = false
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "item_swipe_offset"
    )
    val haptic = LocalHapticFeedback.current
    var hasHapticTriggered by remember { mutableStateOf(false) }

    val swipeModifier = if (onSwipeRight != null) {
        Modifier.pointerInput(app.packageName) {
            detectHorizontalDragGestures(
                onDragStart = {
                    hasHapticTriggered = false
                },
                onDragEnd = {
                    if (dragOffsetX >= 75f) {
                        onSwipeRight()
                    }
                    dragOffsetX = 0f
                    hasHapticTriggered = false
                },
                onDragCancel = {
                    dragOffsetX = 0f
                    hasHapticTriggered = false
                },
                onHorizontalDrag = { change, dragAmount ->
                    if (dragAmount > 0 || dragOffsetX > 0f) {
                        val next = (dragOffsetX + dragAmount).coerceIn(0f, 130f)
                        dragOffsetX = next
                        if (next >= 75f && !hasHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasHapticTriggered = true
                        }
                        change.consume()
                    }
                }
            )
        }
    } else Modifier

    val shape = remember(iconShape) {
        when (iconShape) {
            "CIRCLE" -> CircleShape
            "CYLINDER" -> RoundedCornerShape(16.dp)
            "LOSANGO" -> RoundedCornerShape(6.dp)
            "SQUIRCLE" -> RoundedCornerShape(12.dp)
            "SQUARE" -> RoundedCornerShape(4.dp)
            else -> IconShape
        }
    }

    val colorFilter = remember(isThemedIcons) {
        if (isThemedIcons) {
            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        } else {
            null
        }
    }

    val itemInteractionSource = remember { MutableInteractionSource() }
    val isItemPressed by itemInteractionSource.collectIsPressedAsState()
    val itemScale by animateFloatAsState(
        targetValue = if (isItemPressed) 0.97f else 1f,
        animationSpec = Tessera.motion.springPress,
        label = "app_item_press_scale"
    )

    val imageBitmap: ImageBitmap? = remember(app.bitmap, app.icon) {
        app.bitmap?.asImageBitmap() ?: app.icon?.let { drawableToBitmap(it).asImageBitmap() }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(swipeModifier)
            .graphicsLayer {
                translationX = animatedOffsetX
                scaleX = itemScale
                scaleY = itemScale
            }
            .combinedClickable(
                interactionSource = itemInteractionSource,
                indication = ripple(color = Tessera.palette.ripple),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                start = 24.dp,
                end = if (isRightAligned && isHideAppLabels) 36.dp else 24.dp,
                top = if (isHideAppLabels) 10.dp else 7.dp,
                bottom = if (isHideAppLabels) 10.dp else 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isRightAligned) Arrangement.End else Arrangement.Start
    ) {
        val iconComposable: @Composable () -> Unit = {
            val iconModifier = if (iconShape == "LOSANGO") {
                Modifier
                    .size(34.dp)
                    .rotate(45f)
                    .clip(shape)
            } else if (iconShape == "CYLINDER") {
                Modifier
                    .width(32.dp)
                    .height(42.dp)
                    .clip(shape)
            } else {
                Modifier
                    .size(40.dp)
                    .clip(shape)
            }

            val iconContainerModifier = if (iconShape == "CYLINDER") Modifier.size(42.dp) else Modifier.size(40.dp)
            Box(
                modifier = iconContainerModifier,
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap,
                        contentDescription = app.label,
                        colorFilter = colorFilter,
                        modifier = iconModifier
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(shape)
                            .background(Tessera.palette.surfaceSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.firstLetter.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = Tessera.palette.textSecondary
                        )
                    }
                }

                if (notificationCount > 0) {
                    NotificationBadge(
                        count = notificationCount,
                        accentColor = accentColor,
                        isLightMode = isLightMode,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }

        val textComposable: @Composable (Modifier) -> Unit = { textMod ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isRightAligned) Arrangement.End else Arrangement.Start,
                modifier = textMod
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    ),
                    color = Tessera.palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (app.isShortcut) Modifier.weight(1f, fill = false) else Modifier
                )
                if (app.isShortcut) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PWA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Tessera.palette.textSecondary,
                        modifier = Modifier
                            .background(Tessera.palette.surfaceSubtle, Tessera.shapes.micro)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                if (hasLinkedWidget) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.Widgets,
                        contentDescription = "Widget disponível",
                        tint = Tessera.palette.textSecondary.copy(alpha = 0.55f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        if (isRightAligned) {
            if (!isHideAppLabels) {
                textComposable(Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
            }
            iconComposable()
        } else {
            iconComposable()
            if (!isHideAppLabels) {
                Spacer(modifier = Modifier.width(16.dp))
                textComposable(Modifier.weight(1f))
            }
        }
    }
}
