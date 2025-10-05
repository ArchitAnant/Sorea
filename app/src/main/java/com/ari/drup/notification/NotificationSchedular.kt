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
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = time.toCalendar().apply {
        // Move to tomorrow at same hour/minute/second
        add(Calendar.DATE, 1)
    }

    Log.d("NotificationPush", "Scheduling notification for ${calendar.time}")

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}


fun Timestamp.toCalendar(): Calendar {
    return Calendar.getInstance().apply {
        time = this@toCalendar.toDate()
    }
}
