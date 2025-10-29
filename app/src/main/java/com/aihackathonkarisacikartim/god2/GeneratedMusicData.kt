package com.aihackathonkarisacikartim.god2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

// Bu sınıf artık SupabaseManager.kt'den taşındı. Artık burada tanımlıyoruz.
@Serializable
data class GeneratedMusicData(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("user_id")
    val userId: String,
    val title: String = "",
    val prompt: String? = null,
    val genre: String = "",
    @SerialName("music_url")
    val musicUrl: String,
    @SerialName("cover_url")
    val coverUrl: String,
    @SerialName("music_id")
    val musicId: String = "", // API'den gelen müzik ID'si
    val duration: Long = 180,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
) 