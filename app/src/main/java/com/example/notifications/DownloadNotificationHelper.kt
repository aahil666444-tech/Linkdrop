package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R

object DownloadNotificationHelper {
  const val CHANNEL_ID = "linkdrop_downloads"
  private const val CHANNEL_NAME = "LinkDrop Downloads"

  fun initChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Media download progress and completion notifications"
        setShowBadge(true)
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  fun showProgressNotification(
    context: Context,
    notificationId: Int,
    filename: String,
    progressPercent: Int,
    speed: String,
    downloadedFormatted: String
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        return
      }
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_sys_download)
      .setContentTitle("Downloading: $filename")
      .setContentText("$downloadedFormatted • $speed")
      .setProgress(100, progressPercent.coerceIn(0, 100), progressPercent <= 0)
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)

    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (_: SecurityException) {}
  }

  fun showCompletedNotification(
    context: Context,
    notificationId: Int,
    filename: String,
    fileUri: Uri?,
    mimeType: String
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        return
      }
    }

    val openIntent = if (fileUri != null) {
      Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(fileUri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    } else null

    val pendingIntent = if (openIntent != null) {
      PendingIntent.getActivity(
        context,
        notificationId,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )
    } else null

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_sys_download_done)
      .setContentTitle("Download Complete")
      .setContentText("$filename saved to device")
      .setAutoCancel(true)
      .setOngoing(false)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    if (pendingIntent != null) {
      builder.setContentIntent(pendingIntent)
    }

    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (_: SecurityException) {}
  }

  fun cancelNotification(context: Context, notificationId: Int) {
    try {
      NotificationManagerCompat.from(context).cancel(notificationId)
    } catch (_: Exception) {}
  }
}
