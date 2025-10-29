package com.ui

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ModernBlurredBackground
import com.ui.components.BottomNavBar
import com.network.connectivityState
import com.ui.components.NoInternetScreen
import com.aihackathonkarisacikartim.god2.SupabaseManager
import androidx.compose.material3.CircularProgressIndicator
import com.OctaAIMusicCreator
import com.OctaForgotPasswordScreen
import com.OctaLoginScreen
import com.ui.auth.OctaAISignUpScreen
import com.OctaStartScreen
import com.NeonGlowButton
import com.UserProfileScreen
import com.ui.FullMusicLibrary
import com.ui.SongItem
import com.settings.SessionManager
import io.github.jan.supabase.gotrue.auth
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.aihackathonkarisacikartim.god2.GeneratedMusicData
import kotlinx.coroutines.*
import com.ui.extend.MusicExtendScreen

@Composable
fun AppContent() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    
    // İnternet bağlantısı durumunu izle
    val isConnected by connectivityState()
    var showNoInternetScreen by remember { mutableStateOf(false) }
    
    // İnternet bağlantısı değişimlerini takip et
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            // İnternet bağlantısı kesildiğinde, ekranı göster
            showNoInternetScreen = true
        }
    }
    
    // İnternet bağlantısı olmadığında gösterilecek ekran
    NoInternetScreen(
        isVisible = showNoInternetScreen,
        onRetryClick = {
            // Kullanıcı "Tekrar Dene" butonuna tıkladığında, eğer bağlantı varsa giriş ekranına yönlendir
            if (isConnected) {
                showNoInternetScreen = false
                // Giriş ekranına yönlendir
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    )
    
    // İnternet bağlantısı olmayan ekran görünürken normal uygulamayı gizle
    if (!showNoInternetScreen) {
        NavHost(
            navController = navController,
            startDestination = "start_screen"
        ) {
            composable("start_screen") {
                OctaStartScreen(
                    onSplashFinished = {
                        navController.navigate("login") {
                            popUpTo("start_screen") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("login") {
                OctaLoginScreen(
                    onLogin = { username, password ->
                        navController.navigate("music_creator") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onForgotPassword = {
                        navController.navigate("forgot_password")
                    },
                    onSignup = {
                        navController.navigate("signup")
                    }
                )
            }
            
            composable("signup") {
                OctaAISignUpScreen(
                    onSignUpSuccess = { email, password ->
                        // Kayıt başarılıysa giriş ekranına dön
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("forgot_password") {
                OctaForgotPasswordScreen(
                    onBackClick = {
                        navController.popBackStack() // Önceki ekrana dön
                    },
                    onPasswordReset = {
                        // Şifre sıfırlama başarılıysa login ekranına dön
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true } 
                        }
                    }
                )
            }
            
            composable("music_creator") {
                OctaAIMusicCreator(
                    onMusicCreated = { prompt, genre ->
                        // Müzik oluşturulduktan sonra yapılacak işlem
                        println("Music created with prompt: $prompt and genre: $genre")
                    },
                    onNavigateToProfile = {
                        // Profil sayfasına yönlendir
                        navController.navigate("profile")
                    },
                    navController = navController
                )
            }
            
            composable("profile") {
                UserProfileScreen(
                    onNavigateToMusicGen = {
                        navController.navigate("music_creator")
                    },
                    navController = navController
                )
            }
            
            composable("my_music") {
                // SupabaseManager örneği oluştur
                val supabaseManager = remember { SupabaseManager() }
                val coroutineScope = rememberCoroutineScope()
                val currentUserId = remember { mutableStateOf<String?>(null) }
                
                // Kullanıcı müziklerini tutacak state
                var userMusicList by remember { mutableStateOf<List<GeneratedMusicData>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }
                
                // Kullanıcı ID'si ve müzikleri al
                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        try {
                            // Mevcut kullanıcı ID'sini al
                            val user = supabaseManager.getSupabaseClient().auth.currentUserOrNull()
                            if (user != null) {
                                currentUserId.value = user.id
                                
                                // Kullanıcının müziklerini veritabanından çek
                                val musicList = supabaseManager.getUserGeneratedMusic(user.id)
                                userMusicList = musicList
                            }
                        } catch (e: Exception) {
                            // Hata durumunda log
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                }
                
                // GeneratedMusicData'dan SongItem'a dönüştürme fonksiyonu
                fun convertToSongItems(musicDataList: List<GeneratedMusicData>): List<SongItem> {
                    return musicDataList.map { musicData ->
                        // Duration değerini saniyeden "dakika:saniye" formatına dönüştür
                        val durationSeconds = musicData.duration
                        val minutes = durationSeconds / 60
                        val seconds = durationSeconds % 60
                        val formattedDuration = String.format("%d:%02d", minutes, seconds)
                        
                        SongItem(
                            id = musicData.id,
                            title = musicData.title,
                            artist = "OctaAI",
                            albumArt = musicData.coverUrl,
                            duration = formattedDuration,
                            mediaUri = musicData.musicUrl,
                            genre = musicData.genre,
                            promptText = musicData.prompt,
                            createdAt = musicData.createdAt,
                            musicId = musicData.musicId,
                            durationInSeconds = durationSeconds.toInt()
                        )
                    }
                }
                
                if (isLoading) {
                    // Yükleme göstergesi
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF6C63FF))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Müzik verisi yükleniyor...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // Şarkı listelerini dönüştür
                    val songItems = convertToSongItems(userMusicList)
                    
                    // Listeleri kategorize et
                    val recentlyPlayedSongs = songItems.take(3)
                    val favoriteSongs = songItems.filter { it.isFavorite }.take(5)
                    val recommendedSongs = songItems.takeLast(songItems.size.coerceAtMost(5))
                    
                    // Eğer liste boşsa test verileri ekle (geliştirme için)
                    val testSongs = if (songItems.isEmpty()) {
                        listOf(
                            SongItem(
                                id = "test-song-001",
                                title = "Test Şarkı 1",
                                artist = "OctaAI",
                                duration = "3:15",
                                isFavorite = true
                            ),
                            SongItem(
                                id = "test-song-002",
                                title = "Test Şarkı 2",
                                artist = "OctaAI",
                                duration = "2:45"
                            ),
                            SongItem(
                                id = "test-song-003",
                                title = "Test Şarkı 3",
                                artist = "OctaAI",
                                duration = "4:02",
                                isFavorite = true
                            )
                        )
                    } else {
                        emptyList()
                    }
                    
                    // Test verileri kullanarak listeleri güncelle
                    val finalRecentlyPlayed = if (recentlyPlayedSongs.isEmpty()) testSongs else recentlyPlayedSongs
                    val finalFavorites = if (favoriteSongs.isEmpty() && testSongs.isNotEmpty()) 
                        testSongs.filter { it.isFavorite } else favoriteSongs
                    val finalRecommendations = if (recommendedSongs.isEmpty()) testSongs else recommendedSongs
                    
                    // Scaffold ile bottom navigation ve içerik
                    Scaffold(
                        bottomBar = {
                            BottomNavBar(
                                navController = navController,
                                currentRoute = "my_music"
                            )
                        },
                        containerColor = Color.Transparent
                    ) { paddingValues ->
                        // Müzik kütüphanesi bileşenini göster
                        FullMusicLibrary(
                            recentlyPlayed = finalRecentlyPlayed,
                            favorites = finalFavorites,
                            recommendations = finalRecommendations,
                            navController = navController,
                            onSongClick = { song ->
                                // Şarkıya tıklandığında yapılacak işlem
                                println("Şarkı tıklandı: ${song.title}")
                            },
                            onCustomizeClick = { song ->
                                // Özelleştirme (loop) butonu tıklandığında extend sayfasına yönlendir
                                println("Loop butonuna tıklandı: ${song.title}, ID: ${song.id}")
                                
                                // Direkt olarak navigasyon yapın (navController kesinlikle mevcut olmalı)
                                try {
                                    if (song.id.isNotEmpty()) {
                                        val route = "extend/${song.id}"
                                        println("Navigasyon başlatılıyor: $route")
                                        
                                        println("Extend sayfasına yönlendiriliyor...")
                                        
                                        // Navigasyon işlemini gerçekleştir
                                        navController.navigate(route) {
                                            // Navigasyon yapılandırması
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        println("Navigasyon işlemi tamamlandı")
                                    } else {
                                        println("Hata: Şarkı ID'si boş!")
                                    }
                                } catch (e: Exception) {
                                    println("Navigasyon hatası: ${e.message}")
                                    println("Navigasyon hatası detayı: ${e.stackTraceToString()}")
                                }
                            },
                            onDownloadClick = { song ->
                                // İndirme butonu tıklandığında
                                println("İndirme butonu tıklandı: ${song.title}")
                            },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
            
            // Müzik uzatma ekranı
            composable(
                route = "extend/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.StringType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getString("songId")
                println("Extend sayfası yükleniyor: songId=$songId")
                
                // SupabaseManager örneği oluştur
                val supabaseManager = remember { SupabaseManager() }
                val coroutineScope = rememberCoroutineScope()
                var userSong by remember { mutableStateOf<SongItem?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                
                // Test verisi (her durumda hazır bulunsun)
                val fallbackSong = SongItem(
                    id = songId ?: "test-fallback",
                    title = "Test Şarkı",
                    artist = "OctaAI",
                    duration = "3:45",
                    promptText = "Elektronik dans müziği, hızlı tempo"
                )
                
                // Kullanıcı ID'si ve belirli şarkıyı al
                LaunchedEffect(songId) {
                    coroutineScope.launch {
                        try {
                            println("Extend sayfası veri yükleniyor: songId=$songId")
                            // Şarkıyı veritabanından çek
                            val user = supabaseManager.getSupabaseClient().auth.currentUserOrNull()
                            if (user != null && songId != null) {
                                // Şarkıyı ID'ye göre veritabanından al
                                val musicData = supabaseManager.getGeneratedMusicById(songId)
                                if (musicData != null) {
                                    // Şarkıyı SongItem'a dönüştür
                                    val durationSeconds = musicData.duration
                                    val minutes = durationSeconds / 60
                                    val seconds = durationSeconds % 60
                                    val formattedDuration = String.format("%d:%02d", minutes, seconds)
                                    
                                    userSong = SongItem(
                                        id = musicData.id,
                                        title = musicData.title,
                                        artist = "OctaAI",
                                        albumArt = musicData.coverUrl,
                                        duration = formattedDuration,
                                        mediaUri = musicData.musicUrl,
                                        genre = musicData.genre,
                                        promptText = musicData.prompt,
                                        createdAt = musicData.createdAt,
                                        musicId = musicData.musicId,
                                        durationInSeconds = durationSeconds.toInt()
                                    )
                                    
                                    println("DB'den şarkı bilgisi alındı: ${userSong?.title}")
                                } else {
                                    println("DB'den şarkı bulunamadı, fallback kullanılacak")
                                    userSong = fallbackSong
                                }
                            } else {
                                println("Kullanıcı veya songId null, fallback kullanılacak")
                                userSong = fallbackSong
                            }
                        } catch (e: Exception) {
                            // Hata durumunda log
                            println("Extend sayfası şarkı yükleme hatası: ${e.message}")
                            e.printStackTrace()
                            userSong = fallbackSong
                        } finally {
                            isLoading = false
                        }
                    }
                }
                
                if (isLoading) {
                    // Yükleme göstergesi
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF6C63FF))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Müzik verisi yükleniyor...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else if (userSong != null) {
                    // Debug bilgileri
                    println("Extend sayfası başarıyla yüklendi: ${userSong!!.title}, ID: ${userSong!!.id}")
                    
                    MusicExtendScreen(
                        songItem = userSong!!,
                        onBackClick = { navController.popBackStack() },
                        onExtendSuccess = { taskId: String ->
                            // Uzatma başarılı olduğunda yapılacak işlemler
                            println("Extend işlemi başarılı: TaskID=$taskId")
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Fallback - bu duruma hiç düşmemeli ancak güvenlik için koyalım
                    println("userSong null! Fallback kullanılıyor")
                    
                    MusicExtendScreen(
                        songItem = fallbackSong,
                        onBackClick = { navController.popBackStack() },
                        onExtendSuccess = { taskId: String ->
                            println("Test şarkısı extend işlemi başarılı: TaskID=$taskId")
                            navController.popBackStack()
                        }
                    )
                }
            }
            
        }
    }
}

// Sidebar kaldırıldı, artık gerek yok 