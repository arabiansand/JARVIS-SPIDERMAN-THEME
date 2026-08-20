package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent

/**
 * A futuristic glassmorphic panel container with semi-transparent layered gradients,
 * backdrop blur filter simulation, and holographic edge lighting.
 */
@Composable
fun GlassmorphicPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    blurRadius: Dp = 20.dp,
    accentColor: Color = CyanAccent,
    borderColor: Color = accentColor.copy(alpha = 0.45f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
    ) {
        // 1. Layered blurred chromatic dispersion backdrop (Blur Filter Effect)
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(radius = blurRadius)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.12f),
                            Color(0xFF0F172A).copy(alpha = 0.45f),
                            accentColor.copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 300f)
                    )
                )
        )

        // 2. Semi-transparent frosted glass surface
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E242B).copy(alpha = 0.65f),
                            Color(0xFF12161A).copy(alpha = 0.80f),
                            Color(0xFF0A0D10).copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // 3. Specular top-light sheen & subtle diagonal glass reflection
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(100f, 150f)
                    )
                )
        )

        // 4. Holographic edge stroke
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.5f),
                                borderColor,
                                accentColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(400f, 400f)
                        )
                    ),
                    shape = shape
                )
        )

        // 5. Content inside glass panel
        content()
    }
}

/**
 * Ambient HUD Grid Background that interacts with the glassmorphism blur and transparencies.
 */
@Composable
fun HudAmbientBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = CyanAccent
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Subtle background grid
        val gridSize = 40.dp.toPx()
        val numCols = (width / gridSize).toInt() + 1
        val numRows = (height / gridSize).toInt() + 1

        for (i in 0..numCols) {
            val x = i * gridSize
            drawLine(
                color = accentColor.copy(alpha = 0.03f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
        }

        for (j in 0..numRows) {
            val y = j * gridSize
            drawLine(
                color = accentColor.copy(alpha = 0.03f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Ambient glowing light blooms for the glass panels to refract
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.2f, height * 0.25f),
                radius = 160.dp.toPx()
            ),
            radius = 160.dp.toPx(),
            center = Offset(width * 0.2f, height * 0.25f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(width * 0.8f, height * 0.75f),
                radius = 200.dp.toPx()
            ),
            radius = 200.dp.toPx(),
            center = Offset(width * 0.8f, height * 0.75f)
        )
    }
}
