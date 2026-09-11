package com.tessera.launcher.data.helper

import com.tessera.launcher.data.model.AppInfo

/**
 * Motor de busca semântica offline minimalista.
 * Utiliza um dicionário de palavras-chave (tags) associadas a categorias e nomes de pacotes comuns,
 * permitindo encontrar apps por função (ex: "foto" -> acha Camera/Galeria).
 */
class AppSemanticIndex {
    
    // Mapeamento de termos de busca comuns para pacotes (ou partes de pacotes)
    private val keywordMap = mapOf(
        "foto" to listOf("camera", "gallery", "photos", "snapseed", "instagram"),
        "imagem" to listOf("gallery", "photos"),
        "video" to listOf("youtube", "netflix", "tiktok", "player"),
        "musica" to listOf("spotify", "music", "deezer", "apple.music", "youtube.music"),
        "audio" to listOf("spotify", "music", "podcast"),
        "mensagem" to listOf("whatsapp", "telegram", "messenger", "sms"),
        "chat" to listOf("whatsapp", "telegram", "messenger", "discord"),
        "email" to listOf("gmail", "outlook", "mail"),
        "banco" to listOf("nubank", "itau", "bradesco", "santander", "inter", "c6", "picpay", "bank"),
        "pagamento" to listOf("picpay", "mercadopago", "wallet", "pay"),
        "comida" to listOf("ifood", "uber.eats", "rappi", "delivery"),
        "transporte" to listOf("uber", "99", "indrive", "waze", "maps"),
        "mapa" to listOf("maps", "waze", "navigation"),
        "navegador" to listOf("chrome", "firefox", "brave", "edge", "opera", "browser"),
        "internet" to listOf("chrome", "firefox", "brave", "edge", "opera", "browser"),
        "social" to listOf("instagram", "facebook", "twitter", "reddit", "linkedin", "tiktok", "bsky"),
        "compras" to listOf("shopee", "mercadolivre", "amazon", "aliexpress", "shein"),
        "nota" to listOf("keep", "notes", "evernote", "notion"),
        "documento" to listOf("docs", "word", "pdf", "drive"),
        "arquivo" to listOf("files", "explorer", "arquivos"),
        "sistema" to listOf("settings", "config"),
        "relogio" to listOf("clock", "alarm", "timer"),
        "calendario" to listOf("calendar", "agenda"),
        "clima" to listOf("weather", "tempo")
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
