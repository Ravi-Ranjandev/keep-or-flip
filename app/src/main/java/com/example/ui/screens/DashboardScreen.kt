package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AboutSectionCard
import com.example.ui.components.PhotoImageView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.FavoriteCardBg
import com.example.ui.theme.KeepGreen
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RighteousFontFamily
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GalleryFilter
import com.example.viewmodel.KeepFlipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: KeepFlipViewModel,
  onRequestPermissions: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allPhotos by viewModel.allPhotos.collectAsState()
  val unreviewedPhotos by viewModel.unreviewedPhotos.collectAsState()
  val keptPhotos by viewModel.keptPhotos.collectAsState()
  val flippedPhotos by viewModel.flippedPhotos.collectAsState()
  val favoritePhotos by viewModel.favoritePhotos.collectAsState()
  val totalCleanedBytes by viewModel.totalCleanedBytes.collectAsState()
  val pendingReclaimBytes by viewModel.pendingReclaimBytes.collectAsState()
  val hasPermission by viewModel.hasPermission.collectAsState()
  var showAboutDialog by remember { mutableStateOf(false) }

  // Photo Picker launcher as alternative / selective photo picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty()) {
      viewModel.importPhotos(uris.map { it.toString() })
    }
  }

  val heroPhoto = unreviewedPhotos.firstOrNull() ?: allPhotos.firstOrNull()
  val recentFlipPhoto = flippedPhotos.lastOrNull() ?: heroPhoto
  val recentKeepPhoto = keptPhotos.lastOrNull() ?: allPhotos.firstOrNull()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    // Top Bar
    TopAppBar(
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Keep or Flip",
            style = TextStyle(
              fontFamily = RighteousFontFamily,
              fontSize = 22.sp,
              color = Color.White
            )
          )
        }
      },
      actions = {
        // Refresh real gallery from MediaStore
        IconButton(
          onClick = { viewModel.loadGallery(context) },
          modifier = Modifier.testTag("refresh_gallery_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh Gallery",
            tint = Color(0xFF94A3B8)
          )
        }

        // Import photos
        IconButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          modifier = Modifier.testTag("import_photos_button")
        ) {
          Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = "Import Photos",
            tint = KeepGreen
          )
        }

        // About / Creator Credits (Ravi ranjan)
        IconButton(
          onClick = { showAboutDialog = true },
          modifier = Modifier.testTag("about_button")
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "About Developer",
            tint = Color.White
          )
        }

        // Reset session
        IconButton(
          onClick = { viewModel.resetSession(context) },
          modifier = Modifier.testTag("reset_demo_button")
        ) {
          Icon(
            imageVector = Icons.Default.RestartAlt,
            contentDescription = "Reset Deck",
            tint = Color(0xFF94A3B8)
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
    )

    if (showAboutDialog) {
      com.example.ui.components.AboutDialog(
        onDismissRequest = { showAboutDialog = false }
      )
    }

    // Permission Banner if access is not granted
    if (!hasPermission) {
      Surface(
        color = Color(0xFF271C0F),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp)
          .clickable { onRequestPermissions() }
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Gallery Access Needed",
                style = TextStyle(
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color.White
                )
              )
              Text(
                text = "Tap to grant permission and load your phone photos",
                style = TextStyle(
                  fontFamily = OutfitFontFamily,
                  fontSize = 12.sp,
                  color = Color(0xFFCBD5E1)
                )
              )
            }
          }

          Button(
            onClick = onRequestPermissions,
            colors = ButtonDefaults.buttonColors(containerColor = KeepGreen),
            shape = RoundedCornerShape(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text("Grant", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Storage Summary Banner
    Surface(
      color = Color(0xFF132A1C),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            tint = KeepGreen,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (allPhotos.isNotEmpty()) {
                "${allPhotos.size} Real Photos Scanned"
              } else {
                "Gallery Ready"
              },
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
              )
            )
            Text(
              text = "Total Storage Freed: ${KeepFlipViewModel.formatBytes(totalCleanedBytes)}",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF86EFAC)
              )
            )
          }
        }

        Surface(
          color = Color(0xFF165EA8),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .clickable { viewModel.navigateTo(AppScreen.SWIPE_DECK) }
            .testTag("banner_swipe_now_button")
        ) {
          Text(
            text = "Review",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color.White
            )
          )
        }
      }
    }

    // Scrollable Cards Section matching screenshot 2
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      // Card 1: Top Hero Card ("keep flip")
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.navigateTo(AppScreen.SWIPE_DECK) }
          .testTag("hero_keep_flip_card")
      ) {
        Card(
          shape = RoundedCornerShape(26.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            if (heroPhoto != null) {
              PhotoImageView(
                photo = heroPhoto,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            } else {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color(0xFF17281D)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (!hasPermission) "Grant permission to view gallery" else "No photos found on device",
                  color = Color(0xFF94A3B8),
                  fontFamily = OutfitFontFamily,
                  fontSize = 14.sp
                )
              }
            }

            // Dual tone overlay matching screenshot 2
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.horizontalGradient(
                    0.0f to Color(0x66EF4444),
                    0.45f to Color(0x11000000),
                    0.55f to Color(0x11000000),
                    1.0f to Color(0x6622C55E)
                  )
                )
            )

            // Swipe prompt badge
            Surface(
              color = Color(0xBB000000),
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Swipe,
                  contentDescription = null,
                  tint = KeepGreen,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${unreviewedPhotos.size} to review",
                  style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.White
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "keep flip",
          style = TextStyle(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = Color(0xFFE2E8F0)
          ),
          textAlign = TextAlign.Center
        )
      }

      // Card Row 2: "Recent flip" (Left) and "Recent keep" (Right)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Left: "Recent flip" (Storage Cleaned)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .weight(1f)
            .clickable { viewModel.navigateTo(AppScreen.TRASH) }
            .testTag("recent_flip_card")
        ) {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1.2f)
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              if (recentFlipPhoto != null) {
                PhotoImageView(
                  photo = recentFlipPhoto,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
              // Red tint overlay matching screenshot
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color(0x88DC2626))
              )

              // Badge showing flipped count or freed size
              Surface(
                color = Color(0xBB000000),
                shape = CircleShape,
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
              ) {
                Text(
                  text = if (flippedPhotos.isNotEmpty()) "${flippedPhotos.size}" else KeepFlipViewModel.formatBytes(totalCleanedBytes),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Recent flip",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 16.sp,
              color = Color(0xFFE2E8F0)
            ),
            textAlign = TextAlign.Center
          )
        }

        // Right: "Recent keep"
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .weight(1f)
            .clickable {
              viewModel.setGalleryFilter(GalleryFilter.KEPT)
              viewModel.navigateTo(AppScreen.GALLERY)
            }
            .testTag("recent_keep_card")
        ) {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1.2f)
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              if (recentKeepPhoto != null) {
                PhotoImageView(
                  photo = recentKeepPhoto,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
              // Green tint overlay matching screenshot
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color(0x7716A34A))
              )

              // Badge
              Surface(
                color = Color(0xBB000000),
                shape = CircleShape,
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
              ) {
                Text(
                  text = "${keptPhotos.size}",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Recent keep",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 16.sp,
              color = Color(0xFFE2E8F0)
            ),
            textAlign = TextAlign.Center
          )
        }
      }

      // Card Row 3: "Galary" (Left) and "favorite" (Right)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Left: "Galary" (matching screenshot title)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .weight(1f)
            .clickable {
              viewModel.setGalleryFilter(GalleryFilter.ALL)
              viewModel.navigateTo(AppScreen.GALLERY)
            }
            .testTag("gallery_card")
        ) {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1.2f)
          ) {
            // Mini 2x2 grid preview of real device photos
            Column(modifier = Modifier.fillMaxSize()) {
              Row(modifier = Modifier.weight(1f)) {
                allPhotos.getOrNull(0)?.let { p ->
                  PhotoImageView(photo = p, modifier = Modifier.weight(1f))
                }
                allPhotos.getOrNull(1)?.let { p ->
                  PhotoImageView(photo = p, modifier = Modifier.weight(1f))
                }
              }
              Row(modifier = Modifier.weight(1f)) {
                allPhotos.getOrNull(2)?.let { p ->
                  PhotoImageView(photo = p, modifier = Modifier.weight(1f))
                }
                allPhotos.getOrNull(3)?.let { p ->
                  PhotoImageView(photo = p, modifier = Modifier.weight(1f))
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Galary",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 16.sp,
              color = Color(0xFFE2E8F0)
            ),
            textAlign = TextAlign.Center
          )
        }

        // Right: "favorite"
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .weight(1f)
            .clickable {
              viewModel.setGalleryFilter(GalleryFilter.FAVORITES)
              viewModel.navigateTo(AppScreen.GALLERY)
            }
            .testTag("favorite_card")
        ) {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = FavoriteCardBg),
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1.2f)
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              val favPhoto = favoritePhotos.firstOrNull()
              if (favPhoto != null) {
                PhotoImageView(
                  photo = favPhoto,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88450A48))
                )
              }

              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFE879F9),
                modifier = Modifier.size(36.dp)
              )

              Surface(
                color = Color(0xBB000000),
                shape = CircleShape,
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
              ) {
                Text(
                  text = "${favoritePhotos.size}",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "favorite",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 16.sp,
              color = Color(0xFFE2E8F0)
            ),
            textAlign = TextAlign.Center
          )
        }
      }

      // About Section / Developer Credit
      Spacer(modifier = Modifier.height(24.dp))
      AboutSectionCard(
        onOpenDetails = { showAboutDialog = true }
      )

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
