package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Catégorie pour les transactions financières.
 */
@Serializable
@Entity(tableName = "transaction_categories")
data class TransactionCategory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconName: String = "category",
    val colorHex: String = "#000000",
    val type: TransactionType // INCOME, EXPENSE ou BOTH
)
