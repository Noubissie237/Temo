package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableLazyListState
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Note
import com.propentatech.kumbaka.ui.components.EmptyStateMessage
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.NoteViewModel
import com.propentatech.kumbaka.ui.viewmodel.NoteViewModelFactory
import java.time.format.DateTimeFormatter

/**
 * Écran de gestion des notes
 * Affiche toutes les notes avec recherche
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNoteClick: (String) -> Unit = {},
    onCreateNote: () -> Unit = {}
) {
    // Récupérer le ViewModel
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(application.noteRepository)
    )
    
    // Observer les notes depuis la base de données
    val allNotes by viewModel.notes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    // Focus automatique quand la recherche s'active
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }
    
    val filteredNotes = if (searchQuery.isBlank()) {
        allNotes
    } else {
        allNotes.filter { note ->
            note.title.contains(searchQuery, ignoreCase = true) ||
            note.content.contains(searchQuery, ignoreCase = true)
        }
    }
    
    // État pour le drag & drop
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            // Réorganiser la liste localement
            val mutableNotes = filteredNotes.toMutableList()
            val item = mutableNotes.removeAt(from.index)
            mutableNotes.add(to.index, item)
            
            // Mettre à jour l'ordre dans la base de données
            viewModel.updateNotesOrder(mutableNotes)
        }
    )

    Scaffold(
        topBar = {
            if (isSearchActive) {
                // Barre de recherche
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Rechercher une note...",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    windowInsets = WindowInsets(top = 0.dp)
                )
            } else {
                // Barre normale
                TopAppBar(
                    title = {
                        Text(
                            text = "Mes Notes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Rechercher"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    windowInsets = WindowInsets(top = 0.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Créer une note",
                    tint = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Liste des notes
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredNotes.isEmpty()) {
                item {
                    EmptyStateMessage(
                        message = if (searchQuery.isEmpty()) "Aucune note" else "Aucun résultat",
                        subtitle = if (searchQuery.isEmpty()) "Créez votre première note" else "Essayez avec d'autres mots-clés",
                        icon = if (searchQuery.isEmpty()) Icons.Outlined.Edit else Icons.Outlined.Search,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            }
            
            items(filteredNotes, key = { it.id }) { note ->
                ReorderableItem(reorderableLazyListState, key = note.id) { isDragging ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        isDragging = isDragging,
                        dragHandleModifier = Modifier.longPressDraggableHandle()
                    )
                }
            }
        }
    }
}

/**
 * Carte de note
 */
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (dragHandleModifier != null) dragHandleModifier
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                SurfaceDark
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Titre
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contenu
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryDark,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Liens (si présents)
            if (note.links.isNotEmpty()) {
                Text(
                    text = "🔗 ${note.links.size} lien(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Footer avec date
            Text(
                text = formatNoteDate(note),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
        }
        
        // Icône pour voir les détails
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onClick
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = "Voir les détails",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        }
    }
}

/**
 * Formate la date de la note
 */
fun formatNoteDate(note: Note): String {
    val now = java.time.LocalDateTime.now()
    val noteDate = note.updatedAt
    
    val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
        noteDate.toLocalDate(),
        now.toLocalDate()
    )
    
    return when {
        daysDiff == 0L -> {
            val hoursDiff = java.time.temporal.ChronoUnit.HOURS.between(noteDate, now)
            if (hoursDiff < 1) "À l'instant"
            else if (hoursDiff < 24) "Il y a $hoursDiff heure${if (hoursDiff > 1) "s" else ""}"
            else "Aujourd'hui à ${noteDate.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        }
        daysDiff == 1L -> "Hier"
        daysDiff < 7 -> "Il y a $daysDiff jours"
        else -> noteDate.format(DateTimeFormatter.ofPattern("dd MMM"))
    }
}
