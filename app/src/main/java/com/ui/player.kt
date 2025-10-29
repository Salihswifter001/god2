package com.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.media.AudioAttributes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ui.player.PlayerViewModel
import com.aihackathonkarisacikartim.god2.player.MediaPlayerManager
import androidx.media3.common.MediaItem
import androidx.media3.common.Player as ExoPlayer
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modern horizontal music player with black-blue neon theme
 */
@Composable
fun NeonPlayer(
    songTitle: String,
    artistName: String,
    albumCover: Int? = null,
    isPlaying: Boolean = false,
    duration: Long = 180000, // 3 minutes (in milliseconds)
    currentPosition: Long = 90000, // 1.5 minutes (in milliseconds)
    onPlayPauseClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onSeekTo: (Float) -> Unit = {},
    onExpandClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Neon color palette definitions
    val deepBlack = Color(0xFF050510)
    val darkBlue = Color(0xFF0A1929)
    val neonBlue = Color(0xFF0088FF)
    val electricBlue = Color(0xFF00CCFF)
    val brightCyan = Color(0xFF00FFFF)
    val neonPink = Color(0xFFFF00FF)
    
    // Infinite transition for animation values
    val infiniteTransition = rememberInfiniteTransition(label = "playerAnimations")
    
    // Brightness effect animation
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )
    
    // Pulse animation
    val pulseIntensity by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseIntensity"
    )
    
    // Offset for mist effect animation
    val mistOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mistOffset"
    )
    
    // Second mist effect with different speed
    val mistOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mistOffset2"
    )
    
    // Song cover rotation animation
    val albumRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 20000 else 20000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "albumRotation"
    )
    
    // Stop album rotation animation when not playing
    val actualRotation = if (isPlaying) albumRotation else 0f
    
    // Progress value (0-1 range)
    val progress = currentPosition.toFloat() / duration.toFloat()
    val density = LocalDensity.current
    
    // Time format function
    fun formatTime(timeMs: Long): String {
        val seconds = (timeMs / 1000) % 60
        val minutes = (timeMs / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    // Main container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        deepBlack,
                        darkBlue.copy(alpha = 0.7f),
                        deepBlack
                    )
                )
            )
            .drawBehind {
                // Bright edge effect
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            neonBlue.copy(alpha = 0.1f * glowIntensity),
                            electricBlue.copy(alpha = 0.2f * glowIntensity),
                            neonBlue.copy(alpha = 0.1f * glowIntensity)
                        )
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 4f)
                )
            }
            .padding(12.dp)
    ) {
        // Background mist effect
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.2f)
        ) {
            // Top mist layer
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        neonBlue.copy(alpha = 0.05f),
                        electricBlue.copy(alpha = 0.1f),
                        neonBlue.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startX = -size.width + mistOffset % (size.width * 2),
                    endX = size.width + mistOffset % (size.width * 2)
                ),
                size = size,
                blendMode = BlendMode.SrcOver
            )
            
            // Bottom mist layer (different speed)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        electricBlue.copy(alpha = 0.03f),
                        brightCyan.copy(alpha = 0.07f),
                        electricBlue.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    startX = size.width - (mistOffset2 * 0.7f) % (size.width * 2),
                    endX = size.width * 2 - (mistOffset2 * 0.7f) % (size.width * 2)
                ),
                size = size,
                blendMode = BlendMode.Screen
            )
        }
        
        // Main content: Album cover, song info and controls
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album cover (rotating animated disc)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = neonBlue.copy(alpha = 0.5f * glowIntensity)
                    )
            ) {
                // Animasyonlu dönen albüm
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(darkBlue)
                        .drawBehind {
                            // İç parıltı halkası
                            drawCircle(
                                color = neonBlue.copy(alpha = 0.5f * glowIntensity),
                                radius = size.minDimension / 2,
                                style = Stroke(width = 2f)
                            )
                        }
                ) {
                    // Albüm görseli
                    if (albumCover != null) {
                        Image(
                            painter = painterResource(id = albumCover),
                            contentDescription = "Albüm Kapağı",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(actualRotation)
                        )
                    } else {
                        // Varsayılan albüm ikonu
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = "Albüm",
                            tint = electricBlue.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                                .rotate(actualRotation)
                        )
                    }
                    
                    // Center hole
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(deepBlack)
                            .border(
                                width = 1.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        electricBlue.copy(alpha = 0.7f * glowIntensity),
                                        neonBlue.copy(alpha = 0.3f * glowIntensity)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }
            }
            
            // Middle section: Song information and progress bar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                // Song name
                Text(
                    text = songTitle,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                
                // Artist name
                Text(
                    text = artistName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // İlerleme çubuğu bölümü
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    // İlerleme çubuğu arka planı
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    // İlerleme çubuğuna dokunulduğunda pozisyonu güncelle
                                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                    onSeekTo(newProgress)
                                }
                            }
                    ) {
                        // Arka plan
                        drawRoundRect(
                            color = deepBlack.copy(alpha = 0.6f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                            size = Size(size.width, 4.dp.toPx())
                        )
                        
                        // Progress background (with mist effect)
                        val progressWidth = size.width * progress
                        
                        // Draw animated mist wave
                        val path = Path()
                        path.moveTo(0f, 2.dp.toPx())
                        
                        // Wave effect (along progress bar)
                        for (x in 0..progressWidth.toInt() step 4) {
                            val xPos = x.toFloat()
                            val waveHeight = 1.dp.toPx() * glowIntensity
                            val yOffset = sin((xPos * 0.1f) + mistOffset / 30) * waveHeight
                            path.lineTo(xPos, 2.dp.toPx() + yOffset)
                        }
                        
                        path.lineTo(progressWidth, 2.dp.toPx())
                        path.lineTo(progressWidth, 4.dp.toPx())
                        path.lineTo(0f, 4.dp.toPx())
                        path.close()
                        
                        // Draw progress fill (wave-shaped)
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    neonBlue,
                                    electricBlue,
                                    brightCyan,
                                    neonPink.copy(alpha = 0.7f)
                                )
                            )
                        )
                        
                        // İlerleme parlaklık efekti
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    neonBlue.copy(alpha = 0.3f * glowIntensity),
                                    electricBlue.copy(alpha = 0.5f * glowIntensity),
                                    brightCyan.copy(alpha = 0.3f * glowIntensity)
                                )
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                            size = Size(progressWidth, 4.dp.toPx()),
                            style = Stroke(width = 2f)
                        )
                        
                        // Sis efekti parçacıkları
                        repeat(15) { i ->
                            val xPos = mistOffset.rem(progressWidth) + i * (progressWidth / 15)
                            if (xPos < progressWidth) {
                                val particleSize = (1 + Random.nextFloat() * 2).dp.toPx()
                                val yPos = 2.dp.toPx() + (Random.nextFloat() * 1.5f).dp.toPx() * sin(mistOffset2 / 100 + i)
                                
                                drawCircle(
                                    color = brightCyan.copy(alpha = (0.2f + Random.nextFloat() * 0.4f) * glowIntensity),
                                    radius = particleSize,
                                    center = Offset(xPos, yPos)
                                )
                            }
                        }
                    }
                    
                    // Time information
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Geçen süre
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                        
                        // Toplam süre
                        Text(
                            text = formatTime(duration),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            // Right side: Control buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                // Previous song button
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    neonBlue.copy(alpha = 0.3f),
                                    darkBlue.copy(alpha = 0.1f)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Song",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Play/Pause button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    electricBlue.copy(alpha = 0.8f),
                                    neonBlue.copy(alpha = 0.5f),
                                    darkBlue.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .drawBehind {
                            // Parıltı efekti
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        brightCyan.copy(alpha = 0.4f * glowIntensity),
                                        neonBlue.copy(alpha = 0.2f * glowIntensity),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension * 0.7f,
                                alpha = if (isPlaying) 0.8f else 0.4f
                            )
                        }
                        .scale(if (isPlaying) pulseIntensity else 1f)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Next song button
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    neonBlue.copy(alpha = 0.3f),
                                    darkBlue.copy(alpha = 0.1f)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Song",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Expand button (optional)
                IconButton(
                    onClick = onExpandClick,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun NeonPlayerPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.Black)
            .padding(16.dp)
    ) {
        NeonPlayer(
            songTitle = "Neon Dreams",
            artistName = "Modern Echoes",
            isPlaying = true,
            currentPosition = 120000,
            duration = 240000
        )
    }
} 