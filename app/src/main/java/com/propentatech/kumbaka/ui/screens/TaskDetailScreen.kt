package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskType
import com.propentatech.kumbaka.data.model.TaskPriority
import com.propentatech.kumbaka.data.model.isCompletedToday
import com.propentatech.kumbaka.ui.theme.PriorityHigh
import com.propentatech.kumbaka.ui.theme.PriorityMedium
import com.propentatech.kumbaka.ui.theme.PriorityLow
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModel
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModelFactory
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(application.taskRepository)
    )
    
    val task by viewModel.getTaskById(taskId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de la tâche") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { task?.let { onEdit(it.id) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        task?.let { currentTask ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Titre et statut
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = currentTask.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (currentTask.isCompletedToday()) 
                                    Icons.Default.CheckCircle 
                                else 
                                    Icons.Default.Clear,
                                contentDescription = null,
                                tint = if (currentTask.isCompletedToday()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (currentTask.isCompletedToday()) "Terminée" else "À faire",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Description
                if (currentTask.description.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = currentTask.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Informations
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Informations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Type
                        InfoRow(
                            label = "Type",
                            value = when (currentTask.type) {
                                TaskType.DAILY -> "Quotidienne"
                                TaskType.PERIODIC -> "Périodique"
                                TaskType.OCCASIONAL -> "Occasionnelle"
                            }
                        )
                        
                        // Priorité
                        InfoRow(
                            label = "Priorité",
                            value = when (currentTask.priority) {
                                TaskPriority.HIGH -> "Haute"
                                TaskPriority.MEDIUM -> "Moyenne"
                                TaskPriority.LOW -> "Basse"
                            },
                            valueColor = when (currentTask.priority) {
                                TaskPriority.HIGH -> PriorityHigh
                                TaskPriority.MEDIUM -> PriorityMedium
                                TaskPriority.LOW -> PriorityLow
                            }
                        )
                        
                        // Date de création
                        InfoRow(
                            label = "Créée le",
                            value = currentTask.createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy à HH:mm", Locale.FRENCH))
                        )
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    
    // Dialog de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la tâche") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette tâche ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        task?.let { currentTask -> viewModel.deleteTask(currentTask.id) }
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
