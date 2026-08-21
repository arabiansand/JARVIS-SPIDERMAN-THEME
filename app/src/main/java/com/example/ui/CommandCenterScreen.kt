package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CommandMemory
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.components.GlassmorphicPanel
import com.example.ui.components.HudAmbientBackground
import com.example.util.NetworkStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    memories: List<CommandMemory>,
    networkStatus: NetworkStatus = NetworkStatus.UNAVAILABLE,
    onClearMemory: () -> Unit,
    onSettingsClick: () -> Unit,
    onBack: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("COMMAND CENTER", color = primaryColor, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryColor)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = primaryColor)
                    }
                    IconButton(
                        onClick = onClearMemory,
                        modifier = Modifier.testTag("clear_memory_button")
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear Memory", tint = secondaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HudAmbientBackground(accentColor = primaryColor)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("SYSTEM DIAGNOSTICS", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(14.dp))
                
                // Diagnostics Panel
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        DiagnosticCard(title = "AI ENGINE", status = "ONLINE", color = primaryColor)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val netStatusStr = when(networkStatus) {
                            NetworkStatus.WIFI -> "WIFI"
                            NetworkStatus.CELLULAR -> "CELLULAR"
                            NetworkStatus.AVAILABLE -> "CONNECTED"
                            NetworkStatus.LOSING -> "WEAK"
                            NetworkStatus.LOST, NetworkStatus.UNAVAILABLE -> "OFFLINE"
                        }
                        val netColor = if (networkStatus == NetworkStatus.UNAVAILABLE || networkStatus == NetworkStatus.LOST) secondaryColor else primaryColor
                        DiagnosticCard(title = "NETWORK", status = netStatusStr, color = netColor)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DiagnosticCard(title = "MEMORY", status = "${memories.size} logs", color = primaryColor)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text("COMMAND HISTORY", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (memories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No commands in memory.", color = Color.White.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(memories) { memory ->
                            MemoryItemCard(memory, accentColor = primaryColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticCard(title: String, status: String, color: Color) {
    GlassmorphicPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        shape = RoundedCornerShape(16.dp),
        blurRadius = 16.dp,
        accentColor = color
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(status, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun MemoryItemCard(memory: CommandMemory, accentColor: Color) {
    GlassmorphicPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        blurRadius = 18.dp,
        accentColor = accentColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Q: ${memory.command}", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("A: ${memory.response}", color = accentColor, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

