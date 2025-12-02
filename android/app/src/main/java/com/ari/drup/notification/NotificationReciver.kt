package com.ari.drup.notification


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ari.drup.BuildConfig
import com.ari.drup.MainActivity
import com.ari.drup.R
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.User
import com.ari.drup.data.mainchat.AzureClient
import com.ari.drup.data.mainchat.NotifApi
import com.ari.drup.ui.Screen
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

        if (context == null) return

        val pendingResult = goAsync() // <-- keeps receiver alive for async work

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = UserCache.cachedUserFlow(context).firstOrNull()
                val email = UserCache.cachedEmailFlow(context).firstOrNull()

                if (user == null || email.isNullOrBlank()) {
                    Log.e("NotificationReceiver", "User/email missing")
                    return@launch
                }

                val message = fetchApiData(email)
                showSystemNotification(
                    context,
                    "${listOf("Hi", "Yo", "Hello", "Hey", "What's up").random()}, ${user.name}",
                    message
                )

                // schedule next
                val lastActive = FirebaseManager().getLastActiveTime(email)

                val timestampToSchedule = lastActive?.toNextDaySameTime() ?: run {
                    // Fallback: random time tomorrow between 8:00 AM - 10:00 PM
                    val calendar = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 1) // schedule for next day
                        set(Calendar.HOUR_OF_DAY, (8..22).random())
                        set(Calendar.MINUTE, (0..59).random())
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    Timestamp(calendar.time)
                }

                scheduleNotificationAt(context, timestampToSchedule)


            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Error: ${e.message}", e)
                showSystemNotification(
                    context,
                    "Notification Error",
                    "Unable to fetch update."
                )
            } finally {
                pendingResult.finish() // <-- ensures broadcast completes safely
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

    private fun showSystemNotification(
        context: Context,
        title: String,
        message: String,
        targetRoute: String = Screen.mainChatScreen.route // default screen
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = CHANNEL_ID

        // Create channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Intent that opens MainActivity and navigates to the specific route
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("targetRoute", targetRoute) // pass the route
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // <-- set pending intent
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
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
