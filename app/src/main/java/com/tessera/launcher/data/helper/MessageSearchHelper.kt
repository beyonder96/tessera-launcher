package com.tessera.launcher.data.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.ContextCompat

data class MessageSearchResult(
    val id: Long,
    val address: String,
    val body: String,
    val date: Long
)

class MessageSearchHelper(private val context: Context) {

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun searchMessages(query: String, limit: Int = 10): List<MessageSearchResult> {
        if (!hasSmsPermission() || query.isBlank() || query.length < 2) return emptyList()

        val results = mutableListOf<MessageSearchResult>()
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val selection = "${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.ADDRESS} LIKE ?"
        val selectionArgs = arrayOf("%$query%", "%$query%")
        val sortOrder = "${Telephony.Sms.DATE} DESC"

        runCatching {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndex(Telephony.Sms._ID)
                val addrCol = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyCol = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateCol = cursor.getColumnIndex(Telephony.Sms.DATE)

                while (cursor.moveToNext() && results.size < limit) {
                    val id = if (idCol >= 0) cursor.getLong(idCol) else 0L
                    val address = if (addrCol >= 0) cursor.getString(addrCol) ?: "" else ""
                    val body = if (bodyCol >= 0) cursor.getString(bodyCol) ?: "" else ""
                    val date = if (dateCol >= 0) cursor.getLong(dateCol) else 0L

                    results.add(
                        MessageSearchResult(
                            id = id,
                            address = address,
                            body = body,
                            date = date
                        )
                    )
                }
            }
        }
        return results
    }

    fun openSmsConversation(address: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:$address")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openNewSms(text: String = "") {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:")
            if (text.isNotBlank()) {
                putExtra("sms_body", text)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
