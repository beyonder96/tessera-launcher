package com.tessera.launcher.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tessera.launcher.data.license.BillingConfig
import com.tessera.launcher.data.license.LicenseActivationResult
import com.tessera.launcher.data.license.LicenseManager
import com.tessera.launcher.data.license.LicenseStatus
import com.tessera.launcher.data.license.LicenseTier
import com.tessera.launcher.ui.components.AppleProAmbientMeshBackdrop
import com.tessera.launcher.ui.components.AppleProLifetimeUpgradeCard
import com.tessera.launcher.ui.components.AppleProMedallion
import com.tessera.launcher.ui.components.AppleProMembershipCard
import com.tessera.launcher.ui.components.AppleProPrivilegesBento
import com.tessera.launcher.ui.components.extractCleanFirstName
import com.tessera.launcher.ui.components.resolveAppleProVisuals
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.CharcoalBackground
import com.tessera.launcher.ui.theme.CharcoalCardBackground
import com.tessera.launcher.ui.theme.CharcoalCardBorder
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

/**
 * Diálogo suave de bloqueio comercial (Freemium Soft Lock) ao acessar recurso Tessera Pro.
 */
@Composable
fun ProUpgradeDialog(
    featureName: String,
    licenseStatus: LicenseStatus,
    isLightMode: Boolean,
    onOpenLicenseScreen: () -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = if (isLightMode) LightCardBackground else Color(0xFF101015)
    val cardBorder = if (isLightMode) LightCardBorder else Color(0xFF242430)
    val textPrimary = if (isLightMode) LightTextPrimary else TextPrimary
    val textSecondary = if (isLightMode) LightTextSecondary else TextSecondary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFE2B714).copy(alpha = 0.22f),
                                        Color(0xFFE2B714).copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .border(BorderStroke(1.dp, Color(0xFFE2B714).copy(alpha = 0.40f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Recurso Pro",
                            tint = Color(0xFFE2B714),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Recurso Tessera Pro",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"$featureName\" é um recurso avançado do Tessera Pro.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val notice = if (licenseStatus.isTrialExpired) {
                        "Seu período de teste gratuito de 3 dias expirou. O uso diário de busca, gaveta e apps continua 100% livre para sempre."
                    } else {
                        "Desbloqueie personalização irrestrita, organização completa e a tela lateral."
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isLightMode) Color(0xFFE8E4D8) else Color(0xFF16161D))
                            .border(BorderStroke(1.dp, if (isLightMode) Color(0xFFDDD8CB) else Color(0xFF262632)), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isLightMode) Color(0xFFE0DDD2) else Color(0xFF1F1F28))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDismiss
                                )
                                .tesseraPressFeedback(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Continuar no Free",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isLightMode) Color(0xFF1C1917) else Color.White)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onOpenLicenseScreen
                                )
                                .tesseraPressFeedback(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Desbloquear Pro",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLightMode) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tela de Gerenciamento da Licença, Experiência de Agradecimento Pro (Estilo Apple)
 * e Vitrine de Planos do Tessera Launcher.
 */
@Composable
fun LicenseScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status = uiState.licenseStatus
    val isLight = uiState.isLightMode
    val isCharcoal = uiState.themeMode == "DARK"
    val isAmoled = (uiState.themeMode == "AMOLED" || uiState.isAmoledMode) && !isCharcoal && !isLight

    val bg = when {
        isLight -> LightBackground
        isCharcoal -> CharcoalBackground
        else -> AmoledBlack
    }
    val cardBg = when {
        isLight -> LightCardBackground
        isCharcoal -> CharcoalCardBackground
        else -> Color(0xFF0E0E13)
    }
    val cardBorder = when {
        isLight -> LightCardBorder
        isCharcoal -> CharcoalCardBorder
        else -> Color(0xFF1F1F29)
    }
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val textTertiary = if (isLight) Color(0xFF625B55) else TextTertiary

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val safeTopPadding = maxOf(statusBarTop, cutoutTop).coerceAtLeast(36.dp) + 8.dp
    val safeBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    fun openBrowserUrl(rawUrl: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "Não foi possível abrir o navegador.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSupportEmail(subject: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${BillingConfig.SUPPORT_EMAIL}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "Nenhum aplicativo de e-mail encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        AnimatedContent(
            targetState = status.isSubscribedPro,
            transitionSpec = {
                (fadeIn(tween(380, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.965f, animationSpec = tween(380, easing = FastOutSlowInEasing)))
                    .togetherWith(fadeOut(tween(220, easing = FastOutSlowInEasing)))
            },
            label = "license_screen_mode_transition"
        ) { isSubscribedPro ->
            if (isSubscribedPro) {
                AppleProThankYouExperience(
                    viewModel = viewModel,
                    uiState = uiState,
                    bg = bg,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    textTertiary = textTertiary,
                    isLight = isLight,
                    isAmoled = isAmoled,
                    safeTopPadding = safeTopPadding,
                    safeBottomPadding = safeBottomPadding,
                    onBack = onBack,
                    onOpenBrowserUrl = ::openBrowserUrl,
                    onOpenSupportEmail = { openSupportEmail("Tessera Pro — Suporte Prioritário") }
                )
            } else {
                StandardLicenseStorefront(
                    viewModel = viewModel,
                    uiState = uiState,
                    bg = bg,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    textTertiary = textTertiary,
                    isLight = isLight,
                    safeTopPadding = safeTopPadding,
                    safeBottomPadding = safeBottomPadding,
                    onBack = onBack,
                    openBrowserUrl = ::openBrowserUrl
                )
            }
        }
    }
}

/**
 * Palco de Agradecimento & Assinatura Ativa (Estilo Apple One / Apple Card / iCloud+).
 * Exibido exclusivamente para assinantes e licenças ativas (Vitalício, Anual, Semestral, Mensal, Cortesia e Master).
 */
@Composable
private fun AppleProThankYouExperience(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    bg: Color,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    isLight: Boolean,
    isAmoled: Boolean,
    safeTopPadding: Dp,
    safeBottomPadding: Dp,
    onBack: () -> Unit,
    onOpenBrowserUrl: (String) -> Unit,
    onOpenSupportEmail: () -> Unit
) {
    val status = uiState.licenseStatus
    val visuals = remember(status, uiState.licenseInfo) {
        resolveAppleProVisuals(status, uiState.licenseInfo)
    }
    val firstName = remember(uiState.greetingUserName) {
        extractCleanFirstName(uiState.greetingUserName)
    }
    val lifetimePlan = remember(uiState.availablePlans) {
        uiState.availablePlans.firstOrNull { it.id == "pro_lifetime" }
    }

    var isKeyDrawerExpanded by remember { mutableStateOf(false) }

    // Coreografia de entrada em cascata (120Hz via graphicsLayer, zero recomposição)
    val motion = Tessera.motion
    val stage0 = remember { Animatable(0f) }
    val stage1 = remember { Animatable(0f) }
    val stage2 = remember { Animatable(0f) }
    val stage3 = remember { Animatable(0f) }
    val stage4 = remember { Animatable(0f) }

    LaunchedEffect(status.tier) {
        stage0.snapTo(0f)
        stage1.snapTo(0f)
        stage2.snapTo(0f)
        stage3.snapTo(0f)
        stage4.snapTo(0f)
        launch { stage0.animateTo(1f, motion.springSpatial) }
        launch { delay(75L); stage1.animateTo(1f, motion.springSpatial) }
        launch { delay(150L); stage2.animateTo(1f, motion.springSpatial) }
        launch { delay(225L); stage3.animateTo(1f, motion.springSpatial) }
        launch { delay(300L); stage4.animateTo(1f, motion.springSpatial) }
    }

    val headlineMetalBrush = remember(isLight, visuals.isMaster) {
        when {
            visuals.isMaster && isLight -> Brush.linearGradient(
                colors = listOf(Color(0xFF3730A3), Color(0xFF4F46E5), Color(0xFF0284C7))
            )
            visuals.isMaster -> Brush.linearGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFA5B4FC), Color(0xFF7DD3FC))
            )
            isLight -> Brush.linearGradient(
                colors = listOf(Color(0xFF9A6F1F), Color(0xFFC89B3C), Color(0xFF7C5616))
            )
            else -> Brush.linearGradient(
                colors = listOf(Color(0xFFFFFDF5), Color(0xFFF3D58D), Color(0xFFD6AD54))
            )
        }
    }

    val eyebrowAccent = when {
        visuals.isMaster -> Color(0xFF6E6BF0)
        isLight -> Color(0xFFC89B3C)
        else -> Color(0xFFE5C068)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fundo atmosférico Apple Mesh Gradient (edge-to-edge atrás da barra de status)
        AppleProAmbientMeshBackdrop(
            screenBg = bg,
            accentColor = uiState.activeAccentColor,
            isMaster = visuals.isMaster,
            isLight = isLight
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = safeTopPadding, bottom = safeBottomPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar Cupertino Minimalista
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color(0xCCEFECE2) else Color(0x991C1C24))
                        .border(
                            BorderStroke(1.dp, if (isLight) Color(0x221C1917) else Color(0x22FFFFFF)),
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        )
                        .tesseraPressFeedback(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = if (visuals.isMaster) "TESSERA MASTER" else "TESSERA PRO",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.4.sp
                    ),
                    color = textSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )

                Spacer(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 1. HERO MEDALHÃO & PÍLULA EYEBROW
            // ==========================================
            Column(
                modifier = Modifier.appleCascadeReveal(stage0.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppleProMedallion(
                    isMaster = visuals.isMaster,
                    isLight = isLight,
                    isAmoled = isAmoled
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isLight) Color(0xD9EFECE2) else Color(0xB316161E))
                        .border(
                            BorderStroke(1.dp, eyebrowAccent.copy(alpha = if (isLight) 0.35f else 0.32f)),
                            CircleShape
                        )
                        .padding(horizontal = 13.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLight) Color(0xFF15803D) else Color(0xFF30D158))
                    )
                    Text(
                        text = visuals.eyebrowLabel,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 2. TIPOGRAFIA EDITORIAL CUPERTINO (AGRADECIMENTO)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .appleCascadeReveal(stage1.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (firstName != null) {
                    Text(
                        text = "Obrigado, $firstName.",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 35.sp,
                            letterSpacing = (-1.0).sp,
                            color = textPrimary
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (visuals.isMaster) "Você tem acesso Master." else "Você já é Tessera Pro.",
                        style = TextStyle(
                            brush = headlineMetalBrush,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp,
                            letterSpacing = (-1.1).sp
                        ),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Você já é",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 31.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 36.sp,
                            letterSpacing = (-1.0).sp,
                            color = textPrimary
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = visuals.heroAccentWord,
                        style = TextStyle(
                            brush = headlineMetalBrush,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 41.sp,
                            letterSpacing = (-1.2).sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = visuals.thankYouBody,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ==========================================
            // 3. CARTÃO "APPLE CARD TITANIUM" DA ASSINATURA
            // ==========================================
            AppleProMembershipCard(
                status = status,
                licenseInfo = uiState.licenseInfo,
                visuals = visuals,
                greetingUserName = uiState.greetingUserName,
                isLight = isLight,
                isAmoled = isAmoled,
                modifier = Modifier.appleCascadeReveal(stage2.value)
            )

            Spacer(modifier = Modifier.height(26.dp))

            // ==========================================
            // 4. BENTO GRID COMPACTO DE PRIVILÉGIOS PRO
            // ==========================================
            AppleProPrivilegesBento(
                isMaster = visuals.isMaster,
                isLight = isLight,
                cardBg = cardBg,
                cardBorder = cardBorder,
                modifier = Modifier.appleCascadeReveal(stage3.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 5. RODAPÉ DISCRETO: UPGRADE, GERENCIAMENTO & SUPORTE VIP
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .appleCascadeReveal(stage4.value)
            ) {
                if (!status.isPermanentPro) {
                    AppleProLifetimeUpgradeCard(
                        lifetimePriceLabel = lifetimePlan?.priceFormatted ?: BillingConfig.LIFETIME_PRICE_LABEL,
                        isLight = isLight,
                        cardBg = cardBg,
                        onClick = { viewModel.openCheckoutModal("pro_lifetime") }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }

                Text(
                    text = "GERENCIAR & SUPORTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    ),
                    color = textTertiary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.licenseInfo.isCurrentlyValid()) {
                            AppleProGroupedActionRow(
                                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                                title = "Recibo e assinatura online",
                                subtitle = "Ver detalhes da compra ou desvincular aparelho",
                                isLight = isLight,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                onClick = { viewModel.openCheckoutModal() }
                            )
                            HorizontalDivider(
                                color = cardBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(start = 66.dp, end = 16.dp)
                            )
                        }

                        // Gaveta expansível discreta para troca de código offline
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { isKeyDrawerExpanded = !isKeyDrawerExpanded }
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF181822)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VpnKey,
                                    contentDescription = null,
                                    tint = textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Trocar chave ou ativar código",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Inserir nova chave TESS-XXXX ou licença vitalícia",
                                    fontSize = 11.5.sp,
                                    color = textSecondary
                                )
                            }
                            Icon(
                                imageVector = if (isKeyDrawerExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isKeyDrawerExpanded,
                            enter = expandVertically(tween(240, easing = FastOutSlowInEasing)) + fadeIn(tween(220)),
                            exit = shrinkVertically(tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(150))
                        ) {
                            CompactKeyActivationForm(
                                viewModel = viewModel,
                                isLight = isLight,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                            )
                        }

                        HorizontalDivider(
                            color = cardBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 66.dp, end = 16.dp)
                        )

                        AppleProGroupedActionRow(
                            icon = Icons.Outlined.SupportAgent,
                            title = "Suporte prioritário via WhatsApp",
                            subtitle = "Atendimento direto para assinantes Pro",
                            isLight = isLight,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = {
                                val waUrl = BillingConfig.buildWhatsAppPurchaseUrl(
                                    planTitle = "Suporte Pro (${status.activationTypeLabel})",
                                    priceLabel = "Assinante Ativo"
                                )
                                onOpenBrowserUrl(waUrl)
                            }
                        )

                        HorizontalDivider(
                            color = cardBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 66.dp, end = 16.dp)
                        )

                        AppleProGroupedActionRow(
                            icon = Icons.Outlined.Email,
                            title = "Suporte por E-mail",
                            subtitle = BillingConfig.SUPPORT_EMAIL,
                            isLight = isLight,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onOpenSupportEmail
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Assinatura silenciosa estilo Apple
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Obrigado por apoiar o software independente.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary.copy(alpha = 0.80f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Sem anúncios • Sem rastreio • 100% privado",
                        fontSize = 11.sp,
                        color = textTertiary.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

private fun Modifier.appleCascadeReveal(progress: Float): Modifier = this.graphicsLayer {
    val p = progress.coerceIn(0f, 1f)
    alpha = p
    translationY = (1f - p) * 26.dp.toPx()
    val s = 0.965f + 0.035f * p
    scaleX = s
    scaleY = s
}

@Composable
private fun AppleProGroupedActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLight: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF181822)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = textSecondary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun CompactKeyActivationForm(
    viewModel: MainViewModel,
    isLight: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputKey by remember { mutableStateOf("") }
    var activationFeedback by remember { mutableStateOf<String?>(null) }
    var isActivationSuccess by remember { mutableStateOf(false) }

    fun submitKey() {
        keyboardController?.hide()
        if (inputKey.isBlank()) return
        when (val res = viewModel.activateLicenseKey(inputKey)) {
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

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF14141C))
                .border(
                    BorderStroke(1.dp, if (isLight) Color(0xFFDDD8CB) else Color(0xFF262634)),
                    RoundedCornerShape(14.dp)
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
                                fontSize = 13.5.sp,
                                color = textSecondary.copy(alpha = 0.50f)
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
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        ),
                        cursorBrush = SolidColor(if (isLight) Color.Black else Color.White),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitKey() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color(0xFFDFDACD) else Color(0xFF222230))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                                if (clip.isNotBlank()) {
                                    inputKey = LicenseManager.normalizeKeyInput(clip)
                                }
                            }
                        )
                        .tesseraPressFeedback(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentPaste,
                        contentDescription = "Colar",
                        tint = textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (isLight) Color(0xFF1C1917) else Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { submitKey() }
                )
                .tesseraPressFeedback(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Validar e Ativar Código",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLight) Color.White else Color.Black
            )
        }

        activationFeedback?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isActivationSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = if (isActivationSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = msg,
                    fontSize = 11.5.sp,
                    color = if (isActivationSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Vitrine comercial padrão (exibida para usuários Free ou durante o período de Trial de 72h).
 */
@Composable
private fun StandardLicenseStorefront(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    bg: Color,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    isLight: Boolean,
    safeTopPadding: Dp,
    safeBottomPadding: Dp,
    onBack: () -> Unit,
    openBrowserUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val status = uiState.licenseStatus

    var inputKey by remember { mutableStateOf("") }
    var activationFeedback by remember { mutableStateOf<String?>(null) }
    var isActivationSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(top = safeTopPadding, bottom = safeBottomPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Botão Voltar + Título
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF181820))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        )
                        .tesseraPressFeedback(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "LICENÇA & PRO",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = textSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 1. HERO STATUS CARD
            // ==========================================
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (status.hasProAccess) Color(0xFF10B981).copy(alpha = 0.16f)
                                        else Color(0xFFEF4444).copy(alpha = 0.16f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (status.hasProAccess) Icons.Outlined.Shield else Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = if (status.hasProAccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = status.activationTypeLabel,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = if (status.hasProAccess) "Acesso Pro Ativo" else "Acesso Freemium",
                                    fontSize = 12.sp,
                                    color = if (status.hasProAccess) Color(0xFF10B981) else textSecondary
                                )
                            }
                        }

                        // Badge Pill do Plano
                        val badgeLabel = when (status.tier) {
                            LicenseTier.MASTER -> "MASTER"
                            LicenseTier.PRO_LIFETIME -> BillingConfig.LIFETIME_BADGE
                            LicenseTier.PRO_ANNUAL -> "ANUAL"
                            LicenseTier.PRO_SEMESTRAL -> "SEMESTRAL"
                            LicenseTier.PRO_MONTHLY -> "MENSAL"
                            LicenseTier.PRO_GIFT -> "CORTESIA"
                            LicenseTier.TRIAL -> "72H TRIAL"
                            LicenseTier.FREE -> "FREE"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLight) Color(0xFFE0DDD2) else Color(0xFF1E1E28))
                                .border(BorderStroke(1.dp, if (isLight) Color(0xFFD0CCC0) else Color(0xFF2E2E3C)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = textPrimary
                            )
                        }
                    }

                    if (status.tier == LicenseTier.TRIAL) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val progressFraction = (status.remainingMillis.toFloat() / BillingConfig.TRIAL_DURATION_MS).coerceIn(0f, 1f)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tempo restante de testes",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                            Text(
                                text = if (status.hoursRemaining > 24) {
                                    "${status.daysRemaining}d restantes"
                                } else {
                                    "${status.hoursRemaining}h restantes"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (progressFraction > 0.25f) uiState.activeAccentColor else Color(0xFFEF4444),
                            trackColor = if (isLight) Color(0xFFDDD8CB) else Color(0xFF242430)
                        )
                    } else if (status.activeKeyMasked != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        val isEmailLicense = status.activeKeyMasked.contains("@")
                        Text(
                            text = if (isEmailLicense) "E-mail vinculado: ${status.activeKeyMasked}" else "Chave ativa: ${status.activeKeyMasked}",
                            fontSize = 12.sp,
                            fontFamily = if (isEmailLicense) FontFamily.Default else FontFamily.Monospace,
                            color = textSecondary
                        )
                        if (uiState.licenseInfo.isCurrentlyValid()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gerenciar assinatura online →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = uiState.activeAccentColor,
                                modifier = Modifier.clickable { viewModel.openCheckoutModal() }
                            )
                        }
                    }

                    if (status.isClockTampered) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.WarningAmber, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Text(
                                text = "O relógio do dispositivo foi alterado. O acesso Pro permanece pausado até o horário real.",
                                fontSize = 11.5.sp,
                                color = Color(0xFFEF4444),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    if (status.isVaultTampered) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.ErrorOutline, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Adulteração de integridade detectada. Insira uma chave válida para restabelecer o acesso.",
                                fontSize = 11.5.sp,
                                color = Color(0xFFEF4444),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 2. ATIVAÇÃO DE CÓDIGO OFFLINE (TESS-...)
            // ==========================================
            Text(
                text = "ATIVAR COM CÓDIGO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = textTertiary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF14141B))
                            .border(BorderStroke(1.dp, if (isLight) Color(0xFFDDD8CB) else Color(0xFF242432)), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VpnKey,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (inputKey.isEmpty()) {
                                    Text(
                                        text = "TESS-XXXX-XXXX-XXXX",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 14.sp,
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
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    ),
                                    cursorBrush = SolidColor(if (isLight) Color.Black else Color.White),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
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
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Botão Colar
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isLight) Color(0xFFDFDACD) else Color(0xFF22222E))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                                            if (clip.isNotBlank()) {
                                                inputKey = LicenseManager.normalizeKeyInput(clip)
                                            }
                                        }
                                    )
                                    .tesseraPressFeedback(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentPaste,
                                    contentDescription = "Colar",
                                    tint = textPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isLight) Color(0xFF1C1917) else Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
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
                            )
                            .tesseraPressFeedback(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Validar e Ativar Código",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Color.White else Color.Black
                        )
                    }

                    activationFeedback?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isActivationSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = if (isActivationSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (isActivationSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 3. VITRINE DE PLANOS COMERCIAIS (MERCADO PAGO)
            // ==========================================
            Text(
                text = "PLANOS TESSERA PRO (PIX OU CARTÃO)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = textTertiary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            val lifetimePlan = uiState.availablePlans.firstOrNull { it.id == "pro_lifetime" }
            val monthlyPlan = uiState.availablePlans.firstOrNull { it.id == "pro_monthly" }

            // Plano 1: Vitalício (Destaque)
            PlanCard(
                title = lifetimePlan?.title ?: "Tessera Pro Vitalício",
                price = lifetimePlan?.priceFormatted ?: BillingConfig.LIFETIME_PRICE_LABEL,
                subtitle = lifetimePlan?.subtitle ?: "Pagamento único via Pix ou Cartão, acesso permanente",
                badge = lifetimePlan?.badge ?: BillingConfig.LIFETIME_BADGE,
                isHighlighted = true,
                isLightMode = isLight,
                onClick = { viewModel.openCheckoutModal("pro_lifetime") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Plano 2: Mensal
            PlanCard(
                title = monthlyPlan?.title ?: "Tessera Pro Mensal",
                price = monthlyPlan?.priceFormatted ?: BillingConfig.MONTHLY_PRICE_LABEL,
                subtitle = monthlyPlan?.subtitle ?: "Acesso completo por 30 dias via Pix ou Cartão",
                badge = monthlyPlan?.badge,
                isHighlighted = false,
                isLightMode = isLight,
                onClick = { viewModel.openCheckoutModal("pro_monthly") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openCheckoutModal() }
                    .tesseraPressFeedback()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Já pagou via Pix ou Cartão?",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            text = "Restaurar compra vinculada pelo seu e-mail",
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 4. TABELA COMPARATIVA (FREE vs PRO)
            // ==========================================
            Text(
                text = "O QUE ESTÁ INCLUÍDO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = textTertiary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    ComparisonRow("Abertura e busca de apps na gaveta", isFree = true, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Atalhos rápidos e fixação na Home", isFree = true, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Omni-Search (@calc, @con, cálculos)", isFree = true, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Modo Foco Zen e Temas Base", isFree = true, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Desbloqueio de apps já ocultos com PIN", isFree = true, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Criar, editar e gerenciar Pastas de Apps", isFree = false, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Ocultar novos apps com PIN e Biometria", isFree = false, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Tela Lateral (−1) Bento e Feed RSS", isFree = false, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Central de 11 Gestos Personalizados", isFree = false, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Customização Visual (Borrão, Dock, Cores)", isFree = false, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Modo StandBy Pro (Música, Eventos, Pomodoro, Notif.)", isFree = false, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Personalização e Criação de Comandos (@)", isFree = false, isPro = true, isLight = isLight)
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                    ComparisonRow("Backup e Restauração Completa (JSON)", isFree = false, isPro = true, isLight = isLight)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 5. ATENDIMENTO E SUPORTE DIRETO
            // ==========================================
            Text(
                text = "SUPORTE & CONTATO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = textTertiary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(22.dp),
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
                                onClick = {
                                    val waUrl = BillingConfig.buildWhatsAppPurchaseUrl("Suporte", "Dúvidas ou Pix")
                                    openBrowserUrl(waUrl)
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF191922)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.SupportAgent, null, tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Atendimento via WhatsApp",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = "Dúvidas, suporte ou pagamento direto via Pix",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = textSecondary, modifier = Modifier.size(16.dp))
                    }

                    HorizontalDivider(color = cardBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${BillingConfig.SUPPORT_EMAIL}")
                                            putExtra(Intent.EXTRA_SUBJECT, "Tessera Launcher — Suporte de Licença")
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color(0xFFE8E4D8) else Color(0xFF191922)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Email, null, tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Suporte por E-mail",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = BillingConfig.SUPPORT_EMAIL,
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = textSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    subtitle: String,
    badge: String?,
    isHighlighted: Boolean,
    isLightMode: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isLightMode) {
        if (isHighlighted) Color(0xFFFAF7F0) else LightCardBackground
    } else {
        if (isHighlighted) Color(0xFF14141D) else Color(0xFF0F0F14)
    }

    val cardBorder = if (isHighlighted) {
        Color(0xFFE2B714).copy(alpha = 0.55f)
    } else {
        if (isLightMode) LightCardBorder else Color(0xFF1E1E26)
    }

    val textPrimary = if (isLightMode) LightTextPrimary else TextPrimary
    val textSecondary = if (isLightMode) LightTextSecondary else TextSecondary

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .tesseraPressFeedback()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    badge?.let {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isHighlighted) Color(0xFFE2B714).copy(alpha = 0.20f)
                                    else if (isLightMode) Color(0xFFE0DDD2) else Color(0xFF22222E)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isHighlighted) Color(0xFFE2B714).copy(alpha = 0.50f)
                                        else if (isLightMode) Color(0xFFD0CCC0) else Color(0xFF333344)
                                    ),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = it,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighlighted) Color(0xFFE2B714) else textSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = price,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) Color(0xFFE2B714) else textPrimary
                )
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    feature: String,
    isFree: Boolean,
    isPro: Boolean,
    isLight: Boolean
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = feature,
            fontSize = 13.sp,
            color = textPrimary,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFree) "✓ Free" else "—",
                fontSize = 12.sp,
                color = if (isFree) Color(0xFF10B981) else textSecondary.copy(alpha = 0.4f),
                fontWeight = if (isFree) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = "✓ Pro",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE2B714)
            )
        }
    }
}
