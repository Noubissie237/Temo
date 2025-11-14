package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propentatech.kumbaka.data.serializers.LocalDateSerializer
import com.propentatech.kumbaka.data.serializers.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Modèle de données pour un événement
 * Représente un événement planifié avec date, heure et tâches associées
 * Entité Room pour la persistance en base de données
 */
@Serializable
@Entity(tableName = "events")
data class Event(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    @Serializable(with = LocalTimeSerializer::class)
    val time: LocalTime? = null,
    val location: String = "",
    val linkedTasks: List<String> = emptyList(), // IDs des tâches liées
    val displayOrder: Int = 0, // Ordre d'affichage pour le drag & drop
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate = LocalDate.now()
)

/**
 * Extension pour calculer le nombre de jours restants avant un événement
 */
fun Event.daysUntil(): Long {
    val today = LocalDate.now()
    return java.time.temporal.ChronoUnit.DAYS.between(today, date)
}

/**
 * Extension pour obtenir le label de compte à rebours
 * Ex: "J-3", "Aujourd'hui", "Passé de 2 jours"
 */
fun Event.getCountdownLabel(): String {
    val days = daysUntil()
    return when {
        days < 0 -> {
            val daysPassed = -days
            if (daysPassed == 1L) "Passé de 1 jour" else "Passé de $daysPassed jours"
        }
        days == 0L -> "Aujourd'hui"
        days == 1L -> "Demain"
        else -> "J-$days"
    }
}
