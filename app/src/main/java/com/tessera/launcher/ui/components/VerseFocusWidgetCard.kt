package com.tessera.launcher.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LiquidGlassBackground
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary

private data class DailyVerse(val text: String, val reference: String)

private val VERSE_COLLECTION = listOf(
    DailyVerse("O Senhor é o meu pastor; de nada terei falta.", "Salmos 23:1"),
    DailyVerse("Tudo posso naquele que me fortalece.", "Filipenses 4:13"),
    DailyVerse("Lâmpada para os meus pés é a tua palavra e luz, para o meu caminho.", "Salmos 119:105"),
    DailyVerse("Confie no Senhor de todo o seu coração e não se apoie em seu próprio entendimento.", "Provérbios 3:5"),
    DailyVerse("O amor é paciente, o amor é bondoso. Tudo sofre, tudo crê, tudo espera, tudo suporta.", "1 Coríntios 13:4,7"),
    DailyVerse("A persistência é o caminho do êxito.", "Charles Chaplin"),
    DailyVerse("A simplicidade é o último grau de sofisticação.", "Leonardo da Vinci"),
    DailyVerse("Não fui eu que ordenei a você? Seja forte e corajoso! Não se apavore nem desanime.", "Josué 1:9")
)

@Composable
fun VerseFocusWidgetCard(
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    val item = VERSE_COLLECTION[currentIndex % VERSE_COLLECTION.size]

    val border = if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .border(border, CardShape)
            .clip(CardShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    currentIndex = (currentIndex + 1) % VERSE_COLLECTION.size
                }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B1E28)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FormatQuote,
                            contentDescription = "Versículo do dia",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "VERSÍCULO & FOCO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Próximo versículo",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                currentIndex = (currentIndex + 1) % VERSE_COLLECTION.size
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = item,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "verse_anim"
            ) { currentVerse ->
                Column {
                    Text(
                        text = "\"${currentVerse.text}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "— ${currentVerse.reference}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
