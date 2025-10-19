package com.ari.drup.notification


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ari.drup.BuildConfig
import com.ari.drup.R
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.User
import com.ari.drup.data.mainchat.AzureClient
import com.ari.drup.data.mainchat.NotifApi
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotificationReceiver", "onReceive triggered!")

        if (context == null) {
            Log.e("NotificationReceiver", "Context is null — cannot continue")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NotificationReceiver", "Fetching user cache...")

                val user: User? = UserCache.cachedUserFlow(context).firstOrNull()
                val email: String? = UserCache.cachedEmailFlow(context).firstOrNull()

                if (user == null || email.isNullOrBlank()) {
                    Log.e("NotificationReceiver", "User or email not available, skipping notification")
                    return@launch
                }

                Log.d("NotificationReceiver", "Calling API for $email ...")
                val message = fetchApiData(email)

                Log.d("NotificationReceiver", "Message fetched: $message")

                val lastActive = FirebaseManager().getLastActiveTime(email)

                // ✅ Show notification even if the app is closed
                showSystemNotification(
                    context = context,
                    title = user.name ?: "Daily Update",
                    message = message
                )

                // ✅ Schedule next notification (same time, next day)
                lastActive?.let { timestamp ->
                    val nextDayTimestamp = timestamp.toNextDaySameTime()
                    scheduleNotificationAt(context, nextDayTimestamp)
                    Log.d("NotificationReceiver", "Next notification scheduled at $nextDayTimestamp")
                }

            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Error: ${e.message}", e)
                showSystemNotification(
                    context = context,
                    title = "Notification Error",
                    message = "Unable to fetch update. Please check your connection."
                )
            }
        }
    }

    private suspend fun fetchApiData(email: String): String {
        val response = AzureClient().chatApi.getNotification(
            BuildConfig.AZURE_KEY,
            NotifApi(email)
        )
        return response.notification
    }

    private fun showSystemNotification(context: Context, title: String, message: String) {
        val channelId = CHANNEL_ID
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders and messages"
            }
            manager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ensure this icon exists
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Show the notification
        manager.notify(1001, notification)
        Log.d("NotificationReceiver", "System notification displayed.")
    }
}


fun Timestamp.toNextDaySameTime(): Timestamp {
    val cal = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis() // start from "now"
    }

    // Extract time-of-day (hour, minute, second) from the given timestamp
    val srcCal = Calendar.getInstance().apply {
        time = this@toNextDaySameTime.toDate()
    }

    // Apply same hour, minute, second to tomorrow
    cal.add(Calendar.DATE, 1)
    cal.set(Calendar.HOUR_OF_DAY, srcCal.get(Calendar.HOUR_OF_DAY))
    cal.set(Calendar.MINUTE, srcCal.get(Calendar.MINUTE))
    cal.set(Calendar.SECOND, srcCal.get(Calendar.SECOND))
    cal.set(Calendar.MILLISECOND, 0)

    return Timestamp(cal.time)
}
