package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.ai.JarvisState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

private class Particle(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var baseRadius: Float = 3f,
    var radius: Float = 3f,
    var alpha: Float = 0.5f,
    var baseAlpha: Float = 0.5f,
    var angle: Float = 0f,
    var distance: Float = 0f,
    var orbitSpeed: Float = 0.02f,
    var radialSpeed: Float = 0f,
    var phase: Float = 0f
)

/**
 * High-performance Canvas-based particle animation system that visually reacts
 * to JARVIS's active states (IDLE, LISTENING, THINKING, SPEAKING, ERROR).
 */
@Composable
fun FuturisticParticleBackground(
    modifier: Modifier = Modifier,
    jarvisState: JarvisState,
    accentColor: Color = CyanAccent,
    particleCount: Int = 65
) {
    // Remember particles across recompositions to prevent memory allocations in draw loop
    val particles = remember {
        List(particleCount) {
            Particle(
                baseRadius = Random.nextFloat() * 2.5f + 1.5f,
                baseAlpha = Random.nextFloat() * 0.45f + 0.25f,
                angle = Random.nextFloat() * 2f * PI.toFloat(),
                distance = Random.nextFloat() * 450f + 50f,
                orbitSpeed = (Random.nextFloat() * 0.015f + 0.005f) * if (Random.nextBoolean()) 1f else -1f,
                phase = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    var initialized by remember { mutableStateOf(false) }
    var frameTimeNanos by remember { mutableLongStateOf(0L) }

    // Smooth 60 FPS animation frame ticker
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            withFrameNanos { now ->
                if (lastTime != 0L) {
                    frameTimeNanos = now
                }
                lastTime = now
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        // Initialize particle positions on first draw once dimensions are known
        if (!initialized && width > 0 && height > 0) {
            particles.forEach { p ->
                p.x = Random.nextFloat() * width
                p.y = Random.nextFloat() * height
                p.vx = (Random.nextFloat() - 0.5f) * 0.8f
                p.vy = (Random.nextFloat() - 0.5f) * 0.8f
                p.distance = Random.nextFloat() * min(width, height) * 0.45f + 40f
            }
            initialized = true
        }

        if (!initialized) return@Canvas

        val effectiveColor = when (jarvisState) {
            JarvisState.LISTENING -> RedAccent
            JarvisState.ERROR -> RedAccent
            JarvisState.SPEAKING -> CyanAccent
            JarvisState.THINKING -> Color(0xFF64FFDA)
            else -> accentColor
        }

        val t = (frameTimeNanos / 1_000_000L) / 1000f

        // Physics & state-driven particle dynamics
        particles.forEachIndexed { i, p ->
            p.phase += 0.03f

            when (jarvisState) {
                JarvisState.IDLE -> {
                    // Gentle ambient Brownian drift & breathing pulsation
                    p.x += p.vx
                    p.y += p.vy

                    // Wrap around boundaries
                    if (p.x < 0) p.x = width
                    if (p.x > width) p.x = 0f
                    if (p.y < 0) p.y = height
                    if (p.y > height) p.y = 0f

                    p.alpha = p.baseAlpha * (0.6f + 0.4f * sin(p.phase))
                    p.radius = p.baseRadius * (0.8f + 0.2f * sin(p.phase * 1.5f))
                }

                JarvisState.LISTENING -> {
                    // Inward gravitational convergence towards core with energetic audio suction
                    val dx = center.x - p.x
                    val dy = center.y - p.y
                    val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(10f)

                    // Attraction force
                    val force = 2.2f
                    p.vx = (p.vx * 0.94f) + (dx / dist) * force + (Random.nextFloat() - 0.5f) * 0.4f
                    p.vy = (p.vy * 0.94f) + (dy / dist) * force + (Random.nextFloat() - 0.5f) * 0.4f

                    p.x += p.vx
                    p.y += p.vy

                    // Reset particles that collapse into core center
                    if (dist < 40f) {
                        val spawnAngle = Random.nextFloat() * 2f * PI.toFloat()
                        val spawnDist = min(width, height) * (0.35f + Random.nextFloat() * 0.25f)
                        p.x = center.x + cos(spawnAngle) * spawnDist
                        p.y = center.y + sin(spawnAngle) * spawnDist
                        p.vx = 0f
                        p.vy = 0f
                    }

                    p.alpha = (p.baseAlpha * 1.4f).coerceAtMost(1f)
                    p.radius = p.baseRadius * 1.25f
                }

                JarvisState.THINKING -> {
                    // Fast orbital vortex swirling around the AI core
                    p.angle += p.orbitSpeed * 3.5f
                    // Slight orbital breathing
                    val currentDist = p.distance + sin(p.phase * 2f) * 15f
                    p.x = center.x + cos(p.angle) * currentDist
                    p.y = center.y + sin(p.angle) * currentDist

                    p.alpha = (p.baseAlpha * (0.7f + 0.3f * sin(p.angle * 3f + t * 4f))).coerceIn(0.1f, 1f)
                    p.radius = p.baseRadius * (1.1f + 0.3f * sin(p.phase))
                }

                JarvisState.SPEAKING -> {
                    // Outward acoustic shockwave ripples bursting from core
                    val dx = p.x - center.x
                    val dy = p.y - center.y
                    val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

                    val burstSpeed = 3.2f + sin(t * 12f + i) * 1.2f
                    p.vx = (dx / dist) * burstSpeed
                    p.vy = (dy / dist) * burstSpeed

                    p.x += p.vx
                    p.y += p.vy

                    // Respawn near the inner core once propagated outside
                    if (p.x < 0 || p.x > width || p.y < 0 || p.y > height || dist > max(width, height) * 0.6f) {
                        val spawnAngle = Random.nextFloat() * 2f * PI.toFloat()
                        val spawnDist = 45f + Random.nextFloat() * 30f
                        p.x = center.x + cos(spawnAngle) * spawnDist
                        p.y = center.y + sin(spawnAngle) * spawnDist
                    }

                    p.alpha = ((1f - (dist / (max(width, height) * 0.55f))) * 0.9f).coerceIn(0.1f, 0.95f)
                    p.radius = p.baseRadius * (1.2f + 0.4f * sin(t * 10f + i))
                }

                JarvisState.ERROR -> {
                    // Erratic, jittery sparks
                    p.x += (Random.nextFloat() - 0.5f) * 6f
                    p.y += (Random.nextFloat() - 0.5f) * 6f
                    p.alpha = if (Random.nextBoolean()) 0.8f else 0.2f
                    p.radius = p.baseRadius * (0.8f + Random.nextFloat() * 0.8f)
                }
            }
        }

        // Draw Constellation Interconnection Laser Lines in IDLE / THINKING modes
        if (jarvisState == JarvisState.IDLE || jarvisState == JarvisState.THINKING) {
            val maxLinkDist = 95.dp.toPx()
            for (i in 0 until particles.size) {
                val p1 = particles[i]
                for (j in i + 1 until particles.size) {
                    val p2 = particles[j]
                    val dx = p1.x - p2.x
                    val dy = p1.y - p2.y
                    val d2 = dx * dx + dy * dy
                    val maxD2 = maxLinkDist * maxLinkDist

                    if (d2 < maxD2) {
                        val fraction = 1f - (sqrt(d2) / maxLinkDist)
                        val lineAlpha = fraction * 0.18f * min(p1.alpha, p2.alpha)
                        drawLine(
                            color = effectiveColor.copy(alpha = lineAlpha),
                            start = Offset(p1.x, p1.y),
                            end = Offset(p2.x, p2.y),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Draw Inward Energy Streams during LISTENING mode
        if (jarvisState == JarvisState.LISTENING) {
            particles.take(20).forEach { p ->
                val dist = sqrt((center.x - p.x).pow(2) + (center.y - p.y).pow(2))
                if (dist > 50f && dist < 320.dp.toPx()) {
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                effectiveColor.copy(alpha = 0f),
                                effectiveColor.copy(alpha = 0.25f * p.alpha),
                                effectiveColor.copy(alpha = 0.5f * p.alpha)
                            ),
                            start = Offset(p.x, p.y),
                            end = center
                        ),
                        start = Offset(p.x, p.y),
                        end = Offset(
                            p.x + (center.x - p.x) * 0.35f,
                            p.y + (center.y - p.y) * 0.35f
                        ),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Draw Glowing Particles
        particles.forEach { p ->
            val pColor = effectiveColor.copy(alpha = p.alpha)

            // Outer soft atmospheric glow
            drawCircle(
                color = effectiveColor.copy(alpha = p.alpha * 0.25f),
                radius = p.radius * 2.8f,
                center = Offset(p.x, p.y)
            )

            // Core high-intensity particle node
            drawCircle(
                color = pColor,
                radius = p.radius,
                center = Offset(p.x, p.y)
            )

            // Bright specular center
            drawCircle(
                color = Color.White.copy(alpha = (p.alpha * 0.6f).coerceAtMost(0.9f)),
                radius = p.radius * 0.45f,
                center = Offset(p.x, p.y)
            )
        }
    }
}
