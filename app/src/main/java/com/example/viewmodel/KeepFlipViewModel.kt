package com.example.viewmodel

import android.content.Context
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DeleteRequestResult
import com.example.data.PhotoRepository
import com.example.model.PhotoItem
import com.example.model.PhotoStatus
import com.example.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppScreen {
  SPLASH,
  DASHBOARD,
  SWIPE_DECK,
  GALLERY,
  TRASH
}

enum class GalleryFilter(val label: String) {
  ALL("All"),
  KEPT("Kept"),
  UNREVIEWED("Unreviewed"),
  FAVORITES("Favorites")
}

data class SwipeAction(
  val photoId: String,
  val previousStatus: PhotoStatus
)

class KeepFlipViewModel(
  private val repository: PhotoRepository = PhotoRepository()
) : ViewModel() {

  private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  val allPhotos: StateFlow<List<PhotoItem>> = repository.photos
  val isLoading: StateFlow<Boolean> = repository.isLoading

  val unreviewedPhotos: StateFlow<List<PhotoItem>> = allPhotos.map { list ->
    list.filter { it.status == PhotoStatus.UNREVIEWED }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val keptPhotos: StateFlow<List<PhotoItem>> = allPhotos.map { list ->
    list.filter { it.status == PhotoStatus.KEPT }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val flippedPhotos: StateFlow<List<PhotoItem>> = allPhotos.map { list ->
    list.filter { it.status == PhotoStatus.FLIPPED }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val favoritePhotos: StateFlow<List<PhotoItem>> = allPhotos.map { list ->
    list.filter { it.isFavorite }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val totalCleanedBytes: StateFlow<Long> = repository.totalCleanedBytes

  val pendingReclaimBytes: StateFlow<Long> = flippedPhotos.map { list ->
    list.sumOf { it.fileSizeBytes }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

  private val _hasPermission = MutableStateFlow(false)
  val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

  private val _permissionRequestedOnce = MutableStateFlow(false)
  val permissionRequestedOnce: StateFlow<Boolean> = _permissionRequestedOnce.asStateFlow()

  private val _undoStack = MutableStateFlow<List<SwipeAction>>(emptyList())
  val canUndo: StateFlow<Boolean> = _undoStack.map { it.isNotEmpty() }
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

  private val _galleryFilter = MutableStateFlow(GalleryFilter.ALL)
  val galleryFilter: StateFlow<GalleryFilter> = _galleryFilter.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  // Track selected photo IDs in Recently Flipped Photos review mode
  private val _selectedFlippedIds = MutableStateFlow<Set<String>>(emptySet())
  val selectedFlippedIds: StateFlow<Set<String>> = _selectedFlippedIds.asStateFlow()

  // Track photo IDs currently pending system batch-deletion dialog
  private val _pendingDeletePhotoIds = MutableStateFlow<List<String>>(emptyList())
  val pendingDeletePhotoIds: StateFlow<List<String>> = _pendingDeletePhotoIds.asStateFlow()

  // Track whether a batch delete request is actively in-flight to prevent duplicate prompts
  private val _isBatchDeleteInProgress = MutableStateFlow(false)
  val isBatchDeleteInProgress: StateFlow<Boolean> = _isBatchDeleteInProgress.asStateFlow()

  fun initGallery(context: Context) {
    val granted = PermissionUtils.hasGalleryPermission(context)
    _hasPermission.value = granted
    if (granted) {
      loadGallery(context)
    }
  }

  fun updatePermissionState(granted: Boolean, context: Context) {
    _hasPermission.value = granted
    _permissionRequestedOnce.value = true
    if (granted) {
      loadGallery(context)
    }
  }

  fun loadGallery(context: Context) {
    viewModelScope.launch {
      repository.loadDevicePhotos(context)
    }
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  /**
   * KEEP action:
   * Preserves original photo on device, marks as KEPT in session, advances queue.
   */
  fun keepPhoto(photoId: String, context: Context? = null) {
    val photo = allPhotos.value.find { it.id == photoId } ?: return
    _undoStack.update { it + SwipeAction(photoId, photo.status) }
    repository.keepPhoto(photoId, context)
  }

  /**
   * FLIP action:
   * Adds photo to Recently Flipped Photos queue without showing any deletion permission dialog.
   * Does NOT delete the original file.
   */
  fun flipPhoto(photoId: String, context: Context? = null) {
    val photo = allPhotos.value.find { it.id == photoId } ?: return
    _undoStack.update { it + SwipeAction(photoId, photo.status) }
    repository.flipPhoto(photoId, context)
  }

  fun undoLastAction(context: Context? = null) {
    val lastAction = _undoStack.value.lastOrNull() ?: return
    _undoStack.update { it.dropLast(1) }
    if (lastAction.previousStatus == PhotoStatus.UNREVIEWED) {
      val current = allPhotos.value.find { it.id == lastAction.photoId }
      if (current?.status == PhotoStatus.FLIPPED) {
        repository.unflipPhoto(lastAction.photoId, context)
        _statusMessage.value = "Restored flipped photo to deck"
      } else if (current?.status == PhotoStatus.KEPT) {
        repository.unkeepPhoto(lastAction.photoId, context)
        _statusMessage.value = "Undid keep action"
      }
    }
  }

  // Selection Controls for Recently Flipped Photos Review Mode
  fun togglePhotoSelection(photoId: String) {
    _selectedFlippedIds.update { current ->
      if (photoId in current) current - photoId else current + photoId
    }
  }

  fun selectAllFlipped() {
    _selectedFlippedIds.value = flippedPhotos.value.map { it.id }.toSet()
  }

  fun deselectAllFlipped() {
    _selectedFlippedIds.value = emptySet()
  }

  /**
   * Keep Selected:
   * Preserves original files on device and removes selected photos from the pending deletion queue.
   */
  fun keepSelectedPhotos(context: Context? = null) {
    val idsToKeep = _selectedFlippedIds.value
    if (idsToKeep.isEmpty()) return

    repository.keepSelectedPhotos(idsToKeep, context)
    _selectedFlippedIds.value = emptySet()
    _statusMessage.value = "${idsToKeep.size} photos kept safe on device"
  }

  fun restoreFlippedPhoto(photoId: String, context: Context? = null) {
    repository.unflipPhoto(photoId, context)
    _selectedFlippedIds.update { it - photoId }
    _statusMessage.value = "Photo restored to review deck"
  }

  fun restoreAllFlipped(context: Context? = null) {
    repository.restoreAllFlipped(context)
    _selectedFlippedIds.value = emptySet()
    _statusMessage.value = "Restored all flipped photos to deck"
  }

  fun toggleFavorite(photoId: String) {
    repository.toggleFavorite(photoId)
  }

  /**
   * User-controlled batch deletion:
   * Only called when user explicitly confirms permanent deletion.
   */
  fun triggerManualBatchDelete(
    context: Context,
    photos: List<PhotoItem>,
    onConsentNeeded: (IntentSender) -> Unit
  ) {
    if (photos.isEmpty() || _isBatchDeleteInProgress.value) return

    _isBatchDeleteInProgress.value = true
    _pendingDeletePhotoIds.value = photos.map { it.id }

    when (val result = repository.prepareDeleteRequest(context, photos)) {
      is DeleteRequestResult.RequiresUserConsent -> {
        onConsentNeeded(result.intentSender)
      }
      is DeleteRequestResult.Completed -> {
        _isBatchDeleteInProgress.value = false
        val deletedIds = result.photoIds
        _pendingDeletePhotoIds.value = emptyList()
        _selectedFlippedIds.update { it - deletedIds.toSet() }
        repository.onPhotosPermanentlyDeleted(deletedIds, context)
        val totalBytes = photos.filter { it.id in deletedIds }.sumOf { it.fileSizeBytes }
        val mb = totalBytes / (1024.0 * 1024.0)
        _statusMessage.value = String.format(Locale.US, "Deleted %d photos • Freed %.1f MB", deletedIds.size, mb)
      }
      is DeleteRequestResult.Error -> {
        _isBatchDeleteInProgress.value = false
        _pendingDeletePhotoIds.value = emptyList()
        _statusMessage.value = result.message
      }
    }
  }

  fun triggerDeleteSelected(
    context: Context,
    onConsentNeeded: (IntentSender) -> Unit
  ) {
    val selectedIds = _selectedFlippedIds.value
    if (selectedIds.isEmpty()) return
    val photosToDelete = flippedPhotos.value.filter { it.id in selectedIds }
    triggerManualBatchDelete(context, photosToDelete, onConsentNeeded)
  }

  /**
   * Called when Android system confirms deletion in the batch user-consent dialog.
   */
  fun onBatchDeletionConfirmed(context: Context) {
    _isBatchDeleteInProgress.value = false
    val pendingIds = _pendingDeletePhotoIds.value
    if (pendingIds.isNotEmpty()) {
      val deletedPhotos = allPhotos.value.filter { it.id in pendingIds }
      val totalBytes = deletedPhotos.sumOf { it.fileSizeBytes }
      repository.onPhotosPermanentlyDeleted(pendingIds, context)
      _pendingDeletePhotoIds.value = emptyList()
      _selectedFlippedIds.update { it - pendingIds.toSet() }

      // Refresh gallery data so UI stays synchronized with device
      loadGallery(context)

      val mb = totalBytes / (1024.0 * 1024.0)
      _statusMessage.value = String.format(
        Locale.US,
        "Successfully deleted %d photos from device • Freed %.1f MB",
        deletedPhotos.size,
        mb
      )
    }
  }

  /**
   * Called when user cancels the Android system batch-deletion confirmation dialog.
   */
  fun onBatchDeletionCancelled() {
    _isBatchDeleteInProgress.value = false
    _pendingDeletePhotoIds.value = emptyList()
    _statusMessage.value = "Deletion cancelled. Photos kept in Recently Flipped queue."
  }

  fun onPhotosPermanentlyDeleted(deletedIds: List<String>, context: Context? = null) {
    val deletedPhotos = allPhotos.value.filter { it.id in deletedIds }
    val totalBytes = deletedPhotos.sumOf { it.fileSizeBytes }
    repository.onPhotosPermanentlyDeleted(deletedIds, context)
    _pendingDeletePhotoIds.value = emptyList()
    _selectedFlippedIds.update { it - deletedIds.toSet() }

    val mb = totalBytes / (1024.0 * 1024.0)
    _statusMessage.value = String.format(Locale.US, "Deleted %d photos • Freed %.1f MB", deletedIds.size, mb)
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  fun setGalleryFilter(filter: GalleryFilter) {
    _galleryFilter.value = filter
  }

  fun importPhotos(uris: List<String>) {
    if (uris.isNotEmpty()) {
      repository.importCustomPhotos(uris)
      _statusMessage.value = "Imported ${uris.size} photos to review"
    }
  }

  fun createTestPhotos(context: Context) {
    viewModelScope.launch {
      val success = repository.createTestDevicePhotos(context, 3)
      if (success) {
        _statusMessage.value = "Added test photos to device gallery"
      }
    }
  }

  fun resetSession(context: Context? = null) {
    repository.resetSession(context)
    _undoStack.value = emptyList()
    _selectedFlippedIds.value = emptySet()
    _isBatchDeleteInProgress.value = false
    _statusMessage.value = "Session reset"
  }

  companion object {
    fun formatBytes(bytes: Long): String {
      val kb = bytes / 1024.0
      val mb = kb / 1024.0
      val gb = mb / 1024.0
      return when {
        gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
      }
    }
  }
}
