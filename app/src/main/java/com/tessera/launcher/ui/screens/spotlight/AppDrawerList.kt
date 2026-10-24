package com.tessera.launcher.ui.screens.spotlight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleMotion
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import kotlinx.coroutines.launch

/**
 * Gaveta de Aplicativos Tessera by Apple:
 * - Tipografia grande para nomes de apps (26sp).
 * - Sem ícones fixos por padrão: o ícone squircle surge elegantemente apenas no item focado/pressionado.
 * - Scroller A-Z lateral responsivo com bolha de vidro de grande formato e feedback tátil mecânico.
 */
@Composable
fun AppDrawerList(
    apps: List<AppInfo>,
    letterIndexMap: Map<Char, Int>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current
    val scope = rememberCoroutineScope()

    var activeLetterBubble by remember { mutableStateOf<Char?>(null) }
    val alphabet = remember { ('A'..'Z').toList() }

    Box(modifier = modifier.fillMaxSize()) {
        // Lista Vertical de Aplicativos
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 24.dp, end = 38.dp, top = 8.dp, bottom = 96.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = apps,
                key = { it.packageName }
            ) { app ->
                AppDrawerRow(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }

        // Bolha de Vidro A-Z Indicadora Flutuante
        AnimatedVisibility(
            visible = activeLetterBubble != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            activeLetterBubble?.let { letter ->
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .liquidGlass(shape = ContinuousShape(22.dp), style = GlassStyle.Chrome),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        style = typography.largeTitle.copy(fontWeight = FontWeight.Bold),
                        color = colors.label
                    )
                }
            }
        }

        // Barra Lateral de Letras A-Z
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.75f)
                .width(28.dp)
                .pointerInput(alphabet) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            val letterIndex = ((offset.y / size.height) * alphabet.size)
                                .toInt()
                                .coerceIn(0, alphabet.lastIndex)
                            val letter = alphabet[letterIndex]
                            activeLetterBubble = letter
                            Taptic.selection(context)
                            letterIndexMap[letter]?.let { targetIdx ->
                                scope.launch { listState.scrollToItem(targetIdx) }
                            }
                        },
                        onDragEnd = { activeLetterBubble = null },
                        onDragCancel = { activeLetterBubble = null },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            val letterIndex = ((change.position.y / size.height) * alphabet.size)
                                .toInt()
                                .coerceIn(0, alphabet.lastIndex)
                            val letter = alphabet[letterIndex]
                            if (letter != activeLetterBubble) {
                                activeLetterBubble = letter
                                Taptic.selection(context)
                                letterIndexMap[letter]?.let { targetIdx ->
                                    scope.launch { listState.scrollToItem(targetIdx) }
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                alphabet.forEach { letter ->
                    Text(
                        text = letter.toString(),
                        style = typography.caption2.copy(fontWeight = FontWeight.Bold),
                        color = if (activeLetterBubble == letter) colors.accent else colors.tertiaryLabel,
                        modifier = Modifier.padding(vertical = 0.5.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppDrawerRow(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current
    val motion = LocalAppleMotion.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = motion.interactiveFloat,
        label = "row_scale"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = motion.snappyFloat,
        label = "icon_reveal"
    )

    val iconBitmap = remember(app.packageName, app.icon) {
        app.icon?.toBitmap(80, 80)?.asImageBitmap()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(AppleShapes.button)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    Taptic.lightImpact(context)
                    onClick()
                },
                onLongClick = {
                    Taptic.heavyImpact(context)
                    onLongClick()
                }
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícone squircle que se revela com suavidade ao pressionar
        if (iconBitmap != null && iconAlpha > 0.05f) {
            Box(
                modifier = Modifier
                    .size((34 * iconAlpha).dp)
                    .clip(AppleShapes.icon)
                    .graphicsLayer { alpha = iconAlpha }
            ) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width((12 * iconAlpha).dp))
        }

        Text(
            text = app.label,
            style = typography.drawerApp,
            color = colors.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
