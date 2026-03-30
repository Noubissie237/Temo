package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.propentatech.kumbaka.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.MockData
import com.propentatech.kumbaka.data.model.*
import com.propentatech.kumbaka.ui.components.EmptyStateMessage
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.EventViewModel
import com.propentatech.kumbaka.ui.viewmodel.EventViewModelFactory
import com.propentatech.kumbaka.ui.viewmodel.NoteViewModel
import com.propentatech.kumbaka.ui.viewmodel.NoteViewModelFactory
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModel
import com.propentatech.kumbaka.ui.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Écran d'accueil (Dashboard)
 * Affiche un résumé des tâches du jour, événements à venir et notes récentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    securityViewModel: com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel,
    themeViewModel: com.propentatech.kumbaka.ui.viewmodel.ThemeViewModel,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToPlus: () -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onEventClick: (String) -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    onNavigateToAlarms: () -> Unit = {},
    onNavigateToStopwatch: () -> Unit = {}
) {
    // Récupérer le ViewModel pour les tâches
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(application.taskRepository)
    )
    
    // Vérifier et réinitialiser les tâches au chargement de l'écran
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            application.taskResetManager.checkAndResetTasks()
        }
    }
    
    // Observer les tâches depuis la base de données
    val allTasks by taskViewModel.tasks.collectAsState()
    
    // Filtrer les tâches du jour (non complétées)
    val todayTasks = allTasks.filter { task ->
        task.shouldShowToday() && !task.isCompletedToday()
    }.take(3)
    
    // Récupérer le ViewModel pour les événements
    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(application.eventRepository)
    )
    
    // Observer les événements depuis la base de données
    val allEvents by eventViewModel.events.collectAsState()
    val today = remember { java.time.LocalDate.now() }
    // Filtrer uniquement les événements à venir pour la page d'accueil
    val events = allEvents.filter { it.date >= today }.sortedBy { it.date }.take(3)
    
    // Récupérer le ViewModel pour les notes
    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(application.noteRepository)
    )
    
    // Observer les notes depuis la base de données
    val allNotes by noteViewModel.notes.collectAsState()
    val notes = allNotes.sortedByDescending { it.updatedAt ?: it.createdAt }.take(4)

    // ===== MyLive Real Data Injection =====
    // Finance ViewModel
    val financeViewModel: com.propentatech.kumbaka.ui.viewmodel.FinanceViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return com.propentatech.kumbaka.ui.viewmodel.FinanceViewModel(application.financeRepository) as T
            }
        }
    )
    val transactions by financeViewModel.transactions.collectAsState()
    val balance = transactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }

    // Advisor ViewModel
    val advisorViewModel: com.propentatech.kumbaka.ui.viewmodel.AdvisorViewModel = viewModel(
        factory = com.propentatech.kumbaka.ui.viewmodel.AdvisorViewModelFactory(application.database.advisorLogDao())
    )
    val latestAdvice by advisorViewModel.latestLog.collectAsState()
    
    // État de la recherche
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // État du FAB Pulsé
    var isFabExpanded by remember { mutableStateOf(false) }
    
    // Résultats de recherche
    val searchResults = remember(searchQuery, allTasks, allEvents, allNotes) {
        if (searchQuery.isBlank()) {
            emptyMap()
        } else {
            mapOf(
                "Tâches" to allTasks.filter { task ->
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true)
                },
                "Événements" to allEvents.filter { event ->
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.description.contains(searchQuery, ignoreCase = true) ||
                    event.location.contains(searchQuery, ignoreCase = true)
                },
                "Notes" to allNotes.filter { note ->
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.links.any { it.contains(searchQuery, ignoreCase = true) }
                }
            ).filterValues { it.isNotEmpty() }
        }
    }
    
    val username by securityViewModel.username.collectAsState()

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                            placeholder = { Text("Rechercher dans MyLive...", color = Color.Gray) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo MyLive",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (username.isNotEmpty()) "Bonjour, $username" else "MyLive",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                if (username.isNotEmpty()) {
                                    Text(
                                        text = "Tableau de bord",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                },
                onNavigateBack = if (isSearching) { { isSearching = false; searchQuery = "" } } else null,
                actions = {
                    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
                    // Bouton Profil (Accès Plus/Settings)
                    IconButton(onClick = onNavigateToPlus) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profil",
                            tint = Color.White
                        )
                    }
                    
                    IconButton(onClick = { 
                        scope.launch { themeViewModel.setDarkMode(!isDarkMode) }
                    }) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Changer le thème",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Fermer" else "Rechercher",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            PulseActionFAB(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onAction = { action ->
                    isFabExpanded = false
                    when (action) {
                        "task" -> onNavigateToTasks() // Ou ouvrir directement l'éditeur
                        "finance" -> onNavigateToFinance()
                        "note" -> onNavigateToNotes()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Affichage des résultats de recherche
            if (isSearching && searchQuery.isNotBlank()) {
                if (searchResults.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aucun résultat trouvé",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Essayez avec d'autres mots-clés",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    searchResults.forEach { (category, items) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        when (category) {
                            "Tâches" -> {
                                items(items as List<Task>) { task ->
                                    TaskItemCompact(
                                        task = task,
                                        onClick = { onTaskClick(task.id) }
                                    )
                                }
                            }
                            "Événements" -> {
                                items(items as List<Event>) { event ->
                                    EventItemCompact(
                                        event = event,
                                        onClick = { onEventClick(event.id) }
                                    )
                                }
                            }
                            "Notes" -> {
                                items(items as List<Note>) { note ->
                                    NoteItemCompact(
                                        note = note,
                                        onClick = { onNoteClick(note.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Contenu normal de la page d'accueil (MyLive Experience)
                
                // MyLive Pulse Balance
                item {
                    QuickFinancePulse(balance)
                    // Raccourcis Premium : Finance & Rapports
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onNavigateToFinance,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp), tint = Orange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Finances", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = onNavigateToReports,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rapports", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                
                // Widget du Conseiller Expert (Glassmorphism Premium)
                item {
                    AdvisorWidget(latestAdvice)
                }
                
                // Section Tâches du jour
                item {
                    SectionCard(
                        title = "Mes tâches du jour",
                        onSeeAllClick = onNavigateToTasks
                    ) {
                        if (todayTasks.isEmpty()) {
                            EmptyStateMessage(
                                message = "Aucune tâche à faire",
                                subtitle = "Profitez de votre journée, ou ajoutez une tâche",
                                icon = Icons.Outlined.CheckCircle
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                todayTasks.forEach { task ->
                                    TaskItemCompact(
                                        task = task,
                                        onClick = { onTaskClick(task.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Événements à venir
                item {
                    Column {
                        Text(
                            text = "Événements à venir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        if (events.isEmpty()) {
                            EmptyStateMessage(
                                message = "Aucun événement prévu",
                                subtitle = "Créez votre premier événement",
                                icon = Icons.Outlined.DateRange
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                events.forEach { event ->
                                    EventItemCompact(
                                        event = event,
                                        onClick = { onEventClick(event.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Notes récentes
                item {
                    Column {
                        Text(
                            text = "Notes récentes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        if (notes.isEmpty()) {
                            EmptyStateMessage(
                                message = "Aucune note",
                                subtitle = "Commencez à prendre des notes",
                                icon = Icons.Outlined.Edit
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(notes) { note ->
                                    NoteCardCompact(
                                        note = note,
                                        onClick = { onNoteClick(note.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Carte de section avec titre et bouton "Voir tout"
 */
@Composable
fun SectionCard(
    title: String,
    onSeeAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Breathing room
        shape = RoundedCornerShape(24.dp), // Plus joyeux
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onSeeAllClick) {
                    Text(
                        text = "Voir tout",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Item de tâche compact pour le dashboard
 */
@Composable
fun TaskItemCompact(
    task: Task,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox simulée
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Item d'événement compact pour le dashboard
 */
@Composable
fun EventItemCompact(
    event: Event,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                event.time?.let { time ->
                    Text(
                        text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Badge de compte à rebours
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = event.getCountdownLabel(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Carte de note compacte pour le dashboard
 */
@Composable
fun NoteCardCompact(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
        }
    }
}

/**
 * Item de note compact pour les résultats de recherche
 */
@Composable
fun NoteItemCompact(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            if (note.links.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🔗 ${note.links.size} lien(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Widget Premium du Conseiller Expert
 */
@Composable
fun QuickFinancePulse(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Solde MyLive Pulse", color = Color.Gray, fontSize = 12.sp)
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape).background(Orange)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${balance.toLong()} FCFA",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Orange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tendance saine détectée", color = Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

@Composable
fun AdvisorWidget(log: AdvisorLog?) {
    val gradientColors = listOf(Color(0xFFFF6B00), Color(0xFFE85D00))
    val message = log?.generatedMessage ?: "Bienvenue sur MyLive. Je suis votre conseiller expert. Je surveille vos performances jour après jour."
    val moodEmoji = when (log?.advisorMood) {
        AdvisorMood.JOYFUL -> "😊"
        AdvisorMood.WORRIED -> "😟"
        AdvisorMood.FURIOUS -> "😡"
        else -> "🧠"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradientColors))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(moodEmoji, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Conseiller Expert MyLive",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun PulseActionFAB(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAction: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isExpanded) {
            QuickActionItem("Finance", Icons.Default.AddBusiness, Orange) { onAction("finance") }
            QuickActionItem("Tâche", Icons.Default.AddTask, Orange) { onAction("task") }
            QuickActionItem("Note", Icons.Default.NoteAdd, Orange) { onAction("note") }
        }
        
        FloatingActionButton(
            onClick = onToggle,
            containerColor = Color.Black,
            contentColor = Orange,
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Actions Rapides",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun QuickActionItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        FloatingActionButton(
            onClick = onClick,
            containerColor = Orange,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun DynamicGreeting() {
    val hour = java.time.LocalTime.now().hour
    val greeting = when {
        hour in 5..11 -> "Bonjour \uD83C\uDF44"
        hour in 12..17 -> "Bel après-midi \u2600\uFE0F"
        else -> "Bonsoir \uD83C\uDF19"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Prêt à conquérir votre journée ?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
