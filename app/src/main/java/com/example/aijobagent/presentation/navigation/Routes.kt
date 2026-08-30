package com.example.aijobagent.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Lock : Route

    @Serializable
    data object PinSetup : Route

    @Serializable
    data object Main : Route

    @Serializable
    data object Dashboard : Route

    @Serializable
    data object JobList : Route

    @Serializable
    data class JobDetail(val jobId: String) : Route

    @Serializable
    data object Tracker : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class CoverLetter(val jobId: String) : Route

    @Serializable
    data class Interview(val jobId: String) : Route
}
