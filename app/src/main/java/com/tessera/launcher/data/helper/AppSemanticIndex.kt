package com.tessera.launcher.data.helper

import com.tessera.launcher.data.model.AppInfo

/**
 * Motor de busca semântica offline minimalista.
 * Utiliza um dicionário de palavras-chave (tags) associadas a categorias e nomes de pacotes comuns,
 * permitindo encontrar apps por função (ex: "foto" -> acha Camera/Galeria).
 */
class AppSemanticIndex {
    
    // Mapeamento semântico enriquecido com sinônimos e termos cotidianos em português
    private val keywordMap = mapOf(
        "foto" to listOf("camera", "gallery", "photos", "snapseed", "instagram", "imagem", "galeria"),
        "imagem" to listOf("gallery", "photos", "camera", "snapseed"),
        "camera" to listOf("camera", "cam", "lens", "photos"),
        "galeria" to listOf("gallery", "photos", "media"),
        "video" to listOf("youtube", "netflix", "tiktok", "player", "primevideo", "disney", "max", "twitch"),
        "filme" to listOf("netflix", "primevideo", "disney", "max", "youtube"),
        "streaming" to listOf("netflix", "spotify", "primevideo", "disney", "twitch", "youtube"),
        "musica" to listOf("spotify", "music", "deezer", "apple.music", "youtube.music", "sound", "radio"),
        "audio" to listOf("spotify", "music", "podcast", "sound"),
        "podcast" to listOf("spotify", "podcast", "google.android.apps.podcasts"),
        "mensagem" to listOf("whatsapp", "telegram", "messenger", "sms", "messaging"),
        "chat" to listOf("whatsapp", "telegram", "messenger", "discord", "slack"),
        "conversa" to listOf("whatsapp", "telegram", "messenger"),
        "email" to listOf("gmail", "outlook", "mail", "yahoo"),
        "correio" to listOf("gmail", "outlook", "mail", "correios"),
        "banco" to listOf("nubank", "itau", "bradesco", "santander", "inter", "c6", "picpay", "bank", "caixa", "bb"),
        "dinheiro" to listOf("nubank", "itau", "bradesco", "santander", "inter", "c6", "picpay", "bank", "carteira"),
        "pagamento" to listOf("picpay", "mercadopago", "wallet", "pay", "pagseguro", "recargapay"),
        "pix" to listOf("nubank", "itau", "bradesco", "santander", "inter", "c6", "picpay", "mercadopago"),
        "financa" to listOf("nubank", "inter", "wallet", "invest", "organizze", "mobills"),
        "investimento" to listOf("nuinvest", "clear", "rico", "xp", "inter", "binance"),
        "comida" to listOf("ifood", "uber.eats", "rappi", "delivery", "aiqfome", "habibs", "mcdonalds"),
        "almoco" to listOf("ifood", "rappi", "delivery"),
        "refeicao" to listOf("ifood", "rappi", "delivery"),
        "entrega" to listOf("ifood", "rappi", "mercadolivre", "correios", "shopee"),
        "transporte" to listOf("uber", "99", "indrive", "waze", "maps", "moovit"),
        "viagem" to listOf("uber", "99", "maps", "booking", "airbnb", "tripadvisor"),
        "corrida" to listOf("uber", "99", "indrive"),
        "mapa" to listOf("maps", "waze", "navigation", "gps"),
        "gps" to listOf("maps", "waze", "navigation"),
        "rota" to listOf("maps", "waze", "moovit"),
        "navegador" to listOf("chrome", "firefox", "brave", "edge", "opera", "browser"),
        "internet" to listOf("chrome", "firefox", "brave", "edge", "opera", "browser"),
        "social" to listOf("instagram", "facebook", "twitter", "reddit", "linkedin", "tiktok", "bsky", "threads"),
        "rede" to listOf("instagram", "facebook", "twitter", "reddit", "linkedin", "tiktok", "bsky"),
        "compras" to listOf("shopee", "mercadolivre", "amazon", "aliexpress", "shein", "magalu"),
        "loja" to listOf("shopee", "mercadolivre", "amazon", "aliexpress", "shein", "magalu"),
        "nota" to listOf("keep", "notes", "evernote", "notion", "todo"),
        "anotacao" to listOf("keep", "notes", "evernote", "notion"),
        "tarefa" to listOf("todo", "keep", "tasks", "ticktick"),
        "documento" to listOf("docs", "word", "pdf", "drive", "office"),
        "pdf" to listOf("pdf", "acrobat", "drive", "docs"),
        "arquivo" to listOf("files", "explorer", "arquivos", "drive"),
        "nuvem" to listOf("drive", "dropbox", "onedrive", "cloud"),
        "sistema" to listOf("settings", "config"),
        "ajuste" to listOf("settings", "config"),
        "relogio" to listOf("clock", "alarm", "timer"),
        "alarme" to listOf("clock", "alarm"),
        "despertador" to listOf("clock", "alarm"),
        "calendario" to listOf("calendar", "agenda"),
        "agenda" to listOf("calendar", "agenda", "contatos"),
        "clima" to listOf("weather", "tempo", "clima"),
        "tempo" to listOf("weather", "tempo"),
        "jogos" to listOf("game", "games", "play.games"),
        "game" to listOf("game", "games", "play.games")
    )

    /**
     * Retorna o grau de relevância semântica de um app para a query fornecida.
     * @return score (0.0 se não for relevante)
     */
    fun scoreApp(app: AppInfo, query: String, normalizedQuery: String): Double {
        var score = 0.0
        val pkg = app.packageName.lowercase()
        val lbl = app.normalizedLabel

        // Match direto sempre tem prioridade máxima, mas isso já é lidado pela busca normal.
        // Aqui estamos adicionando peso semântico.
        
        for ((keyword, packages) in keywordMap) {
            // Se a query contiver a palavra-chave
            if (normalizedQuery.contains(keyword)) {
                // Verificar se o app está na lista de pacotes associados a essa palavra-chave
                if (packages.any { pkg.contains(it) }) {
                    score += 5.0
                }
                
                // Bônus se o nome do app também contiver a palavra-chave (às vezes o pacote não é claro)
                if (lbl.contains(keyword)) {
                    score += 2.0
                }
            }
        }

        return score
    }
}
