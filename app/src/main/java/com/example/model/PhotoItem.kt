package com.example.model

import android.net.Uri
import androidx.annotation.DrawableRes
import java.util.Locale

enum class PhotoStatus {
  UNREVIEWED,
  KEPT,
  FLIPPED
}

enum class PhotoCategory(val label: String) {
  PORTRAIT("Portrait"),
  SCREENSHOT("Screenshot"),
  MEMORY("Memory"),
  DOWNLOAD("Download"),
  WALLPAPER("Wallpaper"),
  CAMERA("Camera")
}

data class PhotoItem(
  val id: String,
  val title: String,
  @DrawableRes val drawableRes: Int? = null,
  val uriString: String? = null,
  val fileSizeBytes: Long,
  val dateString: String,
  val dateAddedEpochSeconds: Long = 0L,
  val status: PhotoStatus = PhotoStatus.UNREVIEWED,
  val isFavorite: Boolean = false,
  val category: PhotoCategory = PhotoCategory.CAMERA,
  val resolution: String = "1080 x 1920"
) {
  val contentUri: Uri?
    get() = uriString?.let { Uri.parse(it) }

  val formattedSize: String
    get() {
      val kb = fileSizeBytes / 1024.0
      val mb = kb / 1024.0
      val gb = mb / 1024.0
      return when {
        gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
      }
    }
}
