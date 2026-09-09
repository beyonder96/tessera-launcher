package com.tessera.launcher.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Brush
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
fun SearchoMorphingDock(
    isExpanded: Boolean,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onExpandClick: () -> Unit,
    onOpenSettings: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    searchBarStyle: String = "split_pill",
    searchBarTextType: String = "app_name",
    searchBarCustomText: String = "Searcho...",
    currentTime: String = "",
    isLiquidGlass: Boolean = true,
    isAmoledMode: Boolean = true,
    isLightMode: Boolean = false,
    searchBarOpacity: Int = 100,
    widgetContent: (@Composable () -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isSplit = searchBarStyle.startsWith("split_")

    val dockShape = when (searchBarStyle) {
        "pill", "split_pill" -> if (widgetContent != null) RoundedCornerShape(26.dp) else PillShape
        "rounded", "split_rounded" -> RoundedCornerShape(20.dp)
        "square", "split_square" -> RoundedCornerShape(8.dp)
        else -> if (widgetContent != null) RoundedCornerShape(26.dp) else PillShape
    }

    val buttonShape = when (searchBarStyle) {
        "split_pill" -> CircleShape
        "split_rounded" -> RoundedCornerShape(18.dp)
        "split_square" -> RoundedCornerShape(8.dp)
        else -> CircleShape
    }

    val opacityFraction = (searchBarOpacity.coerceIn(0, 100) / 100f)
    // Se opacidade for ajustada (< 1.0f) ou liquid glass ativado, aplica acabamento de vidro líquido translúcido
    val shouldUseLiquidGlass = (isLiquidGlass && !isAmoledMode) || opacityFraction < 0.98f

    val dockBorder = if (shouldUseLiquidGlass) {
        if (isLightMode) {
            BorderStroke(1.dp, lightLiquidGlassBorderBrush(opacityFraction))
        } else {
            BorderStroke(1.dp, liquidGlassBorderBrush(opacityFraction))
        }
    } else {
        val borderColor = when {
            isLightMode -> LightCardBorder
            isAmoledMode -> AmoledCardBorder
            isExpanded -> DarkSurfaceBorderHover
            else -> DarkSurfaceBorder
        }
        BorderStroke(1.dp, borderColor)
    }

    val dockBgModifier = if (shouldUseLiquidGlass) {
        if (isLightMode) {
            Modifier.background(lightLiquidGlassSurfaceBrush(opacityFraction))
        } else {
            Modifier.background(liquidGlassSurfaceBrush(opacityFraction))
        }
    } else if (isAmoledMode) {
        Modifier.background(AmoledCardBackground)
    } else if (isLightMode) {
        Modifier.background(LightCardBackground)
    } else {
        Modifier.background(DarkSurface)
    }

    // Sombra sutil que escala com a opacidade; spotColor é removido quando translúcido para eliminar o halo preto
    val shadowElevation = (8.dp * opacityFraction)
    val shadowAmbientColor = if (isLightMode) {
        Color.Black.copy(alpha = 0.05f * opacityFraction)
    } else {
        Color.Black.copy(alpha = 0.12f * opacityFraction)
    }
    val shadowSpotColor = if (opacityFraction < 0.95f) {
        Color.Transparent
    } else {
        if (isLightMode) Color(0x18000000) else Color.Black.copy(alpha = 0.35f)
    }

    // Cores adaptativas de texto e ícones com base na opacidade e no tema (WCAG AA)
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
        "app_name" -> "Searcho..."
        "current_time" -> if (currentTime.isNotBlank()) currentTime else "09:41"
        "greeting" -> greeting
        "custom" -> searchBarCustomText.ifBlank { "Tessera..." }
        else -> "Searcho..."
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isExpanded) {
            // Estado Recolhido: Círculo ou pílula compacta
            Box(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(56.dp)
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
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onExpandClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Expandir pesquisa",
                    tint = dockTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Estado Expandido: Doca Unificada com Widgets integrados no topo + Botão de Configurações alinhado à base
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                // Card Principal Integrado (Widget no topo + Busca na base)
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                            top = if (widgetContent != null) 14.dp else 4.dp,
                            bottom = if (widgetContent != null) 4.dp else 4.dp
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

                        // Linha de Busca
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Buscar",
                                tint = dockIconTint,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            focusRequester.requestFocus()
                                            onExpandClick()
                                        }
                                    )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

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
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = placeholderText,
                                                color = dockPlaceholderColor,
                                                fontSize = 16.sp,
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

                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Limpar busca",
                                    tint = dockPlaceholderColor,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { onQueryChange("") }
                                        )
                                )
                            } else if (!isSplit) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Configurações",
                                    tint = dockGearTint,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                                onOpenSettings()
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                // Botão de Engrenagem Separado (Nos modos Split, alinhado à base)
                if (isSplit) {
                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(48.dp)
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
                        // Brilho especular do botão
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
