package com.propentatech.kumbaka.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
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
 * Helper pour gérer les notifications de l'application
 * Gère la création des canaux et l'affichage des notifications
 */
object NotificationHelper {
    
    private const val CHANNEL_ID_EVENTS = "event_reminders"
    private const val CHANNEL_NAME_EVENTS = "Rappels d'événements"
    private const val CHANNEL_DESCRIPTION_EVENTS = "Notifications pour les événements à venir"
    
    /**
     * Crée les canaux de notification nécessaires
     * Doit être appelé au démarrage de l'application
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_EVENTS,
                CHANNEL_NAME_EVENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION_EVENTS
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Affiche une notification de rappel pour un événement
     * @param context Contexte de l'application
     * @param eventId ID de l'événement
     * @param eventTitle Titre de l'événement
     * @param eventDescription Description de l'événement
     * @param notificationType Type de notification (DAY_BEFORE ou FIVE_MINUTES)
     */
    fun showEventNotification(
        context: Context,
        eventId: String,
        eventTitle: String,
        eventDescription: String,
        notificationType: NotificationType
    ) {
        // Vérifier la permission pour Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        // Intent pour ouvrir l'application sur l'événement
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("event_id", eventId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construire la notification
        val notificationTitle = when (notificationType) {
            NotificationType.DAY_BEFORE -> "📅 Événement demain"
            NotificationType.FIVE_MINUTES -> "⏰ Événement dans 5 minutes"
        }
        
        val notificationText = when (notificationType) {
            NotificationType.DAY_BEFORE -> "Demain : $eventTitle"
            NotificationType.FIVE_MINUTES -> eventTitle
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EVENTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(if (eventDescription.isNotEmpty()) eventDescription else notificationText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        // Afficher la notification
        val notificationManager = NotificationManagerCompat.from(context)
        val notificationId = generateNotificationId(eventId, notificationType)
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * Vérifie si les notifications sont activées
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
    
    /**
     * Génère un ID unique pour chaque notification
     * Permet d'avoir des notifications séparées pour J-1 et 5min
     */
    private fun generateNotificationId(eventId: String, type: NotificationType): Int {
        val typeCode = when (type) {
            NotificationType.DAY_BEFORE -> 1
            NotificationType.FIVE_MINUTES -> 2
        }
        return (eventId.hashCode() + typeCode * 1000000)
    }
    
    /**
     * Annule toutes les notifications pour un événement
     */
    fun cancelEventNotifications(context: Context, eventId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        NotificationType.values().forEach { type ->
            val notificationId = generateNotificationId(eventId, type)
            notificationManager.cancel(notificationId)
        }
    }
}

/**
 * Types de notifications pour les événements
 */
enum class NotificationType {
    DAY_BEFORE,      // Notification 1 jour avant
    FIVE_MINUTES     // Notification 5 minutes avant
}
