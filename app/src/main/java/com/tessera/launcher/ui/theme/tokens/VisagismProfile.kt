package com.tessera.launcher.ui.theme.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Perfis de Visagismo do Tessera Launcher:
 * Integra forma geométrica, ritmo de animação e harmonia estrutural
 * de acordo com o temperamento estético da interface.
 */
enum class VisagismProfile(
    val id: String,
    val title: String,
    val subtitle: String
) {
    /**
     * Sereno (Fleumático / Calm Tech):
     * Linhas suaves, cantos generosos, transições fluídas e sem sobressaltos.
     */
    SERENE(
        id = "SERENE",
        title = "Sereno",
        subtitle = "Linhas contínuas, cantos generosos, física suave e calma"
    ),

    /**
     * Preciso (Melancólico / Engineered):
     * Geometria apurada, cantos contidos, zero bounce, foco analítico e funcional.
     */
    PRECISE(
        id = "PRECISE",
        title = "Preciso",
        subtitle = "Geometria arquitetônica, cantos retos, transições imediatas"
    ),

    /**
     * Vivo (Sanguíneo / Organic Flow):
     * Curvas dinâmicas (squircles/pílulas), feedback tátil elástico e jovial.
     */
    VIBRANT(
        id = "VIBRANT",
        title = "Vivo",
        subtitle = "Formas orgânicas, cantos dinâmicos e física expressiva"
    ),

    /**
     * Ousado (Colérico / Bold Minimal):
     * Alto contraste, tipografia pesada, transições cortadas e assertivas.
     */
    BOLD(
        id = "BOLD",
        title = "Ousado",
        subtitle = "Contraste forte, linhas incisivas e aceleração imediata"
    );

    companion object {
        fun fromId(id: String?): VisagismProfile {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SERENE
        }
    }
}

/**
 * Resolução dinâmica de formas com base no perfil de visagismo.
 */
fun resolveVisagismShapes(profile: VisagismProfile): TesseraShapes {
    return when (profile) {
        VisagismProfile.SERENE -> TesseraShapes(
            micro = RoundedCornerShape(6.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            card = RoundedCornerShape(24.dp),
            sheet = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            pill = RoundedCornerShape(28.dp),
            full = CircleShape
        )
        VisagismProfile.PRECISE -> TesseraShapes(
            micro = RoundedCornerShape(2.dp),
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp),
            card = RoundedCornerShape(14.dp),
            sheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            pill = RoundedCornerShape(16.dp),
            full = RoundedCornerShape(50)
        )
        VisagismProfile.VIBRANT -> TesseraShapes(
            micro = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(22.dp),
            card = RoundedCornerShape(26.dp),
            sheet = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            pill = CircleShape,
            full = CircleShape
        )
        VisagismProfile.BOLD -> TesseraShapes(
            micro = RoundedCornerShape(3.dp),
            small = RoundedCornerShape(6.dp),
            medium = RoundedCornerShape(10.dp),
            large = RoundedCornerShape(14.dp),
            card = RoundedCornerShape(18.dp),
            sheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            pill = RoundedCornerShape(20.dp),
            full = CircleShape
        )
    }
}

private fun createMotion(
    pressStiffness: Float, pressDamping: Float,
    quickStiffness: Float, quickDamping: Float,
    standardStiffness: Float, standardDamping: Float,
    spatialStiffness: Float, spatialDamping: Float,
    bouncyStiffness: Float, bouncyDamping: Float,
    fadeQuickMillis: Int,
    fadeStandardMillis: Int,
    emphasizedEasing: androidx.compose.animation.core.Easing
): TesseraMotion = TesseraMotion(
    springPress = spring(stiffness = pressStiffness, dampingRatio = pressDamping),
    springQuick = spring(stiffness = quickStiffness, dampingRatio = quickDamping),
    springStandard = spring(stiffness = standardStiffness, dampingRatio = standardDamping),
    springSpatial = spring(stiffness = spatialStiffness, dampingRatio = spatialDamping),
    springSpatialDp = spring(stiffness = spatialStiffness, dampingRatio = spatialDamping),
    springSpatialOffset = spring(stiffness = spatialStiffness, dampingRatio = spatialDamping),
    springBouncy = spring(stiffness = bouncyStiffness, dampingRatio = bouncyDamping),
    fadeQuick = tween(fadeQuickMillis, easing = FastOutSlowInEasing),
    fadeStandard = tween(fadeStandardMillis, easing = FastOutSlowInEasing),
    emphasizedEasing = emphasizedEasing
)

/**
 * Resolução dinâmica de física de motion com base no perfil de visagismo.
 */
fun resolveVisagismMotion(profile: VisagismProfile): TesseraMotion {
    return when (profile) {
        VisagismProfile.SERENE -> createMotion(
            pressStiffness = 1100f, pressDamping = 0.96f,
            quickStiffness = 700f, quickDamping = 0.94f,
            standardStiffness = 450f, standardDamping = 0.92f,
            spatialStiffness = 300f, spatialDamping = 0.90f,
            bouncyStiffness = 350f, bouncyDamping = 0.82f,
            fadeQuickMillis = 180,
            fadeStandardMillis = 260,
            emphasizedEasing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f)
        )
        VisagismProfile.PRECISE -> createMotion(
            pressStiffness = 1800f, pressDamping = 1.0f,
            quickStiffness = 1200f, quickDamping = 1.0f,
            standardStiffness = 800f, standardDamping = 1.0f,
            spatialStiffness = 500f, spatialDamping = 1.0f,
            bouncyStiffness = 600f, bouncyDamping = 0.95f,
            fadeQuickMillis = 120,
            fadeStandardMillis = 180,
            emphasizedEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        )
        VisagismProfile.VIBRANT -> createMotion(
            pressStiffness = 1300f, pressDamping = 0.85f,
            quickStiffness = 800f, quickDamping = 0.82f,
            standardStiffness = 500f, standardDamping = 0.76f,
            spatialStiffness = 320f, spatialDamping = 0.74f,
            bouncyStiffness = 420f, bouncyDamping = 0.68f,
            fadeQuickMillis = 150,
            fadeStandardMillis = 220,
            emphasizedEasing = CubicBezierEasing(0.34f, 1.3f, 0.64f, 1f)
        )
        VisagismProfile.BOLD -> createMotion(
            pressStiffness = 2000f, pressDamping = 0.95f,
            quickStiffness = 1300f, quickDamping = 0.92f,
            standardStiffness = 850f, standardDamping = 0.88f,
            spatialStiffness = 520f, spatialDamping = 0.85f,
            bouncyStiffness = 580f, bouncyDamping = 0.78f,
            fadeQuickMillis = 100,
            fadeStandardMillis = 160,
            emphasizedEasing = CubicBezierEasing(0.1f, 0.9f, 0.2f, 1f)
        )
    }
}
