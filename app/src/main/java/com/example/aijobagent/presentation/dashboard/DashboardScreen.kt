package com.example.aijobagent.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.aijobagent.presentation.jobs.JobCard

@Composable
fun DashboardScreen(
    onJobClick: (String) -> Unit,
    onScan: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Stats grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total Jobs", state.stats.totalJobsFound.toString(), Modifier.weight(1f))
            StatCard("High Match", state.stats.highMatchJobs.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Applied", state.stats.applicationsSent.toString(), Modifier.weight(1f))
            StatCard("Interviews", state.stats.interviews.toString(), Modifier.weight(1f))
            StatCard("Offers", state.stats.offers.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("High Match Jobs", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (state.isScanning) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            IconButton(onClick = { viewModel.scanJobs() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Scan")
            }
        }
        if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(8.dp))

        if (state.highMatchJobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("No high-match jobs yet. Tap scan to search.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.scanJobs() }) { Text("Scan Jobs") }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.highMatchJobs) { job ->
                    JobCard(job = job, onClick = { onJobClick(job.id) })
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
