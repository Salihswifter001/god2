package com.ui.player

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ui.NeonPlayer
import java.io.File
import kotlinx.coroutines.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import com.data.model.AudioModel
import com.ui.player.PlayerViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.ui.player.effects.BlueGradientBackground
import com.ui.player.utils.MusicUtils
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import com.aihackathonkarisacikartim.god2.player.MediaPlayerManager
import java.util.concurrent.TimeUnit
import kotlin.math.PI

/**
 * Screen that combines NeonPlayer UI with real media player functionality
 */
@Composable
fun NeonPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(),
    songTitle: String? = null,
    artistName: String? = null,
    albumCoverUrl: String? = null,
    mediaUri: Uri? = null,
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Track player states
    val isPlaying by viewModel.isPlaying
    val currentPosition by viewModel.currentPosition
    val duration by viewModel.duration
    
    // Album cover animation
    val albumArtScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "albumScale"
    )
    
    // Neon glow effect
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.7f else 0.4f,
        animationSpec = tween(2000, easing = LinearEasing),
        label = "glowAlpha"
    )
    
    // If song information is provided from outside, play this song
    LaunchedEffect(mediaUri, songTitle) {
        println("NeonPlayerScreen - songTitle: $songTitle, artistName: $artistName, mediaUri: $mediaUri")
        if (mediaUri != null && songTitle != null && songTitle.isNotEmpty()) {
            println("Playing media: $mediaUri")
            viewModel.playMedia(
                uri = mediaUri,
                title = songTitle,
                artist = artistName ?: "Unknown Artist"
            )
        } else {
            println("Cannot play - mediaUri: $mediaUri, songTitle: $songTitle")
        }
    }
    
    // Main player container
    Box(modifier = Modifier.fillMaxSize()) {
        // Blue gradient background
        BlueGradientBackground(
            modifier = Modifier.fillMaxSize(),
            isPlaying = isPlaying
        )
        
        // Back button
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(top = 50.dp, start = 16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
        
        // Player content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(56.dp)) // Space for back button
            
            // Album cover
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Album cover background glow
                if (!albumCoverUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .blur(20.dp)
                            .alpha(glowAlpha * 0.5f)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(albumCoverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Album cover image
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer {
                            scaleX = albumArtScale
                            scaleY = albumArtScale
                            shadowElevation = 16f
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF203165),
                                    Color(0xFF0A1025)
                                )
                            )
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(albumCoverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Show music note icon if album cover is not available
                    if (albumCoverUrl.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF3F9BF9),
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center)
                        )
                    }
                    
                    // Outer glow when music is playing
                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 1.5f.dp,
                                    color = Color(0xFF3F9BF9),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Song information
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Song name
                Text(
                    text = songTitle ?: "Unknown Song",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Artist name
                Text(
                    text = artistName ?: "Unknown Artist",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress bar and time information
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Time information
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = MusicUtils.formatDuration(currentPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        
                        Text(
                            text = MusicUtils.formatDuration(duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Progress bar
                    var isSeeking by remember { mutableStateOf(false) }
                    var seekPosition by remember { mutableStateOf(0f) }
                    
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(vertical = 8.dp)
                    ) {
                        // Progress background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .align(Alignment.Center)
                        )
                        
                        // Progress bar
                        val progress = if (isSeeking) {
                            seekPosition
                        } else {
                            if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF3F9BF9),
                                            Color(0xFF2B5EFF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .align(Alignment.CenterStart)
                        )
                        
                        // Dokunma alanı
                        Slider(
                            value = progress,
                            onValueChange = { newValue ->
                                isSeeking = true
                                seekPosition = newValue
                            },
                            onValueChangeFinished = {
                                isSeeking = false
                                val newPosition = (seekPosition * duration).toLong()
                                viewModel.seekTo(newPosition)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3F9BF9),
                                activeTrackColor = Color.Transparent,
                                inactiveTrackColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Küçük kontrol bilgilerini ekleyin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Forward/backward seeking hint
                    Text(
                        text = "Slide to seek forward/backward",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Playback controls
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Previous song
                    IconButton(
                        onClick = { viewModel.playPrevious() },
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = Color(0xFF152A50),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Play/Pause
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF3F9BF9),
                                        Color(0xFF2B5EFF)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Next song
                    IconButton(
                        onClick = { viewModel.playNext() },
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = Color(0xFF152A50),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp)) // Space at the bottom
        }
    }
}

@Preview
@Composable
fun PlayerScreenPreview() {
    MaterialTheme {
        NeonPlayerScreen(
            songTitle = "Sample Song",
            artistName = "Sample Artist",
            albumCoverUrl = null
        )
    }
}

// Define screen types
enum class WindowType {
    Portrait,
    Landscape
}

// Window size tracking class
@Composable
fun rememberWindowInfo(): WindowInfo {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        WindowInfo(
            orientation = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 
                WindowType.Landscape else WindowType.Portrait
        )
    }
}

// Window information class
data class WindowInfo(
    val orientation: WindowType
) 