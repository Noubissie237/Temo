package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propentatech.kumbaka.data.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

/**
 * Historique des complétions de tâches
 * Permet de tracker quand une tâche a été complétée pour les statistiques
 */
@Serializable
@Entity(tableName = "task_completion_history")
data class TaskCompletionHistory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val taskId: String, // ID de la tâche
    @Serializable(with = LocalDateSerializer::class)
    val completionDate: LocalDate, // Date de complétion
    val taskType: TaskType // Type de tâche (pour faciliter les requêtes)
)
