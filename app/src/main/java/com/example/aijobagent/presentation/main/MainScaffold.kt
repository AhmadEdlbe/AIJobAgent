package com.example.aijobagent.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.aijobagent.core.network.OfflineBanner
import com.example.aijobagent.presentation.dashboard.DashboardScreen
import com.example.aijobagent.presentation.jobs.JobListScreen
import com.example.aijobagent.presentation.profile.ProfileScreen
import com.example.aijobagent.presentation.tracker.TrackerScreen

@Composable
fun MainScaffold(
    onJobClick: (String) -> Unit
) {
    var selected by remember { mutableStateOf(0) }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val widthClass = adaptiveInfo.windowSizeClass.windowWidthSizeClass

    // Accessibility: contentDescription for navigation
    val navDesc = when (selected) {
        0 -> "Dashboard selected" ; 1 -> "Jobs selected"; 2 -> "Tracker selected"; else -> "Profile selected"
    }
    Column(modifier = Modifier.fillMaxSize().semantics { contentDescription = navDesc }) {
        OfflineBanner()
        when (widthClass) {
            WindowWidthSizeClass.COMPACT -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(selected = selected == 0, onClick = { selected = 0 }, icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") }, label = { Text("Dashboard") })
                            NavigationBarItem(selected = selected == 1, onClick = { selected = 1 }, icon = { Icon(Icons.Default.Work, contentDescription = "Jobs") }, label = { Text("Jobs") })
                            NavigationBarItem(selected = selected == 2, onClick = { selected = 2 }, icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Tracker") }, label = { Text("Tracker") })
                            NavigationBarItem(selected = selected == 3, onClick = { selected = 3 }, icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }, label = { Text("Profile") })
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        when (selected) {
                            0 -> DashboardScreen(onJobClick = onJobClick)
                            1 -> JobListScreen(onJobClick = onJobClick)
                            2 -> TrackerScreen()
                            3 -> ProfileScreen()
                        }
                    }
                }
            }
            else -> {
                // Rail + content for medium/expanded (tablets/foldables)
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail {
                        NavigationRailItem(selected = selected == 0, onClick = { selected = 0 }, icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") }, label = { Text("Dash") })
                        NavigationRailItem(selected = selected == 1, onClick = { selected = 1 }, icon = { Icon(Icons.Default.Work, contentDescription = "Jobs") }, label = { Text("Jobs") })
                        NavigationRailItem(selected = selected == 2, onClick = { selected = 2 }, icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Tracker") }, label = { Text("Track") })
                        NavigationRailItem(selected = selected == 3, onClick = { selected = 3 }, icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }, label = { Text("Profile") })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        when (selected) {
                            0 -> DashboardScreen(onJobClick = onJobClick)
                            1 -> JobListScreen(onJobClick = onJobClick)
                            2 -> TrackerScreen()
                            3 -> ProfileScreen()
                        }
                    }
                }
            }
        }
    }
}
