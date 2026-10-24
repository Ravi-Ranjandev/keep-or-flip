package com.example.data

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.model.PhotoCategory
import com.example.model.PhotoItem
import com.example.model.PhotoStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class DeleteRequestResult {
  data class RequiresUserConsent(
    val intentSender: IntentSender,
    val uris: List<Uri>,
    val photoIds: List<String>
  ) : DeleteRequestResult()

  data class Completed(
    val successCount: Int,
    val photoIds: List<String>
  ) : DeleteRequestResult()

  data class Error(val message: String) : DeleteRequestResult()
}

class PhotoRepository {

  companion object {
    private const val PREFS_NAME = "keep_flip_prefs"
    private const val KEY_KEPT_IDS = "kept_photo_ids"
    private const val KEY_FLIPPED_IDS = "flipped_photo_ids"
    private const val KEY_DELETED_IDS = "deleted_photo_ids"
    private const val KEY_TOTAL_CLEANED_BYTES = "total_cleaned_bytes"
  }

  private val _photos = MutableStateFlow<List<PhotoItem>>(emptyList())
  val photos: StateFlow<List<PhotoItem>> = _photos.asStateFlow()

  private val _totalCleanedBytes = MutableStateFlow(0L)
  val totalCleanedBytes: StateFlow<Long> = _totalCleanedBytes.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Track kept, flipped, and permanently deleted IDs (persisted across app restarts)
  private val sessionKeptIds = mutableSetOf<String>()
  private val sessionFlippedIds = mutableSetOf<String>()
  private val sessionDeletedIds = mutableSetOf<String>()
  private var isStateInitialized = false

  private fun initPrefsIfNeeded(context: Context) {
    if (isStateInitialized) return
    try {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      sessionKeptIds.clear()
      sessionKeptIds.addAll(prefs.getStringSet(KEY_KEPT_IDS, emptySet()) ?: emptySet())

      sessionFlippedIds.clear()
      sessionFlippedIds.addAll(prefs.getStringSet(KEY_FLIPPED_IDS, emptySet()) ?: emptySet())

      sessionDeletedIds.clear()
      sessionDeletedIds.addAll(prefs.getStringSet(KEY_DELETED_IDS, emptySet()) ?: emptySet())

      _totalCleanedBytes.value = prefs.getLong(KEY_TOTAL_CLEANED_BYTES, 0L)
      isStateInitialized = true
    } catch (e: Exception) {
      Log.e("PhotoRepository", "Failed to load preferences", e)
    }
  }

