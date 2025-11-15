package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskPriority
import com.propentatech.kumbaka.data.model.TaskType
import com.propentatech.kumbaka.data.model.getLabel
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModel
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

/**
 * Écran de création/édition d'une tâche
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    taskId: String?,
    onNavigateBack: () -> Unit
) {
    // Récupérer le repository depuis l'Application
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(application.taskRepository)
    )
    // État local
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TaskType.OCCASIONAL) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedDays by remember { mutableStateOf<List<DayOfWeek>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Stocker la tâche existante pour conserver les dates
    var existingTask by remember { mutableStateOf<Task?>(null) }

    // Charger la tâche si on est en mode édition
    LaunchedEffect(taskId) {
        taskId?.let { id ->
            viewModel.getTaskById(id).collect { task ->
                task?.let {
                    existingTask = it
                    title = it.title
                    description = it.description
                    selectedType = it.type
                    selectedPriority = it.priority
                    selectedDate = it.specificDate
                    selectedDays = it.selectedDays
                }
            }
        }
    }

    val isEditMode = taskId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Modifier la tâche" else "Nouvelle tâche",
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
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(top = 0.dp)
            )
        }
    ) { paddingValues ->
        // Validation du formulaire
        val isFormValid = title.isNotBlank() && when (selectedType) {
            TaskType.DAILY -> true
            TaskType.PERIODIC -> selectedDays.isNotEmpty()
            TaskType.OCCASIONAL -> selectedDate != null
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Champ titre
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre de la tâche") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Champ description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optionnelle)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Sélection du type de tâche
            Text(
                text = "Type de tâche",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskTypeChip(
                    type = TaskType.DAILY,
                    isSelected = selectedType == TaskType.DAILY,
                    onClick = { selectedType = TaskType.DAILY },
                    modifier = Modifier.weight(1f)
                )
                TaskTypeChip(
                    type = TaskType.PERIODIC,
                    isSelected = selectedType == TaskType.PERIODIC,
                    onClick = { selectedType = TaskType.PERIODIC },
                    modifier = Modifier.weight(1f)
                )
                TaskTypeChip(
                    type = TaskType.OCCASIONAL,
                    isSelected = selectedType == TaskType.OCCASIONAL,
                    onClick = { selectedType = TaskType.OCCASIONAL },
                    modifier = Modifier.weight(1f)
                )
            }

            // Sélecteur de jours (pour tâches périodiques)
            if (selectedType == TaskType.PERIODIC) {
                Text(
                    text = "Jours de la semaine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                DayOfWeekSelector(
                    selectedDays = selectedDays,
                    onDaysChanged = { selectedDays = it }
                )
            }

            // Sélecteur de date (pour tâches occasionnelles)
            if (selectedType == TaskType.OCCASIONAL) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                DateQuickSelector(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }

            // Sélection de la priorité
            Text(
                text = "Priorité",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriorityChip(
                    priority = TaskPriority.HIGH,
                    isSelected = selectedPriority == TaskPriority.HIGH,
                    onClick = { selectedPriority = TaskPriority.HIGH },
                    modifier = Modifier.weight(1f)
                )
                PriorityChip(
                    priority = TaskPriority.MEDIUM,
                    isSelected = selectedPriority == TaskPriority.MEDIUM,
                    onClick = { selectedPriority = TaskPriority.MEDIUM },
                    modifier = Modifier.weight(1f)
                )
                PriorityChip(
                    priority = TaskPriority.LOW,
                    isSelected = selectedPriority == TaskPriority.LOW,
                    onClick = { selectedPriority = TaskPriority.LOW },
                    modifier = Modifier.weight(1f)
                )
            }
        }
            
            // Bouton Sauvegarder en bas
            Button(
                onClick = {
                    if (isFormValid) {
                        val task = if (isEditMode && existingTask != null) {
                            // Mode édition : conserver createdAt, mettre à jour updatedAt
                            existingTask!!.copy(
                                title = title,
                                description = description,
                                type = selectedType,
                                specificDate = if (selectedType == TaskType.OCCASIONAL) selectedDate else null,
                                selectedDays = if (selectedType == TaskType.PERIODIC) selectedDays else emptyList(),
                                priority = selectedPriority,
                                updatedAt = LocalDateTime.now()
                            )
                        } else {
                            // Mode création : nouvelle tâche avec createdAt, updatedAt = null
                            Task(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                type = selectedType,
                                specificDate = if (selectedType == TaskType.OCCASIONAL) selectedDate else null,
                                selectedDays = if (selectedType == TaskType.PERIODIC) selectedDays else emptyList(),
                                priority = selectedPriority,
                                isCompleted = false,
                                createdAt = LocalDateTime.now(),
                                updatedAt = null
                            )
                        }
                        
                        if (isEditMode) {
                            viewModel.updateTask(task)
                        } else {
                            viewModel.addTask(task)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Sauvegarder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialogue de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la tâche ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskId?.let { viewModel.deleteTask(it) }
                        showDeleteDialog = false
                        onNavigateBack()
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

/**
 * Chip de sélection de type de tâche
 */
@Composable
fun TaskTypeChip(
    type: TaskType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (isSelected) 
        Color.White 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.getLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * Sélecteur de jours de la semaine
 */
@Composable
fun DayOfWeekSelector(
    selectedDays: List<DayOfWeek>,
    onDaysChanged: (List<DayOfWeek>) -> Unit
) {
    val daysOfWeek = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        daysOfWeek.forEach { day ->
            val isSelected = day in selectedDays
            
            Surface(
                onClick = {
                    onDaysChanged(
                        if (isSelected) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.FRENCH).take(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * Sélecteur rapide de date
 */
@Composable
fun DateQuickSelector(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bouton Aujourd'hui
        Surface(
            onClick = { onDateSelected(today) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = if (selectedDate == today) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aujourd'hui",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedDate == today) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selectedDate == today) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("dd MMM")),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedDate == today) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bouton Demain
        Surface(
            onClick = { onDateSelected(tomorrow) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = if (selectedDate == tomorrow) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Demain",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedDate == tomorrow) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selectedDate == tomorrow) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = tomorrow.format(DateTimeFormatter.ofPattern("dd MMM")),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedDate == tomorrow) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Chip de sélection de priorité
 */
@Composable
fun PriorityChip(
    priority: TaskPriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (priority) {
        TaskPriority.HIGH -> if (isSelected) PriorityHigh else PriorityHigh.copy(alpha = 0.2f)
        TaskPriority.MEDIUM -> if (isSelected) PriorityMedium else PriorityMedium.copy(alpha = 0.2f)
        TaskPriority.LOW -> if (isSelected) PriorityLow else PriorityLow.copy(alpha = 0.2f)
    }

    val textColor = if (isSelected) Color.White else when (priority) {
        TaskPriority.HIGH -> PriorityHigh
        TaskPriority.MEDIUM -> PriorityMedium
        TaskPriority.LOW -> PriorityLow
    }

    val label = when (priority) {
        TaskPriority.HIGH -> "Haute"
        TaskPriority.MEDIUM -> "Moyenne"
        TaskPriority.LOW -> "Basse"
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
