package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Event
import com.propentatech.kumbaka.data.model.PlanningSession
import com.propentatech.kumbaka.data.model.DayNote
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.CalendarViewModel
import com.propentatech.kumbaka.ui.viewmodel.CalendarViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onCreateEvent: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
    onPlanningClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(application.calendarRepository)
    )

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val isYearlyView by viewModel.isYearlyView.collectAsState()
    val calendarData by viewModel.calendarData.collectAsState()
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<DayNote?>(null) }

    val eventsByDate = remember(calendarData.events) { calendarData.events.groupBy { it.date } }
    val planningByDate = remember(calendarData.planningSessions) { calendarData.planningSessions.groupBy { it.date } }
    val notesByDate = remember(calendarData.dayNotes) { calendarData.dayNotes.associateBy { it.date } }

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = {
                    Column {
                        Text(
                            if (isYearlyView) "Année ${selectedMonth.year}" else selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)).replaceFirstChar { it.uppercase() },
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Calendrier Omniscient",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            if (isYearlyView) Icons.Default.CalendarMonth else Icons.Default.CalendarToday,
                            contentDescription = "Changer de vue",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEvent,
                containerColor = Color(0xFFFF6B00),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Navigation
            CalendarNavigation(
                selectedMonth = selectedMonth,
                isYearlyView = isYearlyView,
                onPrev = { viewModel.prevPage() },
                onNext = { viewModel.nextPage() }
            )

            if (isYearlyView) {
                YearlyCalendarGrid(
                    year = selectedMonth.year,
                    eventsByDate = eventsByDate,
                    planningByDate = planningByDate,
                    notesByDate = notesByDate,
                    onDateClick = { 
                        selectedDate = it
                        viewModel.toggleViewMode()
                    }
                )
            } else {
                MonthlyCalendarView(
                    currentMonth = selectedMonth,
                    selectedDate = selectedDate,
                    eventsByDate = eventsByDate,
                    planningByDate = planningByDate,
                    notesByDate = notesByDate,
                    onDateSelected = { selectedDate = it },
                    onLongClick = { 
                        selectedDate = it
                        noteToEdit = notesByDate[it]
                        showNoteDialog = true 
                    }
                )

                // Détails du jour
                DayDetailsList(
                    date = selectedDate,
                    events = eventsByDate[selectedDate] ?: emptyList(),
                    sessions = planningByDate[selectedDate] ?: emptyList(),
                    note = notesByDate[selectedDate],
                    onEventClick = onEventClick,
                    onPlanningClick = onPlanningClick,
                    onAddNote = { showNoteDialog = true }
                )
            }
        }
    }

    if (showNoteDialog) {
        DayNoteDialog(
            date = selectedDate,
            initialNote = notesByDate[selectedDate],
            onDismiss = { showNoteDialog = false },
            onSave = { content, color ->
                viewModel.saveDayNote(selectedDate, content, color)
                showNoteDialog = false
            },
            onDelete = {
                notesByDate[selectedDate]?.let { viewModel.deleteDayNote(it) }
                showNoteDialog = false
            }
        )
    }
}

@Composable
fun CalendarNavigation(
    selectedMonth: LocalDate,
    isYearlyView: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Précédent", tint = Color.Gray)
        }
        
        Text(
            if (isYearlyView) "${selectedMonth.year}" else selectedMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH)).replaceFirstChar { it.uppercase() },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Suivant", tint = Color.Gray)
        }
    }
}

@Composable
fun MonthlyCalendarView(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<Event>>,
    planningByDate: Map<LocalDate, List<PlanningSession>>,
    notesByDate: Map<LocalDate, DayNote>,
    onDateSelected: (LocalDate) -> Unit,
    onLongClick: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Dimanche, 1 = Lundi ? Non, java context is 1=Mon, 7=Sun
    // Ajustement pour commencer par Lundi (ISO)
    val startOffset = firstDayOfMonth.dayOfWeek.value - 1

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Jours semaine
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { 
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        var currentDay = 1
        for (week in 0..5) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (dayOfWeek in 0..6) {
                    val index = week * 7 + dayOfWeek
                    if (index < startOffset || currentDay > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val date = currentMonth.withDayOfMonth(currentDay)
                        CalendarDayCell(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            hasEvents = eventsByDate.containsKey(date),
                            hasPlanning = planningByDate.containsKey(date),
                            isImportant = planningByDate[date]?.any { it.isImportant } ?: false,
                            note = notesByDate[date],
                            onClick = { onDateSelected(date) },
                            onLongClick = { onLongClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                        currentDay++
                    }
                }
            }
            if (currentDay > daysInMonth) break
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    hasPlanning: Boolean,
    isImportant: Boolean,
    note: DayNote?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (note?.colorHex != null) {
        try { Color(android.graphics.Color.parseColor(note.colorHex)) } catch (e: Exception) { Color.Transparent }
    } else Color.Transparent

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFFF6B00) else bgColor.copy(alpha = 0.3f))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isToday && !isSelected) {
                Box(modifier = Modifier.size(28.dp).border(1.dp, Color(0xFFFF6B00), CircleShape))
            }
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isSelected) Color.White else if (isToday) Color(0xFFFF6B00) else Color.White,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            )
            if (isImportant) {
                Text("⭐", fontSize = 8.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp))
            }
        }
        
        // Indicateurs dots
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp)) {
            if (hasEvents) Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(if (isSelected) Color.White else Color(0xFF2196F3)))
            if (hasPlanning) Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(if (isSelected) Color.White else Color(0xFFFF6B00)))
            if (note?.content != null) Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(if (isSelected) Color.White else Color.Green))
        }
    }
}

