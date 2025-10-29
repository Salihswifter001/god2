package com.ui.extend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicApi.MusicExtendService
import com.musicApi.MusicExtendNotificationService
import com.ui.NeonColors
import com.ui.NeonMusicTheme
import com.ui.NeonTheme
import com.ui.SongItem
import com.ui.player.PlayerViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.window.Dialog
import com.ui.components.MusicLoadingAnimation
import com.aihackathonkarisacikartim.god2.GeneratedMusicData
import com.aihackathonkarisacikartim.god2.SupabaseManager
import java.util.UUID
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas
import android.util.Log
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Music extension screen - allows users to extend songs with artificial intelligence
 */
@Composable
fun MusicExtendScreen(
    songItem: SongItem,
    onBackClick: () -> Unit,
    onExtendSuccess: (String) -> Unit
) {
    println("MusicExtendScreen başlatıldı: ${songItem.title}, ID: ${songItem.id}")
    
    // UI görüntüleme bayrağı
    var isScreenVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(songItem.id) {
        println("MusicExtendScreen: LaunchedEffect çalıştı - UI gösterilmeye başlanıyor")
        isScreenVisible = true
    }
    
    val context = LocalContext.current
    val musicExtendService = remember { MusicExtendService(context) }
    val coroutineScope = rememberCoroutineScope()
    val supabaseManager = remember { SupabaseManager() }
    
    // İstek parametreleri için state'ler
    var defaultParamFlag by remember { mutableStateOf(true) } // Her zaman özel mod
    var lyrics by remember { mutableStateOf("") } // Lyrics field
    var genre by remember { mutableStateOf("") } // Genre field
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf(songItem.title) }
    var continueAt by remember { mutableStateOf(30) } // Varsayılan 30 saniye
    val modelOptions = listOf("V3_5", "V3", "V2")
    var selectedModel by remember { mutableStateOf("V3_5") }
    var negativeTags by remember { mutableStateOf("") }
    
    // İşlem durumu
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var taskId by remember { mutableStateOf<String?>(null) }
    var showFullScreenLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("Extending music...") }
    
    // Animasyonlar için
    val infiniteTransition = rememberInfiniteTransition(label = "UI Animations")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )
    
    // TaskID artık background service tarafından yönetiliyor
    // UI'de periyodik kontrol yapmaya gerek yok, service notification gösterecek
    
    if (!isScreenVisible) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Tam ekran yükleme ekranı
    if (showFullScreenLoading) {
        Dialog(
            onDismissRequest = { /* kapatılamaz */ }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                MusicLoadingAnimation(
                    message = loadingMessage,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
    
    // Tema ile sarmalayalım
    NeonMusicTheme {
        // Arka plan ile birlikte tam sayfa içerik
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeonColors.DeepBlue,
                            NeonColors.DarkBackground
                        )
                    )
                )
        ) {
            // Animasyonlu arka plan
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp * (1f - glowIntensity * 0.2f))
            ) {
                // Gradient elipsler çiz
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonColors.ElectricPink.copy(alpha = 0.2f * glowIntensity),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.2f, size.height * 0.3f),
                        radius = size.width * 0.6f
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.3f),
                    radius = size.width * 0.6f
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonColors.NeonBlue.copy(alpha = 0.2f * glowIntensity),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.7f),
                        radius = size.width * 0.7f
                    ),
                    center = Offset(size.width * 0.8f, size.height * 0.7f),
                    radius = size.width * 0.7f
                )
            }
            
            // Ana içerik
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Başlıktan önce ekstra boşluk
                Spacer(modifier = Modifier.height(24.dp))
                
                // Başlık satırı
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Geri butonu
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NeonColors.DarkBackground.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonColors.ElectricPink
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Başlık
                    Text(
                        text = "Add Musical Elements",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Song information card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(
                            elevation = 8.dp,
                            spotColor = NeonColors.NeonBlue.copy(alpha = 0.3f * glowIntensity)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NeonColors.DarkBackground.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonColors.NeonBlue.copy(alpha = 0.5f * glowIntensity),
                                NeonColors.ElectricPink.copy(alpha = 0.5f * glowIntensity)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = NeonColors.ElectricPink,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 8.dp)
                            )
                            
                            Column {
                                Text(
                                    text = songItem.title,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                
                                Text(
                                    text = songItem.artist,
                                    color = NeonColors.TextSecondary,
                                    fontSize = 16.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Duration: ${songItem.duration}",
                            color = NeonColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Parametre seçimi satırı kaldırıldı - her zaman özel mod aktif
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    // Başlık
                    NeonTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Music Title with Added Elements",
                        icon = Icons.Default.Title,
                        glowIntensity = glowIntensity
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lyrics field - main input for lyrics
                    NeonTextField(
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        label = "Add Lyrics",
                        icon = Icons.Default.MusicNote,
                        singleLine = false,
                        maxLines = 6,
                        glowIntensity = glowIntensity,
                        readOnly = false,
                        placeholder = "Enter your lyrics here..."
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Song structure tags
                    Text(
                        text = "Song Structure Tags",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeonColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Structure tag buttons
                    val structureTags = listOf(
                        "[INTRO]" to NeonColors.NeonBlue,
                        "[VERSE]" to NeonColors.ElectricPink,
                        "[PRE-CHORUS]" to NeonColors.DeepPink,
                        "[CHORUS]" to NeonColors.ElectricGreen,
                        "[BRIDGE]" to NeonColors.HotPink,
                        "[OUTRO]" to NeonColors.CyberBlue
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // First row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            structureTags.take(3).forEach { (tag, color) ->
                                OutlinedButton(
                                    onClick = {
                                        lyrics = if (lyrics.isEmpty()) tag
                                        else "$lyrics\n\n$tag\n"
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = color,
                                        containerColor = color.copy(alpha = 0.1f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = color.copy(alpha = 0.5f * glowIntensity)
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    enabled = !isLoading
                                ) {
                                    Text(
                                        text = tag.replace("[", "").replace("]", ""),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        // Second row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            structureTags.drop(3).forEach { (tag, color) ->
                                OutlinedButton(
                                    onClick = {
                                        lyrics = if (lyrics.isEmpty()) tag
                                        else "$lyrics\n\n$tag\n"
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = color,
                                        containerColor = color.copy(alpha = 0.1f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = color.copy(alpha = 0.5f * glowIntensity)
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    enabled = !isLoading
                                ) {
                                    Text(
                                        text = tag.replace("[", "").replace("]", ""),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stil
                    NeonTextField(
                        value = style,
                        onValueChange = { style = it },
                        label = "Music Style",
                        icon = Icons.Default.Style,
                        glowIntensity = glowIntensity
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Genre
                    NeonTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = "Genre",
                        icon = Icons.Default.Category,
                        glowIntensity = glowIntensity,
                        placeholder = "e.g., Pop, Rock, Jazz, Electronic..."
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Negatif Etiketler  
                    NeonTextField(
                        value = negativeTags,
                        onValueChange = { negativeTags = it },
                        label = "Unwanted Features",
                        icon = Icons.Default.NotInterested,
                        glowIntensity = glowIntensity
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Hep gösterilen genel ayarlar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Devam edilecek saniye
                    Text(
                        text = "Second to Add Elements: $continueAt",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Müziğin uzunluğunu parse edelim
                    val durationParts = songItem.duration.split(":")
                    val totalSeconds = if (durationParts.size >= 2) {
                        val minutes = durationParts[0].toIntOrNull() ?: 0
                        val seconds = durationParts[1].toIntOrNull() ?: 0
                        minutes * 60 + seconds
                    } else {
                        0
                    }
                    
                    // En az 15 saniye, en fazla müziğin uzunluğu kadar
                    val minSeconds = 15
                    val maxSeconds = totalSeconds.coerceAtLeast(minSeconds)
                    
                    Slider(
                        value = continueAt.toFloat(),
                        onValueChange = { continueAt = it.toInt() },
                        valueRange = minSeconds.toFloat()..maxSeconds.toFloat(),
                        steps = (maxSeconds - minSeconds),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonColors.ElectricPink,
                            activeTrackColor = NeonColors.NeonBlue,
                            inactiveTrackColor = NeonColors.DarkBackground
                        )
                    )
                    
                    // Second options based on music length
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val stepCount = 5 // Kaç adet adım göstereceğiz
                        val step = (maxSeconds - minSeconds) / stepCount.coerceAtLeast(1)
                        
                        // En az 15 saniye ve müziğin uzunluğu arasında eşit aralıklı değerler
                        val secondsList = if (step > 0) {
                            List(stepCount + 1) { minSeconds + it * step }.distinct()
                        } else {
                            listOf(minSeconds)
                        }
                        
                        secondsList.forEach { second ->
                            Text(
                                text = "$second",
                                color = if (second == continueAt) NeonColors.ElectricPink else NeonColors.TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { continueAt = second }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // İstek gönderme butonu - Arka plan servisi kullanıyor
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            resultMessage = null
                            isError = false
                            
                            try {
                                println("Starting music extension request: ${songItem.id}")
                                
                                // MusicId kontrolü
                                if (songItem.musicId.isNullOrBlank()) {
                                    resultMessage = "Error: This song was not created with API. musicId not found."
                                    isError = true
                                    return@launch
                                }
                                
                                // First, send the extend request to get taskId
                                // Combine lyrics and genre into prompt
                                val combinedPrompt = buildString {
                                    if (genre.isNotBlank()) {
                                        append("Genre: $genre\n\n")
                                    }
                                    append(lyrics)
                                }
                                
                                val result = musicExtendService.extendMusic(
                                    defaultParamFlag = defaultParamFlag,
                                    audioId = songItem.musicId ?: "",
                                    prompt = combinedPrompt,
                                    style = style,
                                    title = title,
                                    continueAt = continueAt,
                                    model = "V4", // Using latest model by default
                                    negativeTags = negativeTags
                                )
                                
                                if (result.isSuccess) {
                                    val newTaskId = result.getOrThrow()
                                    println("Music extension request successful: $newTaskId")
                                    
                                    // Start the background service to track the extension
                                    val serviceIntent = android.content.Intent(context, MusicExtendNotificationService::class.java).apply {
                                        action = MusicExtendNotificationService.ACTION_START
                                        putExtra(MusicExtendNotificationService.EXTRA_TASK_ID, newTaskId)
                                        putExtra(MusicExtendNotificationService.EXTRA_SONG_ID, songItem.id)
                                        putExtra(MusicExtendNotificationService.EXTRA_TITLE, title)
                                        putExtra(MusicExtendNotificationService.EXTRA_PROMPT, combinedPrompt)
                                        putExtra(MusicExtendNotificationService.EXTRA_GENRE, genre.ifBlank { songItem.genre ?: "Pop" })
                                        putExtra(MusicExtendNotificationService.EXTRA_COVER_URL, songItem.albumArt ?: "")
                                        putExtra(MusicExtendNotificationService.EXTRA_DURATION, songItem.durationInSeconds?.toLong() ?: 180L)
                                    }
                                    
                                    // Start the service
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(serviceIntent)
                                    } else {
                                        context.startService(serviceIntent)
                                    }
                                    
                                    // Show success message and navigate back
                                    android.widget.Toast.makeText(
                                        context,
                                        "Extension started! Check notification for progress.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    
                                    // Navigate back after a short delay
                                    kotlinx.coroutines.delay(1500)
                                    onExtendSuccess(newTaskId)
                                    
                                } else {
                                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                                    println("Music extension request failed: $error")
                                    resultMessage = "Error: $error"
                                    isError = true
                                }
                            } catch (e: Exception) {
                                println("Music extension exception: ${e.message}")
                                resultMessage = "Error: ${e.message}"
                                isError = true
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 8.dp,
                            spotColor = NeonColors.ElectricPink.copy(alpha = 0.4f * glowIntensity)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        NeonColors.MidnightPurple,
                                        NeonColors.DeepBlue
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Arka plan glow efekti
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .align(Alignment.Center)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            NeonColors.ElectricPink.copy(alpha = 0.7f * glowIntensity),
                                            NeonColors.NeonBlue.copy(alpha = 0.7f * glowIntensity),
                                            NeonColors.ElectricPink.copy(alpha = 0.7f * glowIntensity)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )
                        
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = NeonColors.ElectricPink,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upgrade,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Add Musical Elements",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Sonuç mesajı
                AnimatedVisibility(visible = resultMessage != null) {
                    resultMessage?.let { message ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isError) 
                                    NeonColors.ElectricPink.copy(alpha = 0.2f) 
                                else 
                                    NeonColors.ElectricGreen.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isError) NeonColors.ElectricPink else NeonColors.ElectricGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = message,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Bilgi metni
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NeonColors.DarkBackground.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "About Adding Musical Elements",
                            color = NeonColors.NeonBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "This feature allows you to add new musical elements to your existing songs using artificial intelligence. Processing may take a few minutes. When completed, it will appear in your library.",
                            color = NeonColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

/**
 * Neon stilinde özelleştirilmiş TextField
 */
@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    glowIntensity: Float = 1f,
    readOnly: Boolean = false,
    placeholder: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Metin alanını çevreleyen kutu
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Arka plan glow efekti
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (singleLine) 54.dp else (32.dp * maxLines.coerceAtLeast(2) + 16.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .blur(radius = 6.dp * glowIntensity)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                NeonColors.NeonBlue.copy(alpha = 0.15f * glowIntensity),
                                NeonColors.ElectricPink.copy(alpha = 0.15f * glowIntensity),
                                NeonColors.NeonBlue.copy(alpha = 0.15f * glowIntensity)
                            )
                        )
                    )
            )
            
            // Ana metin alanı
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        spotColor = NeonColors.NeonBlue.copy(alpha = 0.3f * glowIntensity)
                    ),
                label = {
                    Text(
                        text = label,
                        color = NeonColors.TextSecondary.copy(alpha = 0.8f)
                    )
                },
                leadingIcon = if (icon != null) {
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = NeonColors.NeonBlue.copy(alpha = 0.8f * glowIntensity)
                        )
                    }
                } else null,
                placeholder = if (placeholder != null) {
                    {
                        Text(
                            text = placeholder,
                            color = NeonColors.TextSecondary.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NeonColors.DarkBackground.copy(alpha = 0.8f),
                    unfocusedContainerColor = NeonColors.DarkBackground.copy(alpha = 0.6f),
                    focusedBorderColor = NeonColors.NeonBlue.copy(alpha = 0.8f * glowIntensity),
                    unfocusedBorderColor = NeonColors.NeonBlue.copy(alpha = 0.4f),
                    cursorColor = NeonColors.NeonBlue,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp
                ),
                singleLine = singleLine,
                maxLines = maxLines,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                readOnly = readOnly
            )
        }
    }
} 