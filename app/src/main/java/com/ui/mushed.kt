package com.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern ve neon karanlık mavi tonlarında "Kütüphanem" başlığı
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun NeonKutuphaneTitle(modifier: Modifier = Modifier) {
    // Neon animasyonu için sonsuz geçiş
    val infiniteTransition = rememberInfiniteTransition(label = "neonGlow")
    
    // Parlaklık animasyonu
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )
    
    // Blur animasyonu
    val blurRadius by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blurRadius"
    )
    
    // Kenar ışıması animasyonu için renk geçişi
    val neonBlue = Color(0xFF00BFFF)
    val deepBlue = Color(0xFF0040FF)
    val darkerBlue = Color(0xFF000080)
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .drawBehind {
                // Arka plan ışıması çiz
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonBlue.copy(alpha = 0.2f * glowIntensity),
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.6f,
                    center = center,
                    alpha = 0.5f * glowIntensity
                )
            }
    ) {
        // İç içe metinler ile derinlik efekti
        // Gölge katmanı
        Text(
            text = "Kütüphanem",
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        darkerBlue.copy(alpha = 0.7f),
                        deepBlue.copy(alpha = 0.5f)
                    )
                )
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 2.dp, y = 2.dp)
                .blur(radius = blurRadius.dp)
        )
        
        // Orta katman
        Text(
            text = "Kütüphanem",
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        deepBlue,
                        neonBlue
                    )
                )
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .blur(radius = (blurRadius * 0.5f).dp)
        )
        
        // Üst katman (en parlak)
        Text(
            text = "Kütüphanem",
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = glowIntensity * 0.8f),
                        neonBlue
                    )
                )
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun NeonKutuphaneTitlePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        NeonKutuphaneTitle()
    }
} 