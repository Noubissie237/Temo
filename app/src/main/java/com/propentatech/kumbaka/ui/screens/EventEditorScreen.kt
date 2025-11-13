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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Event
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.EventViewModel
import com.propentatech.kumbaka.ui.viewmodel.EventViewModelFactory
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Écran de création/édition d'événement
 * Permet de créer ou modifier un événement avec titre, description, date et tâches liées
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(
    eventId: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    // Récupérer le ViewModel
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(application.eventRepository)
    )
    
    // État local
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val isEditMode = eventId != null
    
    // Charger l'événement si on est en mode édition
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.getEventById(eventId).collect { event ->
                event?.let {
                    title = it.title
                    description = it.description
                    location = it.location
                    selectedDate = it.date
                    selectedTime = it.time
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (eventId == null) "Nouvel événement" else "Modifier l'événement",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
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
                ),
                windowInsets = WindowInsets(top = 0.dp)
            )
        }
    ) { paddingValues ->
        // Validation du formulaire
        val isFormValid = title.isNotBlank() && selectedDate != null
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Titre
            item {
                Column {
                    Text(
                        text = "Titre",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nom de l'événement") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // Description (optionnelle)
            item {
                Column {
                    Text(
                        text = "Description (optionnelle)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        placeholder = { Text("Ajouter une description...") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // Lieu
            item {
                Column {
                    Text(
                        text = "Lieu",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ajouter un lieu...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // Date
            item {
                Column {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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
                                .clickable { showDatePicker = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            // Badge de compte à rebours
                            val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), selectedDate)
                            if (daysUntil >= 0) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = AccentLightBlue
                                ) {
                                    Text(
                                        text = if (daysUntil == 0L) "Aujourd'hui" else "J-$daysUntil",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Heure (optionnelle)
            item {
                Column {
                    Text(
                        text = "Heure (optionnelle)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true },
                        placeholder = { Text("Ajouter une heure...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (selectedTime != null) {
                                IconButton(onClick = { selectedTime = null }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Supprimer l'heure",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        readOnly = true,
                        enabled = false
                    )
                }
            }
            }
            
            // Bouton Sauvegarder en bas
            Button(
                onClick = {
                    if (isFormValid) {
                        val event = Event(
                            id = eventId ?: UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            location = location,
                            date = selectedDate,
                            time = selectedTime
                        )
                        
                        if (isEditMode) {
                            viewModel.updateEvent(event)
                        } else {
                            viewModel.addEvent(event)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
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
        }
    }
    
    // Dialogue de sélection de date
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Dialogue de sélection d'heure
    if (showTimePicker) {
        TimePickerDialog(
            selectedTime = selectedTime,
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
    
    // Dialogue de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'événement ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventId?.let { viewModel.deleteEvent(it) }
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

/**
 * Dialogue de sélection de date
 */
@Composable
fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var tempYear by remember { mutableStateOf(selectedDate.year) }
    var tempMonth by remember { mutableStateOf(selectedDate.monthValue) }
    var tempDay by remember { mutableStateOf(selectedDate.dayOfMonth) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sélectionner une date") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Année
                OutlinedTextField(
                    value = tempYear.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { year -> tempYear = year }
                    },
                    label = { Text("Année") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Mois
                OutlinedTextField(
                    value = tempMonth.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { month -> 
                            if (month in 1..12) tempMonth = month 
                        }
                    },
                    label = { Text("Mois (1-12)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Jour
                OutlinedTextField(
                    value = tempDay.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { day -> 
                            if (day in 1..31) tempDay = day 
                        }
                    },
                    label = { Text("Jour (1-31)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val newDate = LocalDate.of(tempYear, tempMonth, tempDay)
                        onDateSelected(newDate)
                    } catch (e: Exception) {
                        // Date invalide, ne rien faire
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Dialogue de sélection d'heure
 */
@Composable
fun TimePickerDialog(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var tempHour by remember { mutableStateOf(selectedTime?.hour ?: 12) }
    var tempMinute by remember { mutableStateOf(selectedTime?.minute ?: 0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sélectionner une heure") },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heure
                OutlinedTextField(
                    value = tempHour.toString().padStart(2, '0'),
                    onValueChange = { 
                        it.toIntOrNull()?.let { hour -> 
                            if (hour in 0..23) tempHour = hour 
                        }
                    },
                    label = { Text("HH") },
                    modifier = Modifier.weight(1f)
                )
                
                Text(":", style = MaterialTheme.typography.headlineMedium)
                
                // Minutes
                OutlinedTextField(
                    value = tempMinute.toString().padStart(2, '0'),
                    onValueChange = { 
                        it.toIntOrNull()?.let { minute -> 
                            if (minute in 0..59) tempMinute = minute 
                        }
                    },
                    label = { Text("MM") },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newTime = LocalTime.of(tempHour, tempMinute)
                    onTimeSelected(newTime)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Composant supprimé - LinkedTaskItem
 * La fonctionnalité de tâches liées sera implémentée plus tard
 */
