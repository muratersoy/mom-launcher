package com.momlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

data class AppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val iconBitmap: Bitmap? = null
)

class AppsRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val myPackageName = context.packageName
        val appList = mutableListOf<AppItem>()
        val seenPackages = mutableSetOf<String>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo?.packageName ?: continue
            // Kendi launcher uygulamamızı hariç tut
            if (pkg == myPackageName) continue
            // Aynı paketi birden fazla launcher activity'si olsa bile tekil tut
            if (!seenPackages.add(pkg)) continue

            val label = info.loadLabel(pm)?.toString()?.trim() ?: pkg
            val drawable = try {
                info.loadIcon(pm) ?: pm.getApplicationIcon(pkg)
            } catch (e: Exception) {
                null
            }

            val bitmap = try {
                drawable?.let { d ->
                    val targetSize = 128
                    d.toBitmap(width = targetSize, height = targetSize)
                }
            } catch (e: Exception) {
                null
            }

            appList.add(AppItem(id = pkg, name = label, packageName = pkg, iconBitmap = bitmap))
        }

        val collator = Collator.getInstance(Locale("tr", "TR")).apply {
            strength = Collator.PRIMARY
        }
        appList.sortedWith { a, b -> collator.compare(a.name, b.name) }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
