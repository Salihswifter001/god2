package com.aihackathonkarisacikartim.god2.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Medya oynatıcı yönetimi için helper sınıf
 */
class MediaPlayerManager(private val context: Context) {
    
    // ExoPlayer örneği
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    
    // Oynatıcı durumları
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    
    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _duration.value = player.duration.coerceAtLeast(0L)
                        _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                        updatePlayingState()
                    }
                    Player.STATE_ENDED -> {
                        // Şarkı bittiğinde otomatik olarak durdurup başa sarıyor
                        player.seekTo(0)
                        player.pause()
                        updatePlayingState()
                    }
                    else -> {}
                }
            }
            
            override fun onIsPlayingChanged(playing: Boolean) {
                updatePlayingState()
            }
        })
    }
    
    // Medya yükleme ve oynatma
    fun loadAndPlay(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
    
    // Oynat/Duraklat
    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        updatePlayingState()
    }
    
    // Durdur
    fun stop() {
        player.stop()
        updatePlayingState()
    }
    
    // İleri sar
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }
    
    // İlerleme yüzdesi
    fun seekToPercent(percent: Float) {
        val position = (_duration.value * percent).toLong()
        seekTo(position)
    }
    
    // Player durumunu günceller
    private fun updatePlayingState() {
        _isPlaying.value = player.isPlaying
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
    }
    
    // Kaynakları serbest bırak
    fun release() {
        player.release()
    }
} 