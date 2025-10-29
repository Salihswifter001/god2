package com.settings

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalTextApi::class)
@Composable
fun LanguageSelectionScreen(
    navController: NavController,
    languageManager: LanguageManager,
    onLanguageSelected: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    val context = LocalContext.current
    
    // Neon renkleri
    val neonColors = listOf(
        Color(0xFF00FF00), // Neon Yeşil
        Color(0xFF00FFFF), // Neon Cyan
        Color(0xFFFF00FF)  // Neon Pembe
    )
    
    // Animasyon için
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val position by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Dil Seçin / Select Language",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = neonColors,
                        start = Offset(0f, 0f),
                        end = Offset(position * 100f, 0f)
                    ),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            
            // Dil seçenekleri
            Language.values().forEach { language ->
                val isSelected = selectedLanguage == language
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(neonColors)
                            } else {
                                Brush.horizontalGradient(listOf(Color.DarkGray, Color.DarkGray))
                            }
                        )
                        .clickable {
                            selectedLanguage = language
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = language.displayName,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            // Devam butonu
            AnimatedVisibility(
                visible = selectedLanguage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = {
                        selectedLanguage?.let { language ->
                            languageManager.updateLanguageAndRecreateActivity(language, context as? Activity)
                            languageManager.setFirstLaunchCompleted()
                            onLanguageSelected()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FF00)
                    )
                ) {
                    Text(
                        text = "Devam Et / Continue",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
} 