package com.tessera.launcher.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.bridge.TesseraBridgeContract
import com.tessera.launcher.data.bridge.TesseraCoreBridgeClient
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LiquidGlassBackground
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val currencySymbols = DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"))
private val currencyFormat = DecimalFormat("#,##0.00", currencySymbols)

/**
 * Card Bento de Finanças & Saldo sincronizado com Tessera Core.
 */
@Composable
fun TesseraFinancePulseCard(
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true,
    isLightMode: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snapshot by TesseraCoreBridgeClient.snapshot.collectAsState()

    if (snapshot == null || !snapshot!!.isInstalled) return

    val snap = snapshot!!
    val isMasked = snap.isFinanceMasked
    val balStr = if (isMasked) "••••••" else "R$ ${currencyFormat.format(snap.financeBalance)}"
    val vrStr = if (isMasked) "••••••" else "R$ ${currencyFormat.format(snap.financeVrBalance)}"

    val cardTextPrimary = if (isLightMode) Color(0xFF1C1B1F) else TextPrimary
    val cardTextSecondary = if (isLightMode) Color(0xFF57534E) else TextSecondary
    val badgeBg = if (isLightMode) Color(0x2600E676) else Color(0xFF1B2B22)
    val maskButtonBg = if (isLightMode) Color(0xFFDDD8CB) else Color(0xFF1E1E26)

    val border = if (isLightMode) {
        BorderStroke(0.5.dp, Color(0xFFDDD8CB))
    } else if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLightMode) Color(0xFFEFECE2) else if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(border, CardShape)
            .clip(CardShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tessera://open/finance")).apply {
                            setPackage(TesseraBridgeContract.CORE_PACKAGE)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val launch = context.packageManager.getLaunchIntentForPackage(TesseraBridgeContract.CORE_PACKAGE)
                        if (launch != null) context.startActivity(launch)
                    }
                }
            )
            .padding(16.dp)
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
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Payments,
                            contentDescription = "Pulso Financeiro",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PULSO FINANCEIRO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = cardTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(maskButtonBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                coroutineScope.launch {
                                    TesseraCoreBridgeClient.toggleFinanceMask()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMasked) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Ocultar valores",
                        tint = cardTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saldo em Contas",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = cardTextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = balStr,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = cardTextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Benefício VR",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = cardTextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = vrStr,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        ),
                        color = Color(0xFF00E676)
                    )
                }
            }
        }
    }
}

/**
 * Card Bento de Ponto CLT & Jornada sincronizado com Tessera Core.
 */
@Composable
fun TesseraTimeClockCard(
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true,
    isLightMode: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snapshot by TesseraCoreBridgeClient.snapshot.collectAsState()

    if (snapshot == null || !snapshot!!.isInstalled) return

    val snap = snapshot!!
    val status = snap.timeClockStatus ?: "CLT"
    val workedMin = snap.timeClockWorkedMinutes
    val targetMin = snap.timeClockTargetMinutes.coerceAtLeast(60)
    val progress = (workedMin.toFloat() / targetMin).coerceIn(0f, 1f)

    val cardTextPrimary = if (isLightMode) Color(0xFF1C1B1F) else TextPrimary
    val cardTextSecondary = if (isLightMode) Color(0xFF57534E) else TextSecondary
    val badgeBg = if (isLightMode) Color(0x262962FF) else Color(0xFF1D223B)
    val progressTrackBg = if (isLightMode) Color(0xFFDDD8CB) else Color(0xFF222430)

    val border = if (isLightMode) {
        BorderStroke(0.5.dp, Color(0xFFDDD8CB))
    } else if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLightMode) Color(0xFFEFECE2) else if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(border, CardShape)
            .clip(CardShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tessera://open/timeclock")).apply {
                            setPackage(TesseraBridgeContract.CORE_PACKAGE)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val launch = context.packageManager.getLaunchIntentForPackage(TesseraBridgeContract.CORE_PACKAGE)
                        if (launch != null) context.startActivity(launch)
                    }
                }
            )
            .padding(16.dp)
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
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Ponto CLT",
                            tint = Color(0xFF448AFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PONTO & JORNADA CLT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = cardTextSecondary
                    )
                }

                // Botão Bater Ponto
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2962FF))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                coroutineScope.launch {
                                    val res = TesseraCoreBridgeClient.punchTimeClock()
                                    res.onSuccess { msg ->
                                        Toast.makeText(context, "✓ $msg", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        val launch = context.packageManager.getLaunchIntentForPackage(TesseraBridgeContract.CORE_PACKAGE)
                                        if (launch != null) {
                                            Toast.makeText(context, "Abrindo Tessera Core...", Toast.LENGTH_SHORT).show()
                                            context.startActivity(launch)
                                        } else {
                                            Toast.makeText(context, "Tessera Core não encontrado", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bater Ponto",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status: $status",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = cardTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${workedMin / 60}h ${workedMin % 60}min de ${targetMin / 60}h previstas",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = cardTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de Progresso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(progressTrackBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF448AFF))
                )
            }
        }
    }
}

/**
 * Card Bento de Lista de Mercado sincronizado com Tessera Core.
 */
@Composable
fun TesseraMarketCard(
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true,
    isLightMode: Boolean = false
) {
    val context = LocalContext.current
    val snapshot by TesseraCoreBridgeClient.snapshot.collectAsState()

    if (snapshot == null || !snapshot!!.isInstalled) return

    val snap = snapshot!!
    val count = snap.marketPendingCount
    val totalEst = snap.marketEstimatedTotal
    val items = snap.marketTopItems

    val cardTextPrimary = if (isLightMode) Color(0xFF1C1B1F) else TextPrimary
    val cardTextSecondary = if (isLightMode) Color(0xFF57534E) else TextSecondary
    val badgeBg = if (isLightMode) Color(0x26FFB300) else Color(0xFF2E2416)

    val border = if (isLightMode) {
        BorderStroke(0.5.dp, Color(0xFFDDD8CB))
    } else if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLightMode) Color(0xFFEFECE2) else if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(border, CardShape)
            .clip(CardShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tessera://open/market")).apply {
                            setPackage(TesseraBridgeContract.CORE_PACKAGE)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val launch = context.packageManager.getLaunchIntentForPackage(TesseraBridgeContract.CORE_PACKAGE)
                        if (launch != null) context.startActivity(launch)
                    }
                }
            )
            .padding(16.dp)
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
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Lista de Mercado",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LISTA DE MERCADO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = cardTextSecondary
                    )
                }

                if (totalEst > 0) {
                    Text(
                        text = "Est. R$ ${currencyFormat.format(totalEst)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFFFB300)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (count > 0) {
                Text(
                    text = "$count ${if (count == 1) "item pendente" else "itens pendentes"}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = cardTextPrimary
                )
                if (items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = items.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = cardTextSecondary,
                        maxLines = 1
                    )
                }
            } else {
                Text(
                    text = "Nenhum item pendente no carrinho",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = cardTextSecondary
                )
            }
        }
    }
}
