package com.tessera.launcher

import com.tessera.launcher.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class AppInfoTest {

    @Test
    fun computeFirstLetter_standardLetters_returnsUppercaseChar() {
        assertEquals('A', AppInfo.computeFirstLetter("App Store"))
        assertEquals('B', AppInfo.computeFirstLetter("browser"))
        assertEquals('Z', AppInfo.computeFirstLetter("Zenith"))
    }

    @Test
    fun computeFirstLetter_numbersOrSymbols_returnsHash() {
        assertEquals('#', AppInfo.computeFirstLetter("1Password"))
        assertEquals('#', AppInfo.computeFirstLetter("2048"))
        assertEquals('#', AppInfo.computeFirstLetter("@twitter"))
        assertEquals('#', AppInfo.computeFirstLetter(""))
    }

    @Test
    fun appInfo_withShortcut_preservesShortcutMetadata() {
        val app = AppInfo(
            label = "Twitter PWA",
            packageName = "com.android.chrome",
            activityName = "",
            icon = null,
            isShortcut = true,
            shortcutId = "https://x.com/",
            shortcutIntentUri = "intent:#Intent;action=android.intent.action.VIEW;end"
        )

        assertEquals("Twitter PWA", app.label)
        assertEquals('T', app.firstLetter)
        assertEquals(true, app.isShortcut)
        assertEquals("https://x.com/", app.shortcutId)
        assertEquals("intent:#Intent;action=android.intent.action.VIEW;end", app.shortcutIntentUri)
    }
}
