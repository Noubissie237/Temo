package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propentatech.kumbaka.data.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

/**
 * Journal d'humeur quotidien.
 */
@Serializable
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate = LocalDate.now(),
    val mood: UserMood,
    val note: String = ""
)

enum class UserMood {
    HAPPY,
    NEUTRAL,
    SAD,
    ANXIOUS,
    ANGRY,
    EXHAUSTED
}
