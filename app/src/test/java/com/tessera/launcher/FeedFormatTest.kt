package com.tessera.launcher

import com.tessera.launcher.data.repository.FeedRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FeedFormatTest {

    @Test
    fun formatFeedText_cdataWithImgAndClosing_removesSymbolsAndImg() {
        val raw = "<![CDATA[ <img src=\"https://s2-g1.glbimg.com/test.jpeg\" /><br /> ]]> g1 20 anos: relembre as mudanças no Vaticano durante o período"
        val result = FeedRepository.formatFeedText(raw)
        assertFalse("Deveria remover ]]>", result.contains("]]>"))
        assertFalse("Deveria remover CDATA", result.contains("CDATA"))
        assertFalse("Deveria remover tag img", result.contains("<img"))
        assertEquals(
            "g1 20 anos: relembre as mudanças no Vaticano durante o período",
            result
        )
    }

    @Test
    fun formatFeedText_deduplicatesTitleAtStart() {
        val title = "Como é a nova regra do PIX que pode ajudar a inibir golpes"
        val raw = "]]> Como é a nova regra do PIX que pode ajudar a inibir golpes Bruno Peres/Agência Brasil via BBC Se você usa PIX no dia a dia"
        val result = FeedRepository.formatFeedText(raw, titleToDeduplicate = title)
        assertFalse("Deveria remover ]]>", result.contains("]]>"))
        assertFalse("Deveria deduplicar o título no início", result.startsWith(title))
        assertEquals(
            "Bruno Peres/Agência Brasil via BBC Se você usa PIX no dia a dia",
            result
        )
    }

    @Test
    fun formatFeedText_htmlEntities_decodesProperly() {
        val raw = "&quot;Apple&quot; anuncia sucess&atilde;o &amp; mudan&ccedil;as &mdash; confira!"
        val result = FeedRepository.formatFeedText(raw)
        assertEquals(
            "\"Apple\" anuncia sucessão & mudanças — confira!",
            result
        )
    }

    @Test
    fun formatFeedText_strayLeadingPunctuation_removesCleanly() {
        val raw = "]]> - — : Notícia importante sobre tecnologia."
        val result = FeedRepository.formatFeedText(raw)
        assertEquals("Notícia importante sobre tecnologia.", result)
    }
}
