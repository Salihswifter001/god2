package com

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * OCTA Fütüristik Logo Composable
 * Modern, animasyonlu ve fütüristik bir OCTA logosu
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun OctaFuturisticLogo() {
    // Özel yazı tipi tanımlama - Projenize Orbitron fontunu eklemeniz gerekir
    // Res klasöründe font/orbitron_bold.ttf olarak ekleyebilirsiniz
    // Gerçek font referansı (Projenize eklenmeli):
    // val orbitronFont = FontFamily(Font(R.font.orbitron_bold, FontWeight.Bold))
    // Önizleme ve genel kullanım için geçici font:
    val orbitronFont = FontFamily.SansSerif // Doğrudan atama yapıldı
    
    // Temel değişkenler için state'ler
    var lineProgress by remember { mutableStateOf(0f) }
    val lettersVisible = remember { mutableStateListOf(false, false, false, false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Başlangıç animasyonu
    LaunchedEffect(Unit) {
        // Çizgi animasyonu
        val lineAnim = Animatable(0f)
        coroutineScope.launch {
            lineAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(2000, easing = LinearEasing)
            ) { 
                lineProgress = this.value
            }
        }
        
        // Çizgi tamamlandığında harfleri sırayla göster
        delay(200)
        for (i in 0..3) {
            delay((i + 1) * 100L)
            lettersVisible[i] = true
        }
    }
    
    // Sürekli animasyonlar
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    
    // Parıldama animasyonu
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowIntensity"
    )
    
    // Renk tonu değişimi animasyonu (mavi aralığında)
    val hue by infiniteTransition.animateFloat(
        initialValue = 200f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hue"
    )
    
    // Işıma faktörü hesaplama (sinüs dalgası)
    val getGlowFactor = { offset: Float ->
        0.4f + 0.6f * sin(PI.toFloat() * 2f * ((glowIntensity + offset) % 1f)).toFloat()
    }
    
    // Ana container
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Logo arkaplanı
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .blur(30.dp)
            ) {
                // Radyal gradyan
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.hsv(hue, 1f, 0.3f).copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.maxDimension / 2
                    ),
                    center = center,
                    radius = size.maxDimension / 2
                )
            }
            
            // Merkez ışıma
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .blur(2.dp)
                    .background(
                        color = Color.hsv(hue, 1f, 0.7f).copy(alpha = getGlowFactor(0f)),
                        shape = CircleShape // Doğru referans
                    )
                    .align(Alignment.Center)
            )
            
            // Harfler
            Row(
                modifier = Modifier.padding(vertical = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                val letters = listOf('O', 'C', 'T', 'A')
                
                letters.forEachIndexed { index, letter ->
                    // Harf animasyonları için Box
                    // Not: animateEnterExit Jetpack Compose'un stabil bir parçası değil.
                    // Gerçek bir implementasyon için farklı bir yaklaşım gerekebilir.
                    // Şimdilik basit alpha animasyonu kullanalım.
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (lettersVisible[index]) 1f else 0f
                                // İsteğe bağlı: Daha yumuşak giriş için scale animasyonu eklenebilir
                                // transformOrigin = TransformOrigin.Center
                                // scaleX = if (lettersVisible[index]) 1f else 0.8f
                                // scaleY = if (lettersVisible[index]) 1f else 0.8f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Ana harf
                        Text(
                            text = letter.toString(),
                            style = TextStyle(
                                fontSize = 70.sp,
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.hsv(hue - 10, 1f, 0.95f),
                                        Color.hsv(hue, 1f, 0.8f),
                                        Color.hsv(hue + 10, 1f, 0.6f)
                                    )
                                )
                            )
                        )
                        
                        // Işıma efekti
                        Text(
                            text = letter.toString(),
                            style = TextStyle(
                                fontSize = 70.sp,
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Bold,
                                color = Color.hsv(hue, 1f, 0.7f)
                                    .copy(alpha = getGlowFactor(index * 0.25f) * 0.5f)
                            ),
                            modifier = Modifier.blur(8.dp)
                        )
                        
                        // Dijital çizgi
                        if (lettersVisible[index]) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter) // Harfin altına hizala
                                    .offset(y = 30.dp) // Biraz aşağı kaydır
                                    .width(40.dp)
                                    .height(2.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.hsv(hue, 1f, 0.7f)
                                                    .copy(alpha = getGlowFactor(index * 0.25f) * 0.3f),
                                                Color.hsv(hue, 1f, 0.8f)
                                                    .copy(alpha = getGlowFactor(index * 0.25f) * 0.6f),
                                                Color.hsv(hue, 1f, 0.7f)
                                                    .copy(alpha = getGlowFactor(index * 0.25f) * 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(1.dp) // Doğru referans
                                    )
                                    .blur(1.dp)
                            )
                        }
                        
                        // Alt nokta
                        if (lettersVisible[index]) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter) // Harfin altına hizala
                                    .offset(y = 40.dp) // Çizginin biraz altına
                                    .size(4.dp)
                                    .background(
                                        color = Color.hsv(hue, 1f, 0.8f)
                                            .copy(alpha = getGlowFactor(index * 0.25f) * 0.7f),
                                        shape = CircleShape // Doğru referans
                                    )
                                    .blur(1.dp)
                            )
                        }
                    }
                }
            }
            
            // Yatay çizgi animasyonu
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(lineProgress)
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.hsv(hue - 30, 1f, 0.7f).copy(alpha = 0.4f),
                                Color.hsv(hue, 1f, 0.8f).copy(alpha = 0.8f),
                                Color.hsv(hue + 30, 1f, 0.7f).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .blur(2.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun OctaFuturisticLogoPreview() {
    OctaFuturisticLogo()
} 