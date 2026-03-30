package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Jalon (Milestone) lié à un Projet.
 */
@Serializable
@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val displayOrder: Int = 0
)
