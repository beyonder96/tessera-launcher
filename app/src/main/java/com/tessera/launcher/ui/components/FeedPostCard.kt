package com.tessera.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.FeedPost
import com.tessera.launcher.ui.theme.*

@Composable
fun FeedPostCard(
    post: FeedPost,
    isLightMode: Boolean,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isLightMode) LightCardBackground else DarkSurface
    val borderColor = if (isLightMode) LightCardBorder else DarkSurfaceBorder
    val primaryTextColor = if (isLightMode) LightTextPrimary else TextPrimary
    val secondaryTextColor = if (isLightMode) LightTextSecondary else TextSecondary
    val tertiaryTextColor = if (isLightMode) LightTextSecondary else TextTertiary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(post.url) },
        shape = CardShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${post.sourceLabel} · ${post.relativeTime}",
                    color = secondaryTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (post.title != null) {
                Text(
                    text = post.title,
                    color = primaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (post.body.isNotBlank()) {
                Text(
                    text = post.body,
                    color = secondaryTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ArrowUpward,
                    contentDescription = "Score",
                    modifier = Modifier.size(12.dp),
                    tint = tertiaryTextColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.score.toString(),
                    color = tertiaryTextColor,
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comments",
                    modifier = Modifier.size(12.dp),
                    tint = tertiaryTextColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.commentCount.toString(),
                    color = tertiaryTextColor,
                    fontSize = 11.sp
                )
            }

            if (post.aiSummary != null) {
                Surface(
                    shape = PillShape,
                    color = if (isLightMode) LightCardBackground else DarkSurfaceVariant,
                    border = BorderStroke(1.dp, if (isLightMode) LightCardBorder else DarkSurfaceBorderHover)
                ) {
                    Text(
                        text = post.aiSummary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}
