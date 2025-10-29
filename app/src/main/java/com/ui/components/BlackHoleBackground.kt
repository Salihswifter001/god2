package com.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.*
import kotlin.random.Random

@Composable
fun BlackHoleBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blackhole")
    
    // Kara delik dönüş animasyonu
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Spiral animasyonu
    val spiralPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spiral"
    )
    
    // Çekim gücü animasyonu
    val pullStrength by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pull"
    )
    
    // Event horizon pulse
    val eventHorizonPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Parçacıklar için state
    val particles = remember { generateParticles(100) }
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = minOf(size.width, size.height) / 2
        
        // Arka plan - daha açık uzay rengi
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1a0033),  // Koyu mor
                    Color(0xFF000522),  // Çok koyu mavi
                    Color(0xFF000000)   // Siyah
                )
            )
        )
        
        // Arka plan gradient - daha görünür renklerde
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3A0066).copy(alpha = 0.8f), // Çok açık mor
                    Color(0xFF1A0033).copy(alpha = 0.9f),
                    Color(0xFF0A0015)
                ),
                center = Offset(centerX, centerY),
                radius = maxRadius * 2
            )
        )
        
        // Accretion disk (toplama diski) - dönen spiral
        drawAccretionDisk(
            center = Offset(centerX, centerY),
            maxRadius = maxRadius,
            rotation = rotation,
            spiralPhase = spiralPhase,
            pullStrength = pullStrength
        )
        
        // Gravitational lensing efekti
        drawGravitationalLensing(
            center = Offset(centerX, centerY),
            radius = maxRadius * 0.8f,
            rotation = rotation * 0.5f
        )
        
        // Çekilen parçacıklar
        drawParticles(
            particles = particles,
            center = Offset(centerX, centerY),
            maxRadius = maxRadius,
            spiralPhase = spiralPhase,
            pullStrength = pullStrength
        )
        
        // Event horizon (olay ufku)
        drawEventHorizon(
            center = Offset(centerX, centerY),
            radius = maxRadius * 0.3f * eventHorizonPulse
        )
        
        // Merkez kara delik
        drawBlackHole(
            center = Offset(centerX, centerY),
            radius = maxRadius * 0.2f * eventHorizonPulse
        )
    }
}

private fun DrawScope.drawAccretionDisk(
    center: Offset,
    maxRadius: Float,
    rotation: Float,
    spiralPhase: Float,
    pullStrength: Float
) {
    val spiralCount = 3
    val segmentCount = 50
    
    for (spiral in 0 until spiralCount) {
        val spiralOffset = (spiral * 2 * PI / spiralCount).toFloat()
        
        for (i in 0 until segmentCount) {
            val t = i.toFloat() / segmentCount
            val angle = spiralPhase + spiralOffset + t * 4 * PI.toFloat()
            val radius = maxRadius * (1 - t * 0.7f) * pullStrength
            
            val x = center.x + cos(angle + rotation * PI.toFloat() / 180) * radius
            val y = center.y + sin(angle + rotation * PI.toFloat() / 180) * radius
            
            val alpha = (1 - t) * 0.9f  // Daha görünür
            val size = (1 - t) * 25f     // Daha büyük parçacıklar
            
            // Spiral renk geçişi - daha parlak mavi ve mor tonları
            val color = when {
                t < 0.3f -> Color(0xFF00FFFF).copy(alpha = minOf(alpha * 1.5f, 1f)) // Çok parlak cyan
                t < 0.6f -> Color(0xFFFF00FF).copy(alpha = minOf(alpha * 1.3f, 1f)) // Çok parlak magenta
                else -> Color(0xFFFFFF00).copy(alpha = minOf(alpha * 1.2f, 1f)) // Parlak sarı
            }
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0f)
                    ),
                    center = Offset(x.toFloat(), y.toFloat()),
                    radius = size
                ),
                radius = size,
                center = Offset(x.toFloat(), y.toFloat()),
                blendMode = BlendMode.Plus
            )
        }
    }
}

