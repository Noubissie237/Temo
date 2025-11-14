package com.propentatech.kumbaka.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskType
import com.propentatech.kumbaka.data.model.isCompletedToday
import com.propentatech.kumbaka.data.model.shouldShowToday
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModel
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Écran de statistiques des tâches
 * Affiche des graphiques et métriques sur les tâches complétées/non complétées
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Récupérer le ViewModel
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(application.taskRepository)
    )
    
    // Observer toutes les tâches
    val allTasks by viewModel.tasks.collectAsState()
    
    // Calculer les statistiques
    val today = LocalDate.now()
    
    // Tâches applicables aujourd'hui (qu'elles soient complétées ou non)
    val tasksToday = allTasks.filter { task ->
        // Vérifier que la tâche existait déjà aujourd'hui
        val taskExistsToday = task.createdAt.toLocalDate() <= today
        
        if (!taskExistsToday) {
            false
        } else {
            when (task.type) {
                TaskType.DAILY -> true
                TaskType.PERIODIC -> task.selectedDays.contains(today.dayOfWeek)
                TaskType.OCCASIONAL -> task.specificDate == today
            }
        }
    }
    
    val completedToday = tasksToday.filter { it.isCompletedToday() }
    val notCompletedToday = tasksToday.filter { !it.isCompletedToday() }
    
    val completionRate = if (tasksToday.isNotEmpty()) {
        (completedToday.size.toFloat() / tasksToday.size.toFloat() * 100).toInt()
    } else 0
    
    // Statistiques par type
    val dailyTasks = allTasks.filter { it.type == TaskType.DAILY }
    val periodicTasks = allTasks.filter { it.type == TaskType.PERIODIC }
    val occasionalTasks = allTasks.filter { it.type == TaskType.OCCASIONAL }
    
    // Statistiques sur 7 jours
    val last7Days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
    
    // Graphique : nombre de tâches COMPLÉTÉES par jour
    val completionByDay = last7Days.map { date ->
        val completedForDay = allTasks.filter { task ->
            // Vérifier que la tâche existait à cette date
            val taskExistedOnDate = task.createdAt.toLocalDate() <= date
            
            if (!taskExistedOnDate) {
                false
            } else {
                // Vérifier si elle a été complétée ce jour
                when (task.type) {
                    TaskType.DAILY -> task.lastCompletedDate == date
                    TaskType.PERIODIC -> task.selectedDays.contains(date.dayOfWeek) && task.lastCompletedDate == date
                    TaskType.OCCASIONAL -> task.specificDate == date && task.isCompleted
                }
            }
        }
        completedForDay.size
    }
    
    // Statistiques détaillées des 7 derniers jours
    val weekStats = last7Days.map { date ->
        // Tâches qui devaient être faites ce jour
        val tasksForDay = allTasks.filter { task ->
            // Vérifier que la tâche existait déjà à cette date
            val taskExistedOnDate = task.createdAt.toLocalDate() <= date
            
            if (!taskExistedOnDate) {
                false
            } else {
                when (task.type) {
                    TaskType.DAILY -> true // Applicable si elle existait
                    TaskType.PERIODIC -> task.selectedDays.contains(date.dayOfWeek)
                    TaskType.OCCASIONAL -> task.specificDate == date
                }
            }
        }
        
        // Tâches complétées ce jour
        val completedForDay = tasksForDay.filter { task ->
            when (task.type) {
                TaskType.DAILY -> task.lastCompletedDate == date
                TaskType.PERIODIC -> task.lastCompletedDate == date
                TaskType.OCCASIONAL -> task.isCompleted && task.specificDate == date
            }
        }
        
        Triple(date, tasksForDay.size, completedForDay.size)
    }
    
    val totalTasksLast7Days = weekStats.sumOf { it.second }
    val totalCompletedLast7Days = weekStats.sumOf { it.third }
    val averageCompletionRate7Days = if (totalTasksLast7Days > 0) {
        (totalCompletedLast7Days.toFloat() / totalTasksLast7Days * 100).toInt()
    } else 0
    
    // Meilleur jour et pire jour
    val bestDay = weekStats.maxByOrNull { 
        if (it.second > 0) it.third.toFloat() / it.second else 0f 
    }
    val worstDay = weekStats.filter { it.second > 0 }.minByOrNull { 
        it.third.toFloat() / it.second 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistiques",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            // En-tête avec date
            item {
                Text(
                    text = "Aujourd'hui - ${today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Graphique circulaire de complétion
            item {
                CompletionCircleCard(
                    completedCount = completedToday.size,
                    totalCount = tasksToday.size,
                    completionRate = completionRate
                )
            }
            
            // Cartes de statistiques rapides
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Programmées",
                        value = tasksToday.size.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Complétées",
                        value = completedToday.size.toString(),
                        icon = Icons.Default.Done,
                        color = PriorityLow
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "En attente",
                        value = notCompletedToday.size.toString(),
                        icon = Icons.Default.Warning,
                        color = PriorityMedium
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Taux",
                        value = "$completionRate%",
                        icon = Icons.Default.Star,
                        color = SecondaryPurple
                    )
                }
            }
            
            // Section des 7 derniers jours
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Performance des 7 derniers jours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Carte récapitulative des 7 jours
            item {
                WeeklySummaryCard(
                    totalTasks = totalTasksLast7Days,
                    completedTasks = totalCompletedLast7Days,
                    averageRate = averageCompletionRate7Days,
                    bestDay = bestDay,
                    worstDay = worstDay
                )
            }
            
            // Graphique d'activité
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Activité quotidienne",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                WeeklyActivityChart(
                    days = last7Days,
                    completionCounts = completionByDay
                )
            }
            
            // Répartition par type
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Répartition par type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                TaskTypeBreakdownCard(
                    dailyCount = dailyTasks.size,
                    periodicCount = periodicTasks.size,
                    occasionalCount = occasionalTasks.size,
                    totalCount = allTasks.size
                )
            }
        }
    }
}

