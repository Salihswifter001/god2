package com

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun NeonGlowButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontFamily: FontFamily = FontFamily.SansSerif, // Font ailesi parametresi
    textStyle: TextStyle = TextStyle( // Opsiyonel tam TextStyle
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonGlow")

    // Neon Mavi tonları (açıktan koyuya doğru ayarlanabilir)
    val baseNeonColor = Color(0f, 0.8f, 1f) // Ana parlak mavi

    // Parlama yoğunluğu animasyonu (shadow alpha ve border alpha için)
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, // Başlangıç alfa
        targetValue = 0.7f, // Bitiş alfa
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing), // Yumuşak geçiş
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Hafif renk tonu değişimi animasyonu (opsiyonel, daha dinamik bir görünüm için)
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f, // Hue'yu hafifçe değiştir
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )

    // Ana neon rengini hafifçe değiştir
    val angleInRadians = (colorShift * PI.toFloat() / 180f)
    val animatedNeonColor = baseNeonColor.copy(
        blue = (baseNeonColor.blue + sin(angleInRadians.toDouble()).toFloat() * 0.1f).coerceIn(0f, 1f)
    )

    // Butonun köşe yuvarlaklığı
    val buttonShape = RoundedCornerShape(28.dp)

    // Gölge rengi (animasyonlu alfa ile)
    val shadowColor = animatedNeonColor.copy(alpha = glowAlpha * 0.6f) // Alfa animasyonunu gölgeye uygula

    // Kenarlık fırçası (animasyonlu alfa ile)
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            animatedNeonColor.copy(alpha = glowAlpha * 0.8f),
            animatedNeonColor.copy(alpha = glowAlpha * 0.4f)
        )
    )
    
    // Normal durum için arka plan fırçası (koyu gradyan)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.5f),
            Color(0.05f, 0.05f, 0.15f, 0.7f) // Koyu mavi-siyah tonu
        )
    )
    
    // Devre dışı durum rengi
    val disabledColor = Color.DarkGray.copy(alpha = 0.5f)


    Box(
        modifier = modifier
            .height(56.dp) // Yükseklik ayarı
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = buttonShape,
                spotColor = if (enabled) shadowColor else Color.Transparent, // Animasyonlu gölge rengi
                ambientColor = if (enabled) shadowColor.copy(alpha = shadowColor.alpha * 0.5f) else Color.Transparent
            )
            .clip(buttonShape) // Gölgenin ve arka planın şekle uyması için
            .background(
                brush = if (enabled) backgroundBrush else SolidColor(disabledColor),
                shape = buttonShape
            )
            .border(
                border = BorderStroke(
                    width = if (enabled) 1.5.dp else 1.dp, // Hafif kalın kenarlık
                    brush = if (enabled) borderBrush else SolidColor(disabledColor.copy(alpha=0.8f)) // Animasyonlu kenarlık
                ),
                shape = buttonShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = textStyle.copy( // Gelen stili kopyala ve üzerine yaz
                 fontFamily = fontFamily,
                 color = if (enabled) Color.White else Color.Gray.copy(alpha = 0.7f) // Duruma göre metin rengi
             )
        )
    }
}


// Önizleme için
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun NeonGlowButtonPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Önizleme arka planı
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NeonGlowButton(onClick = {}, text = "Neon Button")
            NeonGlowButton(onClick = {}, text = "Disabled Button", enabled = false)
        }
    }
} 