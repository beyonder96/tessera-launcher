package com.tessera.launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

/**
 * Dock Flutuante Liquid Glass do Tessera 3.0.
 * Apresenta acabamento de vidro Chrome com reflexo de luz superior,
 * ícones squircle dos apps previstos pelo contexto e botão de busca Spotlight integrado.
 */
@Composable
fun GlassDock(
    predictedApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .liquidGlass(
                    shape = ContinuousShape(34.dp),
                    style = GlassStyle.Chrome
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Apps previstos pelo ContextualPredictor (até 4 apps)
            predictedApps.take(4).forEach { app ->
                val iconBitmap = remember(app.packageName, app.icon) {
                    app.icon?.toBitmap(96, 96)?.asImageBitmap()
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(AppleShapes.icon)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                Taptic.lightImpact(context)
                                onAppClick(app)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(46.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(AppleShapes.icon)
                        )
                    }
                }
            }

            if (predictedApps.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .clip(AppleShapes.capsule)
                )
            }

            // Botão Spotlight integrado
            Row(
                modifier = Modifier
                    .clip(AppleShapes.capsule)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            Taptic.lightImpact(context)
                            onSearchClick()
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Buscar",
                    tint = colors.label,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Buscar",
                    style = typography.subheadlineBold,
                    color = colors.label
                )
            }
        }
    }
}
