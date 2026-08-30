package com.example.aijobagent.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.aijobagent.presentation.coverletter.CoverLetterScreen
import com.example.aijobagent.presentation.coverletter.CoverLetterViewModel
import com.example.aijobagent.presentation.interview.InterviewScreen
import com.example.aijobagent.presentation.interview.InterviewViewModel
import com.example.aijobagent.presentation.jobs.JobDetailScreen
import com.example.aijobagent.presentation.jobs.JobDetailViewModel
import com.example.aijobagent.presentation.main.MainScaffold
import com.example.aijobagent.presentation.security.LockScreen
import com.example.aijobagent.presentation.security.PinSetupScreen

@Composable
fun AppNavigation() {
    val topLevelRoutes = remember { setOf(Route.Lock, Route.Main) }
    val navigationState = rememberNavigationState(
        startRoute = Route.Lock,
        topLevelRoutes = topLevelRoutes
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val entryProvider = entryProvider<androidx.navigation3.runtime.NavKey> {
        entry<Route.Lock> {
            LockScreen(
                onUnlocked = { navigator.navigate(Route.Main) }
            )
        }
        entry<Route.PinSetup> {
            PinSetupScreen(onDone = { navigator.navigate(Route.Main) })
        }
        entry<Route.Main> {
            MainScaffold(
                onJobClick = { jobId -> navigator.navigate(Route.JobDetail(jobId)) }
            )
        }
        entry<Route.JobDetail> { key ->
            val vm: JobDetailViewModel = hiltViewModel()
            LaunchedEffect(key.jobId) { vm.setJobId(key.jobId) }
            JobDetailScreen(
                viewModel = vm,
                onBack = { navigator.goBack() },
                onCoverLetter = { jobId -> navigator.navigate(Route.CoverLetter(jobId)) },
                onInterview = { jobId -> navigator.navigate(Route.Interview(jobId)) }
            )
        }
        entry<Route.CoverLetter> { key ->
            val vm: CoverLetterViewModel = hiltViewModel()
            LaunchedEffect(key.jobId) { vm.setJobId(key.jobId) }
            CoverLetterScreen(viewModel = vm, onBack = { navigator.goBack() })
        }
        entry<Route.Interview> { key ->
            val vm: InterviewViewModel = hiltViewModel()
            LaunchedEffect(key.jobId) { vm.setJobId(key.jobId) }
            InterviewScreen(viewModel = vm, onBack = { navigator.goBack() })
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}
