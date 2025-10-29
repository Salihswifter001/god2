package com.ui.player.effects

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Mavi tonlarında gradient ve blur efektli arka plan
 */
@Composable
fun BlurGradientBackground(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    // Gradient için renk animasyonu
    val animationDuration = 3000
    
    // İlk mavi ton animasyonu
    var blue1Anim by remember { mutableStateOf(true) }
    val blue1 by animateColorAsState(
        targetValue = if (blue1Anim) Color(0xFF051C3D) else Color(0xFF0A2B5C),
        animationSpec = tween(animationDuration, easing = LinearEasing),
        label = "blueAnimation1"
    )
    
    // İkinci mavi ton animasyonu
    var blue2Anim by remember { mutableStateOf(true) }
    val blue2 by animateColorAsState(
        targetValue = if (blue2Anim) Color(0xFF163974) else Color(0xFF235AB4),
        animationSpec = tween(animationDuration, easing = LinearEasing),
        label = "blueAnimation2"
    )
    
    // Üçüncü mavi ton animasyonu
    var blue3Anim by remember { mutableStateOf(true) }
    val blue3 by animateColorAsState(
        targetValue = if (blue3Anim) Color(0xFF0F1E44) else Color(0xFF0C122C),
        animationSpec = tween(animationDuration, easing = LinearEasing),
        label = "blueAnimation3"
    )
    
    // Renk değişimi için zamanlayıcı
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(animationDuration.toLong())
            blue1Anim = !blue1Anim
            blue2Anim = !blue2Anim
            blue3Anim = !blue3Anim
        }
    }
    
    // Parlaklık animasyonu (çalan müzik varsa)
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.9f else 0.6f,
        animationSpec = tween(1500, easing = LinearEasing),
        label = "glowAlpha"
    )
    
    // Pulsasyon animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "pulsationTransition")
    val pulsateScale = infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsateAnimation"
    )
    
    Box(modifier = modifier) {
        // Gradient arka plan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            blue1,
                            blue2,
                            blue3
                        )
                    )
                )
        ) 
        
        // Parlayan dalgalar
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
        ) {
            // Merkez parlaklık
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            // Üst parlama dairesi
            drawCircle(
                color = Color(0x500A84FF).copy(alpha = glowAlpha * 0.7f),
                radius = size.minDimension * 0.4f * pulsateScale.value,
                center = Offset(centerX, centerY * 0.6f)
            )
            
            // Alt parlama dairesi
            drawCircle(
                color = Color(0x402B4EFF).copy(alpha = glowAlpha * 0.5f),
                radius = size.minDimension * 0.5f * pulsateScale.value,
                center = Offset(centerX, centerY * 1.4f)
            )
            
            // Merkez parlama
            if (isPlaying) {
                drawCircle(
                    color = Color(0x801E90FF).copy(alpha = glowAlpha * 0.3f),
                    radius = size.minDimension * 0.2f * pulsateScale.value,
                    center = Offset(centerX, centerY)
                )
            }
        }
        
        // Hafif karartma katmanı
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x55000000))
        )
    }
} 