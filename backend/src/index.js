/**
 * Tessera Launcher — Backend Serverless de Pagamentos (Cloudflare Workers + D1 + Mercado Pago)
 *
 * Secrets obrigatórios configurados na Cloudflare (NUNCA colocar no código ou APK):
 *   - MP_ACCESS_TOKEN   : Access Token de produção do Mercado Pago
 *   - LICENSE_SECRET    : Chave secreta para assinatura HMAC-SHA256 dos tokens de licença
 *
 * Secret recomendado (obtido ao configurar Webhooks no painel do Mercado Pago):
 *   - MP_WEBHOOK_SECRET : Assinatura secreta para validar o cabeçalho x-signature
 */

const MP_STATEMENT_DESCRIPTOR = "TESSERAAPP";
const MP_ITEM_CATEGORY_ID = "services";

// Edite livremente os planos, títulos e preços aqui sem precisar atualizar o APK:
const PLANS = {
  pro_monthly: {
    id: "pro_monthly",
    title: "Tessera Pro Mensal",
    subtitle: "Acesso completo aos recursos Pro com renovação flexível.",
    priceBrl: 4.90,
    priceFormatted: "R$ 4,90 / mês",
    durationDays: 30,
    badge: null
  },
  pro_lifetime: {
    id: "pro_lifetime",
    title: "Tessera Pro Vitalício",
    subtitle: "Pague uma única vez e tenha acesso permanente.",
    priceBrl: 24.90,
    priceFormatted: "R$ 24,90",
    durationDays: null,
    badge: "Mais Popular"
  }
};

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization"
};

function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      ...CORS_HEADERS
    }
  });
}

