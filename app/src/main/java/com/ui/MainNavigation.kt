package com.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ui.extend.MusicExtendScreen

/**
 * Ana navigasyon sistemi
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    
    // Test verileri
    val songList = remember {
        listOf(
            SongItem(
                id = "1",
                title = "Cosmic Dreams",
                artist = "Nebula Beats",
                duration = "3:24",
                isFavorite = true
            ),
            SongItem(
                id = "2",
                title = "Digital Love",
                artist = "Electronic Heart",
                duration = "4:15",
                isExplicit = true
            ),
            SongItem(
                id = "3",
                title = "Neon Nights",
                artist = "Synth Wave",
                duration = "5:02",
                isFavorite = true,
                isDownloaded = true
            )
        )
    }
    
    // Seçili şarkı
    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    
    NavHost(navController = navController, startDestination = "library") {
        // Kütüphane ekranı
        composable("library") {
            FullMusicLibrary(
                recentlyPlayed = songList,
                favorites = songList.filter { it.isFavorite },
                recommendations = songList,
                onSongClick = { song ->
                    // Şarkı çalma işlemi
                },
                onDownloadClick = { song ->
                    // İndirme işlemi
                },
                onCustomizeClick = { song ->
                    selectedSong = song
                    navController.navigate("extend/${song.id}")
                }
            )
        }
        
        // Müzik uzatma ekranı
        composable(
            route = "extend/{songId}",
            arguments = listOf(navArgument("songId") { type = NavType.StringType })
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId")
            val song = songList.find { it.id == songId } ?: songList.first()
            
            MusicExtendScreen(
                songItem = song,
                onBackClick = { navController.popBackStack() },
                onExtendSuccess = { taskId ->
                    // Uzatma başarılı olduğunda yapılacak işlemler
                    // Örneğin bir süre bekleyip ana sayfaya dönme
                    navController.popBackStack()
                }
            )
        }
    }
} 