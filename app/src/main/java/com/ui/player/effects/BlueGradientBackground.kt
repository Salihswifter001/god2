package com.ui.player.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

/**
 * Mavi neon gradyan arka plan efekti
 * Müzik player için özel olarak tasarlanmış, oynatma durumuna göre animasyonları değiştirir
 */
@Composable
fun BlueGradientBackground(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false
) {
    // Ana tema renkleri
    val deepBlue = Color(0xFF080B18)
    val midBlue = Color(0xFF102148)
    val lightBlue = Color(0xFF0068FF)
    val brightBlue = Color(0xFF00B7FF)
    
    // Animasyon geçişleri
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundTransition")
    
    // Arkaplan dalga hareketleri - oynatma durumuna göre hız ayarı
    val animationSpeed = if (isPlaying) 1.0f else 0.5f
    
    // Dalga animasyonu
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f, // 2π
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (10000 / animationSpeed).toInt(), 
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    
    // Renk yoğunluğu animasyonu
    val colorIntensity by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (5000 / animationSpeed).toInt(),
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorIntensity"
    )
    
    // Orbitler için animasyon
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (20000 / animationSpeed).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )
    
    // Arkaplan
    Box(
        modifier = modifier
    ) {
        // Ana gradient arkaplan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            deepBlue,
                            midBlue.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        
        // Arka plan efektleri - Canvas ile çizilecek
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (isPlaying) 20.dp else 10.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            
            // Alt yarım daire parlama efekti
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lightBlue.copy(alpha = 0.1f * colorIntensity),
                        midBlue.copy(alpha = 0.05f * colorIntensity),
                        Color.Transparent
                    )
                ),
                radius = canvasHeight * 0.6f,
                center = Offset(centerX, canvasHeight + canvasHeight * 0.3f)
            )
            
            // Üst sol köşe parlama efekti
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        brightBlue.copy(alpha = 0.1f * colorIntensity),
                        lightBlue.copy(alpha = 0.05f * colorIntensity),
                        Color.Transparent
                    )
                ),
                radius = canvasWidth * 0.4f,
                center = Offset(canvasWidth * 0.15f, canvasHeight * 0.15f)
            )
            
            // Yörünge efektleri
            val orbitRadians = Math.toRadians(orbitRotation.toDouble())
            val orbitX = centerX + cos(orbitRadians) * centerX * 0.7f
            val orbitY = centerY + sin(orbitRadians) * centerY * 0.5f
            
            // Hareketli ışık noktası
            drawCircle(
                color = brightBlue.copy(alpha = 0.2f * colorIntensity),
                radius = 60f * colorIntensity,
                center = Offset(orbitX.toFloat(), orbitY.toFloat()),
                blendMode = BlendMode.Screen
            )
            
            // Parçacık efektleri
            if (isPlaying) {
                // Oynatılırken ekstra parçacıklar göster
                val particleCount = 10
                val seed = System.currentTimeMillis() / 1000
                val random = Random(seed.toInt())
                
                for (i in 0 until particleCount) {
                    val angle = (i * 36 + orbitRotation) % 360
                    val angleRad = Math.toRadians(angle.toDouble())
                    val distance = random.nextDouble(0.2, 0.8) * centerX.coerceAtMost(centerY)
                    val particleX = centerX + cos(angleRad) * distance
                    val particleY = centerY + sin(angleRad) * distance
                    val radius = (5 + random.nextDouble(0.0, 15.0) * colorIntensity).toFloat()
                    
                    drawCircle(
                        color = brightBlue.copy(alpha = (0.1f + random.nextFloat() * 0.2f) * colorIntensity),
                        radius = radius,
                        center = Offset(particleX.toFloat(), particleY.toFloat()),
                        blendMode = BlendMode.Screen
                    )
                }
            }
        }
    }
} 