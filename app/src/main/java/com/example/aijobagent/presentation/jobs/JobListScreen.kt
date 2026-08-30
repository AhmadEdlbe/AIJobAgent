package com.example.aijobagent.presentation.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.aijobagent.domain.model.Seniority
import com.example.aijobagent.domain.model.TechStack
import com.example.aijobagent.domain.model.WorkMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    onJobClick: (String) -> Unit,
    viewModel: JobListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState() // not used filtered
    val filter by viewModel.getFilter().collectAsState()
    val jobs by viewModel.jobsFiltered.collectAsState()

    var search by remember { mutableStateOf(filter.searchQuery) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Jobs") }, actions = {
            IconButton(onClick = { viewModel.scan() }) { Icon(Icons.Default.Refresh, contentDescription = "Scan") }
        })
        // Search
        OutlinedTextField(
            value = search,
            onValueChange = { search = it; viewModel.onSearch(it) },
            label = { Text("Search jobs") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        // Filters
        LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TechStack.entries.toList()) { tech ->
                FilterChip(
                    selected = tech in filter.techStacks,
                    onClick = { viewModel.toggleTech(tech) },
                    label = { Text(tech.name) }
                )
            }
        }
        LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(WorkMode.entries.toList()) { mode ->
                FilterChip(selected = mode in filter.workModes, onClick = { viewModel.toggleWorkMode(mode) }, label = { Text(mode.name) })
            }
            items(Seniority.entries.toList()) { s ->
                FilterChip(selected = s in filter.seniorities, onClick = { viewModel.toggleSeniority(s) }, label = { Text(s.name) })
            }
        }
        Spacer(Modifier.height(8.dp))
        // List
        if (jobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No jobs found")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.scan() }) { Text("Scan Jobs") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(jobs) { job ->
                    JobCard(job = job, onClick = { onJobClick(job.id) }, onFavorite = { viewModel.toggleFavorite(job) })
                }
            }
        }
    }
}
