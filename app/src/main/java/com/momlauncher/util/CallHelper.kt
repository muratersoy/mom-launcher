package com.momlauncher.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat

object CallHelper {

    fun makeCall(context: Context, phoneNumber: String) {
        val cleanNumber = phoneNumber.trim().replace(" ", "")
        if (cleanNumber.isBlank()) return

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(cleanNumber)}"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(cleanNumber)}"))
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Arama başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
