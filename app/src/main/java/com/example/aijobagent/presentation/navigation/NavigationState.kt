package com.example.aijobagent.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.*

/**
 * State holder for navigation state.
 */
class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>
) {
    var topLevelRoute: NavKey by topLevelRoute
    val stacksInUse: List<NavKey>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}

/**
 * Create a navigation state.
 * Fixed: rememberNavBackStack must be called in composition scope, not inside remember calculation.
 * NavigationState itself does not need additional remember – its contained state (topLevelRoute, backStacks)
 * are already remembered.
 */
@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>
): NavigationState {
    // Use remember (not rememberSaveable) for NavKey – NavKey is not Bundle-saveable by default
    // and would crash with IllegalArgumentException. NavBackStack already handles its own saveable.
    val topLevelRoute = remember {
        mutableStateOf(startRoute)
    }

    // Each top-level route gets its own back stack. Called directly in composition.
    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return NavigationState(
        startRoute = startRoute,
        topLevelRoute = topLevelRoute,
        backStacks = backStacks
    )
}

/**
 * Convert NavigationState into NavEntries.
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): SnapshotStateList<NavEntry<NavKey>> {

    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider
        )
    }

    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}
