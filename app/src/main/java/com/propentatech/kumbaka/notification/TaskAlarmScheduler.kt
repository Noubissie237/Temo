package com.propentatech.kumbaka.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Planificateur infaillible de notifications pour les tâches utilisant AlarmManager.
 */
class TaskAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTaskAlarms(task: Task) {
        // Annuler les alarmes existantes pour éviter les doublons
        cancelTaskAlarms(task.id)

        // Ne pas planifier si la tâche est terminée/annulée
        if (task.state == TaskState.DONE || task.state == TaskState.MISSED) return

        val today = LocalDate.now()
        
        // Calculer l'heure de début exacte si définie
        if (task.startTime != null) {
            val startDateTime = today.atTime(task.startTime)
            scheduleExact(task, startDateTime.minusMinutes(10), "START_MINUS_10", 1)
            scheduleExact(task, startDateTime.minusMinutes(5), "START_MINUS_5", 2)
        }

        // Calculer l'heure de fin exacte si définie
        if (task.endTime != null) {
            val endDateTime = today.atTime(task.endTime)
            scheduleExact(task, endDateTime.minusMinutes(5), "END_MINUS_5", 3)
        }
    }

    private fun scheduleExact(task: Task, notificationTime: LocalDateTime, alarmType: String, offsetCode: Int) {
        if (notificationTime.isBefore(LocalDateTime.now())) return

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskAlarmReceiver.EXTRA_ALARM_TYPE, alarmType)
        }

        // Request code unique combinant l'ID de tâche et le décalage pour avoir plusieurs alarmes par tâche
        val requestCode = task.id.hashCode() + offsetCode

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            Log.d("TaskAlarmScheduler", "Alarme de tâche ($alarmType) planifiée pour ${task.title} à $notificationTime")
        } catch (e: SecurityException) {
            Log.e("TaskAlarmScheduler", "Erreur de permission d'alarme exacte pour les tâches", e)
        }
    }

    fun cancelTaskAlarms(taskId: String) {
        for (offsetCode in 1..3) {
            val intent = Intent(context, TaskAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.hashCode() + offsetCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
