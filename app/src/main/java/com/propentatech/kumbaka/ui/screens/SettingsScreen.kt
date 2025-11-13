package com.propentatech.kumbaka.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.manager.ExportData
import com.propentatech.kumbaka.notification.NotificationHelper
import com.propentatech.kumbaka.ui.theme.*
import com.propentatech.kumbaka.ui.viewmodel.ThemeViewModel
import com.propentatech.kumbaka.ui.viewmodel.ThemeViewModelFactory
import kotlinx.coroutines.launch

/**
 * Écran des paramètres
 * Permet de gérer les données, l'apparence, les notifications et affiche les infos de l'app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Récupérer le ThemeViewModel
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val themeViewModel: ThemeViewModel = viewModel(
        factory = ThemeViewModelFactory(application.themePreferences)
    )
    
    // Observer l'état du mode sombre
    val darkModeEnabled by themeViewModel.isDarkMode.collectAsState()
    
    // États pour les dialogues
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCleanupDialog by remember { mutableStateOf(false) }
    var exportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var importUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val scope = rememberCoroutineScope()
    val dataManager = application.dataExportImportManager
    
    // État pour les notifications
    var notificationsEnabled by remember { 
        mutableStateOf(NotificationHelper.areNotificationsEnabled(context)) 
    }
    
    // Launcher pour demander la permission de notifications (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsEnabled = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notifications activées", Toast.LENGTH_SHORT).show()
            // Replanifier toutes les notifications
            scope.launch {
                application.eventRepository.rescheduleAllNotifications()
            }
        } else {
            Toast.makeText(context, "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Launcher pour créer un fichier (export)
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            exportUri = it
            showExportDialog = true
        }
    }
    
    // Launcher pour ouvrir un fichier (import)
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            importUri = it
            showImportDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paramètres",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(top = 0.dp)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section DONNÉES
            item {
                SettingsSection(title = "DONNÉES") {
                    SettingsItem(
                        icon = Icons.Default.Share,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = AccentLightBlue,
                        title = "Exporter les données",
                        subtitle = "Sauvegarder vos données",
                        onClick = { 
                            createFileLauncher.launch(dataManager.generateExportFileName())
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.Add,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = AccentLightBlue,
                        title = "Importer les données",
                        subtitle = "Restaurer vos données",
                        onClick = { 
                            openFileLauncher.launch(arrayOf("application/json"))
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.Clear,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = AccentLightBlue,
                        title = "Nettoyer les événements passés",
                        subtitle = "Supprimer les anciens événements",
                        onClick = { showCleanupDialog = true }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        iconTint = MaterialTheme.colorScheme.error,
                        iconBackground = MaterialTheme.colorScheme.errorContainer,
                        title = "Supprimer toutes les données",
                        subtitle = "Action irréversible",
                        onClick = { showDeleteDialog = true }
                    )
                }
            }

            // Section NOTIFICATIONS
            item {
                SettingsSection(title = "NOTIFICATIONS") {
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Notifications,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = AccentLightBlue,
                        title = "Rappels d'événements",
                        subtitle = "Recevoir des notifications pour les événements",
                        isChecked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Demander la permission si Android 13+
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    // Pour les versions antérieures, ouvrir les paramètres système
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            } else {
                                // Ouvrir les paramètres pour désactiver
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                    
                    if (notificationsEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Notification 1 jour avant l'événement\n• Notification 5 minutes avant (si heure définie)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
                        )
                    }
                }
            }

            // Section APPARENCE
            item {
                SettingsSection(title = "APPARENCE") {
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Settings,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = AccentLightBlue,
                        title = "Mode sombre",
                        isChecked = darkModeEnabled,
                        onCheckedChange = { themeViewModel.toggleDarkMode(it) }
                    )
                
                }
            }

            // Section À PROPOS
            item {
                SettingsSection(title = "À PROPOS") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = AccentLightBlue,
                        title = "Version de l'application",
                        subtitle = "1.0.0",
                        onClick = { }
                    )
                }
            }
        }
    }
    
    // Dialogues Export/Import/Delete
    if (showExportDialog && exportUri != null) {
        ExportDataDialog(
            onDismiss = { showExportDialog = false },
            onExport = { exportData ->
                showExportDialog = false
                scope.launch {
                    try {
                        context.contentResolver.openOutputStream(exportUri!!)?.use { outputStream ->
                            val result = dataManager.exportData(outputStream, exportData)
                            result.onSuccess { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }.onFailure { error ->
                                Toast.makeText(context, "Erreur: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
    
    if (showImportDialog && importUri != null) {
        ImportDataDialog(
            onDismiss = { showImportDialog = false },
            onImport = { importData ->
                showImportDialog = false
                scope.launch {
                    try {
                        context.contentResolver.openInputStream(importUri!!)?.use { inputStream ->
                            val result = dataManager.importData(inputStream, importData)
                            result.onSuccess { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }.onFailure { error ->
                                Toast.makeText(context, "Erreur: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
    
    if (showCleanupDialog) {
        CleanupPastEventsDialog(
            onDismiss = { showCleanupDialog = false },
            onCleanup = { option ->
                showCleanupDialog = false
                scope.launch {
                    val count = when (option) {
                        com.propentatech.kumbaka.data.manager.CleanupOption.ALL -> 
                            application.eventCleanupManager.deleteAllPastEvents()
                        else -> 
                            application.eventCleanupManager.deletePastEventsOlderThan(option.days)
                    }
                    val message = if (count > 0) {
                        "$count événement(s) supprimé(s)"
                    } else {
                        "Aucun événement à supprimer"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    if (showDeleteDialog) {
        DeleteAllDataDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                scope.launch {
                    application.taskRepository.deleteAllTasks()
                    application.noteRepository.deleteAllNotes()
                    application.eventRepository.deleteAllEvents()
                    Toast.makeText(context, "Toutes les données ont été supprimées", Toast.LENGTH_SHORT).show()
                }
                showDeleteDialog = false
            }
        )
    }
}

/**
 * Dialogue de sélection des données à exporter
 */
