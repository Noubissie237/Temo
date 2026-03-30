package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propentatech.kumbaka.data.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Modèle de données pour une Habitude.
 */
@Serializable
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val colorHex: String = "#3498db",
    val iconName: String = "bolt",
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)

enum class HabitFrequency {
    DAILY,
    WEEKLY
}
