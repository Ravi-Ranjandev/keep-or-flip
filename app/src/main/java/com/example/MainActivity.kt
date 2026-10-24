package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.PhotoItem
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SwipeDeckScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.KeepOrFlipTheme
import com.example.ui.theme.OutfitFontFamily
import com.example.util.PermissionUtils
import com.example.viewmodel.AppScreen
import com.example.viewmodel.KeepFlipViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      KeepOrFlipTheme {
        val context = LocalContext.current
        val viewModel: KeepFlipViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()
        val statusMessage by viewModel.statusMessage.collectAsState()

        // Single Android System Deletion Consent Dialog Launcher for Batch Requests
        val deleteIntentLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
          if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onBatchDeletionConfirmed(context)
          } else {
            viewModel.onBatchDeletionCancelled()
          }
        }

        // Permission Request Launcher
        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
          val granted = permissions.values.any { it }
          viewModel.updatePermissionState(granted, context)
        }

        LaunchedEffect(Unit) {
          viewModel.initGallery(context)
        }

        // Auto-dismiss status notification after 3 seconds
        LaunchedEffect(statusMessage) {
          if (statusMessage != null) {
            delay(3000)
            viewModel.clearStatusMessage()
          }
        }

        fun executeDelete(photos: List<PhotoItem>) {
          viewModel.triggerManualBatchDelete(
            context = context,
            photos = photos,
            onConsentNeeded = { intentSender ->
              deleteIntentLauncher.launch(
                IntentSenderRequest.Builder(intentSender).build()
              )
            }
          )
        }

        Scaffold(
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(DarkBackground)
              .safeDrawingPadding()
          ) {
            Crossfade(
              targetState = currentScreen,
              label = "screen_transition"
            ) { screen ->
              when (screen) {
                AppScreen.SPLASH -> {
                  SplashScreen(
                    onStartClicked = {
                      if (!PermissionUtils.hasGalleryPermission(context)) {
                        permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                      }
                      viewModel.navigateTo(AppScreen.DASHBOARD)
                    }
                  )
                }
                AppScreen.DASHBOARD -> {
                  DashboardScreen(
                    viewModel = viewModel,
                    onRequestPermissions = {
                      permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                    }
                  )
                }
                AppScreen.SWIPE_DECK -> {
                  SwipeDeckScreen(
                    viewModel = viewModel,
                    onRequestPermissions = {
                      permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                    },
                    onBackPressed = {
                      viewModel.navigateTo(AppScreen.DASHBOARD)
                    }
                  )
                }
                AppScreen.TRASH -> {
                  TrashScreen(
                    viewModel = viewModel,
                    onTriggerDeleteSelected = { photos ->
                      executeDelete(photos)
                    },
                    onBackPressed = {
                      viewModel.navigateTo(AppScreen.DASHBOARD)
                    }
                  )
                }
                AppScreen.GALLERY -> {
                  GalleryScreen(
                    viewModel = viewModel,
                    onRequestDeletePhoto = { photo ->
                      executeDelete(listOf(photo))
                    },
                    onBackPressed = {
                      viewModel.navigateTo(AppScreen.DASHBOARD)
                    }
                  )
                }
              }
            }

            // In-app Status Notification Toast Pill
            AnimatedVisibility(
              visible = statusMessage != null,
              enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
              exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
              modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            ) {
              Surface(
                color = Color(0xEE1E293B),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 6.dp
              ) {
                Text(
                  text = statusMessage ?: "",
                  modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                  style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}
