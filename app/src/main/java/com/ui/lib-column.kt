package com.ui

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.material.icons.filled.Sort
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.material.ripple.rememberRipple
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ui.player.PlayerViewModel
import coil.compose.AsyncImage
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.border
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.content.Context
import android.widget.Toast
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

// Kategori türü için enum (daha sonra kullanılabilir)
enum class CategoryDisplayType {
    HORIZONTAL_CARDS,
    VERTICAL_LIST,
    GRID_LAYOUT
}

// Playlist data class
data class Playlist(
    val id: String,
    val name: String,
    val imageUrl: String
)

// Neon renkler için sabit sınıfı - Mor/Siyah tema (Adsız.png ile uyumlu)
object NeonColors {
    val NeonBlue = Color(0xFFAA00FF)  // Mor (ekrandaki gibi)
    val DeepBlue = Color(0xFF6600CC)  // Koyu mor
    val ElectricPink = Color(0xFFFF00FF)  // Neon pembe/mor
    val ElectricGreen = Color(0xFF00FF66) // Neon yeşil
    val SlateGray = Color(0xFF1A0033)  // Koyu mor arka plan
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFB8B8B8)  // Gri metin
    val TextTertiary = Color(0xFF888888) // Daha koyu gri
    val DarkBackground = Color(0xFF0A0014)  // Çok koyu mor/siyah
    val DarkerBackground = Color(0xFF000000) // Saf siyah
    val MidnightPurple = Color(0xFF1A0033)  // Koyu mor
    val Divider = Color(0xFF2A1A3A)  // Mor tonlu çizgi
    
    // Eski libtheme.kt'den alınan renkler - Mor tema
    val NeonPink = Color(0xFFFF00FF)  // Neon pembe/mor
    val DeepPink = Color(0xFFFF1493)  // Koyu pembe
    val HotPink = Color(0xFFFF66CC)  // Açık pembe
    val ElectricBlue = Color(0xFF00CCFF)  // Elektrik mavisi (vurgu için)
    val CyberBlue = Color(0xFF0077FF)  // Siber mavisi
    
    // Gradyan renkler
    val NeonGradientPink = listOf(NeonPink.copy(alpha = 0.8f), ElectricPink)
    val NeonGradientBlue = listOf(NeonBlue.copy(alpha = 0.8f), ElectricBlue)
    val NeonGradientPurple = listOf(NeonPink.copy(alpha = 0.5f), DeepBlue.copy(alpha = 0.7f))
}

// Özel fırçalar için sınıf
@Immutable
data class NeonBrushes(
    val primaryGradient: Brush = Brush.horizontalGradient(NeonColors.NeonGradientPink),
    val secondaryGradient: Brush = Brush.horizontalGradient(NeonColors.NeonGradientBlue),
    val accentGradient: Brush = Brush.horizontalGradient(NeonColors.NeonGradientPurple),
    val backgroundGradient: Brush = Brush.verticalGradient(
        listOf(
            Color(0xFF0A0014),   // Çok koyu mor/siyah
            Color(0xFF1A0033),   // Koyu mor
            Color(0xFF0A0014)    // Çok koyu mor/siyah
        )
    ),
    val buttonGradient: Brush = Brush.horizontalGradient(
        listOf(
            NeonColors.ElectricPink.copy(alpha = 0.8f),
            NeonColors.CyberBlue.copy(alpha = 0.8f)
        )
    )
)

// NeonTheme sınıfı ve statik kaynaklar
object NeonTheme {
    val brushes = NeonBrushes()
    val shapes = Shapes(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp)
    )
}

/**
 * Neon tema bileşeni
 */
@Composable
fun NeonMusicTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(
                primary = NeonColors.ElectricPink,
                secondary = NeonColors.NeonBlue,
                tertiary = NeonColors.ElectricGreen,
                background = NeonColors.DarkBackground,
                surface = Color(0xFF1A0033)  // Koyu mor yüzey
            )
        } else {
            lightColorScheme(
                primary = NeonColors.ElectricPink,
                secondary = NeonColors.NeonBlue,
                tertiary = NeonColors.ElectricGreen
            )
        },
        shapes = NeonTheme.shapes,
        content = content
    )
}

/**
 * Neon temalı Spotify benzeri bir müzik listesi bileşeni
 */
