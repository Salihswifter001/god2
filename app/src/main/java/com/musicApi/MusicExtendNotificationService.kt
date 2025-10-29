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
import com.aihackathonkarisacikartim.god2.GeneratedMusicData
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.settings.SessionManager
import kotlinx.coroutines.*
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Background Service for Music Extension
 * Handles music extension process with notifications similar to MusicGenerationService
 */
class MusicExtendNotificationService : Service() {
    
    companion object {
        const val CHANNEL_ID = "MusicExtendChannel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "START_MUSIC_EXTEND"
        const val ACTION_STOP = "STOP_MUSIC_EXTEND"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_SONG_ID = "song_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_PROMPT = "prompt"
        const val EXTRA_GENRE = "genre"
        const val EXTRA_COVER_URL = "cover_url"
        const val EXTRA_DURATION = "duration"
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var musicExtendService: MusicExtendService
    private lateinit var supabaseManager: SupabaseManager
    private lateinit var sessionManager: SessionManager
    private var checkJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        musicExtendService = MusicExtendService(this)
        supabaseManager = SupabaseManager()
        sessionManager = SessionManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                val songId = intent.getStringExtra(EXTRA_SONG_ID) ?: ""
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Music"
                val prompt = intent.getStringExtra(EXTRA_PROMPT) ?: ""
                val genre = intent.getStringExtra(EXTRA_GENRE) ?: "Pop"
                val coverUrl = intent.getStringExtra(EXTRA_COVER_URL) ?: ""
                val duration = intent.getLongExtra(EXTRA_DURATION, 180)
                
                if (taskId != null) {
                    startForeground(NOTIFICATION_ID, createNotification(title))
                    startExtendCheck(taskId, songId, title, prompt, genre, coverUrl, duration)
                }
            }
            ACTION_STOP -> {
                stopExtendCheck()
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
                "Music Extension",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music extension process in progress"
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
            Intent(this, MusicExtendNotificationService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎵 Extending Music")
            .setContentText(title)
            .setSubText("Adding musical elements...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setColor(Color.parseColor("#667EEA"))
            .setColorized(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel",
                stopIntent
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🎵 $title\n🔄 Extending with new elements...")
                    .setSummaryText("Music Extension")
            )
            .setProgress(100, 0, true)
            .build()
    }
    
    private fun updateNotification(message: String, progress: Int = -1) {
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicExtendNotificationService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎵 Extending Music")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setColor(Color.parseColor("#667EEA"))
            .setColorized(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel",
                stopIntent
            )
        
        if (progress >= 0) {
            builder.setProgress(100, progress, false)
                .setSubText("$progress% completed")
        } else {
            builder.setProgress(100, 0, true)
                .setSubText("Processing...")
        }
        
        val notification = builder.build()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun startExtendCheck(
        taskId: String,
        songId: String,
        title: String,
        prompt: String,
        genre: String,
        coverUrl: String,
        duration: Long
    ) {
        checkJob?.cancel()
        checkJob = serviceScope.launch {
            var attempts = 0
            var isCompleted = false
            var waitCounter = 0
            
            Log.d("ExtendService", "Music extend check started: $taskId")
            
            // Check every 5 seconds for up to 10 minutes
            while (!isCompleted && attempts < 120) { 
                delay(5000) // Wait 5 seconds
                attempts++
                waitCounter++
                
                // Update notification messages
                val message = when (waitCounter) {
                    in 1..5 -> "Analyzing source file..."
                    in 6..10 -> "Extracting musical elements..."
                    in 11..20 -> "Creating continuation melodies..."
                    in 21..30 -> "Synthesizing new sections..."
                    in 31..40 -> "Arranging transitions..."
                    in 41..50 -> "Finalizing processes..."
                    else -> "Completing extension..."
                }
                
                val progressPercent = (attempts * 100 / 120)
                updateNotification(message, progressPercent)
                
                try {
                    // Check the status from API
                    val statusResult = musicExtendService.checkExtendStatus(taskId)
                    
                    if (statusResult.isSuccess && statusResult.getOrNull() == true) {
                        Log.d("ExtendService", "Extension completed! Getting results: $taskId")
                        
                        // Get detailed results from API
                        val request = Request.Builder()
                            .url("${musicExtendService.getApiBaseUrl()}/generate/record-info?taskId=$taskId")
                            .get()
                            .addHeader("Accept", "application/json")
                            .addHeader("Authorization", musicExtendService.getApiKey())
                            .build()
                        
                        val client = OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build()
                        
                        val responseBody = withContext(Dispatchers.IO) {
                            val response = client.newCall(request).execute()
                            response.body?.string() ?: "{}"
                        }
                        
                        val responseJson = JSONObject(responseBody)
                        
                        // Get user information
                        val currentUser = supabaseManager.getCurrentUser()
                        
                        if (currentUser != null) {
                            if (responseJson.has("data") && !responseJson.isNull("data")) {
                                val data = responseJson.getJSONObject("data")
                                
                                // Check for sunoData in response
                                val hasResponseWithSunoData = data.has("response") && 
                                                          !data.isNull("response") && 
                                                          data.getJSONObject("response").has("sunoData") &&
                                                          !data.getJSONObject("response").isNull("sunoData")
                                
                                if (hasResponseWithSunoData) {
                                    val response = data.getJSONObject("response")
                                    val sunoArray = response.getJSONArray("sunoData")
                                    
                                    if (sunoArray.length() > 0) {
                                        Log.d("ExtendService", "Found ${sunoArray.length()} extended songs")
                                        
                                        var successCount = 0
                                        for (i in 0 until sunoArray.length()) {
                                            val sunoItem = sunoArray.getJSONObject(i)
                                            
                                            // Get audio URL
                                            var audioUrl = ""
                                            if (sunoItem.has("audioUrl") && !sunoItem.isNull("audioUrl")) {
                                                audioUrl = sunoItem.getString("audioUrl")
                                            } else if (sunoItem.has("sourceAudioUrl") && !sunoItem.isNull("sourceAudioUrl")) {
                                                audioUrl = sunoItem.getString("sourceAudioUrl")
                                            }
                                            
                                            // Get image URL
                                            var imageUrl = coverUrl // Use original cover by default
                                            if (sunoItem.has("imageUrl") && !sunoItem.isNull("imageUrl")) {
                                                val apiImageUrl = sunoItem.getString("imageUrl")
                                                if (apiImageUrl.isNotBlank()) {
                                                    imageUrl = apiImageUrl
                                                }
                                            }
                                            
                                            // Get title
                                            var songTitle = title
                                            if (sunoItem.has("title") && !sunoItem.isNull("title")) {
                                                val apiTitle = sunoItem.getString("title")
                                                if (apiTitle.isNotBlank()) {
                                                    songTitle = apiTitle
                                                }
                                            }
                                            
                                            // Skip if no audio URL
                                            if (audioUrl.isBlank()) {
                                                Log.d("ExtendService", "No audio URL for song ${i+1}")
                                                continue
                                            }
                                            
                                            // Extended müzikler için unique musicId oluştur
                                            val uniqueMusicId = "${taskId}_extended_${i+1}_${System.currentTimeMillis()}"
                                            
                                            // Save extended music to database
                                            val extendedMusic = GeneratedMusicData(
                                                userId = currentUser.id,
                                                title = "$songTitle (Extended) ${if (sunoArray.length() > 1) "${i+1}" else ""}",
                                                prompt = prompt.ifEmpty { "Extended version" },
                                                genre = genre,
                                                musicUrl = audioUrl,
                                                coverUrl = imageUrl,
                                                musicId = uniqueMusicId, // Unique musicId kullan
                                                duration = duration + 210 // Add ~3.5 minutes
                                            )
                                            
                                            val saveResult = supabaseManager.saveGeneratedMusic(extendedMusic)
                                            if (saveResult.isSuccess) {
                                                Log.d("ExtendService", "Extended music ${i+1} saved to database")
                                                successCount++
                                            } else {
                                                Log.e("ExtendService", "Failed to save extended music ${i+1}")
                                            }
                                        }
                                        
                                        if (successCount > 0) {
                                            // Clear any pending tasks
                                            sessionManager.clearPendingMusicTask()
                                            
                                            // Show success notification
                                            showSuccessNotification(title, successCount)
                                            
                                            isCompleted = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.w("ExtendService", "No auth session")
                            showAuthRequiredNotification()
                            isCompleted = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ExtendService", "Check error: ${e.message}")
                }
            }
            
            if (!isCompleted) {
                Log.d("ExtendService", "Music extension timed out")
                showTimeoutNotification()
            }
            
            // Stop the service
            stopForeground(true)
            stopSelf()
        }
    }
    
    private fun showSuccessNotification(title: String, count: Int) {
        val message = if (count > 1) {
            "$count extended versions added to your library"
        } else {
            "Extended version added to your library"
        }
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("✅ Extension Complete!")
            .setContentText("$title (Extended)")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setColor(Color.parseColor("#48BB78"))
            .setColorized(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("✅ Successfully Extended!\n\n🎵 $title\n📚 $message")
            )
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    private fun showAuthRequiredNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Login Required")
            .setContentText("Your extended music is ready! Please login to add to library")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }
    
    private fun showTimeoutNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏱️ Timeout")
            .setContentText("Music extension took too long. Please check the app")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 3, notification)
    }
    
    private fun stopExtendCheck() {
        checkJob?.cancel()
        checkJob = null
        Log.d("ExtendService", "Music extension check stopped")
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopExtendCheck()
        serviceScope.cancel()
    }
}