package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskPriority
import com.propentatech.kumbaka.data.model.TaskType
import com.propentatech.kumbaka.data.model.isCompletedToday
import com.propentatech.kumbaka.ui.components.EmptyStateMessage
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModel
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModelFactory
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Écran affichant toutes les tâches créées, groupées par type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(
    onNavigateBack: () -> Unit = {},
    onTaskClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(application.taskRepository)
    )
    
    val allTasks by viewModel.tasks.collectAsState()
    
    // Grouper les tâches par type
    val dailyTasks = allTasks.filter { it.type == TaskType.DAILY }
    val periodicTasks = allTasks.filter { it.type == TaskType.PERIODIC }
    val occasionalTasks = allTasks.filter { it.type == TaskType.OCCASIONAL }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Toutes mes tâches",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(top = 0.dp)
            )
        }
    ) { paddingValues ->
        if (allTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateMessage(
                    message = "Aucune tâche créée",
                    subtitle = "Créez votre première tâche pour commencer",
                    icon = Icons.Default.TaskAlt
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Statistiques en haut
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(
                                label = "Total",
                                value = allTasks.size.toString(),
                                icon = Icons.Default.TaskAlt
                            )
                            StatItem(
                                label = "Quotidiennes",
                                value = dailyTasks.size.toString(),
                                icon = Icons.Default.CalendarMonth
                            )
                            StatItem(
                                label = "Périodiques",
                                value = periodicTasks.size.toString(),
                                icon = Icons.Default.DateRange
                            )
                            StatItem(
                                label = "Occasionnelles",
                                value = occasionalTasks.size.toString(),
                                icon = Icons.Default.Event
                            )
                        }
                    }
                }
                
                // Tâches quotidiennes
                if (dailyTasks.isNotEmpty()) {
                    item {
                        TaskGroupHeader(
                            title = "Tâches quotidiennes",
                            count = dailyTasks.size,
                            icon = Icons.Default.CalendarMonth
                        )
                    }
                    items(dailyTasks.sortedBy { it.displayOrder }) { task ->
                        AllTaskItem(
                            task = task,
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
                
                // Tâches périodiques
                if (periodicTasks.isNotEmpty()) {
                    item {
                        TaskGroupHeader(
                            title = "Tâches périodiques",
                            count = periodicTasks.size,
                            icon = Icons.Default.DateRange
                        )
                    }
                    items(periodicTasks.sortedBy { it.displayOrder }) { task ->
                        AllTaskItem(
                            task = task,
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
                
                // Tâches occasionnelles
                if (occasionalTasks.isNotEmpty()) {
                    item {
                        TaskGroupHeader(
                            title = "Tâches occasionnelles",
                            count = occasionalTasks.size,
                            icon = Icons.Default.Event
                        )
                    }
                    items(occasionalTasks.sortedBy { it.specificDate }) { task ->
                        AllTaskItem(
                            task = task,
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun TaskGroupHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AllTaskItem(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Indicateur de priorité
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when (task.priority) {
                            TaskPriority.HIGH -> PriorityHigh
                            TaskPriority.MEDIUM -> PriorityMedium
                            TaskPriority.LOW -> PriorityLow
                        }
                    )
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Informations supplémentaires selon le type
                when (task.type) {
                    TaskType.DAILY -> {
                        Text(
                            text = "Tous les jours",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TaskType.PERIODIC -> {
                        val daysText = task.selectedDays.joinToString(", ") { day ->
                            when (day) {
                                java.time.DayOfWeek.MONDAY -> "Lun"
                                java.time.DayOfWeek.TUESDAY -> "Mar"
                                java.time.DayOfWeek.WEDNESDAY -> "Mer"
                                java.time.DayOfWeek.THURSDAY -> "Jeu"
                                java.time.DayOfWeek.FRIDAY -> "Ven"
                                java.time.DayOfWeek.SATURDAY -> "Sam"
                                java.time.DayOfWeek.SUNDAY -> "Dim"
                            }
                        }
                        Text(
                            text = daysText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TaskType.OCCASIONAL -> {
                        task.specificDate?.let { date ->
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Statut de complétion
            if (task.isCompletedToday()) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complétée",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
