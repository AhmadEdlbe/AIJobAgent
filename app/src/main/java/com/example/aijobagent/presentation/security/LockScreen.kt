package com.example.aijobagent.presentation.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    viewModel: LockViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val unlocked by viewModel.unlocked.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(unlocked) {
        if (unlocked) {
            onUnlocked()
            viewModel.resetUnlock()
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // If no PIN required, this screen shouldn't be shown, but handle
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("AI Job Agent Locked", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Enter PIN to unlock", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = state.pin,
            onValueChange = { viewModel.onEvent(LockEvent.PinChanged(it)) },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { viewModel.onEvent(LockEvent.SubmitPin) }, modifier = Modifier.fillMaxWidth()) {
            Text("Unlock")
        }
        if (state.isBiometricAvailable) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    val activity = context as? FragmentActivity ?: return@OutlinedButton
                    val helper = (viewModel as? Any) // We need BiometricHelper via viewModel, but we can get via composition? Instead inject? Simpler: use BiometricHelper directly via helper inside viewModel.
                    // To avoid complexity, just try biometric via viewModel event; viewModel already has helper.
                    // We'll call helper via viewModel's exposed method? For now trigger via helper from screen.
                    // Workaround: get BiometricHelper via Hilt? Instead recreate helper here.
                    // Easiest: call viewModel.onEvent(BiometricSuccess) after helper callback.
                    // Use BiometricHelper from viewModel? We'll just instantiate.
                    // For simplicity, bypass and directly use androidx.biometric
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Use Biometric")
            }
            // Actual biometric trigger: we can use a button that directly triggers viewModel's biometric
            BiometricButton(onSuccess = { viewModel.onEvent(LockEvent.BiometricSuccess) })
        }
    }
}

@Composable
private fun BiometricButton(onSuccess: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var helper: com.example.aijobagent.core.security.BiometricHelper? = null
    // Use compositionLocal? We'll just create helper if available
    Button(
        onClick = {
            if (activity != null) {
                val bm = androidx.biometric.BiometricManager.from(context)
                if (bm.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                    val prompt = androidx.biometric.BiometricPrompt(activity, androidx.core.content.ContextCompat.getMainExecutor(activity),
                        object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) { onSuccess() }
                        })
                    val info = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Unlock AI Job Agent")
                        .setSubtitle("Biometric authentication")
                        .setNegativeButtonText("Cancel")
                        .build()
                    prompt.authenticate(info)
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Fingerprint, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Biometric Unlock")
    }
}
