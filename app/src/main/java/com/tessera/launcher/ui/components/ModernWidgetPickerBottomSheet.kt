package com.tessera.launcher.ui.components

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppWidgetGroup(
    val packageName: String,
    val appName: String,
    val appIcon: Bitmap?,
    val widgets: List<AppWidgetProviderItem>
)

data class AppWidgetProviderItem(
    val info: AppWidgetProviderInfo,
    val label: String,
    val spanX: Int,
    val spanY: Int,
    val previewBitmap: Bitmap?
)

/**
 * Seletor de Widgets Moderno e Visual (estilo Pixel Launcher / One UI).
 * Apresenta a prévia real do widget (gráfico do widget), dimensões da grade (ex: 4x2)
 * e agrupamento elegante por aplicativo com busca rápida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernWidgetPickerBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelectWidget: (AppWidgetProviderInfo) -> Unit,
    appWidgetManager: AppWidgetManager,
    filterPackage: String? = null,
    isLightMode: Boolean = false,
    isAmoledMode: Boolean = false,
    accentColor: Color = Color(0xFF3B82F6)
) {
    if (!isOpen) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val containerBg = when {
        isLightMode -> LightCardBackground
        isAmoledMode -> AmoledBlack
        else -> DarkSurface
    }
    val cardBg = when {
        isLightMode -> Color(0xFFEDE9DF)
        isAmoledMode -> AmoledCardBackground
        else -> Color(0xFF191922)
    }
    val cardBorder = when {
        isLightMode -> LightCardBorder
        isAmoledMode -> AmoledCardBorder
        else -> DarkSurfaceBorder
    }
    val primaryText = if (isLightMode) Color(0xFF1C1917) else Color.White
    val secondaryText = if (isLightMode) Color(0xFF78716C) else Color(0xFF94A3B8)
    val inputBg = if (isLightMode) Color(0xFFE2DDD2) else Color(0xFF22222E)

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val allGroups = remember { mutableStateListOf<AppWidgetGroup>() }
    val expandedPackages = remember { mutableStateListOf<String>() }

    // Carrega o catálogo de widgets em background com previews reais
    LaunchedEffect(filterPackage) {
        isLoading = true
        allGroups.clear()
        expandedPackages.clear()

        val loaded = withContext(Dispatchers.IO) {
            loadInstalledWidgetCatalog(context, appWidgetManager, filterPackage)
        }
        allGroups.addAll(loaded)
        if (filterPackage != null && loaded.isNotEmpty()) {
            expandedPackages.add(loaded.first().packageName)
        }
        isLoading = false
    }

    // Filtragem em tempo real pela barra de pesquisa
    val filteredGroups = remember(searchQuery, allGroups.toList()) {
        if (searchQuery.isBlank()) {
            allGroups.toList()
        } else {
            val query = searchQuery.trim().lowercase()
            allGroups.mapNotNull { group ->
                val appMatches = group.appName.lowercase().contains(query)
                val matchingWidgets = group.widgets.filter {
                    it.label.lowercase().contains(query) || appMatches
                }
                if (matchingWidgets.isNotEmpty()) {
                    group.copy(widgets = matchingWidgets)
                } else null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Drag handle visual
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(secondaryText.copy(alpha = 0.35f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cabeçalho e Barra de Pesquisa
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Widgets,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (filterPackage != null) "Vincular Widget" else "Widgets",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Toque no widget para adicionar ao launcher",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campo de Busca Rápida
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(inputBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        tint = secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = primaryText,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(accentColor),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Buscar widgets ou apps...",
                                    color = secondaryText,
                                    fontSize = 13.5.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Limpar",
                                tint = secondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Conteúdo: Carregamento, Lista Vazia ou Catálogo
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = accentColor,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Carregando miniaturas de widgets...",
                            fontSize = 13.sp,
                            color = secondaryText
                        )
                    }
                }
            } else if (filteredGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum widget encontrado",
                        fontSize = 14.sp,
                        color = secondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(
                        items = filteredGroups,
                        key = { it.packageName }
                    ) { group ->
                        val isExpanded = searchQuery.isNotBlank() || expandedPackages.contains(group.packageName)
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            label = "chevron_rotate"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                        ) {
                            // Cabeçalho do App (Toque para expandir os widgets do app)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (searchQuery.isBlank()) {
                                            if (expandedPackages.contains(group.packageName)) {
                                                expandedPackages.remove(group.packageName)
                                            } else {
                                                expandedPackages.add(group.packageName)
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (group.appIcon != null) {
                                    Image(
                                        bitmap = group.appIcon.asImageBitmap(),
                                        contentDescription = group.appName,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(accentColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = group.appName.take(1).uppercase(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = group.appName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${group.widgets.size} ${if (group.widgets.size == 1) "widget disponível" else "widgets disponíveis"}",
                                        fontSize = 11.5.sp,
                                        color = secondaryText
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Expandir",
                                    tint = secondaryText,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(rotationAngle)
                                )
                            }

                            // Lista de Widgets Revelada com Miniatura Visual Real
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    group.widgets.forEach { widgetItem ->
                                        WidgetPreviewCard(
                                            item = widgetItem,
                                            appIcon = group.appIcon,
                                            appName = group.appName,
                                            primaryText = primaryText,
                                            secondaryText = secondaryText,
                                            cardBorder = cardBorder,
                                            accentColor = accentColor,
                                            onClick = {
                                                onDismiss()
                                                onSelectWidget(widgetItem.info)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card individual com a prévia visual real do widget e dimensões.
 */
