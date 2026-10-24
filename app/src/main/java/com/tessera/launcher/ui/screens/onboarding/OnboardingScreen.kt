package com.tessera.launcher.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.design.components.CapsuleButton
import com.tessera.launcher.ui.design.components.SquircleIcon
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import com.tessera.launcher.ui.theme.apple.appleSpring
import kotlinx.coroutines.delay

/**
 * Onboarding Oficial Apple "Olá" (Designed in Cupertino).
 * Animação de saudações internacionais com caligrafia Apple e apresentação dos pilares do Tessera 3.0.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val greetings = listOf("Olá", "Hello", "Bonjour", "Ciao", "Hola")
    var currentGreetingIndex by remember { mutableIntStateOf(0) }
    var isGreetingStage by remember { androidx.compose.runtime.mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repeat(greetings.size) { index ->
            currentGreetingIndex = index
            delay(1200)
        }
        isGreetingStage = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.systemBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        if (isGreetingStage) {
            // Estágio 1: Saudação Cursiva "Olá" / "Hello"
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = greetings[currentGreetingIndex],
                    transitionSpec = {
                        fadeIn(animationSpec = appleSpring()) togetherWith fadeOut(animationSpec = appleSpring())
                    },
                    label = "greeting_anim"
                ) { greeting ->
                    Text(
                        text = greeting,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Serif,
                        color = colors.label,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Estágio 2: Apresentação dos Pilares Tessera 3.0
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Bem-vindo ao\nTessera 3.0",
                        style = typography.largeTitle.copy(fontWeight = FontWeight.Bold),
                        color = colors.label,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Designed in Cupertino • Made for Android",
                        style = typography.subheadline,
                        color = colors.secondaryLabel
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Destaques de Recursos
                    OnboardingFeatureRow(
                        icon = Icons.Outlined.AutoAwesome,
                        iconBackground = Color(0xFF007AFF),
                        title = "Liquid Glass & Dynamic Island",
                        description = "Transparência óptica real com desfoque de fundo e ilha dinâmica de mídia e notificações ativas."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OnboardingFeatureRow(
                        icon = Icons.Outlined.Search,
                        iconBackground = Color(0xFF34C759),
                        title = "Spotlight Search Unificado",
                        description = "Busca inteligente de apps, contatos e IA com barra inferior ergonômica acima do teclado."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OnboardingFeatureRow(
                        icon = Icons.Outlined.Layers,
                        iconBackground = Color(0xFFAF52DE),
                        title = "Smart Stack & Relógio Hero",
                        description = "Widgets empilháveis com rotação inteligente contextual e relógio monumental personalizável."
                    )
                }

                CapsuleButton(
                    text = "Começar",
                    onClick = {
                        Taptic.lightImpact(context)
                        onFinish()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    description: String
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        SquircleIcon(
            imageVector = icon,
            backgroundColor = iconBackground,
            size = 44.dp,
            iconSize = 24.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.headline,
                color = colors.label
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = typography.footnote,
                color = colors.secondaryLabel
            )
        }
    }
}
