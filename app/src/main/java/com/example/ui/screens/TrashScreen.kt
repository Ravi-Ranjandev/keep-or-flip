package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PhotoItem
import com.example.model.PhotoStatus
import com.example.ui.components.PhotoImageView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.FlipRed
import com.example.ui.theme.KeepGreen
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RighteousFontFamily
import com.example.viewmodel.KeepFlipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
  viewModel: KeepFlipViewModel,
  onTriggerDeleteSelected: (List<PhotoItem>) -> Unit,
  onBackPressed: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackPressed() }

  val context = LocalContext.current
  val view = LocalView.current
  val flippedPhotos by viewModel.flippedPhotos.collectAsState()
  val selectedIds by viewModel.selectedFlippedIds.collectAsState()
  val totalCleanedBytes by viewModel.totalCleanedBytes.collectAsState()
  val isBatchDeleteInProgress by viewModel.isBatchDeleteInProgress.collectAsState()

  // In-app confirmation dialog state
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }

  // Photo Preview Dialog state
  var previewPhoto by remember { mutableStateOf<PhotoItem?>(null) }

  val selectedPhotos = flippedPhotos.filter { it.id in selectedIds }
  val selectedSize = selectedPhotos.sumOf { it.fileSizeBytes }
  val pendingTotalSize = flippedPhotos.sumOf { it.fileSizeBytes }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    // Top Bar with Selection Controls
    TopAppBar(
      title = {
        Text(
          text = "Recently Flipped",
          style = TextStyle(
            fontFamily = RighteousFontFamily,
            fontSize = 20.sp,
            color = Color.White
          )
        )
      },
      navigationIcon = {
        IconButton(
          onClick = onBackPressed,
          modifier = Modifier.testTag("trash_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
      },
      actions = {
        if (flippedPhotos.isNotEmpty()) {
          val allSelected = selectedIds.size == flippedPhotos.size
          TextButton(
            onClick = {
              view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
              if (allSelected) {
                viewModel.deselectAllFlipped()
              } else {
                viewModel.selectAllFlipped()
              }
            },
            modifier = Modifier.testTag("select_all_toggle_button")
          ) {
            Text(
              text = if (allSelected) "Deselect All" else "Select All",
              color = Color(0xFF60A5FA),
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            )
          }
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
    )

    // Storage Status & Selection Summary Header
    Surface(
      color = Color(0xFF1B161B),
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (selectedIds.isNotEmpty()) {
              "Selected ${selectedIds.size} of ${flippedPhotos.size} photos"
            } else if (flippedPhotos.isNotEmpty()) {
              "${flippedPhotos.size} photos pending review"
            } else {
              "Total Storage Cleaned"
            },
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Medium,
              fontSize = 13.sp,
              color = if (selectedIds.isNotEmpty()) Color(0xFFFCA5A5) else Color(0xFFFDA4AF)
            )
          )
          Text(
            text = if (selectedIds.isNotEmpty()) {
              KeepFlipViewModel.formatBytes(selectedSize)
            } else if (flippedPhotos.isNotEmpty()) {
              KeepFlipViewModel.formatBytes(pendingTotalSize)
            } else {
              KeepFlipViewModel.formatBytes(totalCleanedBytes)
            },
            style = TextStyle(
              fontFamily = RighteousFontFamily,
              fontSize = 24.sp,
              color = Color.White
            )
          )
          Text(
            text = if (flippedPhotos.isNotEmpty()) {
              "Tap photos to select • Press Delete Selected to clean"
            } else {
              "Original files safely removed from device"
            },
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontSize = 12.sp,
              color = Color(0xFF94A3B8)
            )
          )
        }

        Surface(
          color = FlipRed.copy(alpha = 0.2f),
          shape = CircleShape,
          modifier = Modifier.size(52.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.DeleteForever,
              contentDescription = null,
              tint = FlipRed,
              modifier = Modifier.size(28.dp)
            )
          }
        }
      }
    }

    // Grid of Recently Flipped Photos or Empty State
    if (flippedPhotos.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(32.dp)
        ) {
          Surface(
            color = Color(0x2216A34A),
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = KeepGreen,
                modifier = Modifier.size(44.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "No Flipped Photos",
            style = TextStyle(
              fontFamily = RighteousFontFamily,
              fontSize = 22.sp,
              color = Color.White
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (totalCleanedBytes > 0) {
              "All selected photos have been cleaned! Total freed: ${KeepFlipViewModel.formatBytes(totalCleanedBytes)}."
            } else {
              "Swipe left on photos in the deck to add them here for review and batch cleaning."
            },
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontSize = 14.sp,
              color = Color(0xFF94A3B8)
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      // Photo Review Grid
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("trash_photos_grid"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(flippedPhotos, key = { it.id }) { photo ->
          val isSelected = photo.id in selectedIds

          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) Color(0xFF221111) else DarkSurfaceCard
            ),
            border = if (isSelected) {
              androidx.compose.foundation.BorderStroke(2.5.dp, FlipRed)
            } else {
              androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF233A2C))
            },
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(0.85f)
              .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.togglePhotoSelection(photo.id)
              }
              .testTag("trash_item_${photo.id}")
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              PhotoImageView(
                photo = photo,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )

              // Selection Tint Overlay
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55DC2626))
                )
              } else {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000))
                )
              }

              // Selection Checkmark Badge (Top Start)
              Surface(
                color = if (isSelected) FlipRed else Color(0x99000000),
                shape = CircleShape,
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color.White) else null,
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(10.dp)
                  .size(28.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }

              // Fullscreen Preview Icon (Top End)
              Surface(
                color = Color(0xCC000000),
                shape = CircleShape,
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
                  .size(32.dp)
                  .clip(CircleShape)
                  .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    previewPhoto = photo
                  }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Preview larger size",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              // Bottom photo details
              Surface(
                color = Color(0xDD000000),
                shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .align(Alignment.BottomCenter)
              ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                  Text(
                    text = photo.title,
                    style = TextStyle(
                      fontFamily = OutfitFontFamily,
                      fontWeight = FontWeight.Medium,
                      fontSize = 12.sp,
                      color = Color.White
                    ),
                    maxLines = 1
                  )
                  Text(
                    text = "${photo.formattedSize} • ${photo.dateString}",
                    style = TextStyle(
                      fontFamily = OutfitFontFamily,
                      fontSize = 11.sp,
                      color = if (isSelected) Color(0xFFFCA5A5) else Color(0xFFCBD5E1)
                    )
                  )
                }
              }
            }
          }
        }
      }
    }

    // Bottom Action Bar: Keep Selected & Delete Selected Controls
    if (flippedPhotos.isNotEmpty()) {
      Surface(
        color = Color(0xFF131D16),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F3526)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // "Keep Selected" Button — Preserves original files and removes from deletion queue
            OutlinedButton(
              onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.keepSelectedPhotos(context)
              },
              enabled = selectedIds.isNotEmpty(),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("keep_selected_button"),
              shape = RoundedCornerShape(24.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = KeepGreen,
                disabledContentColor = Color(0x5594A3B8)
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (selectedIds.isNotEmpty()) KeepGreen else Color(0x3394A3B8)
              )
            ) {
              Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = if (selectedIds.isNotEmpty()) KeepGreen else Color(0x5594A3B8), modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (selectedIds.isNotEmpty()) "Keep Selected (${selectedIds.size})" else "Keep Selected",
                style = TextStyle(
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp
                )
              )
            }

            // "Delete Selected (N)" Button — Triggers in-app summary confirmation then system batch deletion
            Button(
              onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                showDeleteConfirmDialog = true
              },
              enabled = selectedIds.isNotEmpty() && !isBatchDeleteInProgress,
              modifier = Modifier
                .weight(1.2f)
                .height(48.dp)
                .testTag("delete_selected_button"),
              shape = RoundedCornerShape(24.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = FlipRed,
                disabledContainerColor = Color(0xFF3B1D1D),
                disabledContentColor = Color(0x55FFFFFF)
              )
            ) {
              Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (selectedIds.isNotEmpty()) {
                  "Delete Selected (${selectedIds.size})"
                } else {
                  "Delete Selected"
                },
                style = TextStyle(
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color.White
                )
              )
            }
          }
        }
      }
    }
  }

  // In-app Confirmation Summary Dialog before permanent deletion
  if (showDeleteConfirmDialog && selectedPhotos.isNotEmpty()) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmDialog = false },
      title = {
        Text(
          text = "Permanently delete ${selectedPhotos.size} photos?",
          style = TextStyle(
            fontFamily = RighteousFontFamily,
            fontSize = 20.sp,
            color = Color.White
          )
        )
      },
      text = {
        Text(
          text = "This will submit a batch deletion request to Android to remove ${selectedPhotos.size} photos (${KeepFlipViewModel.formatBytes(selectedSize)}) from your device storage. Deleted photos cannot be restored.",
          style = TextStyle(
            fontFamily = OutfitFontFamily,
            fontSize = 14.sp,
            color = Color(0xFFCBD5E1)
          )
        )
      },
      containerColor = Color(0xFF1E293B),
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirmDialog = false
            onTriggerDeleteSelected(selectedPhotos)
          },
          colors = ButtonDefaults.buttonColors(containerColor = FlipRed)
        ) {
          Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmDialog = false }) {
          Text("Cancel", color = Color(0xFF94A3B8))
        }
      }
    )
  }

  // Larger Photo Preview Dialog
  previewPhoto?.let { photo ->
    Dialog(onDismissRequest = { previewPhoto = null }) {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131F17)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1f)
          ) {
            PhotoImageView(
              photo = photo,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit
            )

            IconButton(
              onClick = { previewPhoto = null },
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color(0x88000000), CircleShape)
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close preview", tint = Color.White)
            }
          }

          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = photo.title,
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Size: ${photo.formattedSize} • Resolution: ${photo.resolution}",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
              )
            )
            Text(
              text = "Date: ${photo.dateString}",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              val isSelected = photo.id in selectedIds
              OutlinedButton(
                onClick = {
                  viewModel.togglePhotoSelection(photo.id)
                },
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FlipRed else Color(0xFF64748B))
              ) {
                Text(
                  text = if (isSelected) "Deselect" else "Select",
                  color = if (isSelected) FlipRed else Color.White
                )
              }

              Button(
                onClick = {
                  viewModel.restoreFlippedPhoto(photo.id, context)
                  previewPhoto = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = KeepGreen),
                shape = RoundedCornerShape(16.dp)
              ) {
                Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Keep & Restore", color = Color.White)
              }
            }
          }
        }
      }
    }
  }
}
