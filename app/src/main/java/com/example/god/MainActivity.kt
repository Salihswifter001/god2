package com.example.god

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.OctaAIMusicCreator // musicgen.kt'den import
import com.OctaLoginScreen // login.kt'den import
import com.OctaForgotPasswordScreen // forgot.kt'den import
import com.UserProfileScreen // profile.kt'den import
import com.ui.FullMusicLibrary // lib-column.kt'den import
import com.ui.SongItem // lib-column.kt'den import
import com.ui.player.NeonPlayerScreen // Gerçek müzik çalar ekranı
import com.ui.player.ModernPlayerScreen // Modern müzik çalar ekranı
import com.SidebarItem
import com.NeonSidebar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicApi.MusicViewModel
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import com.settings.SessionManager
import com.ui.BlackHoleWelcomeScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// Tema renkleri, MusicGenre data class, MusicGenreSelector vb. tanımlamalar buradan kaldırıldı.
// Bu tanımlamalar musicgen.kt (veya ilgili başka bir dosya) içinde bulunmalı.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            // Uygulamanızın temasını burada ayarlayabilirsiniz
            // Örneğin: YourAppTheme { ... }
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black // Arka plan rengi siyaha çevrildi
            ) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext // applicationContext kullan
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check for existing session
    var startDestination by remember { mutableStateOf("splash") }
    var savedUsername by remember { mutableStateOf("") }
    var showWelcomeScreen by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }
    
    // Check if user is already logged in
    LaunchedEffect(Unit) {
        println("DEBUG MainActivity: Checking session...")
        println("DEBUG MainActivity: Context: $context")
        isLoggedIn = sessionManager.isLoggedIn()
        println("DEBUG MainActivity: Session check - isLoggedIn: $isLoggedIn")
        println("DEBUG MainActivity: Saved userId: ${sessionManager.getUserId()}")
        println("DEBUG MainActivity: Saved email: ${sessionManager.getUserEmail()}")
        println("DEBUG MainActivity: Saved username: ${sessionManager.getUsername()}")
        if (isLoggedIn) {
            // Get username from session or fetch from Supabase
            savedUsername = sessionManager.getUsername() ?: run {
                // If username not in session, fetch from Supabase
                coroutineScope.launch {
                    val supabaseManager = com.aihackathonkarisacikartim.god2.SupabaseManager()
                    val userId = sessionManager.getUserId()
                    if (userId != null) {
                        val userDetails = supabaseManager.getUserDetails(userId)
                        userDetails?.username ?: sessionManager.getUserEmail()?.substringBefore("@") ?: "Kullanıcı"
                    } else {
                        sessionManager.getUserEmail()?.substringBefore("@") ?: "Kullanıcı"
                    }
                }
                sessionManager.getUserEmail()?.substringBefore("@") ?: "Kullanıcı"
            }
            showWelcomeScreen = true
            startDestination = "welcome"
        } else {
            startDestination = "login"
        }
    }
    
    // Mevcut çalan şarkı durumunu takip etmek için
    var currentSong by remember { mutableStateOf<SongItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    
    NavHost(navController = navController, startDestination = if (isLoggedIn) "welcome" else "login") {
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
            OctaLoginScreen(
                onLogin = { username, password ->
                    // Giriş başarılıysa müzik oluşturma ekranına git
                    navController.navigate("music_creator") {
                        // Geri tuşuna basıldığında login ekranına dönmemek için
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate("forgot_password")
                },
                // onMusicScreen parametresi login ekranında varsa ve
                // doğrudan müzik ekranına geçiş içinse:
                onMusicScreen = {
                     navController.navigate("music_creator") {
                         popUpTo("login") { inclusive = true }
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
                        popUpTo("login") { inclusive = true } // Geri tuşuna basıldığında forgot_password'a dönme
                    }
                }
            )
        }
        composable("music_creator") {
            OctaAIMusicCreator(
                onMusicCreated = { prompt, genre ->
                    // Müzik oluşturulduktan sonra yapılacak işlem (şimdilik bir şey yok)
                    println("Music created with prompt: $prompt and genre: $genre")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                navController = navController
            )
            // MusicGenreSelectionScreen() // Bu ekranı OctaAIMusicCreator içine entegre etmiştik
        }
        
        composable("profile") {
            UserProfileScreen(
                onNavigateToMusicGen = {
                    navController.navigate("music_creator")
                },
                navController = navController
            )
        }
        
        // Müzik çalar ekranını ekledik
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
        
        composable("my_music") {
            // Müzik kütüphanesi ekranı 
            Box(modifier = Modifier.fillMaxSize()) {
                // Sidebar bileşeni
                val sidebarItems = listOf(
                    SidebarItem(Icons.Filled.Home, "Ana Sayfa", "home"),
                    SidebarItem(Icons.Filled.Person, "Profilim", "profile"),
                    SidebarItem(Icons.Filled.MusicNote, "Müzik Oluştur", "music"),
                    SidebarItem(Icons.Filled.LibraryMusic, "Müziklerim", "my_music"),
                    SidebarItem(Icons.Filled.VideoLibrary, "Videolar", "videos"),
                    SidebarItem(Icons.Filled.Info, "Hakkında", "about")
                )
                
                var isExpanded by remember { mutableStateOf(false) }
                
                NeonSidebar(
                    items = sidebarItems,
                    selectedRoute = "my_music", // Seçili rota
                    onItemClick = { route -> 
                        when (route) {
                            "profile" -> navController.navigate("profile")
                            "music" -> navController.navigate("music_creator")
                            "my_music" -> navController.navigate("my_music")
                            "about" -> navController.navigate("about")
                            "videos" -> navController.navigate("videos")
                            "home" -> navController.navigate("home")
                            // Diğer rotalar için yönlendirmeleri ekleyin
                        }
                    },
                    isExpanded = isExpanded,
                    onToggleExpand = { isExpanded = !isExpanded },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                
                // Kütüphane içeriği - MusicViewModel'den verileri çekiyoruz
                val musicViewModel: MusicViewModel = viewModel()
                val userMusicsSongItems = produceState<List<SongItem>>(emptyList()) {
                    value = musicViewModel.getUserMusicsAsSongItems()
                }
                
                // İçeriğin padding değeri için animasyon
                val paddingTransition = updateTransition(
                    targetState = isExpanded,
                    label = "paddingTransition"
                )
                
                val startPadding by paddingTransition.animateDp(
                    transitionSpec = {
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    },
                    label = "paddingAnimation"
                ) { expanded -> if (expanded) 200.dp else 65.dp }
                
                if (userMusicsSongItems.value.isEmpty()) {
                    // Yükleniyor durumu
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = startPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0f, 0.8f, 1f) // Neon mavi
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Müzikleriniz yükleniyor...",
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Müzikleri göster
                    FullMusicLibrary(
                        recentlyPlayed = userMusicsSongItems.value,
                        favorites = userMusicsSongItems.value.filter { it.isFavorite },
                        onSongClick = { song ->
                            println("MainActivity - onSongClick called")
                            println("Song details: Title=${song.title}, Artist=${song.artist}, MediaUri=${song.mediaUri}")
                            
                            // Seçilen şarkıyı kaydet ve medya oynatıcı ekranına git
                            currentSong = song
                            isPlaying = true
                            
                            // URL encode the parameters to handle special characters
                            val encodedTitle = Uri.encode(song.title)
                            val encodedArtist = Uri.encode(song.artist)
                            val encodedAlbumArt = Uri.encode(song.albumArt ?: "")
                            val encodedMediaUri = Uri.encode(song.mediaUri ?: "")
                            
                            // Debug log
                            println("Encoded values: Title=$encodedTitle, Artist=$encodedArtist, MediaUri=$encodedMediaUri")
                            
                            val route = "music_player/$encodedTitle/$encodedArtist/$encodedAlbumArt/$encodedMediaUri"
                            println("Navigation route: $route")
                            
                            try {
                                navController.navigate(route)
                                println("Navigation successful")
                            } catch (e: Exception) {
                                println("Navigation failed: ${e.message}")
                                e.printStackTrace()
                            }
                        },
                        onCustomizeClick = { song ->
                            // Seçilen şarkıyı kaydet ve özelleştirme ekranına git
                            currentSong = song
                            navController.navigate("customize_music")
                        },
                        modifier = Modifier.padding(start = startPadding) // Animasyonlu padding
                    )
                }
            }
        }
        // Başka ekranlar varsa buraya eklenebilir (örneğin kayıt ekranı)
        // composable("register") { RegisterScreen(navController) }
    }
}

// MainActivity içindeki @Preview fonksiyonları da kaldırıldı,
// ilgili composable'ların kendi dosyalarında (@Preview) olmalı.