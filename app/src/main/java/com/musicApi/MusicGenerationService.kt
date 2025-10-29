package com.musicApi

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.aihackathonkarisacikartim.god2.MainActivity
import com.aihackathonkarisacikartim.god2.R
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.settings.SessionManager
import com.incrementCreatedMusic
import kotlinx.coroutines.*

/**
 * Foreground Service - Android tarafından ASLA durdurulmaz!
 * Müzik oluşturma işlemini arka planda güvenle takip eder
 */
class MusicGenerationService : Service() {
    
    companion object {
        const val CHANNEL_ID = "MusicGenerationChannel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "START_MUSIC_GENERATION"
        const val ACTION_STOP = "STOP_MUSIC_GENERATION"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_PROMPT = "prompt"
        const val EXTRA_USER_ID = "user_id"
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var musicApiService: MusicApiService
    private lateinit var supabaseManager: SupabaseManager
    private lateinit var sessionManager: SessionManager
    private var checkJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        musicApiService = MusicApiService(this)
        supabaseManager = SupabaseManager()
        sessionManager = SessionManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Müzik"
                val prompt = intent.getStringExtra(EXTRA_PROMPT) ?: ""
                val userId = intent.getStringExtra(EXTRA_USER_ID)
                
                if (taskId != null && userId != null) {
                    startForeground(NOTIFICATION_ID, createNotification(title))
                    startMusicCheck(taskId, title, prompt, userId)
                }
            }
            ACTION_STOP -> {
                stopMusicCheck()
                stopForeground(true)
                stopSelf()
            }
        }
        
        return START_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Müzik Oluşturma",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Müzik oluşturma işlemi devam ediyor"
                setShowBadge(false)
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(title: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicGenerationService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎵 Müzik Oluşturuluyor")
            .setContentText(title)
            .setSubText("Yeni müziğiniz hazırlanıyor...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setColor(Color.parseColor("#667EEA"))
            .setColorized(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "İptal",
                stopIntent
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("\uD83C\uDFB5 $title\n\uD83D\uDD04 Müziğiniz API'de işleniyor...")
                    .setSummaryText("Müzik Oluşturuluyor")
            )
            .setProgress(100, 0, true)
            .build()
    }
    
    private fun updateNotification(message: String, progress: Int = -1) {
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicGenerationService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎵 Müzik Oluşturuluyor")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setColor(Color.parseColor("#667EEA"))
            .setColorized(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "İptal",
                stopIntent
            )
        
        if (progress >= 0) {
            builder.setProgress(100, progress, false)
                .setSubText("$progress% tamamlandı")
        } else {
            builder.setProgress(100, 0, true)
                .setSubText("Kontrol ediliyor...")
        }
        
        val notification = builder.build()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun startMusicCheck(taskId: String, title: String, prompt: String, userId: String) {
        checkJob?.cancel()
        checkJob = serviceScope.launch {
            var attempts = 0
            var isCompleted = false
            
            Log.d("MusicService", "Müzik kontrolü başladı: $taskId")
            
            while (!isCompleted && attempts < 60) { // 10 dakika boyunca kontrol et
                delay(10000) // 10 saniye bekle
                attempts++
                
                val progressPercent = (attempts * 100 / 60)
                updateNotification("Kontrol ediliyor... (${attempts}/60)", progressPercent)
                
                try {
                    val statusResult = musicApiService.checkMusicGenerationStatus(taskId, userId)
                    
                    if (statusResult.isSuccess) {
                        val musicData = statusResult.getOrNull()
                        if (musicData != null) {
                            // Müzik hazır!
                            Log.d("MusicService", "Müzik hazır: $musicData")
                            
                            // Müzik hazır, kaydetme işlemine geç
                            // Önce bu müziğin zaten kaydedilip kaydedilmediğini kontrol et
                            val musicAlreadyExists = supabaseManager.checkMusicExistsByMusicId(
                                musicData.musicId, 
                                userId
                            )
                            
                            if (musicAlreadyExists) {
                                Log.d("MusicService", "Müzik zaten kaydedilmiş, tekrar eklenmeyecek: ${musicData.musicId}")
                                
                                // TaskID'yi temizle
                                sessionManager.clearPendingMusicTask()
                                
                                // Başarı bildirimi (müzik zaten var)
                                showAlreadyExistsNotification(title)
                                
                                isCompleted = true
                            } else {
                                // Müziği Supabase'e kaydet
                                val saveResult = supabaseManager.saveGeneratedMusic(musicData)
                                if (saveResult.isSuccess) {
                                    Log.d("MusicService", "Müzik Supabase'e kaydedildi")
                                    
                                    // Müzik sayısını artır
                                    supabaseManager.incrementCreatedMusic(userId)
                                    
                                    // TaskID'yi temizle
                                    sessionManager.clearPendingMusicTask()
                                    
                                    // Başarı bildirimi
                                    showSuccessNotification(title)
                                    
                                    isCompleted = true
                                } else {
                                    Log.e("MusicService", "Müzik kaydedilemedi: ${saveResult.exceptionOrNull()?.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MusicService", "Kontrol hatası: ${e.message}")
                }
            }
            
            if (!isCompleted) {
                Log.d("MusicService", "Müzik kontrolü zaman aşımına uğradı")
                showTimeoutNotification()
            }
            
            // Service'i durdur
            stopForeground(true)
            stopSelf()
        }
    }
    
    private fun showSuccessNotification(title: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("✅ Müzik Hazır!")
            .setContentText("$title kütüphanenize eklendi")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setColor(Color.parseColor("#48BB78"))
            .setColorized(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("✅ Başarıyla Tamamlandı!\n\n🎵 $title\n📚 Kütüphanenize eklendi")
            )
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    private fun showAuthRequiredNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Giriş Gerekli")
            .setContentText("Müziğiniz hazır! Kütüphaneye eklemek için giriş yapın")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }
    
    private fun showTimeoutNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏱️ Zaman Aşımı")
            .setContentText("Müzik oluşturma uzun sürdü. Lütfen uygulamayı açıp kontrol edin")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 3, notification)
    }
    
    private fun showAlreadyExistsNotification(title: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("✅ Müzik Oluşturma Başarılı!")
            .setContentText("$title kütüphanenize eklendi")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 4, notification)
    }
    
    private fun stopMusicCheck() {
        checkJob?.cancel()
        checkJob = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}