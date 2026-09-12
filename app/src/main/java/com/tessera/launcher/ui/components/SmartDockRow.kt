package com.tessera.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.IconShape
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextSecondary

private fun safeDrawableToBitmap(drawable: Drawable): Bitmap {
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

/**
 * Smart Dock: Barra minimalista de aplicativos sugeridos contextualmente.
 *
 * Exibe de 4 a 5 apps previstos com base no horário do dia, hábitos de uso e
 * conexão de dispositivos de áudio (fones de ouvido/Bluetooth).
 */
@Composable
fun SmartDockRow(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: ((AppInfo) -> Unit)? = null,
    modifier: Modifier = Modifier,
    iconShape: String = "DEFAULT",
    isThemedIcons: Boolean = true,
    isLightMode: Boolean = false,
    isAmoledMode: Boolean = false
) {
    if (apps.isEmpty()) return

    val surfaceColor = when {
        isLightMode -> LightCardBackground
        isAmoledMode -> AmoledCardBackground
        else -> DarkSurface.copy(alpha = 0.85f)
    }

    val borderColor = when {
        isLightMode -> LightCardBorder
        isAmoledMode -> AmoledCardBorder
        else -> DarkSurfaceBorder
    }

    Surface(
        modifier = modifier.wrapContentWidth(),
        shape = RoundedCornerShape(22.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            apps.forEach { app ->
                SmartDockItem(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = onAppLongClick?.let { { it(app) } },
                    iconShape = iconShape,
                    isThemedIcons = isThemedIcons,
                    isLightMode = isLightMode
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SmartDockItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    iconShape: String,
    isThemedIcons: Boolean,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = remember(iconShape) {
        when (iconShape) {
            "CIRCLE" -> CircleShape
            "CYLINDER" -> RoundedCornerShape(14.dp)
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

    val imageBitmap: ImageBitmap? = remember(app.bitmap, app.icon) {
        app.bitmap?.asImageBitmap() ?: app.icon?.let { safeDrawableToBitmap(it).asImageBitmap() }
    }

    val labelColor = if (isLightMode) LightTextSecondary else TextSecondary

    Column(
        modifier = modifier
            .width(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = if (isLightMode) Color(0x22000000) else Color(0x22FFFFFF)),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val iconModifier = if (iconShape == "LOSANGO") {
            Modifier
                .size(32.dp)
                .rotate(45f)
                .clip(shape)
        } else if (iconShape == "CYLINDER") {
            Modifier
                .width(30.dp)
                .height(38.dp)
                .clip(shape)
        } else {
            Modifier
                .size(38.dp)
                .clip(shape)
        }

        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = app.label,
                modifier = iconModifier,
                colorFilter = colorFilter
            )
        } else {
            Box(
                modifier = iconModifier.background(Color(0xFF222228)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.take(1).uppercase(),
                    color = labelColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = app.label,
            color = labelColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
