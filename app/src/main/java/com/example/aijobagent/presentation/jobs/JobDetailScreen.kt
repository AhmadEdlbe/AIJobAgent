package com.example.aijobagent.presentation.jobs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    onBack: () -> Unit,
    onCoverLetter: (String) -> Unit,
    onInterview: (String) -> Unit,
    viewModel: JobDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Job Detail") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }) }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val job = state.job
        if (job == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("Job not found") }
            return@Scaffold
        }
        val context = LocalContext.current
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(job.title, style = MaterialTheme.typography.headlineSmall)
            Text("${job.company} • ${job.location} • ${job.workMode.name}", style = MaterialTheme.typography.bodyMedium)
            Text("Source: ${job.source.name} | Seniority: ${job.seniority.name}", style = MaterialTheme.typography.labelMedium)
            Text("Salary: ${job.salaryMin?.let { "$${it}-${job.salaryMax} ${job.currency}" } ?: "Not disclosed"}", style = MaterialTheme.typography.bodyMedium)
            if (job.url.isNotBlank()) {
                val annotated = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                        append(job.url)
                    }
                    addStringAnnotation(tag = "URL", annotation = job.url, start = 0, end = job.url.length)
                }
                ClickableText(text = annotated, onClick = { offset ->
                    annotated.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.item)))
                        } catch (_: Exception) {}
                    }
                }, style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider()
            Text("Description", style = MaterialTheme.typography.titleMedium)
            Text(job.description, style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            Text("AI Matching", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(progress = { job.matchPercentage / 100f }, modifier = Modifier.fillMaxWidth())
            Text("Match Score: ${job.matchPercentage}%", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text("Experience Fit: ${job.experienceFit}")
            Text("Salary Fit: ${job.salaryFit}")
            Text("Why matches: ${job.whyMatches}", style = MaterialTheme.typography.bodySmall)
            Text("Why not: ${job.whyNotMatches}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            Text("Matching skills: ${job.matchingSkills.joinToString(", ").ifEmpty { "None" }}")
            Text("Missing skills: ${job.missingSkills.joinToString(", ").ifEmpty { "None" }}")
            HorizontalDivider()
            // Workflow buttons
            when (state.application?.status?.name) {
                "PENDING_APPROVAL" -> {
                    Text("Waiting for your approval to apply", color = MaterialTheme.colorScheme.primary)
                    Button(onClick = { viewModel.approveAndApply() }, modifier = Modifier.fillMaxWidth()) { Text("Approve & Mark Applied") }
                }
                "APPLIED" -> Text("✅ Applied", color = MaterialTheme.colorScheme.primary)
                "SAVED" -> Text("Saved for later")
                else -> {
                    Button(onClick = { viewModel.requestApproval() }, modifier = Modifier.fillMaxWidth()) { Text("Request Approval to Apply") }
                    OutlinedButton(onClick = { viewModel.saveJob() }, modifier = Modifier.fillMaxWidth()) { Text("Save Job") }
                }
            }
            if (state.isGeneratingLetter) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else {
                Button(onClick = { viewModel.generateCoverLetter() }, modifier = Modifier.fillMaxWidth()) { Text("Generate Cover Letter") }
                state.coverLetter?.let { Text(it.content.take(300) + "...", style = MaterialTheme.typography.bodySmall) }
            }
            OutlinedButton(onClick = { onCoverLetter(job.id) }, modifier = Modifier.fillMaxWidth()) { Text("View/Edit Cover Letter") }
            OutlinedButton(onClick = { onInterview(job.id) }, modifier = Modifier.fillMaxWidth()) { Text("Interview Prep") }
            Button(
                onClick = {
                    if (job.url.isNotBlank()) {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(job.url)))
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = job.url.isNotBlank()
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Original Posting")
            }
        }
    }
}
