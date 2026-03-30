package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.propentatech.kumbaka.data.model.Transaction
import com.propentatech.kumbaka.data.model.TransactionType
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar
import com.propentatech.kumbaka.ui.utils.ContactsHelper
import com.propentatech.kumbaka.ui.viewmodel.FinanceViewModel
import java.time.format.DateTimeFormatter

private val Orange = Color(0xFFFF6B00)
private val OrangeDark = Color(0xFFE85D00)
private val IncomeColor = Color(0xFFFF6B00) // Orange for income
private val ExpenseColor = Color(0xFF1A1A1A) // Black/Dark for expense to match theme constraints

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val balance by viewModel.totalBalance.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { transaction ->
                viewModel.addTransaction(transaction)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = { Text("Finance", fontWeight = FontWeight.Black, color = Color.White, style = MaterialTheme.typography.titleLarge) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Orange,
                contentColor = Color.White,
                modifier = Modifier,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ajouter", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Premium gradient bank card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFF6B00), Color(0xFFFF3D00), Color(0xFFE85D00))))
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("Solde Total", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
                        Column {
                            Text(
                                text = "${balance.toLong()} FCFA",
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (balance >= 0) "Solde positif ✓" else "⚠️ Solde négatif",
                                color = if (balance >= 0) Color.White.copy(alpha = 0.8f) else Color(0xFFFFCA28),
                                fontSize = 13.sp
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val inc = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                            val exp = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                            MiniStat("↑ Revenus", "+${inc.toLong()} FCFA")
                            MiniStat("↓ Dépenses", "-${exp.toLong()} FCFA")
                        }
                    }
                }
            }

            item {
                Text("Activité Récente", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
            }

            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center) {
                        Text("Aucune transaction.\nAppuyez sur + pour commencer.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                    }
                }
            } else {
                items(transactions.sortedByDescending { it.date }) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == TransactionType.INCOME
    val textColor = if (isIncome) Orange else Color.White
    val boxColor = if (isIncome) Orange.copy(alpha = 0.1f) else Color.Black
    val iconColor = if (isIncome) Orange else Color.White
    val icon = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    val formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(boxColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.description.ifEmpty { "Transaction" },
                fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (transaction.contactName != null) {
                Text("👤 ${transaction.contactName}", color = Orange, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            } else if (transaction.source != null) {
                Text("📌 ${transaction.source}", color = Orange, fontSize = 12.sp)
            }
            if (transaction.note != null) {
                Text("💬 ${transaction.note}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            Text(transaction.date.format(formatter),
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }

        Text(
            text = "${if (isIncome) "+" else "-"}${transaction.amount.toLong()} FCFA",
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            fontSize = 16.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showContactDropdown by remember { mutableStateOf(false) }

    val contactsList = remember {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) ContactsHelper.getContactsList(context) else emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Nouvelle Transaction", fontWeight = FontWeight.Bold, color = Orange)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type Selector (Tabs style for better visibility)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .padding(4.dp)
                ) {
                    val modifier = Modifier.weight(1f).height(40.dp)
                    Button(
                        onClick = { isIncome = true },
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) Orange else Color.Transparent,
                            contentColor = if (isIncome) Color.White else Orange
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("Entrée (+)", fontWeight = FontWeight.Bold) }
                    
                    Button(
                        onClick = { isIncome = false },
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIncome) Color.Black else Color.Transparent,
                            contentColor = if (!isIncome) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("Sortie (-)", fontWeight = FontWeight.Bold) }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Montant (FCFA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Source / Contact Selection (Ultra-Visible Section)
                Text("D'où vient cet argent ? / Où va-t-il ?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Orange)
                
                if (contactsList.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Sélectionner un Contact :", fontSize = 11.sp, color = Color.Gray)
                        Box {
                            OutlinedButton(
                                onClick = { showContactDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = if (selectedContact != null) androidx.compose.foundation.BorderStroke(2.dp, Orange) else null
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Orange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(selectedContact?.second ?: "Cliquer pour choisir un contact", color = if (selectedContact != null) Orange else Color.Unspecified)
                            }
                            DropdownMenu(
                                expanded = showContactDropdown,
                                onDismissRequest = { showContactDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 250.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("❌ Aucun contact", fontWeight = FontWeight.Bold) },
                                    onClick = { selectedContact = null; showContactDropdown = false }
                                )
                                contactsList.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.second) },
                                        onClick = { selectedContact = c; showContactDropdown = false }
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedContact == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ou catégories rapides :", fontSize = 11.sp, color = Color.Gray)
                        val chips = if (isIncome) listOf("Salaire", "Cadeau", "Vente", "Autre")
                        else listOf("Courses", "Loyer", "Loisirs", "Santé", "Abo")
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            chips.forEach { chip ->
                                SuggestionChip(
                                    onClick = { source = chip },
                                    label = { Text(chip, fontSize = 10.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (source == chip) Orange else Color.Transparent,
                                        labelColor = if (source == chip) Color.White else Orange
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = Orange, borderWidth = 1.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = source,
                            onValueChange = { source = it },
                            label = { Text("Ou source libre (ex: Netflix…)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Appréciation (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (parsedAmount > 0) {
                        onSave(Transaction(
                            amount = parsedAmount,
                            type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                            description = description,
                            contactUri = selectedContact?.first,
                            contactName = selectedContact?.second,
                            source = source.ifEmpty { null },
                            note = note.ifEmpty { null }
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) { Text("Enregistrer", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
