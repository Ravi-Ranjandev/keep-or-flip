package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object LinkUtils {
  const val DEVELOPER_NAME = "Ravi ranjan"
  const val GITHUB_URL = "https://github.com/Ravi-Ranjandev"
  const val LINKEDIN_URL = "https://www.linkedin.com/in/ravi-ranjan-39a2a8279"
  const val LINKEDIN_DISPLAY_URL = "www.linkedin.com/in/ravi-ranjan-39a2a8279"

  fun openUrl(context: Context, url: String) {
    try {
      val targetUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
        "https://$url"
      } else {
        url
      }
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
