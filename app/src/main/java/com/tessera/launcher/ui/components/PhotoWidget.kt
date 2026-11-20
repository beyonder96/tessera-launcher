package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tessera.launcher.data.helper.MAX_PHOTO_WIDGET_COUNT
import com.tessera.launcher.data.helper.PhotoBitmapCache
import com.tessera.launcher.data.helper.PhotoWidgetGalleryHelper
import com.tessera.launcher.data.helper.loadWidgetThumbFast
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.rememberTesseraHaptics
import com.tessera.launcher.ui.theme.tokens.tesseraGlass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoWidget(
    photoUriString: String?,
    photoUris: List<String> = if (!photoUriString.isNullOrBlank()) listOf(photoUriString) else emptyList(),
    activeIndex: Int = 0,
    onActiveIndexChange: (Int) -> Unit = {},
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit = {},
    onRemovePhotoAt: (Int) -> Unit = { onRemovePhoto() },
    onPhotoClick: () -> Unit = {},
    isLiquidGlass: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberTesseraHaptics()
    val palette = Tessera.palette
    val frameShape = Tessera.shapes.card

    val photos = remember(photoUris, photoUriString) {
        val raw = if (photoUris.isNotEmpty()) photoUris else listOfNotNull(photoUriString)
        PhotoWidgetGalleryHelper.normalizeGallery(raw)
    }
    val safeIndex = remember(photos.size, activeIndex) {
        PhotoWidgetGalleryHelper.clampActiveIndex(activeIndex, photos.size)
    }

    var showOptionsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .sizeIn(maxWidth = 180.dp, maxHeight = 180.dp)
            .fillMaxWidth(0.48f)
            .aspectRatio(1f)
            .tesseraGlass(
                shape = frameShape,
                isLiquidGlass = isLiquidGlass,
                opacityFactor = 0.85f,
                elevation = 8.dp
            )
            .clip(frameShape),
        contentAlignment = Alignment.Center
    ) {
        if (photos.isEmpty()) {
            EmptyPhotoWidgetSlot(
                onClick = {
                    haptics.tap()
                    onPickPhoto()
                }
            )
        } else {
            val pagerState = rememberPagerState(
                initialPage = safeIndex,
                pageCount = { photos.size }
            )

            // Sincroniza índice externo (ex.: seleção pelo modal ou remoção de item)
            LaunchedEffect(safeIndex, photos.size) {
                if (pagerState.currentPage != safeIndex && !pagerState.isScrollInProgress) {
                    pagerState.scrollToPage(safeIndex)
                }
            }

            // Propaga trocas de página feitas pelo usuário com feedback tátil sutil
            LaunchedEffect(pagerState, photos.size) {
                var lastReported = pagerState.currentPage
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    val clamped = PhotoWidgetGalleryHelper.clampActiveIndex(page, photos.size)
                    if (clamped != lastReported) {
                        lastReported = clamped
                        if (pagerState.isScrollInProgress) {
                            haptics.tick()
                        }
                        onActiveIndexChange(clamped)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(photos.size) {
                        detectTapGestures(
                            onTap = {
                                haptics.tap()
                                onPhotoClick()
                            },
                            onLongPress = {
                                haptics.longPress()
                                showOptionsDialog = true
                            }
                        )
                    }
            ) {
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val path = photos[page]
                    PhotoWidgetPage(
                        photoPath = path,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = ((pagerState.currentPage - page) +
                                    pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                                val scale = 1f - 0.06f * pageOffset
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - 0.25f * pageOffset
                            }
                    )
                }

                // Gradiente sutil na base para contraste do indicador de páginas
                if (photos.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        palette.scrim.copy(alpha = 0.48f)
                                    )
                                )
                            )
                    )

                    PhotoPagerIndicators(
                        count = photos.size,
                        currentIndex = pagerState.currentPage,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }

    if (showOptionsDialog && photos.isNotEmpty()) {
        PhotoWidgetOptionsDialog(
            photos = photos,
            activeIndex = safeIndex,
            isLiquidGlass = isLiquidGlass,
            onSelectIndex = { idx ->
                haptics.tick()
                onActiveIndexChange(idx)
            },
            onOpenStandBy = {
                showOptionsDialog = false
                onPhotoClick()
            },
            onPickMorePhotos = {
                showOptionsDialog = false
                onPickPhoto()
            },
            onRemoveCurrent = {
                val targetIndex = safeIndex
                if (photos.size <= 1) {
                    showOptionsDialog = false
                }
                onRemovePhotoAt(targetIndex)
            },
            onRemoveAll = {
                showOptionsDialog = false
                onRemovePhoto()
            },
            onDismiss = { showOptionsDialog = false }
        )
    }
}

