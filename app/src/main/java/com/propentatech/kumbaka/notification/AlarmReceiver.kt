package com.propentatech.kumbaka.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build

/**
 * Récepteur des alarmes exactes envoyées par ExactAlarmScheduler.
 * Déclenche l'affichage immédiat de la notification.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        val alarmId = intent.getStringExtra("alarm_id")

        if (alarmId != null) {
            // C'est une alarme d'horloge
            val label = intent.getStringExtra("alarm_label") ?: "Alarme MyLive"
            val soundUri = intent.getStringExtra("alarm_sound")
            val vibrate = intent.getBooleanExtra("alarm_vibrate", true)

            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("alarm_id", alarmId)
                putExtra("alarm_label", label)
                putExtra("alarm_sound", soundUri)
                putExtra("alarm_vibrate", vibrate)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            return
        }

        if (eventId != null) {
            // C'est une alarme d'événement
            val title = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Événement"
            val desc = intent.getStringExtra(EXTRA_EVENT_DESC) ?: ""

            Log.d("AlarmReceiver", "Alarme reçue pour : $title")
            
            NotificationHelper.showEventNotification(
                context = context,
                eventId = eventId,
                eventTitle = title,
                eventDescription = desc,
                notificationType = NotificationType.FIVE_MINUTES
            )
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_TITLE = "extra_event_title"
        const val EXTRA_EVENT_DESC = "extra_event_desc"
    }
}
