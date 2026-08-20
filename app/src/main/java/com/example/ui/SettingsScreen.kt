package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.data.AppTheme
import com.example.ui.components.GlassmorphicPanel
import com.example.ui.components.HudAmbientBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GraphiteBase
import com.example.ui.theme.RedAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppTheme = AppTheme.MIDNIGHT_BLUE,
    onThemeSelected: (AppTheme) -> Unit = {},
    onBack: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", color = primaryColor, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // THEME SELECTOR SECTION
                Text(
                    text = "HUD THEME PREFERENCE",
                    color = primaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                AppTheme.values().forEach { theme ->
                    val isSelected = currentTheme == theme
                    val themeAccent = when (theme) {
                        AppTheme.MIDNIGHT_BLUE -> CyanAccent
                        AppTheme.CYBER_RED -> RedAccent
                        AppTheme.MATRIX_EMERALD -> EmeraldAccent
                        AppTheme.SOLAR_GOLD -> GoldAccent
                    }
                    val themeSecondary = when (theme) {
                        AppTheme.MIDNIGHT_BLUE -> RedAccent
                        AppTheme.CYBER_RED -> CyanAccent
                        AppTheme.MATRIX_EMERALD -> CyanAccent
                        AppTheme.SOLAR_GOLD -> Color(0xFFFF5252)
                    }

                    GlassmorphicPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("theme_option_${theme.name.lowercase()}")
                            .clickable { onThemeSelected(theme) },
                        shape = RoundedCornerShape(16.dp),
                        blurRadius = 18.dp,
                        accentColor = if (isSelected) themeAccent else primaryColor.copy(alpha = 0.3f),
                        borderColor = if (isSelected) themeAccent else Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Color preview swatches
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(themeAccent)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(themeSecondary)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = theme.displayName,
                                        color = if (isSelected) themeAccent else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = theme.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Selection Indicator
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) themeAccent else TextSecondary.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .background(if (isSelected) themeAccent.copy(alpha = 0.2f) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = themeAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // API CONFIGURATION
                GlassmorphicPanel(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    blurRadius = 18.dp,
                    accentColor = primaryColor
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("API CONFIGURATION", color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Gemini API Key is managed securely via the AI Studio Secrets panel. DataStore preserves theme preferences locally.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // PRIVACY & SECURITY
                GlassmorphicPanel(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    blurRadius = 18.dp,
                    accentColor = primaryColor
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PRIVACY & SECURITY", color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "JARVIS only listens when the microphone is explicitly activated. All voice command history is stored locally in SQLite Room database.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
