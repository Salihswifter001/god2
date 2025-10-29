package com

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class, ExperimentalAnimationApi::class)
@Composable
fun OctaStartScreen(
    onSplashFinished: () -> Unit = {}
) {
    // Ana renkler
    val darkBackground = Color(0xFF050510)
    val neonBlue = Color(0xFF00AAFF)
    val neonCyan = Color(0xFF00FFFF)
    val deepBlue = Color(0xFF0066CC)
    val darkPurple = Color(0xFF1A0030)
    
    // Animasyon durumları
    var logoAnimationDone by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }
    var poweredByVisible by remember { mutableStateOf(false) }
    
    // Ana yazı için animasyon
    val logoScale = animateFloatAsState(
        targetValue = if (logoAnimationDone) 1f else 0f,
        animationSpec = tween(1000, easing = EaseOutBack),
        label = "logoScale"
    )
    
    // Sürekli animasyonlar
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteAnim")
    
    // Parıltı efekti
    val glowIntensity = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Arka plan dalga animasyonu
    val wavePhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )
    
    // Logo etrafındaki pulse efekti
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Renk geçişi animasyonu
    val hueShift = infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hue"
    )
    
    // Sekansı başlat
    LaunchedEffect(Unit) {
        delay(300)
        logoAnimationDone = true
        delay(1000)
        taglineVisible = true
        delay(800)
        poweredByVisible = true
        
        // Splash ekranı tamamlandı
        delay(2500)
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Arka plan animasyonları
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            
            // Arka planda ışık halkası
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF001A33).copy(alpha = 0.4f * glowIntensity.value),
                        darkBackground.copy(alpha = 0f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = size.minDimension * 0.8f
                ),
                center = Offset(centerX, centerY),
                radius = size.minDimension * 0.8f
            )
            
            // Fütüristik grid çizgileri
            val gridLines = 20
            val gridSpacing = size.minDimension / gridLines
            val lineAlpha = 0.1f * glowIntensity.value
            
            // Yatay gridler
            for (i in -gridLines..gridLines) {
                val y = centerY + i * gridSpacing
                
                if (y >= 0 && y <= height) {
                    val distanceFactorY = 1f - (kotlin.math.abs(y - centerY) / (height / 2))
                    val alphaY = lineAlpha * distanceFactorY
                    
                    drawLine(
                        color = deepBlue.copy(alpha = alphaY),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), wavePhase.value % 35f)
                    )
                }
            }
            
            // Dikey gridler
            for (i in -gridLines..gridLines) {
                val x = centerX + i * gridSpacing
                
                if (x >= 0 && x <= width) {
                    val distanceFactorX = 1f - (kotlin.math.abs(x - centerX) / (width / 2))
                    val alphaX = lineAlpha * distanceFactorX
                    
                    drawLine(
                        color = neonBlue.copy(alpha = alphaX),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), -wavePhase.value % 35f)
                    )
                }
            }
            
            // Dalga efekti noktaları
            val particleCount = 120
            val particleRange = size.minDimension * 0.8f
            
            for (i in 0 until particleCount) {
                val angle = (i.toFloat() / particleCount) * 2 * PI.toFloat()
                val waveOffset = sin(angle * 5 + wavePhase.value) * 30f
                val radius = particleRange + waveOffset
                
                val x = centerX + cos(angle) * radius
                val y = centerY + sin(angle) * radius
                
                val particleSize = 2f + 3f * glowIntensity.value
                val particleAlpha = 0.1f + 0.4f * glowIntensity.value * (1f - i.toFloat() / particleCount)
                
                drawCircle(
                    color = neonCyan.copy(alpha = particleAlpha),
                    radius = particleSize,
                    center = Offset(x, y),
                    blendMode = BlendMode.Plus
                )
            }
        }
        
        // Logo ve pulse efekti
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Pulse arka planı
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(pulseScale.value)
                    .alpha(0.2f * glowIntensity.value)
                    .blur(radius = 20.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                neonBlue.copy(alpha = 0.3f * glowIntensity.value),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Ana içerik
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(logoScale.value)
                    .padding(16.dp)
            ) {
                // OCTA logosu
                Text(
                    text = "OCTA",
                    fontSize = 68.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                neonCyan.copy(alpha = glowIntensity.value),
                                neonBlue.copy(alpha = 0.9f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(250f + hueShift.value * 10, 0f)
                        )
                    ),
                    modifier = Modifier
                        .graphicsLayer {
                            shadowElevation = 20f
                        }
                )
                
                // Tagline
                AnimatedVisibility(
                    visible = taglineVisible,
                    enter = fadeIn(tween(800)) + expandVertically(tween(800))
                ) {
                    Text(
                        text = "GELECEĞİN MÜZİĞİ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .graphicsLayer(alpha = glowIntensity.value)
                            .blur(radius = 0.4.dp)
                    )
                }
                
                // Powered by AI
                AnimatedVisibility(
                    visible = poweredByVisible,
                    enter = fadeIn(tween(800)) + expandVertically(tween(800))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        darkPurple,
                                        deepBlue.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "POWERED BY OctaAI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = neonCyan.copy(alpha = glowIntensity.value),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Köşe ışık efektleri
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val cornerGlowAlpha = 0.2f * glowIntensity.value
            val cornerRadius = size.minDimension * 0.6f
            
            // Sol üst
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonBlue.copy(alpha = cornerGlowAlpha),
                        Color.Transparent
                    )
                ),
                radius = cornerRadius,
                center = Offset(0f, 0f)
            )
            
            // Sağ alt
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        deepBlue.copy(alpha = cornerGlowAlpha),
                        Color.Transparent
                    )
                ),
                radius = cornerRadius,
                center = Offset(size.width, size.height)
            )
        }
    }
} 