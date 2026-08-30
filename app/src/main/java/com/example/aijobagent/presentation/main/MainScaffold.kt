package com.example.aijobagent.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.aijobagent.presentation.dashboard.DashboardScreen
import com.example.aijobagent.presentation.jobs.JobListScreen
import com.example.aijobagent.presentation.profile.ProfileScreen
import com.example.aijobagent.presentation.tracker.TrackerScreen

@Composable
fun MainScaffold(
    onJobClick: (String) -> Unit
) {
    var selected by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selected == 0, onClick = { selected = 0 }, icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }, label = { Text("Dashboard") })
                NavigationBarItem(selected = selected == 1, onClick = { selected = 1 }, icon = { Icon(Icons.Default.Work, contentDescription = null) }, label = { Text("Jobs") })
                NavigationBarItem(selected = selected == 2, onClick = { selected = 2 }, icon = { Icon(Icons.Default.TrackChanges, contentDescription = null) }, label = { Text("Tracker") })
                NavigationBarItem(selected = selected == 3, onClick = { selected = 3 }, icon = { Icon(Icons.Default.Person, contentDescription = null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
            when (selected) {
                0 -> DashboardScreen(onJobClick = onJobClick)
                1 -> JobListScreen(onJobClick = onJobClick)
                2 -> TrackerScreen()
                3 -> ProfileScreen()
            }
        }
    }
}
