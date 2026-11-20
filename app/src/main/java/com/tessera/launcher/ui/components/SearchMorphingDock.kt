package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.tessera.launcher.ui.theme.tokens.tesseraGrain
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceBorderHover
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.appleLiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.appleLiquidGlassSheenBrush
import com.tessera.launcher.ui.theme.appleLiquidGlassSurfaceBrush
import com.tessera.launcher.ui.theme.lightLiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.lightLiquidGlassSheenBrush
import com.tessera.launcher.ui.theme.lightLiquidGlassSurfaceBrush
import com.tessera.launcher.ui.theme.liquidGlassBorderBrush
import com.tessera.launcher.ui.theme.liquidGlassSheenBrush
import com.tessera.launcher.ui.theme.liquidGlassSurfaceBrush
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.theme.tokens.Tessera
import java.util.Calendar

@Composable
fun SearchMorphingDock(
    isExpanded: Boolean,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onExpandClick: () -> Unit,
    onOpenSettings: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    searchBarStyle: String = "split_pill",
    searchBarTextType: String = "app_name",
    searchBarCustomText: String = "Tessera...",
    currentTime: String = "",
    isLiquidGlass: Boolean = true,
    isAmoledMode: Boolean = true,
    isLightMode: Boolean = false,
    searchBarOpacity: Int = 100,
    isGeminiGlowEnabled: Boolean = true,
    showAiAssistant: Boolean = false,
    onAiSearchClick: () -> Unit = {},
    onSearchSubmit: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val effectiveGeminiGlow = showAiAssistant && isGeminiGlowEnabled

    val isSplit = searchBarStyle.startsWith("split_")

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = searchQuery,
                selection = TextRange(searchQuery.length)
            )
        )
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = searchQuery,
                selection = TextRange(searchQuery.length)
            )
        }
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            kotlinx.coroutines.delay(40)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        }
    }

    val dockShape = Tessera.shapes.pill
    val buttonShape = Tessera.shapes.full
    val motion = Tessera.motion
    val palette = Tessera.palette

    val opacityFraction = (searchBarOpacity.coerceIn(0, 100) / 100f)
    val shouldUseLiquidGlass = isLiquidGlass || opacityFraction < 0.98f

    val modernSpecularBorder = remember(palette, opacityFraction) {
        Brush.verticalGradient(
            listOf(
                palette.glassStrokeTop.copy(alpha = (palette.glassStrokeTop.alpha * opacityFraction).coerceIn(0.10f, 0.95f)),
                palette.glassStrokeBottom.copy(alpha = (palette.glassStrokeBottom.alpha * opacityFraction).coerceIn(0.03f, 0.35f))
            )
        )
    }

    val dockBorder = if (shouldUseLiquidGlass) {
        BorderStroke(0.75.dp, appleLiquidGlassBorderBrush(isLightMode = palette.isLight, alphaFactor = opacityFraction))
    } else {
        BorderStroke(0.5.dp, modernSpecularBorder)
    }

    val dockBgModifier = if (shouldUseLiquidGlass) {
        Modifier
            .background(
                appleLiquidGlassSurfaceBrush(
                    isLightMode = palette.isLight,
                    isAmoledMode = isAmoledMode,
                    alphaFactor = opacityFraction
                )
            )
            .tesseraGrain(0.025f)
    } else {
        Modifier.background(
            Brush.verticalGradient(
                listOf(palette.surface, palette.background)
            )
        )
    }

    val shadowElevation = (10.dp * opacityFraction)
    val shadowAmbientColor = palette.shadowAmbient.copy(alpha = palette.shadowAmbient.alpha * opacityFraction)
    val shadowSpotColor = if (opacityFraction < 0.95f) Color.Transparent else palette.shadowSpot

    val dockTextPrimary = if (palette.isLight && opacityFraction < 0.35f) Color.White else palette.textPrimary
    val dockPlaceholderColor = if (palette.isLight && opacityFraction < 0.35f) {
        Color.White.copy(alpha = 0.8f)
    } else {
        palette.textSecondary
    }
    val dockIconTint = if (palette.isLight && opacityFraction < 0.35f) Color.White else palette.iconTint
    val dockGearTint = if (palette.isLight && opacityFraction < 0.35f) Color.White else palette.iconTint
    val dockCursorBrush = SolidColor(if (palette.isLight && opacityFraction >= 0.35f) palette.textPrimary else Color.White)

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Bom dia!"
            in 12..17 -> "Boa tarde!"
            else -> "Boa noite!"
        }
    }

    val placeholderText = when (searchBarTextType) {
        "app_name" -> "Pesquisar..."
        "current_time" -> if (currentTime.isNotBlank()) currentTime else "09:41"
        "greeting" -> greeting
        "custom" -> searchBarCustomText.ifBlank { "Pesquisar..." }
        else -> "Pesquisar..."
    }

    val maxExpandedWidth = if (isSplit) screenWidth - 94.dp else screenWidth - 32.dp
    val targetWidth = if (isExpanded) maxExpandedWidth else 56.dp
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = motion.springSpatialDp,
        label = "dock_width"
    )
    val expandProgressState = animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = motion.springSpatial,
        label = "expand_progress"
    )
    val showCollapsedLens by remember { derivedStateOf { expandProgressState.value < 0.25f } }
    val showExpandedContent by remember(isExpanded) {
        derivedStateOf { isExpanded || expandProgressState.value > 0.05f }
    }

    val dockInteractionSource = remember { MutableInteractionSource() }
    val isDockPressed by dockInteractionSource.collectIsPressedAsState()
    val dockScaleState = animateFloatAsState(
        targetValue = if (isDockPressed) 0.94f else 1f,
        animationSpec = motion.springPress,
        label = "dock_press_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Container Central da Doca com Morphing Contínuo e Aura de Blur
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Aura de Blur Óptico Fosco (Backing Frosted Glass Blur)
                Box(
                    modifier = Modifier
                        .width(animatedWidth + 8.dp)
                        .height(58.dp)
                        .graphicsLayer {
                            val s = dockScaleState.value
                            scaleX = s
                            scaleY = s
                        }
                        .blur(radius = 16.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            color = if (isLightMode) {
                                Color.White.copy(alpha = 0.50f * opacityFraction)
                            } else {
                                Color.Black.copy(alpha = 0.75f * opacityFraction)
                            },
                            shape = dockShape
                        )
                )

                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .height(54.dp)
                    .graphicsLayer {
                        val s = dockScaleState.value
                        scaleX = s
                        scaleY = s
                    }
                    .shadow(
                        elevation = shadowElevation,
                        shape = dockShape,
                        ambientColor = shadowAmbientColor,
                        spotColor = shadowSpotColor
                    )
                    .clip(dockShape)
                    .then(dockBgModifier)
                    .border(dockBorder, dockShape)
                    .then(
                        if (!isExpanded) {
                            Modifier.clickable(
                                interactionSource = dockInteractionSource,
                                indication = null,
                                onClick = onExpandClick
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Brilho especular curvado estilo Vidro Líquido Apple (Apple Liquid Glass Curvature Sheen)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(dockShape)
                        .background(
                            if (shouldUseLiquidGlass) {
                                appleLiquidGlassSheenBrush(isLightMode = isLightMode, alphaFactor = opacityFraction)
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isLightMode) 0.22f else 0.08f),
                                        Color.Transparent
                                    )
                                )
                            }
                        )
                )

                // Micro-borda de luz pura especular no topo (Apple Chamfer Rim Light)
                if (shouldUseLiquidGlass) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 10.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = if (isLightMode) 0.50f * opacityFraction else 0.28f * opacityFraction),
                                        Color.White.copy(alpha = if (isLightMode) 0.80f * opacityFraction else 0.45f * opacityFraction),
                                        Color.White.copy(alpha = if (isLightMode) 0.50f * opacityFraction else 0.28f * opacityFraction),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Borda rotativa de IA composta apenas quando a doca está expandida com brilho ativo
                if (effectiveGeminiGlow && isExpanded) {
                    AiGlowBorderOverlay(
                        shape = dockShape,
                        isLightMode = isLightMode,
                        modifier = Modifier.matchParentSize()
                    )
                }

                // 1. Modo Lupa Inativa: Ícone Moderno sem círculo interior redundante
                if (showCollapsedLens) {
                    val lensInteraction = remember { MutableInteractionSource() }
                    val isLensPressed by lensInteraction.collectIsPressedAsState()
                    val lensScaleState = animateFloatAsState(
                        targetValue = if (isLensPressed) 0.90f else 1f,
                        animationSpec = motion.springPress,
                        label = "lens_press_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .graphicsLayer {
                                alpha = 1f - (expandProgressState.value * 4f).coerceIn(0f, 1f)
                                val s = lensScaleState.value
                                scaleX = s
                                scaleY = s
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ModernSearchIcon(
                            tint = if (isLightMode) dockTextPrimary else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 2. Modo Expandido: Linha de Busca Completa com Lupa
                if (showExpandedContent) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                alpha = ((expandProgressState.value - 0.05f) / 0.95f).coerceIn(0f, 1f)
                            }
                    ) {
                        // Lupa Moderna sem cápsula interior
                        ModernSearchIcon(
                            tint = if (shouldUseLiquidGlass) {
                                if (isLightMode) dockTextPrimary else Color.White.copy(alpha = 0.92f)
                            } else dockIconTint,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    interactionSource = dockInteractionSource,
                                    indication = null,
                                    onClick = {
                                        focusRequester.requestFocus()
                                        onExpandClick()
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newTfv ->
                                textFieldValue = newTfv
                                if (newTfv.text != searchQuery) {
                                    onQueryChange(newTfv.text)
                                }
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = dockTextPrimary,
                                fontSize = 16.sp
                            ),
                            cursorBrush = dockCursorBrush,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onSearchSubmit(textFieldValue.text)
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxHeight(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = placeholderText,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = dockPlaceholderColor,
                                                fontSize = 15.sp
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .focusRequester(focusRequester)
                        )

                        // Ações Dinâmicas da Barra
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            AnimatedVisibility(
                                visible = searchQuery.isNotEmpty(),
                                enter = fadeIn(motion.fadeQuick) + scaleIn(animationSpec = motion.springQuick),
                                exit = fadeOut(motion.fadeQuick) + scaleOut(animationSpec = motion.fadeQuick)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(palette.ripple)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { onQueryChange("") }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Limpar busca",
                                        tint = dockTextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (searchQuery.isEmpty()) {
                                if (showAiAssistant) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = onAiSearchClick
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AutoAwesome,
                                            contentDescription = "IA Assistente",
                                            tint = dockIconTint.copy(alpha = 0.75f),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                if (!isSplit) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    keyboardController?.hide()
                                                    focusManager.clearFocus()
                                                    onOpenSettings()
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = "Configurações",
                                            tint = dockGearTint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

            // Botão de Engrenagem Separado nos modos Split
            if (isSplit) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(motion.fadeStandard) + scaleIn(animationSpec = motion.springQuick),
                    exit = fadeOut(motion.fadeQuick) + scaleOut(animationSpec = motion.springQuick)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(10.dp))

                        val settingsInteractionSource = remember { MutableInteractionSource() }
                        val isSettingsPressed by settingsInteractionSource.collectIsPressedAsState()
                        val settingsScaleState = animateFloatAsState(
                            targetValue = if (isSettingsPressed) 0.94f else 1f,
                            animationSpec = motion.springPress,
                            label = "settings_press_scale"
                        )

                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // Aura de Blur Óptico Fosco atrás do botão de engrenagem
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .graphicsLayer {
                                        val s = settingsScaleState.value
                                        scaleX = s
                                        scaleY = s
                                    }
                                    .blur(radius = 12.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                    .background(
                                        color = if (isLightMode) {
                                            Color.White.copy(alpha = 0.50f * opacityFraction)
                                        } else {
                                            Color.Black.copy(alpha = 0.75f * opacityFraction)
                                        },
                                        shape = buttonShape
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                .graphicsLayer {
                                    val s = settingsScaleState.value
                                    scaleX = s
                                    scaleY = s
                                }
                                .shadow(
                                    elevation = shadowElevation,
                                    shape = buttonShape,
                                    ambientColor = shadowAmbientColor,
                                    spotColor = shadowSpotColor
                                )
                                .clip(buttonShape)
                                .then(dockBgModifier)
                                .border(dockBorder, buttonShape)
                                .clickable(
                                    interactionSource = settingsInteractionSource,
                                    indication = null,
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onOpenSettings()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Brilho especular curvado estilo Vidro Líquido Apple
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(buttonShape)
                                    .background(
                                        if (shouldUseLiquidGlass) {
                                            appleLiquidGlassSheenBrush(isLightMode = isLightMode, alphaFactor = opacityFraction)
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = if (isLightMode) 0.22f else 0.08f),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                    )
                            )

                            if (effectiveGeminiGlow && isExpanded) {
                                AiGlowBorderOverlay(
                                    shape = buttonShape,
                                    isLightMode = isLightMode,
                                    modifier = Modifier.matchParentSize()
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Configurações",
                                tint = dockGearTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

/**
 * Borda especular animada para IA composta sob demanda (apenas quando a doca está expandida)
 * e com rotação em GPU via `drawWithCache`, evitando alocação de Brush ou recomposições por frame.
 */
@Composable
private fun AiGlowBorderOverlay(
    shape: Shape,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "phone_ai_glow")
    val angleState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_glow_rotation"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .drawWithCache {
                val colors = if (isLightMode) {
                    com.tessera.launcher.ui.theme.PhoneAiLightGlowColors
                } else {
                    com.tessera.launcher.ui.theme.PhoneAiWhiteGlowColors
                }
                val outline = shape.createOutline(size, layoutDirection, this)
                val stroke = Stroke(width = 0.85.dp.toPx())
                onDrawBehind {
                    val rad = Math.toRadians(angleState.value.toDouble())
                    val cosVal = Math.cos(rad).toFloat()
                    val sinVal = Math.sin(rad).toFloat()
                    val brush = Brush.linearGradient(
                        colors = colors,
                        start = Offset(x = 500f * (1f - cosVal), y = 200f * (1f - sinVal)),
                        end = Offset(x = 500f * (1f + cosVal), y = 200f * (1f + sinVal))
                    )
                    drawOutline(
                        outline = outline,
                        brush = brush,
                        style = stroke
                    )
                }
            }
    )
}

/**
 * Ícone Moderno e Minimalista de Lupa de Busca (Redesenho Geométrico Especular).
 * Possui proporções ópticas refinadas, haste esguia a 45 graus com terminação arredondada,
 * sem círculos interiores ou molduras redundantes.
 */
@Composable
fun ModernSearchIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    strokeWidth: Dp = 1.9.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()

        // Centro óptico da lente (equilibrado para o quadrante superior-esquerdo)
        val centerX = w * 0.42f
        val centerY = h * 0.42f
        val radius = w * 0.27f

        // 1. Lente circular perfeitamente equilibrada e lapidada
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 2. Haste esbelta e elegante a 45 graus com ponta arredondada (estilo iOS Spotlight / SF Symbols)
        val cos45 = 0.70710678f
        val handleStartX = centerX + radius * cos45
        val handleStartY = centerY + radius * cos45
        val handleEndX = w * 0.85f
        val handleEndY = h * 0.85f

        drawLine(
            color = tint,
            start = Offset(handleStartX, handleStartY),
            end = Offset(handleEndX, handleEndY),
            strokeWidth = strokePx * 1.15f,
            cap = StrokeCap.Round
        )
    }
}
