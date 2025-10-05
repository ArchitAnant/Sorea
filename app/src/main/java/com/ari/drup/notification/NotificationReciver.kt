package com.ari.drup.notification


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.d("NotificationReceiver", "Context is null")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NotificationReceiver", "Fetching API data...")

                val user: User? = UserCache.cachedUserFlow(context).firstOrNull()
                val email: String? = UserCache.cachedEmailFlow(context).firstOrNull()

                if (user == null || email.isNullOrBlank()) {
                    Log.e("NotificationReceiver", "User or email not available, skipping notification")
                    return@launch
                }

                val message = fetchApiData() // suspend function to get data from API
                val lastActive = FirebaseManager().getLastActiveTime(email)

                withContext(Dispatchers.Main) {
                    val builder = getNotificationBuilder(
                        context,
                        user.name ?: "",  // avoid NPE if name is null
                        message
                    )
                    pushNotification(context, builder)
                }

                lastActive?.let {
                    scheduleNotificationAt(context, it)
                }

            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Failed: ${e.message}", e)
            }
        }
    }


    private suspend fun fetchApiData(): String {
//        val client = HttpClient() // Using Ktor
//        val response: String = client.get("https://your-api-endpoint.com/data")
//        client.close()
//        return response
        return "How are you doing? How about some chat?"
    }
}