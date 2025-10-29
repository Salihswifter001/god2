package com.ui

/**
 * Bir şarkıyı temsil eden veri sınıfı.
 * Bu sınıf, kütüphane sayfasında gösterilen şarkıların verilerini içerir.
 */
data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val albumArt: String? = null,
    val duration: String = "0:00",
    val mediaUri: String? = null,
    val mediaResourceId: Int? = null,
    val isFavorite: Boolean = false,
    val isExplicit: Boolean = false,
    val genre: String? = null,
    val albumId: String? = null,
    val albumName: String? = null,
    val year: Int? = null,
    val promptText: String? = null,  // Şarkının oluşturulmasında kullanılan metin
    val createdAt: Long? = null,     // Oluşturulma zamanı
    val isDownloaded: Boolean = false, // Şarkının indirilip indirilmediği
    val coverArtUrl: String? = null,   // Albüm kapağı URL'si
    val durationInSeconds: Int? = null,  // Saniye cinsinden şarkı süresi
    val musicId: String? = null    // API'den gelen müzik ID'si - müzik uzatma için gerekli
) 