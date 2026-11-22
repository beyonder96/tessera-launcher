package com.tessera.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.license.LicenseStatus
import com.tessera.launcher.data.license.LicenseTier

/**
 * Micro-tag discreta para sinalizar recursos exclusivos de assinatura.
 * - Durante o período de avaliação (TRIAL): exibe "TESTE" em tom âmbar discreto.
 * - Após expirar o teste (FREE): exibe "PRO" em tom de destaque sutil.
 * - Com assinatura ativa ou licença Master: não renderiza nada.
 */
@Composable
fun SubscriptionMicroTag(
    licenseStatus: LicenseStatus,
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val label = when {
        licenseStatus.tier == LicenseTier.TRIAL -> "TESTE"
        !licenseStatus.hasProAccess -> "PRO"
        else -> return
    }
    SubscriptionMicroTagLabel(
        label = label,
        isTrial = licenseStatus.tier == LicenseTier.TRIAL,
        isLightMode = isLightMode,
        modifier = modifier
    )
}

@Composable
fun SubscriptionMicroTagLabel(
    label: String?,
    isTrial: Boolean = label == "TESTE",
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (label.isNullOrBlank()) return

    val tint = if (isTrial) {
        if (isLightMode) Color(0xFFB45309) else Color(0xFFE2B714)
    } else {
        if (isLightMode) Color(0xFF4F46E5) else Color(0xFFA5B4FC)
    }
    val bgAlpha = if (isLightMode) 0.12f else 0.14f
    val borderAlpha = if (isLightMode) 0.34f else 0.36f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(tint.copy(alpha = bgAlpha))
            .border(BorderStroke(0.75.dp, tint.copy(alpha = borderAlpha)), RoundedCornerShape(5.dp))
            .padding(horizontal = 5.dp, vertical = 1.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp,
            lineHeight = 10.sp,
            color = tint
        )
    }
}
