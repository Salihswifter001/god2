package com.aihackathonkarisacikartim.god2

import com.settings.LanguageManager
import com.settings.SessionManager
import com.network.NetworkUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.OctaAIMusicCreator // musicgen.kt'den import
import com.ui.ModernMusicGenerator // Yeni modern müzik oluşturucu
// import com.OctaLoginScreen // ESKİ login ekranı - KULLANILMIYOR
import com.ui.auth.OctaAILoginScreen // YENİ modern login ekranı
import com.OctaForgotPasswordScreen // forgot.kt'den import
import com.UserProfileScreen // profile.kt'den import
import com.ui.auth.OctaAISignUpScreen // Yeni modern signup ekranı
import com.ui.FullMusicLibrary // lib-column.kt'den import
import com.ui.SongItem // lib-column.kt'den import
import com.ui.player.NeonPlayerScreen // Gerçek müzik çalar ekranı
import com.ui.player.ModernPlayerScreen // Modern müzik çalar ekranı
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CardMembership
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicApi.MusicViewModel
import com.about.ModernAboutScreen // Modern about screen import
import com.splash.OctaAISplashScreen // Yeni animasyonlu splash screen
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.ui.text.TextStyle
import com.theme.NeonThemeProvider // Tema provider'ı import ediyoruz
// LanguageSelectionScreen import'u kaldırıldı
import androidx.compose.ui.platform.LocalContext
import android.app.DownloadManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ui.components.NoInternetScreen
import androidx.compose.runtime.derivedStateOf
import androidx.navigation.navArgument
import androidx.navigation.NavType
import io.github.jan.supabase.gotrue.user.UserInfo
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.ui.graphics.vector.ImageVector
import com.payment.PaymentScreen
import com.ui.extend.MusicExtendScreen
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.aihackathonkarisacikartim.god2.GeneratedMusicData
import androidx.compose.runtime.rememberCoroutineScope
import io.github.jan.supabase.gotrue.auth
import com.ui.BlackHoleWelcomeScreen

class MainActivity : ComponentActivity() {
    
    // OctaApplication kaldırıldı
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OctaApplication kaldırıldı - artık gerekli değil
        
