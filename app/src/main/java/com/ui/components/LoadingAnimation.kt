package com.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

/**
 * Müzik oluşturma sırasında gösterilen özel yükleme animasyonu
 */
@Composable
fun MusicLoadingAnimation(
    message: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingTransition")
    
    // Ana döngü animasyonu
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Ses dalgası animasyonları
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    
    // Nota pozisyon animasyonları
    val notePositionX1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "notePositionX1"
    )
    
    val notePositionY1 by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "notePositionY1"
    )
    
    val notePositionX2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "notePositionX2"
    )
    
    val notePositionY2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "notePositionY2"
    )
    
    val notePositionX3 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "notePositionX3"
    )
    
    val notePositionY3 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "notePositionY3"
    )
    
    // Nota döndürme animasyonları
    val noteRotation1 by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noteRotation1"
    )
    
    val noteRotation2 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noteRotation2"
    )
    
    val noteRotation3 by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noteRotation3"
    )
    
    // Nota boyut animasyonları
    val noteScale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noteScale1"
    )
    
    val noteScale2 by infiniteTransition.animateFloat(
        initialValue = 1.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noteScale2"
    )
    
    val noteScale3 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noteScale3"
    )
    
    // Parlaklık pulsasyonu
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Hue renk animasyonu
    val hue by infiniteTransition.animateFloat(
        initialValue = 200f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hue"
    )
    
    val primaryColor = Color.hsv(hue, 0.9f, glow)
    val secondaryColor = Color.hsv((hue + 120) % 360, 0.8f, glow)
    val tertiaryColor = Color.hsv((hue + 240) % 360, 0.7f, glow)
    
    // Mesajı satırlara böl
    val lines = message.split("\n")
    val mainMessage = if (lines.isNotEmpty()) lines[0] else ""
    val subMessage = if (lines.size > 1) lines[1] else ""
    
    Card(
        modifier = modifier
            .padding(16.dp)
            .widthIn(max = 320.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0f, 0.02f, 0.05f, 0.85f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Arka plan dalgalı daireler
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            // Hardware acceleration için basit ayar - uyumlu versiyon
                            alpha = 0.99f
                        )
                ) {
                    // Dönen dalgalı daire
                    val radius = size.minDimension / 2.2f
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    
                    // Dalgalı çember için optimizasyon - daha az nokta kullanımı
                    val path = Path().apply {
                        val points = 90 // 180'den 90'a düşürüldü, görsel fark olmaz
                        for (i in 0..points) {
                            val angle = (i.toFloat() / points) * 2f * PI.toFloat() + rotation * (PI.toFloat() / 180f)
                            val waveOffset = sin(angle * 10 + wave1) * 8f + cos(angle * 8 + wave2) * 5f
                            val x = centerX + (radius + waveOffset) * kotlin.math.cos(angle)
                            val y = centerY + (radius + waveOffset) * kotlin.math.sin(angle)
                            
                            if (i == 0) {
                                moveTo(x, y)
                            } else {
                                lineTo(x, y)
                            }
                        }
                        close()
                    }
                    
                    // İç dolgu ekleniyor
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.15f),
                                secondaryColor.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY),
                            radius = radius * 1.2f
                        )
                    )
                    
                    // Gradient stroke ile çiz (daha kalın çizgi ve parlak renkler)
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(primaryColor, secondaryColor, tertiaryColor, primaryColor),
                            start = Offset(centerX - radius, centerY - radius),
                            end = Offset(centerX + radius, centerY + radius)
                        ),
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Ses dalgaları (daha modern çoklu dalgalar)
                    val baseWaveY = centerY + radius * 0.6f
                    
                    // Dalga çizimini optimize et - daha büyük step değerleri ve daha az dalga
                    // Birinci dalga grubu
                    for (offset in -20..20 step 20) { // 10'dan 20'ye çıkarıldı
                        val waveY = baseWaveY + offset
                        val intensity = 1f - (kotlin.math.abs(offset) / 30f)
                        val wavePath = Path().apply {
                            moveTo(0f, waveY)
                            // Dalga noktalarının sıklığını azalt ama görünüşü koru
                            for (x in 0..size.width.toInt() step 15) { // 5'ten 15'e çıkarıldı
                                val y = waveY + sin(x * 0.05f + wave1 + offset * 0.1f) * (25f * intensity)
                                lineTo(x.toFloat(), y)
                            }
                        }
                        
                        drawPath(
                            path = wavePath,
                            color = primaryColor.copy(alpha = 0.3f * intensity),
                            style = Stroke(width = (1.5f * intensity).dp.toPx())
                        )
                    }
                    
                    // İkinci dalga grubu (daha farklı frekans)
                    for (offset in -15..15 step 15) { // 10'dan 15'e çıkarıldı
                        val waveY = baseWaveY - 15f + offset
                        val intensity = 1f - (kotlin.math.abs(offset) / 20f)
                        val wavePath = Path().apply {
                            moveTo(0f, waveY)
                            // Dalga noktalarının sıklığını azalt
                            for (x in 0..size.width.toInt() step 15) { // 5'ten 15'e çıkarıldı
                                val y = waveY + sin(x * 0.07f + wave2 + offset * 0.08f) * (20f * intensity)
                                lineTo(x.toFloat(), y)
                            }
                        }
                        
                        drawPath(
                            path = wavePath,
                            color = secondaryColor.copy(alpha = 0.25f * intensity),
                            style = Stroke(width = (1.2f * intensity).dp.toPx())
                        )
                    }
                }
                
                // Modern müzik notaları (konumlandırılmış, döndürülmüş ve ölçeklendirilmiş)
                // Nota 1
                MusicNote(
                    color = primaryColor,
                    scale = noteScale1,
                    rotation = noteRotation1,
                    offsetX = notePositionX1,
                    offsetY = notePositionY1
                )
                
                // Nota 2
                MusicNote(
                    color = secondaryColor,
                    scale = noteScale2,
                    rotation = noteRotation2,
                    offsetX = notePositionX2,
                    offsetY = notePositionY2
                )
                
                // Nota 3
                MusicNote(
                    color = tertiaryColor,
                    scale = noteScale3,
                    rotation = noteRotation3,
                    offsetX = notePositionX3,
                    offsetY = notePositionY3
                )
            }
            
            // Mesaj kısmı
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = mainMessage,
                color = primaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            if (subMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subMessage,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            // Uyarı mesajı için kontrol (satır içinde "kapatmayın" ifadesi arıyoruz)
            if (lines.size > 2 && lines[2].contains("kapatmayın")) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = lines[2],
                    color = Color(0xFFFF6B6B), // Kırmızı-turuncu uyarı rengi
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Mesajın tamamının içinde "kapatmayın" ifadesi olup olmadığını kontrol et
            // (Büyük/küçük harf duyarsız arama için)
            val warningLine = lines.find { it.contains("kapatmayın", ignoreCase = true) || it.contains("KAPATMAYIN") }
            if (warningLine != null && warningLine !in listOf(mainMessage, subMessage)) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = warningLine,
                    color = Color(0xFFFF6B6B), // Kırmızı-turuncu uyarı rengi
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MusicNote(
    color: Color,
    scale: Float,
    rotation: Float,
    offsetX: Float,
    offsetY: Float
) {
    // Müzik notası çizimi
    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
    ) {
        // Nota baş kısmı (oval şekil)
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 18.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.8f)
                        )
                    )
                )
        )
        
        // Nota sapı
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(25.dp)
                .background(color)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = (-15).dp)
        )
    }
} 