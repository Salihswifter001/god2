package com.settings

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.core.os.LocaleListCompat
import java.util.Locale

// Dil seçenekleri enum sınıfı
enum class Language(val displayName: String, val code: String) {
    TURKISH("Türkçe", "tr"),
    ENGLISH("English", "en")
}

/**
 * Uygulama dil ayarlarını yöneten sınıf
 */
class LanguageManager(private val context: Context) {
    
    companion object {
        const val LANGUAGE_PREFERENCE = "language_preference"
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val LANGUAGE_CODE_TURKISH = "tr"
        const val LANGUAGE_CODE_ENGLISH = "en"
        private const val PREFERENCES_NAME = "language_preferences"
    }
    
    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    
    fun getCurrentLanguageCode(): String {
        return sharedPreferences.getString(LANGUAGE_PREFERENCE, LANGUAGE_CODE_ENGLISH) ?: LANGUAGE_CODE_ENGLISH
    }
    
    fun getCurrentLanguage(): Language {
        val code = getCurrentLanguageCode()
        return when (code) {
            LANGUAGE_CODE_TURKISH -> Language.TURKISH
            else -> Language.ENGLISH
        }
    }
    
    fun isFirstLaunch(): Boolean {
        // Dil seçimi kaldırıldı - her zaman false döndür
        return false
    }
    
    fun setFirstLaunchCompleted() {
        sharedPreferences.edit().putBoolean(IS_FIRST_LAUNCH, false).apply()
    }
    
    fun saveLanguagePreference(languageCode: String) {
        sharedPreferences.edit().putString(LANGUAGE_PREFERENCE, languageCode).apply()
    }
    
    fun applyCurrentLanguage() {
        applyLanguage(getCurrentLanguageCode())
    }
    
    /**
     * Dili değiştir ve uygula
     */
    fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                    LocaleList(locale)
            } catch (e: Exception) {
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
            }
        } else {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            try {
                val localeList = LocaleListCompat.create(locale)
                val cls = Class.forName("androidx.appcompat.app.AppCompatDelegate")
                val method = cls.getMethod("setApplicationLocales", LocaleListCompat::class.java)
                method.invoke(null, localeList)
            } catch (e: Exception) {
                Log.e("LanguageManager", "AppCompatDelegate kullanılamadı", e)
            }
        }
        
        saveLanguagePreference(languageCode)
    }
    
    /**
     * Dili güncelle ve aktiviteyi yeniden başlat
     */
    fun updateLanguageAndRecreateActivity(language: Language, activity: Activity?) {
        Log.d("LanguageManager", "Dil değiştiriliyor: ${language.code}, Aktivite: ${activity?.javaClass?.simpleName}")
        
        saveLanguagePreference(language.code)
        applyLanguage(language.code)
        
        activity?.let {
            val intent = it.intent
            it.finish()
            it.startActivity(intent)
            it.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d("LanguageManager", "Aktivite yeniden başlatıldı")
        } ?: run {
            Log.e("LanguageManager", "Aktivite null, yeniden başlatılamadı")
        }
    }
    
    fun updateLanguage(language: Language) {
        saveLanguagePreference(language.code)
        applyLanguage(language.code)
    }
    
    fun isEnglishSelected(): Boolean {
        return getCurrentLanguageCode() == LANGUAGE_CODE_ENGLISH
    }
    
    fun isTurkishSelected(): Boolean {
        return getCurrentLanguageCode() == LANGUAGE_CODE_TURKISH
    }
} 