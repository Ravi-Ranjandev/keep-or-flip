package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.model.PhotoItem
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.KeepGreen

@Composable
fun PhotoImageView(
  photo: PhotoItem,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop
) {
  Box(
    modifier = modifier.background(DarkSurfaceCard),
    contentAlignment = Alignment.Center
  ) {
    when {
      photo.contentUri != null || !photo.uriString.isNullOrBlank() -> {
        val modelData = photo.contentUri ?: photo.uriString
        var isLoading by remember(photo.id) { mutableStateOf(true) }
        var isError by remember(photo.id) { mutableStateOf(false) }

        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(modelData)
            .crossfade(true)
            .build(),
          contentDescription = photo.title,
          modifier = Modifier.fillMaxSize(),
          contentScale = contentScale,
          onLoading = {
            isLoading = true
            isError = false
          },
          onSuccess = {
            isLoading = false
            isError = false
          },
          onError = {
            isLoading = false
            isError = true
          }
        )

        if (isLoading) {
          CircularProgressIndicator(
            color = KeepGreen,
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp
          )
        } else if (isError) {
          Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = "Failed to load",
            tint = Color(0xFF64748B),
            modifier = Modifier.size(48.dp)
          )
        }
      }
      photo.drawableRes != null -> {
        Image(
          painter = painterResource(id = photo.drawableRes),
          contentDescription = photo.title,
          modifier = Modifier.fillMaxSize(),
          contentScale = contentScale
        )
      }
      else -> {
        Icon(
          imageVector = Icons.Default.Image,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(48.dp)
        )
      }
    }
  }
}
