package com.musicgen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text // Örnek içerik için
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp // Örnek içerik için
import androidx.compose.ui.unit.sp

/**
 * Modern siyah-neon mavi animasyonlu gradyan arka planı oluşturan Composable.
 * Bu Composable, çağrıldığı alana tam ekran yayılan bir Box döndürür.
 * İçeriği bu Box'ın içine yerleştirilmelidir.
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier, // Dışarıdan modifier alabilmesi için eklendi
    content: @Composable BoxScope.() -> Unit // İçeriği dışarıdan alabilmesi için eklendi
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arka plan renk geçişi")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradyan ilerlemesi"
    )

    val colorBlack = Color(0xFF000000)
    val colorNeonBlue = Color(0xFF00FFFF) // Neon Mavi

    BoxWithConstraints(modifier = modifier.fillMaxSize()) { // Dış modifier uygulandı
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Manuel lerp fonksiyonu kullanıldı
        val startOffsetX = lerp(-width, 0f, progress)
        val endOffsetX = lerp(width, width * 2f, progress)

        val animatedBrush = Brush.linearGradient(
            colors = listOf(colorBlack, colorNeonBlue, colorBlack, colorNeonBlue),
            start = Offset(startOffsetX, -height * 0.5f),
            end = Offset(endOffsetX, height * 1.5f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = animatedBrush),
            contentAlignment = Alignment.Center // Varsayılan olarak içeriği ortala
        ) {
            content() // Dışarıdan gelen içeriği burada göster
        }
    }
}

// Basit bir lerp fonksiyonu (Float için)
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

// Önizleme için Composable
@Preview(showBackground = true)
@Composable
fun AnimatedGradientPreview() {
    // Önizlemede arka planın nasıl görüneceğini göstermek için
    // AnimatedGradientBackground'ı çağırıp içine örnek bir içerik ekleyelim.
    AnimatedGradientBackground {
        // BoxScope içinde olduğumuz için Alignment gibi modifierları doğrudan kullanabiliriz.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Önizleme İçeriği", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Arka Plan Animasyonu", color = Color.White)
        }
    }
} 