package com.tessera.launcher.ui.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.CheckoutSession
import com.tessera.launcher.data.model.LicenseInfo
import com.tessera.launcher.data.model.PlanOption
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.CharcoalBackground
import com.tessera.launcher.ui.theme.CharcoalCardBackground
import com.tessera.launcher.ui.theme.CharcoalCardBorder
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightDivider
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesseraCheckoutModal(
    isOpen: Boolean,
    plans: List<PlanOption>,
    preferredPlanId: String = "pro_lifetime",
    licenseInfo: LicenseInfo,
    activeSession: CheckoutSession?,
    isCreatingCheckout: Boolean,
    isRestoringPurchase: Boolean,
    checkoutMessage: String?,
    isLightMode: Boolean,
    isAmoledMode: Boolean,
    accentColor: Color,
    onStartCheckout: (planId: String, email: String, method: String, onOpenExternalUrl: (String) -> Unit) -> Unit,
    onCheckPaymentNow: () -> Unit,
    onBackToPlans: () -> Unit,
    onRestorePurchase: (email: String) -> Unit,
    onClearLicense: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val containerBg = when {
        isLightMode -> LightBackground
        isAmoledMode -> AmoledBlack
        else -> CharcoalBackground
    }
    val cardBg = when {
        isLightMode -> LightCardBackground
        isAmoledMode -> Color(0xFF0D0D11)
        else -> CharcoalCardBackground
    }
    val cardBorder = when {
        isLightMode -> LightCardBorder
        isAmoledMode -> AmoledCardBorder
        else -> CharcoalCardBorder
    }
    val primaryText = if (isLightMode) LightTextPrimary else TextPrimary
    val secondaryText = if (isLightMode) LightTextSecondary else TextSecondary
    val mutedText = if (isLightMode) Color(0xFF78716C) else TextTertiary
    val highlightColor = remember(accentColor, isLightMode) {
        if (accentColor == Color.White && isLightMode) Color(0xFF1C1917)
        else if (accentColor == Color.White) Color.White
        else accentColor
    }
    val primaryButtonTextColor = remember(highlightColor) {
        val luminance = (0.299f * highlightColor.red + 0.587f * highlightColor.green + 0.114f * highlightColor.blue)
        if (luminance > 0.6f) Color(0xFF111111) else Color.White
    }

    var selectedPlanId by rememberSaveable(preferredPlanId) {
        mutableStateOf(
            plans.firstOrNull { it.id == preferredPlanId }?.id
                ?: plans.firstOrNull { it.id == "pro_lifetime" }?.id
                ?: plans.firstOrNull()?.id
                ?: "pro_lifetime"
        )
    }
    var emailInput by rememberSaveable { mutableStateOf(licenseInfo.licensedEmail.orEmpty()) }
    var showRestoreSection by rememberSaveable { mutableStateOf(false) }
    var restoreEmailInput by rememberSaveable { mutableStateOf(licenseInfo.licensedEmail.orEmpty()) }

    val openUrl: (String) -> Unit = { url ->
        openUrlInCustomTabs(
            context = context,
            url = url,
            toolbarColor = containerBg,
            isLightMode = isLightMode
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 14.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (isLightMode) Color(0xFFD0C9B9) else Color(0xFF44444E))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                licenseInfo.isCurrentlyValid() -> {
                    ActiveLicenseContent(
                        licenseInfo = licenseInfo,
                        checkoutMessage = checkoutMessage,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        highlightColor = highlightColor,
                        primaryButtonTextColor = primaryButtonTextColor,
                        onClearLicense = onClearLicense,
                        onDone = onDismiss
                    )
                }

                activeSession != null && activeSession.status != "approved" -> {
                    PendingCheckoutContent(
                        session = activeSession,
                        checkoutMessage = checkoutMessage,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        highlightColor = highlightColor,
                        primaryButtonTextColor = primaryButtonTextColor,
                        isLightMode = isLightMode,
                        onCopyPix = { code ->
                            copyToClipboard(context, "Código Pix", code)
                            Toast.makeText(context, "Código Pix copiado!", Toast.LENGTH_SHORT).show()
                        },
                        onOpenExternalUrl = openUrl,
                        onCheckPaymentNow = onCheckPaymentNow,
                        onBackToPlans = onBackToPlans
                    )
                }

                else -> {
                    PlanSelectionContent(
                        plans = plans,
                        selectedPlanId = selectedPlanId,
                        onSelectPlan = { selectedPlanId = it },
                        emailInput = emailInput,
                        onEmailChange = { emailInput = it },
                        isCreatingCheckout = isCreatingCheckout,
                        isRestoringPurchase = isRestoringPurchase,
                        showRestoreSection = showRestoreSection,
                        onToggleRestoreSection = {
                            if (!showRestoreSection && restoreEmailInput.isBlank() && emailInput.isNotBlank()) {
                                restoreEmailInput = emailInput
                            }
                            showRestoreSection = !showRestoreSection
                        },
                        restoreEmailInput = restoreEmailInput,
                        onRestoreEmailChange = { restoreEmailInput = it },
                        checkoutMessage = checkoutMessage,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        mutedText = mutedText,
                        highlightColor = highlightColor,
                        primaryButtonTextColor = primaryButtonTextColor,
                        isLightMode = isLightMode,
                        onPayPix = {
                            onStartCheckout(selectedPlanId, emailInput, "pix", openUrl)
                        },
                        onPayCard = {
                            onStartCheckout(selectedPlanId, emailInput, "card", openUrl)
                        },
                        onRestore = {
                            onRestorePurchase(restoreEmailInput)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveLicenseContent(
    licenseInfo: LicenseInfo,
    checkoutMessage: String?,
    cardBg: Color,
    cardBorder: Color,
    primaryText: Color,
    secondaryText: Color,
    highlightColor: Color,
    primaryButtonTextColor: Color,
    onClearLicense: () -> Unit,
    onDone: () -> Unit
) {
    val formattedExpiry = remember(licenseInfo.expiresAt) {
        if (licenseInfo.expiresAt <= 0L) {
            "Vitalício (Sem expiração)"
        } else {
            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
            "Ativo até ${fmt.format(Date(licenseInfo.expiresAt))}"
        }
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(highlightColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Verified,
            contentDescription = "Licença Ativa",
            tint = highlightColor,
            modifier = Modifier.size(28.dp)
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "TESSERA PRO ATIVO",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 1.4.sp
        ),
        color = primaryText
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Sua licença foi verificada e vinculada a este aparelho.",
        fontSize = 13.sp,
        color = secondaryText,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            LicenseDetailRow("Plano", licenseInfo.planDisplayName(), primaryText, secondaryText)
            Spacer(modifier = Modifier.height(10.dp))
            LicenseDetailRow("E-mail vinculado", licenseInfo.licensedEmail ?: "—", primaryText, secondaryText)
            Spacer(modifier = Modifier.height(10.dp))
            LicenseDetailRow("Validade", formattedExpiry, primaryText, secondaryText)
        }
    }

    if (!checkoutMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = checkoutMessage,
            fontSize = 12.sp,
            color = highlightColor,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = onDone,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = highlightColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            text = "Concluir",
            fontWeight = FontWeight.SemiBold,
            color = primaryButtonTextColor
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = onClearLicense) {
        Text(
            text = "Desvincular licença deste aparelho",
            fontSize = 12.sp,
            color = secondaryText
        )
    }
}

@Composable
private fun LicenseDetailRow(
    label: String,
    value: String,
    primaryText: Color,
    secondaryText: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = secondaryText)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PendingCheckoutContent(
    session: CheckoutSession,
    checkoutMessage: String?,
    cardBg: Color,
    cardBorder: Color,
    primaryText: Color,
    secondaryText: Color,
    highlightColor: Color,
    primaryButtonTextColor: Color,
    isLightMode: Boolean,
    onCopyPix: (String) -> Unit,
    onOpenExternalUrl: (String) -> Unit,
    onCheckPaymentNow: () -> Unit,
    onBackToPlans: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(cardBg)
                .border(BorderStroke(1.dp, cardBorder), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackToPlans
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar aos planos",
                tint = primaryText,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (session.method == "pix") "PAGAMENTO VIA PIX" else "CHECKOUT MERCADO PAGO",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp
            ),
            color = primaryText
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    if (session.method == "pix") {
        val qrBitmap: ImageBitmap? = remember(session.qrCodeBase64) {
            decodeBase64ImageBitmap(session.qrCodeBase64)
        }

        if (qrBitmap != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrBitmap,
                    contentDescription = "QR Code Pix",
                    modifier = Modifier.size(190.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!session.pixCopyPaste.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PIX COPIA E COLA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = secondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = session.pixCopyPaste,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = primaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onCopyPix(session.pixCopyPaste) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = highlightColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = primaryButtonTextColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copiar Código Pix",
                    fontWeight = FontWeight.SemiBold,
                    color = primaryButtonTextColor
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = highlightColor,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Checkout aberto no navegador",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Conclua o pagamento no Mercado Pago para retornar automaticamente e ativar sua licença.",
                    fontSize = 12.sp,
                    color = secondaryText,
                    textAlign = TextAlign.Center
                )
                if (!session.checkoutUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onOpenExternalUrl(session.checkoutUrl) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = highlightColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = primaryButtonTextColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abrir Checkout Mercado Pago",
                            fontWeight = FontWeight.SemiBold,
                            color = primaryButtonTextColor
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = highlightColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Aguardando confirmação automática...",
            fontSize = 12.sp,
            color = secondaryText
        )
    }

    if (!checkoutMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = checkoutMessage,
            fontSize = 12.sp,
            color = if (isLightMode) Color(0xFFB45309) else Color(0xFFFBBF24),
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedButton(
        onClick = onCheckPaymentNow,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Refresh,
            contentDescription = null,
            tint = primaryText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Já paguei, verificar agora",
            fontWeight = FontWeight.Medium,
            color = primaryText
        )
    }
}

@Composable
private fun PlanSelectionContent(
    plans: List<PlanOption>,
    selectedPlanId: String,
    onSelectPlan: (String) -> Unit,
    emailInput: String,
    onEmailChange: (String) -> Unit,
    isCreatingCheckout: Boolean,
    isRestoringPurchase: Boolean,
    showRestoreSection: Boolean,
    onToggleRestoreSection: () -> Unit,
    restoreEmailInput: String,
    onRestoreEmailChange: (String) -> Unit,
    checkoutMessage: String?,
    cardBg: Color,
    cardBorder: Color,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    highlightColor: Color,
    primaryButtonTextColor: Color,
    isLightMode: Boolean,
    onPayPix: () -> Unit,
    onPayCard: () -> Unit,
    onRestore: () -> Unit
) {
    Text(
        text = "TESSERA PRO",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 1.6.sp
        ),
        color = primaryText
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Escolha seu plano e pague via Mercado Pago (Pix ou Cartão)",
        fontSize = 13.sp,
        color = secondaryText,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    plans.forEach { plan ->
        val isSelected = plan.id == selectedPlanId
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = if (isSelected) highlightColor.copy(alpha = if (isLightMode) 0.10f else 0.12f) else cardBg,
            border = BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) highlightColor else cardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onSelectPlan(plan.id) }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) highlightColor else Color.Transparent)
                        .border(
                            BorderStroke(1.5.dp, if (isSelected) highlightColor else mutedText),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = primaryButtonTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plan.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryText
                        )
                        if (!plan.badge.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(highlightColor.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = plan.badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (highlightColor == Color.White && isLightMode) primaryText else highlightColor
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = plan.subtitle,
                        fontSize = 12.sp,
                        color = secondaryText
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = plan.priceFormatted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = emailInput,
        onValueChange = onEmailChange,
        label = { Text("E-mail para vincular sua licença", fontSize = 12.sp) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = secondaryText,
                modifier = Modifier.size(18.dp)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = highlightColor,
            unfocusedBorderColor = cardBorder,
            focusedTextColor = primaryText,
            unfocusedTextColor = primaryText,
            focusedLabelColor = highlightColor,
            unfocusedLabelColor = secondaryText,
            cursorColor = highlightColor
        ),
        modifier = Modifier.fillMaxWidth()
    )

    if (!checkoutMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = checkoutMessage,
            fontSize = 12.sp,
            color = if (isLightMode) Color(0xFFB91C1C) else Color(0xFFFCA5A5),
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onPayPix,
        enabled = !isCreatingCheckout,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = highlightColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isCreatingCheckout) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = primaryButtonTextColor
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Gerando pagamento...",
                fontWeight = FontWeight.SemiBold,
                color = primaryButtonTextColor
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.QrCode2,
                contentDescription = null,
                tint = primaryButtonTextColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pagar com Pix Instantâneo",
                fontWeight = FontWeight.SemiBold,
                color = primaryButtonTextColor
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedButton(
        onClick = onPayCard,
        enabled = !isCreatingCheckout,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CreditCard,
            contentDescription = null,
            tint = primaryText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Pagar com Cartão (Mercado Pago)",
            fontWeight = FontWeight.Medium,
            color = primaryText
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    TextButton(onClick = onToggleRestoreSection) {
        Icon(
            imageVector = Icons.Outlined.Restore,
            contentDescription = null,
            tint = secondaryText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (showRestoreSection) "Ocultar restauração" else "Já adquiriu? Restaurar compra",
            fontSize = 12.sp,
            color = secondaryText
        )
    }

    AnimatedVisibility(visible = showRestoreSection) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 1.dp,
                color = if (isLightMode) LightDivider else Color(0xFF1E1E26),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            OutlinedTextField(
                value = restoreEmailInput,
                onValueChange = onRestoreEmailChange,
                label = { Text("E-mail usado na compra", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = highlightColor,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onRestore,
                enabled = !isRestoringPurchase,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                if (isRestoringPurchase) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isRestoringPurchase) "Consultando..." else "Restaurar Licença",
                    fontWeight = FontWeight.Medium,
                    color = primaryText
                )
            }
        }
    }
}

private fun decodeBase64ImageBitmap(base64Str: String?): ImageBitmap? {
    if (base64Str.isNullOrBlank()) return null
    return runCatching {
        val clean = base64Str.substringAfter("base64,", base64Str)
        val bytes = Base64.decode(clean, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun openUrlInCustomTabs(
    context: Context,
    url: String,
    toolbarColor: Color,
    isLightMode: Boolean
) {
    val uri = Uri.parse(url)
    val launchedCustomTab = runCatching {
        val colorParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(toolbarColor.toArgb())
            .build()
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .setColorScheme(
                if (isLightMode) CustomTabsIntent.COLOR_SCHEME_LIGHT
                else CustomTabsIntent.COLOR_SCHEME_DARK
            )
            .setDefaultColorSchemeParams(colorParams)
            .build()
        if (context !is Activity) {
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        customTabsIntent.launchUrl(context, uri)
    }.isSuccess

    if (launchedCustomTab) return

    runCatching {
        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
    }.onFailure {
        Toast.makeText(context, "Não foi possível abrir o navegador.", Toast.LENGTH_SHORT).show()
    }
}
