package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propentatech.kumbaka.data.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entité gardant une trace de l'humeur du Conseiller Expert et de ses messages générés.
 */
@Serializable
@Entity(tableName = "advisor_logs")
data class AdvisorLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime = LocalDateTime.now(),
    val advisorMood: AdvisorMood,
    val generatedMessage: String,
    val eventTypeTriggered: String? = null // Ce qui a déclenché l'avis (ex: "NEGATIVE_BALANCE")
)

enum class AdvisorMood {
    JOYFUL,
    WORRIED,
    ANGRY,
    FURIOUS
}
