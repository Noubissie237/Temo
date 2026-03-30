package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.propentatech.kumbaka.data.model.TransactionType
import com.propentatech.kumbaka.finance.ReportGenerator
import com.propentatech.kumbaka.ui.components.PieChart
import com.propentatech.kumbaka.ui.components.PieSlice
import com.propentatech.kumbaka.ui.components.TrendLineChart
import com.propentatech.kumbaka.ui.viewmodel.FinanceViewModel

// Orange palette constants
private val Orange = Color(0xFFFF6B00)
private val HighOrange = Color(0xFFFF8C42)
private val DarkOrange = Color(0xFFE85D00)
private val SoftOrange = Color(0xFFFF6B00).copy(alpha = 0.2f)
private val IncomeColor = Color(0xFFFF6B00)
private val ExpenseColor = Color(0xFF1F1F1F) // Black for expenses to maintain contrast in Orange/Black theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedPeriod by remember { mutableStateOf(ReportGenerator.ReportPeriod.MONTHLY) }

    val report = remember(transactions, selectedPeriod) {
        ReportGenerator.generateReport(transactions, selectedPeriod)
    }

    // Build pie slices per source (contact or description)
    val incomePieSlices = remember(transactions, selectedPeriod) {
        val palette = listOf(
            Color(0xFFFF6B00), // Primary Orange
            Color(0xFFE85D00), // Darker Orange
            Color(0xFFFF8C42), // Lighter Orange
            Color(0xFFFFB380), // Very Light Orange
            Color(0xFFB34B00)  // Deep Brownish Orange
        )
        transactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.contactName ?: it.source ?: it.description.ifEmpty { "Autre" } }
            .entries.mapIndexed { i, (label, txs) ->
                PieSlice(label.take(14), txs.sumOf { it.amount }.toFloat(), palette[i % palette.size])
            }
    }

    val expensePieSlices = remember(transactions, selectedPeriod) {
        val palette = listOf(
            Color(0xFF262626), // Near Black
            Color(0xFF404040), // Dark Gray
            Color(0xFF595959), // Medium Gray
            Color(0xFF737373), // Light Gray
            Color(0xFF8C8C8C)  // Lighter Gray
        )
        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.contactName ?: it.source ?: it.description.ifEmpty { "Autre" } }
            .entries.mapIndexed { i, (label, txs) ->
                PieSlice(label.take(14), txs.sumOf { it.amount }.toFloat(), palette[i % palette.size])
            }
    }

    // Build 30-day trend data
    val trendPoints = remember(transactions) {
        val sorted = transactions.sortedBy { it.date }
        if (sorted.size < 2) listOf(0f, 0f)
        else {
            var running = 0f
            sorted.map { t ->
                running += if (t.type == TransactionType.INCOME) t.amount.toFloat() else -t.amount.toFloat()
                running
            }.takeLast(30)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rapports & Statistiques", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Period selector
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ReportGenerator.ReportPeriod.values().forEachIndexed { index, period ->
                        SegmentedButton(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Orange,
                                activeContentColor = Color.White
                            )
                        ) {
                            Text(when(period) {
                                ReportGenerator.ReportPeriod.DAILY   -> "Jour"
                                ReportGenerator.ReportPeriod.WEEKLY  -> "Semaine"
                                ReportGenerator.ReportPeriod.MONTHLY -> "Mois"
                                ReportGenerator.ReportPeriod.YEARLY  -> "Année"
                            }, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Big bilan card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (report.netBalance >= 0) Color(0xFF1A1A1A) else Color(0xFF0D0D0D))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Assessment, contentDescription = null,
                            tint = if (report.netBalance >= 0) Orange else Color.White,
                            modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Bilan Net", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                        Text(
                            text = "${if (report.netBalance >= 0) "+" else ""}${report.netBalance.toLong()} FCFA",
                            color = if (report.netBalance >= 0) IncomeColor else ExpenseColor,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Revenus / Dépenses
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoBox("Revenus", "+${report.totalIncome.toLong()} FCFA", IncomeColor, Icons.Default.TrendingUp, Modifier.weight(1f))
                    InfoBox("Dépenses", "-${report.totalExpense.toLong()} FCFA", ExpenseColor, Icons.Default.TrendingDown, Modifier.weight(1f))
                }
            }

            // Courbe tendance
            item {
                TrendLineChart(
                    points = trendPoints,
                    label = "Évolution du solde",
                    lineColor = Orange
                )
            }

            // Pie chart revenus
            if (incomePieSlices.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(16.dp)
                    ) {
                        Text("Revenus par source", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(16.dp))
                        PieChart(
                            slices = incomePieSlices,
                            centerLabel = "+${report.totalIncome.toLong()} FCFA",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Pie chart dépenses
            if (expensePieSlices.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(16.dp)
                    ) {
                        Text("Dépenses par source", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(16.dp))
                        PieChart(
                            slices = expensePieSlices,
                            centerLabel = "-${report.totalExpense.toLong()} FCFA",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Appréciation / Conseils
            item {
                val appreciation = when {
                    report.netBalance > 0 && report.totalExpense < report.totalIncome * 0.5 ->
                        "🏆 Excellent ! Vous épargnez plus de 50% de vos revenus."
                    report.netBalance > 0 ->
                        "👍 Bien. Vos finances sont équilibrées. Continuez sur cette lancée."
                    report.netBalance == 0.0 ->
                        "⚖️ À l'équilibre. Essayez de constituer une épargne."
                    else ->
                        "⚠️ Attention ! Vos dépenses dépassent vos revenus. Réduisez les sorties non essentielles."
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Orange.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(appreciation, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp)
                }
            }
        }
    }
}

@Composable
private fun InfoBox(
    title: String, value: String, color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
