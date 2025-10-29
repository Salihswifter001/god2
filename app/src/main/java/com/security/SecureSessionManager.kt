package com.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import io.github.jan.supabase.gotrue.user.UserInfo

/**
 * Güvenli oturum yönetimi sınıfı.
 * EncryptedSharedPreferences kullanarak hassas verileri şifreli olarak saklar.
 */
class SecureSessionManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "secure_octa_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }
    
    private val sharedPreferences: SharedPreferences
    
    init {
        // Master key oluştur
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        
        // Şifreli SharedPreferences oluştur
        sharedPreferences = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Kullanıcı oturum bilgilerini güvenli bir şekilde kaydet
     */
    fun saveUserSession(
        userInfo: UserInfo,
        accessToken: String? = null,
        refreshToken: String? = null,
        expiryTime: Long? = null
    ) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userInfo.id)
            putString(KEY_USER_EMAIL, userInfo.email)
            
            // Token bilgilerini kaydet
            accessToken?.let { putString(KEY_ACCESS_TOKEN, it) }
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            expiryTime?.let { putLong(KEY_TOKEN_EXPIRY, it) }
            
            apply()
        }
    }
    
    /**
     * Access token'ı güncelle
     */
    fun updateAccessToken(token: String, expiryTime: Long) {
        with(sharedPreferences.edit()) {
            putString(KEY_ACCESS_TOKEN, token)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
            apply()
        }
    }
    
    /**
     * Oturum bilgilerini temizle
     */
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Kullanıcının giriş yapmış olup olmadığını kontrol et
     */
    fun isLoggedIn(): Boolean {
        // Token süresi kontrolü ekle
        val tokenExpiry = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
        val currentTime = System.currentTimeMillis()
        
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) &&
                (tokenExpiry == 0L || tokenExpiry > currentTime)
    }
    
    /**
     * Token'ın geçerli olup olmadığını kontrol et
     */
    fun isTokenValid(): Boolean {
        val tokenExpiry = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
        return tokenExpiry > System.currentTimeMillis()
    }
    
    /**
     * Kullanıcı ID'sini döndür
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }
    
    /**
     * Kullanıcı e-posta adresini döndür
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Access token'ı döndür
     */
    fun getAccessToken(): String? {
        return if (isTokenValid()) {
            sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        } else {
            null
        }
    }
    
    /**
     * Refresh token'ı döndür
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Güvenlik için session timeout ekle (30 dakika)
     */
    fun checkSessionTimeout(): Boolean {
        val lastActivity = sharedPreferences.getLong("last_activity", 0)
        val currentTime = System.currentTimeMillis()
        val timeout = 30 * 60 * 1000 // 30 dakika
        
        return if (currentTime - lastActivity > timeout) {
            clearSession()
            false
        } else {
            updateLastActivity()
            true
        }
    }
    
    /**
     * Son aktivite zamanını güncelle
     */
    private fun updateLastActivity() {
        sharedPreferences.edit()
            .putLong("last_activity", System.currentTimeMillis())
            .apply()
    }
}