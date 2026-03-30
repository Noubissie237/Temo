package com.propentatech.kumbaka.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.Transaction
import com.propentatech.kumbaka.data.model.TransactionCategory
import com.propentatech.kumbaka.data.repository.FinanceRepository
import com.propentatech.kumbaka.notification.ActionNotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val context: Context? = null
) : ViewModel() {

    val transactions = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBalance = repository.getTotalBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
            context?.let {
                ActionNotificationHelper.onTransactionCreated(it, transaction.amount, transaction.type.name)
            }
        }
    }

    fun addCategory(category: TransactionCategory) {
        viewModelScope.launch { repository.addCategory(category) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}
