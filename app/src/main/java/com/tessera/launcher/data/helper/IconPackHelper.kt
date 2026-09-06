package com.tessera.launcher.data.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null
)

class IconPackHelper(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    // Cache em memória do pacote carregado atualmente:
    // Chave: "com.example.app/com.example.app.MainActivity" ou "com.example.app"
    // Valor: Nome do drawable (ex: "com_example_app")
    private var cachedIconPackPackage: String? = null
    private var cachedComponentMap: Map<String, String> = emptyMap()
    private var cachedResources: Resources? = null

    companion object {
        private val ICON_PACK_INTENT_ACTIONS = listOf(
            "com.novalauncher.THEME",
            "org.adw.launcher.THEMES",
            "com.gau.go.launcherex.theme",
            "com.fede.launcher.THEME_ICONPACK",
            "com.dlto.atom.launcher.THEME",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME"
        )
    }

    /**
     * Retorna a lista de todos os pacotes de ícones instalados no sistema.
     */
    fun getInstalledIconPacks(): List<IconPackInfo> {
        val result = mutableListOf<IconPackInfo>()
        val seenPackages = mutableSetOf<String>()

        for (action in ICON_PACK_INTENT_ACTIONS) {
            val intent = Intent(action)
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (!seenPackages.contains(pkg) && pkg != context.packageName) {
                    seenPackages.add(pkg)
                    val label = runCatching { info.loadLabel(packageManager).toString() }.getOrDefault(pkg)
                    val icon = runCatching { info.loadIcon(packageManager) }.getOrNull()
                    result.add(IconPackInfo(packageName = pkg, label = label, icon = icon))
                }
            }
        }
        return result.sortedBy { it.label.lowercase() }
    }

    /**
     * Carrega o appfilter.xml do pacote de ícones e armazena em cache.
     */
    @Synchronized
    fun preloadIconPack(iconPackPackage: String?): Boolean {
        if (iconPackPackage.isNullOrBlank()) {
            cachedIconPackPackage = null
            cachedComponentMap = emptyMap()
            cachedResources = null
            return false
        }

        if (cachedIconPackPackage == iconPackPackage && cachedComponentMap.isNotEmpty()) {
            return true
        }

        try {
            val iconPackRes = packageManager.getResourcesForApplication(iconPackPackage)
            val map = mutableMapOf<String, String>()

            // 1. Tenta carregar de res/xml/appfilter.xml
            var parser: XmlPullParser? = null
            val resId = iconPackRes.getIdentifier("appfilter", "xml", iconPackPackage)
            if (resId != 0) {
                parser = iconPackRes.getXml(resId)
            }

            // 2. Se não encontrou no res/xml, tenta carregar dos assets
            var assetStream: InputStream? = null
            if (parser == null) {
                try {
                    assetStream = iconPackRes.assets.open("appfilter.xml")
                    val factory = XmlPullParserFactory.newInstance()
                    parser = factory.newPullParser().apply {
                        setInput(assetStream, "utf-8")
                    }
                } catch (_: Exception) {
                    // Sem appfilter nos assets
                }
            }

            if (parser != null) {
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                        val component = parser.getAttributeValue(null, "component")
                        val drawableName = parser.getAttributeValue(null, "drawable")

                        if (!component.isNullOrBlank() && !drawableName.isNullOrBlank()) {
                            // Extrai do formato "ComponentInfo{com.whatsapp/com.whatsapp.HomeActivity}"
                            val cleanComp = component
                                .removePrefix("ComponentInfo{")
                                .removeSuffix("}")
                                .trim()

                            map[cleanComp] = drawableName

                            // Também mapeia pelo package name simples como fallback
                            val pkgOnly = cleanComp.substringBefore("/")
                            if (!map.containsKey(pkgOnly)) {
                                map[pkgOnly] = drawableName
                            }
                        }
                    }
                    eventType = parser.next()
                }
                assetStream?.close()
            }

            cachedIconPackPackage = iconPackPackage
            cachedComponentMap = map
            cachedResources = iconPackRes
            return true
        } catch (e: Exception) {
            cachedIconPackPackage = null
            cachedComponentMap = emptyMap()
            cachedResources = null
            return false
        }
    }

    /**
     * Retorna o ícone do app a partir do pacote de ícones ativo.
     * Retorna null se não houver ícone customizado, para que o chamador faça fallback para o ícone padrão.
     */
    fun getIconForApp(
        packageName: String,
        activityName: String?,
        iconPackPackage: String?
    ): Drawable? {
        if (iconPackPackage.isNullOrBlank()) return null

        if (cachedIconPackPackage != iconPackPackage) {
            preloadIconPack(iconPackPackage)
        }

        val res = cachedResources ?: return null
        val fullKey = if (!activityName.isNullOrBlank()) "$packageName/$activityName" else packageName

        val drawableName = cachedComponentMap[fullKey] ?: cachedComponentMap[packageName] ?: return null

        val drawableResId = res.getIdentifier(drawableName, "drawable", iconPackPackage)
        if (drawableResId == 0) return null

        return runCatching {
            ResourcesCompat.getDrawable(res, drawableResId, null)
        }.getOrNull()
    }
}
