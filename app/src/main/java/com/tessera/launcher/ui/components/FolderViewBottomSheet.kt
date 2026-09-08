package com.tessera.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.state.AppFolder
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderViewBottomSheet(
    folder: AppFolder,
    allApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
    isAmoledMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val folderApps = remember(folder.packageNames, allApps) {
        folder.packageNames.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
    }

    val monoFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    val containerBg = if (isAmoledMode) AmoledBlack else DarkSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF33333E))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 20.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp)
        ) {
            // Header: Ícone da pasta, Nome, Contagem e Botão Fechar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1B1B22))
                        .border(BorderStroke(1.dp, Color(0xFF282834)), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (folderApps.size == 1) "1 aplicativo" else "${folderApps.size} aplicativos",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF191920))
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (folderApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum aplicativo nesta pasta.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = TextTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(
                        items = folderApps,
                        key = { it.packageName }
                    ) { app ->
                        val imageBitmap = remember(app.icon) {
                            app.icon?.let { drawableToBitmap(it).asImageBitmap() }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        onAppClick(app)
                                        onDismiss()
                                    },
                                    onLongClick = {
                                        onAppLongClick(app)
                                    }
                                )
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1B1B22))
                                    .border(BorderStroke(1.dp, Color(0xFF282834)), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageBitmap != null) {
                                    Image(
                                        bitmap = imageBitmap,
                                        contentDescription = app.label,
                                        colorFilter = monoFilter,
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Text(
                                        text = app.firstLetter.toString(),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
