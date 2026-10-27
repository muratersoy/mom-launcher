package com.momlauncher.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class NextPrayer(
    val prayerName: String,
    val prayerTime: String,
    val location: String = "Keçiören"
)

class PrayerTimesRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("prayer_times_cache", Context.MODE_PRIVATE)

    suspend fun getNextPrayer(): NextPrayer? = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 1. Önce bugünün önbelleğine bak
        val cached = getCachedTimes(todayStr)
        val timingsMap = cached ?: fetchWithFallback(todayStr)

        if (timingsMap == null || timingsMap.isEmpty()) {
            return@withContext null
        }

        // En yakın namaz vaktini hesapla
        calculateNextPrayer(timingsMap)
    }

    private fun fetchWithFallback(todayStr: String): Map<String, String>? {
        // Kaynak 1: Aladhan API (Ankara Keçiören Koordinatları, Diyanet Yöntemi)
        try {
            val result1 = fetchFromAladhan()
            if (result1 != null && result1.isNotEmpty()) {
                saveCache(todayStr, result1)
                return result1
            }
        } catch (e: Exception) {
            Log.w(TAG, "Kaynak 1 (Aladhan) başarısız oldu, Kaynak 2'ye geçiliyor", e)
        }

        // Kaynak 2: MuslimSalat API (Ankara)
        try {
            val result2 = fetchFromMuslimSalat()
            if (result2 != null && result2.isNotEmpty()) {
                saveCache(todayStr, result2)
                return result2
            }
        } catch (e: Exception) {
            Log.w(TAG, "Kaynak 2 (MuslimSalat) da başarısız oldu", e)
        }

        return null
    }

    private fun fetchFromAladhan(): Map<String, String>? {
        val urlStr = "https://api.aladhan.com/v1/timings?latitude=39.9880&longitude=32.8610&method=13"
        val response = httpGet(urlStr) ?: return null

        val json = JSONObject(response)
        if (json.optInt("code") != 200 && json.optString("status") != "OK") return null

        val timings = json.getJSONObject("data").getJSONObject("timings")
        val map = mutableMapOf<String, String>()

        val rawImsak = cleanTime(timings.getString("Fajr"))
        val rawGunes = cleanTime(timings.getString("Sunrise"))
        val sabahTime = calculateHanafiSabahTime(rawImsak, rawGunes)

        map["Sabah"] = sabahTime
        map["Güneş"] = rawGunes
        map["Öğle"] = cleanTime(timings.getString("Dhuhr"))
        map["İkindi"] = cleanTime(timings.getString("Asr"))
        map["Akşam"] = cleanTime(timings.getString("Maghrib"))
        map["Yatsı"] = cleanTime(timings.getString("Isha"))

        return map
    }

    private fun fetchFromMuslimSalat(): Map<String, String>? {
        val urlStr = "https://muslimsalat.com/ankara.json"
        val response = httpGet(urlStr) ?: return null

        val json = JSONObject(response)
        val items = json.optJSONArray("items") ?: return null
        if (items.length() == 0) return null

        val item = items.getJSONObject(0)
        val map = mutableMapOf<String, String>()

        val rawImsak = convert12To24(item.optString("fajr"))
        val rawGunes = convert12To24(item.optString("shurooq"))
        val sabahTime = calculateHanafiSabahTime(rawImsak, rawGunes)

        map["Sabah"] = sabahTime
        map["Güneş"] = rawGunes
        map["Öğle"] = convert12To24(item.optString("dhuhr"))
        map["İkindi"] = convert12To24(item.optString("asr"))
        map["Akşam"] = convert12To24(item.optString("maghrib"))
        map["Yatsı"] = convert12To24(item.optString("isha"))

        return map
    }

    /**
     * Hanefi mezhebine göre sabah namazını kılmanın müstehap / efdal vakti "İsfâr"dır (ortalığın aydınlanması).
     * Hadis: "Sabah namazını aydınlıkta kılınız; zira ecri daha büyüktür." (Tirmizî, Salât 115).
     * Türkiye'de Diyanet İşleri Başkanlığı ve asırlık Hanefi cami uygulamasında
     * sabah ezanı ve namaz vakti Güneş doğuşundan 45 dakika öncedir.
     */
    private fun calculateHanafiSabahTime(imsakStr: String, gunesStr: String): String {
        val imsakMinutes = parseTimeToMinutes(imsakStr)
        val gunesMinutes = parseTimeToMinutes(gunesStr)
        val isfarMinutes = gunesMinutes - 45
        val sabahMinutes = if (isfarMinutes > imsakMinutes) isfarMinutes else imsakMinutes
        return formatMinutesToTime(sabahMinutes)
    }

    private fun formatMinutesToTime(minutes: Int): String {
        val totalMinutes = if (minutes < 0) minutes + 24 * 60 else minutes
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    private fun calculateNextPrayer(timings: Map<String, String>): NextPrayer? {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val prayerOrder = listOf("Sabah", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")

        for (name in prayerOrder) {
            val timeStr = timings[name] ?: continue
            val prayerMinutes = parseTimeToMinutes(timeStr)
            if (prayerMinutes > currentMinutes) {
                return NextPrayer(prayerName = name, prayerTime = timeStr, location = "Keçiören")
            }
        }

        // Yatsı vakti de geçmişse sıradaki vakit yarının Sabah namazı vaktidir
        val sabahTime = timings["Sabah"] ?: "06:15"
        return NextPrayer(prayerName = "Sabah", prayerTime = sabahTime, location = "Keçiören")
    }

    private fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        if (parts.size >= 2) {
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }
        return 0
    }

    private fun cleanTime(raw: String): String {
        val trimmed = raw.trim()
        val colonIdx = trimmed.indexOf(":")
        if (colonIdx != -1 && trimmed.length >= colonIdx + 3) {
            val hour = trimmed.substring(0, colonIdx).padStart(2, '0')
            val min = trimmed.substring(colonIdx + 1, colonIdx + 3)
            return "$hour:$min"
        }
        return trimmed.take(5)
    }

    private fun convert12To24(raw: String): String {
        return try {
            val inputFormat = SimpleDateFormat("h:mm a", Locale.US)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(raw.trim())
            if (date != null) outputFormat.format(date) else cleanTime(raw)
        } catch (e: Exception) {
            cleanTime(raw)
        }
    }

    private fun saveCache(dateKey: String, map: Map<String, String>) {
        val editor = prefs.edit()
        editor.putString("cached_date", dateKey)
        for ((k, v) in map) {
            editor.putString("prayer_$k", v)
        }
        editor.apply()
    }

    private fun getCachedTimes(dateKey: String): Map<String, String>? {
        val cachedDate = prefs.getString("cached_date", null)
        if (cachedDate != dateKey) return null

        val prayerOrder = listOf("Sabah", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")
        val map = mutableMapOf<String, String>()
        for (name in prayerOrder) {
            val v = prefs.getString("prayer_$name", null)
            if (v != null) {
                map[name] = v
            } else {
                return null
            }
        }
        return map
    }

    private fun httpGet(urlStr: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            var url = URL(urlStr)
            var redirects = 0
            while (redirects < 4) {
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 4000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0 MomLauncher/1.0")
                }
                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP) {
                    val newUrl = connection.getHeaderField("Location")
                    url = if (newUrl.startsWith("http")) URL(newUrl) else URL(url, newUrl)
                    redirects++
                } else if (code == HttpURLConnection.HTTP_OK) {
                    break
                } else {
                    return null
                }
            }

            val reader = BufferedReader(InputStreamReader(connection?.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            sb.toString()
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    companion object {
        private const val TAG = "PrayerTimesRepo"
    }
}
