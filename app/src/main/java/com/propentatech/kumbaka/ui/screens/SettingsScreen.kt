package com.propentatech.kumbaka.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Écran des paramètres
 * Permet de gérer les données, l'apparence, les notifications et affiche les infos de l'app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    securityViewModel: com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
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
    var pendingExportData by remember { mutableStateOf<ExportData?>(null) }
    
    // État pour le profil
    var showProfileDialog by remember { mutableStateOf(false) }
    val username by securityViewModel.username.collectAsState()
    
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
            // Exporter avec les données sélectionnées précédemment
            pendingExportData?.let { exportData ->
                scope.launch {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            val result = dataManager.exportData(outputStream, exportData)
                            result.onSuccess { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }.onFailure { error ->
                                Toast.makeText(context, "Erreur: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        pendingExportData = null
                    }
                }
            }
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
                        icon = Icons.Default.InsertChartOutlined,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        title = "Statistiques",
                        subtitle = "Voir vos statistiques de tâches",
                        onClick = onNavigateToStatistics
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.CloudUpload,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        title = "Exporter les données",
                        subtitle = "Sauvegarder vos données",
                        onClick = { 
                            showExportDialog = true
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.CloudDownload,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        title = "Importer les données",
                        subtitle = "Restaurer vos données",
                        onClick = { 
                            openFileLauncher.launch(arrayOf("application/json"))
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.AutoDelete,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
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
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
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

            // Section PROFIL (Premium)
            item {
                SettingsSection(title = "MON PROFIL") {
                    SettingsItem(
                        icon = Icons.Default.AccountCircle,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        title = "Gérer mon profil",
                        subtitle = "Changer mon nom ou mon code PIN",
                        onClick = { showProfileDialog = true }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Fingerprint,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        title = "Empreinte digitale",
                        subtitle = "Utiliser la biométrie pour déverrouiller",
                        isChecked = securityViewModel.isFingerprintEnabled(),
                        onCheckedChange = { securityViewModel.setFingerprintEnabled(it) }
                    )
                }
            }

            // Section SAUVEGARDE CLOUD (MyLive Premium)
            item {
                val cloudPrefs = application.cloudPreferences
                var cloudEnabled by remember { mutableStateOf(cloudPrefs.isCloudBackupEnabled()) }
                var accountName by remember { mutableStateOf(cloudPrefs.getGoogleAccountName()) }
                var showAdvancedCloud by remember { mutableStateOf(false) }
                var webClientId by remember { mutableStateOf(cloudPrefs.getGoogleWebClientId() ?: "") }
                val lastBackup = cloudPrefs.getLastBackupTime()
                
                val signInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val data = result.data
                    val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                        cloudPrefs.setGoogleAccountName(account.email)
                        accountName = account.email
                        Toast.makeText(context, "Connecté : ${account.email}", Toast.LENGTH_SHORT).show()
                    } catch (e: com.google.android.gms.common.api.ApiException) {
                        val errorMsg = when (e.statusCode) {
                            com.google.android.gms.common.api.CommonStatusCodes.DEVELOPER_ERROR -> "Erreur de configuration (SHA-1/Package)"
                            com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> "Erreur réseau"
                            com.google.android.gms.common.api.CommonStatusCodes.SIGN_IN_REQUIRED -> "Connexion requise"
                            else -> "Erreur ${e.statusCode}: ${e.message}"
                        }
                        android.util.Log.e("GoogleAuth", "Sign-in failed: ${e.statusCode}", e)
                        Toast.makeText(context, "Échec Google : $errorMsg", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        android.util.Log.e("GoogleAuth", "Sign-in failed", e)
                        Toast.makeText(context, "Erreur inconnue : ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                SettingsSection(title = "SAUVEGARDE CLOUD") {
                    SettingsItem(
                        icon = Icons.Default.Cloud,
                        iconTint = Color(0xFF4285F4), // Google Blue
                        iconBackground = Color(0xFF4285F4).copy(alpha = 0.1f),
                        title = if (accountName == null) "Connecter Google Drive" else "Google Drive connecté",
                        subtitle = accountName ?: "Synchronisez vos données sur le cloud",
                        onClick = {
                            if (accountName == null) {
                                try {
                                    val intent = application.googleDriveManager.getSignInIntent(
                                        webClientId.ifBlank { null }
                                    )
                                    signInLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur lors de l'ouverture de Google : ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Déjà connecté", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Section Avancée Toggle
                    Text(
                        text = if (showAdvancedCloud) "Cacher les paramètres avancés" else "Afficher les paramètres avancés",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { showAdvancedCloud = !showAdvancedCloud }
                    )

                    if (showAdvancedCloud) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("Configuration Google Cloud", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = webClientId,
                                onValueChange = { 
                                    webClientId = it
                                    cloudPrefs.setGoogleWebClientId(it)
                                },
                                label = { Text("Web Client ID (Optionnel)") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("xxxxxx.apps.googleusercontent.com") },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("Infos pour console Google Cloud :", style = MaterialTheme.typography.labelSmall)
                            
                            val packageInfo = "Package: ${context.packageName}"
                            val sha1Info = "SHA-1: FF:63:AE:03:4F:76:0B:D0:CC:1A:03:EB:4B:72:AB:48:01:D9:A0:86"
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(packageInfo, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Package", context.packageName)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Package copié", Toast.LENGTH_SHORT).show()
                                }) { Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp)) }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(sha1Info, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("SHA1", "FF:63:AE:03:4F:76:0B:D0:CC:1A:03:EB:4B:72:AB:48:01:D9:A0:86")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "SHA-1 copié", Toast.LENGTH_SHORT).show()
                                }) { Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp)) }
                            }
                            
                            Button(
                                onClick = {
                                    cloudPrefs.setGoogleAccountName(null)
                                    cloudPrefs.setCloudBackupEnabled(false)
                                    cloudPrefs.setGoogleWebClientId(null)
                                    webClientId = ""
                                    accountName = null
                                    cloudEnabled = false
                                    Toast.makeText(context, "Configuration réinitialisée", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text("Réinitialiser Cloud")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Sync,
                        iconTint = Color(0xFF34A853), // Google Green
                        iconBackground = Color(0xFF34A853).copy(alpha = 0.1f),
                        title = "Sauvegarde automatique",
                        subtitle = if (lastBackup > 0) {
                            val fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            "Dernière synchro : ${java.time.Instant.ofEpochMilli(lastBackup).atZone(java.time.ZoneId.systemDefault()).format(fmt)}"
                        } else "Sauvegarde quotidienne sur Drive",
                        isChecked = cloudEnabled,
                        onCheckedChange = { enabled ->
                            if (accountName != null) {
                                cloudEnabled = enabled
                                cloudPrefs.setCloudBackupEnabled(enabled)
                                if (enabled) {
                                    // Programmer le worker
                                    val constraints = androidx.work.Constraints.Builder()
                                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                                        .build()
                                    val request = androidx.work.PeriodicWorkRequestBuilder<com.propentatech.kumbaka.data.cloud.BackupWorker>(
                                        24, java.util.concurrent.TimeUnit.HOURS
                                    ).setConstraints(constraints).build()
                                    
                                    androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                        "cloud_backup",
                                        androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                                        request
                                    )
                                    Toast.makeText(context, "Sauvegarde planifiée toutes les 24h", Toast.LENGTH_SHORT).show()
                                } else {
                                    androidx.work.WorkManager.getInstance(context).cancelUniqueWork("cloud_backup")
                                }
                            } else {
                                Toast.makeText(context, "Veuillez d'abord connecter votre compte Google", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }

            // Section APPARENCE
            item {
                SettingsSection(title = "APPARENCE") {
                    SettingsItemWithSwitch(
                        icon = Icons.Default.DarkMode,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        title = "Mode sombre",
                        isChecked = darkModeEnabled,
                        onCheckedChange = { themeViewModel.setDarkMode(it) }
                    )
                
                }
            }

            // Section CONTACT & PARTAGE
            item {
                SettingsSection(title = "CONTACT & PARTAGE") {
                    SettingsItem(
                        icon = Icons.Default.Share,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        title = "Partager MyLive",
                        subtitle = "Faites découvrir l'application à vos amis",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Découvrez MyLive, l'assistant omniscient pour booster votre productivité ! Téléchargez l'app ici : https://mylive.app")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.Whatsapp,
                        iconTint = Color(0xFF25D366), // Couleur WhatsApp
                        iconBackground = Color(0xFF25D366).copy(alpha = 0.1f),
                        title = "Nous contacter",
                        subtitle = "Donnez votre avis, proposez une mise à jour, ou posez vos questions",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/+237650970526")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Impossible d'ouvrir WhatsApp",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
            
            // Section À PROPOS
            item {
                SettingsSection(title = "À PROPOS") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        title = "Version de l'application",
                        subtitle = "1.2.0 - MyLive Premium edition",
                        onClick = { }
                    )
                    
                    if (username.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsItem(
                            icon = Icons.Default.Person,
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconBackground = MaterialTheme.colorScheme.primaryContainer,
                            title = "Utilisateur",
                            subtitle = username,
                            onClick = { }
                        )
                    }
                }
            }
        }
    }
    
    // Dialogues Export/Import/Delete/Profile
    if (showProfileDialog) {
        ProfileEditDialog(
            currentUsername = username,
            onDismiss = { showProfileDialog = false },
            onSave = { newName, newPin ->
                if (newName.isNotEmpty()) securityViewModel.updateUsername(newName)
                if (newPin.isNotEmpty()) securityViewModel.updatePin(newPin)
                showProfileDialog = false
                Toast.makeText(context, "Profil mis à jour", Toast.LENGTH_SHORT).show()
            }
        )
    }
    if (showExportDialog) {
        ExportDataDialog(
            onDismiss = { showExportDialog = false },
            onExport = { exportData ->
                showExportDialog = false
                pendingExportData = exportData
                // Lancer le sélecteur de fichier après avoir choisi les données
                createFileLauncher.launch(dataManager.generateExportFileName())
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

/**
 * Dialogue d'édition du profil
 */
@Composable
fun ProfileEditDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentUsername) }
    var pin by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Éditer mon profil", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom d'utilisateur") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("Nouveau PIN (4 chiffres)") },
                    placeholder = { Text("Laisser vide pour ne pas changer") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, pin) }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
