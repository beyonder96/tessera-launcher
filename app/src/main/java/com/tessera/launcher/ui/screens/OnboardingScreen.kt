package com.tessera.launcher.ui.screens

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tessera.launcher.data.license.LicenseActivationResult
import com.tessera.launcher.data.license.LicenseManager
import com.tessera.launcher.data.license.LicenseTier
import com.tessera.launcher.ui.components.DefaultLauncherAnimatedCard
import com.tessera.launcher.ui.components.RestrictedSettingsAnimatedCard
import com.tessera.launcher.ui.components.RestrictedSettingsGuideDialog
import com.tessera.launcher.ui.components.SubscriptionMicroTagLabel
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.ACCENT_COLOR_OPTIONS
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private const val ONBOARDING_PAGE_COUNT = 5
private const val ONBOARDING_LAST_PAGE = ONBOARDING_PAGE_COUNT - 1

/**
 * Apresentação de Boas-Vindas Principal do Tessera Launcher (120Hz Editorial Luxury Showcase).
 * 5 Atos Coreografados:
 *  - Ato 1: O Despertar (Minimalismo, velocidade, identidade Tessera e detecção de reinstalação)
 *  - Ato 2: Estética Viva (Tema Automático Dia/Noite animado, AMOLED, Charcoal, Journal & Cores)
 *  - Ato 3: Coreografia Espacial (Simulador visual e tátil dos 3 gestos principais)
 *  - Ato 4: Permissões & Padrão (Animação de Definir como Padrão, porquê de cada permissão e guia Android 13+)
 *  - Ato 5: Assinatura & Modo Teste (Auto-validação de conta/chave, aviso de 72h e início rápido)
 */
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { ONBOARDING_PAGE_COUNT })
    val isLight = uiState.isLightMode
    val accent = uiState.activeAccentColor

    val bg = if (isLight) LightBackground else AmoledBlack
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
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

    // Monitora retorno ao app para atualizar status de permissões e launcher padrão em tempo real
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

            val glowCenterX = width * (0.5f - (pageOffset - 2f) * 0.13f)
            val glowCenterY = height * 0.34f
            val glowRadius = width * 0.72f
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
            // Barra Superior: Logo Tessera + Indicador de Ato + Pular / Fechar
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
                    Text(
                        text = "· ${pagerState.currentPage + 1}/$ONBOARDING_PAGE_COUNT",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
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
                                if (pagerState.currentPage < ONBOARDING_LAST_PAGE) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            page = ONBOARDING_LAST_PAGE,
                                            animationSpec = springSpatial
                                        )
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
                        text = if (uiState.isReplayingOnboarding) "Fechar"
                        else if (pagerState.currentPage == ONBOARDING_LAST_PAGE) "Começar"
                        else "Pular",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
                    )
                }
            }

            // Pager Horizontal dos 5 Atos
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> Act1Awakening(
                        accent = accent,
                        isLight = isLight,
                        isReinstallDetected = uiState.isReinstallDetected
                    )
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
                    3 -> Act4PermissionsAndDefaultSetup(
                        viewModel = viewModel,
                        uiState = uiState,
                        isLight = isLight,
                        accent = accent
                    )
                    4 -> Act5SubscriptionAndLaunch(
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
                    repeat(ONBOARDING_PAGE_COUNT) { idx ->
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
                                if (pagerState.currentPage < ONBOARDING_LAST_PAGE) {
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
                            text = if (pagerState.currentPage == ONBOARDING_LAST_PAGE) {
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
private fun Act1Awakening(
    accent: Color,
    isLight: Boolean,
    isReinstallDetected: Boolean
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary

    val transition = rememberInfiniteTransition(label = "act1_rings")
    val ringPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_phase"
    )
    val breatheScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isReinstallDetected) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (isLight) 0.12f else 0.16f))
                    .border(BorderStroke(1.dp, accent.copy(alpha = 0.35f)), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "BEM-VINDO DE VOLTA • VAMOS REATIVAR TUDO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                    color = if (accent == Color.White && isLight) textPrimary else accent
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Teatro Óptico: 4-Tile Tessera Prism com Anéis Concêntricos
        Box(
            modifier = Modifier
                .size(230.dp)
                .scale(breatheScale),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width * 0.44f

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

                    val rad = Math.toRadians((angleOffset + i * 120.0)).toFloat()
                    val orbitalX = center.x + radius * cos(rad)
                    val orbitalY = center.y + radius * sin(rad)
                    drawCircle(
                        color = accent.copy(alpha = 0.88f),
                        radius = 2.6f,
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

        Spacer(modifier = Modifier.height(28.dp))

        // Tipografia Editorial de Autor
        Text(
            text = "Silêncio Visual.\nVelocidade Pura.",
            fontSize = 31.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 37.sp,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isReinstallDetected) {
                "Detectamos que você baixou o Tessera novamente. Vamos revisar seu estilo, reativar suas permissões e validar sua assinatura automaticamente."
            } else {
                "O Tessera foi desenhado para quem busca foco extremo no celular. Sem poluição visual, sem anúncios e com resposta imediata em 120Hz."
            },
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 3 Pilares do Tessera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            PillarChip(icon = Icons.Outlined.Bolt, label = "120Hz Nativo", isLight = isLight, accent = accent)
            PillarChip(icon = Icons.Outlined.Shield, label = "Zero Rastreio", isLight = isLight, accent = accent)
            PillarChip(icon = Icons.Outlined.BatteryChargingFull, label = "AMOLED Puro", isLight = isLight, accent = accent)
        }
    }
}

@Composable
private fun PillarChip(
    icon: ImageVector,
    label: String,
    isLight: Boolean,
    accent: Color
) {
    val bg = if (isLight) Color(0xFFEBE6D8) else Color(0xFF14141C)
    val border = if (isLight) Color(0xFFD8D2C2) else Color(0xFF252534)
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(BorderStroke(1.dp, border), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (accent == Color.White && isLight) textPrimary else accent,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary
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
// ATO 2: ESTÉTICA VIVA + TEMA AUTOMÁTICO DIA/NOITE
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
    val isAutoSelected = uiState.themeMode.equals("AUTO", ignoreCase = true)

    // Ciclo demonstrativo contínuo Dia ☀️ ↔ Noite 🌙 quando "Automático" está selecionado
    var demoIsNightPhase by remember { mutableStateOf(uiState.isAutoNightNow) }
    LaunchedEffect(isAutoSelected) {
        if (isAutoSelected) {
            while (true) {
                delay(2400)
                demoIsNightPhase = !demoIsNightPhase
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
        Text(
            text = "Estética Viva",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isAutoSelected) {
                "No modo Automático, o Tessera muda sozinho entre Claro de dia (06h–18h) e AMOLED à noite ou com o Modo Escuro do Android."
            } else {
                "Toque nos estilos abaixo para ver a transformação imediata em 120Hz."
            },
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Miniatura Dinâmica de Smartphone ao Vivo (com demonstração Dia/Noite para AUTO)
        MiniDeviceMockup(
            uiState = uiState,
            isAutoDemo = isAutoSelected,
            demoIsNightPhase = demoIsNightPhase,
            accent = accent
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Seletor de Tema Base (4 opções incluindo Automático)
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
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF14141B))
                .border(
                    BorderStroke(1.dp, if (isLight) Color(0xFFDDD8CB) else Color(0xFF22222E)),
                    RoundedCornerShape(14.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val themes = listOf(
                "AUTO" to "Auto",
                "AMOLED" to "AMOLED",
                "DARK" to "Charcoal",
                "LIGHT" to "Journal"
            )
            themes.forEach { (mode, label) ->
                val isSelected = uiState.themeMode.equals(mode, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                        .padding(vertical = 8.dp)
                        .tesseraPressFeedback(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (mode == "AUTO") {
                            Icon(
                                imageVector = Icons.Outlined.BrightnessAuto,
                                contentDescription = null,
                                tint = if (isSelected) textPrimary else textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) textPrimary else textSecondary
                        )
                    }
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
    isAutoDemo: Boolean,
    demoIsNightPhase: Boolean,
    accent: Color
) {
    val effectiveIsLight = if (isAutoDemo) !demoIsNightPhase else uiState.isLightMode
    val effectiveIsAmoled = if (isAutoDemo) demoIsNightPhase else uiState.isAmoledMode

    val deviceBg by animateColorAsState(
        targetValue = if (effectiveIsLight) Color(0xFFF7F4EB) else if (effectiveIsAmoled) Color.Black else Color(0xFF101015),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "mockup_bg"
    )
    val deviceCardBg by animateColorAsState(
        targetValue = if (effectiveIsLight) Color(0xFFEFECE2) else Color(0xFF181822),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "mockup_card_bg"
    )
    val deviceBorder by animateColorAsState(
        targetValue = if (effectiveIsLight) Color(0xFFDDD8CB) else Color(0xFF282836),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "mockup_border"
    )
    val textMain by animateColorAsState(
        targetValue = if (effectiveIsLight) Color(0xFF1C1917) else Color(0xFFF2F2F7),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "mockup_text"
    )
    val mockupAccent by animateColorAsState(
        targetValue = if (accent == Color.White && effectiveIsLight) Color(0xFF1C1917)
        else if (accent == Color(0xFF1C1917) && !effectiveIsLight) Color.White
        else accent,
        animationSpec = tween(550),
        label = "mockup_accent"
    )

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = deviceBg,
        border = BorderStroke(2.dp, deviceBorder),
        modifier = Modifier
            .width(232.dp)
            .height(226.dp)
            .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = mockupAccent.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Indicador Dia/Noite animado quando Automático está selecionado
            if (isAutoDemo) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(deviceCardBg)
                            .border(BorderStroke(1.dp, deviceBorder), CircleShape)
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (effectiveIsLight) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = if (effectiveIsLight) Color(0xFFD97706) else Color(0xFF93C5FD),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (effectiveIsLight) "DIA · JOURNAL" else "NOITE · AMOLED",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = textMain.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Relógio Minimalista Editorial
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isAutoDemo) {
                        if (effectiveIsLight) "09:41" else "21:30"
                    } else {
                        uiState.formattedTime.ifBlank { "09:41" }
                    },
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1).sp,
                    color = textMain
                )
                Text(
                    text = uiState.formattedDate.ifBlank { "QUA, 28 OUT" },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = mockupAccent,
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
                                .background(
                                    if (idx == 0) mockupAccent
                                    else if (effectiveIsLight) Color(0xFFDFDACD) else Color(0xFF242432)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (idx == 0) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (mockupAccent == Color.White) Color.Black else Color.White)
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
                        Icon(Icons.Outlined.Search, null, tint = mockupAccent, modifier = Modifier.size(12.dp))
                        Text(text = "Tessera...", fontSize = 9.sp, color = textMain.copy(alpha = 0.7f))
                    }
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(mockupAccent)
                    )
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

        Spacer(modifier = Modifier.height(22.dp))

        // Teatro de Gestos Interativo com Animação Tátil
        GestureTheaterCanvas(demoIndex = activeDemoIndex, accent = accent, isLight = isLight)

        Spacer(modifier = Modifier.height(22.dp))

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
                    val startY = centerY + 38f
                    val endY = centerY - 38f
                    val currentY = startY + (endY - startY) * phase
                    val trailAlpha = (1f - phase).coerceIn(0f, 1f)

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
// ATO 4: PASSO A PASSO DE PERMISSÕES, LANÇADOR PADRÃO & CONFIGURAÇÕES RESTRITAS
// =========================================================================
private data class OnboardingRestrictedPrompt(
    val permissionName: String,
    val openSettings: () -> Unit
)

@Composable
private fun Act4PermissionsAndDefaultSetup(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    isLight: Boolean,
    accent: Color
) {
    val context = LocalContext.current
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val cardBg = if (isLight) LightCardBackground else Color(0xFF0F0F14)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF1E1E26)

    var showRestrictedInlineGuide by remember { mutableStateOf(false) }
    var restrictedDialogPrompt by remember { mutableStateOf<OnboardingRestrictedPrompt?>(null) }

    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateLocationPermission(granted)
        viewModel.refreshAllPermissions(context)
    }

    val calendarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val contactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions(context)
    }

    val openAppInfo = {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    val requestDefaultLauncher = {
        val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
        } else null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            roleManager != null &&
            roleManager.isRoleAvailable(RoleManager.ROLE_HOME)
        ) {
            roleRequestLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
        } else {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }.onFailure {
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
        }
    }

    restrictedDialogPrompt?.let { prompt ->
        RestrictedSettingsGuideDialog(
            permissionName = prompt.permissionName,
            accent = accent,
            isLight = isLight,
            onOpenAppInfo = { openAppInfo() },
            onOpenPermissionSettings = prompt.openSettings,
            onDismiss = { restrictedDialogPrompt = null }
        )
    }

    val grantedStepCount = remember(
        uiState.isDefaultLauncher,
        uiState.hasLocationPermission,
        uiState.hasCalendarPermission,
        uiState.hasContactsPermission,
        uiState.hasNotificationAccess,
        uiState.hasAccessibilityService
    ) {
        listOf(
            uiState.isDefaultLauncher,
            uiState.hasLocationPermission,
            uiState.hasCalendarPermission,
            uiState.hasContactsPermission,
            uiState.hasNotificationAccess,
            uiState.hasAccessibilityService
        ).count { it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Permissões & Início",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Veja exatamente por que cada acesso existe. Tudo é opcional e processado 100% no seu aparelho ($grantedStepCount/6 ativas).",
            fontSize = 12.5.sp,
            lineHeight = 17.5.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Passo 1 em Destaque: Animação interativa de Definir como Lançador Padrão
        DefaultLauncherAnimatedCard(
            isDefaultLauncher = uiState.isDefaultLauncher,
            accent = accent,
            isLight = isLight,
            onRequestDefault = { requestDefaultLauncher() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Passo a Passo das Permissões Principais com o Porquê
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OnboardingPermissionStepRow(
                    icon = Icons.Outlined.LocationOn,
                    title = "Clima & Localização",
                    why = "Por quê: atualiza a previsão local no Smart Glance sem abrir apps.",
                    isGranted = uiState.hasLocationPermission,
                    isLight = isLight,
                    accent = accent,
                    onClick = {
                        if (!uiState.hasLocationPermission) {
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )

                HorizontalDivider(color = cardBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 14.dp))

                OnboardingPermissionStepRow(
                    icon = Icons.Outlined.CalendarToday,
                    title = "Calendário & Agenda",
                    why = "Por quê: exibe seu próximo compromisso no topo da tela inicial.",
                    isGranted = uiState.hasCalendarPermission,
                    isLight = isLight,
                    accent = accent,
                    onClick = {
                        if (!uiState.hasCalendarPermission) {
                            calendarLauncher.launch(Manifest.permission.READ_CALENDAR)
                        }
                    }
                )

                HorizontalDivider(color = cardBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 14.dp))

                OnboardingPermissionStepRow(
                    icon = Icons.Outlined.People,
                    title = "Busca de Contatos",
                    why = "Por quê: permite ligar ou enviar mensagem direto pela busca.",
                    isGranted = uiState.hasContactsPermission,
                    isLight = isLight,
                    accent = accent,
                    onClick = {
                        if (!uiState.hasContactsPermission) {
                            contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }
                )

                HorizontalDivider(color = cardBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 14.dp))

                val openNotifSettings = {
                    runCatching {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                }
                OnboardingPermissionStepRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Notificações & Música",
                    why = "Por quê: controla Spotify/YouTube Music e mostra badges sutis.",
                    requiresAndroid13Note = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !uiState.hasNotificationAccess,
                    isGranted = uiState.hasNotificationAccess,
                    isLight = isLight,
                    accent = accent,
                    onClick = {
                        if (!uiState.hasNotificationAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            restrictedDialogPrompt = OnboardingRestrictedPrompt(
                                permissionName = "Notificações & Música",
                                openSettings = { openNotifSettings() }
                            )
                        } else {
                            openNotifSettings()
                        }
                    }
                )

                HorizontalDivider(color = cardBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 14.dp))

                val openAccSettings = {
                    runCatching {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                }
                OnboardingPermissionStepRow(
                    icon = Icons.Outlined.AccessibilityNew,
                    title = "Acessibilidade (2× Toque)",
                    why = "Por quê: permite bloquear a tela com dois toques no fundo vazio.",
                    requiresAndroid13Note = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !uiState.hasAccessibilityService,
                    isGranted = uiState.hasAccessibilityService,
                    isLight = isLight,
                    accent = accent,
                    onClick = {
                        if (!uiState.hasAccessibilityService && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            restrictedDialogPrompt = OnboardingRestrictedPrompt(
                                permissionName = "Acessibilidade",
                                openSettings = { openAccSettings() }
                            )
                        } else {
                            openAccSettings()
                        }
                    }
                )
            }
        }

        // Guia Expansível de Configurações Restritas (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isLight) Color(0xFFF3ECDA) else Color(0xFF15131C),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showRestrictedInlineGuide = !showRestrictedInlineGuide }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = if (isLight) Color(0xFFB45309) else Color(0xFFF59E0B),
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Android bloqueou Acessibilidade/Notificações? Ver passo a passo",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary,
                            lineHeight = 15.sp
                        )
                    }
                    Icon(
                        imageVector = if (showRestrictedInlineGuide) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showRestrictedInlineGuide) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    RestrictedSettingsAnimatedCard(
                        accent = accent,
                        isLight = isLight,
                        onOpenAppInfo = { openAppInfo() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun OnboardingPermissionStepRow(
    icon: ImageVector,
    title: String,
    why: String,
    isGranted: Boolean,
    isLight: Boolean,
    accent: Color,
    requiresAndroid13Note: Boolean = false,
    onClick: () -> Unit
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val successGreen = Color(0xFF10B981)

    val pulseScale = remember { Animatable(1f) }
    var previousGranted by remember { mutableStateOf(isGranted) }
    LaunchedEffect(isGranted) {
        if (isGranted && !previousGranted) {
            pulseScale.snapTo(0.84f)
            pulseScale.animateTo(
                targetValue = 1.15f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            pulseScale.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
        }
        previousGranted = isGranted
    }

    val iconBg by animateColorAsState(
        targetValue = if (isGranted) successGreen.copy(alpha = 0.16f)
        else if (isLight) Color(0xFFE8E4D8) else Color(0xFF191924),
        animationSpec = tween(280),
        label = "onb_perm_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .scale(pulseScale.value)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Outlined.Check else icon,
                contentDescription = null,
                tint = if (isGranted) successGreen else textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                if (requiresAndroid13Note) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.16f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "GUIA ⋮",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLight) Color(0xFFB45309) else Color(0xFFF59E0B)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = why,
                fontSize = 11.sp,
                lineHeight = 14.5.sp,
                color = textSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            AnimatedVisibility(visible = true, enter = scaleIn() + fadeIn()) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Concedida",
                    tint = successGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(alpha = if (isLight) 0.13f else 0.16f))
                    .border(BorderStroke(1.dp, accent.copy(alpha = 0.35f)), RoundedCornerShape(9.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Ativar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (accent == Color.White && isLight) textPrimary else accent
                )
            }
        }
    }
}

// =========================================================================
// ATO 5: ASSINATURA, AUTO-VALIDAÇÃO & MODO TESTE COM AVISO
// =========================================================================
@Composable
private fun Act5SubscriptionAndLaunch(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    isLight: Boolean,
    accent: Color
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val cardBg = if (isLight) LightCardBackground else Color(0xFF0F0F14)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF1E1E26)

    val status = uiState.licenseStatus
    val isPaidOrMaster = status.hasProAccess && status.tier != LicenseTier.TRIAL

    // Tenta validar automaticamente a assinatura vinculada à conta ao chegar neste ato
    LaunchedEffect(Unit) {
        viewModel.autoValidateSavedSubscriptionIfNeeded()
    }

    var isActivationOpen by remember { mutableStateOf(false) }
    var activationTab by remember { mutableIntStateOf(0) } // 0 = E-mail da Conta, 1 = Chave TESS-XXXX
    var inputEmail by remember { mutableStateOf(uiState.licenseInfo.licensedEmail.orEmpty()) }
    var inputKey by remember { mutableStateOf("") }
    var keyFeedback by remember { mutableStateOf<String?>(null) }
    var isKeySuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Cartão Principal: Assinatura Validada OU Modo Teste Ativo com Aviso
        if (isPaidOrMaster) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = if (isLight) Color(0xFFECFDF5) else Color(0xFF0D1B16),
                border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.60f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFF10B981).copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "ASSINATURA VALIDADA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.3.sp,
                                color = Color(0xFF10B981)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PRO ATIVO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = status.activationTypeLabel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = buildString {
                            append("Todos os recursos avançados do Tessera foram liberados automaticamente.")
                            status.activeKeyMasked?.let { linked ->
                                append("\nVinculado a: $linked")
                            }
                        },
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = textSecondary
                    )
                }
            }
        } else {
            // Cartão do Modo Teste com Aviso Explícito
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
                        .padding(20.dp)
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
                                .border(
                                    BorderStroke(1.dp, Color(0xFFE2B714).copy(alpha = 0.42f)),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (status.isTrialExpired) "MODO FREE" else "MODO TESTE ATIVADO",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = if (isLight) Color(0xFFB45309) else Color(0xFFE2B714)
                            )
                        }

                        Text(
                            text = if (status.isTrialExpired) "EXPIRADO" else "72H PRO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (status.isTrialExpired) {
                            "Período de Teste Encerrado"
                        } else {
                            "Avaliação Completa por 3 Dias"
                        },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Banner de Aviso explícito explicando o Modo Teste e a micro-tag TESTE
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE2B714).copy(alpha = if (isLight) 0.12f else 0.10f),
                        border = BorderStroke(1.dp, Color(0xFFE2B714).copy(alpha = 0.32f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = if (isLight) Color(0xFFB45309) else Color(0xFFE2B714),
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Aviso do Modo Teste",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    SubscriptionMicroTagLabel(
                                        label = "TESTE",
                                        isTrial = true,
                                        isLightMode = isLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Como nenhuma assinatura ou chave foi vinculada, ativamos 72h de teste grátis. Funções marcadas com a tag TESTE exigem assinatura após esse prazo; a gaveta de apps e a busca continuam 100% gratuitas para sempre.",
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }

                    if (uiState.isAutoValidatingSubscription) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = uiState.autoValidationFeedback ?: "Verificando assinatura...",
                                fontSize = 11.5.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        if (!isPaidOrMaster) {
            Spacer(modifier = Modifier.height(12.dp))

            // Gaveta Expansível Unificada: Validar por E-mail da Conta OU Chave TESS-XXXX
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
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Outlined.VpnKey, null, tint = textSecondary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Já tenho assinatura ou chave",
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

                            // Alternador: [ E-mail da Conta | Chave TESS ]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF14141B))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Conta (E-mail)", "Chave TESS").forEachIndexed { idx, label ->
                                    val selected = activationTab == idx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(
                                                if (selected) {
                                                    if (isLight) Color.White else Color(0xFF252534)
                                                } else Color.Transparent
                                            )
                                            .clickable { activationTab = idx }
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) textPrimary else textSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (activationTab == 0) {
                                // Validação por E-mail vinculado à assinatura
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF14141B))
                                        .border(
                                            BorderStroke(1.dp, if (isLight) Color(0xFFDDD8CB) else Color(0xFF242432)),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 11.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Outlined.Email, null, tint = textSecondary, modifier = Modifier.size(16.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (inputEmail.isEmpty()) {
                                                Text(
                                                    text = "seu@email.com",
                                                    style = TextStyle(fontSize = 13.sp, color = textSecondary.copy(alpha = 0.5f))
                                                )
                                            }
                                            BasicTextField(
                                                value = inputEmail,
                                                onValueChange = { inputEmail = it },
                                                singleLine = true,
                                                textStyle = TextStyle(
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = textPrimary
                                                ),
                                                cursorBrush = SolidColor(if (isLight) Color.Black else Color.White),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Email,
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        keyboardController?.hide()
                                                        viewModel.validateSubscriptionByEmailForOnboarding(inputEmail)
                                                    }
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
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
                                        .clickable(enabled = !uiState.isAutoValidatingSubscription) {
                                            keyboardController?.hide()
                                            viewModel.validateSubscriptionByEmailForOnboarding(inputEmail)
                                        }
                                        .tesseraPressFeedback(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isAutoValidatingSubscription) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            color = if (isLight) Color.White else Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "Validar Assinatura da Conta",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) Color.White else Color.Black
                                        )
                                    }
                                }

                                uiState.autoValidationFeedback?.let { feedback ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = feedback,
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.5.sp,
                                        color = if (isPaidOrMaster) Color(0xFF10B981) else textSecondary
                                    )
                                }
                            } else {
                                // Ativação por Chave TESS-XXXX-XXXX-XXXX
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF14141B))
                                        .border(
                                            BorderStroke(1.dp, if (isLight) Color(0xFFDDD8CB) else Color(0xFF242432)),
                                            RoundedCornerShape(12.dp)
                                        )
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
                                                when (val res = viewModel.activateLicenseKey(inputKey)) {
                                                    is LicenseActivationResult.Success -> {
                                                        keyFeedback = res.message
                                                        isKeySuccess = true
                                                        inputKey = ""
                                                    }
                                                    is LicenseActivationResult.InvalidKey -> {
                                                        keyFeedback = res.message
                                                        isKeySuccess = false
                                                    }
                                                    is LicenseActivationResult.ExpiredKey -> {
                                                        keyFeedback = res.message
                                                        isKeySuccess = false
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

                                keyFeedback?.let { msg ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = msg,
                                        fontSize = 11.5.sp,
                                        color = if (isKeySuccess) Color(0xFF10B981) else Color(0xFFEF4444)
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
