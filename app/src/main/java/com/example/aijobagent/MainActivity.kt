package com.example.aijobagent

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import com.example.aijobagent.presentation.navigation.AppNavigation
import com.example.aijobagent.ui.theme.AIJobAgentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIJobAgentTheme {
                AppNavigation()
            }
        }
    }
}
