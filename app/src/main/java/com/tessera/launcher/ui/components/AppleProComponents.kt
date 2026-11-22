package com.tessera.launcher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.R
import com.tessera.launcher.data.license.BillingConfig
import com.tessera.launcher.data.license.LicenseStatus
import com.tessera.launcher.data.license.LicenseTier
import com.tessera.launcher.data.model.LicenseInfo
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.theme.appleLiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.appleLiquidGlassSurfaceBrush
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val CupertinoChampagne = Color(0xFFE5C068)
private val CupertinoGoldWarm = Color(0xFFC89B3C)
private val CupertinoSky = Color(0xFF5AC8FA)
private val CupertinoIndigo = Color(0xFF6E6BF0)
private val CupertinoEmerald = Color(0xFF30D158)
private val CupertinoEmeraldLight = Color(0xFF15803D)

@Immutable
data class AppleProPlanVisuals(
    val tierBadge: String,
    val eyebrowLabel: String,
    val heroAccentWord: String,
    val thankYouBody: String,
    val validitySummary: String,
    val progressFraction: Float?,
    val bannerTitle: String,
    val isMaster: Boolean
)

@Immutable
private data class BentoPrivilegeItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val tintDark: Color,
    val tintLight: Color
)

/**
 * Extrai o primeiro nome limpo para personalização editorial da saudação.
 */
fun extractCleanFirstName(rawName: String): String? {
    val first = rawName.trim().split(Regex("\\s+")).firstOrNull()?.trim().orEmpty()
    return first.takeIf { it.isNotBlank() }?.take(18)
}

/**
 * Resolve textos, selos e informações de validade da experiência Apple Pro Membership
 * de forma pura e determinística a partir do status de licença.
 */
fun resolveAppleProVisuals(
    status: LicenseStatus,
    licenseInfo: LicenseInfo
): AppleProPlanVisuals {
    val isMaster = status.tier == LicenseTier.MASTER
    val tierBadge = when (status.tier) {
        LicenseTier.MASTER -> "MASTER"
        LicenseTier.PRO_LIFETIME -> BillingConfig.LIFETIME_BADGE
        LicenseTier.PRO_ANNUAL -> "ANUAL"
        LicenseTier.PRO_SEMESTRAL -> "SEMESTRAL"
        LicenseTier.PRO_MONTHLY -> "MENSAL"
        LicenseTier.PRO_GIFT -> "CORTESIA"
        LicenseTier.TRIAL -> "72H TRIAL"
        LicenseTier.FREE -> "FREE"
    }

    val eyebrowLabel = when (status.tier) {
        LicenseTier.MASTER -> "ACESSO MASTER VERIFICADO"
        LicenseTier.PRO_LIFETIME -> "MEMBRO FUNDADOR • VITALÍCIO"
        LicenseTier.PRO_GIFT -> "PASSE CORTESIA PRO ATIVO"
        else -> "ASSINATURA PRO VERIFICADA"
    }

    val heroAccentWord = if (isMaster) "Tessera Master." else "Tessera Pro."

    val thankYouBody = when (status.tier) {
        LicenseTier.MASTER ->
            "Obrigado por construir e moldar o Tessera. Todos os recursos Pro, motores de IA e ferramentas avançadas estão liberados sem limites."
        LicenseTier.PRO_LIFETIME ->
            "Obrigado por acreditar no Tessera. Seu apoio mantém um launcher rápido, privado, sem anúncios e feito à mão — desbloqueado para sempre."
        LicenseTier.PRO_GIFT ->
            "Aproveite a experiência completa do Tessera Pro. Obrigado por fazer parte da nossa comunidade e explorar cada detalhe com a gente."
        else ->
            "Muito obrigado por assinar o Tessera Pro. Seu apoio torna possível o desenvolvimento independente, contínuo e 100% livre de anúncios."
    }

    val bannerTitle = if (isMaster) "Você tem Tessera Master" else "Você já é Tessera Pro"

    val expiryTimestamp = status.expiresAtMs ?: licenseInfo.expiresAt.takeIf { it > 0L }
    val validitySummary = when {
        status.isPermanentPro -> "Acesso permanente • Sem expiração"
        expiryTimestamp != null && expiryTimestamp > 0L -> {
            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
            val dateStr = fmt.format(Date(expiryTimestamp))
            val remainingPart = when {
                status.daysRemaining > 1 -> "${status.daysRemaining} dias restantes"
                status.daysRemaining == 1 -> "1 dia restante"
                status.hoursRemaining > 0 -> "${status.hoursRemaining}h restantes"
                else -> "Ativo"
            }
            "Válido até $dateStr • $remainingPart"
        }
        status.daysRemaining > 0 -> "Ativo • ${status.daysRemaining} dias restantes"
        status.hoursRemaining > 0 -> "Ativo • ${status.hoursRemaining}h restantes"
        else -> "Todos os recursos Pro desbloqueados"
    }

    val totalDurationDays = when (status.tier) {
        LicenseTier.PRO_MONTHLY -> 30f
        LicenseTier.PRO_SEMESTRAL -> 180f
        LicenseTier.PRO_ANNUAL -> 365f
        LicenseTier.PRO_GIFT -> BillingConfig.GIFT_MAX_DAYS.toFloat()
        else -> null
    }
    val progressFraction = if (!status.isPermanentPro && totalDurationDays != null && status.remainingMillis > 0L) {
        val totalMs = totalDurationDays * 24f * 60f * 60f * 1000f
        (status.remainingMillis.toFloat() / totalMs).coerceIn(0.04f, 1f)
    } else {
        null
    }

    return AppleProPlanVisuals(
        tierBadge = tierBadge,
        eyebrowLabel = eyebrowLabel,
        heroAccentWord = heroAccentWord,
        thankYouBody = thankYouBody,
        validitySummary = validitySummary,
        progressFraction = progressFraction,
        bannerTitle = bannerTitle,
        isMaster = isMaster
    )
}

