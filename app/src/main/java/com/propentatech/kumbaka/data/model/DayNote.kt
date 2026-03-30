package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Entité représentant une note spéciale ou une couleur associée à un jour spécifique du calendrier
 */
@Entity(tableName = "day_notes")
data class DayNote(
    @PrimaryKey
    val date: LocalDate, // Une seule note par jour
    val content: String,
    val colorHex: String?, // Couleur optionnelle pour le jour
    val isImportant: Boolean = false
)
