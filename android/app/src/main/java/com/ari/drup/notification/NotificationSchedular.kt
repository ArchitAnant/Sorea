package com.ari.drup.notification


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.Timestamp
import java.util.Calendar

fun scheduleNotificationAt(context: Context, time: Timestamp) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Extract time of day from given timestamp
    val calNow = Calendar.getInstance()
    val calTarget = Calendar.getInstance().apply {
        timeInMillis = time.toDate().time
        set(Calendar.YEAR, calNow.get(Calendar.YEAR))
        set(Calendar.MONTH, calNow.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, calNow.get(Calendar.DAY_OF_MONTH))
    }

    // If time has already passed today → schedule for tomorrow
    if (calTarget.before(calNow)) {
        calTarget.add(Calendar.DAY_OF_MONTH, 1)
    }

    // Use date as requestCode: YYYYMMDD → unique per day
    val requestCode = calTarget.get(Calendar.YEAR) * 10000 +
            (calTarget.get(Calendar.MONTH) + 1) * 100 +
            calTarget.get(Calendar.DAY_OF_MONTH)

    val intent = Intent(context, NotificationReceiver::class.java)

    // Check if a notification for this day is already scheduled
    val existingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    if (existingIntent != null) {
        Log.d("NotificationPush", "Notification already scheduled for this day — skipping")
        return
    }

    // Otherwise, create a new PendingIntent
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    Log.d("NotificationPush", "Scheduling notification for ${calTarget.time}")


    // Schedule with AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calTarget.timeInMillis,
        pendingIntent
    )
    val prefs = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
    val codes = prefs.getStringSet("scheduled_codes", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    codes.add(requestCode.toString())
    prefs.edit().putStringSet("scheduled_codes", codes).apply()
}
fun cancelAllScheduledNotifications(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val prefs = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
    val codes = prefs.getStringSet("scheduled_codes", emptySet()) ?: emptySet()
    Log.d("NotificationCancel","Size of scheduled list ${codes.size}")

    for (codeStr in codes) {
        val requestCode = codeStr.toInt()
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationPush", "Cancelled notification with requestCode $requestCode")
        }
    }

    // Clear the stored list
    prefs.edit().remove("scheduled_codes").apply()
}

fun cancelAllTestNotifications(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // List all request codes you used for testing
    val testRequestCodes = listOf(1001) // add more if needed

    val intent = Intent(context, NotificationReceiver::class.java)

    testRequestCodes.forEach { code ->
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationPush", "Cancelled scheduled notification with code $code")
        }
    }

    // Optional: clear any saved codes in SharedPreferences if you used them
    val prefs = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
    prefs.edit().remove("scheduled_codes").apply()
}





fun Timestamp.toCalendar(): Calendar {
    return Calendar.getInstance().apply {
        time = this@toCalendar.toDate()
    }
}
