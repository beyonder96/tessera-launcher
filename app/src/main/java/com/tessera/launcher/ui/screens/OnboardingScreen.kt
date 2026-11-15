package com.tessera.launcher.ui.screens

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tessera.launcher.R
import com.tessera.launcher.data.license.BillingConfig
import com.tessera.launcher.data.license.LicenseActivationResult
import com.tessera.launcher.data.license.LicenseManager
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.ACCENT_COLOR_OPTIONS
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Apresentação de Boas-Vindas Principal do Tessera Launcher (120Hz Editorial Luxury Showcase).
 * 4 Atos Coreografados:
 *  - Ato 1: O Despertar (Minimalismo, velocidade e identidade Tessera)
 *  - Ato 2: Estética Viva (Customização ao vivo de Tema & Cores no celular miniatura)
 *  - Ato 3: Coreografia Espacial (Simulador visual e tátil dos 3 gestos principais)
 *  - Ato 4: Passe de Fundador & Início (72h Pro liberado, definir como padrão e início rápido)
 */
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val isLight = uiState.isLightMode
    val accent = uiState.activeAccentColor

    val bg = if (isLight) LightBackground else AmoledBlack
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val textTertiary = if (isLight) Color(0xFF78716C) else TextTertiary
    val cardBg = if (isLight) LightCardBackground else Color(0xFF0F0F14)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF22222E)
    val springSpatial = Tessera.motion.springSpatial

    // Interceptação do botão Voltar
    BackHandler(enabled = true) {
        if (pagerState.currentPage > 0) {
            scope.launch {
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage - 1,
                    animationSpec = springSpatial
                )
            }
        } else if (uiState.isReplayingOnboarding) {
            viewModel.completeOnboarding()
        }
    }

    // Monitora retorno ao app para checar se virou launcher padrão
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAllPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val safeTopPadding = maxOf(statusBarTop, cutoutTop).coerceAtLeast(36.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(top = safeTopPadding)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        // Fundo Parallax Matemático Dinâmico (Dot Matrix + Halo de Acento)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isLight) 0.55f else 0.85f)
        ) {
            val width = size.width
            val height = size.height
            val pageOffset = pagerState.currentPage + pagerState.currentPageOffsetFraction

            // Halo Ambiente de Luz Reativo
            val glowCenterX = width * (0.5f - (pageOffset - 1.5f) * 0.15f)
            val glowCenterY = height * 0.35f
            val glowRadius = width * 0.70f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = if (isLight) 0.12f else 0.18f),
                        accent.copy(alpha = if (isLight) 0.04f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(glowCenterX, glowCenterY),
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = Offset(glowCenterX, glowCenterY)
            )

            // Malha Arquitetural de Pontos (Dot Matrix Nothing/Editorial)
            val dotSpacing = 28f
            val dotRadius = 1.25f
            val dotColor = if (isLight) Color(0x181C1917) else Color(0x22FFFFFF)
            val startY = 20f
            val endY = height - 20f
            var x = 20f
            while (x < width) {
                var y = startY
                while (y < endY) {
                    val distToGlow = kotlin.math.hypot(x - glowCenterX, y - glowCenterY)
                    val alphaMultiplier = if (distToGlow < glowRadius) 1.6f else 1f
                    drawCircle(
                        color = dotColor.copy(alpha = (dotColor.alpha * alphaMultiplier).coerceAtMost(0.35f)),
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    y += dotSpacing
                }
                x += dotSpacing
            }
        }

        // Conteúdo Principal em Camadas
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra Superior: Logo Tessera + Pular / Fechar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Text(
                        text = "TESSERA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.4.sp
                        ),
                        color = textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF16161D))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (pagerState.currentPage < 3) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(page = 3, animationSpec = springSpatial)
                                    }
                                } else {
                                    viewModel.completeOnboarding()
                                }
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .tesseraPressFeedback(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.isReplayingOnboarding) "Fechar" else if (pagerState.currentPage == 3) "Começar" else "Pular",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
                    )
                }
            }

            // Pager Horizontal dos 4 Atos
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> Act1Awakening(accent = accent, isLight = isLight)
                    1 -> Act2LivingAesthetics(
                        viewModel = viewModel,
                        uiState = uiState,
                        isLight = isLight,
                        accent = accent
                    )
                    2 -> Act3SpatialChoreography(
                        accent = accent,
                        isLight = isLight
                    )
                    3 -> Act4FounderPassAndLaunch(
                        viewModel = viewModel,
                        uiState = uiState,
                        isLight = isLight,
                        accent = accent
                    )
                }
            }

            // Barra Inferior: Indicador de Páginas + Botão Próximo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pílulas Indicadoras
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { idx ->
                        val isCurrent = pagerState.currentPage == idx
                        val pillWidth by animateFloatAsState(
                            targetValue = if (isCurrent) 28f else 8f,
                            animationSpec = Tessera.motion.springQuick,
                            label = "onboarding_pill_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(pillWidth.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) accent
                                    else if (isLight) Color(0xFFD4CEBD) else Color(0xFF282834)
                                )
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(page = idx, animationSpec = springSpatial)
                                    }
                                }
                        )
                    }
                }

                // Botão de Avanço / Conclusão
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isLight) Color(0xFF1C1917) else Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (pagerState.currentPage < 3) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            page = pagerState.currentPage + 1,
                                            animationSpec = springSpatial
                                        )
                                    }
                                } else {
                                    viewModel.completeOnboarding()
                                }
                            }
                        )
                        .padding(horizontal = 22.dp)
                        .tesseraPressFeedback(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (pagerState.currentPage == 3) {
                                if (uiState.isReplayingOnboarding) "Concluir" else "Entrar no Tessera"
                            } else "Avançar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Color.White else Color.Black
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = if (isLight) Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// ATO 1: O DESPERTAR (THE AWAKENING)
// =========================================================================
@Composable
private fun Act1Awakening(accent: Color, isLight: Boolean) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary

    val transition = rememberInfiniteTransition(label = "act1_rings")
    val ringPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_phase"
    )
    val breatheScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Teatro Óptico: 4-Tile Tessera Prism com Anéis Concêntricos
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(breatheScale),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width * 0.44f

                // 3 Anéis Concêntricos em Rotação Lenta
                for (i in 0..2) {
                    val radius = baseRadius * (0.65f + i * 0.22f)
                    val strokeWidth = 1.2f
                    val angleOffset = ringPhase * 360f * (if (i % 2 == 0) 1 else -1)

                    drawCircle(
                        color = accent.copy(alpha = (0.10f + i * 0.05f)),
                        radius = radius,
                        center = center,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    // Pontos orbitais de precisão
                    val rad = Math.toRadians((angleOffset + i * 120.0)).toFloat()
                    val orbitalX = center.x + radius * cos(rad)
                    val orbitalY = center.y + radius * sin(rad)
                    drawCircle(
                        color = accent.copy(alpha = 0.85f),
                        radius = 2.4f,
                        center = Offset(orbitalX, orbitalY)
                    )
                }
            }

            // Mosaico 4-Tile Tessera em Vidro Especular
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrismTile(accent = accent, isLight = isLight, rotationDeg = -4f)
                    PrismTile(accent = accent, isLight = isLight, rotationDeg = 4f)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrismTile(accent = accent, isLight = isLight, rotationDeg = 4f)
                    PrismTile(accent = accent, isLight = isLight, rotationDeg = -4f)
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Tipografia Editorial de Autor
        Text(
            text = "Silêncio Visual.\nVelocidade Pura.",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "O Tessera foi desenhado para quem busca clareza extrema no celular. Sem poluição visual, sem anúncios e com latência zero.",
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
            color = textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun PrismTile(accent: Color, isLight: Boolean, rotationDeg: Float) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .rotate(rotationDeg)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        if (isLight) Color(0xFFFFFFFF).copy(alpha = 0.85f) else Color(0xFF202028).copy(alpha = 0.90f),
                        if (isLight) Color(0xFFEAE5D8).copy(alpha = 0.90f) else Color(0xFF101015).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isLight) 0.65f else 0.35f),
                            accent.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    )
                ),
                RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accent)
        )
    }
}

