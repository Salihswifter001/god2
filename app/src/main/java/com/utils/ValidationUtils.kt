package com.utils

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Input validation yardımcı sınıfı.
 * Güvenli input kontrolü için kullanılır.
 */
object ValidationUtils {
    
    // Şifre gereksinimleri
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 128
    
    // Kullanıcı adı gereksinimleri
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 30
    
    /**
     * E-posta validasyonu
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && 
               email.length <= 254 && // RFC 5321 standardı
               Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Güçlü şifre validasyonu
     * - En az 8 karakter
     * - En az bir büyük harf
     * - En az bir küçük harf
     * - En az bir rakam
     * - En az bir özel karakter
     */
    fun isValidPassword(password: String): PasswordValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add("Şifre en az $MIN_PASSWORD_LENGTH karakter olmalıdır")
        }
        
        if (password.length > MAX_PASSWORD_LENGTH) {
            errors.add("Şifre en fazla $MAX_PASSWORD_LENGTH karakter olmalıdır")
        }
        
        if (!password.any { it.isUpperCase() }) {
            errors.add("En az bir büyük harf içermelidir")
        }
        
        if (!password.any { it.isLowerCase() }) {
            errors.add("En az bir küçük harf içermelidir")
        }
        
        if (!password.any { it.isDigit() }) {
            errors.add("En az bir rakam içermelidir")
        }
        
        val specialChars = "@#$%^&+=!?*"
        if (!password.any { it in specialChars }) {
            errors.add("En az bir özel karakter içermelidir ($specialChars)")
        }
        
        // Yaygın zayıf şifreleri kontrol et
        val weakPasswords = listOf(
            "12345678", "password", "123456789", "qwerty123",
            "admin123", "letmein", "welcome", "monkey"
        )
        
        if (password.lowercase() in weakPasswords) {
            errors.add("Bu şifre çok yaygın ve güvensiz")
        }
        
        return PasswordValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            strength = calculatePasswordStrength(password)
        )
    }
    
    /**
     * Şifre gücü hesaplama
     */
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0
        
        // Uzunluk puanı
        score += when {
            password.length >= 16 -> 3
            password.length >= 12 -> 2
            password.length >= 8 -> 1
            else -> 0
        }
        
        // Karakter çeşitliliği puanı
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score += 2
        
        return when {
            score >= 7 -> PasswordStrength.VERY_STRONG
            score >= 5 -> PasswordStrength.STRONG
            score >= 3 -> PasswordStrength.MEDIUM
            score >= 2 -> PasswordStrength.WEAK
            else -> PasswordStrength.VERY_WEAK
        }
    }
    
    /**
     * Kullanıcı adı validasyonu
     */
    fun isValidUsername(username: String): UsernameValidationResult {
        val errors = mutableListOf<String>()
        
        if (username.length < MIN_USERNAME_LENGTH) {
            errors.add("Kullanıcı adı en az $MIN_USERNAME_LENGTH karakter olmalıdır")
        }
        
        if (username.length > MAX_USERNAME_LENGTH) {
            errors.add("Kullanıcı adı en fazla $MAX_USERNAME_LENGTH karakter olmalıdır")
        }
        
        // Sadece harf, rakam, alt çizgi ve tire izin ver
        val usernamePattern = "^[a-zA-Z0-9_-]+$"
        if (!Pattern.matches(usernamePattern, username)) {
            errors.add("Kullanıcı adı sadece harf, rakam, alt çizgi ve tire içerebilir")
        }
        
        // İlk karakter harf veya rakam olmalı
        if (username.isNotEmpty() && !username[0].isLetterOrDigit()) {
            errors.add("Kullanıcı adı harf veya rakam ile başlamalıdır")
        }
        
        // Yasaklı kullanıcı adları
        val reservedUsernames = listOf(
            "admin", "root", "administrator", "system", "moderator",
            "support", "help", "api", "null", "undefined"
        )
        
        if (username.lowercase() in reservedUsernames) {
            errors.add("Bu kullanıcı adı kullanılamaz")
        }
        
        return UsernameValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * SQL Injection koruması için string temizleme
     */
    fun sanitizeInput(input: String): String {
        return input
            .replace("'", "''")  // Tek tırnak escape
            .replace("\"", "\"\"") // Çift tırnak escape
            .replace("\\", "\\\\") // Backslash escape
            .replace("%", "\\%")   // LIKE wildcard escape
            .replace("_", "\\_")   // LIKE wildcard escape
            .trim()
    }
    
    /**
     * XSS koruması için HTML karakterlerini escape et
     */
    fun escapeHtml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }
    
    /**
     * Telefon numarası validasyonu (Türkiye formatı)
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        // Türkiye telefon formatı: +90 5XX XXX XX XX
        val phonePattern = "^(\\+90|0)?5[0-9]{9}$"
        val cleanPhone = phone.replace(" ", "").replace("-", "")
        return Pattern.matches(phonePattern, cleanPhone)
    }
    
    /**
     * Kredi kartı numarası validasyonu (Luhn algoritması)
     */
    fun isValidCreditCard(cardNumber: String): Boolean {
        val cleanNumber = cardNumber.replace(" ", "").replace("-", "")
        
        if (!cleanNumber.all { it.isDigit() }) return false
        if (cleanNumber.length !in 13..19) return false
        
        // Luhn algoritması
        var sum = 0
        var isDouble = false
        
        for (i in cleanNumber.length - 1 downTo 0) {
            var digit = cleanNumber[i] - '0'
            
            if (isDouble) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            
            sum += digit
            isDouble = !isDouble
        }
        
        return sum % 10 == 0
    }
    
    /**
     * URL validasyonu
     */
    fun isValidUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }
    
    /**
     * Dosya adı güvenlik kontrolü
     */
    fun isValidFileName(fileName: String): Boolean {
        // Path traversal attack koruması
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return false
        }
        
        // Yasaklı karakterler
        val invalidChars = listOf('<', '>', ':', '"', '|', '?', '*')
        if (fileName.any { it in invalidChars }) {
            return false
        }
        
        // Maksimum uzunluk
        if (fileName.length > 255) {
            return false
        }
        
        return true
    }
    
    /**
     * Dosya uzantısı güvenlik kontrolü
     */
    fun isAllowedFileExtension(fileName: String, allowedExtensions: List<String>): Boolean {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return extension in allowedExtensions.map { it.lowercase() }
    }
}

/**
 * Şifre validasyon sonucu
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val strength: PasswordStrength
)

/**
 * Kullanıcı adı validasyon sonucu
 */
data class UsernameValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

/**
 * Şifre gücü seviyeleri
 */
enum class PasswordStrength {
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}