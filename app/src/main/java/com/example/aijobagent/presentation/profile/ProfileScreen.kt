package com.example.aijobagent.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val editable by viewModel.editable.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var skillInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("My Profile", style = MaterialTheme.typography.headlineMedium)
        Text("All data stored locally and encrypted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(value = editable.fullName, onValueChange = { v -> viewModel.updateField { p -> p.copy(fullName = v) } }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = editable.email, onValueChange = { v -> viewModel.updateField { p -> p.copy(email = v) } }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = editable.phoneNumber, onValueChange = { v -> viewModel.updateField { p -> p.copy(phoneNumber = v) } }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = editable.linkedInUrl, onValueChange = { v -> viewModel.updateField { p -> p.copy(linkedInUrl = v) } }, label = { Text("LinkedIn URL") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = editable.gitHubUrl, onValueChange = { v -> viewModel.updateField { p -> p.copy(gitHubUrl = v) } }, label = { Text("GitHub URL") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = editable.preferredCountries.joinToString(", "), onValueChange = { v -> viewModel.updateField { p -> p.copy(preferredCountries = v.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }) } }, label = { Text("Preferred Countries (comma separated)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = editable.preferredJobTitles.joinToString(", "), onValueChange = { v -> viewModel.updateField { p -> p.copy(preferredJobTitles = v.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }) } }, label = { Text("Preferred Job Titles") }, modifier = Modifier.fillMaxWidth())

        Text("Skills", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = skillInput, onValueChange = { skillInput = it }, label = { Text("Add skill") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.addSkill(skillInput); skillInput = "" }) { Icon(Icons.Default.Add, contentDescription = null) }
        }
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            editable.skills.forEach { skill ->
                AssistChip(
                    onClick = { viewModel.removeSkill(skill) },
                    label = { Text(skill) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        OutlinedTextField(
            value = editable.resumeText ?: "",
            onValueChange = { v -> viewModel.updateField { p -> p.copy(resumeText = v) } },
            label = { Text("Resume Text (paste)") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 6
        )
        ResumePdfPicker(
            currentPath = editable.resumePath,
            onPicked = { path, name, text ->
                viewModel.updateField { p -> p.copy(resumePath = path, resumeText = text ?: p.resumeText) }
            }
        )
        Text("Resume PDF stored encrypted locally (AES256_GCM)", style = MaterialTheme.typography.bodySmall)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        BackendSettingsSection()

        if (uiState.error != null) Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
        if (uiState.saved) Text("Saved ✅", color = MaterialTheme.colorScheme.primary)

        Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isSaving) {
            if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Save Profile")
        }
    }
}

@Composable
private fun ResumePdfPicker(
    currentPath: String?,
    onPicked: (String, String, String?) -> Unit
) {
    val context = LocalContext.current
    val fileManager = remember { com.example.aijobagent.core.security.EncryptedFileManager(context) }
    var fileName by remember { mutableStateOf(currentPath?.substringAfterLast("/") ?: "") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val input = context.contentResolver.openInputStream(uri) ?: return@rememberLauncherForActivityResult
                val bytes = input.readBytes()
                input.close()
                val name = "resume_${System.currentTimeMillis()}.pdf"
                val path = fileManager.saveResumeFile(name, bytes)
                // Try extract text (simple)
                val text = try { String(bytes).take(4000) } catch (_: Exception) { null }
                fileName = name
                onPicked(path, name, text)
            } catch (e: Exception) {
                fileName = "Failed: ${e.message}"
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { launcher.launch("application/pdf") }) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Pick Resume PDF")
            }
            if (fileName.isNotBlank()) Text(fileName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
        if (currentPath != null) Text("Encrypted at: $currentPath", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BackendSettingsSection(
    vm: com.example.aijobagent.presentation.settings.SettingsViewModel = hiltViewModel()
) {
    val enabled by vm.backendEnabled.collectAsState()
    val url by vm.backendUrl.collectAsState()
    val key by vm.openAiKey.collectAsState()
    var showKey by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Advanced Settings", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Enable Backend (Spring Boot)", modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = { vm.setBackendEnabled(it) })
            }
            OutlinedTextField(value = url, onValueChange = { vm.setBackendUrl(it) }, label = { Text("Backend URL") }, modifier = Modifier.fillMaxWidth(), enabled = enabled)
            Text("OpenAI API Key (stored encrypted)", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = key,
                onValueChange = { vm.setOpenAiKey(it) },
                label = { Text("sk-...") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = { TextButton(onClick = { showKey = !showKey }) { Text(if (showKey) "Hide" else "Show") } }
            )
            Text("If key is set, AI matching uses GPT-4o-mini; otherwise heuristic. Backend when enabled proxies OpenAI.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
