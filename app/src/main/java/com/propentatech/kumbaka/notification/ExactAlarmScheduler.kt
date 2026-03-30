package com.propentatech.kumbaka.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.propentatech.kumbaka.data.model.Event
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Planificateur infaillible de notifications utilisant AlarmManager.
 * Utilisé pour s'assurer que les rappels (ex: 5 minutes avant) sonnent à la minute près.
 */
class ExactAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExactAlarm(event: Event, minutesBefore: Long) {
        if (event.time == null) return

        val notificationTime = event.date.atTime(event.time).minusMinutes(minutesBefore)
        if (notificationTime.isBefore(LocalDateTime.now())) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(AlarmReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(AlarmReceiver.EXTRA_EVENT_DESC, event.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback si la permission n'est pas accordée (on utilise la méthode standard)
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d("ExactAlarm", "Alarme planifiée pour événement ${event.title} à $notificationTime")
        } catch (e: SecurityException) {
            Log.e("ExactAlarm", "Erreur de permission d'alarme exacte", e)
        }
    }

    fun cancelAlarm(eventId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
