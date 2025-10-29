package com.about

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ui.components.BlackHoleBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAboutScreen(
    navController: NavController,
    onNavigateToMusic: () -> Unit = {},
    onNavigateToMyMusic: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // Animasyonlar
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Kara delik arka plan
        BlackHoleBackground(
            modifier = Modifier
                .fillMaxSize()
                .blur(1.dp)
        )
        
        // Koyu overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Geri butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
            
            // Logo ve başlık
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulse),
                contentAlignment = Alignment.Center
            ) {
                // Dönen halka
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF00D9FF),
                                    Color(0xFF8B5CF6),
                                    Color(0xFFFF00FF),
                                    Color(0xFF00D9FF)
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Merkez logo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.3f),
                                    Color(0xFF00D9FF).copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Başlık
            Text(
                text = "OctaAI Music",
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00D9FF),
                            Color(0xFF8B5CF6),
                            Color(0xFFFF00FF)
                        )
                    )
                )
            )
            
            Text(
                text = "Version 1.1.0",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = "The Future of AI Music Production",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            
            // Tab seçici
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF00D9FF),
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("About") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Privacy") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Terms of Use") }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // İçerik
            when (selectedTab) {
                0 -> AboutContent()
                1 -> PrivacyPolicyContent()
                2 -> TermsOfServiceContent()
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00D9FF).copy(alpha = 0.3f),
                            Color(0xFF8B5CF6).copy(alpha = 0.3f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Contact",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ContactButton(
                            icon = Icons.Filled.Email,
                            text = "support@octaai.com",
                            onClick = { uriHandler.openUri("mailto:support@octaai.com") }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        ContactButton(
                            icon = Icons.Filled.Language,
                            text = "octaai.com",
                            onClick = { uriHandler.openUri("https://octaai.com") }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "© 2024 OctaAI Music - All Rights Reserved",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AboutContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Özellikler
        FeatureCard(
            icon = Icons.Filled.AutoAwesome,
            title = "OctaAI V4.5PLUS Technology",
            description = "Create professional quality music with our most advanced AI music production model that we developed in-house. Our unique AI model learned from 15 million songs."
        )
        
        FeatureCard(
            icon = Icons.Filled.Speed,
            title = "Fast Production",
            description = "High-quality music production in an average of 2-3 minutes. Optimized parallel processing technology."
        )
        
        FeatureCard(
            icon = Icons.Filled.MusicNote,
            title = "Multiple Genre Support",
            description = "Ability to produce in Pop, Rock, Jazz, Classical, Electronic and many more music genres."
        )
        
        FeatureCard(
            icon = Icons.Filled.Mic,
            title = "Vocal Synthesis",
            description = "Professional sound quality with realistic vocal performances and lyrics."
        )
        
        FeatureCard(
            icon = Icons.Filled.Copyright,
            title = "No Copyright Issues",
            description = "All generated music belongs to you. You have all usage rights including commercial use."
        )
        
        // İstatistikler
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF8B5CF6).copy(alpha = 0.1f)
            ),
            border = BorderStroke(
                1.dp,
                Color(0xFF8B5CF6).copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("10K+", "Users")
                StatItem("50K+", "Generated Music")
                StatItem("4.8", "Rating")
            }
        }
    }
}

