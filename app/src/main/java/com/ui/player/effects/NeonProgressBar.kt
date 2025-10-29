package com.ui.player.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ui.player.utils.MusicUtils
import kotlin.math.sin

/**
 * Basit neon ilerleme çubuğu - Garantili animasyon
 */
@Composable
fun NeonProgressBar(
    progress: Float,
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    onSeek: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Ana neon renkler
    val neonPink = Color(0xFFFF00FF)
    val neonBlue = Color(0xFF00FFFF)
    
    // Dokunma durumu
    var isTouched by remember { mutableStateOf(false) }
    
    // Animasyon kontrolcüsü
    val infiniteTransition = rememberInfiniteTransition(label = "mainTransition")
    
    // Basit nabız efekti
    val pulse = infiniteTransition.animateFloat(
        initialValue = 0.4f, 
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Dalga animasyonu
    val waveOffset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, 
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            )
        ),
        label = "wave"
    )
    
    Column(modifier = modifier) {
        // Süre bilgileri
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = MusicUtils.formatDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Text(
                text = MusicUtils.formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // İlerleme çubuğu
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            isTouched = true
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek?.invoke(newProgress)
                            awaitRelease()
                            isTouched = false
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val barHeightPx = with(LocalDensity.current) { 3.dp.toPx() }
            
            // Arka plan çizgisi
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.Center)
            ) {
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
            }
            
            // Ana ilerleme çizgisi
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceAtLeast(0.01f))
                    .height(3.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                neonBlue,
                                neonPink
                            )
                        ), 
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(1.5.dp)
                    )
            )
            
            // Parlaklık efekti
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceAtLeast(0.01f))
                    .height(16.dp)
                    .align(Alignment.CenterStart)
                    .blur(8.dp)
            ) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            neonBlue.copy(alpha = pulse.value * 0.5f),
                            neonPink.copy(alpha = pulse.value * 0.5f)
                        )
                    ),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            
            // Dalgalı çizgi animasyonu
            if (progress > 0.02f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(20.dp)
                        .align(Alignment.CenterStart)
                        .blur(2.dp)
                ) {
                    val path = Path()
                    val centerY = size.height / 2
                    val progressWidth = size.width
                    val waveHeight = 3.dp.toPx()
                    
                    path.moveTo(0f, centerY)
                    
                    for (i in 0..100) {
                        val x = progressWidth * i / 100
                        val offset = waveOffset.value
                        val wave = sin((i + offset) / 15f) * waveHeight
                        path.lineTo(x, centerY + wave)
                    }
                    
                    drawPath(
                        path = path,
                        color = neonBlue.copy(alpha = 0.6f * pulse.value),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            
            // İlerleme noktası (daima görünür)
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth * progress - 8.dp).coerceAtLeast(0.dp))
                    .size(16.dp)
                    .align(Alignment.CenterStart)
            ) {
                // Arka plan parlaklık
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(6.dp)
                ) {
                    val radius = (8.dp.toPx() * (0.8f + (pulse.value * 0.4f)))
                    drawCircle(
                        color = neonPink.copy(alpha = 0.6f),
                        radius = radius
                    )
                }
                
                // Ana nokta
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .align(Alignment.Center)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    neonPink
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
            
            // Dokunma efekti
            if (isTouched) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(4.dp)
                ) {
                    drawLine(
                        color = neonPink.copy(alpha = 0.4f),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 12.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
