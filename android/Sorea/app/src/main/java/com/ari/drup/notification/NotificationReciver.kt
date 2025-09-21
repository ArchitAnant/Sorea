package com.ari.drup.notification


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val message = fetchApiData() // suspend function to get data from API
                    withContext(Dispatchers.Main) {
                        val builder = getNotificationBuilder(context, message)
                        pushNotification(context, builder)
                    }
                } catch (e: Exception) {
                    Log.e("NotificationReceiver", "Failed to fetch API: ${e.message}")
                }
            }
        } else {
            Log.d("NotificationReceiver", "Context is null")
        }
    }

    private suspend fun fetchApiData(): String {
//        val client = HttpClient() // Using Ktor
//        val response: String = client.get("https://your-api-endpoint.com/data")
//        client.close()
//        return response
        return "This is the Text from fetchApiData"
    }
}