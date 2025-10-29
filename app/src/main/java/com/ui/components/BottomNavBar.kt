package com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: String,
    modifier: Modifier = Modifier
) {
    // Renk tanımlamaları - Glass theme için güncellenmiş
    val backgroundColor = Color(0xFF1A0033)
    val selectedColor = Color(0xFFFF00DE) // Bright pink-purple for glass
    val unselectedColor = Color(0xFFB0B0B0) // Lighter gray for better visibility
    val plusButtonColor = Color(0xFFFF00FF)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Add padding for system navigation bar
            .height(70.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glassmorphism background layers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    spotColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(
                    // Semi-transparent base with gradient
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x1AFFFFFF), // Ultra thin white at top
                            Color(0x0DFFFFFF), // Even thinner at bottom
                        )
                    )
                )
                .blur(radius = 10.dp) // Glass blur effect
                .border(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x40FFFFFF), // Subtle white border top
                            Color(0x1AFFFFFF)  // Fading border bottom
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
        )
        
        // Frosted glass overlay with gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x0D9C27B0), // Ultra thin purple left
                            Color(0x0DE91E63), // Ultra thin pink middle
                            Color(0x0D9C27B0)  // Ultra thin purple right
                        )
                    )
                )
                .drawBehind {
                    // Noise texture for frosted glass effect
                    for (i in 0..15) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.02f),
                            radius = kotlin.random.Random.nextFloat() * 50f,
                            center = Offset(
                                kotlin.random.Random.nextFloat() * size.width,
                                kotlin.random.Random.nextFloat() * size.height
                            )
                        )
                    }
                }
        )
        
        // Dark overlay for better contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(
                    Color(0x40000000) // Semi-transparent black for depth
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Library butonu
            NavBarItem(
                icon = Icons.Default.LibraryMusic,
                label = "Library",
                isSelected = currentRoute == "my_music",
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = {
                    if (currentRoute != "my_music") {
                        navController.navigate("my_music") {
                            popUpTo(navController.graph.startDestinationId) { 
                                saveState = true 
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
            
            // Ortadaki + butonu with glass effect
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = plusButtonColor.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(
                        Color(0x40FFFFFF) // Glass background
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0x80FFFFFF),
                                Color(0x40FFFFFF)
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur(radius = 0.5.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                plusButtonColor.copy(alpha = 0.7f),
                                plusButtonColor.copy(alpha = 0.4f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable {
                        navController.navigate("music_creator") {
                            popUpTo(navController.graph.startDestinationId) { 
                                saveState = true 
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Music",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Profile butonu
            NavBarItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentRoute == "profile",
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = {
                    if (currentRoute != "profile") {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId) { 
                                saveState = true 
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) selectedColor else unselectedColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) selectedColor else unselectedColor,
            fontSize = 11.sp
        )
    }
}