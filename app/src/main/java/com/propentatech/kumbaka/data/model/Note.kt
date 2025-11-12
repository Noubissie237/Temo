package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.propentatech.kumbaka.data.database.Converters
import java.time.LocalDateTime
import java.util.UUID

/**
 * Modèle de données pour une note
 * Une note contient du texte et peut avoir des liens de référence
 */
@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String = "",
    val links: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
