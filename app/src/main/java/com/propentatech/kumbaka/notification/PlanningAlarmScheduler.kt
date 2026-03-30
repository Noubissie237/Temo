package com.propentatech.kumbaka.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.propentatech.kumbaka.data.model.PlanningSession
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class PlanningAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarms(session: PlanningSession) {
        val date = session.date
        val startTime = session.startTime ?: return // Pas d'heure → pas d'alarme

        session.reminderMinutesBefore.forEachIndexed { idx, minutesBefore ->
            val triggerTime = LocalDateTime.of(date, startTime).minusMinutes(minutesBefore.toLong())
            val triggerMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (triggerMillis <= System.currentTimeMillis()) return@forEachIndexed

            val requestCode = (session.id.hashCode() + idx * 1000) and Int.MAX_VALUE

            val intent = Intent(context, PlanningAlarmReceiver::class.java).apply {
                putExtra("SESSION_ID", session.id)
                putExtra("SESSION_TITLE", session.title)
                putExtra("MINUTES_BEFORE", minutesBefore)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            scheduleExact(triggerMillis, pendingIntent)
        }
    }

    fun cancelAlarms(sessionId: String) {
        for (idx in 0..9) {
            val requestCode = (sessionId.hashCode() + idx * 1000) and Int.MAX_VALUE
            val intent = Intent(context, PlanningAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    private fun scheduleExact(triggerMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }
}
