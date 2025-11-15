package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propentatech.kumbaka.data.serializers.DayOfWeekListSerializer
import com.propentatech.kumbaka.data.serializers.LocalDateSerializer
import com.propentatech.kumbaka.data.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Modèle de données pour une tâche
 * Entité Room pour la persistance en base de données
 */
@Serializable
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val type: TaskType = TaskType.OCCASIONAL,
    @Serializable(with = LocalDateSerializer::class)
    val specificDate: LocalDate? = null, // Pour les tâches occasionnelles
    @Serializable(with = DayOfWeekListSerializer::class)
    val selectedDays: List<DayOfWeek> = emptyList(), // Pour les tâches périodiques
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    @Serializable(with = LocalDateSerializer::class)
    val lastCompletedDate: LocalDate? = null, // Pour savoir quand elle a été complétée
    val displayOrder: Int = 0, // Ordre d'affichage pour le drag & drop
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime? = null
)

/**
 * Types de tâches
 */
enum class TaskType {
    DAILY,       // Quotidienne - se répète chaque jour
    PERIODIC,    // Périodique - se répète certains jours de la semaine
    OCCASIONAL   // Occasionnelle - une seule fois
}

/**
 * Niveaux de priorité d'une tâche
 */
enum class TaskPriority {
    LOW,     // Priorité basse (vert)
    MEDIUM,  // Priorité moyenne (orange)
    HIGH     // Priorité haute (rouge)
}

/**
 * Extension pour vérifier si une tâche doit être affichée aujourd'hui
 */
fun Task.shouldShowToday(): Boolean {
    val today = LocalDate.now()
    
    return when (type) {
        TaskType.DAILY -> {
            // Tâche quotidienne : toujours afficher sauf si complétée aujourd'hui
            lastCompletedDate != today
        }
        TaskType.PERIODIC -> {
            // Tâche périodique : afficher si aujourd'hui est dans les jours sélectionnés
            // et pas complétée aujourd'hui
            val todayDayOfWeek = today.dayOfWeek
            todayDayOfWeek in selectedDays && lastCompletedDate != today
        }
        TaskType.OCCASIONAL -> {
            // Tâche occasionnelle : afficher si la date correspond et pas complétée
            specificDate == today && !isCompleted
        }
    }
}

/**
 * Extension pour vérifier si une tâche est complétée aujourd'hui
 */
fun Task.isCompletedToday(): Boolean {
    val today = LocalDate.now()
    return when (type) {
        TaskType.DAILY, TaskType.PERIODIC -> lastCompletedDate == today
        TaskType.OCCASIONAL -> isCompleted
    }
}

/**
 * Extension pour obtenir le label du type de tâche
 */
fun TaskType.getLabel(): String {
    return when (this) {
        TaskType.DAILY -> "Quotidienne"
        TaskType.PERIODIC -> "Périodique"
        TaskType.OCCASIONAL -> "Occasionnelle"
    }
}
