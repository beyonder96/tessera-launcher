package com.tessera.launcher.data.helper

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.tessera.launcher.data.model.AppCategory
import com.tessera.launcher.data.model.AppInfo
import java.util.concurrent.ConcurrentHashMap

/**
 * Classificador semântico e contextual de aplicativos.
 * Combina a categoria oficial declarada pelo Android com regras heurísticas de alta precisão
 * para classificar aplicativos em categorias essenciais e minimalistas.
 */
class AppCategoryClassifier(private val context: Context) {

    private val cache = ConcurrentHashMap<String, AppCategory>()

    // Mapeamento heurístico de palavras-chave / pacotes
    private val socialKeywords = listOf(
        "whatsapp", "telegram", "instagram", "facebook", "twitter", "reddit",
        "tiktok", "discord", "slack", "messenger", "threads", "bluesky", "bsky",
        "linkedin", "snapchat", "wechat", "signal", "viber", "skype", "social", "chat"
    )

    private val productivityKeywords = listOf(
        "notes", "keep", "notion", "todo", "task", "ticktick", "evernote",
        "calendar", "agenda", "mail", "gmail", "outlook", "yahoo", "docs", "word",
        "excel", "sheets", "slides", "powerpoint", "pdf", "drive", "dropbox",
        "onedrive", "scanner", "nubank", "itau", "bradesco", "santander", "inter",
        "c6", "picpay", "bank", "banco", "wallet", "carteira", "invest", "xp", "rico",
        "clear", "binance", "mercadopago", "pagseguro", "recargapay", "mobills", "organizze"
    )

    private val mediaKeywords = listOf(
        "camera", "photos", "gallery", "galeria", "snapseed", "lightroom", "vlc",
        "youtube", "spotify", "music", "deezer", "netflix", "primevideo", "disney",
        "hbo", "max", "twitch", "tiktok", "podcast", "audio", "player", "sound",
        "foto", "video", "cinema", "crunchyroll", "globoplay"
    )

    private val utilitiesKeywords = listOf(
        "chrome", "firefox", "brave", "edge", "opera", "browser", "navegador",
        "settings", "config", "clock", "relogio", "alarm", "alarme", "calculator",
        "calculadora", "files", "arquivos", "explorer", "maps", "waze", "moovit",
        "uber", "99", "indrive", "ifood", "rappi", "delivery", "shopee", "mercadolivre",
        "amazon", "shein", "aliexpress", "weather", "clima", "tempo", "security", "vpn"
    )

    private val gameKeywords = listOf(
        "game", "games", "play.games", "unity", "craft", "subway", "clash", "roblox",
        "freefire", "pubg", "brawl", "pokemon", "candy", "asphalt", "ea.", "riotgames"
    )

    fun classifyApp(app: AppInfo): AppCategory {
        return cache.getOrPut(app.packageName) {
            resolveCategory(app)
        }
    }

    private fun resolveCategory(app: AppInfo): AppCategory {
        val pkg = app.packageName.lowercase()
        val label = app.normalizedLabel

        // 1. Verificação oficial da categoria do sistema Android (API 26+)
        try {
            val appInfo = context.packageManager.getApplicationInfo(app.packageName, 0)
            val osCategory = appInfo.category
            when (osCategory) {
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_SOCIAL,
                ApplicationInfo.CATEGORY_NEWS -> return AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
                ApplicationInfo.CATEGORY_MAPS,
                ApplicationInfo.CATEGORY_ACCESSIBILITY -> return AppCategory.UTILITIES
            }
        } catch (_: Exception) {}

        // 2. Classificação Heurística por Pacote e Rótulo
        if (gameKeywords.any { pkg.contains(it) || label.contains(it) }) {
            return AppCategory.GAMES
        }
        if (socialKeywords.any { pkg.contains(it) || label.contains(it) }) {
            return AppCategory.SOCIAL
        }
        if (productivityKeywords.any { pkg.contains(it) || label.contains(it) }) {
            return AppCategory.PRODUCTIVITY
        }
        if (mediaKeywords.any { pkg.contains(it) || label.contains(it) }) {
            return AppCategory.MEDIA
        }
        if (utilitiesKeywords.any { pkg.contains(it) || label.contains(it) }) {
            return AppCategory.UTILITIES
        }

        // 3. Fallback inteligente
        return AppCategory.UTILITIES
    }
}
