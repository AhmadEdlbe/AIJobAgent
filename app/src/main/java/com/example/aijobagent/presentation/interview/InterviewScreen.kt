package com.example.aijobagent.presentation.interview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    onBack: () -> Unit,
    viewModel: InterviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text("Interview Prep") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } })
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(state.job?.title ?: "", style = MaterialTheme.typography.titleMedium)
            Text(state.job?.company ?: "", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
                return@Column
            }
            val prep = state.prep
            if (prep == null) {
                Text("Generate AI interview questions tailored to this job.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { viewModel.generate() }, modifier = Modifier.fillMaxWidth(), enabled = !state.isGenerating) {
                    if (state.isGenerating) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Generate Questions")
                }
            } else {
                Button(onClick = { viewModel.generate() }, modifier = Modifier.fillMaxWidth(), enabled = !state.isGenerating) {
                    if (state.isGenerating) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Regenerate")
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(prep.questions) { q ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(q.category.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(q.question, style = MaterialTheme.typography.bodyMedium)
                                Divider()
                                Text("Suggested Answer:", style = MaterialTheme.typography.labelMedium)
                                Text(q.suggestedAnswer, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }
    }
}