/**
 * Banner Estilo Apple One / iCloud+ exibido no Hub de Configurações quando o usuário já é Pro.
 */
@Composable
fun AppleProSettingsBanner(
    status: LicenseStatus,
    licenseInfo: LicenseInfo,
    greetingUserName: String,
    accentColor: Color,
    isLight: Boolean,
    isAmoled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visuals = remember(status, licenseInfo) { resolveAppleProVisuals(status, licenseInfo) }
    val firstName = remember(greetingUserName) { extractCleanFirstName(greetingUserName) }
    val shape = RoundedCornerShape(26.dp)

    val transition = rememberInfiniteTransition(label = "settings_pro_banner_glow")
    val glowPulse by transition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_glow_pulse"
    )

    val primaryText = if (isLight) LightTextPrimary else TextPrimary
    val secondaryText = if (isLight) LightTextSecondary else TextSecondary
    val heroTint = when {
        visuals.isMaster -> CupertinoIndigo
        isLight -> CupertinoGoldWarm
        else -> CupertinoChampagne
    }
    val secondaryGlowTint = remember(accentColor, isLight, visuals.isMaster) {
        when {
            visuals.isMaster -> CupertinoSky
            accentColor == Color.White -> CupertinoSky
            else -> accentColor
        }
    }

    val surfaceBrush = remember(isLight, isAmoled) {
        when {
            isLight -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFDF8),
                    Color(0xFFF3EFE4),
                    Color(0xFFECE6D8)
                )
            )
            isAmoled -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF17171D),
                    Color(0xFF0E0E12),
                    Color(0xFF07070A)
                )
            )
            else -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF23232B),
                    Color(0xFF191920),
                    Color(0xFF14141A)
                )
            )
        }
    }

    val borderBrush = remember(isLight, heroTint) {
        Brush.linearGradient(
            colors = listOf(
                if (isLight) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.24f),
                heroTint.copy(alpha = if (isLight) 0.42f else 0.34f),
                if (isLight) Color(0xFFD5CEBE) else Color.White.copy(alpha = 0.08f)
            )
        )
    }

    val subtitleText = remember(firstName, visuals.isMaster, status.activationTypeLabel) {
        when {
            firstName != null -> "Obrigado pelo apoio, $firstName • ${status.activationTypeLabel}"
            visuals.isMaster -> "Obrigado por criar o Tessera • Acesso ilimitado ativo"
            else -> "Obrigado por apoiar o Tessera • ${status.activationTypeLabel}"
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(surfaceBrush)
            .border(BorderStroke(1.dp, borderBrush), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .tesseraPressFeedback()
    ) {
        // Aura radial Cupertino sutil no canto direito superior
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        heroTint.copy(alpha = (if (isLight) 0.16f else 0.18f) * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.12f),
                    radius = w * 0.52f
                ),
                radius = w * 0.52f,
                center = Offset(w * 0.88f, h * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryGlowTint.copy(alpha = (if (isLight) 0.08f else 0.10f) * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.95f),
                    radius = w * 0.42f
                ),
                radius = w * 0.42f,
                center = Offset(w * 0.15f, h * 0.95f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medalhão squircle estilo Apple
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                heroTint.copy(alpha = if (isLight) 0.22f else 0.26f),
                                heroTint.copy(alpha = if (isLight) 0.08f else 0.10f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.dp, heroTint.copy(alpha = if (isLight) 0.42f else 0.48f)),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (visuals.isMaster) Icons.Outlined.AutoAwesome else Icons.Outlined.Verified,
                    contentDescription = "Assinatura Pro Ativa",
                    tint = heroTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = visuals.bannerTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        color = primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(heroTint.copy(alpha = if (isLight) 0.15f else 0.18f))
                            .border(
                                BorderStroke(0.8.dp, heroTint.copy(alpha = 0.45f)),
                                CircleShape
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = visuals.tierBadge,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = if (isLight) CupertinoGoldWarm else heroTint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    color = secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color(0x121C1917) else Color(0x16FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = secondaryText,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * Fundo atmosférico estilo Apple Mesh Gradient (3 halos orgânicos em órbita lenta).
 * Desenhado inteiramente no Canvas para 120Hz sem recomposições de layout.
 */
@Composable
fun AppleProAmbientMeshBackdrop(
    screenBg: Color,
    accentColor: Color,
    isMaster: Boolean,
    isLight: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "apple_mesh_aura")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mesh_phase"
    )

    val primaryAura = when {
        isMaster -> CupertinoIndigo
        isLight -> CupertinoGoldWarm
        else -> CupertinoChampagne
    }
    val secondaryAura = if (isMaster) CupertinoSky else CupertinoSky
    val tertiaryAura = remember(accentColor, isLight, isMaster) {
        when {
            isMaster -> Color(0xFFA78BFA)
            accentColor == Color.White -> Color(0xFFE5E5EA)
            else -> accentColor
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val topStageHeight = h * 0.56f

        val orb1Center = Offset(
            x = w * (0.50f + 0.18f * cos(phase)),
            y = topStageHeight * (0.28f + 0.08f * sin(phase))
        )
        val orb2Center = Offset(
            x = w * (0.24f + 0.14f * sin(phase * 0.8f + 1.2f)),
            y = topStageHeight * (0.42f + 0.10f * cos(phase * 0.8f))
        )
        val orb3Center = Offset(
            x = w * (0.78f + 0.12f * cos(phase + 2.4f)),
            y = topStageHeight * (0.48f + 0.09f * sin(phase + 2.4f))
        )

        val alphaPrimary = if (isLight) 0.15f else 0.17f
        val alphaSecondary = if (isLight) 0.10f else 0.12f
        val alphaTertiary = if (isLight) 0.08f else 0.10f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryAura.copy(alpha = alphaPrimary), Color.Transparent),
                center = orb1Center,
                radius = w * 0.72f
            ),
            radius = w * 0.72f,
            center = orb1Center
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryAura.copy(alpha = alphaSecondary), Color.Transparent),
                center = orb2Center,
                radius = w * 0.62f
            ),
            radius = w * 0.62f,
            center = orb2Center
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tertiaryAura.copy(alpha = alphaTertiary), Color.Transparent),
                center = orb3Center,
                radius = w * 0.58f
            ),
            radius = w * 0.58f,
            center = orb3Center
        )

        // Transição suave da aura para a cor exata de fundo da tela
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.46f to screenBg.copy(alpha = 0.55f),
                    0.72f to screenBg,
                    1.0f to screenBg
                )
            )
        )
    }
}

