package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JarvisNavGraph
import com.example.ui.JarvisViewModel
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: JarvisViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()

            MyApplicationTheme(appTheme = currentTheme) {
                val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
                
                LaunchedEffect(micPermissionState.status.isGranted) {
                    if (micPermissionState.status.isGranted) {
                        viewModel.initializeSpeech()
                    }
                }

                LaunchedEffect(Unit) {
                    if (!micPermissionState.status.isGranted) {
                        micPermissionState.launchPermissionRequest()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JarvisNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}
