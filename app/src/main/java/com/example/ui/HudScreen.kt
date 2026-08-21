package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.JarvisState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GraphiteBase
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.RedAccent
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.components.GlassmorphicPanel
import com.example.ui.components.HudAmbientBackground
import com.example.ui.components.FuturisticParticleBackground

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.testTag

import androidx.camera.view.LifecycleCameraController
import com.example.ui.components.VisionCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HudScreen(
    jarvisState: JarvisState,
    spokenText: String,
    aiResponse: String,
    visionModeEnabled: Boolean = false,
    cameraController: LifecycleCameraController? = null,
    onToggleVisionMode: () -> Unit = {},
    onMicClick: () -> Unit,
    onOpenCommandCenter: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val backgroundColor = MaterialTheme.colorScheme.background

    val activeAccentColor by animateColorAsState(
        targetValue = when (jarvisState) {
            JarvisState.LISTENING -> secondaryColor
            JarvisState.ERROR -> secondaryColor
            JarvisState.SPEAKING -> primaryColor
            JarvisState.THINKING -> tertiaryColor
            else -> primaryColor
        },
        label = "activeAccent"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (visionModeEnabled && cameraPermissionState.status.isGranted && cameraController != null) {
            VisionCamera(controller = cameraController)
        } else {
            // Ambient Futuristic Grid & Light Glows for Glassmorphic Depth
            HudAmbientBackground(accentColor = activeAccentColor)
    
            // Reactive Canvas-Based Glowing Particle Animation System
            FuturisticParticleBackground(
                jarvisState = jarvisState,
                accentColor = activeAccentColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Top Bar in Glassmorphic Panel
            GlassmorphicPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(14.dp),
                blurRadius = 16.dp,
                accentColor = activeAccentColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(activeAccentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "JARVIS: ${jarvisState.name}",
                            color = activeAccentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        "SYSTEM: ONLINE",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Jarvis AI Core
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                JarvisCore(
                    state = jarvisState,
                    primaryAccent = primaryColor,
                    alertAccent = secondaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Subtitles / Logs HUD Glassmorphic Panel
            GlassmorphicPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp, max = 180.dp),
                shape = RoundedCornerShape(20.dp),
                blurRadius = 24.dp,
                accentColor = activeAccentColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (spokenText.isEmpty() && aiResponse.isEmpty()) {
                        Text(
                            "Tap the microphone or say \"Hey JARVIS\"",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        if (spokenText.isNotEmpty()) {
                            Text(
                                "USER > \"$spokenText\"",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (aiResponse.isNotEmpty()) {
                            Text(
                                "JARVIS > $aiResponse",
                                color = activeAccentColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Controls in Glassmorphic Dock
            GlassmorphicPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                blurRadius = 18.dp,
                accentColor = activeAccentColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenCommandCenter,
                        border = BorderStroke(1.dp, activeAccentColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("command_center_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = activeAccentColor.copy(alpha = 0.08f)
                        )
                    ) {
                        Text(
                            "COMMAND CENTER",
                            color = activeAccentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (!cameraPermissionState.status.isGranted) {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                                onToggleVisionMode()
                            },
                            containerColor = if (visionModeEnabled) secondaryColor else backgroundColor,
                            contentColor = if (visionModeEnabled) backgroundColor else primaryColor,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                if (visionModeEnabled) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Vision",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    
                        FloatingActionButton(
                            onClick = onMicClick,
                            containerColor = if (jarvisState == JarvisState.LISTENING) secondaryColor else primaryColor,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("mic_fab_button")
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = "Listen",
                                tint = backgroundColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JarvisCore(
    state: JarvisState,
    primaryAccent: Color = CyanAccent,
    alertAccent: Color = RedAccent
) {
    val transition = rememberInfiniteTransition(label = "core")
    
    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = when (state) {
            JarvisState.LISTENING -> 1.3f
            JarvisState.SPEAKING -> 1.1f
            else -> 1.0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(when (state) {
                JarvisState.LISTENING -> 600
                JarvisState.SPEAKING -> 300
                else -> 1500
            }, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == JarvisState.THINKING) 800 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "time"
    )

    val accentColor = when (state) {
        JarvisState.LISTENING -> alertAccent
        JarvisState.ERROR -> alertAccent
        else -> primaryAccent
    }

    Canvas(modifier = Modifier
        .size(240.dp)
        .scale(scale)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2.5f
        
        // Inner glowing core
        drawCircle(
            color = accentColor.copy(alpha = if (state == JarvisState.LISTENING) 0.4f else 0.2f),
            radius = radius * 0.8f,
            center = center
        )
        
        if (state != JarvisState.SPEAKING) {
            drawCircle(
                color = accentColor,
                radius = radius * 0.5f,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )
        } else {
            // Draw Waveform when Speaking
            val waveWidth = radius * 1.2f
            val numBars = 9
            val barWidth = waveWidth / numBars
            
            for (i in 0 until numBars) {
                val xOffset = -waveWidth / 2f + (i + 0.5f) * barWidth
                // Taper edges with sine
                val normalizedX = (i.toFloat() / (numBars - 1)) * Math.PI.toFloat()
                val heightMultiplier = sin(normalizedX)
                
                // Fast oscillation 
                val barHeight = radius * 0.8f * heightMultiplier * (0.3f + 0.7f * kotlin.math.abs(sin(time * 8f + i * 1.5f)))
                
                drawLine(
                    color = accentColor,
                    start = Offset(center.x + xOffset, center.y - barHeight / 2),
                    end = Offset(center.x + xOffset, center.y + barHeight / 2),
                    strokeWidth = (barWidth * 0.5f),
                    cap = StrokeCap.Round
                )
            }
        }
        
        // Radar ring
        for (i in 0 until 12) {
            val angle = (i * 30 + rotation) * (Math.PI / 180)
            val startX = center.x + (radius * 0.6f) * cos(angle).toFloat()
            val startY = center.y + (radius * 0.6f) * sin(angle).toFloat()
            val endX = center.x + (radius * 0.9f) * cos(angle).toFloat()
            val endY = center.y + (radius * 0.9f) * sin(angle).toFloat()
            drawLine(
                color = accentColor.copy(alpha = 0.7f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // Outer bounds
        drawCircle(
            color = accentColor.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        
        // Extra pulsing outer ring during listening
        if (state == JarvisState.LISTENING) {
            drawCircle(
                color = accentColor.copy(alpha = 0.15f),
                radius = radius * 1.2f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
