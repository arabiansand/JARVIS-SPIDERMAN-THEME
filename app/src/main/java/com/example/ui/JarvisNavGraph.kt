package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun JarvisNavGraph(viewModel: JarvisViewModel = viewModel()) {
    val navController = rememberNavController()
    
    val jarvisState by viewModel.jarvisState.collectAsState()
    val spokenText by viewModel.spokenText.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val memories by viewModel.memory.collectAsState(initial = emptyList())
    val currentTheme by viewModel.currentTheme.collectAsState()

    NavHost(navController = navController, startDestination = "hud") {
        composable("hud") {
            HudScreen(
                jarvisState = jarvisState,
                spokenText = spokenText,
                aiResponse = aiResponse,
                onMicClick = { viewModel.startListening() },
                onOpenCommandCenter = { navController.navigate("command_center") }
            )
        }
        
        composable("command_center") {
            CommandCenterScreen(
                memories = memories,
                onClearMemory = { viewModel.clearMemory() },
                onSettingsClick = { navController.navigate("settings") },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                currentTheme = currentTheme,
                onThemeSelected = { viewModel.setTheme(it) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
