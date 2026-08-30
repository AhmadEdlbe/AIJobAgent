package com.example.aijobagent.presentation.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.aijobagent.domain.model.ApplicationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel = hiltViewModel()
) {
    val apps by viewModel.applications.collectAsState()
    var filter by remember { mutableStateOf<ApplicationStatus?>(null) }

    val filtered = if (filter == null) apps else apps.filter { it.status == filter }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Application Tracker") })
        // Filter chips
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
            ApplicationStatus.entries.forEach { s ->
                FilterChip(selected = filter == s, onClick = { filter = s }, label = { Text(s.name) })
            }
        }
        Spacer(Modifier.height(8.dp))
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No applications. Jobs you save or apply will appear here.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { app ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(app.job?.title ?: app.jobId, style = MaterialTheme.typography.titleMedium)
                            Text(app.job?.company ?: "", style = MaterialTheme.typography.bodyMedium)
                            Text("Status: ${app.status.name}", color = MaterialTheme.colorScheme.primary)
                            Text("Updated: ${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(app.updatedAt))}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    Button(onClick = { expanded = true }) { Text("Change Status") }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        ApplicationStatus.entries.forEach { s ->
                                            DropdownMenuItem(text = { Text(s.name) }, onClick = { viewModel.updateStatus(app.id, s); expanded = false })
                                        }
                                    }
                                }
                                OutlinedButton(onClick = { viewModel.delete(app.id) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}
