package com.ui.player.utils

import java.util.concurrent.TimeUnit

/**
 * Müzik oynatıcı için yardımcı fonksiyonlar
 */
object MusicUtils {
    
    /**
     * Milisaniye cinsinden süreyi formatlı olarak döndürür (mm:ss)
     * @param duration Milisaniye cinsinden süre
     * @return "mm:ss" formatında süre stringi
     */
    fun formatDuration(duration: Long): String {
        if (duration <= 0) return "0:00"
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - 
                     TimeUnit.MINUTES.toSeconds(minutes)
        
        return String.format("%d:%02d", minutes, seconds)
    }
    
    /**
     * Saniye cinsinden süreyi formatlı olarak döndürür (mm:ss)
     * @param durationInSeconds Saniye cinsinden süre
     * @return "mm:ss" formatında süre stringi
     */
    fun formatDurationFromSeconds(durationInSeconds: Long): String {
        if (durationInSeconds <= 0) return "0:00"
        
        val minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds)
        val seconds = durationInSeconds - TimeUnit.MINUTES.toSeconds(minutes)
        
        return String.format("%d:%02d", minutes, seconds)
    }
    
    /**
     * Saniye cinsinden süreyi daha detaylı bir formatta döndürür (hh:mm:ss veya mm:ss)
     * Bir saatten uzun süreler için saat de gösterilir
     * @param durationInSeconds Saniye cinsinden süre
     * @return "hh:mm:ss" veya "mm:ss" formatında süre stringi
     */
    fun formatDurationLong(durationInSeconds: Long): String {
        if (durationInSeconds <= 0) return "0:00"
        
        val hours = TimeUnit.SECONDS.toHours(durationInSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds) - 
                     TimeUnit.HOURS.toMinutes(hours)
        val seconds = durationInSeconds - TimeUnit.MINUTES.toSeconds(minutes) - 
                     TimeUnit.HOURS.toSeconds(hours)
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
} 