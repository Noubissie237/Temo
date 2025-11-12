package com.propentatech.kumbaka.data.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Modèle de données pour une note
 * Supporte différents types de contenu : texte, checklist, image, lien
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String = "",
    val type: NoteType = NoteType.TEXT,
    val imageUrl: String? = null,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val links: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Type de note
 */
enum class NoteType {
    TEXT,       // Note texte simple
    CHECKLIST,  // Note avec checklist
    IMAGE,      // Note avec image
    LINK        // Note avec lien
}

/**
 * Item d'une checklist dans une note
 */
data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false
)
