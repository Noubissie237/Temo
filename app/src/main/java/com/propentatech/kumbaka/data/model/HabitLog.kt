package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propentatech.kumbaka.data.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

/**
 * Log quotidien d'une habitude.
 */
@Serializable
@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val status: HabitStatus
)

enum class HabitStatus {
    COMPLETED,
    SKIPPED,
    FAILED
}