  private fun persistState(context: Context?) {
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit()
        .putStringSet(KEY_KEPT_IDS, HashSet(sessionKeptIds))
        .putStringSet(KEY_FLIPPED_IDS, HashSet(sessionFlippedIds))
        .putStringSet(KEY_DELETED_IDS, HashSet(sessionDeletedIds))
        .putLong(KEY_TOTAL_CLEANED_BYTES, _totalCleanedBytes.value)
        .apply()
    } catch (e: Exception) {
      Log.e("PhotoRepository", "Failed to persist state", e)
    }
  }

  suspend fun loadDevicePhotos(context: Context): List<PhotoItem> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    initPrefsIfNeeded(context)
    val resultList = mutableListOf<PhotoItem>()
    val seenIds = HashSet<Long>()

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
      MediaStore.Images.Media._ID,
      MediaStore.Images.Media.DISPLAY_NAME,
      MediaStore.Images.Media.SIZE,
      MediaStore.Images.Media.DATE_ADDED,
      MediaStore.Images.Media.WIDTH,
      MediaStore.Images.Media.HEIGHT
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    try {
      context.contentResolver.query(
        collection,
        projection,
        null,
        null,
        sortOrder
      )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val widthColumn = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
        val heightColumn = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idColumn)
          if (!seenIds.add(id)) continue
          val idStr = id.toString()
          if (idStr in sessionDeletedIds) continue

          val name = cursor.getString(nameColumn) ?: "Photo_$id.jpg"
          val size = cursor.getLong(sizeColumn)
          if (size <= 0) continue

          val dateAddedSec = cursor.getLong(dateColumn)
          val dateStr = if (dateAddedSec > 0) {
            dateFormat.format(Date(dateAddedSec * 1000L))
          } else {
            "Recent"
          }

          val width = if (widthColumn != -1) cursor.getInt(widthColumn) else 0
          val height = if (heightColumn != -1) cursor.getInt(heightColumn) else 0
          val resStr = if (width > 0 && height > 0) "$width x $height" else "Full Resolution"

          val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

          val category = when {
            name.lowercase().contains("screenshot") -> PhotoCategory.SCREENSHOT
            name.lowercase().contains("wa") || name.lowercase().contains("whatsapp") -> PhotoCategory.DOWNLOAD
            name.lowercase().contains("portrait") -> PhotoCategory.PORTRAIT
            name.lowercase().contains("wallpaper") -> PhotoCategory.WALLPAPER
            else -> PhotoCategory.CAMERA
          }

          val currentStatus = when {
            idStr in sessionFlippedIds -> PhotoStatus.FLIPPED
            idStr in sessionKeptIds -> PhotoStatus.KEPT
            else -> PhotoStatus.UNREVIEWED
          }

          resultList.add(
            PhotoItem(
              id = idStr,
              title = name,
              drawableRes = null,
              uriString = contentUri.toString(),
              fileSizeBytes = size,
              dateString = dateStr,
              dateAddedEpochSeconds = dateAddedSec,
              status = currentStatus,
              isFavorite = false,
              category = category,
              resolution = resStr
            )
          )
        }
      }

      // Revalidate persisted flipped/kept photos against files actually found in MediaStore
      val foundIdStrings = seenIds.map { it.toString() }.toSet()
      sessionFlippedIds.retainAll(foundIdStrings)
      sessionKeptIds.retainAll(foundIdStrings)
      persistState(context)

    } catch (e: Exception) {
      Log.e("PhotoRepository", "Failed to query real device gallery photos", e)
    } finally {
      _isLoading.value = false
    }

    _photos.value = resultList
    resultList
  }

  fun keepPhoto(photoId: String, context: Context? = null) {
    sessionFlippedIds.remove(photoId)
    sessionKeptIds.add(photoId)
    persistState(context)
    _photos.update { list ->
      list.map { if (it.id == photoId) it.copy(status = PhotoStatus.KEPT) else it }
    }
  }

  fun unkeepPhoto(photoId: String, context: Context? = null) {
    sessionKeptIds.remove(photoId)
    persistState(context)
    _photos.update { list ->
      list.map { if (it.id == photoId) it.copy(status = PhotoStatus.UNREVIEWED) else it }
    }
  }

  fun flipPhoto(photoId: String, context: Context? = null) {
    sessionKeptIds.remove(photoId)
    sessionFlippedIds.add(photoId)
    persistState(context)
    _photos.update { list ->
      list.map { if (it.id == photoId) it.copy(status = PhotoStatus.FLIPPED) else it }
    }
  }

  fun unflipPhoto(photoId: String, context: Context? = null) {
    sessionFlippedIds.remove(photoId)
    persistState(context)
    _photos.update { list ->
      list.map { if (it.id == photoId) it.copy(status = PhotoStatus.UNREVIEWED) else it }
    }
  }

  /**
   * Keep Selected:
   * Removes selected photos from pending-deletion queue while preserving original files on device.
   * Marks them as KEPT.
   */
  fun keepSelectedPhotos(photoIds: Set<String>, context: Context? = null) {
    sessionFlippedIds.removeAll(photoIds)
    sessionKeptIds.addAll(photoIds)
    persistState(context)
    _photos.update { list ->
      list.map { if (it.id in photoIds) it.copy(status = PhotoStatus.KEPT) else it }
    }
  }

  fun restoreAllFlipped(context: Context? = null) {
    sessionFlippedIds.clear()
    persistState(context)
    _photos.update { list ->
      list.map { if (it.status == PhotoStatus.FLIPPED) it.copy(status = PhotoStatus.UNREVIEWED) else it }
    }
  }

  fun toggleFavorite(photoId: String) {
    _photos.update { list ->
      list.map { if (it.id == photoId) it.copy(isFavorite = !it.isFavorite) else it }
    }
  }

  /**
   * Batch deletion request:
   * Collects selected photo content URIs and creates ONE request via MediaStore.createDeleteRequest(contentResolver, uris).
   */
  fun prepareDeleteRequest(context: Context, photosToDelete: List<PhotoItem>): DeleteRequestResult {
    val uris = photosToDelete.mapNotNull { it.contentUri }
    val ids = photosToDelete.map { it.id }
    if (uris.isEmpty()) {
      return DeleteRequestResult.Error("No valid photos to delete")
    }

    val contentResolver = context.contentResolver

    return when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
        // Modern Android 11+ (API 30+) official MediaStore batch deletion request
        try {
          val pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris)
          DeleteRequestResult.RequiresUserConsent(
            intentSender = pendingIntent.intentSender,
            uris = uris,
            photoIds = ids
          )
        } catch (e: Exception) {
          Log.e("PhotoRepository", "createDeleteRequest error", e)
          DeleteRequestResult.Error(e.message ?: "Failed to create batch delete request")
        }
      }
      Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
        // Android 10 (API 29) RecoverableSecurityException handling
        try {
          var count = 0
          for (uri in uris) {
            count += contentResolver.delete(uri, null, null)
          }
          DeleteRequestResult.Completed(count, ids)
        } catch (e: RecoverableSecurityException) {
          DeleteRequestResult.RequiresUserConsent(
            intentSender = e.userAction.actionIntent.intentSender,
            uris = uris,
            photoIds = ids
          )
        } catch (e: Exception) {
          DeleteRequestResult.Error(e.message ?: "Failed to delete")
        }
      }
      else -> {
        // Android 9 and below
        try {
          var count = 0
          for (uri in uris) {
            count += contentResolver.delete(uri, null, null)
          }
          DeleteRequestResult.Completed(count, ids)
        } catch (e: Exception) {
          DeleteRequestResult.Error(e.message ?: "Failed to delete")
        }
      }
    }
  }

  fun onPhotosPermanentlyDeleted(deletedIds: List<String>, context: Context? = null) {
    sessionDeletedIds.addAll(deletedIds)
    sessionFlippedIds.removeAll(deletedIds.toSet())
    sessionKeptIds.removeAll(deletedIds.toSet())
    var freed = 0L

    _photos.update { currentList ->
      val remaining = mutableListOf<PhotoItem>()
      for (item in currentList) {
        if (item.id in deletedIds) {
          freed += item.fileSizeBytes
        } else {
          remaining.add(item)
        }
      }
      remaining
    }

    _totalCleanedBytes.update { it + freed }
    persistState(context)
  }

  fun importCustomPhotos(uris: List<String>) {
    val newPhotos = uris.mapIndexed { idx, uriStr ->
      PhotoItem(
        id = UUID.randomUUID().toString(),
        title = "Imported_Photo_${System.currentTimeMillis() % 10000}_$idx.jpg",
        drawableRes = null,
        uriString = uriStr,
        fileSizeBytes = 3_500_000L,
        dateString = "Imported Today",
        status = PhotoStatus.UNREVIEWED,
        isFavorite = false,
        category = PhotoCategory.CAMERA,
        resolution = "3024 x 4032"
      )
    }
    _photos.update { newPhotos + it }
  }

  suspend fun createTestDevicePhotos(context: Context, count: Int = 3): Boolean = withContext(Dispatchers.IO) {
    var createdCount = 0
    val colors = listOf(
      Triple("Forest_Green_Snap.jpg", Color.rgb(34, 197, 94), "KEEP TEST PHOTO"),
      Triple("Coral_Sunset_Shot.jpg", Color.rgb(239, 68, 68), "FLIP TEST PHOTO"),
      Triple("Cobalt_Sky_View.jpg", Color.rgb(59, 130, 246), "GALLERY PHOTO")
    )

    for ((fileName, color, label) in colors.take(count)) {
      try {
        val values = ContentValues().apply {
          put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
          put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
          }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
          context.contentResolver.openOutputStream(uri)?.use { out ->
            val bitmap = Bitmap.createBitmap(1080, 1440, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(color)

            val paint = Paint().apply {
              setColor(Color.WHITE)
              textSize = 54f
              isAntiAlias = true
              textAlign = Paint.Align.CENTER
            }
            canvas.drawText(label, 540f, 720f, paint)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
          }

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
          }
          createdCount++
        }
      } catch (e: Exception) {
        Log.e("PhotoRepository", "Failed to create test photo", e)
      }
    }

    if (createdCount > 0) {
      loadDevicePhotos(context)
      true
    } else {
      false
    }
  }

  fun resetSession(context: Context? = null) {
    sessionKeptIds.clear()
    sessionFlippedIds.clear()
    sessionDeletedIds.clear()
    persistState(context)
    _photos.update { list ->
      list.map { it.copy(status = PhotoStatus.UNREVIEWED) }
    }
  }
}
