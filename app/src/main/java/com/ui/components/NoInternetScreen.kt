package com.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * İnternet bağlantısı olmadığında gösterilecek ekran.
 * 
 * @param isVisible Ekranın görünür olup olmadığını belirler.
 * @param onRetryClick Yeniden dene butonuna tıklandığında çağrılacak callback.
 */
@Composable
fun NoInternetScreen(
    isVisible: Boolean,
    onRetryClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
        exit = fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animasyonlu WiFi ikonu
                NoInternetAnimation()
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Başlık
                Text(
                    text = "İnternet Bağlantısı Yok",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Açıklama
                Text(
                    text = "Lütfen internet bağlantınızı kontrol edin ve tekrar deneyin.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Yeniden dene butonu için interaktif durum
                var isButtonPressed by remember { mutableStateOf(false) }
                
                // Buton animasyonu için değerler
                val buttonScale by animateFloatAsState(
                    targetValue = if (isButtonPressed) 0.95f else 1f,
                    animationSpec = tween(
                        durationMillis = if (isButtonPressed) 100 else 200,
                        easing = if (isButtonPressed) FastOutSlowInEasing else LinearOutSlowInEasing
                    ),
                    label = "buttonScale"
                )
                
                val buttonColor = MaterialTheme.colorScheme.primary
                val buttonGlowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                
                // Yeniden dene butonu
                Button(
                    onClick = {
                        // Basma durumunu simüle etmek için
                        isButtonPressed = true
                        
                        // Butona basma hissini vermek için kısa bir gecikme ekliyoruz
                        scope.launch {
                            delay(150)
                            isButtonPressed = false
                            onRetryClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .scale(buttonScale)
                        .shadow(
                            elevation = if (isButtonPressed) 2.dp else 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = buttonGlowColor
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = buttonColor.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Yeniden Dene",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * İnternet bağlantısı yok animasyonu.
 */
@Composable
fun NoInternetAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    // Ana animasyon için
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Dalgalar için
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveScale1"
    )
    
    val waveAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    
    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveScale2"
    )
    
    // Yanıp sönen parlama efekti
    var isGlowing by remember { mutableStateOf(false) }
    val glowAlpha by animateFloatAsState(
        targetValue = if (isGlowing) 0.7f else 0.2f,
        animationSpec = tween(500),
        label = "glow"
    )
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            isGlowing = !isGlowing
        }
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Dalgalar
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(waveScale1)
                .alpha(waveAlpha1)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
        )
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(waveScale2)
                .alpha(waveAlpha2)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
        )
        
        // Ana ikon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .alpha(glowAlpha)
            )
            
            Icon(
                imageVector = Icons.Filled.WifiOff,
                contentDescription = "İnternet Bağlantısı Yok",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(60.dp)
            )
        }
        
        // Küçük bulut ikonları
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier
                .size(40.dp)
                .offset(60.dp, -30.dp)
                .graphicsLayer {
                    this.rotationZ = 15f
                }
        )
        
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier
                .size(30.dp)
                .offset(-70.dp, 30.dp)
                .graphicsLayer {
                    this.rotationZ = -10f
                }
        )
    }
} 