/**
 * Medalhão metálico central estilo Apple Watch / Apple Pay com anel especular rotativo
 * e respiração orgânica suave.
 */
@Composable
fun AppleProMedallion(
    isMaster: Boolean,
    isLight: Boolean,
    isAmoled: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "apple_medallion")
    val ringRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "medallion_ring_rotation"
    )
    val breatheScale by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "medallion_breathe"
    )

    val heroTint = when {
        isMaster -> CupertinoIndigo
        isLight -> CupertinoGoldWarm
        else -> CupertinoChampagne
    }
    val secondaryTint = if (isMaster) CupertinoSky else Color(0xFFFFF1C1)

    val glassSurface = remember(isLight, isAmoled) {
        appleLiquidGlassSurfaceBrush(isLightMode = isLight, isAmoledMode = isAmoled, alphaFactor = 1f)
    }
    val glassBorder = remember(isLight) {
        appleLiquidGlassBorderBrush(isLightMode = isLight, alphaFactor = 1f)
    }

    Box(
        modifier = modifier
            .size(116.dp)
            .graphicsLayer {
                scaleX = breatheScale
                scaleY = breatheScale
            },
        contentAlignment = Alignment.Center
    ) {
        // Anéis concêntricos, brilho especular giratório e partículas orbitais serenas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width * 0.46f
            val innerHaloRadius = size.width * 0.39f

            // Halo suave central
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        heroTint.copy(alpha = if (isLight) 0.24f else 0.28f),
                        heroTint.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.50f
                ),
                radius = size.width * 0.50f,
                center = center
            )

            // Anel externo ultra-fino estático
            drawCircle(
                color = if (isLight) Color(0x221C1917) else Color(0x1EFFFFFF),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Arco especular metálico giratório (Estilo Apple Pay / Pro Ring)
            rotate(degrees = ringRotation, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.22f to heroTint.copy(alpha = 0.12f),
                            0.42f to secondaryTint.copy(alpha = if (isLight) 0.72f else 0.92f),
                            0.52f to heroTint.copy(alpha = 0.85f),
                            0.68f to Color.Transparent,
                            1.00f to Color.Transparent
                        ),
                        center = center
                    ),
                    radius = innerHaloRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3 micro-pontos orbitais delicados
            for (i in 0..2) {
                val angleDeg = ringRotation * (if (i == 1) -0.6f else 0.8f) + i * 120f
                val rad = Math.toRadians(angleDeg.toDouble())
                val orbitR = if (i == 1) outerRadius else innerHaloRadius + 4.dp.toPx()
                val px = center.x + (orbitR * cos(rad)).toFloat()
                val py = center.y + (orbitR * sin(rad)).toFloat()
                drawCircle(
                    color = (if (i == 0) secondaryTint else heroTint).copy(alpha = if (isLight) 0.65f else 0.80f),
                    radius = if (i == 0) 2.2.dp.toPx() else 1.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Núcleo em Vidro Líquido Cupertino
        Box(
            modifier = Modifier
                .size(74.dp)
                .shadow(
                    elevation = if (isLight) 12.dp else 20.dp,
                    shape = CircleShape,
                    spotColor = heroTint.copy(alpha = 0.45f),
                    ambientColor = heroTint.copy(alpha = 0.25f)
                )
                .clip(CircleShape)
                .background(glassSurface)
                .border(BorderStroke(1.2.dp, glassBorder), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMaster) Icons.Outlined.AutoAwesome else Icons.Outlined.Verified,
                contentDescription = "Tessera Pro",
                tint = heroTint,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

/**
 * Cartão "Apple Card Titanium" da Assinatura Ativa, com reflexo especular diagonal periódico
 * (Specular Shimmer Sweep), borda luminosa superior e informações da credencial.
 */
@Composable
fun AppleProMembershipCard(
    status: LicenseStatus,
    licenseInfo: LicenseInfo,
    visuals: AppleProPlanVisuals,
    greetingUserName: String,
    isLight: Boolean,
    isAmoled: Boolean,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(28.dp)
    val transition = rememberInfiniteTransition(label = "apple_card_shimmer")

    // Passagem elegante de brilho diagonal (0..1 atravessa o cartão, 1..2.6 fica em repouso calmo)
    val shimmerProgress by transition.animateFloat(
        initialValue = -0.55f,
        targetValue = 2.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "card_shimmer_sweep"
    )
    val statusDotPulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_dot_pulse"
    )

    val heroTint = when {
        visuals.isMaster -> CupertinoIndigo
        isLight -> CupertinoGoldWarm
        else -> CupertinoChampagne
    }
    val emeraldColor = if (isLight) CupertinoEmeraldLight else CupertinoEmerald
    val cardTextPrimary = if (isLight) Color(0xFF1C1917) else Color(0xFFF5F5F7)
    val cardTextSecondary = if (isLight) Color(0xFF57534E) else Color(0xFFA1A1AA)
    val cardTextMuted = if (isLight) Color(0xFF78716C) else Color(0xFF71717A)

    // Superfície Titânio Escovado (Apple Card Light Pearl / Dark Titanium)
    val titaniumBrush = remember(isLight, isAmoled, visuals.isMaster) {
        when {
            isLight -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF6F3EB),
                    Color(0xFFEBE5D7)
                )
            )
            visuals.isMaster -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1B1B28),
                    Color(0xFF111119),
                    Color(0xFF0A0A10)
                )
            )
            isAmoled -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1A1A1F),
                    Color(0xFF101014),
                    Color(0xFF09090C)
                )
            )
            else -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF24242B),
                    Color(0xFF18181E),
                    Color(0xFF121217)
                )
            )
        }
    }

    val rimLightBorder = remember(isLight, heroTint) {
        Brush.verticalGradient(
            colors = listOf(
                if (isLight) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.30f),
                heroTint.copy(alpha = if (isLight) 0.36f else 0.28f),
                if (isLight) Color(0xFFD2CBB9) else Color.White.copy(alpha = 0.06f)
            )
        )
    }

    val firstName = remember(greetingUserName) { extractCleanFirstName(greetingUserName) }
    val credentialLabel = remember(status.activeKeyMasked, licenseInfo.licensedEmail) {
        when {
            !licenseInfo.licensedEmail.isNullOrBlank() -> "CONTA VINCULADA"
            status.activeKeyMasked?.contains("@") == true -> "CONTA VINCULADA"
            !status.activeKeyMasked.isNullOrBlank() -> "CHAVE VERIFICADA"
            else -> "DISPOSITIVO"
        }
    }
    val credentialValue = remember(status.activeKeyMasked, licenseInfo.licensedEmail, firstName) {
        when {
            !licenseInfo.licensedEmail.isNullOrBlank() -> licenseInfo.licensedEmail
            !status.activeKeyMasked.isNullOrBlank() -> status.activeKeyMasked
            firstName != null -> "Titular: $firstName"
            else -> "Licença local verificada"
        }
    }
    val isKeyMonospace = !credentialValue.contains("@") && credentialValue.startsWith("TESS-")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isLight) 10.dp else 22.dp,
                shape = cardShape,
                spotColor = heroTint.copy(alpha = if (isLight) 0.16f else 0.22f),
                ambientColor = Color.Black.copy(alpha = 0.30f)
            )
            .clip(cardShape)
            .background(titaniumBrush)
            .border(BorderStroke(1.dp, rimLightBorder), cardShape)
    ) {
        // Reflexo metálico diagonal animado + luz ambiente interna
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Brilho sutil no canto superior direito do titânio
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        heroTint.copy(alpha = if (isLight) 0.14f else 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.92f, h * 0.08f),
                    radius = w * 0.62f
                ),
                radius = w * 0.62f,
                center = Offset(w * 0.92f, h * 0.08f)
            )

            // Reflexo especular diagonal (shimmer sweep)
            if (shimmerProgress in -0.5f..1.5f) {
                val sweepX = w * shimmerProgress
                val bandWidth = w * 0.42f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (isLight) 0.34f else 0.085f),
                            Color.Transparent
                        ),
                        start = Offset(sweepX - bandWidth, 0f),
                        end = Offset(sweepX + bandWidth * 0.65f, h)
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Linha Superior: Marca Tessera + Badge do Tier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_tessera_header_logo),
                        contentDescription = "Tessera",
                        colorFilter = ColorFilter.tint(cardTextPrimary),
                        modifier = Modifier.height(26.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(heroTint.copy(alpha = if (isLight) 0.16f else 0.20f))
                            .border(
                                BorderStroke(0.8.dp, heroTint.copy(alpha = 0.50f)),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (visuals.isMaster) "MASTER" else "PRO",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.1.sp,
                            color = if (isLight) CupertinoGoldWarm else heroTint
                        )
                    }
                }

                // Pílula metálica do plano
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isLight) Color(0x141C1917) else Color(0x16FFFFFF))
                        .border(
                            BorderStroke(0.8.dp, if (isLight) Color(0x221C1917) else Color(0x26FFFFFF)),
                            CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = visuals.tierBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.7.sp,
                        color = cardTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Miolo: Plano Ativo & Validade
            Text(
                text = "PLANO ATIVO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = cardTextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = status.activationTypeLabel,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = cardTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = visuals.validitySummary,
                fontSize = 12.5.sp,
                color = cardTextSecondary
            )

            // Barra ultrafina apenas para planos temporários
            visuals.progressFraction?.let { fraction ->
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .clip(CircleShape),
                    color = heroTint,
                    trackColor = if (isLight) Color(0x1A1C1917) else Color(0x1EFFFFFF)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = if (isLight) Color(0x181C1917) else Color(0x18FFFFFF)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Rodapé do Cartão: Credencial + Status Vivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = credentialLabel,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = cardTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = credentialValue,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = if (isKeyMonospace) FontFamily.Monospace else FontFamily.Default,
                        color = cardTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Selo ATIVO pulsante
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(emeraldColor.copy(alpha = if (isLight) 0.12f else 0.14f))
                        .border(
                            BorderStroke(0.8.dp, emeraldColor.copy(alpha = 0.35f)),
                            CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer { alpha = statusDotPulse }
                            .clip(CircleShape)
                            .background(emeraldColor)
                    )
                    Text(
                        text = "ATIVO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9.sp,
                        color = emeraldColor
                    )
                }
            }
        }
    }
}

