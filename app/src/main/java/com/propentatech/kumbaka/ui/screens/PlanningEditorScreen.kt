package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.propentatech.kumbaka.data.model.*
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar
import com.propentatech.kumbaka.ui.viewmodel.PlanningViewModel
import com.propentatech.kumbaka.ui.viewmodel.PlanningViewModelFactory
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val PALETTE = listOf(
    "#FF6B00", "#E91E63", "#9C27B0", "#3F51B5",
    "#2196F3", "#009688", "#4CAF50", "#FF9800"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningEditorScreen(
    sessionId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: PlanningViewModel = viewModel(factory = PlanningViewModelFactory(application.planningRepository))

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PlanningType.DAILY) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedColor by remember { mutableStateOf(PALETTE[0]) }
    var isImportant by remember { mutableStateOf(false) }
    var reminders by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var recurrence by remember { mutableStateOf(PlanningRecurrence.NONE) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var existingSession by remember { mutableStateOf<PlanningSession?>(null) }

    LaunchedEffect(sessionId) {
        sessionId?.let { id ->
            val s = viewModel.allSessions.value.find { it.id == id }
            s?.let {
                existingSession = it
                title = it.title
                description = it.description
                selectedType = it.type
                selectedDate = it.date
                startTime = it.startTime
                endTime = it.endTime
                selectedColor = it.color
                isImportant = it.isImportant
                reminders = it.reminderMinutesBefore.toSet()
                recurrence = it.recurrence
            }
        }
    }

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = {
                    Text(
                        text = if (sessionId == null) "Nouvelle Séance" else "Modifier Séance",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                onNavigateBack = onNavigateBack,
                actions = {
                    if (sessionId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Titre
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre de la séance") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optionnelle)") },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                maxLines = 3
            )

            // Type de planning
            Text("Type de planning", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanningType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text("${type.emoji} ${type.label}") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Date
            Text("Date", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            DateQuickSelector(selectedDate = selectedDate, onDateSelected = { selectedDate = it })

            // Horaires
            Text("Horaires", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    TimeQuickSelector(selectedTime = startTime, onTimeSelected = { startTime = it }, label = "Début")
                }
                Box(Modifier.weight(1f)) {
                    TimeQuickSelector(selectedTime = endTime, onTimeSelected = { endTime = it }, label = "Fin")
                }
            }

            // Couleur
            Text("Couleur", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PALETTE.forEach { hex ->
                    val c = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(if (selectedColor == hex) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
                            .clickable { selectedColor = hex }
                    )
                }
            }

            // Rappels
            Text("Rappels avant la séance", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10 to "10 min", 30 to "30 min", 60 to "1 heure", 1440 to "1 jour").forEach { (mins, label) ->
                    FilterChip(
                        selected = mins in reminders,
                        onClick = {
                            reminders = if (mins in reminders) reminders - mins else reminders + mins
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Récurrence
            Text("Récurrence", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanningRecurrence.values().forEach { rec ->
                    FilterChip(
                        selected = recurrence == rec,
                        onClick = { recurrence = rec },
                        label = { Text(rec.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Date Importante
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Date importante ⭐", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text("Marquer cette date sur le calendrier", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isImportant, onCheckedChange = { isImportant = it })
            }

            // Bouton Sauvegarder
            val isValid = title.isNotBlank()
            Button(
                onClick = {
                    val session = if (existingSession != null) {
                        existingSession!!.copy(
                            title = title, description = description, type = selectedType,
                            date = selectedDate, startTime = startTime, endTime = endTime,
                            color = selectedColor, isImportant = isImportant,
                            reminderMinutesBefore = reminders.sorted(),
                            recurrence = recurrence
                        )
                    } else {
                        PlanningSession(
                            id = UUID.randomUUID().toString(),
                            title = title, description = description, type = selectedType,
                            date = selectedDate, startTime = startTime, endTime = endTime,
                            color = selectedColor, isImportant = isImportant,
                            reminderMinutesBefore = reminders.sorted(),
                            recurrence = recurrence
                        )
                    }
                    if (existingSession != null) viewModel.updateSession(session)
                    else viewModel.addSession(session)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = isValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sauvegarder", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la séance ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    sessionId?.let { viewModel.deleteSession(it) }
                    showDeleteDialog = false
                    onNavigateBack()
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Supprimer")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") } }
        )
    }
}
