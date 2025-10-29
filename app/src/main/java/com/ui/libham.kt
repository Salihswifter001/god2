package com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

/**
 * Müzik kütüphanesi için neon temalı animasyonlu dikey üç nokta menüsü
 */
@Composable
fun NeonMusicOptionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    showDelete: Boolean = true,
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {},
    onAddToFavoritesClick: () -> Unit = {},
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Animasyon durumları
    val density = LocalDensity.current
    val transitionState = remember {
        MutableTransitionState(false).apply {
            targetState = expanded
        }
    }

    // Hedef durumu güncelle
    LaunchedEffect(expanded) {
        transitionState.targetState = expanded
    }

    if (transitionState.currentState || transitionState.targetState) {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
        ) {
            // Popup animasyonu
            val transition = updateTransition(transitionState, "menuTransition")
            val scale by transition.animateFloat(
                transitionSpec = {
                    if (false isTransitioningTo true) {
                        tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    } else {
                        tween(
                            durationMillis = 150,
                            easing = FastOutSlowInEasing
                        )
                    }
                },
                label = "scale"
            ) { state -> if (state) 1f else 0.8f }

            val alpha by transition.animateFloat(
                transitionSpec = {
                    if (false isTransitioningTo true) {
                        tween(
                            durationMillis = 200,
                            easing = LinearEasing
                        )
                    } else {
                        tween(
                            durationMillis = 150,
                            easing = LinearEasing
                        )
                    }
                },
                label = "alpha"
            ) { state -> if (state) 1f else 0f }

            // Kenar ışıması animasyonu
            val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glowAlpha"
            )

            // Menü kartı
            Box(
                modifier = modifier
                    .scale(scale)
                    .alpha(alpha)
                    .padding(end = 8.dp, top = 8.dp)
                    .drawBehind {
                        // Glow effect
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonColors.ElectricPink.copy(alpha = glowAlpha * 0.5f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width * 1.5f
                            ),
                            radius = size.width,
                            center = center,
                            alpha = 0.15f
                        )
                    }
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NeonColors.MidnightPurple.copy(alpha = 0.95f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                NeonColors.ElectricPink.copy(alpha = glowAlpha),
                                NeonColors.NeonBlue.copy(alpha = glowAlpha * 0.7f)
                            )
                        ),
                        width = 1.dp
                    ),
                    modifier = Modifier
                        .width(220.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Favori ekleme/çıkarma seçeneği
                        NeonMenuOption(
                            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            title = if (isFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                            iconTint = if (isFavorite) NeonColors.NeonPink else NeonColors.TextPrimary,
                            onClick = onAddToFavoritesClick
                        )
                        
                        // Çalma listesine ekleme seçeneği
                        NeonMenuOption(
                            icon = Icons.Filled.PlaylistAdd,
                            title = "Çalma Listesine Ekle",
                            onClick = onAddToPlaylistClick
                        )
                        
                        // Paylaşma seçeneği
                        NeonMenuOption(
                            icon = Icons.Filled.Share,
                            title = "Paylaş",
                            onClick = onShareClick
                        )
                        
                        // Silme seçeneği (isteğe bağlı)
                        if (showDelete) {
                            Divider(
                                color = NeonColors.Divider,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            NeonMenuOption(
                                icon = Icons.Filled.Delete,
                                title = "Sil",
                                iconTint = NeonColors.ElectricPink,
                                titleColor = NeonColors.ElectricPink,
                                onClick = onDeleteClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Menüdeki her bir seçenek için bileşen
 */
@Composable
private fun NeonMenuOption(
    icon: ImageVector,
    title: String,
    iconTint: Color = NeonColors.TextPrimary,
    titleColor: Color = NeonColors.TextPrimary,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = true,
                    color = NeonColors.ElectricPink
                ),
                onClick = onClick
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            color = titleColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * Dikey üç nokta butonunu gösteren bileşen
 */
@Composable
fun NeonMoreVertButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    // Glow animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "dotGlow")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Hover ve press için geçiş durumları
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .then(
                if (isActive) {
                    Modifier.background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonColors.ElectricPink.copy(alpha = 0.2f + (glowIntensity * 0.1f)),
                                Color.Transparent
                            )
                        )
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = true,
                    color = NeonColors.ElectricPink
                ),
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Daha Fazla",
            tint = if (isActive) NeonColors.ElectricPink else NeonColors.TextSecondary,
            modifier = Modifier
                .size(24.dp)
                .then(
                    if (isActive) {
                        Modifier.blur(radius = (glowIntensity * 1).dp)
                    } else {
                        Modifier
                    }
                )
        )
    }
}

/**
 * Örnek kullanım için bileşen - neon temada şarkı satırı ile daha vert düğmesi
 */
@Composable
fun SongItemWithMenu(
    songTitle: String,
    artistName: String,
    duration: String,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NeonColors.SlateGray.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Albüm simgesi (örnek için basitleştirilmiş)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NeonColors.MidnightPurple,
                            NeonColors.SlateGray
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = NeonColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Şarkı bilgileri
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = songTitle,
                color = NeonColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = artistName,
                color = NeonColors.TextSecondary,
                fontSize = 14.sp
            )
        }
        
        // Şarkı süresi
        Text(
            text = duration,
            color = NeonColors.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        // Menü düğmesi
        Box {
            NeonMoreVertButton(
                onClick = { showMenu = true },
                isActive = showMenu
            )
            
            // Pop-up menü
            NeonMusicOptionsMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                isFavorite = isFavorite,
                onAddToFavoritesClick = {
                    onFavoriteToggle()
                    showMenu = false
                },
                onDeleteClick = {
                    onDeleteClick()
                    showMenu = false 
                },
                onShareClick = {
                    onShareClick()
                    showMenu = false
                },
                onAddToPlaylistClick = {
                    onAddToPlaylistClick()
                    showMenu = false
                }
            )
        }
    }
}

// Örnek kullanım için ön izleme
@Composable
@androidx.compose.ui.tooling.preview.Preview
fun NeonMenuPreview() {
    NeonMusicTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeonColors.DarkBackground)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SongItemWithMenu(
                    songTitle = "Galaktik Şehir",
                    artistName = "OctaAI",
                    duration = "3:24",
                    isFavorite = true
                )
                
                SongItemWithMenu(
                    songTitle = "Neon Rüyalar",
                    artistName = "OctaAI",
                    duration = "2:56",
                    isFavorite = false
                )
                
                SongItemWithMenu(
                    songTitle = "Dijital Yağmur",
                    artistName = "OctaAI",
                    duration = "4:12",
                    isFavorite = false
                )
            }
        }
    }
} 