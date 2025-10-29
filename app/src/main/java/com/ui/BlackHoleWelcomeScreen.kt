package com.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun BlackHoleWelcomeScreen(
    username: String,
    onAnimationComplete: () -> Unit
) {
    var showText by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    
    // Event horizon pulsing animation
    val eventHorizonPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Accretion disk rotation
    val diskRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        )
    )
    
    // Gravitational lensing effect
    val lensingEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Particle animation around event horizon
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing)
        )
    )
    
    // Text fade in animation
    LaunchedEffect(Unit) {
        delay(1500)
        showText = true
        delay(3000)
        onAnimationComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Background stars
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawStars(this)
        }
        
        // Black hole with event horizon
        Canvas(
            modifier = Modifier
                .size(350.dp)
                .scale(eventHorizonPulse)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            
            // Gravitational lensing background distortion
            for (i in 5 downTo 1) {
                val lensRadius = radius * (1f + i * 0.3f) * (1f + lensingEffect * 0.1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x0A9C27B0),
                            Color(0x059C27B0),
                            Color.Transparent
                        ),
                        center = center,
                        radius = lensRadius
                    ),
                    radius = lensRadius,
                    center = center
                )
            }
            
            // Accretion disk
            rotate(degrees = diskRotation, pivot = center) {
                // Outer disk layers
                for (i in 0..3) {
                    val diskRadius = radius * (1.5f - i * 0.1f)
                    val alpha = 0.1f - i * 0.02f
                    
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFFFF6B00).copy(alpha = alpha),
                                Color(0xFFFFD700).copy(alpha = alpha * 0.7f),
                                Color(0xFFFF4500).copy(alpha = alpha),
                                Color(0xFFFF8C00).copy(alpha = alpha * 0.5f),
                                Color(0xFFFF6B00).copy(alpha = alpha)
                            ),
                            center = center
                        ),
                        radius = diskRadius,
                        center = center,
                        style = Stroke(width = 15.dp.toPx() - i * 3.dp.toPx())
                    )
                }
            }
            
            // Event horizon glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x20FF6B00),
                        Color(0x40FF4500),
                        Color(0x60FF0000),
                        Color.Black
                    ),
                    center = center,
                    radius = radius * 0.9f
                ),
                radius = radius * 0.9f,
                center = center
            )
            
            // Photon sphere (bright ring at event horizon)
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.3f + lensingEffect * 0.2f),
                radius = radius * 0.75f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Particle effects around event horizon
            for (i in 0..12) {
                val angle = (i * 30f + particlePhase * 180f / PI.toFloat()) * PI.toFloat() / 180f
                val particleRadius = radius * 0.8f
                val particleX = center.x + cos(angle) * particleRadius
                val particleY = center.y + sin(angle) * particleRadius
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0x80FFFFFF),
                            Color.Transparent
                        ),
                        radius = 5.dp.toPx()
                    ),
                    radius = 3.dp.toPx(),
                    center = Offset(particleX, particleY)
                )
            }
            
            // Inner event horizon (absolute black)
            drawCircle(
                color = Color.Black,
                radius = radius * 0.6f,
                center = center
            )
            
            // Hawking radiation effect (subtle glow at the very edge)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black,
                        Color.Black,
                        Color(0x10FFFFFF)
                    ),
                    center = center,
                    radius = radius * 0.62f
                ),
                radius = radius * 0.62f,
                center = center
            )
        }
        
        // Welcome text with glow effect
        if (showText) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.blur(0.5.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = username,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            shadowElevation = 8.dp.toPx()
                        }
                )
            }
        }
    }
}

// Extension function to draw stars
private fun DrawScope.drawStars(scope: DrawScope) {
    val random = kotlin.random.Random(42) // Fixed seed for consistent stars
    
    for (i in 0..200) {
        val x = random.nextFloat() * scope.size.width
        val y = random.nextFloat() * scope.size.height
        val starSize = random.nextFloat() * 2f + 0.5f
        val brightness = random.nextFloat() * 0.8f + 0.2f
        
        scope.drawCircle(
            color = Color.White.copy(alpha = brightness),
            radius = starSize,
            center = Offset(x, y)
        )
    }
}