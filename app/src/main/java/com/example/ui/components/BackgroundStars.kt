package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.LunarGold
import com.example.ui.theme.MidnightBlue
import kotlin.random.Random

@Composable
fun BackgroundStars(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MidnightBlue,
                        DeepBlack
                    )
                )
            )
    ) {
        // Draw starry elements dynamically in a high-performance Canvas
        val infiniteTransition = rememberInfiniteTransition(label = "stars")
        val twinkleFraction by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteSpec(dur = 3200),
            label = "twinkle"
        )
        val constellationOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 40f,
            animationSpec = infiniteSpec(dur = 12000),
            label = "drift"
        )

        val stars = remember {
            List(30) {
                StarData(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    size = Random.nextFloat() * 4f + 1f,
                    opacitySpeed = Random.nextFloat() * 0.5f + 0.5f,
                    color = if (Random.nextBoolean()) Color.White else LunarGold.copy(alpha = 0.8f)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (star in stars) {
                // Adjust position with subtle drift
                val actualX = (star.x * size.width + constellationOffset * star.size * 0.1f) % size.width
                val actualY = star.y * size.height
                
                // Twinkling opacity modifier
                val alpha = (0.3f + (0.7f * twinkleFraction * star.opacitySpeed))
                    .coerceIn(0f, 1f)
                
                drawCircle(
                    color = star.color.copy(alpha = alpha),
                    radius = star.size,
                    center = Offset(actualX, actualY)
                )
            }
            
            // Draw a subtle, soft glowing outline of a crescent moon in top right corner
            val moonCenterX = size.width - 150f
            val moonCenterY = 200f
            drawCircle(
                color = LunarGold.copy(alpha = 0.08f),
                radius = 70f,
                center = Offset(moonCenterX, moonCenterY)
            )
            drawCircle(
                color = LunarGold.copy(alpha = 0.25f),
                radius = 45f,
                center = Offset(moonCenterX, moonCenterY)
            )
            drawCircle(
                color = Color(0xFF020306), // Mask it to create a crescent
                radius = 42f,
                center = Offset(moonCenterX - 18f, moonCenterY - 6f)
            )
        }

        content()
    }
}

private fun infiniteSpec(dur: Int): InfiniteRepeatableSpec<Float> {
    return infiniteRepeatable(
        animation = tween(dur, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    )
}

private data class StarData(
    val x: Float,
    val y: Float,
    val size: Float,
    val opacitySpeed: Float,
    val color: Color
)
