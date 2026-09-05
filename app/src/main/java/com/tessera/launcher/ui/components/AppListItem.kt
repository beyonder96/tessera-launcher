package com.tessera.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.IconShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 72
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 72
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Composable
fun AppListItem(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monoColorFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    val imageBitmap: ImageBitmap? = remember(app.icon) {
        app.icon?.let { drawableToBitmap(it).asImageBitmap() }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = TextSecondary),
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = imageBitmap,
                contentDescription = app.label,
                colorFilter = monoColorFilter,
                modifier = Modifier
                    .size(36.dp)
                    .clip(IconShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(IconShape)
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.firstLetter.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = app.label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
