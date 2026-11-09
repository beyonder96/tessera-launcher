package com.tessera.launcher.ui.components.hud

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.LiveActivityInfo

/**
 * Conteúdo do modo compacto padrão da Ilha Dinâmica.
 */
@Composable
fun IslandCompactContent(
    activity: LiveActivityInfo,
    defaultAccent: Color,
    modifier: Modifier = Modifier
) {
    val presentation = remember(activity, defaultAccent) {
        HudPresentationMapper.map(activity, defaultAccent)
    }

    Row(
        modifier = modifier
            .widthIn(min = 120.dp, max = 290.dp)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Leading: Artwork ou Ícone da marca
        val safeArtwork = remember(activity.artwork) {
            runCatching {
                activity.artwork?.takeIf { !it.isRecycled }?.asImageBitmap()
            }.getOrNull()
        }

        if (safeArtwork != null) {
            Image(
                bitmap = safeArtwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(presentation.accentColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = presentation.icon,
                    contentDescription = null,
                    tint = presentation.accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(7.dp))

        // Título e Informação Crítica
        Text(
            text = presentation.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        if (!presentation.criticalText.isNullOrBlank()) {
            Text(
                text = " • ${presentation.criticalText}",
                color = presentation.accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Trailing: Cronômetro, Progresso ou Ponto Vivo
        if (presentation.isChronometer && presentation.chronometerBaseMs != null) {
            LiveChronometer(
                baseMs = presentation.chronometerBaseMs,
                isCountDown = presentation.isCountDown,
                color = presentation.accentColor,
                fontSize = 10.sp
            )
        } else if (presentation.progressPercent != null && presentation.progressPercent > 0f) {
            CircularProgressIndicator(
                progress = { presentation.progressPercent },
                modifier = Modifier.size(11.dp),
                color = presentation.accentColor,
                trackColor = Color(0xFF27272A),
                strokeWidth = 1.8.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(presentation.accentColor)
            )
        }
    }
}