@Composable
private fun WidgetPreviewCard(
    item: AppWidgetProviderItem,
    appIcon: Bitmap?,
    appName: String,
    primaryText: Color,
    secondaryText: Color,
    cardBorder: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Título e Chip de Dimensão do Widget (ex: "4 × 2")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${item.spanX} × ${item.spanY}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Imagem da Prévia Real do Widget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp, max = 180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.12f))
                    .border(0.5.dp, cardBorder.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.previewBitmap != null) {
                    Image(
                        bitmap = item.previewBitmap.asImageBitmap(),
                        contentDescription = item.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Fallback elegante quando o app não fornece previewDrawable nativo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        if (appIcon != null) {
                            Image(
                                bitmap = appIcon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Widgets,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${item.spanX} × ${item.spanY} • $appName",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão Adicionar Sutil
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Adicionar",
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Adicionar",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }
        }
    }
}

/**
 * Consulta e agrupa todos os widgets instalados no aparelho por aplicativo.
 */
private fun loadInstalledWidgetCatalog(
    context: Context,
    appWidgetManager: AppWidgetManager,
    filterPackage: String? = null
): List<AppWidgetGroup> {
    val pm = context.packageManager
    val providers = try {
        appWidgetManager.installedProviders
    } catch (_: Exception) {
        emptyList()
    }

    val groupedByPackage = providers.groupBy { it.provider.packageName }
    val result = mutableListOf<AppWidgetGroup>()

    for ((pkg, list) in groupedByPackage) {
        if (filterPackage != null && pkg != filterPackage) continue

        val appInfo = try {
            pm.getApplicationInfo(pkg, 0)
        } catch (_: Exception) {
            null
        } ?: continue

        val appName = pm.getApplicationLabel(appInfo).toString()
        val appIconDrawable = try {
            pm.getApplicationIcon(appInfo)
        } catch (_: Exception) {
            null
        }
        val appIconBitmap = rasterizeDrawableToBitmap(appIconDrawable, 120, 120)

        val widgetItems = list.map { providerInfo ->
            val label = try {
                providerInfo.loadLabel(pm)
            } catch (_: Exception) {
                providerInfo.label
            } ?: appName

            val span = calculateWidgetGridSpan(providerInfo)

            // Carrega a imagem real de preview fornecida pelo app
            val previewDrawable = try {
                providerInfo.loadPreviewImage(context, 0)
            } catch (_: Exception) {
                null
            } ?: try {
                providerInfo.loadIcon(context, 0)
            } catch (_: Exception) {
                null
            }

            val previewBmp = rasterizeDrawableToBitmap(previewDrawable, 480, 320)

            AppWidgetProviderItem(
                info = providerInfo,
                label = label,
                spanX = span.first,
                spanY = span.second,
                previewBitmap = previewBmp
            )
        }.sortedBy { it.label }

        result.add(
            AppWidgetGroup(
                packageName = pkg,
                appName = appName,
                appIcon = appIconBitmap,
                widgets = widgetItems
            )
        )
    }

    return result.sortedBy { it.appName.lowercase() }
}

/**
 * Calcula dimensões X e Y da grade baseadas nas especificações do Android AOSP.
 */
private fun calculateWidgetGridSpan(providerInfo: AppWidgetProviderInfo): Pair<Int, Int> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (providerInfo.targetCellWidth > 0 && providerInfo.targetCellHeight > 0) {
            return Pair(providerInfo.targetCellWidth, providerInfo.targetCellHeight)
        }
    }
    val minWidthDp = providerInfo.minWidth
    val minHeightDp = providerInfo.minHeight
    val spanX = ((minWidthDp + 30) / 70).coerceIn(1, 6)
    val spanY = ((minHeightDp + 30) / 70).coerceIn(1, 6)
    return Pair(spanX, spanY)
}

/**
 * Rasteriza qualquer Drawable para Bitmap com dimensões controladas.
 */
private fun rasterizeDrawableToBitmap(drawable: Drawable?, maxWidth: Int, maxHeight: Int): Bitmap? {
    if (drawable == null) return null
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        val bmp = drawable.bitmap
        if (bmp.width <= maxWidth && bmp.height <= maxHeight) return bmp
        val aspect = bmp.width.toFloat() / bmp.height.toFloat()
        val targetW = if (aspect >= 1f) maxWidth else (maxHeight * aspect).toInt()
        val targetH = if (aspect >= 1f) (maxWidth / aspect).toInt() else maxHeight
        return Bitmap.createScaledBitmap(bmp, targetW.coerceAtLeast(1), targetH.coerceAtLeast(1), true)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
        return try {
            val bmp = Bitmap.createBitmap(maxWidth.coerceAtMost(144), maxHeight.coerceAtMost(144), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.background?.let {
                it.setBounds(0, 0, bmp.width, bmp.height)
                it.draw(canvas)
            }
            drawable.foreground?.let {
                it.setBounds(0, 0, bmp.width, bmp.height)
                it.draw(canvas)
            }
            bmp
        } catch (_: Throwable) {
            null
        }
    }
    return try {
        val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(maxWidth) else maxWidth
        val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(maxHeight) else (maxWidth * 2 / 3)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        bmp
    } catch (_: Throwable) {
        null
    }
}
