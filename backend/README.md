# Backend Gratuito de Pagamentos — Tessera Launcher

Backend serverless 100% gratuito rodando em **Cloudflare Workers + Cloudflare D1 (SQLite)** integrado à API oficial do **Mercado Pago** (Pix dinâmico com QR Code/Copia e Cola + Cartão via Checkout Pro).

## Por que usar esse backend?
- **Grátis sem exigir cartão de crédito:** 100.000 requisições/dia no plano gratuito da Cloudflare.
- **Zero hibernação:** responde em milissegundos no Brasil a qualquer hora.
- **Segurança:** seu `MP_ACCESS_TOKEN` fica salvo apenas nos Secrets da Cloudflare — nunca dentro do APK.

---

## Passo a Passo para Publicar (5 minutos)

### 1. Pré-requisitos
- Node.js instalado (para usar `npx wrangler`).
- Conta gratuita na [Cloudflare](https://dash.cloudflare.com/).
- Conta no [Mercado Pago Developers](https://www.mercadopago.com.br/developers) com o **Access Token de Produção** da sua aplicação.

### 2. Autenticar na Cloudflare e Criar o Banco D1
Abra o terminal na pasta `backend/`:
```powershell
cd backend
npx wrangler login
npx wrangler d1 create tessera-licenses-db
```
Copie o `database_id` exibido no terminal e cole no arquivo `backend/wrangler.toml`.

Em seguida, crie a tabela no banco remoto:
```powershell
npx wrangler d1 execute tessera-licenses-db --remote --file=./schema.sql
```

### 3. Configurar suas Chaves Secretas
```powershell
npx wrangler secret put MP_ACCESS_TOKEN
# Cole seu Access Token de Produção do Mercado Pago

npx wrangler secret put LICENSE_SECRET
# Digite uma senha forte aleatória para assinar os tokens de licença

# Opcional/Recomendado após configurar Webhooks no painel do Mercado Pago:
npx wrangler secret put MP_WEBHOOK_SECRET
# Cole a "Assinatura secreta" gerada em Configuração de webhooks para validar x-signature
```

### 4. Fazer Deploy
```powershell
npx wrangler deploy
```
O terminal exibirá a URL pública do seu serviço, por exemplo:
`https://tessera-pay.seu-usuario.workers.dev`

### 5. Vincular no App Android
Abra o arquivo `app/src/main/java/com/tessera/launcher/data/helper/PaymentServiceClient.kt` e substitua `BACKEND_BASE_URL` pela URL gerada no passo anterior.

---

## Como alterar planos e preços
Edite o objeto `PLANS` no topo de `backend/src/index.js` (`pro_monthly` e `pro_lifetime`) e rode novamente `npx wrangler deploy`. As mudanças refletem imediatamente no modal do app sem precisar publicar novo APK.
