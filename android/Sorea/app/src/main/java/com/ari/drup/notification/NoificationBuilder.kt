package com.ari.drup.notification


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context


const val CHANNEL_ID = "channel_id"
const val NOTIFICATION_ID = 0

fun createNotificationChannel(context: Context) {
    val name = "channel_name"
    val descriptionText = "No description"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
    }
    // Register the channel with the system.
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    notificationManager.createNotificationChannel(channel)
}