package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.Image
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
    onNavigateToTasks: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onEventClick: (String) -> Unit = {},
    onNoteClick: (String) -> Unit = {}
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
    val events = allEvents.sortedBy { it.date }.take(3)
    
    // Récupérer le ViewModel pour les notes
    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(application.noteRepository)
    )
    
    // Observer les notes depuis la base de données
    val allNotes by noteViewModel.notes.collectAsState()
    val notes = allNotes.sortedByDescending { it.updatedAt }.take(4)
    
    // État de la recherche
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Rechercher...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                },
                navigationIcon = {
                    if (!isSearching) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo de l'application
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo Temo",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            // Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Temo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            isSearching = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Fermer" else "Rechercher"
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
                // Contenu normal de la page d'accueil
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
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
                        text = time.format(DateTimeFormatter.ofPattern("HH:mm")) + 
                              if (event.time != null) " - ${event.time!!.plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm"))}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Badge de compte à rebours
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AccentLightPurple
            ) {
                Text(
                    text = event.getCountdownLabel(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryPurple,
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
