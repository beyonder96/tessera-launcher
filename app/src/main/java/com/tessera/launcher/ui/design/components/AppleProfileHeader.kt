package com.tessera.launcher.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

/**
 * Cabeçalho de Perfil do Tessera no topo dos Ajustes (Padrão Apple ID / iCloud Card).
 */
@Composable
fun AppleProfileHeader(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val avatarGradient = Brush.linearGradient(
        colors = listOf(
            colors.accent,
            colors.accent.copy(alpha = 0.7f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(ContinuousShape(20.dp))
            .background(colors.secondaryGrouped)
            .liquidGlass(
                style = GlassStyle.Regular,
                shape = ContinuousShape(20.dp)
            )
            .clickable {
                Taptic.lightImpact(context)
                onClick()
            }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar squircle Tessera 3.0
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(ContinuousShape(14.dp))
                    .background(avatarGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "T",
                    style = typography.title1.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tessera Launcher",
                    style = typography.headline,
                    color = colors.label
                )
                Text(
                    text = "Versão 3.0 • Designed in Cupertino",
                    style = typography.subheadline,
                    color = colors.secondaryLabel
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.tertiaryLabel,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
