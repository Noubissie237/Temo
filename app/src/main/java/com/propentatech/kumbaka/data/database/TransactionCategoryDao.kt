package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.TransactionCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionCategoryDao {
    @Query("SELECT * FROM transaction_categories")
    fun getAllCategories(): Flow<List<TransactionCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TransactionCategory)
}