// =========================================================================
// ATO 2: ESTÉTICA VIVA (LIVING AESTHETICS - LIVE PREVIEW)
// =========================================================================
@Composable
private fun Act2LivingAesthetics(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    isLight: Boolean,
    accent: Color
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Estética Viva",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Toque nos estilos abaixo para ver a transformação imediata em 120Hz.",
            fontSize = 13.5.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Miniatura Dinâmica de Smartphone ao Vivo
        MiniDeviceMockup(uiState = uiState, isLight = isLight, accent = accent)

        Spacer(modifier = Modifier.height(20.dp))

        // Seletor de Tema
        Text(
            text = "TEMA BASE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = textSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF14141B))
                .border(BorderStroke(1.dp, if (isLight) Color(0xFFDDD8CB) else Color(0xFF22222E)), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val themes = listOf(
                "AMOLED" to "Preto Puro",
                "DARK" to "Charcoal",
                "LIGHT" to "Journal"
            )
            themes.forEach { (mode, label) ->
                val isSelected = uiState.themeMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                if (isLight) Color.White else Color(0xFF242432)
                            } else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.setThemeMode(mode) }
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .tesseraPressFeedback(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) textPrimary else textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seletor de Acentos Pop
        Text(
            text = "COR DE DESTAQUE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = textSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(ACCENT_COLOR_OPTIONS) { opt ->
                val isSelected = opt.id.equals(uiState.accentColorName, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(opt.color)
                        .border(
                            BorderStroke(
                                if (isSelected) 2.5.dp else 1.dp,
                                if (isSelected) Color.White else Color(0x33FFFFFF)
                            ),
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.setAccentColor(opt.id) }
                        )
                        .tesseraPressFeedback(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (opt.color == Color.White) Color.Black else Color.White)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniDeviceMockup(
    uiState: LauncherUiState,
    isLight: Boolean,
    accent: Color
) {
    val deviceBg = if (isLight) Color(0xFFF7F4EB) else if (uiState.isAmoledMode) Color.Black else Color(0xFF101015)
    val deviceCardBg = if (isLight) Color(0xFFEFECE2) else Color(0xFF181822)
    val deviceBorder = if (isLight) Color(0xFFDDD8CB) else Color(0xFF282836)
    val textMain = if (isLight) Color(0xFF1C1917) else Color(0xFFF2F2F7)

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = deviceBg,
        border = BorderStroke(2.dp, deviceBorder),
        modifier = Modifier
            .width(220.dp)
            .height(210.dp)
            .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = accent.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Relógio Minimalista Editorial
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.formattedTime.ifBlank { "09:41" },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1).sp,
                    color = textMain
                )
                Text(
                    text = uiState.formattedDate.ifBlank { "QUA, 28 OUT" },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    letterSpacing = 1.sp
                )
            }

            // Mini Card de Aplicativos Rápidos
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = deviceCardBg,
                border = BorderStroke(1.dp, deviceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (idx == 0) accent else if (isLight) Color(0xFFDFDACD) else Color(0xFF242432)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (idx == 0) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (accent == Color.White) Color.Black else Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // Mini Barra de Pesquisa Split Pill
            Surface(
                shape = CircleShape,
                color = deviceCardBg,
                border = BorderStroke(1.dp, deviceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.Search, null, tint = accent, modifier = Modifier.size(12.dp))
                        Text(text = "Tessera...", fontSize = 9.sp, color = textMain.copy(alpha = 0.7f))
                    }
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(accent))
                }
            }
        }
    }
}