@Composable
fun PrivacyPolicyContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PolicySection(
            title = "Veri Toplama",
            content = "OctaAI Music, hizmet kalitesini artırmak için minimal kullanıcı verisi toplar. Toplanan veriler:\n\n" +
                    "• E-posta adresi (hesap oluşturma için)\n" +
                    "• Üretilen müzik tercihleri (kişiselleştirme için)\n" +
                    "• Uygulama kullanım istatistikleri (anonim)"
        )
        
        PolicySection(
            title = "Veri Güvenliği",
            content = "Tüm kullanıcı verileri şifrelenerek saklanır. Verileriniz:\n\n" +
                    "• 256-bit AES şifreleme ile korunur\n" +
                    "• Güvenli Supabase sunucularında barındırılır\n" +
                    "• GDPR ve KVKK uyumlu olarak işlenir\n" +
                    "• Üçüncü taraflarla paylaşılmaz"
        )
        
        PolicySection(
            title = "Kullanıcı Hakları",
            content = "Her kullanıcının şu hakları vardır:\n\n" +
                    "• Verilerini görüntüleme ve düzeltme\n" +
                    "• Hesap ve verileri silme\n" +
                    "• Veri taşınabilirliği talep etme\n" +
                    "• Pazarlama iletişimlerinden çıkma"
        )
        
        PolicySection(
            title = "Çerezler",
            content = "Uygulama deneyimini iyileştirmek için çerezler kullanılır:\n\n" +
                    "• Oturum çerezleri (giriş durumu)\n" +
                    "• Tercih çerezleri (tema, dil)\n" +
                    "• Analitik çerezleri (anonim kullanım)"
        )
        
        PolicySection(
            title = "İletişim",
            content = "Gizlilik politikası hakkında sorularınız için:\n\n" +
                    "E-posta: privacy@octaai.com\n" +
                    "Telefon: +90 850 XXX XX XX\n" +
                    "Adres: İstanbul, Türkiye"
        )
    }
}

@Composable
fun TermsOfServiceContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PolicySection(
            title = "Hizmet Kullanımı",
            content = "OctaAI Music kullanarak aşağıdaki şartları kabul edersiniz:\n\n" +
                    "• 13 yaş ve üzeri olmalısınız\n" +
                    "• Hesap bilgilerinizi gizli tutmalısınız\n" +
                    "• Hizmeti yasal amaçlar için kullanmalısınız\n" +
                    "• Diğer kullanıcıların haklarına saygı göstermelisiniz"
        )
        
        PolicySection(
            title = "İçerik Sahipliği",
            content = "Üretilen içerikler hakkında:\n\n" +
                    "• OctaAI ile ürettiğiniz müzikler size aittir\n" +
                    "• Ticari kullanım hakkı sizindir\n" +
                    "• OctaAI, üretilen içeriklerde hak iddia etmez\n" +
                    "• Telif hakkı ihlali durumunda sorumluluk kullanıcıya aittir"
        )
        
        PolicySection(
            title = "Abonelik ve Ödemeler",
            content = "Abonelik şartları:\n\n" +
                    "• Aylık ve yıllık abonelik seçenekleri\n" +
                    "• Otomatik yenileme (iptal edilmediği sürece)\n" +
                    "• İptal durumunda dönem sonuna kadar kullanım\n" +
                    "• 14 gün içinde iade garantisi"
        )
        
        PolicySection(
            title = "Yasaklı Kullanımlar",
            content = "Aşağıdaki kullanımlar kesinlikle yasaktır:\n\n" +
                    "• Telif hakkı ihlali içeren içerik üretimi\n" +
                    "• Nefret söylemi veya zararlı içerik\n" +
                    "• Servisleri hack etme veya zarar verme\n" +
                    "• Spam veya otomatik bot kullanımı"
        )
        
        PolicySection(
            title = "Sorumluluk Reddi",
            content = "OctaAI Music:\n\n" +
                    "• Kesintisiz hizmet garantisi vermez\n" +
                    "• Üretilen içeriğin amaca uygunluğunu garanti etmez\n" +
                    "• Veri kaybından sorumlu değildir\n" +
                    "• Force majeure durumlarında sorumluluk kabul etmez"
        )
        
        PolicySection(
            title = "Değişiklikler",
            content = "Bu şartlar önceden haber verilmeksizin değiştirilebilir. Önemli değişiklikler e-posta ile bildirilir.\n\n" +
                    "Son güncelleme: 30 Ağustos 2025"
        )
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00D9FF).copy(alpha = 0.2f),
                                Color(0xFF8B5CF6).copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF00D9FF),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun PolicySection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.03f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF00D9FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = content,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00D9FF),
                        Color(0xFF8B5CF6)
                    )
                )
            )
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ContactButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color(0xFF00D9FF)
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            textDecoration = TextDecoration.Underline
        )
    }
}