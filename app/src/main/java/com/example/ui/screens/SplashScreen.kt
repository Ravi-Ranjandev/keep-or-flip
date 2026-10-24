package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RighteousFontFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onStartClicked: () -> Unit
) {
  val pulseAnim = remember { Animatable(1f) }

  LaunchedEffect(Unit) {
    pulseAnim.animateTo(
      targetValue = 1.05f,
      animationSpec = infiniteRepeatable(
        animation = tween(1200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      )
    )
  }

  // Vertical gradient matching the screenshot's salmon/coral to olive green
  val splashGradient = Brush.verticalGradient(
    0.0f to Color(0xFFDC5E52),
    0.45f to Color(0xFFB5704E),
    0.75f to Color(0xFF6B7A42),
    1.0f to Color(0xFF3E5F36)
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(splashGradient)
      .clickable { onStartClicked() }
      .testTag("splash_screen_container"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {

      Spacer(modifier = Modifier.weight(1f))

      // Stylized stacked title matching screenshot 1
      Column(
        modifier = Modifier.scale(pulseAnim.value),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-8).dp)
      ) {
        // "keep" in green with heavy black outline/drop shadow
        StylizedText(
          text = "keep",
          textColor = Color(0xFF62D368),
          outlineColor = Color(0xFF142416),
          fontSize = 72.sp
        )

        // "or" in warm gold with outline/drop shadow
        StylizedText(
          text = "or",
          textColor = Color(0xFFE5A024),
          outlineColor = Color(0xFF332008),
          fontSize = 58.sp
        )

        // "flip" in flame red with outline/drop shadow
        StylizedText(
          text = "flip",
          textColor = Color(0xFFF04337),
          outlineColor = Color(0xFF330E0A),
          fontSize = 78.sp
        )
      }

      Spacer(modifier = Modifier.height(36.dp))

      // Feature highlights
      Surface(
        color = Color(0x33000000),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = Color(0xFF86EFAC),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Swipe Right to Keep",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.White
              )
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = null,
              tint = Color(0xFFFCA5A5),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Swipe Left to Delete & Save Storage",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.White
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Bottom Start button
      Button(
        onClick = onStartClicked,
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("start_cleaning_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF142018)
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            tint = Color(0xFF4ADE80),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Start Cleaning",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = Color.White
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
fun StylizedText(
  text: String,
  textColor: Color,
  outlineColor: Color,
  fontSize: androidx.compose.ui.unit.TextUnit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    // Deep black offset shadow
    Text(
      text = text,
      style = TextStyle(
        fontFamily = RighteousFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        color = outlineColor,
        shadow = Shadow(
          color = Color(0xCC000000),
          offset = androidx.compose.ui.geometry.Offset(6f, 8f),
          blurRadius = 6f
        ),
        textAlign = TextAlign.Center
      )
    )

    // Foreground bold colored letters
    Text(
      text = text,
      style = TextStyle(
        fontFamily = RighteousFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        color = textColor,
        shadow = Shadow(
          color = outlineColor,
          offset = androidx.compose.ui.geometry.Offset(2f, 3f),
          blurRadius = 2f
        ),
        textAlign = TextAlign.Center
      )
    )
  }
}
