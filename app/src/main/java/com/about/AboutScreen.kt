package com.about

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ModernBlurredBackground
import com.ui.components.BottomNavBar
import com.aihackathonkarisacikartim.god2.R

/**
 * Uygulama hakkında bilgi veren ekran
 */
@Composable
fun AboutScreen(
    navController: NavController,
    onNavigateToMusic: () -> Unit = {},
    onNavigateToMyMusic: () -> Unit = {}
) {
    // Animasyon değişkenleri
    val infiniteTransition = rememberInfiniteTransition(label = "transition")
    val hue by infiniteTransition.animateFloat(
        initialValue = 200f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hue"
    )

    // Ana ekran Scaffold ile
    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentRoute = "about"
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Arkaplan efekti
            ModernBlurredBackground()
            
            // Ana içerik
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Başlık
            Text(
                text = "OctaAI Hakkında",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White,
                            Color.hsv(hue, 0.7f, 0.9f)
                        )
                    )
                ),
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            // Logo ve versiyon bilgisi
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.hsv(hue, 0.3f, 0.1f),
                                Color.hsv(hue, 0.3f, 0.05f)
                            )
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,   
                    contentDescription = null,
                    tint = Color.hsv(hue, 0.8f, 0.9f),
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Text(
                text = "Versiyon 1.1.0",
                color = Color.hsv(hue, 0.3f, 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            
            // Bilgi kartları
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0f, 0.05f, 0.1f, 0.8f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.hsv(hue, 0.7f, 0.5f),
                            Color.hsv(hue + 30f, 0.7f, 0.5f)
                        )
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OctaAI Müzik Oluşturma Teknolojisi",
                        color = Color.hsv(hue, 0.7f, 0.9f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "OctaAI, yapay zeka tabanlı müzik üretimi için geliştirilmiş ileri seviye bir modeldir. Benzersiz, yenilikçi ve tamamen özgün müzikler oluşturmak için 15 milyondan fazla şarkıyı analiz ederek eğitilmiştir.",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .padding(vertical = 8.dp),
                        color = Color.hsv(hue, 0.3f, 0.7f).copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    
                    Text(
                        text = "Teknik Özellikler",
                        color = Color.hsv(hue, 0.5f, 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        TechnicalFeatureItem(
                            title = "Milyarlarca Parametre",
                            description = "OctaAI'ın derin öğrenme modeli, 8.8 milyar parametreye sahiptir ve 24 katmanlı bir sinir ağı mimarisi kullanır.",
                            color = Color.hsv(hue, 0.7f, 0.9f)
                        )
                        
                        TechnicalFeatureItem(
                            title = "Spektrogram Tabanlı Üretim",
                            description = "Ses dalgalarını 2 boyutlu spektrogramlara dönüştürerek, müzikal yapıları ve kalıpları piksel-seviyesinde analiz eder ve oluşturur.",
                            color = Color.hsv(hue, 0.7f, 0.9f)
                        )
                        
                        TechnicalFeatureItem(
                            title = "Çok Katmanlı Diffusion",
                            description = "İleri düzey diffusion modeli sayesinde, kaotik gürültüden başlayarak adım adım müzik oluşturur ve her aşamada ince detaylar ekler.",
                            color = Color.hsv(hue, 0.7f, 0.9f)
                        )
                        
                        TechnicalFeatureItem(
                            title = "Çoklu Enstrüman Ayırma",
                            description = "16 farklı müzik enstrümanını aynı anda modelleyebilir ve her bir enstrümanın özelliklerini ayrı ayrı kontrol edebilir.",
                            color = Color.hsv(hue, 0.7f, 0.9f)
                        )
                        
                        TechnicalFeatureItem(
                            title = "Vokal Sentezleme",
                            description = "Gelişmiş metinden konuşmaya teknolojisi ile gerçekçi şarkı sözleri ve vokal performanslar yaratabilir.",
                            color = Color.hsv(hue, 0.7f, 0.9f)
                        )
                        
                        TechnicalFeatureItem(
                            title = "Ritim ve Armoni Yakalama",
                            description = "Her türün ritimsel ve armonik özelliklerini anlayan, türe özgü müzikal yapıları başarıyla yakalayan özel algoritmalar içerir.",
                            color = Color.hsv(hue, 0.7f, 0.9f)
                        )
                        
                        TechnicalFeatureItem(
                            title = "Düşük Gecikme Süresi",
                            description = "Optimize edilmiş paralel hesaplama ile ortalama bir şarkı oluşturma süresi yaklaşık 2-3 dakikadır.",
                            color = Color.hsv(hue, 0.7f, 0.9f)
                        )
                    }
                }
            }
            
            // Telif hakkı bilgisi
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0f, 0.05f, 0.1f, 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OctaAI ile oluşturulan tüm müzikler, tamamen kullanıcıya aittir. Yaratılan içerikler için telif hakkı bulunmamaktadır ve ticari amaçlarla kullanılabilir.",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Telif hakkı bilgisi
            Text(
                text = "© 2024 OctaAI - Tüm Hakları Saklıdır",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

/**
 * Teknik özellik öğesi
 */
@Composable
fun TechnicalFeatureItem(
    title: String,
    description: String,
    color: Color
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun TechnicalFeatureItem(
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A0033).copy(alpha = 0.7f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
} 