package com.tessera.launcher

import com.tessera.launcher.data.model.AppNotificationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationHubAndBadgeTest {

    @Test
    fun testAppNotificationItemCreation() {
        val item = AppNotificationItem(
            key = "0|com.whatsapp|1|null|1000",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "João",
            text = "E aí, tudo bem?",
            timestamp = 1700000000000L,
            largeIcon = null,
            contentIntent = null,
            isOngoing = false
        )

        assertEquals("0|com.whatsapp|1|null|1000", item.key)
        assertEquals("com.whatsapp", item.packageName)
        assertEquals("WhatsApp", item.appName)
        assertEquals("João", item.title)
        assertEquals("E aí, tudo bem?", item.text)
        assertEquals(1700000000000L, item.timestamp)
        assertFalse(item.isOngoing)
    }

    @Test
    fun testBadgeCountAggregationByPackage() {
        val items = listOf(
            AppNotificationItem(
                key = "k1",
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "João",
                text = "Msg 1",
                timestamp = 1000L
            ),
            AppNotificationItem(
                key = "k2",
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Maria",
                text = "Msg 2",
                timestamp = 2000L
            ),
            AppNotificationItem(
                key = "k3",
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Grupo Dev",
                text = "Msg 3",
                timestamp = 3000L
            ),
            AppNotificationItem(
                key = "k4",
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Google",
                text = "Alerta de segurança",
                timestamp = 2500L
            )
        )

        val counts = mutableMapOf<String, Int>()
        items.forEach { item ->
            counts[item.packageName] = (counts[item.packageName] ?: 0) + 1
        }

        assertEquals(3, counts["com.whatsapp"])
        assertEquals(1, counts["com.google.android.gm"])
        assertEquals(null, counts["com.spotify.music"])
    }

    @Test
    fun testRecentNotificationsSortingDescending() {
        val item1 = AppNotificationItem("k1", "pkg1", "App1", "T1", "M1", timestamp = 1000L)
        val item2 = AppNotificationItem("k2", "pkg2", "App2", "T2", "M2", timestamp = 5000L)
        val item3 = AppNotificationItem("k3", "pkg3", "App3", "T3", "M3", timestamp = 3000L)

        val sorted = listOf(item1, item2, item3).sortedByDescending { it.timestamp }

        assertEquals("k2", sorted[0].key) // 5000L
        assertEquals("k3", sorted[1].key) // 3000L
        assertEquals("k1", sorted[2].key) // 1000L
    }

    @Test
    fun testDismissNotificationDecrementsCountAndRemovesItem() {
        val activeMap = mutableMapOf(
            "k1" to AppNotificationItem("k1", "com.whatsapp", "WhatsApp", "João", "Msg 1", 1000L),
            "k2" to AppNotificationItem("k2", "com.whatsapp", "WhatsApp", "Maria", "Msg 2", 2000L),
            "k3" to AppNotificationItem("k3", "com.google.android.gm", "Gmail", "Alerta", "Txt", 1500L)
        )

        // Dismiss k1 (WhatsApp)
        activeMap.remove("k1")

        val countsAfterDismiss = mutableMapOf<String, Int>()
        activeMap.values.forEach { item ->
            countsAfterDismiss[item.packageName] = (countsAfterDismiss[item.packageName] ?: 0) + 1
        }

        assertEquals(1, countsAfterDismiss["com.whatsapp"])
        assertEquals(1, countsAfterDismiss["com.google.android.gm"])
        assertEquals(2, activeMap.size)

        // Dismiss k2 (last WhatsApp notification)
        activeMap.remove("k2")

        val countsAfterSecondDismiss = mutableMapOf<String, Int>()
        activeMap.values.forEach { item ->
            countsAfterSecondDismiss[item.packageName] = (countsAfterSecondDismiss[item.packageName] ?: 0) + 1
        }

        assertNull(countsAfterSecondDismiss["com.whatsapp"])
        assertEquals(1, countsAfterSecondDismiss["com.google.android.gm"])
        assertEquals(1, activeMap.size)
    }

    @Test
    fun testDismissAllNotificationsClearsAll() {
        val activeMap = mutableMapOf(
            "k1" to AppNotificationItem("k1", "com.whatsapp", "WhatsApp", "João", "Msg 1", 1000L),
            "k2" to AppNotificationItem("k2", "com.google.android.gm", "Gmail", "Alerta", "Txt", 1500L)
        )

        activeMap.clear()

        assertTrue(activeMap.isEmpty())
        val counts = mutableMapOf<String, Int>()
        activeMap.values.forEach { item ->
            counts[item.packageName] = (counts[item.packageName] ?: 0) + 1
        }
        assertTrue(counts.isEmpty())
    }

    @Test
    fun testNotificationFilterRules() {
        fun shouldIncludeNotification(
            packageName: String,
            isOngoing: Boolean,
            title: String,
            text: String,
            isGroupSummary: Boolean,
            launcherPackageName: String
        ): Boolean {
            if (packageName == launcherPackageName) return false
            if (packageName == "android" && isOngoing) return false
            if (title.isBlank() && text.isBlank()) return false
            if (isGroupSummary && text.isBlank()) return false
            return true
        }

        val launcherPkg = "com.tessera.launcher"

        // 1. Notificação normal válida
        assertTrue(shouldIncludeNotification("com.whatsapp", false, "João", "Oi", false, launcherPkg))

        // 2. Notificação própria da launcher deve ser ignorada
        assertFalse(shouldIncludeNotification(launcherPkg, false, "Tessera", "Atualizado", false, launcherPkg))

        // 3. Sistema Android persistente (ex: USB conectado, Carregando) deve ser ignorado
        assertFalse(shouldIncludeNotification("android", true, "Sistema Android", "Carregando...", false, launcherPkg))

        // 4. Sistema Android com alerta pontual (não persistente) deve ser aceito
        assertTrue(shouldIncludeNotification("android", false, "Alerta de Emergência", "Aviso meteorológico", false, launcherPkg))

        // 5. Notificação sem título nem texto deve ser ignorada
        assertFalse(shouldIncludeNotification("com.some.app", false, "", "", false, launcherPkg))

        // 6. Resumo de grupo sem texto deve ser ignorado
        assertFalse(shouldIncludeNotification("com.whatsapp", false, "3 novas mensagens", "", true, launcherPkg))
    }
}
