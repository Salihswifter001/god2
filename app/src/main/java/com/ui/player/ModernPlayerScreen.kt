package com.ui.player

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ui.SongItem
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * Modern Music Player Screen with Vinyl Record Animation
 */
@Composable
fun ModernPlayerScreen(
    navController: NavController,
    songTitle: String? = null,
    artistName: String? = null,
    albumCoverUrl: String? = null,
    mediaUri: Uri? = null,
    userSongs: List<SongItem> = emptyList(), // User's other songs
    viewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Player states
    val isPlaying by viewModel.isPlaying
    val currentPosition by viewModel.currentPosition
    val duration by viewModel.duration
    // Initialize current song index based on the song title
    val currentSongIndex = remember { 
        val initialIndex = userSongs.indexOfFirst { it.title == songTitle }
        mutableStateOf(if (initialIndex != -1) initialIndex else 0)
    }
    
    // Get current song from index
    val currentSong = remember(currentSongIndex.value, userSongs) {
        userSongs.getOrNull(currentSongIndex.value) ?: userSongs.find { it.title == songTitle } ?: userSongs.getOrNull(0)
    }
    
    // Animation for vinyl rotation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 3000 else 100000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRotation"
    )
    
    // Play the song when screen opens
    LaunchedEffect(mediaUri, songTitle) {
        if (mediaUri != null && songTitle != null && songTitle.isNotEmpty()) {
            viewModel.playMedia(
                uri = mediaUri,
                title = songTitle,
                artist = artistName ?: "Unknown Artist"
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0033), // Dark purple top
                        Color(0xFF0A0014), // Very dark purple
                        Color(0xFF000000)  // Black bottom
                    )
                )
            )
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp) // Space for bottom bar
        ) {
            // Top Bar
            TopBar(
                onBackClick = { navController.popBackStack() },
                onMenuClick = { /* Handle menu */ }
            )
            
            // Vinyl Record
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                VinylRecord(
                    rotation = vinylRotation,
                    albumCoverUrl = currentSong?.albumArt ?: albumCoverUrl,
                    isPlaying = isPlaying
                )
            }
            
            // Song Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentSong?.title ?: songTitle ?: "Unknown Track",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = currentSong?.artist ?: artistName ?: "Unknown Artist",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Seek Bar
            SeekBar(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { position ->
                    viewModel.seekTo(position)
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Media Controls
            MediaControls(
                isPlaying = isPlaying,
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = {
                    val nextIndex = (currentSongIndex.value + 1) % userSongs.size
                    val nextSong = userSongs.getOrNull(nextIndex)
                    if (nextSong != null) {
                        currentSongIndex.value = nextIndex
                        nextSong.mediaUri?.let { uri ->
                            viewModel.playMedia(
                                uri = Uri.parse(uri),
                                title = nextSong.title,
                                artist = nextSong.artist
                            )
                        }
                    }
                },
                onPrevious = {
                    val prevIndex = if (currentSongIndex.value > 0) {
                        currentSongIndex.value - 1
                    } else {
                        userSongs.size - 1
                    }
                    val prevSong = userSongs.getOrNull(prevIndex)
                    if (prevSong != null) {
                        currentSongIndex.value = prevIndex
                        prevSong.mediaUri?.let { uri ->
                            viewModel.playMedia(
                                uri = Uri.parse(uri),
                                title = prevSong.title,
                                artist = prevSong.artist
                            )
                        }
                    }
                },
                onRewind = { viewModel.skipBackward() },
                onFastForward = { viewModel.skipForward() }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Related Tracks Section
            RelatedTracks(
                tracks = userSongs,
                currentTrackId = currentSong?.id,
                onTrackClick = { song ->
                    // Update the current song index
                    val songIndex = userSongs.indexOf(song)
                    if (songIndex != -1) {
                        currentSongIndex.value = songIndex
                    }
                    
                    // Play the selected song
                    song.mediaUri?.let { uri ->
                        viewModel.playMedia(
                            uri = Uri.parse(uri),
                            title = song.title,
                            artist = song.artist
                        )
                    }
                }
            )
        }
        
        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            com.ui.components.BottomNavBar(
                navController = navController,
                currentRoute = "music_player"
            )
        }
    }
}

@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun VinylRecord(
    rotation: Float,
    albumCoverUrl: String?,
    isPlaying: Boolean
) {
    Box(
        modifier = Modifier
            .size(280.dp)
            .graphicsLayer {
                rotationZ = if (isPlaying) rotation else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        // Vinyl grooves
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2
            
            // Draw concentric circles for vinyl grooves
            for (i in 0..40) {
                val radius = maxRadius * (1f - i / 40f)
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f + (i % 2) * 0.1f),
                    radius = radius,
                    center = center,
                    style =     Stroke(width = 1.dp.toPx())
                )
            }
        }
        
        // Center hole/album art
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            if (albumCoverUrl != null) {
                AsyncImage(
                    model = albumCoverUrl,
                    contentDescription = "Album Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // Glowing edge effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(140f, 140f)
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun MediaControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRewind: () -> Unit,
    onFastForward: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Track
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Rewind
        IconButton(
            onClick = onRewind,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FastRewind,
                contentDescription = "Rewind",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Play/Pause
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF00DE), // Hot pink
                            Color(0xFFBC06F9)  // Electric purple
                        )
                    )
                )
                .clickable { onPlayPause() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        
        // Fast Forward
        IconButton(
            onClick = onFastForward,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = "Fast Forward",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Next Track
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun RelatedTracks(
    tracks: List<SongItem>,
    currentTrackId: String?,
    onTrackClick: (SongItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Related Tracks",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(tracks.filter { it.id != currentTrackId }) { track ->
                TrackCard(
                    track = track,
                    onClick = { onTrackClick(track) }
                )
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: SongItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                if (track.albumArt != null) {
                    AsyncImage(
                        model = track.albumArt,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default album art
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF9C27B0),
                                        Color(0xFFE91E63)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Track Title
            Text(
                text = track.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            // Artist
            Text(
                text = track.artist,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        var isUserSeeking by remember { mutableStateOf(false) }
        var seekPosition by remember { mutableStateOf(0f) }
        
        // Progress slider
        Slider(
            value = if (isUserSeeking) seekPosition else {
                if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
            },
            onValueChange = { value ->
                isUserSeeking = true
                seekPosition = value
            },
            onValueChangeFinished = {
                isUserSeeking = false
                onSeek((seekPosition * duration).toLong())
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF00DE), // Hot pink
                activeTrackColor = Color(0xFFFF00DE), // Hot pink gradient
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Time labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Text(
                text = formatTime(duration),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// Helper function to format time
private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}