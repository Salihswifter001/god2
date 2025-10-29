package com.ui.player

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.data.model.AudioModel

/**
 * Medya oynatıcı işlemlerini yöneten ViewModel
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    // ExoPlayer örneği
    val exoPlayer = ExoPlayer.Builder(application).build()
    
    // Oynatıcı durumları
    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying
    
    private val _currentPosition = mutableStateOf(0L)
    val currentPosition: State<Long> = _currentPosition
    
    private val _duration = mutableStateOf(0L)
    val duration: State<Long> = _duration
    
    private val _currentSongTitle = mutableStateOf("Şarkı adı")
    val currentSongTitle: State<String> = _currentSongTitle
    
    private val _currentArtist = mutableStateOf("Sanatçı")
    val currentArtist: State<String> = _currentArtist
    
    private var positionUpdateJob: Job? = null
    
    // MusicList
    private val _musicFilesFlow = MutableStateFlow<List<AudioModel>>(emptyList())
    val musicFilesFlow: StateFlow<List<AudioModel>> = _musicFilesFlow
    
    // Geçerli şarkı
    private val _currentSongFlow = MutableStateFlow<AudioModel?>(null)
    val currentSongFlow: StateFlow<AudioModel?> = _currentSongFlow
    
    // Kaydedilecek şarkı ID'si
    private val _currentSongId = mutableStateOf<String?>(null)
    val currentSongId: State<String?> = _currentSongId
    
    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                        updatePlayingState()
                    }
                    Player.STATE_ENDED -> {
                        playNext()
                    }
                    Player.STATE_BUFFERING -> {
                        // Buffering durumu
                    }
                    Player.STATE_IDLE -> {
                        // Boşta durumu
                    }
                }
            }
            
            override fun onIsPlayingChanged(playing: Boolean) {
                updatePlayingState()
            }
        })
        
        startPositionTracking()
    }
    
    // Konum izleme
    private fun startPositionTracking() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                delay(200)
            }
        }
    }
    
    // Müzik oynatma
    fun playMedia(uri: Uri, title: String, artist: String, songId: String? = null) {
        try {
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            
            _currentSongTitle.value = title
            _currentArtist.value = artist
            _currentSongId.value = songId
            
            exoPlayer.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Asset klasöründen medya yükleme
    fun playMediaFromAsset(context: Context, fileName: String, title: String, artist: String) {
        val uri = Uri.parse("asset:///$fileName")
        playMedia(uri, title, artist)
    }
    
    // Raw klasöründen medya yükleme
    fun playMediaFromRaw(context: Context, resourceId: Int, title: String, artist: String) {
        val uri = Uri.parse("android.resource://${context.packageName}/$resourceId")
        playMedia(uri, title, artist)
    }
    
    // Oynat/Duraklat
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        updatePlayingState()
    }
    
    // İlerleme
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }
    
    // Yüzdelik ilerleme
    fun seekToPercent(percent: Float) {
        val position = (_duration.value * percent).toLong()
        seekTo(position)
    }
    
    // 10 saniye geri
    fun skipBackward() {
        val newPosition = (_currentPosition.value - 10000).coerceAtLeast(0)
        seekTo(newPosition)
    }
    
    // 10 saniye ileri
    fun skipForward() {
        val newPosition = (_currentPosition.value + 10000).coerceAtMost(_duration.value)
        seekTo(newPosition)
    }
    
    // Sonraki şarkı
    fun playNext() {
        val currentSong = _currentSongFlow.value ?: return
        val musicList = _musicFilesFlow.value
        if (musicList.isEmpty()) return
        
        val currentIndex = musicList.indexOfFirst { it.id == currentSong.id }
        if (currentIndex == -1) return
        
        val nextIndex = if (currentIndex < musicList.size - 1) {
            currentIndex + 1
            } else {
            0 // Başa dön
        }
        
        playAudio(musicList[nextIndex])
    }
    
    // Önceki şarkı
    fun playPrevious() {
        val currentSong = _currentSongFlow.value ?: return
        val musicList = _musicFilesFlow.value
        if (musicList.isEmpty()) return
        
        val currentIndex = musicList.indexOfFirst { it.id == currentSong.id }
        if (currentIndex == -1) return
        
        val prevIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            musicList.size - 1 // Sona git
        }
        
        playAudio(musicList[prevIndex])
    }
    
    // Müzik çal
    fun playAudio(audioModel: AudioModel) {
        try {
            exoPlayer.stop()
            
            val mediaItem = MediaItem.fromUri(audioModel.data)
            exoPlayer.setMediaItem(mediaItem)
            
            exoPlayer.prepare()
            exoPlayer.play()
            
            _currentSongFlow.value = audioModel
            _currentSongTitle.value = audioModel.title
            _currentArtist.value = audioModel.artist ?: "Bilinmeyen Sanatçı"
            _currentSongId.value = audioModel.id
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Durum güncelleme
    private fun updatePlayingState() {
        _isPlaying.value = exoPlayer.isPlaying
    }
    
    // Müzik listesini ayarla
    fun setMusicFiles(audioFiles: List<AudioModel>) {
        _musicFilesFlow.value = audioFiles
    }
    
    override fun onCleared() {
        positionUpdateJob?.cancel()
        exoPlayer.release()
        super.onCleared()
    }
} 