@Composable
fun ExportDataDialog(
    onDismiss: () -> Unit,
    onExport: (ExportData) -> Unit
) {
    var includeTasks by remember { mutableStateOf(true) }
    var includeNotes by remember { mutableStateOf(true) }
    var includeEvents by remember { mutableStateOf(true) }
    
    val isValid = includeTasks || includeNotes || includeEvents
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Exporter les données",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sélectionnez les données à exporter :",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Checkbox Tâches
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeTasks = !includeTasks }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeTasks,
                        onCheckedChange = { includeTasks = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tâches",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Checkbox Notes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeNotes = !includeNotes }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeNotes,
                        onCheckedChange = { includeNotes = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Checkbox Événements
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeEvents = !includeEvents }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeEvents,
                        onCheckedChange = { includeEvents = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Événements",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                if (!isValid) {
                    Text(
                        text = "Sélectionnez au moins une option",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onExport(ExportData(includeTasks, includeNotes, includeEvents))
                },
                enabled = isValid
            ) {
                Text("Exporter")
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
 * Dialogue de sélection des données à importer
 */
@Composable
fun ImportDataDialog(
    onDismiss: () -> Unit,
    onImport: (ExportData) -> Unit
) {
    var includeTasks by remember { mutableStateOf(true) }
    var includeNotes by remember { mutableStateOf(true) }
    var includeEvents by remember { mutableStateOf(true) }
    
    val isValid = includeTasks || includeNotes || includeEvents
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Importer les données",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sélectionnez les données à importer :",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Les données seront fusionnées avec vos données existantes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Checkbox Tâches
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeTasks = !includeTasks }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeTasks,
                        onCheckedChange = { includeTasks = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tâches",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Checkbox Notes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeNotes = !includeNotes }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeNotes,
                        onCheckedChange = { includeNotes = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Checkbox Événements
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeEvents = !includeEvents }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeEvents,
                        onCheckedChange = { includeEvents = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Événements",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                if (!isValid) {
                    Text(
                        text = "Sélectionnez au moins une option",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onImport(ExportData(includeTasks, includeNotes, includeEvents))
                },
                enabled = isValid
            ) {
                Text("Importer")
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
 * Dialogue de confirmation de suppression de toutes les données
 */
@Composable
fun DeleteAllDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Supprimer toutes les données ?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Cette action supprimera définitivement toutes vos tâches, notes et événements. Cette action est irréversible.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Supprimer tout")
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
 * Section de paramètres avec titre
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Item de paramètre cliquable
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (subtitle == null) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Item de paramètre avec switch
 */
@Composable
fun SettingsItemWithSwitch(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * Dialogue de nettoyage des événements passés
 */
@Composable
fun CleanupPastEventsDialog(
    onDismiss: () -> Unit,
    onCleanup: (com.propentatech.kumbaka.data.manager.CleanupOption) -> Unit
) {
    var selectedOption by remember { mutableStateOf(com.propentatech.kumbaka.data.manager.CleanupOption.ALL) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Clear,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Nettoyer les événements passés",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sélectionnez les événements à supprimer :",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Options de nettoyage
                com.propentatech.kumbaka.data.manager.CleanupOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = option }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == option,
                            onClick = { selectedOption = option }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Text(
                    text = "Cette action est irréversible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCleanup(selectedOption) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Nettoyer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
