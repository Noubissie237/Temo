package com.propentatech.kumbaka.advisor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.propentatech.kumbaka.MainActivity
import com.propentatech.kumbaka.R
import com.propentatech.kumbaka.data.model.AdvisorMood

/**
 * Gère l'envoi des communications du Conseiller Expert avec un ton ferme.
 */
object AdvisorDispatcher {

    private const val CHANNEL_ID = "advisor_channel"
    private const val CHANNEL_NAME = "Le Conseiller Expert"
    private const val CHANNEL_DESCRIPTION = "Communications strictes de votre assistant personnel"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun dispatchDailyReview(context: Context, mood: AdvisorMood, message: String) {
        val title = when (mood) {
            AdvisorMood.JOYFUL -> "👍 Excellent Travail"
            AdvisorMood.WORRIED -> "⚠️ Attention"
            AdvisorMood.ANGRY -> "🛑 Reprenez-vous en main"
            AdvisorMood.FURIOUS -> "🔥 Tolérance Zéro !"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(3000, notification)
        } catch (e: SecurityException) {
            // Permission manquante (Android 13+)
        }
    }
}
