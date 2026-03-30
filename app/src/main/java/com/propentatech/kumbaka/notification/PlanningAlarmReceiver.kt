package com.propentatech.kumbaka.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.propentatech.kumbaka.R

class PlanningAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra("SESSION_ID") ?: return
        val title = intent.getStringExtra("SESSION_TITLE") ?: "Séance de planning"
        val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 10)

        val channelId = "PLANNING_CHANNEL"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Planning MyLive",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rappels de séances planning"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val bodyText = when (minutesBefore) {
            0 -> "🚀 Ta séance commence maintenant !"
            60 -> "⏰ $title commence dans 1 heure"
            1440 -> "📅 $title est demain — prépare-toi !"
            else -> "⏰ $title commence dans $minutesBefore minutes"
        }

        NotificationHelper.showUniversalNotification(
            context = context,
            title = "Planning MyLive — $title",
            message = bodyText,
            type = NotificationType.PLANNING
        )
    }
}
