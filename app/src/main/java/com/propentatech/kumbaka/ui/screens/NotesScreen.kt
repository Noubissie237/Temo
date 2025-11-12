package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.propentatech.kumbaka.data.MockData
import com.propentatech.kumbaka.data.model.Note
import com.propentatech.kumbaka.data.model.NoteType
import com.propentatech.kumbaka.ui.theme.*
import java.time.format.DateTimeFormatter

/**
 * Écran de gestion des notes
 * Affiche les notes avec filtres par type (Tout, Checklists, Avec images, Liens)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNoteClick: (String) -> Unit = {},
    onCreateNote: () -> Unit = {}
) {
    // Données mock
    val allNotes = remember { MockData.mockNotes }
    var selectedFilter by remember { mutableStateOf<NoteType?>(null) }
    
    val filteredNotes = remember(selectedFilter) {
        if (selectedFilter == null) {
            allNotes
        } else {
            allNotes.filter { it.type == selectedFilter }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mes Notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Recherche */ }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: Options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
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
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtres
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        label = "Tout",
                        isSelected = selectedFilter == null,
                        onClick = { selectedFilter = null }
                    )
                }
                item {
                    FilterChip(
                        label = "Checklists",
                        isSelected = selectedFilter == NoteType.CHECKLIST,
                        onClick = { selectedFilter = NoteType.CHECKLIST }
                    )
                }
                item {
                    FilterChip(
                        label = "Avec images",
                        isSelected = selectedFilter == NoteType.IMAGE,
                        onClick = { selectedFilter = NoteType.IMAGE }
                    )
                }
                item {
                    FilterChip(
                        label = "Liens",
                        isSelected = selectedFilter == NoteType.LINK,
                        onClick = { selectedFilter = NoteType.LINK }
                    )
                }
            }

            // Liste des notes
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredNotes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) }
                    )
                }
            }
        }
    }
}

/**
 * Chip de filtre personnalisé
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryBlue else SurfaceDark,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else TextSecondaryDark,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

/**
 * Carte de note
 */
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Image si présente
            if (note.type == NoteType.IMAGE && note.imageUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF9C27B0),
                                    Color(0xFF2196F3)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Titre
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contenu
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer avec date et icône de type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatNoteDate(note),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
                
                // Icône selon le type de note
                when (note.type) {
                    NoteType.IMAGE -> {
                        Icon(
                            imageVector = Icons.Default.Add, // Remplacer par icône image
                            contentDescription = "Note avec image",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    NoteType.LINK -> {
                        Icon(
                            imageVector = Icons.Default.Add, // Remplacer par icône lien
                            contentDescription = "Note avec lien",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    else -> {}
                }
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
