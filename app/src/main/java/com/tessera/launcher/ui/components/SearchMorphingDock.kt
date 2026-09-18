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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceBorderHover
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
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
    onAiSearchClick: () -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    widgetContent: (@Composable () -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isSplit = searchBarStyle.startsWith("split_")

    val hasTopContent = widgetContent != null
    val dockShape = when (searchBarStyle) {
        "pill", "split_pill" -> if (hasTopContent) RoundedCornerShape(28.dp) else RoundedCornerShape(32.dp)
        "rounded", "split_rounded" -> RoundedCornerShape(22.dp)
        "square", "split_square" -> RoundedCornerShape(12.dp)
        else -> if (hasTopContent) RoundedCornerShape(28.dp) else RoundedCornerShape(32.dp)
    }

    val buttonShape = when (searchBarStyle) {
        "split_pill" -> CircleShape
        "split_rounded" -> RoundedCornerShape(20.dp)
        "split_square" -> RoundedCornerShape(12.dp)
        else -> CircleShape
    }

    // Animação Contínua da Luz Branca Pura Especular de IA (Estilo Ativação Nativa de Celular)
    val infiniteTransition = rememberInfiniteTransition(label = "phone_ai_glow")
    val aiGlowAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_glow_rotation"
    )

    val aiPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_glow_pulse"
    )

    val geminiExpandProgress by animateFloatAsState(
        targetValue = if (isExpanded && isGeminiGlowEnabled) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "gemini_expand_progress"
    )

    val rad = Math.toRadians(aiGlowAngle.toDouble())
    val cosVal = Math.cos(rad).toFloat()
    val sinVal = Math.sin(rad).toFloat()

    val aiBorderBrush = remember(aiGlowAngle, isLightMode) {
        Brush.linearGradient(
            colors = if (isLightMode) com.tessera.launcher.ui.theme.PhoneAiLightGlowColors
                     else com.tessera.launcher.ui.theme.PhoneAiWhiteGlowColors,
            start = Offset(x = 500f * (1f - cosVal), y = 200f * (1f - sinVal)),
            end = Offset(x = 500f * (1f + cosVal), y = 200f * (1f + sinVal))
        )
    }

    val opacityFraction = (searchBarOpacity.coerceIn(0, 100) / 100f)
    val shouldUseLiquidGlass = (isLiquidGlass && !isAmoledMode) || opacityFraction < 0.98f

    // Borda moderna com gradiente sutil de luz física especular
    val modernSpecularBorder = remember(isLightMode, opacityFraction) {
        if (isLightMode) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = (0.80f * opacityFraction).coerceIn(0.20f, 0.95f)),
                    Color(0x22000000).copy(alpha = (0.25f * opacityFraction).coerceIn(0.05f, 0.35f))
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = (0.32f * opacityFraction).coerceIn(0.10f, 0.40f)),
                    Color.White.copy(alpha = (0.06f * opacityFraction).coerceIn(0.02f, 0.12f))
                )
            )
        }
    }

    val dockBorder = if (isGeminiGlowEnabled && isExpanded) {
        BorderStroke(1.5.dp, aiBorderBrush)
    } else if (shouldUseLiquidGlass) {
        if (isLightMode) {
            BorderStroke(1.dp, lightLiquidGlassBorderBrush(opacityFraction))
        } else {
            BorderStroke(1.dp, liquidGlassBorderBrush(opacityFraction))
        }
    } else {
        BorderStroke(1.dp, modernSpecularBorder)
    }

    val dockBgModifier = if (shouldUseLiquidGlass) {
        if (isLightMode) {
            Modifier.background(lightLiquidGlassSurfaceBrush(opacityFraction))
        } else {
            Modifier.background(liquidGlassSurfaceBrush(opacityFraction))
        }
    } else if (isAmoledMode) {
        Modifier.background(
            Brush.verticalGradient(
                listOf(
                    AmoledCardBackground,
                    Color(0xFF070709)
                )
            )
        )
    } else if (isLightMode) {
        Modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF6F7FA)
                )
            )
        )
    } else {
        Modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF16161D),
                    Color(0xFF0F0F14)
                )
            )
        )
    }

    val shadowElevation = (10.dp * opacityFraction)
    val shadowAmbientColor = if (isLightMode) {
        Color(0x14000000)
    } else {
        Color.Black.copy(alpha = 0.25f * opacityFraction)
    }
    val shadowSpotColor = if (opacityFraction < 0.95f) {
        Color.Transparent
    } else {
        if (isLightMode) Color(0x22000000) else Color.Black.copy(alpha = 0.45f)
    }

    val dockTextPrimary = if (isLightMode) {
        if (opacityFraction < 0.35f) Color.White else LightTextPrimary
    } else {
        TextPrimary
    }
    val dockPlaceholderColor = if (isLightMode) {
        if (opacityFraction < 0.35f) Color(0xCCFFFFFF) else Color(0xFF6B7280)
    } else {
        Color(0xFF70707C)
    }
    val dockIconTint = if (isLightMode) {
        if (opacityFraction < 0.35f) Color.White else Color(0xFF374151)
    } else {
        Color(0xFF82828E)
    }
    val dockGearTint = if (isLightMode) {
        if (opacityFraction < 0.35f) Color.White else Color(0xFF374151)
    } else {
        Color(0xFFA0A0AA)
    }
    val dockCursorBrush = SolidColor(if (isLightMode && opacityFraction >= 0.35f) Color.Black else Color.White)

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

    val targetWidth = if (isExpanded) screenWidth - 32.dp else 56.dp
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dock_width"
    )

    val dockInteractionSource = remember { MutableInteractionSource() }
    val isDockPressed by dockInteractionSource.collectIsPressedAsState()
    val dockScale by animateFloatAsState(
        targetValue = if (isDockPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dock_press_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isExpanded) {
            // Estado Recolhido: Cápsula Flutuante Compacta com Feedback de Mola
            Box(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(54.dp)
                    .graphicsLayer {
                        scaleX = dockScale
                        scaleY = dockScale
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
                    .clickable(
                        interactionSource = dockInteractionSource,
                        indication = null,
                        onClick = onExpandClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Brilho especular sutil na borda superior
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(dockShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isLightMode) 0.18f else 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Expandir pesquisa",
                    tint = dockTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Estado Expandido: Doca Moderna com Widgets e Barra de Pesquisa Refinada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                // Card Principal Integrado com Aura Luminosa
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Halo Luminoso Difuso com Física Suave
                    if (isGeminiGlowEnabled && geminiExpandProgress > 0.01f) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer {
                                    alpha = (if (isLightMode) 0.30f else 0.45f) * geminiExpandProgress * aiPulse
                                    scaleX = 1.02f
                                    scaleY = 1.06f
                                }
                                .background(
                                    Brush.radialGradient(
                                        colors = if (isLightMode) {
                                            listOf(
                                                Color(0x3090CAF9),
                                                Color(0x15BBDEFB),
                                                Color(0x05E3F2FD),
                                                Color.Transparent
                                            )
                                        } else {
                                            listOf(
                                                Color(0x40FFFFFF),
                                                Color(0x18FFFFFF),
                                                Color(0x06FFFFFF),
                                                Color.Transparent
                                            )
                                        }
                                    ),
                                    shape = dockShape
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = dockScale
                                scaleY = dockScale
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
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = if (widgetContent != null) 12.dp else 4.dp,
                                bottom = if (widgetContent != null) 6.dp else 4.dp
                            )
                    ) {
                        // Reflexo especular superior do Liquid Design
                        if (shouldUseLiquidGlass && opacityFraction > 0.05f) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(dockShape)
                                    .background(
                                        if (isLightMode) lightLiquidGlassSheenBrush(opacityFraction)
                                        else liquidGlassSheenBrush(opacityFraction)
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (widgetContent != null) {
                                widgetContent()
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(
                                    color = if (isLightMode) {
                                        Color(0xFFE2E8F0).copy(alpha = opacityFraction.coerceAtLeast(0.3f))
                                    } else {
                                        Color.White.copy(alpha = 0.08f * opacityFraction.coerceAtLeast(0.35f))
                                    },
                                    thickness = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            // Linha de Busca Moderna
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Buscar",
                                    tint = dockIconTint,
                                    modifier = Modifier
                                        .size(22.dp)
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
                                    value = searchQuery,
                                    onValueChange = onQueryChange,
                                    textStyle = TextStyle(
                                        color = dockTextPrimary,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    cursorBrush = dockCursorBrush,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            onSearchSubmit(searchQuery)
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
                                                    color = dockPlaceholderColor,
                                                    fontSize = 15.sp,
                                                    fontFamily = FontFamily.SansSerif
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

                                // Ações Dinâmicas da Barra com Animações Fluidas
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    AnimatedVisibility(
                                        visible = searchQuery.isNotEmpty(),
                                        enter = fadeIn(animationSpec = tween(150)) + scaleIn(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        ),
                                        exit = fadeOut(animationSpec = tween(120)) + scaleOut()
                                    ) {
                                        // Botão de Limpar Busca
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isLightMode) Color(0x12000000)
                                                    else Color(0x1FFFFFFF)
                                                )
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
                                        // Ícone de IA para atalho direto quando vazio
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

                // Botão de Engrenagem Separado (Nos modos Split, alinhado à base)
                if (isSplit) {
                    Spacer(modifier = Modifier.width(10.dp))

                    val settingsInteractionSource = remember { MutableInteractionSource() }
                    val isSettingsPressed by settingsInteractionSource.collectIsPressedAsState()
                    val settingsScale by animateFloatAsState(
                        targetValue = if (isSettingsPressed) 0.92f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "settings_press_scale"
                    )

                    Box(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(50.dp)
                            .graphicsLayer {
                                scaleX = settingsScale
                                scaleY = settingsScale
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
                        if (shouldUseLiquidGlass && opacityFraction > 0.05f) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(buttonShape)
                                    .background(
                                        if (isLightMode) lightLiquidGlassSheenBrush(opacityFraction)
                                        else liquidGlassSheenBrush(opacityFraction)
                                    )
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
