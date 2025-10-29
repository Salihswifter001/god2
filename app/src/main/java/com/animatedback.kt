    package com

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ModernMinimalistBackground() {
    // Ana arkaplan renk animasyonları - yavaş geçiş için süreyi uzattım
    val infiniteTransition = rememberInfiniteTransition()
    
    // Yavaş renk değişimi için daha uzun süreli animasyon (60 saniye)
    val hueAnimation = infiniteTransition.animateFloat(
        initialValue = 220f, // Mavi tondan başla
        targetValue = 580f, // 220 + 360 (tam tur)
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing), // 1 dakikalık yavaş geçiş
            repeatMode = RepeatMode.Restart
        ),
        label = "HueAnimation"
    )
    
    // Parlaklık dalgalanması için yavaş animasyon
    val brightnessAnimation = infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BrightnessAnimation"
    )
    
    // Hafif kayma efekti için animasyon
    val offsetAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OffsetAnimation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Ana arkaplan - minimalist ve modern
        Canvas(modifier = Modifier.fillMaxSize()) {
            
            val currentHue = hueAnimation.value % 360
            val complementaryHue = (currentHue + 180) % 360
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            // Ana gradient arkaplan - yavaş değişen
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.hsl(currentHue, 0.7f, 0.2f).copy(alpha = 0.9f),
                        Color.hsl((currentHue + 60) % 360, 0.6f, brightnessAnimation.value).copy(alpha = 0.8f),
                        Color.hsl(complementaryHue, 0.5f, 0.15f).copy(alpha = 0.9f)
                    ),
                    start = Offset(offsetAnimation.value, 0f),
                    end = Offset(size.width - offsetAnimation.value, size.height)
                ),
                size = size
            )
            
            // İkincil katman - daha subtil ton ve derinlik ekler
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.hsl(currentHue, 0.8f, 0.3f).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(
                        centerX + offsetAnimation.value / 2,
                        centerY - offsetAnimation.value / 3
                    ),
                    radius = size.maxDimension
                ),
                size = size
            )
        }
    }
}

// Arkaplanı bulanıklaştırmak için sarmalayıcı composable
@Composable
fun ModernBlurredBackground() {
    // Alt katman - daha fazla bulanık
    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(60.dp) // Daha fazla bulanıklık efekti
    ) {
        ModernMinimalistBackground()
    }
    
    // Orta katman - orta bulanıklık
    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(30.dp)
    ) {
        ModernMinimalistBackground()
    }
    
    // Üst katman - hafif bulanık
    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(15.dp)
    ) {
        ModernMinimalistBackground()
    }
}

// Kullanım örneği
@Preview(showBackground = true)
@Composable
fun ModernBackgroundPreview() {
    ModernBlurredBackground()
}

// Uygulamanızda kullanmak için MainActivity örneği
/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModernBlurredBackground()
            
            // İçeriğinizi buraya ekleyin
        }
    }
}
*/ 