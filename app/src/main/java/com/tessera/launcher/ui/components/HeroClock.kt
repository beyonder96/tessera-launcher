package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.design.components.AppleSheet
import com.tessera.launcher.ui.design.components.CapsuleButton
import com.tessera.launcher.ui.design.components.SegmentedControl
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.AppleTypography
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import com.tessera.launcher.ui.theme.apple.interFont

/**
 * Relógio Hero da Tela Inicial — Padrão Tela de Bloqueio Apple (iOS 17/18/26).
 * Apresenta dígitos monumentais em Inter Display (112sp), números tabulares ("tnum")
 * para estabilidade geométrica absoluta, transição de rolagem suave e editor em sheet.
 */
@Composable
fun HeroClock(
    formattedTime: String,
    formattedDate: String,
    modifier: Modifier = Modifier,
    weatherSummary: String? = null,
    clockWeight: Int = 400,
    accentColor: Color = LocalAppleColors.current.accent,
    onWeightChange: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    var isEditorOpen by remember { mutableStateOf(false) }

    val dateAndWeatherText = remember(formattedDate, weatherSummary) {
        if (!weatherSummary.isNullOrBlank()) {
            "$formattedDate · $weatherSummary"
        } else {
            formattedDate
        }
    }

    val heroStyle = remember(clockWeight, colors.label) {
        TextStyle(
            fontFamily = interFont(weight = clockWeight, opsz = 32f),
            fontWeight = FontWeight(clockWeight),
            fontSize = 112.sp,
            lineHeight = 112.sp,
            letterSpacing = (-0.025).em,
            color = colors.label,
            textAlign = TextAlign.Center
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Toque curto ou longo
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Linha Superior de Contexto (Data e Clima)
        Text(
            text = dateAndWeatherText,
            style = typography.subheadlineBold,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Dígitos Monumentais com transição de virada de minuto vertical suave
        Row(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    Taptic.heavyImpact(context)
                    isEditorOpen = true
                }
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            formattedTime.forEachIndexed { index, char ->
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        slideInVertically { it / 2 } + fadeIn() togetherWith
                                slideOutVertically { -it / 2 } + fadeOut()
                    },
                    label = "clock_digit_$index"
                ) { targetChar ->
                    Text(
                        text = targetChar.toString(),
                        style = heroStyle
                    )
                }
            }
        }
    }

    // Modal Sheet de Personalização do Relógio
    if (isEditorOpen) {
        AppleSheet(
            visible = isEditorOpen,
            onDismissRequest = { isEditorOpen = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Personalizar Relógio",
                    style = typography.title2,
                    color = colors.label
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Seletor de Espessura da Tipografia
                Text(
                    text = "ESPESSURA DA FONTE ($clockWeight)",
                    style = typography.footnoteBold,
                    color = colors.secondaryLabel
                )

                Spacer(modifier = Modifier.height(12.dp))

                val weightOptions = listOf("Leve", "Regular", "Médio", "Negrito")
                val selectedIndex = when {
                    clockWeight <= 250 -> 0
                    clockWeight <= 400 -> 1
                    clockWeight <= 600 -> 2
                    else -> 3
                }

                SegmentedControl(
                    items = weightOptions,
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        val targetWeight = when (index) {
                            0 -> 200
                            1 -> 400
                            2 -> 600
                            else -> 800
                        }
                        onWeightChange(targetWeight)
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                CapsuleButton(
                    text = "Concluído",
                    onClick = { isEditorOpen = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
