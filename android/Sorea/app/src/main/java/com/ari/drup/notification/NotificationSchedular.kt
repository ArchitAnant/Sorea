package com.ari.drup.notification


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

fun scheduleDailyNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
//        set(Calendar.HOUR_OF_DAY, 4)
        add(Calendar.MINUTE, 2)
//        set(Calendar.SECOND, 0)

        // If it's already past 5 PM, schedule for tomorrow
        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

//    alarmManager.setRepeating(
//        AlarmManager.RTC_WAKEUP,
//        calendar.timeInMillis,
//        AlarmManager.INTERVAL_DAY,
//        pendingIntent
//    )
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        2 * 60 * 1000L, // 2 minutes in milliseconds
        pendingIntent
    )
}