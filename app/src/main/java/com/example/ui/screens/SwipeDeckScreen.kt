package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhotoItem
import com.example.ui.components.PhotoImageView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.FlipRed
import com.example.ui.theme.KeepGreen
import com.example.ui.theme.OpenTrashBlue
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RighteousFontFamily
import com.example.viewmodel.AppScreen
import com.example.viewmodel.KeepFlipViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeDeckScreen(
  viewModel: KeepFlipViewModel,
  onRequestPermissions: () -> Unit,
  onBackPressed: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackPressed() }

  val context = LocalContext.current
  val view = LocalView.current
  val coroutineScope = rememberCoroutineScope()

  val allPhotos by viewModel.allPhotos.collectAsState()
  val unreviewedPhotos by viewModel.unreviewedPhotos.collectAsState()
  val keptPhotos by viewModel.keptPhotos.collectAsState()
  val flippedPhotos by viewModel.flippedPhotos.collectAsState()
  val totalCleanedBytes by viewModel.totalCleanedBytes.collectAsState()
  val pendingReclaimBytes by viewModel.pendingReclaimBytes.collectAsState()
  val hasPermission by viewModel.hasPermission.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val canUndo by viewModel.canUndo.collectAsState()

  val currentPhoto = unreviewedPhotos.firstOrNull()
  val nextPhoto = unreviewedPhotos.getOrNull(1)

  // Drag physics animation state
  val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
  var isSwipingOut by remember { mutableStateOf(false) }

  fun triggerKeep(photo: PhotoItem) {
    isSwipingOut = true
    coroutineScope.launch {
      dragOffset.animateTo(
        targetValue = Offset(1200f, 0f),
        animationSpec = tween(220, easing = FastOutSlowInEasing)
      )
      view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
      viewModel.keepPhoto(photo.id, context)
      dragOffset.snapTo(Offset.Zero)
      isSwipingOut = false
    }
  }

  fun triggerFlip(photo: PhotoItem) {
    // Add photo to Recently Flipped Photos queue without interrupting swipe with permission dialogs
    isSwipingOut = true
    coroutineScope.launch {
      dragOffset.animateTo(
        targetValue = Offset(-1200f, 0f),
        animationSpec = tween(220, easing = FastOutSlowInEasing)
      )
      view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
      viewModel.flipPhoto(photo.id, context)
      dragOffset.snapTo(Offset.Zero)
      isSwipingOut = false
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar matching screenshot 3 ("<" back button)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      IconButton(
        onClick = onBackPressed,
        modifier = Modifier.testTag("back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = Color.White,
          modifier = Modifier.size(28.dp)
        )
      }

      // Real gallery photo counter: e.g. "12 of 240"
      if (allPhotos.isNotEmpty()) {
        val total = allPhotos.size
        val reviewed = total - unreviewedPhotos.size
        Surface(
          color = Color(0x33FFFFFF),
          shape = RoundedCornerShape(14.dp)
        ) {
          Text(
            text = "$reviewed of $total",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          )
        }
      } else {
        Text(
          text = "Keep or Flip",
          style = TextStyle(
            fontFamily = RighteousFontFamily,
            fontSize = 18.sp,
            color = Color.White
          )
        )
      }

      // Undo button (reverts last Keep or Flip action)
      IconButton(
        onClick = {
          if (canUndo) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.undoLastAction(context)
          }
        },
        enabled = canUndo,
        modifier = Modifier.testTag("top_undo_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Undo,
          contentDescription = "Undo",
          tint = if (canUndo) Color(0xFFF59E0B) else Color(0x44FFFFFF),
          modifier = Modifier.size(24.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Card Deck Container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      contentAlignment = Alignment.Center
    ) {
      when {
        !hasPermission -> {
          // Permission required state
          Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(0.75f)
              .padding(16.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Surface(
                color = Color(0x22F59E0B),
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(32.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = "Gallery Permission Needed",
                style = TextStyle(
                  fontFamily = RighteousFontFamily,
                  fontSize = 20.sp,
                  color = Color.White
                ),
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "To clean up your real phone storage, Keep or Flip needs access to read your device photos.",
                style = TextStyle(
                  fontFamily = OutfitFontFamily,
                  fontSize = 14.sp,
                  color = Color(0xFF94A3B8)
                ),
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(20.dp))

              Button(
                onClick = onRequestPermissions,
                colors = ButtonDefaults.buttonColors(containerColor = KeepGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.testTag("grant_permission_deck_button")
              ) {
                Text(
                  text = "Grant Gallery Access",
                  color = Color.White,
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
        isLoading -> {
          // Loading spinner
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator(color = KeepGreen, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Loading device gallery...",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 15.sp,
                color = Color(0xFF94A3B8)
              )
            )
          }
        }
        currentPhoto != null -> {
          // Active Photo Card
          if (nextPhoto != null) {
            // Next Card underneath
            Card(
              shape = RoundedCornerShape(28.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
              modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(0.72f)
                .offset(y = 12.dp)
            ) {
              PhotoImageView(
                photo = nextPhoto,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }
          }

          val rotationAngle = (dragOffset.value.x / 20f).coerceIn(-25f, 25f)
          val keepAlpha = (dragOffset.value.x / 140f).coerceIn(0f, 1f)
          val flipAlpha = (-dragOffset.value.x / 140f).coerceIn(0f, 1f)

          Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(0.72f)
              .offset {
                IntOffset(
                  dragOffset.value.x.roundToInt(),
                  (dragOffset.value.y * 0.4f).roundToInt()
                )
              }
              .rotate(rotationAngle)
              .pointerInput(currentPhoto.id) {
                detectDragGestures(
                  onDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                      dragOffset.snapTo(
                        Offset(
                          dragOffset.value.x + dragAmount.x,
                          dragOffset.value.y + dragAmount.y
                        )
                      )
                    }
                  },
                  onDragEnd = {
                    val threshold = 220f
                    when {
                      dragOffset.value.x > threshold -> triggerKeep(currentPhoto)
                      dragOffset.value.x < -threshold -> triggerFlip(currentPhoto)
                      else -> {
                        coroutineScope.launch {
                          dragOffset.animateTo(
                            Offset.Zero,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
                          )
                        }
                      }
                    }
                  },
                  onDragCancel = {
                    coroutineScope.launch {
                      dragOffset.animateTo(Offset.Zero)
                    }
                  }
                )
              }
              .testTag("active_swipe_card")
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              PhotoImageView(
                photo = currentPhoto,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )

              // Dynamic KEEP Stamp overlay (Top Left)
              if (keepAlpha > 0f) {
                Surface(
                  color = KeepGreen.copy(alpha = keepAlpha * 0.85f),
                  shape = RoundedCornerShape(16.dp),
                  border = androidx.compose.foundation.BorderStroke(3.dp, Color.White.copy(alpha = keepAlpha)),
                  modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
                    .rotate(-15f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "KEEP",
                      style = TextStyle(
                        fontFamily = RighteousFontFamily,
                        fontSize = 24.sp,
                        color = Color.White
                      )
                    )
                  }
                }
              }

              // Dynamic FLIP Stamp overlay (Top Right)
              if (flipAlpha > 0f) {
                Surface(
                  color = FlipRed.copy(alpha = flipAlpha * 0.85f),
                  shape = RoundedCornerShape(16.dp),
                  border = androidx.compose.foundation.BorderStroke(3.dp, Color.White.copy(alpha = flipAlpha)),
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .rotate(15f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "FLIP",
                      style = TextStyle(
                        fontFamily = RighteousFontFamily,
                        fontSize = 24.sp,
                        color = Color.White
                      )
                    )
                  }
                }
              }

              // Bottom info gradient
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(110.dp)
                  .align(Alignment.BottomCenter)
                  .background(
                    Brush.verticalGradient(
                      0.0f to Color.Transparent,
                      1.0f to Color(0xEE000000)
                    )
                  )
              )

              // Metadata overlay
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .align(Alignment.BottomCenter)
                  .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = currentPhoto.title,
                    style = TextStyle(
                      fontFamily = OutfitFontFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = Color.White
                    ),
                    maxLines = 1
                  )
                  Text(
                    text = "${currentPhoto.formattedSize} • ${currentPhoto.dateString}",
                    style = TextStyle(
                      fontFamily = OutfitFontFamily,
                      fontSize = 13.sp,
                      color = Color(0xFFCBD5E1)
                    )
                  )
                }

                // Favorite toggle
                IconButton(
                  onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    viewModel.toggleFavorite(currentPhoto.id)
                  }
                ) {
                  Icon(
                    imageVector = if (currentPhoto.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (currentPhoto.isFavorite) Color(0xFFF43F5E) else Color.White,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }
            }
          }
        }
        else -> {
          // Empty or Completed State
          Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(0.85f)
              .padding(12.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Surface(
                color = if (flippedPhotos.isNotEmpty()) Color(0x22EF4444) else if (keptPhotos.isNotEmpty()) Color(0x2222C55E) else Color(0x2260A5FA),
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = if (flippedPhotos.isNotEmpty()) Icons.Default.DeleteSweep else if (keptPhotos.isNotEmpty()) Icons.Default.Check else Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = if (flippedPhotos.isNotEmpty()) FlipRed else if (keptPhotos.isNotEmpty()) KeepGreen else Color(0xFF60A5FA),
                    modifier = Modifier.size(38.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = if (allPhotos.isEmpty()) "No Photos Found" else "All Photos Reviewed!",
                style = TextStyle(
                  fontFamily = RighteousFontFamily,
                  fontSize = 24.sp,
                  color = Color.White
                ),
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = if (allPhotos.isEmpty()) {
                  "Your device gallery has no photos right now. Take photos or add test photos to start cleaning."
                } else if (flippedPhotos.isNotEmpty()) {
                  "${flippedPhotos.size} photos flipped (${KeepFlipViewModel.formatBytes(pendingReclaimBytes)}). Tap 'oppen trash' below to batch delete and free storage!"
                } else {
                  "You've reviewed your gallery! Kept: ${keptPhotos.size} photos safe on your device."
                },
                style = TextStyle(
                  fontFamily = OutfitFontFamily,
                  fontSize = 14.sp,
                  color = Color(0xFF94A3B8)
                ),
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(20.dp))

              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (allPhotos.isEmpty()) {
                  Button(
                    onClick = { viewModel.createTestPhotos(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                    shape = RoundedCornerShape(20.dp)
                  ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Test Photos", color = Color.White)
                  }
                } else {
                  Button(
                    onClick = { viewModel.resetSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27382D)),
                    shape = RoundedCornerShape(20.dp)
                  ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Review Again", color = Color.White)
                  }
                }

                Button(
                  onClick = { viewModel.loadGallery(context) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                  shape = RoundedCornerShape(20.dp)
                ) {
                  Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Refresh", color = Color.White)
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Action Controls below card (Flip, Favorite, Undo, Keep)
    if (currentPhoto != null) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Red Flip button: queues photo to Recently Flipped without permission dialog
        Surface(
          color = FlipRed.copy(alpha = 0.2f),
          shape = CircleShape,
          border = androidx.compose.foundation.BorderStroke(2.dp, FlipRed),
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable { triggerFlip(currentPhoto) }
            .testTag("flip_action_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Flip photo to trash queue",
              tint = FlipRed,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // Star / Favorite button
        Surface(
          color = Color(0x22F59E0B),
          shape = CircleShape,
          border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF59E0B)),
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable {
              view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
              viewModel.toggleFavorite(currentPhoto.id)
            }
            .testTag("favorite_action_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (currentPhoto.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Toggle Favorite",
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(24.dp)
            )
          }
        }

        // Undo button (restores previous Keep or Flip action)
        Surface(
          color = if (canUndo) Color(0x2264748B) else Color(0x1164748B),
          shape = CircleShape,
          border = androidx.compose.foundation.BorderStroke(1.dp, if (canUndo) Color(0xFF94A3B8) else Color(0x3394A3B8)),
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = canUndo) {
              view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
              viewModel.undoLastAction(context)
            }
            .testTag("undo_action_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Undo,
              contentDescription = "Undo",
              tint = if (canUndo) Color.White else Color(0x44FFFFFF),
              modifier = Modifier.size(22.dp)
            )
          }
        }

        // Green Keep button (Leaves photo untouched on device)
        Surface(
          color = KeepGreen.copy(alpha = 0.2f),
          shape = CircleShape,
          border = androidx.compose.foundation.BorderStroke(2.dp, KeepGreen),
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable { triggerKeep(currentPhoto) }
            .testTag("keep_action_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Keep photo",
              tint = KeepGreen,
              modifier = Modifier.size(30.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Bottom Action Button: EXACTLY matching screenshot 3:
    // "[ 🗑 oppen trash ]" in dark blue rounded pill
    Button(
      onClick = { viewModel.navigateTo(AppScreen.TRASH) },
      modifier = Modifier
        .fillMaxWidth(0.68f)
        .height(52.dp)
        .testTag("open_trash_button"),
      colors = ButtonDefaults.buttonColors(
        containerColor = OpenTrashBlue
      ),
      shape = RoundedCornerShape(26.dp),
      elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = if (flippedPhotos.isNotEmpty()) "oppen trash (${flippedPhotos.size})" else "oppen trash",
          style = TextStyle(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))
  }
}