@Composable
fun MusicLibraryColumn(
    songs: List<SongItem>,
    onSongClick: (SongItem) -> Unit = {},
    onPlayClick: (SongItem) -> Unit = {},
    onOptionsClick: (SongItem) -> Unit = {},
    onCustomizeClick: (SongItem) -> Unit = {},
    currentlyPlayingId: String? = null,
    glowIntensity: Float = 1.0f
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0014),  // Çok koyu mor/siyah
                        Color(0xFF1A0033),  // Koyu mor (portal gibi)
                        Color(0xFF0A0014)   // Çok koyu mor/siyah
                    )
                )
            )
    ) {
        itemsIndexed(songs) { index, song ->
            val isPlaying = song.id == currentlyPlayingId
            val backgroundColor = if (isPlaying) {
                Color(0xFF2A1A3A).copy(alpha = 0.6f)  // Mor tonlu vurgu
            } else {
                Color.Transparent
            }
            
            // Song row (neon effect)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(NeonTheme.shapes.medium)
                    .background(backgroundColor)
                    .clickable { onSongClick(song) }
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .then(
                        if (isPlaying) Modifier.shadow(
                            elevation = 8.dp,
                            spotColor = NeonColors.ElectricPink.copy(alpha = 0.3f)
                        ) else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sıra numarası veya çalınıyor işareti
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(24.dp)
                        .padding(end = 8.dp)
                ) {
                    if (isPlaying) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Playing",
                            tint = NeonColors.ElectricPink,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = NeonColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Albüm kapağı
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1A1A1A),
                                    Color(0xFF0F0F0F)
                                )
                            )
                        )
                ) {
                    // Albüm ikon ve glowing efekt
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "${song.title} kapak resmi",
                        tint = if (isPlaying) NeonColors.ElectricPink else NeonColors.TextSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                            .then(
                                if (isPlaying) Modifier.blur(radius = 1.dp) else Modifier
                            )
                    )
                    
                    // Play button (for hover effect)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0f) // Hover olmadığında görünmez
                            .clickable { onPlayClick(song) }
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = NeonColors.ElectricPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Song information
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = song.title,
                            color = if (isPlaying) NeonColors.ElectricPink else NeonColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (song.isExplicit) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(NeonColors.TextSecondary)
                            ) {
                                Text(
                                    text = "E",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = song.artist,
                        color = NeonColors.TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Favorite icon
                if (song.isFavorite) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Favorite",
                        tint = NeonColors.ElectricPink,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 8.dp)
                    )
                }
                
                // Süre
                Text(
                    text = song.duration,
                    color = NeonColors.TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // Özelleştirme butonu (mor parlayan)
                IconButton(
                    onClick = { 
                        println("Loop butonu tıklandı: ${song.title}")
                        println("Loop button clicked - Song ID: ${song.id}")
                        println("Loop butonu onClick öncesi")
                        onCustomizeClick(song) 
                        println("Loop butonu onClick sonrası")
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            shadowElevation = 8f * glowIntensity
                        }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Loop,
                        contentDescription = "Müziği Uzat",
                        tint = Color(0xFFAA00FF)
                            .copy(alpha = 0.7f + (0.3f * glowIntensity)),
                        modifier = Modifier.scale(0.9f + (0.1f * glowIntensity))
                    )
                }
                
                // Animasyonlu Neon Dikey Üç Nokta Menüsü
                Box {
                    var showMenu by remember { mutableStateOf(false) }
                    
                    // Neon Üç Nokta Butonu
                    NeonMoreVertButton(
                        onClick = { showMenu = true },
                        isActive = showMenu
                    )
                    
                    // Neon Menü İçeriği
                    NeonMusicOptionsMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        isFavorite = song.isFavorite,
                        onAddToFavoritesClick = {
                            // Add/remove from favorites operation
                            onOptionsClick(song)
                            showMenu = false
                        },
                        onDeleteClick = {
                            // Delete operation
                            onOptionsClick(song)
                            showMenu = false
                        },
                        onShareClick = {
                            // Share operation
                            onOptionsClick(song)
                            showMenu = false
                        },
                        onAddToPlaylistClick = {
                            // Add to playlist operation
                            onOptionsClick(song)
                            showMenu = false
                        }
                    )
                }
            }
            
            // Ayırıcı çizgi
            if (index < songs.size - 1) {
                Divider(
                    color = NeonColors.Divider,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

/**
 * Tam bir müzik kütüphanesi ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullMusicLibrary(
    recentlyPlayed: List<SongItem> = emptyList(),
    favorites: List<SongItem> = emptyList(),
    recommendations: List<SongItem> = emptyList(),
    navController: androidx.navigation.NavController? = null,
    onSongClick: (SongItem) -> Unit = {},
    onDownloadClick: (SongItem) -> Unit = {},
    onCustomizeClick: (SongItem) -> Unit = {},
    currentlyPlayingId: String? = null,
    modifier: Modifier = Modifier
) {
    // Tema ile sarmala
    NeonMusicTheme {
        var searchQuery by remember { mutableStateOf("") }
        var selectedFilter by remember { mutableStateOf("All") }
        var sortBy by remember { mutableStateOf("Recent") }
        var showSortMenu by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        
        // Parallax scroll state
        val scrollState = rememberLazyListState()
        val scrollOffset by remember { derivedStateOf { scrollState.firstVisibleItemScrollOffset } }
        
        // PlayerViewModel ekledik
        val playerViewModel = viewModel<PlayerViewModel>()
        
        // Seçilen şarkı için state
        var selectedSongIndex by remember { mutableStateOf(0) }
        val allSongs = remember { recentlyPlayed + favorites + recommendations }
        
        // Mevcut çalan şarkıyı bul
        val currentSong = remember(currentlyPlayingId, allSongs) {
            allSongs.firstOrNull { it.id == currentlyPlayingId } ?: allSongs.firstOrNull()
        }
        
        // Arama sonuçları için filtre
        val searchResults = remember(searchQuery, allSongs) {
            if (searchQuery.isBlank()) {
                emptyList<SongItem>()
            } else {
                allSongs.filter { song ->
                    song.title.contains(searchQuery, ignoreCase = true) || 
                    song.artist.contains(searchQuery, ignoreCase = true)
                }
            }
        }
        
        // Arama aktif mi?
        val isSearchActive = searchQuery.isNotBlank()
        
        // PlayerViewModel'den oynatma durumları
        val currentPosition by playerViewModel.currentPosition
        val duration by playerViewModel.duration
        val vmIsPlaying by playerViewModel.isPlaying
        
        // UI için animasyonlar
        val infiniteTransition = rememberInfiniteTransition(label = "UI Animations")
        val glowIntensity by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowIntensity"
        )

        // Pulsing animation
        val pulseSize by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseSize"
        )
        
        // Ana içerik
        Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
            // Arkaplan gradyan animasyonu
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0014),   // Çok koyu mor/siyah
                                Color(0xFF1A0033),   // Koyu mor
                                Color(0xFF0A0014)    // Çok koyu mor/siyah
                            )
                        )
                    )
            )
            
            // Arkaplan deseni
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Grid pattern
                        val gridSpacing = 40.dp.toPx()  // Müzik sayfasıyla aynı spacing
                        val gridColor = Color(0xFFAA00FF).copy(alpha = 0.03f)  // Mor grid çizgiler
                        
                        // Yatay çizgiler - müzik sayfasıyla aynı
                        for (i in 0..(size.height / gridSpacing).toInt()) {
                            val y = i * gridSpacing
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 0.5f  // Müzik sayfasıyla aynı kalınlık
                            )
                        }
                        
                        // Dikey çizgiler - müzik sayfasıyla aynı
                        for (i in 0..(size.width / gridSpacing).toInt()) {
                            val x = i * gridSpacing
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 0.5f  // Müzik sayfasıyla aynı kalınlık
                            )
                        }
                    }
            )
            
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    // Bottom navigation bar - BottomNavBar kullanıyoruz
                    if (navController != null) {
                        com.ui.components.BottomNavBar(
                            navController = navController,
                            currentRoute = "my_music"
                        )
                    }
                },
                topBar = {
                    // Spotify tarzı üst bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A0033).copy(alpha = 0.95f),  // Koyu mor
                        shadowElevation = 8.dp
                    ) {
                        Column {
                            // Başlık ve kullanıcı profili
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Your Library",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${allSongs.size} tracks",
                                        fontSize = 14.sp,
                                        color = NeonColors.TextSecondary
                                    )
                                }
                                
                                // Profil avatarı - Tıklanabilir
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    NeonColors.ElectricPink,
                                                    NeonColors.NeonBlue
                                                )
                                            )
                                        )
                                        .clickable {
                                            // Profile sayfasına yönlendir
                                            navController?.navigate("profile") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            // Arama ve filtreler
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Arama kutusu
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { 
                                        Text("Search tracks...", color = NeonColors.TextTertiary) 
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            tint = NeonColors.TextSecondary
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonColors.ElectricPink,
                                        unfocusedBorderColor = NeonColors.TextTertiary.copy(alpha = 0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = NeonColors.ElectricPink
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 14.sp)
                                )
                                
                                // Sıralama butonu
                                Box {
                                    IconButton(
                                        onClick = { showSortMenu = !showSortMenu }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sort,
                                            contentDescription = "Sort",
                                            tint = Color.White
                                        )
                                    }
                                    
                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false }
                                    ) {
                                        listOf("Recent", "A-Z", "Z-A", "Duration").forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    sortBy = option
                                                    showSortMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Filtre chipleri
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val filters = listOf("All", "Favorites", "Recent", "Downloaded")
                                items(filters) { filter ->
                                    FilterChip(
                                        selected = selectedFilter == filter,
                                        onClick = { selectedFilter = filter },
                                        label = { Text(filter, fontSize = 13.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonColors.ElectricPink.copy(alpha = 0.2f),
                                            selectedLabelColor = NeonColors.ElectricPink,
                                            containerColor = Color.Transparent,
                                            labelColor = NeonColors.TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if (selectedFilter == filter) 
                                                NeonColors.ElectricPink else NeonColors.TextTertiary.copy(alpha = 0.3f),
                                            selectedBorderColor = NeonColors.ElectricPink
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                // Filtre ve sıralama uygula - LazyColumn dışında
                val filteredSongs = remember(searchQuery, selectedFilter, sortBy, allSongs) {
                    var filtered = allSongs
                    
                    // Arama filtresi
                    if (searchQuery.isNotBlank()) {
                        filtered = filtered.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.artist.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    
                    // Kategori filtresi
                    when (selectedFilter) {
                        "Favorites" -> filtered = filtered.filter { it.isFavorite }
                        "Recent" -> filtered = filtered.take(20)
                        "Downloaded" -> filtered = filtered // Tüm şarkılar indirilmiş kabul edilebilir
                    }
                    
                    // Sıralama
                    when (sortBy) {
                        "A-Z" -> filtered.sortedBy { it.title }
                        "Z-A" -> filtered.sortedByDescending { it.title }
                        "Duration" -> filtered.sortedBy { it.durationInSeconds }
                        else -> filtered // Recent - varsayılan sıralama
                    }
                }
                
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Parallax header with glass morphism
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .graphicsLayer {
                                    // Parallax effect
                                    translationY = scrollOffset * 0.5f
                                    alpha = 1f - (scrollOffset / 600f).coerceIn(0f, 1f)
                                }
                        ) {
                            // Animated gradient background
                            val infiniteTransition = rememberInfiniteTransition(label = "gradient")
                            val animatedProgress by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(3000),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "color1"
                            )
                            val color1 = lerp(
                                Color(0xFFAA00FF).copy(alpha = 0.3f),  // Mor
                                Color(0xFFFF00FF).copy(alpha = 0.3f),  // Pembe/mor
                                animatedProgress
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                color1.copy(alpha = 0.15f),  // Yumuşak mor
                                                Color(0xFF0A0014)  // Koyu mor/siyah arka plan
                                            ),
                                            radius = 800f
                                        )
                                    )
                            )
                            
                            // Glass card with stats
                            Card(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFAA00FF).copy(alpha = 0.05f)  // Mor cam efekti
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFFAA00FF).copy(alpha = 0.15f)  // Mor kenar
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Your Music Journey",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        StatItem("${allSongs.size}", "Tracks")
                                        StatItem("${favorites.size}", "Favorites")
                                        StatItem("${recentlyPlayed.size}", "Recent")
                                    }
                                }
                            }
                        }
                    }
                    
                    // Spotify tarzı şarkı listesi
                    if (filteredSongs.isNotEmpty()) {
                        itemsIndexed(filteredSongs) { index, song ->
                            SpotifyTrackItem(
                                song = song,
                                index = index + 1,
                                isPlaying = song.id == currentlyPlayingId,
                                onSongClick = { 
                                    println("SpotifyTrackItem clicked - Song: ${song.title}, MediaUri: ${song.mediaUri}")
                                    onSongClick(song) 
                                },
                                onOptionsClick = { onCustomizeClick(song) },
                                onFavoriteClick = { /* Toggle favorite */ },
                                onDownloadClick = {
                                    coroutineScope.launch {
                                        downloadSong(song, context)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        // Boş durum
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = NeonColors.TextTertiary
                                    )
                                    Text(
                                        text = "No tracks found",
                                        fontSize = 18.sp,
                                        color = NeonColors.TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Try adjusting your filters",
                                        fontSize = 14.sp,
                                        color = NeonColors.TextTertiary
                                    )
                                }
                            }
                        }
                    }
                    
                    // Bottom navigation için ekstra boşluk
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