        setContent {
            NeonThemeProviderWrapper()
        }
    }
    
    @Composable
    private fun NeonThemeProviderWrapper() {
        NeonThemeProvider {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val languageManager = LanguageManager(LocalContext.current)
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Session kontrolü için state'ler
    var isLoggedIn by remember { mutableStateOf(false) }
    var savedUsername by remember { mutableStateOf("") }
    var showWelcomeScreen by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf("splash") }
    
    // İnternet bağlantısı durumunu izle
    val networkUtils = NetworkUtils(LocalContext.current)
    val isConnected by remember { derivedStateOf { networkUtils.isInternetAvailable() } }
    var showNoInternetScreen by remember { mutableStateOf(false) }
    var hasCompletedInitialCheck by remember { mutableStateOf(false) }
    
    // Session kontrolü
    LaunchedEffect(Unit) {
        println("DEBUG MainActivity: Checking session...")
        isLoggedIn = sessionManager.isLoggedIn()
        println("DEBUG MainActivity: isLoggedIn = $isLoggedIn")
        println("DEBUG MainActivity: userId = ${sessionManager.getUserId()}")
        println("DEBUG MainActivity: email = ${sessionManager.getUserEmail()}")
        
        if (isLoggedIn) {
            savedUsername = sessionManager.getUsername() ?: sessionManager.getUserEmail()?.substringBefore("@") ?: "Kullanıcı"
            println("DEBUG MainActivity: Username = $savedUsername")
            showWelcomeScreen = true
            startDestination = "welcome"
        } else {
            startDestination = "login"
        }
    }
    
    // İnternet bağlantısını gözlemle ve durum değişikliklerini işle
    LaunchedEffect(key1 = Unit) {
        networkUtils.observeNetworkStatus().collect { connected ->
            // İlk kontrolü tamamladık
            if (!hasCompletedInitialCheck) {
                hasCompletedInitialCheck = true
            }
            
            if (!connected) {
                // İnternet bağlantısı kesildiğinde, ekranı göster
                showNoInternetScreen = true
            }
        }
    }
    
    // İnternet bağlantısı olmayan ekran
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
            } else {
                // Bağlantı yoksa kullanıcıya geri bildirim ver
                Toast.makeText(context, "Still no internet connection! Please check your connection.", Toast.LENGTH_SHORT).show()
            }
        }
    )
    
    // İnternet bağlantısı olmayan ekran görünürken normal uygulamayı gizle
    if (!showNoInternetScreen) {
        // Müzik çalma durumunu takip eden değişkenler
        var currentSong by remember { mutableStateOf<SongItem?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        
        // İndirme işlevi
        val downloadMusic = { song: SongItem ->
            song.mediaUri?.let { url ->
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(url)
                
                val request = DownloadManager.Request(uri).apply {
                    setTitle(song.title)
                    setDescription("by ${song.artist}")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "OctaAI/${song.title}.mp3")
                }
                
                try {
                    downloadManager.enqueue(request)
                    Toast.makeText(context, "Downloading ${song.title}...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(context, "No downloadable URL found", Toast.LENGTH_SHORT).show()
            }
        }
        
        LaunchedEffect(Unit) {
            // Dil seçimi kaldırıldı - direkt Türkçe olarak başla
            // Varsayılan dil Türkçe olarak ayarlandı
        }
        
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn && showWelcomeScreen) "welcome" else "splash_screen"
        ) {
            // Language selection composable kaldırıldı
            
            composable("splash_screen") {
                OctaAISplashScreen(
                    onSplashComplete = {
                        if (isLoggedIn) {
                            navController.navigate("welcome") {
                                popUpTo("splash_screen") { inclusive = true }
                            }
                        } else {
                            navController.navigate("login") {
                                popUpTo("splash_screen") { inclusive = true }
                            }
                        }
                    }
                )
            }
            
            composable("welcome") {
                BlackHoleWelcomeScreen(
                    username = savedUsername,
                    onAnimationComplete = {
                        navController.navigate("music_creator") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("login") {
                OctaAILoginScreen(  // YENİ modern login ekranı kullanılıyor
                    onLoginSuccess = { email, password ->
                        navController.navigate("music_creator") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onForgotPasswordClick = {
                        navController.navigate("forgot_password")
                    },
                    onSignUpClick = {
                        navController.navigate("signup")
                    }
                )
            }
            
            composable("signup") {
                OctaAISignUpScreen(
                    onSignUpSuccess = { email, password ->
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
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
                        navController.popBackStack()
                    },
                    onPasswordReset = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("music_creator") {
                // Yeni modern müzik oluşturucu kullanılıyor
                ModernMusicGenerator(
                    navController = navController,
                    onMusicCreated = { prompt, genre ->
                        println("Music created with prompt: $prompt and genre: $genre")
                    },
                    onNavigateToProfile = {
                        navController.navigate("profile")
                    }
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
            
            composable(
                "music_player/{title}/{artist}/{albumArt}/{mediaUri}",
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("artist") { type = NavType.StringType },
                    navArgument("albumArt") { type = NavType.StringType },
                    navArgument("mediaUri") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
                val artist = Uri.decode(backStackEntry.arguments?.getString("artist") ?: "")
                val albumArt = Uri.decode(backStackEntry.arguments?.getString("albumArt") ?: "")
                val mediaUri = Uri.decode(backStackEntry.arguments?.getString("mediaUri") ?: "")
                
                println("music_player route - title: $title, artist: $artist, mediaUri: $mediaUri")
                
                // Get user songs for the player
                val musicViewModel: MusicViewModel = viewModel()
                val userSongs = produceState<List<SongItem>>(emptyList()) {
                    value = musicViewModel.getUserMusicsAsSongItems()
                }
                
                ModernPlayerScreen(
                    navController = navController,
                    songTitle = title.ifEmpty { null },
                    artistName = artist.ifEmpty { null },
                    albumCoverUrl = albumArt.ifEmpty { null },
                    mediaUri = mediaUri.ifEmpty { null }?.let { Uri.parse(it) },
                    userSongs = userSongs.value
                )
            }
            
            composable("about") {
                ModernAboutScreen(
                    navController = navController
                )
            }
            
            // Müzik kütüphanesi ekranı
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
                            val user = supabaseManager.getSupabaseClient().auth.currentUserOrNull()
                            currentUserId.value = user?.id
                            println("DEBUG: Library - User ID: ${user?.id}")
                            
                            if (user != null) {
                                // Kullanıcının müziklerini al
                                userMusicList = supabaseManager.getUserGeneratedMusic(user.id)
                                println("DEBUG: Library - Fetched ${userMusicList.size} songs")
                                userMusicList.forEach { music ->
                                    println("DEBUG: Song - ${music.title}, URL: ${music.musicUrl}")
                                }
                            } else {
                                println("DEBUG: Library - No user logged in")
                            }
                        } catch (e: Exception) {
                            println("DEBUG: Library - Error fetching music: ${e.message}")
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                }
                
                // Müzikleri SongItem formatına dönüştür
                val finalRecentlyPlayed = userMusicList.map { music ->
                    val durationSeconds = music.duration
                    val minutes = durationSeconds / 60
                    val seconds = durationSeconds % 60
                    val formattedDuration = String.format("%d:%02d", minutes, seconds)
                    
                    SongItem(
                        id = music.id,
                        title = music.title,
                        artist = "OctaAI",
                        albumArt = music.coverUrl,
                        duration = formattedDuration,
                        mediaUri = music.musicUrl,
                        genre = music.genre,
                        promptText = music.prompt,
                        createdAt = music.createdAt,
                        musicId = music.musicId,
                        durationInSeconds = durationSeconds.toInt()
                    )
                }
                
                // Favori ve önerilen müzikleri ayır
                val finalFavorites = finalRecentlyPlayed.filter { it.isFavorite }
                val finalRecommendations = finalRecentlyPlayed.take(5)
                
                if (isLoading) {
                    // Yükleniyor göstergesi
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFAA00FF)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Müzikler yükleniyor...",
                                color = Color.White
                            )
                        }
                    }
                } else {
                    FullMusicLibrary(
                        recentlyPlayed = finalRecentlyPlayed,
                        favorites = finalFavorites,
                        recommendations = finalRecommendations,
                        navController = navController,
                        onSongClick = { song ->
                            // Müzik çalma işlemi
                            currentSong = song
                            // URL encode parameters to handle special characters
                            val encodedTitle = Uri.encode(song.title)
                            val encodedArtist = Uri.encode(song.artist)
                            val encodedAlbumArt = Uri.encode(song.albumArt ?: "")
                            val encodedMediaUri = Uri.encode(song.mediaUri ?: "")
                            
                            println("MainActivity - Navigating with song: ${song.title}, MediaUri: ${song.mediaUri}")
                            
                            navController.navigate("music_player/$encodedTitle/$encodedArtist/$encodedAlbumArt/$encodedMediaUri") {
                                launchSingleTop = true
                            }
                        },
                        onDownloadClick = { song ->
                            // İndirme işlemi
                        },
                        onCustomizeClick = { song ->
                            // Özelleştirme işlemi
                            navController.navigate("main_extend/${song.id}")
                        }
                    )
                }
            }
            
            // Müzik uzatma ekranı
            composable(
                route = "main_extend/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.StringType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getString("songId")
                println("MainActivity - Extend sayfası yükleniyor: songId=$songId")
                
                // SupabaseManager örneği oluştur
                val supabaseManager = remember { SupabaseManager() }
                val coroutineScope = rememberCoroutineScope()
                var userSong by remember { mutableStateOf<SongItem?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                
                // Yedek veri (sadece veri çekilemediği durumlarda kullanılacak)
                val fallbackSong = SongItem(
                    id = songId ?: "fallback-id",
                    title = "Your Music",
                    artist = "OctaAI",
                    duration = "3:45",
                    promptText = "Trap, beat, bass"
                )
                
                // Kullanıcı ID'si ve belirli şarkıyı al
                LaunchedEffect(songId) {
                    coroutineScope.launch {
                        try {
                            println("MainActivity - Veritabanından şarkı verisi yükleniyor: songId=$songId")
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
                                    
                                    println("Veritabanından şarkı bilgisi alındı: ${userSong?.title}")
                                } else {
                                    println("Veritabanından şarkı bulunamadı, fallback kullanılacak")
                                    userSong = fallbackSong
                                }
                            } else {
                                println("Kullanıcı veya songId null, fallback kullanılacak")
                                userSong = fallbackSong
                            }
                        } catch (e: Exception) {
                            // Hata durumunda log
                            println("Şarkı yükleme hatası: ${e.message}")
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
                                text = "Loading music data...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else if (userSong != null) {
                    // Debug bilgileri
                    println("Uzatma sayfası başarıyla yüklendi: ${userSong!!.title}, ID: ${userSong!!.id}")
                    
                    MusicExtendScreen(
                        songItem = userSong!!,
                        onBackClick = { navController.popBackStack() },
                        onExtendSuccess = { taskId ->
                            // Uzatma başarılı olduğunda yapılacak işlemler
                            println("Müzik uzatma başarılı: TaskID=$taskId")
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Fallback - bu duruma hiç düşmemeli ancak güvenlik için koyalım
                    println("userSong null! Fallback kullanılıyor")
                    
                    MusicExtendScreen(
                        songItem = fallbackSong,
                        onBackClick = { navController.popBackStack() },
                        onExtendSuccess = { taskId ->
                            println("Fallback şarkı uzatma işlemi başarılı: TaskID=$taskId")
                            navController.popBackStack()
                        }
                    )
                }
            }
            
            // Ödeme ekranı
            composable(
                "payment_screen/{planName}/{planPrice}",
                arguments = listOf(
                    navArgument("planName") { type = NavType.StringType },
                    navArgument("planPrice") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val planName = backStackEntry.arguments?.getString("planName") ?: "STANDART"
                val planPrice = backStackEntry.arguments?.getString("planPrice") ?: "0 TL"
                
                PaymentScreen(
                    planName = planName,
                    planPrice = planPrice,
                    onPaymentSuccess = {
                        // Ödeme başarılı olduğunda
                    },
                    onNavigateToProfile = {
                        navController.navigate("profile") {
                            popUpTo("membership") { inclusive = true }
                        }
                    },
                    onNavigateToMembership = {
                        navController.navigate("membership")
                    },
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryContent(
    onCreateMusicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "blackHole")
        
        // Karadelik dönüş animasyonu
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
        
        // Çekim gücü animasyonu
        val gravitationalPull by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pull"
        )
        
        // Işık halkaları animasyonu
        val ringExpansion by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rings"
        )
        
        // Parçacık titreşimi
        val particleVibration by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "vibration"
        )
        
        // Renk geçişi
        val colorPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "colorPhase"
        )
        
        // Arka plan karadelik efekti
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Dış ışık halkaları
            for (i in 5 downTo 1) {
                val radius = 200f * ringExpansion * (i * 0.3f)
                val alpha = 0.1f * (6 - i) / 5f
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0.5f * colorPhase, 0.3f, 1f - 0.5f * colorPhase, alpha),
                            Color(0.3f, 0.1f * colorPhase, 0.8f, alpha * 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }
            
            // Olay ufku
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black,
                        Color(0.1f, 0f, 0.2f, 0.8f),
                        Color(0.3f, 0.1f, 0.5f, 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = 150f * gravitationalPull
                ),
                radius = 150f * gravitationalPull,
                center = center
            )
            
            // Merkez karadelik
            drawCircle(
                color = Color.Black,
                radius = 80f * gravitationalPull,
                center = center
            )
            
            // İç ışıma
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0.6f, 0.2f, 1f, 0.2f),
                        Color(0.4f, 0.1f, 0.8f, 0.4f)
                    ),
                    center = center,
                    radius = 90f * gravitationalPull
                ),
                radius = 90f * gravitationalPull,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Spiral kollar
            for (i in 0..3) {
                val angleOffset = (Math.PI * 2 * i / 4).toFloat()
                val spiralPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, center.y)
                    for (t in 0..100) {
                        val angle = angleOffset + (t * 0.1f) + (rotation * Math.PI / 180).toFloat()
                        val radius = 80f + (t * 2f) * ringExpansion
                        val x = center.x + radius * kotlin.math.cos(angle) + particleVibration
                        val y = center.y + radius * kotlin.math.sin(angle) + particleVibration
                        lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = spiralPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0.5f, 0.2f, 1f, 0.3f),
                            Color.Transparent
                        )
                    ),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        
        // İçerik
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animasyonlu metin
            val textGlow by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "textGlow"
            )
            
            Text(
                text = "No songs in your library",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 1.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color(0.6f, 0.3f, 1f, textGlow),
                        offset = Offset(0f, 0f),
                        blurRadius = 20f * textGlow
                    ),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.graphicsLayer {
                    scaleX = 0.95f + (textGlow * 0.05f)
                    scaleY = 0.95f + (textGlow * 0.05f)
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Your creativity awaits to fill this void",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Animasyonlu buton
            val buttonScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "buttonScale"
            )
            
            Button(
                onClick = onCreateMusicClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .scale(buttonScale)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0.4f, 0.1f, 0.8f, 0.3f),
                                Color(0.6f, 0.2f, 1f, 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0.6f, 0.3f, 1f),
                                Color(0.8f, 0.4f, 1f)
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Create Music",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun sidebarContentPadding(isExpanded: Boolean): Modifier {
    return if (isExpanded) {
        Modifier.padding(start = 65.dp)
    } else {
        Modifier.padding(start = 0.dp)
    }
} 