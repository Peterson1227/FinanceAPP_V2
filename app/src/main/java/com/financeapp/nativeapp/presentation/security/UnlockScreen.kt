package com.financeapp.nativeapp.presentation.security

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UnlockScreen(message: String, pending: Boolean, credentialAvailable: Boolean,
    onUnlock: () -> Unit, onCredential: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text("FinanceAPP", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(32.dp))
            Icon(Icons.Default.Fingerprint, "Biometria", Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text("Confirme sua identidade para acessar suas finanças",
                textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Text(message, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onUnlock, enabled = !pending) { Text("Desbloquear") }
            if (credentialAvailable) {
                TextButton(onClick = onCredential, enabled = !pending) {
                    Text("Usar PIN, senha ou padrão do dispositivo")
                }
            }
        }
    }
}
