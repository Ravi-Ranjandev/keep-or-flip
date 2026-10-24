package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.KeepGreen
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RighteousFontFamily
import com.example.util.LinkUtils

@Composable
fun AboutDialog(
  onDismissRequest: () -> Unit
) {
  val context = LocalContext.current

  fun openUrl(url: String) {
    LinkUtils.openUrl(context, url)
  }

  Dialog(onDismissRequest = onDismissRequest) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF111E16)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF233A2C)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .testTag("about_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top row with close button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = Color(0x3322C55E),
              shape = CircleShape,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = KeepGreen,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "About",
              style = TextStyle(
                fontFamily = RighteousFontFamily,
                fontSize = 20.sp,
                color = Color.White
              )
            )
          }

          IconButton(
            onClick = onDismissRequest,
            modifier = Modifier
              .size(32.dp)
              .testTag("about_close_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF94A3B8)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // App Branding
        Text(
          text = "Keep or Flip",
          style = TextStyle(
            fontFamily = RighteousFontFamily,
            fontSize = 26.sp,
            color = Color.White
          )
        )
        Text(
          text = "Photo Gallery Cleaner v1.0",
          style = TextStyle(
            fontFamily = OutfitFontFamily,
            fontSize = 13.sp,
            color = Color(0xFF86EFAC)
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "A smart Android photo cleanup app designed to help you quickly review your phone photos and reclaim storage.",
          style = TextStyle(
            fontFamily = OutfitFontFamily,
            fontSize = 14.sp,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
          ),
          modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Developer Credit Card
        Surface(
          color = Color(0xFF16271D),
          shape = RoundedCornerShape(18.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF284432)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Developed by",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
              )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = LinkUtils.DEVELOPER_NAME,
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
              ),
              modifier = Modifier.testTag("about_developer_name")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // GitHub link button
            Button(
              onClick = { openUrl(LinkUtils.GITHUB_URL) },
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("github_link_button"),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Code,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "GitHub / Ravi-Ranjandev",
                  style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                  )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                  contentDescription = null,
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LinkedIn link button
            Button(
              onClick = { openUrl(LinkUtils.LINKEDIN_URL) },
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("linkedin_link_button"),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A66C2)),
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "LinkedIn / Ravi Ranjan",
                  style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                  )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                  contentDescription = null,
                  tint = Color(0xCCFFFFFF),
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dismiss button
        OutlinedButton(
          onClick = onDismissRequest,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(23.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
          Text(
            text = "Close",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
