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
}
