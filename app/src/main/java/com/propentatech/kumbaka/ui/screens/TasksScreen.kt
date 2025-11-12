package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskPriority
import com.propentatech.kumbaka.data.model.TaskType
import com.propentatech.kumbaka.data.model.shouldShowToday
import com.propentatech.kumbaka.data.model.isCompletedToday
import com.propentatech.kumbaka.ui.components.EmptyStateMessage
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModel
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Écran de gestion des tâches
 * Affiche les tâches groupées par date (Aujourd'hui, Demain, Terminées)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onTaskClick: (String) -> Unit = {},
    onCreateTask: () -> Unit = {}
) {
    // Récupérer le repository depuis l'Application
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(application.taskRepository)
    )
    
    // Vérifier et réinitialiser les tâches au chargement de l'écran
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            application.taskResetManager.checkAndResetTasks()
        }
    }
    
    // Observer les tâches depuis le ViewModel
    val allTasks by viewModel.tasks.collectAsState()
    
    // Filtrer les tâches par catégorie en utilisant la nouvelle logique
    val todayTasks = allTasks.filter { task ->
        task.shouldShowToday() && !task.isCompletedToday()
    }
    val tomorrowTasks = allTasks.filter { task ->
        when (task.type) {
            TaskType.OCCASIONAL -> task.specificDate == LocalDate.now().plusDays(1) && !task.isCompleted
            TaskType.PERIODIC -> {
                val tomorrowDayOfWeek = LocalDate.now().plusDays(1).dayOfWeek
                tomorrowDayOfWeek in task.selectedDays
            }
            TaskType.DAILY -> false // Les tâches quotidiennes sont toujours dans "Aujourd'hui"
        }
    }
    val completedTasks = allTasks.filter { task ->
        task.isCompletedToday()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mes Tâches",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Filtres */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(top = 0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTask,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter une tâche",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Message si aucune tâche
            if (todayTasks.isEmpty() && tomorrowTasks.isEmpty() && completedTasks.isEmpty()) {
                item {
                    EmptyStateMessage(
                        message = "Aucune tâche",
                        subtitle = "Créez votre première tâche pour commencer",
                        icon = Icons.Outlined.CheckCircle,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            }
            
            // Section Aujourd'hui
            if (todayTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Aujourd'hui",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(todayTasks) { task ->
                    TaskItem(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task.id) },
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }

            // Section Demain
            if (tomorrowTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Demain",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(tomorrowTasks) { task ->
                    TaskItem(
                        task = task,
                        onToggleComplete = { /* Ne rien faire - pas de complétion pour demain */ },
                        onClick = { onTaskClick(task.id) },
                        canToggle = false // Désactiver le toggle pour les tâches de demain
                    )
                }
            }

            // Section Terminées (toujours visible)
            if (completedTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Terminées",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(completedTasks) { task ->
                    TaskItem(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task.id) },
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
        }
    }
}

/**
 * Item de tâche individuel
 */
@Composable
fun TaskItem(
    task: Task,
    onToggleComplete: () -> Unit = {},
    onClick: () -> Unit = {},
    canToggle: Boolean = true // Par défaut, on peut cocher/décocher
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Vérifier si la tâche est complétée (en dehors du Row pour être accessible partout)
        val isTaskCompleted = task.isCompletedToday()
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox (masquée pour les tâches de demain)
            if (canToggle) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isTaskCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .background(
                            if (isTaskCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                Color.Transparent
                        )
                        .clickable(onClick = onToggleComplete),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTaskCompleted) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
            }

            // Contenu de la tâche
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Titre
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isTaskCompleted) 
                        TextDecoration.LineThrough 
                    else 
                        TextDecoration.None,
                    color = if (isTaskCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                
                // Description (si elle existe)
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
                
                // Type de tâche et date sur la même ligne
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge du type
                    val (typeLabel, typeColor) = when (task.type) {
                        TaskType.DAILY -> "Quotidienne" to PriorityLow
                        TaskType.PERIODIC -> "Périodique" to PriorityMedium
                        TaskType.OCCASIONAL -> "Occasionnelle" to PriorityHigh
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Date pour les tâches occasionnelles
                    if (task.type == TaskType.OCCASIONAL && task.specificDate != null) {
                        Text(
                            text = "• ${task.specificDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Indicateur de priorité
            if (!isTaskCompleted) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(getPriorityColor(task.priority))
                )
            }
        }
    }
}

/**
 * Retourne la couleur correspondant à la priorité
 */
@Composable
fun getPriorityColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.HIGH -> PriorityHigh
        TaskPriority.MEDIUM -> PriorityMedium
        TaskPriority.LOW -> PriorityLow
    }
}
