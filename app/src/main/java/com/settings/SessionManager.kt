package com.settings

import android.content.Context
import android.content.SharedPreferences
import io.github.jan.supabase.gotrue.user.UserInfo

/**
 * Kullanıcı oturum bilgilerini yöneten sınıf.
 * Kullanıcı giriş yaptığında bilgileri kaydeder ve
 * uygulama yeniden başlatıldığında oturum durumunu kontrol eder.
 */
class SessionManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "OctaUserSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USERNAME = "username"
        private const val KEY_PENDING_MUSIC_TASK = "pendingMusicTask"
        private const val KEY_PENDING_MUSIC_TIMESTAMP = "pendingMusicTimestamp"
        private const val KEY_PENDING_MUSIC_TITLE = "pendingMusicTitle"
        private const val KEY_PENDING_MUSIC_PROMPT = "pendingMusicPrompt"
    }
    
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Kullanıcı giriş yaptığında oturum bilgilerini kaydeder
     */
    fun saveUserLoginSession(userInfo: UserInfo, username: String? = null) {
        println("DEBUG SessionManager: Saving session for user: ${userInfo.email}")
        val editor = preferences.edit() // Her seferinde yeni editor oluştur
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, userInfo.id)
        editor.putString(KEY_USER_EMAIL, userInfo.email)
        if (username != null) {
            editor.putString(KEY_USERNAME, username)
        }
        val saved = editor.commit() // commit() kullanarak anında kaydet
        println("DEBUG SessionManager: Session saved successfully: $saved")
        println("DEBUG SessionManager: Immediate check - isLoggedIn: ${isLoggedIn()}")
    }
    
    /**
     * Kullanıcı çıkış yaptığında oturum bilgilerini temizler
     */
    fun clearSession() {
        val editor = preferences.edit()
        editor.clear()
        editor.commit()
        println("DEBUG SessionManager: Session cleared")
    }
    
    /**
     * Kullanıcının giriş yapmış olup olmadığını kontrol eder
     */
    fun isLoggedIn(): Boolean {
        val loggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false)
        println("DEBUG SessionManager.isLoggedIn(): $loggedIn")
        println("DEBUG SessionManager - All prefs: ${preferences.all}")
        return loggedIn
    }
    
    /**
     * Kaydedilmiş kullanıcı ID'sini döndürür
     */
    fun getUserId(): String? {
        return preferences.getString(KEY_USER_ID, null)
    }
    
    /**
     * Mevcut kullanıcı ID'sini döndürür
     * OctaReelsScreen ve OctaReelsUploadScreen için kullanılır
     */
    fun getCurrentUserId(): String? {
        return getUserId()
    }
    
    /**
     * Kaydedilmiş kullanıcı e-posta adresini döndürür
     */
    fun getUserEmail(): String? {
        return preferences.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Kaydedilmiş kullanıcı adını döndürür
     */
    fun getUsername(): String? {
        return preferences.getString(KEY_USERNAME, null)
    }
    
    /**
     * Bekleyen müzik üretim görevini kaydeder
     */
    fun savePendingMusicTask(taskId: String, title: String, prompt: String) {
        val editor = preferences.edit()
        editor.putString(KEY_PENDING_MUSIC_TASK, taskId)
        editor.putLong(KEY_PENDING_MUSIC_TIMESTAMP, System.currentTimeMillis())
        editor.putString(KEY_PENDING_MUSIC_TITLE, title)
        editor.putString(KEY_PENDING_MUSIC_PROMPT, prompt)
        editor.commit()
    }
    
    /**
     * Bekleyen müzik üretim görevini döndürür
     */
    fun getPendingMusicTask(): String? {
        // 24 saatten eski görevleri yoksay
        val timestamp = preferences.getLong(KEY_PENDING_MUSIC_TIMESTAMP, 0)
        val currentTime = System.currentTimeMillis()
        val hoursPassed = (currentTime - timestamp) / (1000 * 60 * 60)
        
        return if (hoursPassed < 24) {
            preferences.getString(KEY_PENDING_MUSIC_TASK, null)
        } else {
            clearPendingMusicTask()
            null
        }
    }
    
    /**
     * Bekleyen müzik başlığını döndürür
     */
    fun getPendingMusicTitle(): String? {
        return preferences.getString(KEY_PENDING_MUSIC_TITLE, null)
    }
    
    /**
     * Bekleyen müzik açıklamasını döndürür
     */
    fun getPendingMusicPrompt(): String? {
        return preferences.getString(KEY_PENDING_MUSIC_PROMPT, null)
    }
    
    /**
     * Bekleyen müzik üretim görevini temizler
     */
    fun clearPendingMusicTask() {
        val editor = preferences.edit()
        editor.remove(KEY_PENDING_MUSIC_TASK)
        editor.remove(KEY_PENDING_MUSIC_TIMESTAMP)
        editor.remove(KEY_PENDING_MUSIC_TITLE)
        editor.remove(KEY_PENDING_MUSIC_PROMPT)
        editor.commit()
    }
} 