/**
 * Spotify tarzı şarkı satırı - Glass Morphism & Blur Effects
 */
@Composable
fun SpotifyTrackItem(
    song: SongItem,
    index: Int,
    isPlaying: Boolean,
    onSongClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    
    val animatedScale by animateFloatAsState(
        targetValue = when {
            isPlaying -> 0.98f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "scale"
    )
    
    val glassAlpha by animateFloatAsState(
        targetValue = if (isHovered || isPlaying) 0.15f else 0.08f,
        animationSpec = tween(300),
        label = "glassAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .scale(animatedScale)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Album art blur backdrop
        if (song.albumArt != null) {
            AsyncImage(
                model = song.albumArt,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(25.dp)
                    .alpha(0.3f),
                contentScale = ContentScale.Crop
            )
        }
        
        // Glass morphism overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = glassAlpha * 0.5f),  // Daha az parlak
                            Color.White.copy(alpha = glassAlpha * 0.3f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),  // Daha az parlak
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHovered = true
                            tryAwaitRelease()
                            isHovered = false
                        },
                        onTap = { onSongClick() }
                    )
                }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Numara veya çalma ikonu
            Box(
                modifier = Modifier.width(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    // Animasyonlu equalizer çubukları
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(16.dp)
                    ) {
                        repeat(3) { index ->
                            val infiniteTransition = rememberInfiniteTransition(label = "eq")
                            val height by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        durationMillis = 300 + index * 100,
                                        easing = LinearEasing
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "eqBar$index"
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight(height)
                                    .background(
                                        NeonColors.ElectricPink,
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                } else {
                    Text(
                        text = "$index",
                        color = NeonColors.TextTertiary,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Albüm kapağı
            Card(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1C)  // Koyu gri kart arka planı
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (song.albumArt != null) {
                        AsyncImage(
                            model = song.albumArt,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = NeonColors.TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Şarkı bilgileri
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    color = if (isPlaying) NeonColors.ElectricPink else Color.White,
                    fontSize = 15.sp,
                    fontWeight = if (isPlaying) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = NeonColors.TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Süre
            Text(
                text = song.duration,
                color = NeonColors.TextTertiary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // Download butonu
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00D4FF).copy(alpha = 0.1f),
                                    Color(0xFF0099CC).copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color(0xFF00D4FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Favori butonu
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) 
                        Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) 
                        NeonColors.ElectricPink else NeonColors.TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Seçenekler menüsü
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = NeonColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Eski içeriği temizlendi ve gereksiz kodlar kaldırıldı

/**
 * İstatistik öğesi için glass morphism component
 */
@Composable
fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * Yeni minimal, modern neon kart tasarımı
 */
@Composable
fun NeonSongCard(
    songItem: SongItem,
    isPlaying: Boolean,
    glowIntensity: Float,
    onClick: () -> Unit,
    onDownloadClick: (SongItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animasyon için
    val infiniteTransition = rememberInfiniteTransition(label = "cardAnimation")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )
    
    val glowAlpha = if (isPlaying) borderAlpha * glowIntensity else 0.4f * glowIntensity
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isPlaying) 8.dp else 4.dp,
                spotColor = if (isPlaying) NeonColors.ElectricPink else NeonColors.NeonBlue,
                ambientColor = if (isPlaying) NeonColors.ElectricPink.copy(alpha = 0.4f) else NeonColors.NeonBlue.copy(alpha = 0.3f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A0033).copy(alpha = 0.8f)  // Koyu mor kart
        ),
        border = BorderStroke(
            width = if (isPlaying) 1.5.dp else 1.dp,
            brush = Brush.verticalGradient(
                colors = if (isPlaying) {
                    listOf(
                        NeonColors.ElectricPink.copy(alpha = glowAlpha),
                        NeonColors.NeonBlue.copy(alpha = glowAlpha * 0.7f)
                    )
                } else {
                    listOf(
                        NeonColors.NeonBlue.copy(alpha = 0.5f * glowIntensity),
                        NeonColors.DeepBlue.copy(alpha = 0.3f * glowIntensity)
                    )
                }
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Albüm kapağı
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Black,
                                    Color(0xFF0A0A0A)  // Hafif gri ton
                                )
                            )
                        )
                        .then(
                            if (isPlaying) Modifier.border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        NeonColors.ElectricPink.copy(alpha = glowAlpha),
                                        NeonColors.NeonBlue.copy(alpha = glowAlpha),
                                        NeonColors.ElectricPink.copy(alpha = glowAlpha)
                                    )
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (songItem.albumArt != null) {
                        AsyncImage(
                            model = songItem.albumArt,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Currently playing song highlight
                        if (isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                NeonColors.ElectricPink.copy(alpha = 0.3f),
                                                NeonColors.NeonBlue.copy(alpha = 0.2f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (isPlaying) NeonColors.ElectricPink else NeonColors.NeonBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Song information
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = songItem.title,
                        color = if (isPlaying) NeonColors.ElectricPink else Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = songItem.artist,
                        color = NeonColors.TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (songItem.isFavorite) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = NeonColors.ElectricPink,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        Text(
                            text = songItem.duration,
                            color = NeonColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Download button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (songItem.isDownloaded) 
                                NeonColors.ElectricGreen.copy(alpha = 0.1f)
                            else 
                                NeonColors.NeonBlue.copy(alpha = 0.1f)
                        )
                        .clickable { onDownloadClick(songItem) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (songItem.isDownloaded) Icons.Filled.Check else Icons.Filled.Download,
                        contentDescription = if (songItem.isDownloaded) "Downloaded" else "Download",
                        tint = if (songItem.isDownloaded) NeonColors.ElectricGreen else NeonColors.NeonBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Oynatma göstergesi animasyonu (eğer şarkı çalınıyorsa)
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonColors.ElectricPink)
                )
            }
        }
    }
}

/**
 * Kompakt müzik kartı (3'lü grid için)
 */
@Composable
fun CompactMusicCard(
    songItem: SongItem,
    isPlaying: Boolean,
    glowIntensity: Float,
    onClick: () -> Unit,
    onDownloadClick: (SongItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Kare şeklinde kart
            .shadow(
                elevation = if (isPlaying) 8.dp else 2.dp,
                spotColor = if (isPlaying) NeonColors.ElectricPink else NeonColors.NeonBlue
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A0033).copy(alpha = 0.8f)  // Koyu mor kart
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = if (isPlaying) {
                    listOf(
                        NeonColors.ElectricPink.copy(alpha = 0.5f * glowIntensity),
                        NeonColors.ElectricPink.copy(alpha = 0.2f * glowIntensity)
                    )
                } else {
                    listOf(
                        NeonColors.NeonBlue.copy(alpha = 0.3f * glowIntensity),
                        NeonColors.DeepBlue.copy(alpha = 0.1f * glowIntensity)
                    )
                }
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Albüm kapağı (üst %70)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonColors.DarkBackground,
                                NeonColors.MidnightPurple
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (songItem.albumArt != null) {
                    AsyncImage(
                        model = songItem.albumArt,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Currently playing song highlight
                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = NeonColors.ElectricPink,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isPlaying) NeonColors.ElectricPink else NeonColors.NeonBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
                
                // Favorite and Download indicators
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Download button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF00D4FF).copy(alpha = 0.8f),
                                        Color(0xFF0099CC).copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .clickable { onDownloadClick(songItem) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // Favorite indicator
                    if (songItem.isFavorite) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = NeonColors.ElectricPink,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Song information (bottom 30%)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .padding(4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = songItem.title,
                    color = if (isPlaying) NeonColors.ElectricPink else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = songItem.artist,
                    color = NeonColors.TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Büyük müzik kartı (kayan)
 */
@Composable
fun LargeMusicCard(
    songItem: SongItem,
    isPlaying: Boolean,
    glowIntensity: Float,
    onClick: () -> Unit,
    onDownloadClick: (SongItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .height(220.dp)
            .shadow(
                elevation = if (isPlaying) 12.dp else 4.dp,
                spotColor = if (isPlaying) NeonColors.ElectricPink else NeonColors.NeonBlue
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A1A3A).copy(alpha = 0.7f)  // Mor tonlu kart
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = if (isPlaying) {
                    listOf(
                        NeonColors.ElectricPink.copy(alpha = 0.7f * glowIntensity),
                        NeonColors.ElectricPink.copy(alpha = 0.3f * glowIntensity)
                    )
                } else {
                    listOf(
                        NeonColors.NeonBlue.copy(alpha = 0.5f * glowIntensity),
                        NeonColors.DeepBlue.copy(alpha = 0.2f * glowIntensity)
                    )
                }
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Albüm kapağı (üst %70)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonColors.DarkBackground,
                                NeonColors.MidnightPurple
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (songItem.albumArt != null) {
                    AsyncImage(
                        model = songItem.albumArt,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isPlaying) NeonColors.ElectricPink else NeonColors.NeonBlue,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Play button overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NeonColors.ElectricPink.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // Favorite indicator
                if (songItem.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = NeonColors.ElectricPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Song information (bottom 30%)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = songItem.title,
                    color = if (isPlaying) NeonColors.ElectricPink else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = songItem.artist,
                    color = NeonColors.TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = songItem.duration,
                    color = NeonColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Geliştirilmiş kategori başlığı
 */
@Composable
fun NeonCategoryHeader(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonColors.NeonBlue,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp)
                )
            }
            
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = NeonColors.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = if (icon != null) 36.dp else 0.dp)
            )
        }
    }
}

/**
 * Geliştirilmiş neon arama çubuğu
 */
@Composable
fun NeonSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    glowIntensity: Float = 1f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Arama çubuğunun arkasındaki neon parıltı efekti
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .blur(radius = 8.dp * glowIntensity)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonColors.ElectricPink.copy(alpha = 0.2f * glowIntensity),
                            NeonColors.NeonBlue.copy(alpha = 0.2f * glowIntensity),
                            NeonColors.ElectricPink.copy(alpha = 0.2f * glowIntensity)
                        )
                    )
                )
        )
        
        // Ana arama çubuğu
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = NeonColors.TextSecondary.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ara",
                    tint = NeonColors.NeonBlue.copy(alpha = 0.8f * glowIntensity)
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Temizle",
                            tint = NeonColors.TextSecondary
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    spotColor = NeonColors.NeonBlue.copy(alpha = 0.3f * glowIntensity)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Black.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.8f),
                focusedBorderColor = NeonColors.NeonBlue.copy(alpha = 0.8f * glowIntensity),
                unfocusedBorderColor = NeonColors.NeonBlue.copy(alpha = 0.4f),
                cursorColor = NeonColors.NeonBlue,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true
        )
    }
}