@Composable
private fun EmptyPhotoWidgetSlot(
    onClick: () -> Unit
) {
    val palette = Tessera.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(palette.accentMuted)
                .border(1.dp, palette.surfaceBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Adicionar fotos",
                tint = palette.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Adicionar fotos",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = palette.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Até $MAX_PHOTO_WIDGET_COUNT fotos",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PhotoWidgetPage(
    photoPath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = Tessera.palette
    val motion = Tessera.motion

    // Leitura síncrona do cache pré-aquecido no boot -> 0ms no primeiro frame
    val initialCached = remember(photoPath) {
        PhotoBitmapCache.getThumb(photoPath)?.asImageBitmap()
    }

    val imageBitmap by produceState<ImageBitmap?>(
        initialValue = initialCached,
        key1 = photoPath
    ) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                loadWidgetThumbFast(context, photoPath)?.asImageBitmap()
            }
        }
    }

    Box(
        modifier = modifier.background(palette.surfaceSubtle),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = imageBitmap,
            animationSpec = motion.fadeQuick,
            label = "photoWidgetFade"
        ) { bmp ->
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = "Foto do widget",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    palette.surfaceSubtle,
                                    palette.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        tint = palette.textTertiary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoPagerIndicators(
    count: Int,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val palette = Tessera.palette
    val motion = Tessera.motion

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(palette.scrim.copy(alpha = 0.45f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until count) {
            val isSelected = i == currentIndex
            val pillWidth by animateDpAsState(
                targetValue = if (isSelected) 16.dp else 5.dp,
                animationSpec = motion.springQuickDp,
                label = "photoDotWidth_$i"
            )
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .width(pillWidth)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) palette.textPrimary else palette.textPrimary.copy(alpha = 0.4f)
                    )
            )
        }
    }
}

@Composable
private fun PhotoWidgetOptionsDialog(
    photos: List<String>,
    activeIndex: Int,
    isLiquidGlass: Boolean,
    onSelectIndex: (Int) -> Unit,
    onOpenStandBy: () -> Unit,
    onPickMorePhotos: () -> Unit,
    onRemoveCurrent: () -> Unit,
    onRemoveAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val palette = Tessera.palette
    val motion = Tessera.motion
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(motion.fadeQuick) + scaleIn(
                initialScale = 0.92f,
                animationSpec = motion.springQuick
            ),
            exit = fadeOut(motion.fadeQuick) + scaleOut(
                targetScale = 0.94f,
                animationSpec = motion.springQuick
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .tesseraGlass(
                        shape = Tessera.shapes.card,
                        isLiquidGlass = isLiquidGlass,
                        opacityFactor = 0.96f,
                        elevation = 16.dp
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Widget de Foto",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.surfaceSubtle)
                            .border(0.75.dp, palette.surfaceBorder, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${photos.size}/$MAX_PHOTO_WIDGET_COUNT fotos",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = palette.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Faixa horizontal com miniaturas para alternar rapidamente
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photos.forEachIndexed { index, path ->
                        val selected = index == activeIndex
                        val cached = remember(path) {
                            PhotoBitmapCache.getThumb(path)?.asImageBitmap()
                        }
                        val thumb by produceState<ImageBitmap?>(
                            initialValue = cached,
                            key1 = path
                        ) {
                            if (value == null) {
                                value = withContext(Dispatchers.IO) {
                                    loadWidgetThumbFast(context, path)?.asImageBitmap()
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.surfaceSubtle)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) palette.accent else palette.surfaceBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectIndex(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (thumb != null) {
                                Image(
                                    bitmap = thumb!!,
                                    contentDescription = "Foto ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textTertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PhotoOptionRow(
                    icon = Icons.Rounded.Bedtime,
                    label = "Abrir Modo Repouso",
                    tint = palette.textPrimary,
                    onClick = onOpenStandBy
                )

                if (photos.size < MAX_PHOTO_WIDGET_COUNT) {
                    PhotoOptionRow(
                        icon = Icons.Rounded.PhotoLibrary,
                        label = "Adicionar fotos (${photos.size}/$MAX_PHOTO_WIDGET_COUNT)",
                        tint = palette.textPrimary,
                        onClick = onPickMorePhotos
                    )
                }

                PhotoOptionRow(
                    icon = Icons.Rounded.Delete,
                    label = if (photos.size > 1) "Remover foto atual" else "Remover foto",
                    tint = palette.destructive,
                    onClick = onRemoveCurrent
                )

                if (photos.size > 1) {
                    PhotoOptionRow(
                        icon = Icons.Rounded.DeleteSweep,
                        label = "Remover todas",
                        tint = palette.destructive,
                        onClick = onRemoveAll
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = palette.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoOptionRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint
        )
    }
}
