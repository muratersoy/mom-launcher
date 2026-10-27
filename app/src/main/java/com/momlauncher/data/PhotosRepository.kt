package com.momlauncher.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PhotoItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateTaken: Long
)

class PhotosRepository(private val context: Context) {

    suspend fun getPhotos(limit: Int = 150): List<PhotoItem> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<PhotoItem>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        try {
            val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val queryArgs = Bundle().apply {
                    putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                    putStringArray(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(MediaStore.Images.Media.DATE_ADDED)
                    )
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                }
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    queryArgs,
                    null
                )
            } else {
                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )
            }

            cursor?.use {
                val idColumn = it.getColumnIndex(MediaStore.Images.Media._ID)
                val nameColumn = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = it.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                var count = 0
                while (it.moveToNext() && count < limit) {
                    val id = if (idColumn != -1) it.getLong(idColumn) else -1L
                    if (id != -1L) {
                        val name = if (nameColumn != -1) it.getString(nameColumn) ?: "" else ""
                        val date = if (dateColumn != -1) it.getLong(dateColumn) else 0L
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        photos.add(PhotoItem(id, contentUri, name, date))
                        count++
                    }
                }
            }
            Log.d("PhotosRepository", "Loaded ${photos.size} photos successfully")
        } catch (e: Exception) {
            Log.e("PhotosRepository", "Error querying photos", e)
        }

        photos
    }
}
