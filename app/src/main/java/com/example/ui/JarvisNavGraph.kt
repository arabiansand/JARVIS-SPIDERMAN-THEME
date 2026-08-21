package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.captureImage

@Composable
fun JarvisNavGraph(viewModel: JarvisViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val cameraController = rememberCameraController(context)
    
    val jarvisState by viewModel.jarvisState.collectAsState()
    val spokenText by viewModel.spokenText.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val memories by viewModel.memory.collectAsState(initial = emptyList())
    val currentTheme by viewModel.currentTheme.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isHotwordEnabled by viewModel.isHotwordEnabled.collectAsState()
    val isBackgroundServiceEnabled by viewModel.isBackgroundServiceEnabled.collectAsState()
    
    val visionModeEnabled by viewModel.visionModeEnabled.collectAsState()
    val captureTrigger by viewModel.captureTrigger.collectAsState()

    LaunchedEffect(captureTrigger) {
        if (captureTrigger > 0 && visionModeEnabled) {
            captureImage(context, cameraController) { bitmap ->
                viewModel.onImageCaptured(bitmap)
            }
        }
    }

    NavHost(navController = navController, startDestination = "hud") {
        composable("hud") {
            HudScreen(
                jarvisState = jarvisState,
                spokenText = spokenText,
                aiResponse = aiResponse,
                visionModeEnabled = visionModeEnabled,
                cameraController = cameraController,
                isHotwordEnabled = isHotwordEnabled,
                onToggleVisionMode = { viewModel.toggleVisionMode() },
                onToggleHotwordMode = { viewModel.toggleHotwordMode() },
                onMicClick = { viewModel.startListening() },
                onOpenCommandCenter = { navController.navigate("command_center") }
            )
        }
        
        composable("command_center") {
            CommandCenterScreen(
                memories = memories,
                networkStatus = networkStatus,
                onClearMemory = { viewModel.clearMemory() },
                onSettingsClick = { navController.navigate("settings") },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                currentTheme = currentTheme,
                isHotwordEnabled = isHotwordEnabled,
                isBackgroundServiceEnabled = isBackgroundServiceEnabled,
                onThemeSelected = { viewModel.setTheme(it) },
                onToggleHotword = { viewModel.toggleHotwordMode() },
                onToggleBackgroundService = { viewModel.toggleBackgroundService() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
