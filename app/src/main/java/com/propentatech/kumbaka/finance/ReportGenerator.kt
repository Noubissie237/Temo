package com.propentatech.kumbaka.finance

import com.propentatech.kumbaka.data.model.Transaction
import com.propentatech.kumbaka.data.model.TransactionType
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class FinancialReport(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val largestExpense: Transaction?,
    val largestIncome: Transaction?,
    val topContact: String?
)

object ReportGenerator {

    enum class ReportPeriod {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    fun generateReport(transactions: List<Transaction>, period: ReportPeriod): FinancialReport {
        val now = LocalDateTime.now()
        
        val filteredTransactions = transactions.filter { t ->
            val daysBetween = ChronoUnit.DAYS.between(t.date, now)
            when (period) {
                ReportPeriod.DAILY -> daysBetween == 0L
                ReportPeriod.WEEKLY -> daysBetween <= 7L
                ReportPeriod.MONTHLY -> daysBetween <= 30L
                ReportPeriod.YEARLY -> daysBetween <= 365L
            }
        }

        val incomes = filteredTransactions.filter { it.type == TransactionType.INCOME }
        val expenses = filteredTransactions.filter { it.type == TransactionType.EXPENSE }

        val totalIncome = incomes.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }

        val largestIncome = incomes.maxByOrNull { it.amount }
        val largestExpense = expenses.maxByOrNull { it.amount }

        // Find contact with most interactions
        val topContact = filteredTransactions
            .filter { it.contactName != null }
            .groupingBy { it.contactName!! }
            .eachCount()
            .maxByOrNull { it.value }?.key

        return FinancialReport(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = totalIncome - totalExpense,
            largestExpense = largestExpense,
            largestIncome = largestIncome,
            topContact = topContact
        )
    }
}
