package com.tessera.launcher.ui.components.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Bolha circular satélite exibida ao lado da ilha principal no modo Split.
 * Ao tocar nela, a atividade secundária é promovida para o centro.
 */
@Composable
fun IslandSecondaryBubble(
    activity: LiveActivityInfo?,
    mediaPlayback: MediaPlaybackInfo?,
    defaultAccent: Color,
    onSwap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleShape = CircleShape
    val islandBlack = Color(0xFF000000)
    val subtleBorder = Color.White.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .size(34.dp)
            .shadow(
                elevation = 8.dp,
                shape = bubbleShape,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(bubbleShape)
            .background(islandBlack)
            .border(width = 0.7.dp, color = subtleBorder, shape = bubbleShape)
            .tesseraPressFeedback()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSwap
            ),
        contentAlignment = Alignment.Center
    ) {
        if (activity != null) {
            val presentation = remember(activity, defaultAccent) {
                HudPresentationMapper.map(activity, defaultAccent)
            }
            Icon(
                imageVector = presentation.icon,
                contentDescription = presentation.title,
                tint = presentation.accentColor,
                modifier = Modifier.size(16.dp)
            )
        } else if (mediaPlayback != null && (mediaPlayback.isPlaying || mediaPlayback.hasActiveSession)) {
            AudioWaveform(
                isPlaying = mediaPlayback.isPlaying,
                accentColor = defaultAccent
            )
        }
    }
}