// =========================================================================
// ATO 3: COREOGRAFIA ESPACIAL (SPATIAL CHOREOGRAPHY)
// =========================================================================
@Composable
private fun Act3SpatialChoreography(accent: Color, isLight: Boolean) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val cardBg = if (isLight) LightCardBackground else Color(0xFF0F0F14)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF1E1E26)

    var activeDemoIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Coreografia Espacial",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tudo está a um deslize de distância. Sem procurar em dezenas de telas.",
            fontSize = 13.5.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Teatro de Gestos Interativo com Animação Tátil
        GestureTheaterCanvas(demoIndex = activeDemoIndex, accent = accent, isLight = isLight)

        Spacer(modifier = Modifier.height(24.dp))

        // Lista dos 3 Gestos Principais
        val gestures = listOf(
            Triple("Deslizar ↑", "Gaveta e Busca Instantânea", "Abre o teclado e lista inteligente indexada de A-Z"),
            Triple("Deslizar →", "Tela Lateral (−1)", "Acesso imediato à Central de Widgets Bento e Feed RSS"),
            Triple("2× Toque", "Bloquear a Tela", "Apaga a tela instantaneamente através do serviço de acessibilidade")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            gestures.forEachIndexed { index, (action, title, desc) ->
                val isSelected = activeDemoIndex == index
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) {
                        if (isLight) Color(0xFFFAF7F0) else Color(0xFF14141E)
                    } else cardBg,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) accent.copy(alpha = 0.6f) else cardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { activeDemoIndex = index }
                        )
                        .tesseraPressFeedback()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) accent.copy(alpha = 0.18f)
                                    else if (isLight) Color(0xFFE8E4D8) else Color(0xFF191924)
                                )
                                .border(
                                    BorderStroke(1.dp, if (isSelected) accent.copy(alpha = 0.45f) else Color.Transparent),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = action,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) accent else textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 11.5.sp,
                                color = textSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GestureTheaterCanvas(demoIndex: Int, accent: Color, isLight: Boolean) {
    val transition = rememberInfiniteTransition(label = "gesture_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gesture_phase"
    )

    val cardBg = if (isLight) Color(0xFFEFECE2) else Color(0xFF101017)
    val cardBorder = if (isLight) Color(0xFFDDD8CB) else Color(0xFF222230)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f

            when (demoIndex) {
                0 -> {
                    // Swipe Up (De baixo para cima)
                    val startY = centerY + 38f
                    val endY = centerY - 38f
                    val currentY = startY + (endY - startY) * phase
                    val trailAlpha = (1f - phase).coerceIn(0f, 1f)

                    // Linha Guia com degradê
                    drawLine(
                        brush = Brush.verticalGradient(
                            listOf(accent.copy(alpha = 0.8f), accent.copy(alpha = 0.05f)),
                            startY = endY,
                            endY = startY
                        ),
                        start = Offset(centerX, startY),
                        end = Offset(centerX, endY),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    // Ponto de toque com ripple
                    drawCircle(
                        color = accent.copy(alpha = trailAlpha * 0.45f),
                        radius = 16f * phase + 4f,
                        center = Offset(centerX, currentY)
                    )
                    drawCircle(
                        color = accent,
                        radius = 6f,
                        center = Offset(centerX, currentY)
                    )
                }
                1 -> {
                    // Swipe Right (Da esquerda para a direita)
                    val startX = centerX - 60f
                    val endX = centerX + 60f
                    val currentX = startX + (endX - startX) * phase
                    val trailAlpha = (1f - phase).coerceIn(0f, 1f)

                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.05f), accent.copy(alpha = 0.8f)),
                            startX = startX,
                            endX = endX
                        ),
                        start = Offset(startX, centerY),
                        end = Offset(endX, centerY),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = accent.copy(alpha = trailAlpha * 0.45f),
                        radius = 16f * phase + 4f,
                        center = Offset(currentX, centerY)
                    )
                    drawCircle(
                        color = accent,
                        radius = 6f,
                        center = Offset(currentX, centerY)
                    )
                }
                2 -> {
                    // Toque Duplo (2 taps com pulso)
                    val tap1 = phase < 0.5f
                    val localPhase = if (tap1) phase * 2f else (phase - 0.5f) * 2f
                    val rippleRadius = 8f + localPhase * 28f
                    val rippleAlpha = (1f - localPhase).coerceIn(0f, 1f)

                    drawCircle(
                        color = accent.copy(alpha = rippleAlpha * 0.5f),
                        radius = rippleRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = accent,
                        radius = 7f,
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }
    }
}