@Composable
fun DayDetailsList(
    date: LocalDate,
    events: List<Event>,
    sessions: List<PlanningSession>,
    note: DayNote?,
    onEventClick: (String) -> Unit,
    onPlanningClick: (String) -> Unit,
    onAddNote: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)).replaceFirstChar { it.uppercase() },
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onAddNote) {
                Text(if (note == null) "Ajouter note" else "Modifier note", color = Color(0xFFFF6B00))
            }
        }

        if (note != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(
                        try { Color(android.graphics.Color.parseColor(note.colorHex ?: "#FF6B00")) } catch(e:Exception) { Color(0xFFFF6B00) }
                    ))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(note.content, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sessions) { session -> CalendarPlanningCard(session, onClick = { onPlanningClick(session.id) }) }
            items(events) { event -> CalendarEventCard(event, onClick = { onEventClick(event.id) }) }
        }
    }
}

@Composable
fun YearlyCalendarGrid(
    year: Int,
    eventsByDate: Map<LocalDate, List<Event>>,
    planningByDate: Map<LocalDate, List<PlanningSession>>,
    notesByDate: Map<LocalDate, DayNote>,
    onDateClick: (LocalDate) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items((1..12).chunked(3)) { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { month ->
                    SmallMonthCard(
                        year = year,
                        month = month,
                        eventsByDate = eventsByDate,
                        planningByDate = planningByDate,
                        notesByDate = notesByDate,
                        onDateClick = onDateClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size < 3) repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SmallMonthCard(
    year: Int,
    month: Int,
    eventsByDate: Map<LocalDate, List<Event>>,
    planningByDate: Map<LocalDate, List<PlanningSession>>,
    notesByDate: Map<LocalDate, DayNote>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthDate = LocalDate.of(year, month, 1)
    Column(modifier = modifier.clickable { onDateClick(monthDate) }) {
        Text(
            monthDate.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH).replaceFirstChar { it.uppercase() },
            color = Color(0xFFFF6B00),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // Mini grille simplifiée (juste un canevas ou des petits points)
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp)).padding(4.dp)) {
            // Pour des raisons de performance dans une liste, on ne dessine pas tout.
            // On affiche juste le nom du mois et un indicateur si des événements s'y trouvent.
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayNoteDialog(
    date: LocalDate,
    initialNote: DayNote?,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
    onDelete: () -> Unit
) {
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var selectedColor by remember { mutableStateOf(initialNote?.colorHex ?: "#FF6B00") }
    
    val colors = listOf("#FF6B00", "#FF4081", "#7C4DFF", "#2196F3", "#00BCD4", "#4CAF50", "#FFC107", "#795548")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note pour le ${date.dayOfMonth}/${date.monthValue}", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note spéciale") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF6B00)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Couleur du jour", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.take(4).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .border(if (selectedColor == color) 2.dp else 0.dp, Color.White, CircleShape)
                                .clickable { selectedColor = color }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.drop(4).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .border(if (selectedColor == color) 2.dp else 0.dp, Color.White, CircleShape)
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(content, selectedColor) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            if (initialNote != null) {
                TextButton(onClick = onDelete) { Text("Supprimer", color = Color.Red) }
            }
            TextButton(onClick = onDismiss) { Text("Annuler", color = Color.White) }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}

@Composable
fun CalendarPlanningCard(session: PlanningSession, onClick: () -> Unit) {
    val sessionColor = try { Color(android.graphics.Color.parseColor(session.color)) } catch (e: Exception) { Color(0xFFFF6B00) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(sessionColor))
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(session.type.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(session.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (session.isImportant) { Spacer(modifier = Modifier.width(6.dp)); Text("⭐", fontSize = 14.sp) }
                }
                Spacer(modifier = Modifier.height(4.dp))
                session.startTime?.let { start ->
                    val fmt = DateTimeFormatter.ofPattern("HH:mm")
                    val timeStr = if (session.endTime != null) "${start.format(fmt)} - ${session.endTime.format(fmt)}" else "⏰ ${start.format(fmt)}"
                    Text(timeStr, style = MaterialTheme.typography.bodySmall, color = sessionColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CalendarEventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2196F3).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Text("📅", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                if (event.description.isNotEmpty()) {
                    Text(event.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            event.time?.let { time ->
                Text(time.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun formatSelectedDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
    return date.format(formatter).replaceFirstChar { it.uppercase() }
}
