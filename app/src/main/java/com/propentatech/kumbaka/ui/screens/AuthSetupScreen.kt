package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel
import com.propentatech.kumbaka.ui.viewmodel.SecurityViewModelFactory

private val Orange = Color(0xFFFF6B00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: SecurityViewModel = viewModel(factory = SecurityViewModelFactory(application.securityPreferences))
    
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var useFingerprint by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Sécurisez MyLive",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Orange
            )
            Text(
                "Créez votre profil pour protéger vos données",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Votre Nom") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Orange) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PIN
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                label = { Text("Code PIN (4 chiffres)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Orange) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm PIN
            OutlinedTextField(
                value = confirmPin,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPin = it },
                label = { Text("Confirmer le code PIN") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Orange) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useFingerprint, onCheckedChange = { useFingerprint = it }, colors = CheckboxDefaults.colors(checkedColor = Orange))
                Text("Activer l'empreinte digitale", color = Color.White, fontSize = 14.sp)
            }

            if (error != null) {
                Text(error!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (username.isEmpty()) {
                        error = "Le nom est obligatoire"
                    } else if (pin.length < 4) {
                        error = "Le PIN doit faire 4 chiffres"
                    } else if (pin != confirmPin) {
                        error = "Les codes PIN ne correspondent pas"
                    } else {
                        viewModel.setupSecurity(username, pin)
                        viewModel.setFingerprintEnabled(useFingerprint)
                        onSetupComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text("C'est parti", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}
