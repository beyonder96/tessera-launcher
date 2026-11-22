package com.tessera.launcher.ui.components

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Cartão animado que demonstra visualmente como definir o Tessera como Lançador Padrão
 * no seletor nativo do Android e celebra com pulso elástico quando já está ativo.
 */
@Composable
fun DefaultLauncherAnimatedCard(
    isDefaultLauncher: Boolean,
    accent: Color,
    isLight: Boolean,
    onRequestDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val cardBg = if (isLight) LightCardBackground else Color(0xFF0F0F14)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF222230)
    val successGreen = Color(0xFF10B981)

    val celebrateScale = remember { Animatable(1f) }
    LaunchedEffect(isDefaultLauncher) {
        if (isDefaultLauncher) {
            celebrateScale.snapTo(0.94f)
            celebrateScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    val borderColor by animateColorAsState(
        targetValue = if (isDefaultLauncher) successGreen.copy(alpha = 0.55f) else accent.copy(alpha = 0.42f),
        animationSpec = tween(350),
        label = "default_launcher_border"
    )

    val transition = rememberInfiniteTransition(label = "default_launcher_demo")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "default_launcher_phase"
    )

    // Fase 0.0..0.45: destaque indo até o item Tessera; 0.45..0.85: check marcado; 0.85..1.0: reset suave
    val tesseraSelectedInDemo = isDefaultLauncher || phase >= 0.35f
    val showTapRipple = !isDefaultLauncher && phase in 0.25f..0.52f

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.2.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .scale(celebrateScale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onRequestDefault
            )
            .tesseraPressFeedback()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDefaultLauncher) successGreen.copy(alpha = 0.16f)
                            else accent.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDefaultLauncher) Icons.Outlined.Check else Icons.Outlined.Home,
                        contentDescription = null,
                        tint = if (isDefaultLauncher) successGreen else accent,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isDefaultLauncher) "Lançador Padrão Ativo" else "Definir como Lançador Padrão",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = if (isDefaultLauncher) {
                            "Ao tocar no botão Início, o Tessera abre na hora"
                        } else {
                            "Por quê: evita voltar para a tela inicial antiga ao sair de apps"
                        },
                        fontSize = 11.5.sp,
                        lineHeight = 15.5.sp,
                        color = textSecondary
                    )
                }

                if (!isDefaultLauncher) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLight) Color(0xFF1C1917) else Color.White)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Definir",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Color.White else Color.Black
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = successGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini simulação animada do diálogo nativo "App de Início Padrão" do Android
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isLight) Color(0xFFE7E2D4) else Color(0xFF15151E),
                border = BorderStroke(1.dp, if (isLight) Color(0xFFD7D1C0) else Color(0xFF252534)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SELETOR DO ANDROID • APP DE INÍCIO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = textSecondary.copy(alpha = 0.8f)
                    )

                    // Opção 1: Launcher antigo do sistema
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(
                                        1.5.dp,
                                        if (!tesseraSelectedInDemo) textSecondary else textSecondary.copy(alpha = 0.35f)
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!tesseraSelectedInDemo) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(textSecondary)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tela Inicial Padrão do Sistema",
                            fontSize = 11.sp,
                            color = textSecondary.copy(alpha = if (tesseraSelectedInDemo) 0.55f else 0.9f)
                        )
                    }

                    // Opção 2: Tessera Launcher (selecionado na animação)
                    val rowBg by animateColorAsState(
                        targetValue = if (tesseraSelectedInDemo) {
                            (if (isDefaultLauncher) successGreen else accent).copy(alpha = if (isLight) 0.14f else 0.18f)
                        } else Color.Transparent,
                        animationSpec = tween(260),
                        label = "tessera_demo_row_bg"
                    )
                    val activeDotColor = if (isDefaultLauncher) successGreen else accent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(rowBg)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (tesseraSelectedInDemo) activeDotColor.copy(alpha = 0.45f) else Color.Transparent
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (tesseraSelectedInDemo) activeDotColor else Color.Transparent)
                                        .border(
                                            BorderStroke(1.5.dp, activeDotColor),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tesseraSelectedInDemo) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = if (activeDotColor == Color.White) Color.Black else Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tessera Launcher",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }

                            if (showTapRipple) {
                                val rippleProgress = ((phase - 0.25f) / 0.27f).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .scale(0.8f + rippleProgress * 0.7f)
                                        .alpha(1f - rippleProgress)
                                        .clip(CircleShape)
                                        .background(accent.copy(alpha = 0.45f))
                                )
                            } else if (tesseraSelectedInDemo) {
                                Text(
                                    text = if (isDefaultLauncher) "Ativo ✓" else "Sempre",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeDotColor
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
 * Guia visual animado para desbloquear "Configurações Restritas" no Android 13/14/15/16
 * quando o APK foi instalado fora da Play Store (GitHub / download direto).
 */
@Composable
fun RestrictedSettingsAnimatedCard(
    accent: Color,
    isLight: Boolean,
    onOpenAppInfo: () -> Unit,
    onOpenTargetPermission: (() -> Unit)? = null,
    targetPermissionLabel: String? = null,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val warnAmber = if (isLight) Color(0xFFB45309) else Color(0xFFF59E0B)

    val transition = rememberInfiniteTransition(label = "restricted_settings_anim")
    val animPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "restricted_phase"
    )

    // 0.0..0.38: Pulso no botão 3 pontinhos (⋮)
    // 0.38..0.78: Menu abre e destaca "Permitir configurações restritas"
    // 0.78..1.0: Check de liberado
    val isMenuOpen = animPhase >= 0.36f
    val isAllowedChecked = animPhase >= 0.74f

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isLight) Color(0xFFF3ECDA) else Color(0xFF15131C),
        border = BorderStroke(1.dp, warnAmber.copy(alpha = if (isLight) 0.42f else 0.38f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(warnAmber.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = warnAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Android bloqueou a permissão?",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Proteção de \"Configurações Restritas\" do Android 13+",
                        fontSize = 11.sp,
                        color = warnAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Por segurança, o Android restringe Acessibilidade e Notificações em apps instalados fora da Play Store. Para liberar em 10 segundos:",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ilustração animada da tela "Informações do App" com menu ⋮ → Permitir configurações restritas
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isLight) Color(0xFFEAE4D4) else Color(0xFF1C1B26),
                border = BorderStroke(1.dp, if (isLight) Color(0xFFD5CEBC) else Color(0xFF2D2B3D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Barra simulada de Informações do App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(accent.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Informações do app",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Tessera Launcher",
                                    fontSize = 9.5.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        // Botão 3 pontinhos (⋮) com halo pulsante na fase 1
                        Box(
                            modifier = Modifier.size(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isMenuOpen) {
                                val pulse = (animPhase / 0.36f).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .scale(0.8f + pulse * 0.55f)
                                        .alpha(1f - pulse)
                                        .clip(CircleShape)
                                        .background(warnAmber.copy(alpha = 0.5f))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (!isMenuOpen) warnAmber.copy(alpha = 0.22f)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = "Menu de 3 pontinhos",
                                    tint = if (!isMenuOpen) warnAmber else textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Dropdown animado "Permitir configurações restritas"
                    val menuAlpha by animateFloatAsState(
                        targetValue = if (isMenuOpen) 1f else 0f,
                        animationSpec = tween(220),
                        label = "restricted_menu_alpha"
                    )
                    val menuScale by animateFloatAsState(
                        targetValue = if (isMenuOpen) 1f else 0.90f,
                        animationSpec = tween(220),
                        label = "restricted_menu_scale"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isLight) Color.White else Color(0xFF282738),
                        border = BorderStroke(
                            1.dp,
                            if (isAllowedChecked) Color(0xFF10B981).copy(alpha = 0.65f) else warnAmber.copy(alpha = 0.55f)
                        ),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .alpha(menuAlpha)
                            .scale(menuScale)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isAllowedChecked) Icons.Outlined.CheckCircle else Icons.Outlined.Security,
                                contentDescription = null,
                                tint = if (isAllowedChecked) Color(0xFF10B981) else warnAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isAllowedChecked) "Configurações restritas liberadas!" else "Permitir configurações restritas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }

                    // Dica do passo atual no canto inferior esquerdo
                    Text(
                        text = when {
                            isAllowedChecked -> "Pronto! Volte e ative"
                            isMenuOpen -> "2. Toque na opção"
                            else -> "1. Toque em ⋮ no topo"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAllowedChecked) Color(0xFF10B981) else textSecondary,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botões de ação guiados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLight) Color(0xFF1C1917) else Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenAppInfo
                        )
                        .tesseraPressFeedback()
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text(
                            text = if (onOpenTargetPermission != null) "1. Abrir Info do App (⋮)" else "Abrir Info do App (⋮)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Color.White else Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (onOpenTargetPermission != null && !targetPermissionLabel.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accent.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenTargetPermission
                            )
                            .tesseraPressFeedback()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "2. $targetPermissionLabel",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (accent == Color.White && isLight) LightTextPrimary else accent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = if (accent == Color.White && isLight) LightTextPrimary else accent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog guiado para ajudar o usuário antes de abrir Acessibilidade ou Notificações no Android 13+.
 */
@Composable
fun RestrictedSettingsGuideDialog(
    permissionName: String,
    accent: Color,
    isLight: Boolean,
    onOpenAppInfo: () -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val containerBg = if (isLight) Color(0xFFF7F4EB) else Color(0xFF0E0E14)
    val borderColor = if (isLight) LightCardBorder else Color(0xFF262634)
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = containerBg,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Liberar $permissionName",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Se a chave aparecer cinza/bloqueada no Android, faça primeiro o Passo 1 e depois o Passo 2.",
                        fontSize = 12.sp,
                        color = textSecondary,
                        lineHeight = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    RestrictedSettingsAnimatedCard(
                        accent = accent,
                        isLight = isLight,
                        onOpenAppInfo = onOpenAppInfo,
                        onOpenTargetPermission = {
                            onDismiss()
                            onOpenPermissionSettings()
                        },
                        targetPermissionLabel = "Ativar Agora"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Fechar",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Header de progresso animado para telas de permissões ("X de Y permissões ativas").
 */
@Composable
fun AnimatedPermissionsProgressCard(
    grantedCount: Int,
    totalCount: Int,
    accent: Color,
    isLight: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val cardBg = if (isLight) LightCardBackground else Color(0xFF101016)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF20202C)
    val successGreen = Color(0xFF10B981)
    val allGranted = grantedCount >= totalCount && totalCount > 0

    val targetProgress = if (totalCount > 0) grantedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "permissions_progress"
    )
    val ringColor by animateColorAsState(
        targetValue = if (allGranted) successGreen else if (accent == Color.White && isLight) Color(0xFF1C1917) else accent,
        animationSpec = tween(350),
        label = "permissions_ring_color"
    )
    val trackColor = if (isLight) Color(0xFFDAD4C4) else Color(0xFF242432)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, if (allGranted) successGreen.copy(alpha = 0.45f) else cardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 5.dp.toPx()
                    val diameter = size.minDimension - stroke
                    val topLeft = Offset(stroke / 2f, stroke / 2f)
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(ringColor.copy(alpha = 0.7f), ringColor, ringColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$grantedCount/$totalCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (allGranted) "Tudo Pronto!" else "$grantedCount de $totalCount permissões ativas",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (allGranted) {
                        "Todos os widgets, gestos e recursos do Tessera estão liberados."
                    } else {
                        "Ative somente o que você deseja usar. Nenhum dado sai do aparelho."
                    },
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = textSecondary
                )
            }
        }
    }
}
