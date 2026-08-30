package com.example.aijobagent.presentation.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route
        } else {
            // Safely add to current top-level stack; if missing, do not crash
            val stack = state.backStacks[state.topLevelRoute]
            if (stack != null) {
                // Avoid duplicate consecutive entries
                if (stack.lastOrNull() != route) {
                    stack.add(route)
                }
            }
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return

        // NavBackStack always contains at least its root key, isEmpty is rare.
        // Use size check instead of isEmpty for correct back behavior.
        if (currentStack.size <= 1) {
            // At root of a non-start top-level stack, return to start route
            if (state.topLevelRoute != state.startRoute) {
                state.topLevelRoute = state.startRoute
            }
            return
        }

        val currentRoute = try {
            currentStack.last()
        } catch (_: NoSuchElementException) {
            return
        }

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            if (state.topLevelRoute != state.startRoute) {
                state.topLevelRoute = state.startRoute
            }
        } else {
            currentStack.removeLastOrNull()
        }
    }
}