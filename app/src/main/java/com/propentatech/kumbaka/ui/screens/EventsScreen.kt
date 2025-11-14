package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableLazyListState
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Event
import com.propentatech.kumbaka.data.model.daysUntil
import com.propentatech.kumbaka.data.model.getCountdownLabel
import com.propentatech.kumbaka.ui.components.EmptyStateMessage
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.EventViewModel
import com.propentatech.kumbaka.ui.viewmodel.EventViewModelFactory
import java.time.format.DateTimeFormatter

/**
 * Écran de liste des événements
 * Affiche tous les événements à venir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onEventClick: (String) -> Unit = {},
    onCreateEvent: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {}
) {
    // Récupérer le ViewModel
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(application.eventRepository)
    )
    
    // Observer les événements depuis la base de données
    val allEvents by viewModel.events.collectAsState()
    
    // État pour afficher/masquer l'historique
    var showHistory by remember { mutableStateOf(false) }
    
    // Filtrer les événements : séparer à venir et passés récents (7 jours)
    val today = remember { java.time.LocalDate.now() }
    val sevenDaysAgo = remember { today.minusDays(7) }
    
    val upcomingEvents = allEvents.filter { it.date >= today }.sortedBy { it.date }
    val recentPastEvents = allEvents.filter { it.date < today && it.date >= sevenDaysAgo }.sortedByDescending { it.date }
    val events = if (showHistory) recentPastEvents else upcomingEvents
    
    // État pour le drag & drop
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            // Réorganiser la liste localement
            val mutableEvents = events.toMutableList()
            val item = mutableEvents.removeAt(from.index)
            mutableEvents.add(to.index, item)
            
            // Mettre à jour l'ordre dans la base de données
            viewModel.updateEventsOrder(mutableEvents)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (showHistory) "Historique" else "Événements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Bouton pour basculer entre événements à venir et historique
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Icon(
                            imageVector = if (showHistory) Icons.Default.DateRange else Icons.Outlined.DateRange,
                            contentDescription = if (showHistory) "Voir les événements à venir" else "Voir l'historique",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                onClick = onCreateEvent,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Créer un événement",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (events.isEmpty()) {
                item {
                    EmptyStateMessage(
                        message = if (showHistory) "Aucun événement récent" else "Aucun événement",
                        subtitle = if (showHistory) "Les événements des 7 derniers jours apparaîtront ici" else "Créez votre premier événement",
                        icon = Icons.Outlined.DateRange,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            }
            
            items(events, key = { it.id }) { event ->
                ReorderableItem(reorderableLazyListState, key = event.id) { isDragging ->
                    EventListItem(
                        event = event,
                        onClick = { onEventClick(event.id) },
                        isDragging = isDragging,
                        dragHandleModifier = Modifier.longPressDraggableHandle()
                    )
                }
            }
        }
    }
}

/**
 * Item d'événement dans la liste - Affichage amélioré
 */
@Composable
fun EventListItem(
    event: Event,
    onClick: () -> Unit,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // En-tête : Titre + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Titre
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
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
            
            // Description (si elle existe)
            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
            
            // Informations : Date, heure et lieu
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Date et heure
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    event.time?.let { time ->
                        Text(
                            text = "${event.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} • ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    } ?: run {
                        Text(
                            text = event.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Lieu (si il existe)
                if (event.location.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Drag handle (seulement si dragHandleModifier est fourni)
        if (dragHandleModifier != null) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                modifier = dragHandleModifier,
                onClick = { /* Ne rien faire */ }
            ) {
                Icon(
                    Icons.Outlined.Menu,
                    contentDescription = "Réorganiser",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
    }
}