/**
 * Bento Grid 2x2 compacto exibindo os privilégios Tessera Pro desbloqueados.
 */
@Composable
fun AppleProPrivilegesBento(
    isMaster: Boolean,
    isLight: Boolean,
    cardBg: Color,
    cardBorder: Color,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val textTertiary = if (isLight) Color(0xFF625B55) else TextTertiary
    val checkColor = if (isLight) CupertinoEmeraldLight else CupertinoEmerald

    val items = remember(isMaster) {
        listOf(
            BentoPrivilegeItem(
                icon = Icons.Outlined.Dashboard,
                title = "Tela Lateral (−1)",
                subtitle = "Central de widgets Bento e Feed RSS ilimitados.",
                tintDark = CupertinoSky,
                tintLight = Color(0xFF0284C7)
            ),
            BentoPrivilegeItem(
                icon = Icons.Outlined.Palette,
                title = "Estética & StandBy",
                subtitle = "Vidro líquido, blur, cores e StandBy Pro.",
                tintDark = CupertinoChampagne,
                tintLight = CupertinoGoldWarm
            ),
            BentoPrivilegeItem(
                icon = Icons.Outlined.Fingerprint,
                title = "Pastas & Cofre",
                subtitle = "Pastas inteligentes e apps ocultos com biometria.",
                tintDark = Color(0xFF34D399),
                tintLight = Color(0xFF059669)
            ),
            if (isMaster) {
                BentoPrivilegeItem(
                    icon = Icons.Outlined.AutoAwesome,
                    title = "Motor IA Master",
                    subtitle = "Busca IA, resumos no feed e ferramentas Master.",
                    tintDark = CupertinoIndigo,
                    tintLight = Color(0xFF4F46E5)
                )
            } else {
                BentoPrivilegeItem(
                    icon = Icons.Outlined.TouchApp,
                    title = "Gestos & Backup",
                    subtitle = "11 gestos personalizados, comandos @ e JSON.",
                    tintDark = Color(0xFFA78BFA),
                    tintLight = Color(0xFF7C3AED)
                )
            }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "SEUS PRIVILÉGIOS DESBLOQUEADOS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            ),
            color = textTertiary,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BentoPrivilegeCard(
                    item = items[0],
                    isLight = isLight,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    checkColor = checkColor,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                BentoPrivilegeCard(
                    item = items[1],
                    isLight = isLight,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    checkColor = checkColor,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BentoPrivilegeCard(
                    item = items[2],
                    isLight = isLight,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    checkColor = checkColor,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                BentoPrivilegeCard(
                    item = items[3],
                    isLight = isLight,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    checkColor = checkColor,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun BentoPrivilegeCard(
    item: BentoPrivilegeItem,
    isLight: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    checkColor: Color,
    modifier: Modifier = Modifier
) {
    val iconTint = if (isLight) item.tintLight else item.tintDark

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(iconTint.copy(alpha = if (isLight) 0.13f else 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(checkColor.copy(alpha = if (isLight) 0.12f else 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Liberado",
                        tint = checkColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.25).sp,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 11.5.sp,
                    lineHeight = 15.5.sp,
                    color = textSecondary
                )
            }
        }
    }
}

/**
 * Convite discreto e elegante para migrar para o plano Vitalício
 * (exibido apenas para assinantes com plano temporário).
 */
@Composable
fun AppleProLifetimeUpgradeCard(
    lifetimePriceLabel: String,
    isLight: Boolean,
    cardBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isLight) LightTextPrimary else TextPrimary
    val textSecondary = if (isLight) LightTextSecondary else TextSecondary
    val tint = if (isLight) CupertinoGoldWarm else CupertinoChampagne

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, tint.copy(alpha = if (isLight) 0.40f else 0.34f)),
        modifier = modifier
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = if (isLight) 0.14f else 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WorkspacePremium,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Migrar para o Pro Vitalício",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    color = textPrimary
                )
                Text(
                    text = "Pagamento único de $lifetimePriceLabel • Sem renovações",
                    fontSize = 11.5.sp,
                    color = textSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
