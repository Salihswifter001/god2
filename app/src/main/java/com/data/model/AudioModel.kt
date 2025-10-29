package com.data.model

import android.net.Uri

/**
 * Ses dosyası bilgilerini temsil eden model sınıfı
 */
data class AudioModel(
    val id: String,               // Dosya benzersiz kimliği
    val data: Uri,                // Dosya URI'si
    val title: String,            // Şarkı başlığı
    val artist: String? = null,   // Sanatçı adı
    val album: String? = null,    // Albüm adı
    val duration: Long = 0,       // Şarkı süresi (ms cinsinden)
    val albumArtUri: Uri? = null, // Albüm kapak resmi URI'si
    val addedDate: Long = 0,      // Eklenme tarihi (milisaniye olarak timestamp)
    val isFavorite: Boolean = false // Favori durumu
) 