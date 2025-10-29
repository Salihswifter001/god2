package com

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ModernBlurredBackground
import com.NeonGlowButton
import com.UsernameBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import com.incrementCreatedMusic
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.updateFavoriteGenre
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.musicApi.MusicViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicApi.MusicUiState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.ui.components.MusicLoadingAnimation

enum class MusicCreationState {
    IDLE,
    CREATING,
    COMPLETED,
    ERROR
}

// Music genres list (First 16 genres)
val musicGenres = listOf(
    "EDM", "Hip Hop", "Pop", "Rock", "Classical", "Jazz", "Ambient", "Lo-Fi",
    "Trap", "R&B", "Synthwave", "Techno", "Country", "Metal", "Reggae", "Blues" // First 16 genres
    // "Funk", "Soul", "Disco", "House", "Trance", "Dubstep", "Drum & Bass",
    // "Orchestral", "Cinematic", "Folk", "Acoustic", "Indie", "Alternative"
)

@Composable
fun OptimizedBlurredBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    // Ana animasyon değerleri
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientPosition"
    )

    // İkinci katman animasyonu
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "secondaryGradient"
    )

    // Parçacık efekti için parlama animasyonu
    val particleGlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particleGlow"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Modern gradyan arka plan - daha yumuşak geçişli
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0D0E23),  // Dark navy/purple
                            Color(0xFF1A1B35),  // Medium navy/purple
                            Color(0xFF080A1A)   // Very dark navy
                        ),
                        center = Offset(offsetX * 1000f, offsetY * 1000f),
                        radius = 1500f
                    )
                )
        )

        // İkincil katman - daha dinamik efekt için
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Yumuşak dalgalı çizgiler
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Dinamik efektler için farklı opasite değerleri
            val baseAlpha = 0.05f + 0.03f * particleGlow

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = baseAlpha * 2),  // Indigo
                        Color(0xFF0EA5E9).copy(alpha = baseAlpha),      // Mavi
                        Color.Transparent
                    )
                ),
                radius = canvasWidth * 0.6f,
                center = Offset(
                    x = canvasWidth * (0.2f + 0.6f * offsetX),
                    y = canvasHeight * (0.3f + 0.4f * offsetY)
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = baseAlpha * 2),  // Purple
                        Color(0xFF6366F1).copy(alpha = baseAlpha),      // Indigo
                        Color.Transparent
                    )
                ),
                radius = canvasWidth * 0.5f,
                center = Offset(
                    x = canvasWidth * (0.8f - 0.6f * offsetX),
                    y = canvasHeight * (0.7f - 0.4f * offsetY)
                )
            )
        }

        // İnce ızgara deseni - AI arayüzleri için tipik
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF6366F1).copy(alpha = 0.03f)
                        )
                    )
                )
                .drawBehind {
                    val strokeWidth = 0.5f
                    val spacing = 40.dp.toPx()

                    // Yatay çizgiler
                    for (i in 0..(size.height / spacing).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.04f),
                            start = Offset(0f, i * spacing),
                            end = Offset(size.width, i * spacing),
                            strokeWidth = strokeWidth
                        )
                    }

                    // Dikey çizgiler
                    for (i in 0..(size.width / spacing).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.04f),
                            start = Offset(i * spacing, 0f),
                            end = Offset(i * spacing, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
                }
        )
    }
}

