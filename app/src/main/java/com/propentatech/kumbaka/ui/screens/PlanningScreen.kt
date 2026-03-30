package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.*
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar
import com.propentatech.kumbaka.ui.viewmodel.PlanningViewModel
import com.propentatech.kumbaka.ui.viewmodel.PlanningViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val Orange = Color(0xFFFF6B00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    onCreateSession: () -> Unit,
    onEditSession: (String) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: PlanningViewModel = viewModel(factory = PlanningViewModelFactory(application.planningRepository))

    val allSessions by viewModel.allSessions.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📅 Journée", "📆 Semaine", "📚 Étude", "🔄 Révision")

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = {
                    Text("Planning", fontWeight = FontWeight.Black, color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSession,
                containerColor = Orange,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle séance", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Orange,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> DailyPlanningView(sessions = allSessions.filter { it.type == PlanningType.DAILY }, onEdit = onEditSession, viewModel = viewModel)
                1 -> WeeklyPlanningView(sessions = allSessions, onEdit = onEditSession, viewModel = viewModel)
                2 -> TypedPlanningList(sessions = allSessions.filter { it.type == PlanningType.STUDY }, onEdit = onEditSession, viewModel = viewModel)
                3 -> TypedPlanningList(sessions = allSessions.filter { it.type == PlanningType.REVISION }, onEdit = onEditSession, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DailyPlanningView(sessions: List<PlanningSession>, onEdit: (String) -> Unit, viewModel: PlanningViewModel) {
    val today = LocalDate.now()
    val todaySessions = sessions.filter { it.date == today }.sortedBy { it.startTime }
    val upcoming = sessions.filter { it.date.isAfter(today) }.sortedWith(compareBy({ it.date }, { it.startTime }))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(title = "Aujourd'hui — ${today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH).replaceFirstChar { it.uppercase() }}") }

        if (todaySessions.isEmpty()) {
            item { EmptyPlanningCard() }
        } else {
            items(todaySessions) { session ->
                PlanningSessionCard(session = session, onEdit = { onEdit(session.id) }, onToggle = { viewModel.toggleComplete(session) })
            }
        }

        if (upcoming.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(8.dp)); SectionHeader(title = "À venir") }
            items(upcoming.take(5)) { session ->
                PlanningSessionCard(session = session, onEdit = { onEdit(session.id) }, onToggle = { viewModel.toggleComplete(session) })
            }
        }
    }
}

@Composable
fun WeeklyPlanningView(sessions: List<PlanningSession>, onEdit: (String) -> Unit, viewModel: PlanningViewModel) {
    val today = LocalDate.now()
    val startOfWeek = today.with(DayOfWeek.MONDAY)
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    val sessionsByDate = sessions.groupBy { it.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Semaine du ${startOfWeek.format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(weekDays) { date ->
            WeekDayBlock(
                date = date,
                sessions = sessionsByDate[date] ?: emptyList(),
                isToday = date == today,
                onEdit = onEdit,
                onToggle = { viewModel.toggleComplete(it) }
            )
        }
    }
}

@Composable
fun WeekDayBlock(
    date: LocalDate,
    sessions: List<PlanningSession>,
    isToday: Boolean,
    onEdit: (String) -> Unit,
    onToggle: (PlanningSession) -> Unit
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH).replaceFirstChar { it.uppercase() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(if (isToday) Orange else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayName, fontSize = 10.sp, color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("${date.dayOfMonth}", fontSize = 14.sp, color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (sessions.isEmpty()) {
                    Text("Aucune séance", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("${sessions.size} séance${if (sessions.size > 1) "s" else ""}", fontWeight = FontWeight.SemiBold, color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
            if (sessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                sessions.sortedBy { it.startTime }.forEach { session ->
                    PlanningSessionChip(session = session, onClick = { onEdit(session.id) })
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun PlanningSessionChip(session: PlanningSession, onClick: () -> Unit) {
    val sessionColor = try {
        Color(android.graphics.Color.parseColor(session.color))
    } catch (e: Exception) { Orange }

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(sessionColor.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(sessionColor))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(session.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            session.startTime?.let { start ->
                val fmt = DateTimeFormatter.ofPattern("HH:mm")
                val timeStr = if (session.endTime != null) "${start.format(fmt)} - ${session.endTime.format(fmt)}" else start.format(fmt)
                Text(timeStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (session.isImportant) Text("⭐", fontSize = 14.sp)
    }
}

@Composable
fun TypedPlanningList(sessions: List<PlanningSession>, onEdit: (String) -> Unit, viewModel: PlanningViewModel) {
    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📋", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Aucune séance planifiée", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Appuyez sur + pour créer une séance", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sessions.sortedWith(compareBy({ it.date }, { it.startTime }))) { session ->
                PlanningSessionCard(session = session, onEdit = { onEdit(session.id) }, onToggle = { viewModel.toggleComplete(session) })
            }
        }
    }
}

@Composable
fun PlanningSessionCard(session: PlanningSession, onEdit: () -> Unit, onToggle: () -> Unit) {
    val sessionColor = try { Color(android.graphics.Color.parseColor(session.color)) } catch (e: Exception) { Orange }
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    val dateFmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(0.dp)) {
            // Bandeau couleur latéral
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(sessionColor))
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(session.type.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(session.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (session.isImportant) { Spacer(modifier = Modifier.width(6.dp)); Text("⭐", fontSize = 14.sp) }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(session.date.format(dateFmt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    session.startTime?.let { start ->
                        val timeLabel = if (session.endTime != null) "${start.format(fmt)} → ${session.endTime.format(fmt)}" else "⏰ ${start.format(fmt)}"
                        Text(timeLabel, style = MaterialTheme.typography.bodySmall, color = sessionColor, fontWeight = FontWeight.SemiBold)
                    }
                    if (session.reminderMinutesBefore.isNotEmpty()) {
                        Text("🔔 ${session.reminderMinutesBefore.joinToString(", ") { formatReminder(it) }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Checkbox(
                    checked = session.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = sessionColor)
                )
            }
        }
    }
}

fun formatReminder(mins: Int): String = when (mins) {
    1440 -> "1j"
    60 -> "1h"
    else -> "${mins}min"
}

@Composable
fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
fun EmptyPlanningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Journée libre !", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Profitez ou planifiez quelque chose", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
