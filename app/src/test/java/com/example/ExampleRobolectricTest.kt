package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PhotoCategory
import com.example.model.PhotoItem
import com.example.model.PhotoStatus
import com.example.util.PermissionUtils
import com.example.viewmodel.AppScreen
import com.example.viewmodel.KeepFlipViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Keep or Flip", appName)
  }

  @Test
  fun `permission check helper returns boolean`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val perms = PermissionUtils.getRequiredPermissions()
    assertTrue("Permissions array should not be empty", perms.isNotEmpty())
    assertNotNull("Permission status should be readable", PermissionUtils.hasGalleryPermission(context))
  }

  @Test
  fun `opening recently flipped photos never deletes anything automatically`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = KeepFlipViewModel()

    val uris = (1..5).map { "content://media/external/images/media/auto_$it" }
    viewModel.importPhotos(uris)

    // Flip 5 photos
    for (p in viewModel.allPhotos.value) {
      viewModel.flipPhoto(p.id, context)
    }

    assertEquals(5, viewModel.flippedPhotos.value.size)
    assertEquals(0L, viewModel.totalCleanedBytes.value)

    // Simulate navigating to Recently Flipped Photos
    viewModel.navigateTo(AppScreen.TRASH)

    // Must still have all 5 photos and 0 bytes cleaned (NO automatic deletion!)
    assertEquals(5, viewModel.flippedPhotos.value.size)
    assertEquals(0L, viewModel.totalCleanedBytes.value)
  }

  @Test
  fun `test review mode select 3 of 10 and delete selected`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = KeepFlipViewModel()

    val uris = (1..10).map { "content://media/external/images/media/sub_$it" }
    viewModel.importPhotos(uris)

    for (p in viewModel.allPhotos.value) {
      viewModel.flipPhoto(p.id, context)
    }
    assertEquals(10, viewModel.flippedPhotos.value.size)

    // Select only 3 photos
    val flipped = viewModel.flippedPhotos.value
    val selectThree = flipped.take(3)
    for (p in selectThree) {
      viewModel.togglePhotoSelection(p.id)
    }
    assertEquals(3, viewModel.selectedFlippedIds.value.size)

    // Delete only the 3 selected photos
    val selectedIds = selectThree.map { it.id }
    val selectedBytes = selectThree.sumOf { it.fileSizeBytes }
    viewModel.onPhotosPermanentlyDeleted(selectedIds, context)

    // Queue must now have 7 photos remaining, and total cleaned must equal size of 3 photos
    assertEquals(7, viewModel.flippedPhotos.value.size)
    assertEquals(selectedBytes, viewModel.totalCleanedBytes.value)
    assertEquals(0, viewModel.selectedFlippedIds.value.size)
  }

  @Test
  fun `test keep selected preserves photos and removes from deletion queue`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = KeepFlipViewModel()

    val uris = (1..6).map { "content://media/external/images/media/keep_$it" }
    viewModel.importPhotos(uris)

    for (p in viewModel.allPhotos.value) {
      viewModel.flipPhoto(p.id, context)
    }
    assertEquals(6, viewModel.flippedPhotos.value.size)

    // Select 2 photos to keep
    val flipped = viewModel.flippedPhotos.value
    val toKeep = flipped.take(2)
    for (p in toKeep) {
      viewModel.togglePhotoSelection(p.id)
    }
    assertEquals(2, viewModel.selectedFlippedIds.value.size)

    // Press Keep Selected
    viewModel.keepSelectedPhotos(context)

    // Flipped queue must now be 4 photos
    assertEquals(4, viewModel.flippedPhotos.value.size)
    // Kept photos must now include the 2 photos
    assertEquals(2, viewModel.keptPhotos.value.size)
    assertEquals(0, viewModel.selectedFlippedIds.value.size)
    // No storage deleted
    assertEquals(0L, viewModel.totalCleanedBytes.value)
  }

  @Test
  fun `test flip 20 photos and batch delete confirmation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = KeepFlipViewModel()

    val uris = (1..20).map { "content://media/external/images/media/$it" }
    viewModel.importPhotos(uris)

    for (photo in viewModel.allPhotos.value) {
      viewModel.flipPhoto(photo.id, context)
    }

    assertEquals(20, viewModel.flippedPhotos.value.size)
    assertEquals(0L, viewModel.totalCleanedBytes.value)

    // Select all
    viewModel.selectAllFlipped()
    assertEquals(20, viewModel.selectedFlippedIds.value.size)

    // User cancels deletion
    viewModel.onBatchDeletionCancelled()
    assertEquals(20, viewModel.flippedPhotos.value.size)
    assertEquals(0L, viewModel.totalCleanedBytes.value)

    // User confirms deletion
    val expectedFreedBytes = viewModel.flippedPhotos.value.sumOf { it.fileSizeBytes }
    viewModel.onPhotosPermanentlyDeleted(viewModel.flippedPhotos.value.map { it.id }, context)

    assertEquals(0, viewModel.flippedPhotos.value.size)
    assertEquals(expectedFreedBytes, viewModel.totalCleanedBytes.value)
  }

  @Test
  fun `test about developer credit info and links`() {
    org.junit.Assert.assertEquals("Ravi ranjan", com.example.util.LinkUtils.DEVELOPER_NAME)
    org.junit.Assert.assertEquals("https://github.com/Ravi-Ranjandev", com.example.util.LinkUtils.GITHUB_URL)
    org.junit.Assert.assertTrue(com.example.util.LinkUtils.LINKEDIN_URL.contains("ravi-ranjan-39a2a8279"))
    org.junit.Assert.assertTrue(com.example.util.LinkUtils.LINKEDIN_DISPLAY_URL.contains("ravi-ranjan-39a2a8279"))
  }
}
