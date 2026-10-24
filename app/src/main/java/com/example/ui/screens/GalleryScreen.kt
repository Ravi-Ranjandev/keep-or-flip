package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.viewmodel.GalleryFilter
import com.example.viewmodel.KeepFlipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
  viewModel: KeepFlipViewModel,
  onRequestDeletePhoto: (PhotoItem) -> Unit,
  onBackPressed: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackPressed() }

  val context = LocalContext.current
  val allPhotos by viewModel.allPhotos.collectAsState()
  val keptPhotos by viewModel.keptPhotos.collectAsState()
  val unreviewedPhotos by viewModel.unreviewedPhotos.collectAsState()
  val favoritePhotos by viewModel.favoritePhotos.collectAsState()
  val activeFilter by viewModel.galleryFilter.collectAsState()

  var selectedDetailPhoto by remember { mutableStateOf<PhotoItem?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty()) {
      viewModel.importPhotos(uris.map { it.toString() })
    }
  }

  val displayPhotos = when (activeFilter) {
    GalleryFilter.ALL -> allPhotos
    GalleryFilter.KEPT -> keptPhotos
    GalleryFilter.UNREVIEWED -> unreviewedPhotos
    GalleryFilter.FAVORITES -> favoritePhotos
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Bar
      TopAppBar(
        title = {
          Text(
            text = "Galary",
            style = TextStyle(
              fontFamily = RighteousFontFamily,
              fontSize = 22.sp,
              color = Color.White
            )
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBackPressed,
            modifier = Modifier.testTag("gallery_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        },
        actions = {
          IconButton(
            onClick = { viewModel.loadGallery(context) },
            modifier = Modifier.testTag("gallery_refresh_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh Gallery",
              tint = Color(0xFF94A3B8)
            )
          }

          IconButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.testTag("gallery_add_photos_button")
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = "Add Photos",
              tint = KeepGreen
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
      )

      // Filter Chips Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        GalleryFilter.values().forEach { filter ->
          val isSelected = filter == activeFilter
          val count = when (filter) {
            GalleryFilter.ALL -> allPhotos.size
            GalleryFilter.KEPT -> keptPhotos.size
            GalleryFilter.UNREVIEWED -> unreviewedPhotos.size
            GalleryFilter.FAVORITES -> favoritePhotos.size
          }

          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setGalleryFilter(filter) },
            label = {
              Text(
                text = "${filter.label} ($count)",
                fontFamily = OutfitFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = when (filter) {
                GalleryFilter.KEPT -> Color(0xFF14532D)
                GalleryFilter.UNREVIEWED -> Color(0xFF1E293B)
                GalleryFilter.FAVORITES -> Color(0xFF581C87)
                else -> Color(0xFF0F2B1D)
              },
              selectedLabelColor = Color.White,
              containerColor = Color(0xFF16251C),
              labelColor = Color(0xFF94A3B8)
            ),
            border = null,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("filter_chip_${filter.name}")
          )
        }
      }

      // Photos Grid
      if (displayPhotos.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PhotoLibrary,
              contentDescription = null,
              tint = Color(0xFF64748B),
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No photos in ${activeFilter.label}",
              style = TextStyle(
                fontFamily = RighteousFontFamily,
                fontSize = 18.sp,
                color = Color.White
              )
            )
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          modifier = Modifier
            .fillMaxSize()
            .testTag("gallery_grid"),
          contentPadding = PaddingValues(8.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(displayPhotos, key = { it.id }) { photo ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
              modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable { selectedDetailPhoto = photo }
                .testTag("gallery_item_${photo.id}")
            ) {
              Box(modifier = Modifier.fillMaxSize()) {
                PhotoImageView(
                  photo = photo,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )

                // Status Indicator
                when (photo.status) {
                  PhotoStatus.KEPT -> {
                    Surface(
                      color = KeepGreen.copy(alpha = 0.9f),
                      shape = CircleShape,
                      modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(14.dp)
                        )
                      }
                    }
                  }
                  else -> {}
                }

                if (photo.isFavorite) {
                  Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .padding(6.dp)
                      .size(16.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Floating button to import custom photos
    FloatingActionButton(
      onClick = {
        photoPickerLauncher.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
      },
      containerColor = KeepGreen,
      contentColor = Color.White,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("fab_import_photos")
    ) {
      Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Add photos")
    }
  }

  // Photo Detail Dialog
  selectedDetailPhoto?.let { photo ->
    val livePhoto = allPhotos.find { it.id == photo.id } ?: photo
    Dialog(onDismissRequest = { selectedDetailPhoto = null }) {
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
              photo = livePhoto,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )

            IconButton(
              onClick = { selectedDetailPhoto = null },
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color(0x88000000), CircleShape)
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
          }

          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = livePhoto.title,
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Size: ${livePhoto.formattedSize} • ${livePhoto.resolution}",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
              )
            )
            Text(
              text = "Date: ${livePhoto.dateString}",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
              )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = { viewModel.toggleFavorite(livePhoto.id) }
              ) {
                Icon(
                  imageVector = if (livePhoto.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Favorite",
                  tint = if (livePhoto.isFavorite) Color(0xFFF43F5E) else Color.White
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Delete button (triggers real Android MediaStore deletion)
                Surface(
                  color = FlipRed.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                      selectedDetailPhoto = null
                      onRequestDeletePhoto(livePhoto)
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = FlipRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = FlipRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                  }
                }

                // Keep button
                Surface(
                  color = KeepGreen.copy(alpha = if (livePhoto.status == PhotoStatus.KEPT) 1f else 0.2f),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.keepPhoto(livePhoto.id) }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Keep", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
