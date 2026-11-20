package com.tessera.launcher

import com.tessera.launcher.data.helper.MAX_PHOTO_WIDGET_COUNT
import com.tessera.launcher.data.helper.PhotoWidgetGalleryHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoWidgetGalleryTest {

    @Test
    fun mergeRespectsMaxLimitOfSixAndDeduplicates() {
        val initial = listOf("/p/1.jpg", "/p/2.jpg")
        val added = listOf("/p/2.jpg", "/p/3.jpg", "/p/4.jpg", "/p/5.jpg", "/p/6.jpg", "/p/7.jpg")
        val merged = PhotoWidgetGalleryHelper.mergePhotos(initial, added)

        assertEquals(MAX_PHOTO_WIDGET_COUNT, merged.size)
        assertEquals(listOf("/p/1.jpg", "/p/2.jpg", "/p/3.jpg", "/p/4.jpg", "/p/5.jpg", "/p/6.jpg"), merged)
        assertEquals(0, PhotoWidgetGalleryHelper.remainingSlots(merged.size))
    }

    @Test
    fun removePhotoAtClampsAndShiftsActiveIndexCorrectly() {
        val list = listOf("a", "b", "c", "d")

        // Removing before active shifts active left
        val (afterBefore, idxBefore) = PhotoWidgetGalleryHelper.removePhotoAt(list, index = 1, currentActiveIndex = 3)
        assertEquals(listOf("a", "c", "d"), afterBefore)
        assertEquals(2, idxBefore)

        // Removing last active clamps to new last element
        val (afterLast, idxLast) = PhotoWidgetGalleryHelper.removePhotoAt(list, index = 3, currentActiveIndex = 3)
        assertEquals(listOf("a", "b", "c"), afterLast)
        assertEquals(2, idxLast)

        // Removing final remaining element resets index to 0
        val (empty, idxEmpty) = PhotoWidgetGalleryHelper.removePhotoAt(listOf("only"), index = 0, currentActiveIndex = 0)
        assertEquals(emptyList<String>(), empty)
        assertEquals(0, idxEmpty)
    }

    @Test
    fun remainingSlotsNeverNegativeOrAboveSix() {
        assertEquals(6, PhotoWidgetGalleryHelper.remainingSlots(0))
        assertEquals(4, PhotoWidgetGalleryHelper.remainingSlots(2))
        assertEquals(0, PhotoWidgetGalleryHelper.remainingSlots(6))
        assertEquals(0, PhotoWidgetGalleryHelper.remainingSlots(99))
    }
}