// =========================================================================
// ATO 4: PASSE DE FUNDADOR & INÍCIO (FOUNDER PASS & LAUNCH)
// =========================================================================
@Composable
private fun Act4FounderPassAndLaunch(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    isLight: Boolean,
    accent: Color
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val cardBg = if (isLight) LightCardBackground else Color(0xFF0F0F14)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF1E1E26)

    var inputKey by remember { mutableStateOf("") }
    var activationFeedback by remember { mutableStateOf<String?>(null) }
    var isActivationSuccess by remember { mutableStateOf(false) }
    var isActivationOpen by remember { mutableStateOf(false) }

    // Launcher de seleção de app padrão (Android 10+ / API 29+ RoleManager)
    val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(RoleManager::class.java)
    } else {
        null
    }

    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    fun requestDefaultLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            roleRequestLauncher.launch(intent)
        } else {
            runCatching {
                val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }.onFailure {
                runCatching {
                    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Cartão Metálico Especular do Passe de Fundador
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = if (isLight) Color(0xFFFAF7F0) else Color(0xFF13131C),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFFE2B714).copy(alpha = 0.85f),
                        accent.copy(alpha = 0.45f),
                        Color(0xFFE2B714).copy(alpha = 0.25f)
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFFE2B714).copy(alpha = 0.20f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2B714).copy(alpha = 0.16f))
                            .border(BorderStroke(1.dp, Color(0xFFE2B714).copy(alpha = 0.40f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PASSE DE FUNDADOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.4.sp,
                            color = Color(0xFFE2B714)
                        )
                    }

                    Text(
                        text = "72H PRO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Acesso Completo Liberado",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Você tem 3 dias de cortesia para explorar pastas, gestos, personalização do dock e a tela lateral. Após o teste, a gaveta, busca e uso diário permanecem 100% gratuitos para sempre.",
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Campo Opcional: Como gostaria de ser chamado
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SEU NOME OU APELIDO (OPCIONAL)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.greetingUserName.isEmpty()) {
                            Text(
                                text = "Como podemos te chamar?",
                                style = TextStyle(fontSize = 14.sp, color = textSecondary.copy(alpha = 0.5f))
                            )
                        }

                        BasicTextField(
                            value = uiState.greetingUserName,
                            onValueChange = { viewModel.setGreetingUserName(it) },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            ),
                            cursorBrush = SolidColor(if (isLight) Color.Black else Color.White),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botão de 1 Toque: Definir como Lançador Padrão
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardBg,
            border = BorderStroke(
                1.dp,
                if (uiState.isDefaultLauncher) Color(0xFF10B981).copy(alpha = 0.5f) else cardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { requestDefaultLauncher() }
                )
                .tesseraPressFeedback()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.isDefaultLauncher) Color(0xFF10B981).copy(alpha = 0.16f)
                            else if (isLight) Color(0xFFE8E4D8) else Color(0xFF191924)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isDefaultLauncher) Icons.Outlined.Check else Icons.Outlined.Home,
                        contentDescription = null,
                        tint = if (uiState.isDefaultLauncher) Color(0xFF10B981) else textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.isDefaultLauncher) "Lançador Padrão Configurado" else "Definir como Lançador Padrão",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = if (uiState.isDefaultLauncher) "Tessera já gerencia sua tela inicial" else "Evita que o sistema volte para a tela antiga",
                        fontSize = 11.5.sp,
                        color = textSecondary
                    )
                }

                if (!uiState.isDefaultLauncher) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gaveta Expansível: "Já tenho um código TESS-..."
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isActivationOpen = !isActivationOpen }
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Outlined.VpnKey, null, tint = textSecondary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Já tenho uma chave de ativação",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }
                    Icon(
                        imageVector = if (isActivationOpen) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = isActivationOpen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        HorizontalDivider(color = cardBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF14141B))
                                .border(BorderStroke(1.dp, if (isLight) Color(0xFFDDD8CB) else Color(0xFF242432)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (inputKey.isEmpty()) {
                                        Text(
                                            text = "TESS-XXXX-XXXX-XXXX",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                color = textSecondary.copy(alpha = 0.5f)
                                            )
                                        )
                                    }

                                    BasicTextField(
                                        value = inputKey,
                                        onValueChange = { raw ->
                                            inputKey = LicenseManager.normalizeKeyInput(raw)
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        ),
                                        cursorBrush = SolidColor(if (isLight) Color.Black else Color.White),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                val clipboard = LocalClipboardManager.current
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isLight) Color(0xFFDFDACD) else Color(0xFF20202C))
                                        .clickable {
                                            val clip = clipboard.getText()?.text.orEmpty()
                                            if (clip.isNotBlank()) {
                                                inputKey = LicenseManager.normalizeKeyInput(clip)
                                            }
                                        }
                                        .tesseraPressFeedback(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.ContentPaste, null, tint = textPrimary, modifier = Modifier.size(15.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isLight) Color(0xFF1C1917) else Color.White)
                                .clickable {
                                    keyboardController?.hide()
                                    if (inputKey.isNotBlank()) {
                                        val res = viewModel.activateLicenseKey(inputKey)
                                        when (res) {
                                            is LicenseActivationResult.Success -> {
                                                activationFeedback = res.message
                                                isActivationSuccess = true
                                                inputKey = ""
                                            }
                                            is LicenseActivationResult.InvalidKey -> {
                                                activationFeedback = res.message
                                                isActivationSuccess = false
                                            }
                                            is LicenseActivationResult.ExpiredKey -> {
                                                activationFeedback = res.message
                                                isActivationSuccess = false
                                            }
                                        }
                                    }
                                }
                                .tesseraPressFeedback(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ativar Chave",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Color.White else Color.Black
                            )
                        }

                        activationFeedback?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg,
                                fontSize = 11.5.sp,
                                color = if (isActivationSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}
