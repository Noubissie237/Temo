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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.propentatech.kumbaka.data.model.ChecklistItem
import com.propentatech.kumbaka.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Écran d'édition de note
 * Permet de créer/éditer une note avec titre, contenu, checklist, image et liens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf("Titre") }
    var content by remember { mutableStateOf("Commencez à écrire votre note ici...") }
    var hasImage by remember { mutableStateOf(false) }
    var checklistItems by remember { 
        mutableStateOf(listOf(
            ChecklistItem(text = "Finaliser le design de l'application", isChecked = true),
            ChecklistItem(text = "Préparer la présentation pour lundi", isChecked = false),
            ChecklistItem(text = "Contacter le client pour un retour", isChecked = false)
        ))
    }
    var hasLink by remember { mutableStateOf(true) }
    var linkText by remember { mutableStateOf("Consulter la documentation du projet") }
    
    val lastModified = remember { LocalDateTime.now() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    // Actions de la barre supérieure
                    IconButton(onClick = { hasImage = !hasImage }) {
                        Icon(
                            Icons.Default.Add, // Remplacer par icône image
                            contentDescription = "Ajouter une image"
                        )
                    }
                    IconButton(onClick = { /* TODO: Format texte */ }) {
                        Icon(
                            Icons.Default.Add, // Remplacer par icône texte
                            contentDescription = "Formater le texte"
                        )
                    }
                    IconButton(onClick = { /* TODO: Partager */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Partager"
                        )
                    }
                    IconButton(onClick = { /* TODO: Supprimer */ }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // TODO: Sauvegarder la note
                    onNavigateBack()
                },
                containerColor = SecondaryPurple,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Sauvegarder",
                    tint = Color.White
                )
            }
        },
        bottomBar = {
            // Barre d'outils en bas
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { 
                        checklistItems = checklistItems + ChecklistItem(text = "")
                    }) {
                        Icon(
                            Icons.Default.Add, // Remplacer par icône checklist
                            contentDescription = "Ajouter checklist"
                        )
                    }
                    IconButton(onClick = { hasImage = !hasImage }) {
                        Icon(
                            Icons.Default.Add, // Remplacer par icône image
                            contentDescription = "Ajouter image"
                        )
                    }
                    IconButton(onClick = { hasLink = !hasLink }) {
                        Icon(
                            Icons.Default.Add, // Remplacer par icône lien
                            contentDescription = "Ajouter lien"
                        )
                    }
                }
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
            // Titre
            item {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    placeholder = {
                        Text(
                            text = "Titre",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // Contenu texte
            item {
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = {
                        Text(
                            text = "Commencez à écrire votre note ici...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // Image (si présente)
            if (hasImage) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6B8E23),
                                        Color(0xFF556B2F)
                                    )
                                )
                            )
                    ) {
                        IconButton(
                            onClick = { hasImage = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Supprimer l'image",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Checklist
            items(checklistItems) { item ->
                ChecklistItemRow(
                    item = item,
                    onCheckedChange = { isChecked ->
                        checklistItems = checklistItems.map {
                            if (it.id == item.id) it.copy(isChecked = isChecked) else it
                        }
                    },
                    onTextChange = { newText ->
                        checklistItems = checklistItems.map {
                            if (it.id == item.id) it.copy(text = newText) else it
                        }
                    },
                    onDelete = {
                        checklistItems = checklistItems.filter { it.id != item.id }
                    }
                )
            }

            // Lien
            if (hasLink) {
                item {
                    Text(
                        text = linkText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { /* TODO: Ouvrir le lien */ }
                    )
                }
            }

            // Date de modification
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Modifié le ${lastModified.format(DateTimeFormatter.ofPattern("dd MMM. à HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Ligne d'item de checklist
 */
@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (item.isChecked) SecondaryPurple else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .background(
                    if (item.isChecked) SecondaryPurple else Color.Transparent
                )
                .clickable { onCheckedChange(!item.isChecked) },
            contentAlignment = Alignment.Center
        ) {
            if (item.isChecked) {
                Text(
                    text = "✓",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Texte
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (item.isChecked) 
                MaterialTheme.colorScheme.onSurfaceVariant 
            else 
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
