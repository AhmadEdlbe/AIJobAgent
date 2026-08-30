package com.example.aijobagent.presentation.jobs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aijobagent.domain.model.Job

@Composable
fun JobCard(job: Job, onClick: () -> Unit, onFavorite: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(job.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${job.company} • ${job.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${job.source.name} • ${job.workMode.name}", style = MaterialTheme.typography.labelSmall)
                }
                if (onFavorite != null) {
                    IconButton(onClick = onFavorite) {
                        Icon(if (job.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (job.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(job.description, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(job.seniority.name) })
                AssistChip(onClick = {}, label = { Text(job.techStacks.firstOrNull()?.name ?: "General") })
            }
            Spacer(Modifier.height(8.dp))
            // Match score
            LinearProgressIndicator(progress = { job.matchPercentage / 100f }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Match ${job.matchPercentage}%", style = MaterialTheme.typography.labelMedium, color = when {
                    job.matchPercentage >= 75 -> MaterialTheme.colorScheme.primary
                    job.matchPercentage >= 50 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                })
                Text(job.salaryMin?.let { "$${it}-${job.salaryMax}" } ?: "Salary N/A", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
