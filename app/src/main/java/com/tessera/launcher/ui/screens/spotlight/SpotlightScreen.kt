package com.tessera.launcher.ui.screens.spotlight

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.tessera.launcher.data.helper.ContactInfo
import com.tessera.launcher.data.helper.OmniResult
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.design.components.GroupedRow
import com.tessera.launcher.ui.design.components.GroupedSection
import com.tessera.launcher.ui.design.components.SquircleIcon
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

/**
 * Spotlight Search oficial do Tessera 3.0.
 * Experiência unificada inspirada no Spotlight do iOS 26 com barra inferior
 * responsiva acima do teclado e seções categorizadas com Apple Intelligence.
 */
@Composable
fun SpotlightScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    matchingApps: List<AppInfo>,
    matchingContacts: List<ContactInfo>,
    omniResult: OmniResult?,
    calculatorResult: String?,
    aiResponse: String?,
    isAiLoading: Boolean,
    onAppClick: (AppInfo) -> Unit,
    onContactClick: (ContactInfo) -> Unit,
    onAskAi: (String) -> Unit,
    onWebSearchClick: (String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Borda luminosa multicolorida animada para o card de IA (Apple Intelligence)
    val infiniteTransition = rememberInfiniteTransition(label = "apple_intelligence")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "gradient_shift"
    )

    val intelligenceBrush = remember(gradientShift) {
        Brush.sweepGradient(
            colors = listOf(
                Color(0xFF007AFF),
                Color(0xFF5856D6),
                Color(0xFFAF52DE),
                Color(0xFFFF2D55),
                Color(0xFFFF9500),
                Color(0xFF007AFF)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        // Lista de Resultados do Spotlight
        LazyColumn(
            contentPadding = PaddingValues(
                top = statusBarTop + 16.dp,
                bottom = imeBottomPadding + 74.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            // Cartão de Resposta da IA (Apple Intelligence)
            if (isAiLoading || !aiResponse.isNullOrBlank() || query.startsWith("@ai", ignoreCase = true)) {
                item(key = "spotlight_ai_card") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(AppleShapes.card)
                            .border(1.5.dp, intelligenceBrush, AppleShapes.card)
                            .background(colors.secondaryGrouped)
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFAF52DE),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Apple Intelligence",
                                    style = typography.headline,
                                    color = colors.label
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isAiLoading) "Pensando..." else (aiResponse ?: "Toque para pesquisar com IA"),
                                style = typography.body,
                                color = if (isAiLoading) colors.secondaryLabel else colors.label
                            )
                        }
                    }
                }
            }

            // Melhor Resultado (Top Hit)
            if (matchingApps.isNotEmpty()) {
                val topApp = matchingApps.first()
                item(key = "spotlight_top_hit") {
                    GroupedSection(header = "Melhor Resultado") {
                        val iconBitmap = remember(topApp.packageName, topApp.icon) {
                            topApp.icon?.toBitmap(96, 96)?.asImageBitmap()
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Taptic.lightImpact(context)
                                    onAppClick(topApp)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (iconBitmap != null) {
                                Image(
                                    bitmap = iconBitmap,
                                    contentDescription = topApp.label,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(AppleShapes.icon)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(AppleShapes.icon)
                                        .background(colors.fill)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = topApp.label,
                                    style = typography.title3,
                                    color = colors.label
                                )
                                Text(
                                    text = "Aplicativo",
                                    style = typography.footnote,
                                    color = colors.secondaryLabel
                                )
                            }
                        }
                    }
                }
            }

            // Aplicativos correspondentes
            if (matchingApps.size > 1) {
                item(key = "spotlight_apps_section") {
                    GroupedSection(header = "Aplicativos") {
                        matchingApps.drop(1).take(5).forEachIndexed { idx, app ->
                            val iconBitmap = remember(app.packageName, app.icon) {
                                app.icon?.toBitmap(64, 64)?.asImageBitmap()
                            }
                            GroupedRow(
                                title = app.label,
                                leadingIcon = {
                                    if (iconBitmap != null) {
                                        Image(
                                            bitmap = iconBitmap,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(AppleShapes.small)
                                        )
                                    }
                                },
                                onClick = {
                                    Taptic.lightImpact(context)
                                    onAppClick(app)
                                },
                                showDivider = idx < 4
                            )
                        }
                    }
                }
            }

            // Cálculos / Conversões / Omni
            if (!calculatorResult.isNullOrBlank() || omniResult != null) {
                item(key = "spotlight_calc_section") {
                    GroupedSection(header = "Cálculo") {
                        val resultText = calculatorResult ?: omniResult?.primaryResult ?: ""
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = resultText,
                                style = typography.title1,
                                color = colors.label
                            )
                        }
                    }
                }
            }

            // Contatos
            if (matchingContacts.isNotEmpty()) {
                item(key = "spotlight_contacts_section") {
                    GroupedSection(header = "Contatos") {
                        matchingContacts.take(3).forEachIndexed { idx, contact ->
                            GroupedRow(
                                title = contact.name,
                                subtitle = contact.phoneNumber,
                                leadingIcon = {
                                    SquircleIcon(
                                        imageVector = Icons.Outlined.Person,
                                        backgroundColor = Color(0xFF007AFF)
                                    )
                                },
                                onClick = {
                                    Taptic.lightImpact(context)
                                    onContactClick(contact)
                                },
                                showDivider = idx < 2
                            )
                        }
                    }
                }
            }

            // Buscar na Web
            if (query.isNotBlank()) {
                item(key = "spotlight_web_search") {
                    GroupedSection(header = "Buscar na Web") {
                        GroupedRow(
                            title = "Pesquisar \"$query\"",
                            leadingIcon = {
                                SquircleIcon(
                                    imageVector = Icons.Outlined.Language,
                                    backgroundColor = Color(0xFF34C759)
                                )
                            },
                            onClick = {
                                Taptic.lightImpact(context)
                                onWebSearchClick(query)
                            },
                            showDivider = false
                        )
                    }
                }
            }
        }

        // Barra Inferior de Busca do Spotlight (Fixada acima do teclado)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = imeBottomPadding + 8.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = ContinuousShape(24.dp),
                        style = GlassStyle.Chrome
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = typography.body.copy(color = colors.label),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Spotlight",
                                style = typography.body,
                                color = colors.tertiaryLabel
                            )
                        }
                        innerTextField()
                    }
                )

                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(AppleShapes.capsule)
                            .background(colors.fill)
                            .clickable {
                                Taptic.lightImpact(context)
                                onQueryChange("")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Limpar",
                            tint = colors.secondaryLabel,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Cancelar",
                    style = typography.subheadlineBold,
                    color = colors.accent,
                    modifier = Modifier.clickable {
                        Taptic.lightImpact(context)
                        onCloseClick()
                    }
                )
            }
        }
    }
}
