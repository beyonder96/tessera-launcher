package com.tessera.launcher.ui.components.hud

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.PackageActivityDetails
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Card expandido especializado em rastreamento de encomendas de e-commerce e Correios.
 */
@Composable
fun PackageExpandedContent(
    details: PackageActivityDetails,
    accentColor: Color,
    onOpenAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .widthIn(min = 300.dp, max = 340.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabeçalho: Logo do App + Nome + Botão Abrir
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val safeLogo = remember(details.packageLogo) {
                    runCatching {
                        details.packageLogo?.takeIf { !it.isRecycled }?.asImageBitmap()
                    }.getOrNull()
                }

                if (safeLogo != null) {
                    Image(
                        bitmap = safeLogo,
                        contentDescription = details.appName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = details.appName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botão Abrir App
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24))
                    .tesseraPressFeedback()
                    .clickable { onOpenAppClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = "Abrir aplicativo",
                    tint = Color(0xFFA1A1AA),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Título e Previsão
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = details.headline,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!details.etaText.isNullOrBlank()) {
                Text(
                    text = details.etaText,
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!details.itemSummary.isNullOrBlank()) {
                Text(
                    text = details.itemSummary,
                    color = Color(0xFFA1A1AA),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Código de Rastreio com cópia
        if (!details.trackingCode.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF141418))
                    .border(0.6.dp, Color(0xFF27272A), RoundedCornerShape(10.dp))
                    .tesseraPressFeedback()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(details.trackingCode))
                        Toast.makeText(context, "Código copiado: ${details.trackingCode}", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CÓDIGO DE RASTREIO",
                        color = Color(0xFF71717A),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = details.trackingCode,
                        color = Color(0xFFEDEDED),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copiar código",
                    tint = Color(0xFFA1A1AA),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Barra de Marcos (Comprado -> Enviado -> Em rota -> Entregue)
        if (details.milestones.isNotEmpty()) {
            MilestoneProgressBar(
                milestones = details.milestones,
                accentColor = accentColor
            )
        }

        // Ações da notificação
        if (details.actions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                details.actions.take(2).forEach { action ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(17.dp))
                            .tesseraPressFeedback()
                            .clickable {
                                try {
                                    action.intent?.send()
                                } catch (_: Exception) {}
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = action.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
