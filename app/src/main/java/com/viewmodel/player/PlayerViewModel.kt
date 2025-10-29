package com.viewmodel.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.model.AudioModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class PlayerViewModel(private val context: Context) : ViewModel() {
    
    // ExoPlayer örneği
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    
    // Müzik dosyaları listesi
    private val _musicFilesLiveData = MutableStateFlow<List<AudioModel>>(emptyList())
    val musicFilesLiveData: StateFlow<List<AudioModel>> = _musicFilesLiveData
    
    // Şu anki şarkı
    private val _currentSongLiveData = MutableStateFlow<AudioModel?>(null)
    val currentSongLiveData: StateFlow<AudioModel?> = _currentSongLiveData
    
    // Çalma durumu
    private val _isPlayingLiveData = MutableStateFlow(false)
    val isPlayingLiveData: StateFlow<Boolean> = _isPlayingLiveData
    
    // Geçerli pozisyon
    private val _currentPositionLiveData = MutableStateFlow(0L)
    val currentPositionLiveData: StateFlow<Long> = _currentPositionLiveData
    
    // Şarkı süresi
    private val _durationLiveData = MutableStateFlow(0L)
    val durationLiveData: StateFlow<Long> = _durationLiveData
    
    // Ses seviyesi
    private val _volumeLiveData = MutableStateFlow(1f)
    val volumeLiveData: StateFlow<Float> = _volumeLiveData
    
    // Karıştırma modu
    private val _isShuffleEnabledLiveData = MutableStateFlow(false)
    val isShuffleEnabledLiveData: StateFlow<Boolean> = _isShuffleEnabledLiveData
    
    // Tekrarlama modu
    private val _isRepeatEnabledLiveData = MutableStateFlow(false)
    val isRepeatEnabledLiveData: StateFlow<Boolean> = _isRepeatEnabledLiveData
    
    // Playback durumu
    private val _playbackStateLiveData = MutableStateFlow(Player.STATE_IDLE)
    val playbackStateLiveData: StateFlow<Int> = _playbackStateLiveData
    
    // Şarkı sözleri
    private val _lyricsLiveData = MutableStateFlow<String?>(null)
    val lyricsLiveData: StateFlow<String?> = _lyricsLiveData
    
    // Pozisyon güncelleme işi
    private var positionUpdateJob: Job? = null
    
    // Geçerli şarkı indeksi
    private var currentSongIndex = 0
    
    // HTTP istemcisi
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    init {
        // ExoPlayer dinleyicisi
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                _playbackStateLiveData.value = state
                
                when (state) {
                    Player.STATE_READY -> {
                        _durationLiveData.value = exoPlayer.duration
                    }
                    Player.STATE_ENDED -> {
                        if (_isRepeatEnabledLiveData.value) {
                            exoPlayer.seekTo(0)
                            exoPlayer.play()
                        } else {
                            playNext()
                        }
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlayingLiveData.value = isPlaying
                
                if (isPlaying) {
                    startPositionUpdateJob()
                } else {
                    positionUpdateJob?.cancel()
                }
            }
        })
    }
    
    /**
     * Konum güncelleme işini başlatır
     */
    private fun startPositionUpdateJob() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                _currentPositionLiveData.value = exoPlayer.currentPosition
                delay(500) // Her 500ms'de bir güncelle
            }
        }
    }
    
    /**
     * Müzik dosyaları listesini ayarlar
     */
    fun setMusicFiles(files: List<AudioModel>) {
        _musicFilesLiveData.value = files
    }
    
    /**
     * Bir şarkıyı çalmaya başlar
     */
    fun playAudio(audioModel: AudioModel) {
        viewModelScope.launch {
            try {
                // Önceki şarkıyı durdur
                exoPlayer.stop()
                
                // Şarkı modelini güncelle
                _currentSongLiveData.value = audioModel
                
                // MediaItem oluştur ve oynatıcıya ayarla
                val mediaItem = MediaItem.fromUri(audioModel.data)
                exoPlayer.setMediaItem(mediaItem)
                
                // Hazırla ve çal
                exoPlayer.prepare()
                exoPlayer.play()
                
                // Şarkı indeksini güncelle
                currentSongIndex = _musicFilesLiveData.value.indexOf(audioModel)
                
                // Şarkı sözlerini getir
                fetchLyrics(audioModel)
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Şarkı oynatılırken hata oluştu", e)
            }
        }
    }
    
    /**
     * Şarkıyı duraklat
     */
    fun pauseAudio() {
        exoPlayer.pause()
    }
    
    /**
     * Şarkıyı devam ettir
     */
    fun resumeAudio() {
        exoPlayer.play()
    }
    
    /**
     * Önceki şarkıyı çal
     */
    fun playPrevious() {
        val musicList = _musicFilesLiveData.value
        if (musicList.isEmpty()) return
        
        val nextIndex = if (_isShuffleEnabledLiveData.value) {
            (0 until musicList.size).random()
        } else {
            // Önceki şarkıya git veya listeyi başa sar
            if (currentSongIndex > 0) currentSongIndex - 1 else musicList.size - 1
        }
        
        playAudio(musicList[nextIndex])
    }
    
    /**
     * Sonraki şarkıyı çal
     */
    fun playNext() {
        val musicList = _musicFilesLiveData.value
        if (musicList.isEmpty()) return
        
        val nextIndex = if (_isShuffleEnabledLiveData.value) {
            (0 until musicList.size).random()
        } else {
            // Sonraki şarkıya git veya listeyi başa sar
            if (currentSongIndex < musicList.size - 1) currentSongIndex + 1 else 0
        }
        
        playAudio(musicList[nextIndex])
    }
    
    /**
     * Belirli bir pozisyona atla
     */
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }
    
    /**
     * Ses seviyesini ayarla
     */
    fun setVolume(volume: Float) {
        _volumeLiveData.value = volume
        exoPlayer.volume = volume
    }
    
    /**
     * Tekrarlama modunu değiştir
     */
    fun toggleRepeat() {
        _isRepeatEnabledLiveData.value = !_isRepeatEnabledLiveData.value
    }
    
    /**
     * Karıştırma modunu değiştir
     */
    fun toggleShuffle() {
        _isShuffleEnabledLiveData.value = !_isShuffleEnabledLiveData.value
    }
    
    /**
     * Şarkı sözlerini getir
     */
    private fun fetchLyrics(audioModel: AudioModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Gerçek uygulamada burada bir API'den veya yerel depolamadan şarkı sözleri alınabilir
                // Bu örnek için basitleştirilmiş şarkı sözleri
                delay(500) // API çağrısı simülasyonu
                
                val sampleLyrics = """
                    Bu bir örnek şarkı sözüdür
                    "${audioModel.title}" için
                    
                    Birinci Verse
                    Şarkı sözleri burada olacak
                    Daha fazla şarkı sözü
                    Ve devam ediyor
                    
                    Nakarat
                    Bu bir nakarat
                    Nakaratlar genellikle tekrarlanır
                    Melodisi daha akılda kalıcıdır
                    
                    İkinci Verse
                    Şarkının ikinci kısmı
                    Genellikle farklı sözler içerir
                    Ama aynı melodi yapısında
                    
                    Nakarat (Tekrar)
                    Bu bir nakarat
                    Nakaratlar genellikle tekrarlanır
                    Melodisi daha akılda kalıcıdır
                    
                    Outro
                    Şarkı böyle sona erer
                    Teşekkürler dinlediğiniz için
                """.trimIndent()
                
                withContext(Dispatchers.Main) {
                    _lyricsLiveData.value = sampleLyrics
                }
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Şarkı sözleri getirilirken hata oluştu", e)
                withContext(Dispatchers.Main) {
                    _lyricsLiveData.value = "Şarkı sözleri bulunamadı."
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        positionUpdateJob?.cancel()
        exoPlayer.release()
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Context
                PlayerViewModel(context)
            }
        }
    }
} 