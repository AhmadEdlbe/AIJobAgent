package com.example.aijobagent.presentation.main

import androidx.compose.runtime.Composable

@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {
    MainScaffold(onJobClick = {})
}
