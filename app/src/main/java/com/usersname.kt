package com

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.aihackathonkarisacikartim.god2.UserDetails
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Kullanıcı adını modern bir şekilde gösteren bileşen
 */
@Composable
fun ModernUsernameBadge(
    modifier: Modifier = Modifier
) {
    // Supabase Manager'dan kullanıcı bilgilerini al
    val supabaseManager = remember { SupabaseManager() }
    var username by remember { mutableStateOf("") }
    var membershipType by remember { mutableStateOf("Standard") }
    var currentUserInfo by remember { mutableStateOf<UserInfo?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Kullanıcı bilgilerini yükle
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val user = supabaseManager.getCurrentUser()
                currentUserInfo = user

                if (user != null) {
                    // Kullanıcı giriş yapmış, kullanıcı adını al
                    val details = supabaseManager.getUserDetails(user.id)
                    username = details?.username ?: user.email?.split("@")?.get(0) ?: "User"

                    // Kullanıcının üyelik tipini al
                    val stats = supabaseManager.getUserStats(user.id)
                    membershipType = stats.membership_type
                } else {
                    username = "Guest"
                    membershipType = "Standard"
                }
            } catch (e: Exception) {
                println("DEBUG: Error getting username: ${e.message}")
                e.printStackTrace()
                username = "User"
                membershipType = "Standard"
            }
        }
    }

    // Orbitron font tanımı
    val orbitronFont = FontFamily.SansSerif

    // Üyelik tipine göre renk ve ikon belirleme
    val (primaryColor, secondaryColor, membershipIcon) = when (membershipType.lowercase()) {
        "pro" -> Triple(Color(0xFF41AE9B), Color(0xFF68E1CC), Icons.Filled.Verified)
        "max" -> Triple(Color(0xFFA155DB), Color(0xFFE278FF), Icons.Filled.Diamond)
        "unlimited" -> Triple(Color(0xFFFF5722), Color(0xFFFFB74D), Icons.Filled.Star)
        else -> Triple(Color(0xFF3B82F6), Color(0xFF60A5FA), null)
    }

    // Animasyon için glow efekti
    val infiniteTransition = rememberInfiniteTransition(label = "badgeGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Kullanıcı adı kartı
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(60.dp)
            .shadow(
                elevation = 4.dp,
                spotColor = primaryColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(30.dp)
            )
            .clip(RoundedCornerShape(30.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0, 0, 0, 180)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.1f),
                            secondaryColor.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Üyelik rozeti (Standard dışındaki üyelikler için)
                if (membershipType.lowercase() != "standard" && membershipIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.7f),
                                        secondaryColor.copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .border(
                                width = 1.5f.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = glowAlpha),
                                        secondaryColor.copy(alpha = glowAlpha * 0.8f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = membershipIcon,
                            contentDescription = membershipType,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Kullanıcı adı
                Text(
                    text = "Hello $username",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = orbitronFont,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Üyelik etiketi
                if (membershipType.lowercase() != "standard") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(primaryColor, secondaryColor)
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = membershipType.uppercase(),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kullanıcı tarafından kullanılabilecek tamamlanmış bileşen
 */
@Composable
fun UsernameBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ModernUsernameBadge()
    }
} 