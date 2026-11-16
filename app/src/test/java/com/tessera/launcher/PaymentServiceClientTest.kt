package com.tessera.launcher

import com.tessera.launcher.data.helper.PaymentServiceClient
import com.tessera.launcher.data.model.DEFAULT_PRO_PLANS
import com.tessera.launcher.data.model.LicenseInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentServiceClientTest {

    @Test
    fun parsePlansResponse_parsesMonthlyAndLifetimePlans() {
        val json = """
            {
              "plans": [
                {
                  "id": "pro_monthly",
                  "title": "Tessera Pro Mensal",
                  "subtitle": "Renovação a cada 30 dias.",
                  "priceBrl": 4.9,
                  "priceFormatted": "R$ 4,90 / mês",
                  "durationDays": 30,
                  "badge": null
                },
                {
                  "id": "pro_lifetime",
                  "title": "Tessera Pro Vitalício",
                  "subtitle": "Pagamento único.",
                  "priceBrl": 24.9,
                  "priceFormatted": "R$ 24,90",
                  "durationDays": null,
                  "badge": "Mais Popular"
                }
              ]
            }
        """.trimIndent()

        val plans = PaymentServiceClient.parsePlansResponse(json)
        assertEquals(2, plans.size)

        assertEquals("pro_monthly", plans[0].id)
        assertEquals(30, plans[0].durationDays)
        assertNull(plans[0].badge)
        assertEquals("R$ 4,90 / mês", plans[0].priceFormatted)

        assertEquals("pro_lifetime", plans[1].id)
        assertNull(plans[1].durationDays)
        assertEquals("Mais Popular", plans[1].badge)
        assertEquals("R$ 24,90", plans[1].priceFormatted)
    }

    @Test
    fun parsePlansResponse_emptyArrayFallsBackToDefaults() {
        val plans = PaymentServiceClient.parsePlansResponse("""{"plans":[]}""")
        assertEquals(DEFAULT_PRO_PLANS, plans)
    }

    @Test
    fun buildCreateCheckoutBody_normalizesAndEscapesJson() {
        val body = PaymentServiceClient.buildCreateCheckoutBody(
            planId = "pro_lifetime",
            email = "  User@Example.COM ",
            method = "PIX"
        )
        assertEquals(
            """{"planId":"pro_lifetime","email":"user@example.com","method":"pix"}""",
            body
        )
    }

    @Test
    fun parseCheckoutSession_parsesPixAndCardResponses() {
        val pixJson = """
            {
              "paymentId": "123456789",
              "externalReference": "tess_abc",
              "planId": "pro_monthly",
              "method": "pix",
              "status": "pending",
              "pixCopyPaste": "00020126580014br.gov.bcb.pix",
              "qrCodeBase64": "iVBORw0KGgo=",
              "checkoutUrl": "https://www.mercadopago.com.br/payments/123"
            }
        """.trimIndent()

        val pixSession = PaymentServiceClient.parseCheckoutSession(pixJson)
        assertEquals("123456789", pixSession.paymentId)
        assertEquals("tess_abc", pixSession.externalReference)
        assertEquals("pro_monthly", pixSession.planId)
        assertEquals("pix", pixSession.method)
        assertEquals("pending", pixSession.status)
        assertEquals("00020126580014br.gov.bcb.pix", pixSession.pixCopyPaste)
        assertEquals("iVBORw0KGgo=", pixSession.qrCodeBase64)

        val cardJson = """
            {
              "paymentId": "tess_card_1",
              "planId": "pro_lifetime",
              "method": "card",
              "status": "pending",
              "pixCopyPaste": null,
              "qrCodeBase64": null,
              "checkoutUrl": "https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=xyz"
            }
        """.trimIndent()

        val cardSession = PaymentServiceClient.parseCheckoutSession(cardJson)
        assertEquals("card", cardSession.method)
        assertNull(cardSession.pixCopyPaste)
        assertTrue(cardSession.checkoutUrl!!.startsWith("https://www.mercadopago.com.br"))
    }

    @Test(expected = IllegalStateException::class)
    fun parseCheckoutSession_throwsWhenServerReturnsError() {
        PaymentServiceClient.parseCheckoutSession("""{"error":"Informe um e-mail válido."}""")
    }

    @Test
    fun parseStatusResponse_parsesApprovedAndNotFound() {
        val approvedJson = """
            {
              "status": "approved",
              "isProActive": true,
              "activePlanId": "pro_lifetime",
              "licensedEmail": "cliente@tessera.app",
              "licenseToken": "payload.sig",
              "activatedAt": 1700000000000,
              "expiresAt": 0
            }
        """.trimIndent()

        val approved = PaymentServiceClient.parseStatusResponse(approvedJson)
        assertEquals("approved", approved.status)
        val approvedLicense = approved.licenseInfo
        assertNotNull(approvedLicense)
        assertTrue(approvedLicense!!.isProActive)
        assertEquals("pro_lifetime", approvedLicense.activePlanId)
        assertEquals("cliente@tessera.app", approvedLicense.licensedEmail)
        assertEquals("payload.sig", approvedLicense.licenseToken)
        assertEquals(0L, approvedLicense.expiresAt)

        val notFoundJson = """
            {
              "status": "not_found",
              "isProActive": false,
              "message": "Nenhuma licença ativa encontrada para este e-mail."
            }
        """.trimIndent()

        val notFound = PaymentServiceClient.parseStatusResponse(notFoundJson)
        assertEquals("not_found", notFound.status)
        assertNull(notFound.licenseInfo)
        assertEquals("Nenhuma licença ativa encontrada para este e-mail.", notFound.message)
    }

    @Test
    fun licenseInfo_isCurrentlyValid_respectsMonthlyExpirationAndLifetime() {
        val now = 2_000_000_000_000L

        val lifetime = LicenseInfo(
            isProActive = true,
            activePlanId = "pro_lifetime",
            activatedAt = 1_000_000_000_000L,
            expiresAt = 0L
        )
        assertTrue(lifetime.isCurrentlyValid(now))

        val activeMonthly = LicenseInfo(
            isProActive = true,
            activePlanId = "pro_monthly",
            activatedAt = now - 86_400_000L,
            expiresAt = now + 86_400_000L
        )
        assertTrue(activeMonthly.isCurrentlyValid(now))

        val expiredMonthly = activeMonthly.copy(expiresAt = now - 1L)
        assertFalse(expiredMonthly.isCurrentlyValid(now))

        val inactive = lifetime.copy(isProActive = false)
        assertFalse(inactive.isCurrentlyValid(now))
    }

    @Test
    fun isBackendConfigured_validatesHttpsAndPlaceholder() {
        assertFalse(PaymentServiceClient.isBackendConfigured("https://tessera-pay.seu-usuario.workers.dev"))
        assertFalse(PaymentServiceClient.isBackendConfigured("http://tessera-pay.meudominio.workers.dev"))
        assertTrue(PaymentServiceClient.isBackendConfigured("https://tessera-pay.kenned.workers.dev"))
    }
}
