package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Link
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
import com.propentatech.kumbaka.data.model.Note
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.NoteViewModel
import com.propentatech.kumbaka.ui.viewmodel.NoteViewModelFactory
import java.time.LocalDateTime
import java.util.UUID

/**
 * Écran d'édition de note
 * Permet de créer/éditer une note avec titre, contenu et liens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    // Récupérer le ViewModel
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(application.noteRepository)
    )
    
    // État local
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var links by remember { mutableStateOf(listOf<String>()) }
    var newLink by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Stocker la note existante pour conserver les dates
    var existingNote by remember { mutableStateOf<Note?>(null) }
    
    val isEditMode = noteId != null
    
    // Charger la note si on est en mode édition
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNoteById(noteId).collect { note ->
                note?.let {
                    existingNote = it
                    title = it.title
                    content = it.content
                    links = it.links
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Modifier la note" else "Nouvelle note",
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
                )
            )
        }
    ) { paddingValues ->
        // Validation du formulaire
        val isFormValid = title.isNotBlank()
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titre *") },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Contenu
            item {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    label = { Text("Contenu (optionnel)") },
                    placeholder = { Text("Écrivez votre note ici...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Section Liens
            item {
                Text(
                    text = "Liens de référence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Liste des liens
            items(links) { link ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = link,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { links = links.filter { it != link } }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Ajouter un lien
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newLink,
                        onValueChange = { newLink = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nouveau lien") },
                        placeholder = { Text("https://...") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    IconButton(
                        onClick = {
                            if (newLink.isNotBlank()) {
                                links = links + newLink
                                newLink = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            Icons.Outlined.AddLink,
                            contentDescription = "Ajouter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Bouton de sauvegarde en bas
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val note = if (isEditMode && existingNote != null) {
                                    // Mode édition : conserver createdAt, mettre à jour updatedAt
                                    existingNote!!.copy(
                                        title = title,
                                        content = content,
                                        links = links,
                                        updatedAt = LocalDateTime.now()
                                    )
                                } else {
                                    // Mode création : nouvelle note avec createdAt, updatedAt = null
                                    Note(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        content = content,
                                        links = links,
                                        createdAt = LocalDateTime.now(),
                                        updatedAt = null
                                    )
                                }
                                
                                if (isEditMode) {
                                    viewModel.updateNote(note)
                                } else {
                                    viewModel.addNote(note)
                                }
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .height(56.dp),
                        enabled = isFormValid,
                        shape = RoundedCornerShape(16.dp),
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
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Dialogue de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la note ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteId?.let { viewModel.deleteNote(it) }
                        onNavigateBack()
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
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