/**
 * Animasyonlu neon kütüphane başlığı
 */
@Composable
fun EnhancedNeonTitle(
    glowIntensity: Float = 1.0f
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "MÜZİKLERİM",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .drawBehind {
                    // Neon parıltı efekti
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                NeonColors.NeonBlue.copy(alpha = 0.1f * glowIntensity),
                                NeonColors.ElectricPink.copy(alpha = 0.2f * glowIntensity),
                                NeonColors.NeonBlue.copy(alpha = 0.1f * glowIntensity),
                                Color.Transparent
                            )
                        ),
                        alpha = 0.8f
                    )
                }
        )
    }
}

/**
 * Tab Button Component
 */
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = if (isSelected) NeonColors.ElectricPink else NeonColors.TextSecondary,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        )
        
        // Alt çizgi göstergesi
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(3.dp)
                    .width(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                NeonColors.ElectricPink,
                                NeonColors.NeonBlue
                            )
                        ),
                        shape = RoundedCornerShape(1.5.dp)
                    )
            )
        }
    }
}

/**
 * Playlist Card Component
 */
@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Playlist görseli
        Card(
            modifier = Modifier
                .size(160.dp)
                .shadow(
                    elevation = 8.dp,
                    spotColor = NeonColors.ElectricPink.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Box {
                AsyncImage(
                    model = playlist.imageUrl,
                    contentDescription = playlist.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                ),
                                startY = 100f
                            )
                        )
                )
                
                // Play button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonColors.ElectricPink.copy(alpha = 0.9f),
                                    NeonColors.NeonBlue.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // Playlist adı
        Text(
            text = playlist.name,
            style = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Enhanced download function using Android DownloadManager for reliable downloads
 */
suspend fun downloadSong(
    song: SongItem,
    context: Context
) {
    withContext(Dispatchers.IO) {
        try {
            // Check if mediaUri exists
            if (song.mediaUri.isNullOrBlank()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Download link not available for ${song.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@withContext
            }
            
            // Process the URL
            var downloadUrl = song.mediaUri.trim()
            
            // Ensure HTTPS for Supabase URLs
            if (downloadUrl.contains("supabase") && downloadUrl.startsWith("http://")) {
                downloadUrl = downloadUrl.replace("http://", "https://")
            }
            
            android.util.Log.d("DownloadSong", "Downloading from URL: $downloadUrl")
            
            // Use Android DownloadManager for reliable download
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            
            // Create safe filename
            val safeTitle = song.title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            val fileName = "${safeTitle}_${song.id}.mp3"
            
            // Create download request
            val request = android.app.DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Downloading ${song.title}")
                setDescription("OctaAI Music Download")
                setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "OctaAI_Music/$fileName")
                allowScanningByMediaScanner()
                setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)
                setMimeType("audio/mpeg")
            }
            
            // Start download
            val downloadId = downloadManager.enqueue(request)
            android.util.Log.d("DownloadSong", "Download started with ID: $downloadId")
            
            // Show notification
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Download started: ${song.title}\nCheck notification for progress",
                    Toast.LENGTH_LONG
                ).show()
            }
            
        } catch (e: Exception) {
            android.util.Log.e("DownloadSong", "DownloadManager failed: ${e.message}, trying direct download", e)
            // Fallback to direct download if DownloadManager fails
            try {
                downloadSongDirect(song, context)
            } catch (directError: Exception) {
                android.util.Log.e("DownloadSong", "Direct download also failed: ${directError.message}", directError)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Download failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

/**
 * Direct download method as fallback for when DownloadManager fails
 */
private suspend fun downloadSongDirect(
    song: SongItem,
    context: Context
) {
    withContext(Dispatchers.IO) {
        val downloadUrl = song.mediaUri?.trim() ?: throw Exception("No download URL")
        
        // Create directory
        val downloadsDir = File(
            android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            ),
            "OctaAI_Music"
        )
        
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        // Create filename
        val safeTitle = song.title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val fileName = "${safeTitle}_${song.id}.mp3"
        val file = File(downloadsDir, fileName)
        
        // Check if already exists
        if (file.exists()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "${song.title} already downloaded!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return@withContext
        }
        
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Downloading ${song.title}...",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        // Download using OkHttp (if available) or HttpURLConnection
        val url = java.net.URL(downloadUrl)
        val connection = url.openConnection() as java.net.HttpURLConnection
        
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 60000
            connection.readTimeout = 60000
            connection.setRequestProperty("User-Agent", "OctaAI-Music-App/1.0")
            connection.doInput = true
            
            connection.connect()
            val responseCode = connection.responseCode
            
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val outputStream = java.io.FileOutputStream(file)
                
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }
                
                android.util.Log.d("DownloadSong", "File downloaded successfully: ${file.length()} bytes")
                
                // Verify file is not empty
                if (file.length() > 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "${song.title} downloaded successfully!\nLocation: Downloads/OctaAI_Music",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    
                    // Register in MediaStore for Android 10+
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        val values = android.content.ContentValues().apply {
                            put(android.provider.MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                            put(android.provider.MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                            put(android.provider.MediaStore.Audio.Media.RELATIVE_PATH, 
                                "${android.os.Environment.DIRECTORY_DOWNLOADS}/OctaAI_Music")
                        }
                        
                        context.contentResolver.insert(
                            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            values
                        )
                    }
                } else {
                    file.delete()
                    throw Exception("Downloaded file is empty")
                }
            } else {
                throw Exception("Server returned code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
} 