@Composable
fun OptimizedLoadingAnimation(
    message: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF60A5FA)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    // Animasyon için değişkenler
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val progressWidth by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progressWidth"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Modern AI-style loading
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Yükleniyor simgesi
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dönen dış halka
                    Canvas(modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer { rotationZ = rotation }
                    ) {
                        drawArc(
                            color = color,
                            startAngle = 0f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // İç halka
                    Canvas(modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer { rotationZ = -rotation * 0.7f }
                    ) {
                        drawArc(
                            color = color.copy(alpha = 0.7f),
                            startAngle = 180f,
                            sweepAngle = 240f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Merkez ikon
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .size(30.dp)
                            .scale(pulse)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Processing message
                Text(
                    text = "Creative Process in Progress",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Detaylı mesaj
                Text(
                    text = message,
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // İlerleme çubuğu
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressWidth)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        color,
                                        color.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .align(Alignment.CenterStart)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Motivational message
                Text(
                    text = "Your AI music is being prepared, please wait...",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.5f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun OctaAIMusicCreator(
    onMusicCreated: (prompt: String, genre: String) -> Unit = { _, _ -> },
    onNavigateToProfile: () -> Unit = {},
    navController: NavController
) {
    // Orbitron font
    val orbitronFont = FontFamily.SansSerif // Orbitron fontunu projenizde tanımlayın

    // MusicViewModel kullanıyoruz
    val musicViewModel: MusicViewModel = viewModel()

    // Farklı müzik türleri için neon renk paleti (Daha fazla renk ekleyebilirsiniz)
    val neonGenreColors = remember { // remember ile state değiştiğinde yeniden hesaplanmasını engelle
        listOf(
            Color(0xFF06B6D4), // Turkuaz
            Color(0xFF22C55E), // Yeşil
            Color(0xFFD946EF), // Fuşya
            Color(0xFFFACC15), // Sarı
            Color(0xFF3B82F6), // Mavi
            Color(0xFFEC4899), // Pembe
            Color(0xFF84CC16), // Limon
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEF4444), // Kırmızı
            Color(0xFFF97316), // Turuncu
            Color(0xFF9333EA), // Dark Purple
            Color(0xFF0EA5E9), // Açık Mavi
            Color(0xFFF472B6), // Açık Pembe
            Color(0xFF4ADE80), // Açık Yeşil
            Color(0xFF2563EB), // Dark Blue
            Color(0xFFA855F7)  // Eflatun
        ).take(musicGenres.size) // musicGenres listesi kadar renk al
    }

    // ViewModel'den durumları alalım
    val prompt by musicViewModel.prompt
    val selectedGenre by musicViewModel.selectedGenre
    val uiState by musicViewModel.uiState.collectAsState()
    val isVocalModeActive by musicViewModel.isVocalModeActive
    val vocalInput by musicViewModel.vocalInput
    val title by musicViewModel.title

    // State değişkenleri
    var creationState by remember { mutableStateOf(MusicCreationState.IDLE) }
    var showElements by remember { mutableStateOf(false) }
    var showGenres by remember { mutableStateOf(true) }
    var showButton by remember { mutableStateOf(false) }

    // Ses kaydı için değişkenler
    var isRecording by remember { mutableStateOf(false) }
    // Vokal kayıt özelliği yakında eklenecek
    var showRecordingPopup by remember { mutableStateOf(false) } // Coming soon
    var recordingTimer by remember { mutableStateOf(0) }

    // State variables related to music creation
    var waveAnimation by remember { mutableStateOf(0f) }
    var waveAnimationJob by remember { mutableStateOf<Job?>(null) }
    var generatedMusicUrl by remember { mutableStateOf<String?>(null) }

    // Tema rengi
    val primaryHue by remember { mutableStateOf(210f) }

    // Uygulama arkaplandan dönünce bekleyen müzikleri kontrol et
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                musicViewModel.checkForPendingMusic()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // UI durumunu MusicCreationState'e dönüştür
    LaunchedEffect(uiState) {
        when (uiState) {
            is MusicUiState.Idle -> {
                creationState = MusicCreationState.IDLE
            }
            is MusicUiState.Loading -> {
                creationState = MusicCreationState.CREATING
                // Dalga animasyonunu başlat
                waveAnimationJob?.cancel()
                waveAnimationJob = launch {
                    while (true) {
                        delay(100)
                        waveAnimation += 0.1f
                    }
                }
            }
            is MusicUiState.Success -> {
                creationState = MusicCreationState.COMPLETED
                // Dalga animasyonunu durdur
                waveAnimationJob?.cancel()

                // 5 saniye sonra state'i sıfırla
                delay(5000)
                creationState = MusicCreationState.IDLE
            }
            is MusicUiState.Error -> {
                creationState = MusicCreationState.ERROR
                // Dalga animasyonunu durdur
                waveAnimationJob?.cancel()
            }
        }
    }

    // Kayıt zamanlayıcısı
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTimer = 0
            while (isRecording) {
                delay(1000)
                recordingTimer++

                // Maksimum kayıt süresi (30 saniye)
                if (recordingTimer >= 30) {
                    isRecording = false
                    // Kaydı bitir ve kaydet
                    // TODO: Gerçek kaydetme işlemini implement et
                    val filePath = "recording_${System.currentTimeMillis()}.mp3"
                    musicViewModel.setRecordedVocalPath(filePath)
                }
            }
        }
    }

    // Coroutine scope, focus yönetimi
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val promptFocusRequester = remember { FocusRequester() }
    val promptInteractionSource = remember { MutableInteractionSource() }
    val isPromptFocused by promptInteractionSource.collectIsFocusedAsState()

    // Animasyon değişkenleri - optimize edilmiş
    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val hue by infiniteTransition.animateFloat(
        initialValue = primaryHue,
        targetValue = primaryHue + 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hue"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,  // Daha küçük pulse aralığı
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Glow faktörü - basitleştirilmiş
    val getGlowFactor = { 0.7f } // Sabit bir değer kullanarak hesaplamaları azalt

    // UI öğelerini aşamalı gösterme
    LaunchedEffect(Unit) {
        delay(300)
        showElements = true
        delay(500)
        showGenres = true
        delay(300)
        showButton = true
    }

    // Kayıt popup dialog - Coming Soon
    /* if (showRecordingPopup) {
        Dialog(
            onDismissRequest = {
                if (!isRecording) showRecordingPopup = false
            }
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF3B82F6).copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Başlık
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Vocal Recording",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ses dalgası animasyonu
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isRecording)
                                Color(0xFFF43F5E).copy(alpha = 0.5f)
                            else
                                Color(0xFF3B82F6).copy(alpha = 0.3f)
                        )
                    ) {
                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Animasyonlu ses dalgası
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(30) { i ->
                                        val barHeight = remember { Animatable(0.2f) }
                                        LaunchedEffect(waveAnimation) {
                                            val targetValue = 0.2f + 0.8f * sin(
                                                (waveAnimation * 5 + i * 0.2f) * 2 * PI.toFloat()
                                            ).let { abs(it) }.coerceIn(0.2f, 1f)
                                            barHeight.animateTo(
                                                targetValue = targetValue,
                                                animationSpec = tween(300, easing = LinearEasing)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(80.dp * barHeight.value)
                                                .background(
                                                    color = if (i % 3 == 0)
                                                        Color(0xFFF43F5E).copy(alpha = 0.7f)
                                                    else
                                                        Color(0xFF60A5FA).copy(alpha = 0.7f),
                                                    RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                }

                                // Kayıt süresi
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF7F1D1D).copy(alpha = 0.7f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color(0xFFEF4444), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = String.format("%02d:%02d", recordingTimer / 60, recordingTimer % 60),
                                            color = Color.White,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontFamily = orbitronFont,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        } else if (musicViewModel.recordedVocalPath.value != null) {
                            // Kaydedilmiş vokal dosyası varsa göster
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Vocal recorded",
                                        color = Color.White,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontFamily = orbitronFont,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Press 'OK' to confirm recording",
                                        color = Color(0xFF94A3B8),
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = orbitronFont
                                        )
                                    )
                                }
                            }
                        } else {
                            // Kayıt talimatları
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Ready to record",
                                        color = Color.White,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontFamily = orbitronFont,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Press record button to start\nMaximum 30 seconds",
                                        color = Color(0xFF94A3B8),
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = orbitronFont
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Coming Soon Badge - AI Vokal Klonlama
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier,
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1A0033).copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6),
                                        Color(0xFF00D9FF),
                                        Color(0xFFFF00FF)
                                    )
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🚀 Coming Soon: AI Vocal Cloning",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = orbitronFont,
                                        fontWeight = FontWeight.Bold,
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF00D9FF),
                                                Color(0xFF8B5CF6)
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }
                    
                    // Aksiyon butonları
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Kayıt butonu - Devre dışı (Coming Soon)
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Glow efekti
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF8B5CF6).copy(alpha = 0.4f),
                                                Color(0xFF00D9FF).copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                            
                            Button(
                                onClick = { /* Disabled - Coming Soon */ },
                                enabled = false,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF3B82F6).copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Coming Soon",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // İptal butonu (sadece kayıtta değilken aktif)
                        OutlinedButton(
                            onClick = {
                                // Kaydı temizle ve dialog'u kapat
                                if (!isRecording) {
                                    musicViewModel.setRecordedVocalPath(null)
                                    showRecordingPopup = false
                                }
                            },
                            enabled = !isRecording,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.8f),
                                disabledContentColor = Color.White.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF64748B).copy(alpha = if (!isRecording) 0.5f else 0.2f)
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Tamam butonu (sadece kayıtta değilken aktif)
                        Button(
                            onClick = {
                                // Dialog'u kapat ve kaydı sakla
                                if (!isRecording) {
                                    showRecordingPopup = false
                                }
                            },
                            enabled = !isRecording && musicViewModel.recordedVocalPath.value != null,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6),
                                disabledContainerColor = Color(0xFF3B82F6).copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "OK",
                                color = Color.White,
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    } */

    // Arkaplan - optimize edilmiş
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // Kütüphane sayfasındaki gibi arkaplan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0014),   // Çok koyu mor/siyah
                            Color(0xFF1A0033),   // Koyu mor
                            Color(0xFF0A0014)    // Çok koyu mor/siyah
                        )
                    )
                )
        )
        
        // Grid pattern overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val gridSpacing = 40.dp.toPx()
                    val gridColor = Color(0xFFAA00FF).copy(alpha = 0.03f)
                    
                    // Yatay çizgiler
                    for (i in 0..(size.height / gridSpacing).toInt()) {
                        val y = i * gridSpacing
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 0.5f
                        )
                    }
                    
                    // Dikey çizgiler
                    for (i in 0..(size.width / gridSpacing).toInt()) {
                        val x = i * gridSpacing
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 0.5f
                        )
                    }
                }
        )
        
        // Scaffold ile bottom navigation ve içerik
        Scaffold(
            bottomBar = {
                com.ui.components.BottomNavBar(
                    navController = navController,
                    currentRoute = "music_creator"
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Animasyonlu arkaplan - optimize edilmiş
            // OptimizedBlurredBackground() // Commented out for consistent bottom bar appearance

            // Ana içerik kolonu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
        ) {
            // Beta version warning message - at the top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF97316).copy(alpha = 0.2f) // Turuncu yarı-transparan arka plan
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.7f)) // Turuncu kenarlık
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF97316), // Turuncu ikon
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Beta Version",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF97316) // Turuncu başlık
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This app is in beta version. Errors may occur, thank you for your understanding.",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = orbitronFont,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }

            // Yükleniyor durumunda basitleştirilmiş animasyon göster
            if (creationState == MusicCreationState.CREATING) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    OptimizedLoadingAnimation(
                        message = (uiState as? MusicUiState.Loading)?.message ?: "Creating music...",
                        modifier = Modifier.fillMaxWidth(0.8f),
                        color = Color.hsv(hue, 0.8f, 0.8f)
                    )
                }
            } else {
                // Ana içerik - sadece yükleme durumu değilse göster
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // UI State Message (Loading/Error/Success)
                    AnimatedVisibility(
                        visible = uiState !is MusicUiState.Idle,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        when (uiState) {
                            is MusicUiState.Loading -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF60A5FA),
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Processing",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = (uiState as MusicUiState.Loading).message,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                            is MusicUiState.Error -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = Color(0xFFF87171),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Error Occurred",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = (uiState as MusicUiState.Error).message,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                            is MusicUiState.Success -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4ADE80),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Successful",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = (uiState as MusicUiState.Success).message,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                            is MusicUiState.Idle -> { /* Idle durumunda bir şey gösterme */ }
                        }
                    }

                    // Kullanıcı adı kartı
                    UsernameBadge(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 12.dp)
                    )

                    // Başlık
                    AnimatedVisibility(
                        visible = showElements,
                        enter = fadeIn(tween(800)) + slideInVertically(
                            initialOffsetY = { -50 },
                            animationSpec = tween(800)
                        ),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            // Modern Logo ve İkon
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                Color(0xFF1E40AF).copy(alpha = 0.05f),
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF60A5FA).copy(alpha = 0.8f),
                                                Color(0xFF3B82F6).copy(alpha = 0.3f),
                                                Color(0xFF1D4ED8).copy(alpha = 0.1f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // İç halka
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF60A5FA).copy(alpha = 0.3f),
                                                    Color(0xFF3B82F6).copy(alpha = 0.1f)
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Modern AI müzik ikonu
                                    Icon(
                                        imageVector = Icons.Filled.MusicNote,
                                        contentDescription = "Music",
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .scale(pulseScale)
                                    )

                                    // Animasyonlu halka
                                    Canvas(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .scale(pulseScale)
                                    ) {
                                        drawCircle(
                                            color = Color(0xFF60A5FA).copy(alpha = 0.2f),
                                            style = Stroke(
                                                width = 2f,
                                                pathEffect = PathEffect.dashPathEffect(
                                                    floatArrayOf(4f, 4f),
                                                    phase = 10f * waveAnimation
                                                )
                                            ),
                                            radius = size.minDimension / 2.2f
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Başlık yazısı - modern tasarım
                            Text(
                                text = "OCTA AI STUDIO",
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontFamily = orbitronFont,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 1.sp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFBAE6FD),
                                            Color(0xFF60A5FA),
                                            Color(0xFF3B82F6)
                                        )
                                    )
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Alt başlık
                            Text(
                                text = "Music Creator",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = orbitronFont,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 1.5.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Model bilgisi
                            Card(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .wrapContentWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = when(creationState) {
                                        MusicCreationState.IDLE -> "OCTA VIO V2"
                                        MusicCreationState.CREATING -> "Creating Music..."
                                        MusicCreationState.COMPLETED -> "Music Created!"
                                        MusicCreationState.ERROR -> "Process Failed"
                                    },
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = orbitronFont,
                                        color = Color(0xFF60A5FA)
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Remaining credits info - modern design
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (musicViewModel.remainingCredits.value >= 10)
                                        Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFF7F1D1D).copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (musicViewModel.remainingCredits.value >= 10)
                                        Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFF87171).copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (musicViewModel.remainingCredits.value >= 10)
                                            Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (musicViewModel.remainingCredits.value >= 10)
                                            Color(0xFF34D399) else Color(0xFFF87171),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Remaining Credits: ${musicViewModel.remainingCredits.value}",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = orbitronFont,
                                            fontWeight = FontWeight.Medium,
                                            color = if (musicViewModel.remainingCredits.value >= 10)
                                                Color(0xFF34D399) else Color(0xFFF87171)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Vocal Mode Button
                    AnimatedVisibility(
                        visible = showElements,
                        enter = fadeIn(tween(800, delayMillis = 100)) + expandHorizontally(
                            animationSpec = tween(800),
                            expandFrom = Alignment.Start
                        ),
                        exit = fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Vocal Mode",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontFamily = orbitronFont,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFE2E8F0)
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Info ikonu
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Information about vocal mode",
                                        tint = Color(0xFF60A5FA).copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Switch(
                                    checked = isVocalModeActive,
                                    onCheckedChange = { musicViewModel.toggleVocalMode() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF60A5FA),
                                        checkedTrackColor = Color(0xFF3B82F6).copy(alpha = 0.3f),
                                        checkedBorderColor = Color(0xFF60A5FA).copy(alpha = 0.7f),
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                        uncheckedBorderColor = Color(0xFF64748B).copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }

                    // Vocal recording button (only when vocal mode is active)
                    AnimatedVisibility(
                        visible = isVocalModeActive,
                        enter = fadeIn(tween(300)) + expandVertically(
                            animationSpec = tween(300),
                            expandFrom = Alignment.Top
                        ),
                        exit = fadeOut(tween(200)) + shrinkVertically(
                            animationSpec = tween(200),
                            shrinkTowards = Alignment.Top
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Text input for vocals
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFF3B82F6).copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Lyrics",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontFamily = orbitronFont,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF60A5FA)
                                        ),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // Keep lyrics content as a state
                                    var textFieldValue by remember {
                                        mutableStateOf(musicViewModel.vocalInput.value)
                                    }

                                    // TextField için bir focus requester
                                    val textFieldFocusRequester = remember { FocusRequester() }

                                    // Focus yöneticisi
                                    val focusManager = LocalFocusManager.current

                                    // Butonlardan çağıracağımız coroutineScope
                                    val localCoroutineScope = rememberCoroutineScope()

                                    // Değiştiğinde ViewModel'i güncelliyoruz
                                    LaunchedEffect(textFieldValue) {
                                        if (textFieldValue != musicViewModel.vocalInput.value) {
                                            musicViewModel.updateVocalInput(textFieldValue)
                                        }
                                    }

                                    // Update when vocal input changes (if changed from outside)
                                    LaunchedEffect(musicViewModel.vocalInput.value) {
                                        if (textFieldValue != musicViewModel.vocalInput.value) {
                                            // Update variable with new text content
                                            textFieldValue = musicViewModel.vocalInput.value
                                        }
                                    }

                                    OutlinedTextField(
                                        value = textFieldValue,
                                        onValueChange = { newValue ->
                                            textFieldValue = newValue
                                        },
                                        placeholder = {
                                            Text(
                                                "Write your lyrics here...",
                                                color = Color(0xFF64748B),
                                                fontFamily = orbitronFont,
                                                fontSize = 14.sp
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            cursorColor = Color(0xFF60A5FA),
                                            focusedBorderColor = Color(0xFF3B82F6),
                                            unfocusedBorderColor = Color(0xFF64748B).copy(alpha = 0.5f),
                                            focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.3f),
                                            unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.3f),
                                        ),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = orbitronFont,
                                            color = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .focusRequester(textFieldFocusRequester),
                                        maxLines = 8,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Show info message if lyrics have been entered
                                    AnimatedVisibility(
                                        visible = textFieldValue.isNotBlank(),
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF34D399),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Your lyrics will be added to the music",
                                                style = TextStyle(
                                                    fontSize = 12.sp,
                                                    fontFamily = orbitronFont,
                                                    color = Color(0xFF94A3B8),
                                                    fontStyle = FontStyle.Italic
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Başlık giriş alanı
                    AnimatedVisibility(
                        visible = showElements,
                        enter = fadeIn(tween(800, delayMillis = 200)) + expandVertically(
                            animationSpec = tween(800),
                            expandFrom = Alignment.Top
                        ),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF3B82F6).copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Etiket
                                Text(
                                    text = "Music title",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontFamily = orbitronFont,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF60A5FA)
                                    ),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Başlık girişi
                                val titleFocusRequester = remember { FocusRequester() }
                                val titleInteractionSource = remember { MutableInteractionSource() }
                                val isTitleFocused by titleInteractionSource.collectIsFocusedAsState()

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { musicViewModel.updateTitle(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(titleFocusRequester),
                                    placeholder = {
                                        Text(
                                            "Write a title for your music (optional)",
                                            color = Color(0xFF64748B),
                                            fontFamily = orbitronFont,
                                            fontSize = 14.sp
                                        )
                                    },
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontFamily = orbitronFont,
                                        fontSize = 16.sp
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        cursorColor = Color(0xFF60A5FA),
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedBorderColor = Color(0xFF64748B).copy(alpha = 0.5f),
                                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.3f),
                                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.3f),
                                    ),
                                    interactionSource = titleInteractionSource,
                                    enabled = creationState != MusicCreationState.CREATING,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { promptFocusRequester.requestFocus() }),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Prompt giriş alanı
                    AnimatedVisibility(
                        visible = showElements,
                        enter = fadeIn(tween(800, delayMillis = 200)) + expandVertically(
                            animationSpec = tween(800),
                            expandFrom = Alignment.Top
                        ),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF3B82F6).copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Etiket
                                Text(
                                    text = "Music recipe",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontFamily = orbitronFont,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF60A5FA)
                                    ),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Prompt girişi
                                OutlinedTextField(
                                    value = prompt,
                                    onValueChange = { musicViewModel.updatePrompt(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .focusRequester(promptFocusRequester),
                                    placeholder = {
                                        Text(
                                            "Describe the music you want to create...\nExample: 'An energetic song with electronic rhythms and a catchy melody'",
                                            color = Color(0xFF64748B),
                                            fontFamily = orbitronFont,
                                            fontSize = 14.sp
                                        )
                                    },
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontFamily = orbitronFont,
                                        fontSize = 16.sp
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        cursorColor = Color(0xFF60A5FA),
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedBorderColor = Color(0xFF64748B).copy(alpha = 0.5f),
                                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.3f),
                                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.3f),
                                    ),
                                    interactionSource = promptInteractionSource,
                                    enabled = creationState != MusicCreationState.CREATING,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Music genre selection
                    AnimatedVisibility(
                        visible = showGenres,
                        enter = fadeIn(tween(800, delayMillis = 400)) + expandVertically(
                            animationSpec = tween(800),
                            expandFrom = Alignment.Top
                        ),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // Etiket
                            Text(
                                text = "Select music style",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = orbitronFont,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA)
                                ),
                                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                            )

                            // Tür seçimi - Daha modern FlowRow benzeri yapı
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(musicGenres) { genre ->
                                    val isSelected = genre == selectedGenre
                                    // Her tür için farklı bir modern renk
                                    val genreIndex = musicGenres.indexOf(genre)
                                    val genreColor = if (genreIndex != -1 && genreIndex < neonGenreColors.size) {
                                        neonGenreColors[genreIndex]
                                    } else {
                                        Color(0xFF3B82F6) // Varsayılan mavi
                                    }

                                    Card(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .clickable(enabled = creationState != MusicCreationState.CREATING) {
                                                musicViewModel.updateSelectedGenre(genre)
                                                musicViewModel.setRandomPromptForGenre(genre)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected)
                                                genreColor.copy(alpha = 0.15f)
                                            else
                                                Color(0xFF1E293B).copy(alpha = 0.7f)
                                        ),
                                        shape = RoundedCornerShape(22.dp),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) genreColor.copy(alpha = 0.8f) else Color(0xFF64748B).copy(alpha = 0.3f)
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = if (isSelected) 4.dp else 0.dp
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                                .fillMaxHeight()
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(genreColor, CircleShape)
                                                        .padding(end = 4.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            Text(
                                                text = genre,
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontFamily = orbitronFont,
                                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                                    color = if (isSelected) genreColor else Color(0xFFE2E8F0)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Seçilen türe göre modern prompt önerisi göster
                            AnimatedVisibility(
                                visible = selectedGenre.isNotEmpty(),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                val genreIndex = musicGenres.indexOf(selectedGenre)
                                val genreColor = if (genreIndex != -1 && genreIndex < neonGenreColors.size) {
                                    neonGenreColors[genreIndex]
                                } else {
                                    Color(0xFF3B82F6) // Varsayılan mavi
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, genreColor.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Info,
                                                contentDescription = null,
                                                tint = genreColor.copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Suggestion for music in selected style:",
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontFamily = orbitronFont,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "${prompt.takeIf { it.isNotBlank() } ?: "Click to see suggested prompt in this category"}",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontFamily = orbitronFont,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    musicViewModel.setRandomPromptForGenre(selectedGenre)
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = genreColor
                                                ),
                                                border = BorderStroke(1.dp, genreColor.copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "New suggestion",
                                                    fontSize = 14.sp,
                                                    fontFamily = orbitronFont
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Vokal Modu Bölümü - Modern Tasarım
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(600, delayMillis = 300)) + expandVertically(
                            animationSpec = tween(600),
                            expandFrom = Alignment.Top
                        ),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA).copy(alpha = 0.5f),
                                        Color(0xFF764BA2).copy(alpha = 0.5f)
                                    )
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Vokal Modu Başlık ve Switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = if (isVocalModeActive) Color(0xFF667EEA) else Color(0xFF64748B),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .scale(if (isVocalModeActive) pulseScale else 1f)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Vokal Modu",
                                                style = TextStyle(
                                                    fontSize = 18.sp,
                                                    fontFamily = orbitronFont,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                            Text(
                                                text = if (isVocalModeActive) "Şarkı sözleri ekleyin" else "Enstrümantal müzik",
                                                style = TextStyle(
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            )
                                        }
                                    }
                                    
                                    Switch(
                                        checked = isVocalModeActive,
                                        onCheckedChange = { musicViewModel.toggleVocalMode() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF667EEA),
                                            uncheckedThumbColor = Color(0xFF94A3B8),
                                            uncheckedTrackColor = Color(0xFF334155)
                                        )
                                    )
                                }

                                // Vokal içerik alanı
                                AnimatedVisibility(
                                    visible = isVocalModeActive,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                    ) {
                                        // Şarkı sözleri giriş alanı
                                        OutlinedTextField(
                                            value = vocalInput,
                                            onValueChange = { musicViewModel.updateVocalInput(it) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .background(
                                                    Color(0xFF0F172A).copy(alpha = 0.3f),
                                                    RoundedCornerShape(12.dp)
                                                ),
                                            placeholder = {
                                                Text(
                                                    "Şarkı sözlerinizi buraya yazın...",
                                                    color = Color(0xFF64748B),
                                                    fontSize = 14.sp
                                                )
                                            },
                                            textStyle = TextStyle(
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontFamily = orbitronFont
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                cursorColor = Color(0xFF667EEA),
                                                focusedBorderColor = Color(0xFF667EEA),
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            maxLines = 6
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Şarkı yapısı etiketleri başlık
                                        Text(
                                            text = "Şarkı Yapısı Etiketleri:",
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontFamily = orbitronFont,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF94A3B8)
                                            ),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        // Modern etiket butonları - FlowRow benzeri düzen
                                        val structureTags = listOf(
                                            "[INTRO]" to Color(0xFF3B82F6),
                                            "[VERSE]" to Color(0xFF8B5CF6),
                                            "[PRE-CHORUS]" to Color(0xFFEC4899),
                                            "[CHORUS]" to Color(0xFFF59E0B),
                                            "[BRIDGE]" to Color(0xFF10B981),
                                            "[OUTRO]" to Color(0xFFEF4444)
                                        )
                                        
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // İlk satır
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                structureTags.take(3).forEach { (tag, color) ->
                                                    OutlinedButton(
                                                        onClick = {
                                                            musicViewModel.updateVocalInput(
                                                                if (vocalInput.isEmpty()) tag
                                                                else "$vocalInput\n\n$tag\n"
                                                            )
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
                                                            color = color.copy(alpha = 0.5f)
                                                        ),
                                                        shape = RoundedCornerShape(18.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = tag.replace("[", "").replace("]", ""),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = orbitronFont
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            // İkinci satır
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                structureTags.drop(3).forEach { (tag, color) ->
                                                    OutlinedButton(
                                                        onClick = {
                                                            musicViewModel.updateVocalInput(
                                                                if (vocalInput.isEmpty()) tag
                                                                else "$vocalInput\n\n$tag\n"
                                                            )
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
                                                            color = color.copy(alpha = 0.5f)
                                                        ),
                                                        shape = RoundedCornerShape(18.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = tag.replace("[", "").replace("]", ""),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = orbitronFont
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Vokal kayıt butonu (opsiyonel)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        OutlinedButton(
                                            onClick = { /* Coming soon */ },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            enabled = false,
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFF64748B),
                                                containerColor = Color(0xFF334155).copy(alpha = 0.1f),
                                                disabledContentColor = Color(0xFF64748B).copy(alpha = 0.6f),
                                                disabledContainerColor = Color(0xFF334155).copy(alpha = 0.05f)
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = Color(0xFF334155).copy(alpha = 0.3f)
                                            ),
                                            shape = RoundedCornerShape(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color(0xFF64748B).copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Vokal Kaydet (Yakında)",
                                                fontSize = 14.sp,
                                                fontFamily = orbitronFont,
                                                color = Color(0xFF64748B).copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Create music button - optimized
                    AnimatedVisibility(
                        visible = showButton,
                        enter = fadeIn(tween(800, delayMillis = 600)) + expandVertically(
                            animationSpec = tween(800),
                            expandFrom = Alignment.Top
                        ),
                        exit = fadeOut()
                    ) {
                        val buttonEnabled = prompt.isNotBlank() && creationState != MusicCreationState.CREATING &&
                                (!isVocalModeActive || (isVocalModeActive && vocalInput.isNotBlank()))

                        // Modern tasarımlı buton
                        Button(
                            onClick = {
                                if (creationState != MusicCreationState.CREATING) {
                                    // Focus'u kaybettir
                                    focusManager.clearFocus()

                                    // Start music creation process
                                    musicViewModel.generateMusic()

                                    // Immediately switch to loading state
                                    creationState = MusicCreationState.CREATING

                                    // Call the callback
                                    onMusicCreated(prompt, selectedGenre)
                                }
                            },
                            enabled = buttonEnabled,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6),
                                disabledContainerColor = Color(0xFF64748B).copy(alpha = 0.3f)
                            ),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp,
                                disabledElevation = 0.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(vertical = 24.dp)
                                .height(56.dp)
                        ) {
                            // Icon ve metin layout
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val buttonText = when (creationState) {
                                    MusicCreationState.CREATING -> "Creating Music..."
                                    MusicCreationState.COMPLETED -> "Music Created!"
                                    else -> if (isVocalModeActive && (vocalInput.isNotBlank() || musicViewModel.recordedVocalPath.value != null))
                                        "Create Music with Vocals"
                                    else
                                        "Create Music"
                                }

                                // Dinamik ikon (durum göre değişen)
                                val icon = when(creationState) {
                                    MusicCreationState.CREATING -> Icons.Default.Refresh
                                    MusicCreationState.COMPLETED -> Icons.Default.CheckCircle
                                    else -> Icons.Default.MusicNote
                                }

                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = buttonText,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = orbitronFont,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    } // Outer Box end
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun OctaAIMusicCreatorPreview() {
    // Gerçek bir NavController kullanmak yerine null kullanıyoruz (sadece önizleme için)
    OctaAIMusicCreator(
        navController = androidx.navigation.NavHostController(LocalContext.current)
    )
}
}