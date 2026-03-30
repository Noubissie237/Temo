package com.propentatech.kumbaka.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.data.model.Alarm
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar
import com.propentatech.kumbaka.ui.viewmodel.AlarmViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel = viewModel(),
    onBack: () -> Unit
) {
    val alarms by viewModel.allAlarms.collectAsState(initial = emptyList())
    var showEditDialog by remember { mutableStateOf<Alarm?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = { Text("Alarmes MyLive", color = Color.White, fontWeight = FontWeight.Bold) },
                onNavigateBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEditDialog = Alarm(time = "08:00") },
                containerColor = Color(0xFFFF6B00),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (alarms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune alarme configurée", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onDelete = { viewModel.deleteAlarm(alarm) },
                            onClick = { showEditDialog = alarm }
                        )
                    }
                }
            }
        }

        showEditDialog?.let { alarm ->
            AlarmEditBottomSheet(
                alarm = alarm,
                onDismiss = { showEditDialog = null },
                onSave = { updatedAlarm ->
                    if (alarm.id.isEmpty()) {
                        viewModel.addAlarm(updatedAlarm.copy(id = UUID.randomUUID().toString()))
                    } else {
                        viewModel.updateAlarm(updatedAlarm)
                    }
                    showEditDialog = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditBottomSheet(
    alarm: Alarm,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    var time by remember { mutableStateOf(alarm.time) }
    var label by remember { mutableStateOf(alarm.label) }
    var vibrate by remember { mutableStateOf(alarm.vibrate) }
    var soundUri by remember { mutableStateOf(alarm.soundUri) }
    var soundName by remember { mutableStateOf(alarm.soundName) }
    var selectedDays by remember { mutableStateOf(alarm.daysOfWeek) }
    
    var showTimePicker by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            soundUri = it.toString()
            soundName = "Musique perso" // On pourrait extraire le nom du fichier
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Configurer l'alarme", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)

            // Hour Selection
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Heure", fontWeight = FontWeight.Bold)
                    Text(time, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF6B00))
                }
            }

            // Days Selection
            Column {
                Text("Répéter", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val days = listOf("L", "M", "M", "J", "V", "S", "D")
                    days.forEachIndexed { index, d ->
                        val isSelected = selectedDays[index] == '1'
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFFF6B00) else Color.Gray.copy(alpha = 0.2f))
                                .clickable {
                                    val newDays = selectedDays.toCharArray()
                                    newDays[index] = if (isSelected) '0' else '1'
                                    selectedDays = String(newDays)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(d, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Vibreur", modifier = Modifier.weight(1f))
                    Switch(checked = vibrate, onCheckedChange = { vibrate = it })
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                    audioPickerLauncher.launch("audio/*")
                }) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sonnerie")
                        Text(soundName, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B00))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    onSave(alarm.copy(
                        time = time,
                        label = label,
                        vibrate = vibrate,
                        soundUri = soundUri,
                        soundName = soundName,
                        daysOfWeek = selectedDays
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
            ) {
                Text("Enregistrer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }

    if (showTimePicker) {
        val splitTime = time.split(":")
        val timerState = rememberTimePickerState(
            initialHour = splitTime[0].toInt(),
            initialMinute = splitTime[1].toInt(),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = String.format(Locale.US, "%02d:%02d", timerState.hour, timerState.minute)
                    showTimePicker = false
                }) { Text("OK", color = Color(0xFFFF6B00)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Annuler") }
            },
            text = {
                TimePicker(state = timerState)
            }
        )
    }
}

@Composable
fun AlarmCard(alarm: Alarm, onToggle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.time,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (alarm.isEnabled) Color.White else Color.Gray
                )
                if (alarm.label.isNotBlank()) {
                    Text(alarm.label, fontSize = 14.sp, color = Color.Gray)
                }
                Text(
                    text = formatDays(alarm.daysOfWeek),
                    fontSize = 12.sp,
                    color = if (alarm.isEnabled) Color(0xFFFF6B00) else Color.Gray
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red.copy(alpha = 0.6f))
                }
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF6B00),
                        checkedTrackColor = Color(0xFFFF6B00).copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

fun formatDays(days: String): String {
    if (days == "1111111") return "Tous les jours"
    if (days == "1111100") return "Semaine"
    if (days == "0000011") return "Week-end"
    if (days == "0000000") return "Une fois"
    
    val dayNames = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
    val selected = dayNames.filterIndexed { index, _ -> days[index] == '1' }
    return selected.joinToString(", ")
}
