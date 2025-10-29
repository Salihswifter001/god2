package com.ui.player.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LightningEffects(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var lightning1Alpha by remember { mutableStateOf(0f) }
    var lightning2Alpha by remember { mutableStateOf(0f) }
    var lightning3Alpha by remember { mutableStateOf(0f) }
    
    val lightning1Paths = remember { generateLightningPaths(6, 1.2f) }
    val lightning2Paths = remember { generateLightningPaths(5, 0.8f) }
    val lightning3Paths = remember { generateLightningPaths(7, 1.5f) }
    
    // Şimşek animasyonu
    LaunchedEffect(key1 = isPlaying) {
        if (isPlaying) {
            while (true) {
                // Mavi şimşek
                delay(Random.nextLong(1800, 4500))
                lightning1Alpha = 0.8f
                delay(80)
                lightning1Alpha = 0.3f
                delay(30)
                lightning1Alpha = 0.6f
                delay(50)
                lightning1Alpha = 0f
                
                // Turkuaz şimşek
                delay(Random.nextLong(900, 2800))
                lightning2Alpha = 0.7f
                delay(120)
                lightning2Alpha = 0.2f
                delay(40)
                lightning2Alpha = 0.5f
                delay(70)
                lightning2Alpha = 0f
                
                // Mor şimşek
                delay(Random.nextLong(1500, 5000))
                lightning3Alpha = 0.9f
                delay(100)
                lightning3Alpha = 0.4f
                delay(50)
                lightning3Alpha = 0.7f
                delay(80)
                lightning3Alpha = 0f
            }
        }
    }
    
    Canvas(
        modifier = modifier.alpha(0.8f)
    ) {
        // Mavi şimşek
        if (lightning1Alpha > 0) {
            for (path in lightning1Paths) {
                drawPath(
                    path = path,
                    color = Color(0x4080C7FF).copy(alpha = lightning1Alpha * 0.3f),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                    blendMode = BlendMode.Screen
                )
                
                drawPath(
                    path = path,
                    color = Color(0xFF80C7FF).copy(alpha = lightning1Alpha),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                    blendMode = BlendMode.Screen
                )
            }
        }
        
        // Turkuaz şimşek
        if (lightning2Alpha > 0) {
            for (path in lightning2Paths) {
                drawPath(
                    path = path,
                    color = Color(0x4080FFFC).copy(alpha = lightning2Alpha * 0.3f),
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
                    blendMode = BlendMode.Screen
                )
                
                drawPath(
                    path = path,
                    color = Color(0xFF80FFFC).copy(alpha = lightning2Alpha),
                    style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round),
                    blendMode = BlendMode.Screen
                )
            }
        }
        
        // Mor şimşek
        if (lightning3Alpha > 0) {
            for (path in lightning3Paths) {
                drawPath(
                    path = path,
                    color = Color(0x40A080FF).copy(alpha = lightning3Alpha * 0.3f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                    blendMode = BlendMode.Screen
                )
                
                drawPath(
                    path = path,
                    color = Color(0xFFA080FF).copy(alpha = lightning3Alpha),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    blendMode = BlendMode.Screen
                )
            }
        }
    }
}

/**
 * Rastgele şimşek yolu oluşturur
 */
private fun generateLightningPaths(count: Int, intensity: Float = 1.0f): List<Path> {
    val paths = mutableListOf<Path>()
    
    repeat(count) {
        val path = Path()
        
        // Rastgele başlangıç noktası (üstten)
        val startX = Random.nextFloat() * 1000
        path.moveTo(startX, 0f)
        
        var currentX = startX
        var currentY = 0f
        
        // Şimşeğin zigzag kollarını oluşturma
        val segments = Random.nextInt(5, 15)
        repeat(segments) {
            val angleVariation = Random.nextFloat() * 0.8f + 0.6f // 0.6 - 1.4 arası
            // İntensiteye göre daha uzun veya daha kısa şimşekler
            val newX = currentX + (Random.nextFloat() - 0.5f) * 200 * angleVariation * intensity
            val newY = currentY + Random.nextFloat() * 100 * (1 + it / segments.toFloat()) * intensity
            
            path.lineTo(newX, newY)
            
            currentX = newX
            currentY = newY
            
            // Bazen şimşekten yan dallar oluştur - intensiteye göre daha çok dal
            if (Random.nextFloat() > (0.7f - (intensity * 0.1f))) {
                val branchPath = Path()
                branchPath.moveTo(currentX, currentY)
                
                var branchX = currentX
                var branchY = currentY
                
                val branchSegments = Random.nextInt(2, (5 * intensity).toInt().coerceAtLeast(2))
                repeat(branchSegments) {
                    val branchAngle = Random.nextFloat() * 2 * kotlin.math.PI
                    val branchLength = Random.nextFloat() * 60 + 20 * intensity
                    
                    val newBranchX = branchX + (cos(branchAngle) * branchLength).toFloat()
                    val newBranchY = branchY + (sin(branchAngle) * branchLength).toFloat()
                    
                    branchPath.lineTo(newBranchX, newBranchY)
                    
                    branchX = newBranchX
                    branchY = newBranchY
                }
                
                paths.add(branchPath)
            }
        }
        
        paths.add(path)
    }
    
    return paths
} 