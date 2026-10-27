package com.momlauncher.data

import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class ContactItem(
    val id: String,
    val name: String,
    val number: String,
    val photoUri: String?,
    val initial: String
)

class ContactsRepository(private val context: Context) {

    suspend fun getContacts(searchQuery: String = ""): List<ContactItem> = withContext(Dispatchers.IO) {
        val contactsList = mutableListOf<ContactItem>()
        val seenKeys = mutableSetOf<String>()

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        val selection = if (searchQuery.isNotBlank()) {
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        } else {
            null
        }

        val selectionArgs = if (searchQuery.isNotBlank()) {
            val q = "%$searchQuery%"
            arrayOf(q, q)
        } else {
            null
        }

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE LOCALIZED ASC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                while (it.moveToNext()) {
                    val id = if (idIndex != -1) it.getString(idIndex) ?: "" else ""
                    val name = if (nameIndex != -1) it.getString(nameIndex) ?: "" else ""
                    val rawNumber = if (numberIndex != -1) it.getString(numberIndex) ?: "" else ""
                    val photoUri = if (photoIndex != -1) it.getString(photoIndex) else null

                    val cleanNumber = rawNumber.replace("\\s+".toRegex(), "")
                    val dedupeKey = "$name:$cleanNumber"
                    if (!seenKeys.contains(dedupeKey) && name.isNotBlank() && cleanNumber.isNotBlank()) {
                        seenKeys.add(dedupeKey)

                        val trimmed = name.trim()
                        val firstChar = if (trimmed.isNotEmpty()) {
                            trimmed.substring(0, 1).uppercase(Locale("tr", "TR"))
                        } else {
                            "#"
                        }

                        val initial = if (firstChar.matches("[A-ZÇĞİÖŞÜ]".toRegex())) firstChar else "#"

                        contactsList.add(
                            ContactItem(
                                id = id,
                                name = name,
                                number = rawNumber,
                                photoUri = photoUri,
                                initial = initial
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        contactsList
    }

    suspend fun addContact(name: String, phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        val cleanNumber = phoneNumber.trim()
        if (cleanName.isBlank() || cleanNumber.isBlank()) return@withContext false

        try {
            val ops = ArrayList<ContentProviderOperation>()
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, cleanName)
                    .build()
            )
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, cleanNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