function normalizeEmail(email) {
  return String(email || "").trim().toLowerCase();
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

async function signLicenseToken(payloadObj, secret) {
  const secretKey = secret || "tessera-default-dev-secret";
  const encoder = new TextEncoder();
  const payloadStr = JSON.stringify(payloadObj);
  const payloadB64 = btoa(unescape(encodeURIComponent(payloadStr)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");

  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(secretKey),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const sigBuffer = await crypto.subtle.sign("HMAC", key, encoder.encode(payloadB64));
  const sigBytes = new Uint8Array(sigBuffer);
  let binary = "";
  for (let i = 0; i < sigBytes.length; i++) {
    binary += String.fromCharCode(sigBytes[i]);
  }
  const sigB64 = btoa(binary)
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");

  return `${payloadB64}.${sigB64}`;
}

function hexToBytes(hex) {
  const clean = String(hex || "").trim().toLowerCase();
  if (!/^[0-9a-f]+$/.test(clean) || clean.length % 2 !== 0) {
    return null;
  }
  const bytes = new Uint8Array(clean.length / 2);
  for (let i = 0; i < bytes.length; i++) {
    bytes[i] = parseInt(clean.slice(i * 2, i * 2 + 2), 16);
  }
  return bytes;
}

function parseMpSignatureHeader(headerValue) {
  const parsed = { ts: null, v1: null };
  if (!headerValue) return parsed;
  for (const part of String(headerValue).split(",")) {
    const eqIndex = part.indexOf("=");
    if (eqIndex <= 0) continue;
    const key = part.slice(0, eqIndex).trim();
    const val = part.slice(eqIndex + 1).trim();
    if (key === "ts") parsed.ts = val;
    if (key === "v1") parsed.v1 = val;
  }
  return parsed;
}

function buildWebhookManifest(dataId, requestId, ts) {
  let manifest = "";
  if (dataId != null && String(dataId).length > 0) {
    manifest += `id:${String(dataId).toLowerCase()};`;
  }
  if (requestId) {
    manifest += `request-id:${requestId};`;
  }
  manifest += `ts:${ts};`;
  return manifest;
}

async function verifyMercadoPagoWebhookSignature(request, url, body, secret) {
  if (!secret) return true;

  const xSignature = request.headers.get("x-signature");
  const xRequestId = request.headers.get("x-request-id");
  const { ts, v1 } = parseMpSignatureHeader(xSignature);
  const sigBytes = hexToBytes(v1);

  if (!ts || !sigBytes) {
    return false;
  }

  const queryDataId = url.searchParams.get("data.id") || url.searchParams.get("id");
  const bodyDataId = body?.data?.id != null ? String(body.data.id) : null;
  const candidateIds = [];
  if (queryDataId) candidateIds.push(queryDataId);
  if (bodyDataId && !candidateIds.includes(bodyDataId)) candidateIds.push(bodyDataId);
  if (candidateIds.length === 0) candidateIds.push(null);

  const encoder = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["verify"]
  );

  for (const candidateId of candidateIds) {
    const manifest = buildWebhookManifest(candidateId, xRequestId, ts);
    const ok = await crypto.subtle.verify("HMAC", key, sigBytes, encoder.encode(manifest));
    if (ok) return true;
  }

  return false;
}

async function fetchMpPayment(paymentId, mpAccessToken) {
  const res = await fetch(`https://api.mercadopago.com/v1/payments/${encodeURIComponent(paymentId)}`, {
    headers: {
      "Authorization": `Bearer ${mpAccessToken}`,
      "Accept": "application/json"
    }
  });
  if (!res.ok) return null;
  return await res.json();
}

async function searchMpPaymentByExternalRef(externalRef, mpAccessToken) {
  const url = `https://api.mercadopago.com/v1/payments/search?external_reference=${encodeURIComponent(externalRef)}&sort=date_created&criteria=desc&limit=1`;
  const res = await fetch(url, {
    headers: {
      "Authorization": `Bearer ${mpAccessToken}`,
      "Accept": "application/json"
    }
  });
  if (!res.ok) return null;
  const data = await res.json();
  return Array.isArray(data.results) && data.results.length > 0 ? data.results[0] : null;
}

async function activateLicenseFromMpPayment(mpPayment, env) {
  if (!mpPayment || mpPayment.status !== "approved") {
    return null;
  }

  const now = Date.now();
  const mpPaymentId = String(mpPayment.id);
  const externalRef = mpPayment.external_reference || null;
  const metaPlanId = mpPayment.metadata?.plan_id || null;
  const metaEmail = mpPayment.metadata?.email ? normalizeEmail(mpPayment.metadata.email) : null;

  let existingRow = null;
  if (externalRef) {
    existingRow = await env.DB.prepare(
      "SELECT * FROM licenses WHERE external_reference = ? LIMIT 1"
    ).bind(externalRef).first();
  }
  if (!existingRow) {
    existingRow = await env.DB.prepare(
      "SELECT * FROM licenses WHERE payment_id = ? LIMIT 1"
    ).bind(mpPaymentId).first();
  }

  const planId = PLANS[metaPlanId]?.id || PLANS[existingRow?.plan_id]?.id || "pro_lifetime";
  const plan = PLANS[planId];
  const email = metaEmail || existingRow?.email || normalizeEmail(mpPayment.payer?.email);
  const method = existingRow?.method || (mpPayment.payment_method_id === "pix" ? "pix" : "card");

  if (!email) return null;

  // Se já existe token aprovado para este mesmo payment_id, apenas devolve
  if (existingRow && existingRow.status === "approved" && existingRow.license_token && existingRow.payment_id === mpPaymentId) {
    return existingRow;
  }

  const activatedAt = existingRow?.activated_at && existingRow.activated_at > 0
    ? existingRow.activated_at
    : now;
  const expiresAt = plan.durationDays
    ? activatedAt + plan.durationDays * 24 * 60 * 60 * 1000
    : null;

  const licenseToken = await signLicenseToken(
    {
      paymentId: mpPaymentId,
      email,
      planId,
      activatedAt,
      expiresAt
    },
    env.LICENSE_SECRET
  );

  // Atualiza registro pendente criado no checkout ou insere novo
  if (existingRow) {
    await env.DB.prepare(
      `UPDATE licenses
       SET payment_id = ?, email = ?, plan_id = ?, method = ?, status = 'approved',
           license_token = ?, activated_at = ?, expires_at = ?, updated_at = ?
       WHERE payment_id = ?`
    )
      .bind(mpPaymentId, email, planId, method, licenseToken, activatedAt, expiresAt, now, existingRow.payment_id)
      .run();
  } else {
    await env.DB.prepare(
      `INSERT INTO licenses (payment_id, external_reference, email, plan_id, method, status, license_token, activated_at, expires_at, updated_at)
       VALUES (?, ?, ?, ?, ?, 'approved', ?, ?, ?, ?)
       ON CONFLICT(payment_id) DO UPDATE SET
         status = 'approved',
         license_token = excluded.license_token,
         activated_at = excluded.activated_at,
         expires_at = excluded.expires_at,
         updated_at = excluded.updated_at`
    )
      .bind(mpPaymentId, externalRef, email, planId, method, licenseToken, activatedAt, expiresAt, now)
      .run();
  }

  return {
    payment_id: mpPaymentId,
    external_reference: externalRef,
    email,
    plan_id: planId,
    method,
    status: "approved",
    license_token: licenseToken,
    activated_at: activatedAt,
    expires_at: expiresAt
  };
}

async function handleCreateCheckout(request, env, origin) {
  if (!env.MP_ACCESS_TOKEN) {
    return jsonResponse({ error: "MP_ACCESS_TOKEN não configurado no Worker." }, 500);
  }

  let body;
  try {
    body = await request.json();
  } catch {
    return jsonResponse({ error: "Corpo JSON inválido." }, 400);
  }

  const plan = PLANS[body.planId];
  const email = normalizeEmail(body.email);
  const method = body.method === "card" ? "card" : "pix";

  if (!plan) {
    return jsonResponse({ error: "Plano inválido." }, 400);
  }
  if (!isValidEmail(email)) {
    return jsonResponse({ error: "Informe um e-mail válido para vincular sua licença." }, 400);
  }

  const externalRef = `tess_${crypto.randomUUID()}`;
  const now = Date.now();
  const webhookUrl = `${origin}/api/webhook?source_news=webhooks`;

  if (method === "pix") {
    const mpPayload = {
      transaction_amount: plan.priceBrl,
      description: `${plan.title} — Tessera Launcher`,
      payment_method_id: "pix",
      statement_descriptor: MP_STATEMENT_DESCRIPTOR,
      external_reference: externalRef,
      notification_url: webhookUrl,
      payer: { email },
      additional_info: {
        items: [
          {
            id: plan.id,
            title: plan.title,
            description: plan.subtitle,
            category_id: MP_ITEM_CATEGORY_ID,
            quantity: 1,
            unit_price: plan.priceBrl
          }
        ]
      },
      metadata: {
        plan_id: plan.id,
        email
      }
    };

    const mpRes = await fetch("https://api.mercadopago.com/v1/payments", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${env.MP_ACCESS_TOKEN}`,
        "Content-Type": "application/json",
        "X-Idempotency-Key": externalRef
      },
      body: JSON.stringify(mpPayload)
    });

    const mpData = await mpRes.json();
    if (!mpRes.ok) {
      return jsonResponse({
        error: mpData.message || "Falha ao criar cobrança Pix no Mercado Pago."
      }, 502);
    }

    const paymentId = String(mpData.id);
    const txData = mpData.point_of_interaction?.transaction_data || {};

    await env.DB.prepare(
      `INSERT INTO licenses (payment_id, external_reference, email, plan_id, method, status, activated_at, expires_at, updated_at)
       VALUES (?, ?, ?, ?, 'pix', 'pending', 0, NULL, ?)`
    )
      .bind(paymentId, externalRef, email, plan.id, now)
      .run();

    return jsonResponse({
      paymentId,
      externalReference: externalRef,
      planId: plan.id,
      method: "pix",
      status: mpData.status || "pending",
      pixCopyPaste: txData.qr_code || null,
      qrCodeBase64: txData.qr_code_base64 || null,
      checkoutUrl: txData.ticket_url || null
    });
  }

  // Método "card": cria Preferência do Mercado Pago Checkout Pro com retorno via Deep Link
  const prefPayload = {
    items: [
      {
        id: plan.id,
        title: plan.title,
        description: plan.subtitle,
        category_id: MP_ITEM_CATEGORY_ID,
        quantity: 1,
        currency_id: "BRL",
        unit_price: plan.priceBrl
      }
    ],
    payer: { email },
    statement_descriptor: MP_STATEMENT_DESCRIPTOR,
    external_reference: externalRef,
    notification_url: webhookUrl,
    metadata: {
      plan_id: plan.id,
      email
    },
    back_urls: {
      success: `tessera://payment/callback?status=approved&plan=${plan.id}&ref=${externalRef}`,
      pending: `tessera://payment/callback?status=pending&plan=${plan.id}&ref=${externalRef}`,
      failure: `tessera://payment/callback?status=failure&plan=${plan.id}&ref=${externalRef}`
    },
    auto_return: "approved"
  };

  const prefRes = await fetch("https://api.mercadopago.com/checkout/preferences", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${env.MP_ACCESS_TOKEN}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify(prefPayload)
  });

  const prefData = await prefRes.json();
  if (!prefRes.ok) {
    return jsonResponse({
      error: prefData.message || "Falha ao criar sessão de cartão no Mercado Pago."
    }, 502);
  }

  await env.DB.prepare(
    `INSERT INTO licenses (payment_id, external_reference, email, plan_id, method, status, activated_at, expires_at, updated_at)
     VALUES (?, ?, ?, ?, 'card', 'pending', 0, NULL, ?)`
  )
    .bind(externalRef, externalRef, email, plan.id, now)
    .run();

  return jsonResponse({
    paymentId: externalRef,
    externalReference: externalRef,
    planId: plan.id,
    method: "card",
    status: "pending",
    pixCopyPaste: null,
    qrCodeBase64: null,
    checkoutUrl: prefData.init_point || prefData.sandbox_init_point || null
  });
}

async function handleWebhook(request, env) {
  if (!env.MP_ACCESS_TOKEN) {
    return jsonResponse({ ok: false }, 500);
  }

  const url = new URL(request.url);
  let body = {};
  try {
    body = await request.json();
  } catch {
    // Alguns webhooks antigos do MP enviam parâmetros apenas via querystring
  }

  const isSignatureValid = await verifyMercadoPagoWebhookSignature(
    request,
    url,
    body,
    env.MP_WEBHOOK_SECRET
  );
  if (!isSignatureValid) {
    return jsonResponse({ error: "Assinatura webhook inválida." }, 401);
  }

  const topic = body.type || body.topic || url.searchParams.get("topic") || url.searchParams.get("type");
  const dataId = body.data?.id || url.searchParams.get("data.id") || url.searchParams.get("id");

  if (topic === "payment" && dataId) {
    const mpPayment = await fetchMpPayment(dataId, env.MP_ACCESS_TOKEN);
    if (mpPayment && mpPayment.status === "approved") {
      await activateLicenseFromMpPayment(mpPayment, env);
    }
  }

  return jsonResponse({ received: true }, 200);
}

async function handleStatusOrRestore(url, env) {
  const paymentIdParam = (url.searchParams.get("paymentId") || "").trim();
  const emailParam = normalizeEmail(url.searchParams.get("email") || "");
  const now = Date.now();

  if (paymentIdParam) {
    let row = await env.DB.prepare(
      "SELECT * FROM licenses WHERE payment_id = ? OR external_reference = ? LIMIT 1"
    ).bind(paymentIdParam, paymentIdParam).first();

    // Fallback ativo na API do Mercado Pago caso o webhook ainda não tenha chegado
    if ((!row || row.status !== "approved") && env.MP_ACCESS_TOKEN) {
      let mpPayment = null;
      if (/^\d+$/.test(paymentIdParam)) {
        mpPayment = await fetchMpPayment(paymentIdParam, env.MP_ACCESS_TOKEN);
      } else {
        mpPayment = await searchMpPaymentByExternalRef(paymentIdParam, env.MP_ACCESS_TOKEN);
      }
      if (mpPayment && mpPayment.status === "approved") {
        row = await activateLicenseFromMpPayment(mpPayment, env);
      }
    }

    if (!row) {
      return jsonResponse({ status: "pending", isProActive: false });
    }

    const isExpired = row.expires_at != null && row.expires_at <= now;
    const isProActive = row.status === "approved" && !isExpired;

    return jsonResponse({
      status: isExpired ? "expired" : row.status,
      isProActive,
      activePlanId: row.plan_id,
      licensedEmail: row.email,
      licenseToken: row.license_token || null,
      activatedAt: row.activated_at || 0,
      expiresAt: row.expires_at || 0
    });
  }

  if (emailParam) {
    if (!isValidEmail(emailParam)) {
      return jsonResponse({ error: "E-mail inválido." }, 400);
    }

    const row = await env.DB.prepare(
      `SELECT * FROM licenses
       WHERE LOWER(email) = LOWER(?)
         AND status = 'approved'
         AND (expires_at IS NULL OR expires_at > ?)
       ORDER BY activated_at DESC
       LIMIT 1`
    ).bind(emailParam, now).first();

    if (!row) {
      return jsonResponse({
        status: "not_found",
        isProActive: false,
        message: "Nenhuma licença ativa encontrada para este e-mail."
      });
    }

    return jsonResponse({
      status: "approved",
      isProActive: true,
      activePlanId: row.plan_id,
      licensedEmail: row.email,
      licenseToken: row.license_token || null,
      activatedAt: row.activated_at || 0,
      expiresAt: row.expires_at || 0
    });
  }

  return jsonResponse({ error: "Informe paymentId ou email." }, 400);
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    const url = new URL(request.url);

    if (request.method === "GET" && url.pathname === "/api/plans") {
      return jsonResponse({ plans: Object.values(PLANS) });
    }

    if (request.method === "POST" && url.pathname === "/api/checkout/create") {
      return handleCreateCheckout(request, env, url.origin);
    }

    if (request.method === "POST" && url.pathname === "/api/webhook") {
      return handleWebhook(request, env);
    }

    if (request.method === "GET" && url.pathname === "/api/checkout/status") {
      return handleStatusOrRestore(url, env);
    }

    return jsonResponse({ error: "Rota não encontrada." }, 404);
  }
};
