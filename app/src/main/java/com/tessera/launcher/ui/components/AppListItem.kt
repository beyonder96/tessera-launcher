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
    isRightAligned: Boolean = false
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 550f),
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
            "SQUIRCLE" -> RoundedCornerShape(14.dp)
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

    val imageBitmap: ImageBitmap? = remember(app.bitmap, app.icon) {
        app.bitmap?.asImageBitmap() ?: app.icon?.let { drawableToBitmap(it).asImageBitmap() }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(swipeModifier)
            .graphicsLayer { translationX = animatedOffsetX }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(0x22FFFFFF)),
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

            if (imageBitmap != null) {
                Box(
                    modifier = if (iconShape == "CYLINDER") Modifier.size(42.dp) else Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap,
                        contentDescription = app.label,
                        colorFilter = colorFilter,
                        modifier = iconModifier
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(shape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.firstLetter.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        val textComposable: @Composable (Modifier) -> Unit = { textMod ->
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (isRightAligned) TextAlign.End else TextAlign.Start,
                modifier = textMod
            )
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
