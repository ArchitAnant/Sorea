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

    val intent = Intent(context, NotificationReceiver::class.java)
    val requestCode = 1001

    val existingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    if (existingIntent != null) {
        Log.d("NotificationPush", "Notification already scheduled — skipping")
        return
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

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

    Log.d("NotificationPush", "Scheduling notification for ${calTarget.time}")

    // Schedule with AlarmManager (works when app is killed)
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calTarget.timeInMillis,
        pendingIntent
    )
}




fun Timestamp.toCalendar(): Calendar {
    return Calendar.getInstance().apply {
        time = this@toCalendar.toDate()
    }
}
