package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.propentatech.kumbaka.data.model.Habit
import com.propentatech.kumbaka.data.model.HabitStatus
import com.propentatech.kumbaka.data.model.UserMood
import com.propentatech.kumbaka.ui.viewmodel.LifestyleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifestyleScreen(
    viewModel: LifestyleViewModel,
    onNavigateBack: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val todayMood by viewModel.todayMood.collectAsState()
    
    var showAddHabit by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitDesc by remember { mutableStateOf("") }
    var newHabitColor by remember { mutableStateOf("#FF6B00") } // Orange par défaut
    
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            com.propentatech.kumbaka.ui.components.PremiumTopAppBar(
                title = { 
                    Text(
                        "Habitudes & Lifestyle", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHabit = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle Habitude")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MoodSelector(
                    currentMood = todayMood?.mood,
                    onMoodSelected = { viewModel.logTodayMood(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Habitudes Quotidiennes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (habits.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Aucune habitude définie.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(habits) { habit ->
                    val isCompleted = todayLogs.any { it.habitId == habit.id && it.status == HabitStatus.COMPLETED }
                    HabitItem(
                        habit = habit,
                        isCompleted = isCompleted,
                        onToggle = { 
                            val newStatus = if (isCompleted) HabitStatus.SKIPPED else HabitStatus.COMPLETED
                            viewModel.toggleHabitToday(habit.id, newStatus)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showAddHabit) {
            ModalBottomSheet(
                onDismissRequest = { showAddHabit = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text("Nouvelle Habitude", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("Nom de l'habitude") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = newHabitDesc,
                        onValueChange = { newHabitDesc = it },
                        label = { Text("Description (optionnel)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (newHabitName.isNotBlank()) {
                                viewModel.addHabit(Habit(
                                    id = java.util.UUID.randomUUID().toString(),
                                    name = newHabitName,
                                    description = newHabitDesc,
                                    colorHex = newHabitColor
                                ))
                                newHabitName = ""
                                newHabitDesc = ""
                                showAddHabit = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
                    ) {
                        Text("Créer l'habitude", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MoodSelector(currentMood: UserMood?, onMoodSelected: (UserMood) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(20.dp)
    ) {
        Text(
            text = "Comment vous sentez-vous aujourd'hui ?",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MoodIcon(UserMood.HAPPY, "😊", currentMood == UserMood.HAPPY) { onMoodSelected(UserMood.HAPPY) }
            MoodIcon(UserMood.NEUTRAL, "😐", currentMood == UserMood.NEUTRAL) { onMoodSelected(UserMood.NEUTRAL) }
            MoodIcon(UserMood.ANXIOUS, "😰", currentMood == UserMood.ANXIOUS) { onMoodSelected(UserMood.ANXIOUS) }
            MoodIcon(UserMood.ANGRY, "😠", currentMood == UserMood.ANGRY) { onMoodSelected(UserMood.ANGRY) }
            MoodIcon(UserMood.EXHAUSTED, "😫", currentMood == UserMood.EXHAUSTED) { onMoodSelected(UserMood.EXHAUSTED) }
        }
    }
}

@Composable
fun MoodIcon(mood: UserMood, emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 28.sp)
    }
}

@Composable
fun HabitItem(habit: Habit, isCompleted: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isCompleted) Color(android.graphics.Color.parseColor(habit.colorHex)) else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = habit.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
            )
            if (habit.description.isNotEmpty()) {
                Text(
                    text = habit.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
