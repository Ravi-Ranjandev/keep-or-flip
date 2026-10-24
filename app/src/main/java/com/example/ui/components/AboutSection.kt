package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.KeepGreen
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RighteousFontFamily
import com.example.util.LinkUtils

@Composable
fun AboutSectionCard(
  onOpenDetails: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF101B14)),
    border = BorderStroke(1.dp, Color(0xFF223829)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("about_section")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header with badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = Color(0x3322C55E),
            shape = CircleShape,
            modifier = Modifier.size(38.dp)
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
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "About & Credit",
              style = TextStyle(
                fontFamily = RighteousFontFamily,
                fontSize = 18.sp,
                color = Color.White
              )
            )
            Text(
              text = "App Creator & Links",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF86EFAC)
              )
            )
          }
        }

        Surface(
          color = Color(0xFF1E3524),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.clickable { onOpenDetails() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Details",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Info",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0)
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Creator Name Pill Card
      Surface(
        color = Color(0xFF17291E),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF2B4734)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = KeepGreen,
            shape = CircleShape,
            modifier = Modifier.size(42.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "RR",
                style = TextStyle(
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = Color.Black
                )
              )
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = "Developed by",
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            )
            Text(
              text = LinkUtils.DEVELOPER_NAME,
              style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
              ),
              modifier = Modifier.testTag("about_developer_name")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // GitHub Button
      Button(
        onClick = { LinkUtils.openUrl(context, LinkUtils.GITHUB_URL) },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("github_link_button"),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Code,
            contentDescription = "GitHub",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "GitHub",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "(Ravi-Ranjandev)",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 12.sp,
              color = Color(0xFF94A3B8)
            )
          )
          Spacer(modifier = Modifier.weight(1f))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // LinkedIn Button
      Button(
        onClick = { LinkUtils.openUrl(context, LinkUtils.LINKEDIN_URL) },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("linkedin_link_button"),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A66C2)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "LinkedIn",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "LinkedIn",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "(ravi-ranjan-39a2a8279)",
            style = TextStyle(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 12.sp,
              color = Color(0xCCFFFFFF)
            )
          )
          Spacer(modifier = Modifier.weight(1f))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = Color(0xCCFFFFFF),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