/**
 * Carte avec graphique circulaire de complétion
 */
@Composable
fun CompletionCircleCard(
    completedCount: Int,
    totalCount: Int,
    completionRate: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Progression du jour",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Graphique circulaire animé
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    completionRate = completionRate
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$completionRate%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$completedCount / $totalCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Indicateur circulaire de progression animé
 */
@Composable
fun CircularProgressIndicator(
    completionRate: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress_animation"
    )
    
    Canvas(modifier = modifier.size(200.dp)) {
        val strokeWidth = 20.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        
        // Cercle de fond
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Arc de progression
        drawArc(
            color = Color(0xFF6200EE),
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Carte de statistique rapide
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Graphique d'activité hebdomadaire
 */
@Composable
fun WeeklyActivityChart(
    days: List<LocalDate>,
    completionCounts: List<Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val maxCount = completionCounts.maxOrNull() ?: 1
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { index, date ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Barre
                        val height = if (maxCount > 0) {
                            ((completionCounts[index].toFloat() / maxCount) * 100).dp
                        } else 0.dp
                        
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(height.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Jour de la semaine
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("EEE")).take(1),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Nombre
                        Text(
                            text = completionCounts[index].toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Carte de répartition par type de tâche
 */
@Composable
fun TaskTypeBreakdownCard(
    dailyCount: Int,
    periodicCount: Int,
    occasionalCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TaskTypeRow(
                label = "Quotidiennes",
                count = dailyCount,
                total = totalCount,
                color = Color(0xFF4CAF50)
            )
            
            TaskTypeRow(
                label = "Périodiques",
                count = periodicCount,
                total = totalCount,
                color = Color(0xFF2196F3)
            )
            
            TaskTypeRow(
                label = "Occasionnelles",
                count = occasionalCount,
                total = totalCount,
                color = Color(0xFFFF9800)
            )
        }
    }
}

/**
 * Carte récapitulative des 7 derniers jours
 */
@Composable
fun WeeklySummaryCard(
    totalTasks: Int,
    completedTasks: Int,
    averageRate: Int,
    bestDay: Triple<LocalDate, Int, Int>?,
    worstDay: Triple<LocalDate, Int, Int>?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Titre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Résumé hebdomadaire",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Badge du taux moyen
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        averageRate >= 80 -> PriorityLow.copy(alpha = 0.2f)
                        averageRate >= 50 -> PriorityMedium.copy(alpha = 0.2f)
                        else -> PriorityHigh.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = "$averageRate%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            averageRate >= 80 -> PriorityLow
                            averageRate >= 50 -> PriorityMedium
                            else -> PriorityHigh
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Statistiques principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$completedTasks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriorityLow
                    )
                    Text(
                        text = "Complétées",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$totalTasks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${totalTasks - completedTasks}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriorityHigh
                    )
                    Text(
                        text = "Manquées",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Message de motivation
            val motivationMessage = when {
                averageRate >= 80 -> "Excellente discipline ! Continuez comme ça !"
                averageRate >= 60 -> "Bon travail ! Encore un petit effort !"
                averageRate >= 40 -> "Vous progressez, restez motivé !"
                else -> "Chaque jour est une nouvelle opportunité !"
            }
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = motivationMessage,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Ligne de répartition par type
 */
@Composable
fun TaskTypeRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$count tâches",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Barre de progression
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            val percentage = if (total > 0) count.toFloat() / total else 0f
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
