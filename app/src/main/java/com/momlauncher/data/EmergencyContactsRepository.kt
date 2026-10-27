package com.momlauncher.data

import android.content.Context
import android.content.SharedPreferences

data class EmergencyContact(
    val slotIndex: Int,
    val name: String,
    val number: String,
    val photoUri: String?
)

class EmergencyContactsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("emergency_contacts", Context.MODE_PRIVATE)

    fun getContacts(): List<EmergencyContact?> {
        val list = mutableListOf<EmergencyContact?>()
        for (i in 0 until 6) {
            val name = prefs.getString("name_$i", null)
            val number = prefs.getString("number_$i", null)
            val photoUri = prefs.getString("photo_$i", null)

            if (!name.isNullOrBlank() && !number.isNullOrBlank()) {
                list.add(EmergencyContact(i, name, number, photoUri))
            } else {
                list.add(null)
            }
        }
        return list
    }

    fun saveContact(slotIndex: Int, name: String, number: String, photoUri: String?) {
        prefs.edit()
            .putString("name_$slotIndex", name)
            .putString("number_$slotIndex", number)
            .putString("photo_$slotIndex", photoUri)
            .apply()
    }

    fun removeContact(slotIndex: Int) {
        prefs.edit()
            .remove("name_$slotIndex")
            .remove("number_$slotIndex")
            .remove("photo_$slotIndex")
            .apply()
    }
}
