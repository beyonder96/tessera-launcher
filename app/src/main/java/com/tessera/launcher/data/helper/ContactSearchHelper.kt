package com.tessera.launcher.data.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class ContactInfo(
    val name: String,
    val phoneNumber: String
)

class ContactSearchHelper(private val context: Context) {

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun normalize(str: String): String {
        return java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    fun searchContacts(query: String, limit: Int = 50): List<ContactInfo> {
        if (!hasContactsPermission() || query.isBlank()) return emptyList()

        val list = mutableListOf<ContactInfo>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        val normalizedQuery = normalize(query)
        val digitQuery = query.filter { it.isDigit() }

        runCatching {
            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext() && list.size < limit) {
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) ?: "Contato" else "Contato"
                    val number = if (numberIndex >= 0) cursor.getString(numberIndex) ?: "" else ""

                    if (number.isNotBlank()) {
                        val normName = normalize(name)
                        val normNumber = number.filter { it.isDigit() }
                        val matchesName = normName.contains(normalizedQuery)
                        val matchesNumber = digitQuery.isNotEmpty() && normNumber.contains(digitQuery)

                        if ((matchesName || matchesNumber) && !list.any { it.name == name && it.phoneNumber == number }) {
                            list.add(ContactInfo(name = name, phoneNumber = number))
                        }
                    }
                }
            }
        }
        return list
    }

    fun callContact(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
