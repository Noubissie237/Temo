package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
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
                title = { 
                    Text(
                        text = "Détails de la tâche",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(top = 0.dp)
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // En-tête avec titre et statut
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Statut badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (currentTask.isCompletedToday())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (currentTask.isCompletedToday()) 
                                    Icons.Default.CheckCircle 
                                else 
                                    Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (currentTask.isCompletedToday())
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (currentTask.isCompletedToday()) "Terminée" else "À faire",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTask.isCompletedToday())
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Titre
                        Text(
                            text = currentTask.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Description
                if (currentTask.description.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Description",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = currentTask.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                            )
                        }
                    }
                }
                
                // Informations détaillées
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Informations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Type de tâche
                        DetailInfoRow(
                            icon = when (currentTask.type) {
                                TaskType.DAILY -> Icons.Default.Today
                                TaskType.PERIODIC -> Icons.Default.CalendarMonth
                                TaskType.OCCASIONAL -> Icons.Default.Event
                            },
                            label = "Type de tâche",
                            value = when (currentTask.type) {
                                TaskType.DAILY -> "Quotidienne"
                                TaskType.PERIODIC -> "Périodique"
                                TaskType.OCCASIONAL -> "Occasionnelle"
                            }
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        
                        // Priorité
                        DetailInfoRow(
                            icon = Icons.Default.Flag,
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
                        
                        // Date spécifique pour tâches occasionnelles
                        if (currentTask.type == TaskType.OCCASIONAL && currentTask.specificDate != null) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            DetailInfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Date prévue",
                                value = currentTask.specificDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH))
                            )
                        }
                        
                        // Jours sélectionnés pour tâches périodiques
                        if (currentTask.type == TaskType.PERIODIC && currentTask.selectedDays.isNotEmpty()) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            DetailInfoRow(
                                icon = Icons.Default.DateRange,
                                label = "Jours de la semaine",
                                value = currentTask.selectedDays.joinToString(", ") { day ->
                                    when (day) {
                                        DayOfWeek.MONDAY -> "Lundi"
                                        DayOfWeek.TUESDAY -> "Mardi"
                                        DayOfWeek.WEDNESDAY -> "Mercredi"
                                        DayOfWeek.THURSDAY -> "Jeudi"
                                        DayOfWeek.FRIDAY -> "Vendredi"
                                        DayOfWeek.SATURDAY -> "Samedi"
                                        DayOfWeek.SUNDAY -> "Dimanche"
                                    }
                                }
                            )
                        }
                        
                        // Dernière complétion
                        if (currentTask.lastCompletedDate != null) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            DetailInfoRow(
                                icon = Icons.Default.CheckCircle,
                                label = "Dernière complétion",
                                value = currentTask.lastCompletedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH))
                            )
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        
                        // Date de création
                        DetailInfoRow(
                            icon = Icons.Default.Schedule,
                            label = "Créée le",
                            value = currentTask.createdAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy à HH:mm", Locale.FRENCH))
                        )
                        
                        // Date de modification
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        DetailInfoRow(
                            icon = Icons.Default.Update,
                            label = "Modifiée le",
                            value = currentTask.updatedAt?.format(DateTimeFormatter.ofPattern("dd MMMM yyyy à HH:mm", Locale.FRENCH)) ?: "Jamais"
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

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}
