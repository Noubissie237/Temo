package com.propentatech.kumbaka.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Récepteur des alarmes exactes pour les tâches.
 */
class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tâche"
        val type = intent.getStringExtra(EXTRA_ALARM_TYPE) ?: "REMINDER"

        Log.d("TaskAlarmReceiver", "Alarme reçue pour la tâche : $title ($type)")
        
        val notificationMessage = when(type) {
            "START_MINUS_10" -> "Commence dans 10 minutes : $title"
            "START_MINUS_5" -> "Préparez-vous ! Dans 5 minutes : $title"
            "END_MINUS_5" -> "Se termine dans 5 minutes : $title"
            else -> title
        }

        NotificationHelper.showUniversalNotification(
            context = context,
            title = "Rappel de Tâche",
            message = notificationMessage,
            type = NotificationType.PROJECT // Réutilisation d'une icône appropriée
        )
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
    }
}
