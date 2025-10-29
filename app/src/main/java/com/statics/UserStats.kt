package com.statics

import kotlinx.serialization.Serializable

@Serializable
data class UserStats(
    val userId: String = "",
    val usedCredits: Int = 0,
    val remainingCredits: Int = 100,
    val lastLogin: String? = null,
    val membershipType: String = "free",
    val created_music_count: Int = 0,
    val favorite_genres: Map<String, Int> = mapOf()
)