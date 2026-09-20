package com.example.aijobagent.core.network

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(val monitor: NetworkMonitor) : ViewModel()

@Composable
fun OfflineBanner(modifier: Modifier = Modifier, vm: NetworkViewModel = hiltViewModel()) {
    val online by vm.monitor.isOnline.collectAsState()
    if (!online) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = modifier.fillMaxWidth()) {
            Text("Offline — showing cached jobs. Some features need internet.", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}
