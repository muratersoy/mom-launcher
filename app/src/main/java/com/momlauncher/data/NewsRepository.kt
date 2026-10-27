package com.momlauncher.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.Html
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

data class NewsItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val source: String = "TRT Haber",
    val pubDate: String? = null,
    val link: String? = null
)

class NewsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("news_cache_pref", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "NewsRepository"
        private const val KEY_CACHED_JSON = "cached_news_json"
        private const val KEY_LAST_FETCH = "last_fetch_time"
        private const val CACHE_VALIDITY_MS = 15 * 60 * 1000L // 15 dakika

        private const val URL_TRT_MANSET = "https://www.trthaber.com/manset_articles.rss"
        private const val URL_TRT_SONDAKIKA = "https://www.trthaber.com/sondakika.rss"
        private const val URL_NTV_FALLBACK = "https://www.ntv.com.tr/son-dakika.rss"
    }

    suspend fun getNews(forceRefresh: Boolean = false): List<NewsItem> = withContext(Dispatchers.IO) {
        val cached = getCachedNews()
        val lastFetch = prefs.getLong(KEY_LAST_FETCH, 0L)
        val now = System.currentTimeMillis()

        // Önbellek tazeyse ve zorla yenileme istenmediyse hemen döndür
        if (!forceRefresh && cached.isNotEmpty() && (now - lastFetch < CACHE_VALIDITY_MS)) {
            return@withContext cached
        }

        // İnternetten çekmeyi dene
        val freshNews = fetchOnlineNews()
        if (freshNews.isNotEmpty()) {
            saveCache(freshNews)
            return@withContext freshNews
        }

        // İnternet başarısızsa önbelleği döndür
        cached
    }

    private fun fetchOnlineNews(): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        val seenTitles = mutableSetOf<String>()

        // 1. TRT Manşetler (Büyük kaliteli görseller ve ana haberler)
        try {
            val mansetXml = downloadUrl(URL_TRT_MANSET)
            if (!mansetXml.isNullOrBlank()) {
                val parsed = parseRss(mansetXml, "TRT Haber")
                for (item in parsed) {
                    if (seenTitles.add(item.title.trim())) {
                        list.add(item)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "TRT Manşet yüklenirken hata", e)
        }

        // 2. TRT Son Dakika (Güncel gelişmeler)
        try {
            val sonDakikaXml = downloadUrl(URL_TRT_SONDAKIKA)
            if (!sonDakikaXml.isNullOrBlank()) {
                val parsed = parseRss(sonDakikaXml, "TRT Son Dakika")
                for (item in parsed) {
                    if (seenTitles.add(item.title.trim())) {
                        list.add(item)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "TRT Son Dakika yüklenirken hata", e)
        }

        // 3. Fallback: TRT başarısız olduysa NTV'den dene
        if (list.isEmpty()) {
            try {
                val ntvXml = downloadUrl(URL_NTV_FALLBACK)
                if (!ntvXml.isNullOrBlank()) {
                    val parsed = parseAtomOrRss(ntvXml, "NTV")
                    for (item in parsed) {
                        if (seenTitles.add(item.title.trim())) {
                            list.add(item)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "NTV Fallback yüklenirken hata", e)
            }
        }

        return list
    }

    private fun parseRss(xml: String, sourceName: String): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var inItem = false

            var title = ""
            var description = ""
            var imageUrl: String? = null
            var link: String? = null
            var pubDate: String? = null
            var guid: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            inItem = true
                            title = ""
                            description = ""
                            imageUrl = null
                            link = null
                            pubDate = null
                            guid = null
                        } else if (inItem) {
                            when {
                                tagName.equals("title", ignoreCase = true) -> {
                                    title = parser.nextText()
                                }
                                tagName.equals("description", ignoreCase = true) -> {
                                    description = parser.nextText()
                                }
                                tagName.equals("link", ignoreCase = true) -> {
                                    link = parser.nextText()
                                }
                                tagName.equals("guid", ignoreCase = true) -> {
                                    guid = parser.nextText()
                                }
                                tagName.equals("pubDate", ignoreCase = true) -> {
                                    pubDate = parser.nextText()
                                }
                                tagName.equals("imageUrl", ignoreCase = true) -> {
                                    imageUrl = parser.nextText().trim()
                                }
                                tagName.equals("enclosure", ignoreCase = true) -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (!url.isNullOrBlank() && imageUrl == null) {
                                        imageUrl = url.trim()
                                    }
                                }
                                tagName.equals("content", ignoreCase = true) ||
                                tagName.equals("media:content", ignoreCase = true) -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (!url.isNullOrBlank() && imageUrl == null) {
                                        imageUrl = url.trim()
                                    }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true) && inItem) {
                            inItem = false

                            // Eğer resim enclosure'dan gelmediyse description içerisindeki img src'yi bul
                            if (imageUrl.isNullOrBlank() && description.contains("<img", ignoreCase = true)) {
                                imageUrl = extractImageFromHtml(description)
                            }

                            val cleanTitle = cleanHtml(title)
                            val cleanDesc = cleanHtml(description)

                            if (cleanTitle.isNotBlank() && (cleanDesc.isNotBlank() || !imageUrl.isNullOrBlank())) {
                                val formattedDate = formatTurkishDate(pubDate)
                                items.add(
                                    NewsItem(
                                        id = guid ?: link ?: cleanTitle.hashCode().toString(),
                                        title = cleanTitle,
                                        description = cleanDesc,
                                        imageUrl = imageUrl,
                                        source = sourceName,
                                        pubDate = formattedDate,
                                        link = link
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "XML parse hatası", e)
        }
        return items
    }

    private fun parseAtomOrRss(xml: String, sourceName: String): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var inEntry = false

            var title = ""
            var description = ""
            var imageUrl: String? = null
            var link: String? = null
            var pubDate: String? = null
            var id: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("entry", ignoreCase = true) || tagName.equals("item", ignoreCase = true)) {
                            inEntry = true
                            title = ""
                            description = ""
                            imageUrl = null
                            link = null
                            pubDate = null
                            id = null
                        } else if (inEntry) {
                            when {
                                tagName.equals("title", ignoreCase = true) -> {
                                    title = parser.nextText()
                                }
                                tagName.equals("summary", ignoreCase = true) ||
                                tagName.equals("description", ignoreCase = true) ||
                                tagName.equals("content", ignoreCase = true) -> {
                                    if (description.isBlank()) {
                                        description = parser.nextText()
                                    }
                                }
                                tagName.equals("id", ignoreCase = true) -> {
                                    id = parser.nextText()
                                }
                                tagName.equals("published", ignoreCase = true) ||
                                tagName.equals("pubDate", ignoreCase = true) -> {
                                    pubDate = parser.nextText()
                                }
                                tagName.equals("enclosure", ignoreCase = true) -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (!url.isNullOrBlank() && imageUrl == null) {
                                        imageUrl = url.trim()
                                    }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if ((tagName.equals("entry", ignoreCase = true) || tagName.equals("item", ignoreCase = true)) && inEntry) {
                            inEntry = false

                            if (imageUrl.isNullOrBlank() && description.contains("<img", ignoreCase = true)) {
                                imageUrl = extractImageFromHtml(description)
                            }

                            val cleanTitle = cleanHtml(title)
                            val cleanDesc = cleanHtml(description)

                            if (cleanTitle.isNotBlank()) {
                                items.add(
                                    NewsItem(
                                        id = id ?: cleanTitle.hashCode().toString(),
                                        title = cleanTitle,
                                        description = cleanDesc,
                                        imageUrl = imageUrl,
                                        source = sourceName,
                                        pubDate = formatTurkishDate(pubDate),
                                        link = link
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Atom parse hatası", e)
        }
        return items
    }

    private fun extractImageFromHtml(html: String): String? {
        val regex = Regex("""src=["'](https?://[^"']+)["']""", RegexOption.IGNORE_CASE)
        val match = regex.find(html)
        return match?.groupValues?.getOrNull(1)
    }

    private fun cleanHtml(raw: String): String {
        if (raw.isBlank()) return ""
        var s = raw.replace("<![CDATA[", "").replace("]]>", "")
        s = s.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        s = s.replace(Regex("<img[^>]*>", RegexOption.IGNORE_CASE), "")

        val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(s).toString()
        }

        return decoded.trim().replace(Regex("\\s+"), " ")
    }

    private fun formatTurkishDate(dateStr: String?): String? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            // EEE, dd MMM yyyy HH:mm:ss Z (RSS standardı)
            val inFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val date = inFormat.parse(dateStr.trim()) ?: return dateStr
            val outFormat = SimpleDateFormat("dd MMMM HH:mm", Locale("tr", "TR"))
            outFormat.format(date)
        } catch (e: Exception) {
            try {
                // ISO 8601 (2026-10-27T22:30:00+03:00)
                val inFormatIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val date = inFormatIso.parse(dateStr.trim()) ?: return dateStr
                val outFormat = SimpleDateFormat("dd MMMM HH:mm", Locale("tr", "TR"))
                outFormat.format(date)
            } catch (e2: Exception) {
                dateStr
            }
        }
    }

    private fun downloadUrl(urlStr: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) MomLauncher/1.0")
                setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val sb = java.lang.StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                reader.close()
                sb.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "İndirme başarısız: $urlStr", e)
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun getCachedNews(): List<NewsItem> {
        val json = prefs.getString(KEY_CACHED_JSON, null) ?: return emptyList()
        val list = mutableListOf<NewsItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NewsItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        imageUrl = if (obj.has("imageUrl") && !obj.isNull("imageUrl")) obj.getString("imageUrl") else null,
                        source = obj.optString("source", "TRT Haber"),
                        pubDate = if (obj.has("pubDate") && !obj.isNull("pubDate")) obj.getString("pubDate") else null,
                        link = if (obj.has("link") && !obj.isNull("link")) obj.getString("link") else null
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Önbellek okuma hatası", e)
        }
        return list
    }

    private fun saveCache(news: List<NewsItem>) {
        try {
            val array = JSONArray()
            for (item in news) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("description", item.description)
                    put("imageUrl", item.imageUrl)
                    put("source", item.source)
                    put("pubDate", item.pubDate)
                    put("link", item.link)
                }
                array.put(obj)
            }
            prefs.edit()
                .putString(KEY_CACHED_JSON, array.toString())
                .putLong(KEY_LAST_FETCH, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Önbellek kaydetme hatası", e)
        }
    }
}
