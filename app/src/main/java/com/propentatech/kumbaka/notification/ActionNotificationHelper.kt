package com.propentatech.kumbaka.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.propentatech.kumbaka.MainActivity
import com.propentatech.kumbaka.R

/**
 * Dispatcher de notifications pour chaque action utilisateur.
 * Chaque création, modification ou suppression importante génère
 * une notification "flash" instantanée pour un feedback immédiat.
 */
object ActionNotificationHelper {

    private const val CHANNEL_ID = "event_reminders" // Réutilise le canal existant

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun buildAndSend(context: Context, emoji: String, title: String, message: String) {
        if (!canNotify(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, (System.currentTimeMillis() % 10000).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("$emoji $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    fun onTaskCreated(context: Context, title: String) =
        buildAndSend(context, "✅", "Tâche créée", "\"$title\" ajoutée à votre liste.")

    fun onTaskCompleted(context: Context, title: String) =
        buildAndSend(context, "🎉", "Tâche accomplie !", "Bravo ! \"$title\" est terminée.")

    fun onEventCreated(context: Context, title: String) =
        buildAndSend(context, "📅", "Événement planifié", "\"$title\" est dans votre agenda.")

    fun onNoteCreated(context: Context, title: String) =
        buildAndSend(context, "📝", "Note enregistrée", "\"$title\" a été sauvegardée.")

    fun onTransactionCreated(context: Context, amount: Double, type: String) {
        val sign = if (type == "INCOME") "+" else "-"
        buildAndSend(context, "💰", "Transaction enregistrée",
            "Montant : $sign${amount.toLong()} FCFA — Solde mis à jour.")
    }

    fun onProjectCreated(context: Context, title: String) =
        buildAndSend(context, "🚀", "Projet lancé", "\"$title\" a démarré. Bonne chance !")

    fun onHabitLogged(context: Context, habitName: String) =
        buildAndSend(context, "⭐", "Habitude validée", "\"$habitName\" cochée aujourd'hui. Continuez !")

    fun onAdvisorAlert(context: Context, mood: String, message: String) {
        val emoji = when (mood) {
            "JOYFUL"  -> "😊"
            "WORRIED" -> "😟"
            "FURIOUS" -> "🔴"
            else      -> "🧠"
        }
        buildAndSend(context, emoji, "Votre Conseiller Expert", message)
    }
}
