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
 * Types de planning supportés
 */
enum class PlanningType(val label: String, val emoji: String) {
    DAILY("Journée", "📅"),
    WEEKLY("Semaine", "📆"),
    STUDY("Étude", "📚"),
    REVISION("Révision", "🔄")
}

/**
 * Récurrence d'une séance de planning
 */
enum class PlanningRecurrence(val label: String) {
    NONE("Aucune"),
    DAILY("Quotidienne"),
    WEEKLY("Hebdomadaire")
}

/**
 * Entité Room représentant une séance de planning
 */
@Serializable
@Entity(tableName = "planning_sessions")
data class PlanningSession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,
    val description: String = "",
    val type: PlanningType = PlanningType.DAILY,

    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,

    @Serializable(with = LocalTimeSerializer::class)
    val startTime: LocalTime? = null,

    @Serializable(with = LocalTimeSerializer::class)
    val endTime: LocalTime? = null,

    /** Couleur hex ex: "#FF6B00" */
    val color: String = "#FF6B00",

    /** Si true → étoile ⭐ sur le calendrier */
    val isImportant: Boolean = false,

    /** Liste de minutes avant le début pour déclencher une alarme ex: [10, 30, 60] */
    val reminderMinutesBefore: List<Int> = emptyList(),

    val recurrence: PlanningRecurrence = PlanningRecurrence.NONE,
    val isCompleted: Boolean = false,

    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate = LocalDate.now()
)
