package com.example.aijobagent

import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.JobSource
import com.example.aijobagent.domain.model.Seniority
import com.example.aijobagent.domain.model.UserProfile
import com.example.aijobagent.domain.model.WorkMode
import com.example.aijobagent.presentation.navigation.Navigator
import com.example.aijobagent.presentation.navigation.NavigationState
import com.example.aijobagent.presentation.navigation.Route
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CrashRegressionTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun profileValidation_doesNotCrash_onEmpty() = runTest {
        val profile = UserProfile(fullName = "", email = "", skills = emptyList())
        assertFalse(profile.isComplete())
    }

    @Test fun profileValidation_doesNotCrash_onComplete() = runTest {
        val profile = UserProfile(fullName = "Ahmad", email = "a@b.com", skills = listOf("Kotlin"))
        assertTrue(profile.isComplete())
    }

    @Test fun jobMatching_doesNotCrash_onEmptyRequirements() = runTest {
        val job = Job(id="1", title="Android Dev", company="Test", description="Kotlin job", location="Remote")
        assertEquals(0, job.matchPercentage)
        assertNotNull(job.whyMatches)
    }

    @Test fun navigator_doesNotCrash_onGoBack_emptyStack() {
        val lockStack: NavBackStack<NavKey> = NavBackStack(Route.Lock as NavKey)
        val mainStack: NavBackStack<NavKey> = NavBackStack(Route.Main as NavKey)
        val backStacks = mapOf<NavKey, NavBackStack<NavKey>>(Route.Lock to lockStack, Route.Main to mainStack)
        val state = NavigationState(startRoute = Route.Lock, topLevelRoute = mutableStateOf<NavKey>(Route.Lock), backStacks = backStacks)
        val navigator = Navigator(state)
        navigator.goBack()
        assertEquals(Route.Lock, state.topLevelRoute)
        navigator.navigate(Route.Main)
        assertEquals(Route.Main, state.topLevelRoute)
        navigator.goBack()
        assertEquals(Route.Lock, state.topLevelRoute)
        navigator.navigate(Route.JobDetail("123"))
        assertTrue(backStacks[Route.Lock]!!.contains(Route.JobDetail("123")))
        navigator.goBack()
        assertFalse(backStacks[Route.Lock]!!.contains(Route.JobDetail("123")))
    }

    @Test fun navigator_doesNotCrash_onDuplicateNavigate() {
        val lockStack: NavBackStack<NavKey> = NavBackStack(Route.Lock as NavKey)
        val mainStack: NavBackStack<NavKey> = NavBackStack(Route.Main as NavKey)
        val backStacks = mapOf<NavKey, NavBackStack<NavKey>>(Route.Lock to lockStack, Route.Main to mainStack)
        val state = NavigationState(startRoute = Route.Lock, topLevelRoute = mutableStateOf<NavKey>(Route.Lock), backStacks = backStacks)
        val navigator = Navigator(state)
        navigator.navigate(Route.JobDetail("123"))
        navigator.navigate(Route.JobDetail("123"))
        assertEquals(1, lockStack.count { it == Route.JobDetail("123") })
        navigator.navigate(Route.Main)
        navigator.navigate(Route.Main)
        assertEquals(Route.Main, state.topLevelRoute)
    }

    @Test fun navigator_doesNotCrash_onNullStack() {
        val state = NavigationState(startRoute = Route.Lock, topLevelRoute = mutableStateOf<NavKey>(Route.Lock), backStacks = emptyMap())
        val navigator = Navigator(state)
        try { navigator.goBack() } catch (e: Exception) { fail("goBack should not throw") }
        try { navigator.navigate(Route.JobDetail("1")) } catch (e: Exception) { fail("navigate should not throw") }
    }

    @Test fun jobModel_handlesAllSources() {
        JobSource.entries.forEach { source ->
            val job = Job(id="1", title="T", company="C", description="D", location="L", source = source)
            assertEquals(source, job.source)
        }
        WorkMode.entries.forEach { mode ->
            val job = Job(id="1", title="T", company="C", description="D", location="L", workMode = mode)
            assertEquals(mode, job.workMode)
        }
        Seniority.entries.forEach { s ->
            val job = Job(id="1", title="T", company="C", description="D", location="L", seniority = s)
            assertEquals(s, job.seniority)
        }
    }
}
