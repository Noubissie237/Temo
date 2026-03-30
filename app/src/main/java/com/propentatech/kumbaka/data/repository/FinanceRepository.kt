package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.TransactionCategoryDao
import com.propentatech.kumbaka.data.database.TransactionDao
import com.propentatech.kumbaka.data.model.Transaction
import com.propentatech.kumbaka.data.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: TransactionCategoryDao
) {

    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    fun getAllCategories(): Flow<List<TransactionCategory>> {
        return categoryDao.getAllCategories()
    }

    fun getTotalBalance(): Flow<Double> {
        return transactionDao.getAllTransactions().map { transactions ->
            transactions.sumOf { 
                if (it.type == com.propentatech.kumbaka.data.model.TransactionType.INCOME) it.amount else -it.amount
            }
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun addCategory(category: TransactionCategory) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
}