private fun DrawScope.drawGravitationalLensing(
    center: Offset,
    radius: Float,
    rotation: Float
) {
    val ringCount = 5
    
    for (ring in 0 until ringCount) {
        val ringRadius = radius * (0.5f + ring * 0.2f)
        val alpha = 0.15f / (ring + 1)
        
        rotate(rotation + ring * 30f, pivot = center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF00D9FF).copy(alpha = 0f),
                        Color(0xFF00D9FF).copy(alpha = alpha),
                        Color(0xFF8B5CF6).copy(alpha = alpha),
                        Color(0xFF00D9FF).copy(alpha = 0f)
                    ),
                    center = center
                ),
                radius = ringRadius,
                center = center,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(10f, 5f),
                        phase = rotation
                    )
                )
            )
        }
    }
}

private fun DrawScope.drawParticles(
    particles: List<Particle>,
    center: Offset,
    maxRadius: Float,
    spiralPhase: Float,
    pullStrength: Float
) {
    particles.forEach { particle ->
        val angle = particle.angle + spiralPhase * particle.speed
        val currentRadius = particle.radius * (1 - spiralPhase / (2 * PI.toFloat()) * 0.3f)
        
        // Kara deliğe yaklaştıkça hızlanan spiral hareket
        val spiralFactor = 1 - (currentRadius / maxRadius)
        val spiralAngle = angle + spiralFactor * PI.toFloat() * pullStrength
        
        val x = center.x + cos(spiralAngle) * currentRadius
        val y = center.y + sin(spiralAngle) * currentRadius
        
        // Merkeze yaklaştıkça küçülen ve parlaklaşan parçacıklar
        val distanceRatio = currentRadius / maxRadius
        val size = particle.size * distanceRatio
        val alpha = (1 - distanceRatio) * 0.8f + 0.2f
        
        drawCircle(
            color = particle.color.copy(alpha = alpha),
            radius = size,
            center = Offset(x, y),
            blendMode = BlendMode.Plus
        )
        
        // Parçacık izi
        for (trail in 1..5) {
            val trailAngle = spiralAngle - trail * 0.1f
            val trailRadius = currentRadius + trail * 5f
            val trailX = center.x + cos(trailAngle) * trailRadius
            val trailY = center.y + sin(trailAngle) * trailRadius
            val trailAlpha = alpha * (1 - trail * 0.2f)
            
            drawCircle(
                color = particle.color.copy(alpha = trailAlpha * 0.3f),
                radius = size * (1 - trail * 0.15f),
                center = Offset(trailX, trailY),
                blendMode = BlendMode.Plus
            )
        }
    }
}

private fun DrawScope.drawEventHorizon(
    center: Offset,
    radius: Float
) {
    // Olay ufku gradient halkaları
    val rings = 3
    for (i in 0 until rings) {
        val ringRadius = radius * (1 + i * 0.1f)
        val alpha = 0.3f / (i + 1)
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF00FF).copy(alpha = alpha),
                    Color(0xFF00FFFF).copy(alpha = alpha * 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = ringRadius
            ),
            radius = ringRadius,
            center = center,
            blendMode = BlendMode.Plus
        )
    }
}

private fun DrawScope.drawBlackHole(
    center: Offset,
    radius: Float
) {
    // Merkez kara delik - gradient ile
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black,
                Color(0xFF220044),  // Koyu mor
                Color(0xFF440088)   // Mor
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    
    // İç glow efekti
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black,
                Color.Black,
                Color(0xFF6B46C1).copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.5f
        ),
        radius = radius * 1.5f,
        center = center,
        blendMode = BlendMode.Plus
    )
}

data class Particle(
    val angle: Float,
    val radius: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

private fun generateParticles(count: Int): List<Particle> {
    return List(count) {
        Particle(
            angle = Random.nextFloat() * 2 * PI.toFloat(),
            radius = 100f + Random.nextFloat() * 300f,
            speed = 0.5f + Random.nextFloat() * 1.5f,
            size = 2f + Random.nextFloat() * 4f,
            color = when (Random.nextInt(4)) {
                0 -> Color(0xFF00D9FF)
                1 -> Color(0xFF8B5CF6)
                2 -> Color(0xFFFCA5A5)
                else -> Color(0xFFFBBF24)
            }
        )
    }
}