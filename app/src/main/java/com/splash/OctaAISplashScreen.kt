package com.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun OctaAISplashScreen(
    onSplashComplete: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showSlogan by remember { mutableStateOf(false) }
    
    // Animasyon değerleri
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Logo rotasyon animasyonu
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logoRotation"
    )
    
    // Logo scale animasyonu
    val logoScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    // Parıltı animasyonu
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )
    
    // Dalga animasyonu
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    
    // Arka plan gradient animasyonu
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )
    
    // Parçacık animasyonu için
    val particleAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleAnimation"
    )
    
    // Başlangıç animasyonları
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
        delay(800)
        showText = true
        delay(500)
        showSlogan = true
        delay(2500)
        onSplashComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Animasyonlu arka plan
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dinamik gradient arka plan
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A0033).copy(alpha = 0.8f),
                        Color(0xFF000511),
                        Color.Black
                    ),
                    center = Offset(
                        (size.width / 2 + cos(gradientOffset / 500) * 100).toFloat(),
                        (size.height / 2 + sin(gradientOffset / 500) * 100).toFloat()
                    ),
                    radius = size.minDimension * 0.8f
                )
            )
            
            // Animasyonlu parçacıklar
            val particleCount = 50
            for (i in 0 until particleCount) {
                val angle = (i.toFloat() / particleCount) * 2 * PI.toFloat()
                val baseRadius = size.minDimension * 0.4f
                val waveOffset = sin(angle * 3 + wavePhase) * 30f
                val radius = baseRadius + waveOffset
                
                val x = size.center.x + cos(angle + particleAnimation * 2 * PI.toFloat()) * radius
                val y = size.center.y + sin(angle + particleAnimation * 2 * PI.toFloat()) * radius
                
                val particleAlpha = (0.2f + 0.3f * sin(particleAnimation * 2 * PI.toFloat() + i)).coerceIn(0f, 1f)
                val particleSize = 2f + 3f * sin(particleAnimation * PI.toFloat() + i).absoluteValue
                
                drawCircle(
                    color = when (i % 3) {
                        0 -> Color(0xFFEC4899).copy(alpha = particleAlpha)
                        1 -> Color(0xFF3B82F6).copy(alpha = particleAlpha)
                        else -> Color(0xFF8B5CF6).copy(alpha = particleAlpha)
                    },
                    radius = particleSize,
                    center = Offset(x.toFloat(), y.toFloat()),
                    blendMode = BlendMode.Plus
                )
            }
            
            // Dönen halka efekti
            rotate(logoRotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFEC4899).copy(alpha = 0.3f),
                            Color(0xFF3B82F6).copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = center
                    ),
                    radius = size.minDimension * 0.35f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        
        // Gradient blur efektleri
        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = (-150).dp)
                .size(400.dp)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEC4899).copy(alpha = glowIntensity * 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .offset(x = 200.dp, y = 500.dp)
                .size(400.dp)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3B82F6).copy(alpha = glowIntensity * 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Ana içerik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animasyonlu Logo
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // Dış halka animasyonu
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(logoRotation * 0.5f)
                ) {
                    val radius = size.minDimension / 2.5f
                    
                    // Neon halka 1
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFEC4899),
                                Color(0xFF8B5CF6),
                                Color(0xFF3B82F6)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        radius = radius,
                        center = center,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(10f, 5f),
                                phase = wavePhase * 10
                            )
                        )
                    )
                    
                    // İç halka
                    drawCircle(
                        color = Color.White.copy(alpha = glowIntensity),
                        radius = radius * 0.7f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Merkez nokta
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFEC4899).copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        radius = radius * 0.2f,
                        center = center
                    )
                }
                
                // Logo glow efekti
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(20.dp)
                        .alpha(glowIntensity)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEC4899).copy(alpha = 0.5f),
                                Color(0xFF3B82F6).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 3,
                        center = center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Animated Text
            androidx.compose.animation.AnimatedVisibility(
                visible = showText,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = tween(1000)
                ) + androidx.compose.animation.expandVertically(
                    animationSpec = tween(1000)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // OctaAI Text with Neon Effect
                    val annotatedText = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFFF00DE),
                                shadow = Shadow(
                                    color = Color(0xFFFF00DE).copy(alpha = glowIntensity),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 20f
                                )
                            )
                        ) {
                            append("Octa")
                        }
                        
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF0073FF),
                                shadow = Shadow(
                                    color = Color(0xFF0073FF).copy(alpha = glowIntensity),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 20f
                                )
                            )
                        ) {
                            append("AI")
                        }
                    }
                    
                    Text(
                        text = annotatedText,
                        style = TextStyle(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Slogan with fade-in animation
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showSlogan,
                        enter = androidx.compose.animation.fadeIn(
                            animationSpec = tween(1500)
                        ) + androidx.compose.animation.slideInVertically(
                            animationSpec = tween(1500),
                            initialOffsetY = { it / 2 }
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "From depths where even light cannot escape, melodies emerge",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFD1D5DB),
                                    letterSpacing = 1.sp,
                                    shadow = Shadow(
                                        color = Color(0xFF3B82F6).copy(alpha = 0.3f),
                                        offset = Offset(0f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Alt metin
                            Text(
                                text = "AI-Powered Music Creation",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF),
                                    letterSpacing = 1.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Loading indicator
            androidx.compose.animation.AnimatedVisibility(
                visible = showSlogan,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = tween(1000)
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animasyonlu loading dots
                    repeat(3) { index ->
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = index * 200),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot_$index"
                        )
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .alpha(dotAlpha)
                                .background(
                                    color = when (index) {
                                        0 -> Color(0xFFEC4899)
                                        1 -> Color(0xFF8B5CF6)
                                        else -> Color(0xFF3B82F6)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}