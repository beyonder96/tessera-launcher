package com.tessera.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.motion.IconBoundsHolder
import com.tessera.launcher.ui.motion.captureIconBounds
import com.tessera.launcher.ui.state.AppFolder
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraGlass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.ui.text.style.TextOverflow

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderSearchCard(
    folder: AppFolder,
    allApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onFolderClick: (AppFolder) -> Unit,
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true,
    onAppClickWithBounds: ((AppInfo, Rect?) -> Unit)? = null
) {
    val palette = Tessera.palette
    val cardShape = Tessera.shapes.card
    val tileShape = Tessera.shapes.medium

    val folderApps = remember(folder.packageNames, allApps) {
        folder.packageNames.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
    }

    val monoFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .tesseraGlass(shape = cardShape, isLiquidGlass = isLiquidGlass, elevation = 6.dp)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabeçalho da Pasta (Ícone + Nome + Contagem + Chevron) - Clicável para abrir modal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(tileShape)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onFolderClick(folder) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(tileShape)
                        .background(palette.surfaceSubtle)
                        .border(BorderStroke(0.75.dp, palette.surfaceBorder), tileShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = folder.name,
                        tint = palette.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = if (folderApps.size == 1) "1 aplicativo" else "${folderApps.size} aplicativos",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = palette.textSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Abrir pasta",
                    tint = palette.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Grade/Linha de Apps da pasta (atalho para abrir diretamente)
            if (folderApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    folderApps.take(5).forEach { app ->
                        val imageBitmap = remember(app.icon) {
                            app.icon?.let { drawableToBitmap(it).asImageBitmap() }
                        }
                        val iconBounds = remember(app.packageName) { IconBoundsHolder() }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .captureIconBounds(iconBounds)
                                .clip(tileShape)
                                .background(palette.surfaceSubtle)
                                .border(BorderStroke(0.75.dp, palette.surfaceBorder), tileShape)
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (onAppClickWithBounds != null) onAppClickWithBounds(app, iconBounds.rootRect)
                                        else onAppClick(app)
                                    },
                                    onLongClick = { onAppLongClick(app) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = app.label,
                                    colorFilter = monoFilter,
                                    modifier = Modifier.size(30.dp)
                                )
                            } else {
                                Text(
                                    text = app.firstLetter.toString(),
                                    color = palette.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Se a pasta contiver mais de 5 apps, mostra badge de sobra clicável
                    if (folderApps.size > 5) {
                        val remaining = folderApps.size - 5
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(tileShape)
                                .background(palette.surface)
                                .border(BorderStroke(0.75.dp, palette.surfaceBorder), tileShape)
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onFolderClick(folder) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+$remaining",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
