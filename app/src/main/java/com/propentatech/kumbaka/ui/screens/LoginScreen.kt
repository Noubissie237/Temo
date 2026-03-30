package com.propentatech.kumbaka.ui.screens

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.ui.viewmodel.AuthState
import com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel
import com.propentatech.kumbaka.ui.viewmodel.SecurityViewModelFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private val Orange = Color(0xFFFF6B00)

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val viewModel: SecurityViewModel = viewModel(factory = SecurityViewModelFactory(application.securityPreferences))
    
    val authState by viewModel.authState.collectAsState()
    val pinBuffer by viewModel.pinBuffer.collectAsState()
    val error by viewModel.error.collectAsState()
    val username by viewModel.username.collectAsState()

    LaunchedEffect(authState) {
        if (authState == AuthState.AUTHENTICATED) {
            onAuthenticated()
        }
    }

    // Biometric Prompt (System API)
    val executor = remember { context.mainExecutor }
    val biometricPrompt = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BiometricPrompt.Builder(context)
                .setTitle("Connexion MyLive")
                .setSubtitle("Utilisez votre empreinte pour vous connecter")
                .setNegativeButton("Utiliser le PIN", executor) { _, _ -> }
                .build()
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Orange.copy(alpha = 0.1f))
                    .border(1.dp, Orange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("ML", color = Orange, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Bonjour, $username",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Entrez votre code PIN pour continuer",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            // PIN Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val filled = index < pinBuffer.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (filled) Orange else Color.DarkGray)
                            .border(1.dp, if (filled) Orange else Color.Transparent, CircleShape)
                    )
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(error!!, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(64.dp))

            // NumPad
            NumPad(
                onDigit = { viewModel.onPinDigit(it) },
                onDelete = { viewModel.onPinDelete() },
                showBiometric = viewModel.isFingerprintEnabled(),
                onBiometricClick = { 
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && biometricPrompt != null) {
                        biometricPrompt.authenticate(
                            CancellationSignal(),
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                                    super.onAuthenticationSucceeded(result)
                                    viewModel.login()
                                }
                            }
                        )
                    } else {
                        android.util.Log.e("MyLiveBiometric", "Biométrie non disponible sur cette version d'Android")
                    }
                }
            )
        }
    }
}

@Composable
fun NumPad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    showBiometric: Boolean,
    onBiometricClick: () -> Unit
) {
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("fingerprint", "0", "backspace")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        digits.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) {
                row.forEach { item ->
                    when (item) {
                        "fingerprint" -> {
                            if (showBiometric) {
                                KeyButton(icon = Icons.Default.Fingerprint, onClick = onBiometricClick, color = Orange)
                            } else {
                                Spacer(modifier = Modifier.size(70.dp))
                            }
                        }
                        "backspace" -> KeyButton(icon = Icons.Default.Backspace, onClick = onDelete, color = Color.White)
                        else -> KeyButton(text = item, onClick = { onDigit(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    color: Color = Color.White
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(text, color = color, fontSize = 28.sp, fontWeight = FontWeight.Medium)
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        }
    }
}
