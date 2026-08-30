package com.example.aijobagent.presentation.coverletter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverLetterScreen(
    onBack: () -> Unit,
    viewModel: CoverLetterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text("Cover Letter") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } })
    }) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(state.job?.title ?: "", style = MaterialTheme.typography.titleMedium)
            Text(state.job?.company ?: "", style = MaterialTheme.typography.bodyMedium)
            Divider()
            val letter = state.letter
            if (letter == null) {
                Text("No cover letter yet. Generate one with AI.")
                Button(onClick = { viewModel.generate() }, modifier = Modifier.fillMaxWidth(), enabled = !state.isGenerating) {
                    if (state.isGenerating) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Generate Cover Letter")
                }
            } else {
                OutlinedTextField(
                    value = state.editedContent,
                    onValueChange = { viewModel.onEdit(it) },
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    label = { Text("Cover Letter (editable)") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { viewModel.saveEdited() }, modifier = Modifier.weight(1f)) { Text("Save") }
                    OutlinedButton(onClick = { viewModel.generate() }, modifier = Modifier.weight(1f), enabled = !state.isGenerating) { Text("Regenerate") }
                }
                if (letter.isEdited) Text("Edited", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Text("⚠️ Never submit automatically. You must approve every application manually.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
