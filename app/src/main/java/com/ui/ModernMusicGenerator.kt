package com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.scale
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.musicApi.MusicViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicApi.MusicUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Shadow
import kotlin.math.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode

// Color scheme from code.html
object ModernMusicColors {
    val Primary = Color(0xFFBC06F9)
    val Secondary = Color(0xFF40214A)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFBD8ECC)
    val BackgroundPrimary = Color(0xFF1E1023)
    val BackgroundSecondary = Color(0xFF2E1835)
}

// Data classes for selections
data class GenreSelection(val name: String, var isSelected: Boolean = false)
data class MoodSelection(val name: String, var isSelected: Boolean = false)
data class InstrumentSelection(val name: String, var isSelected: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernMusicGenerator(
    navController: NavController,
    onMusicCreated: (String, String) -> Unit = { _, _ -> },
    onNavigateToProfile: () -> Unit = {}
) {
    val musicViewModel: MusicViewModel = viewModel()
    val uiState by musicViewModel.uiState.collectAsState()
    val currentProgress by musicViewModel.currentProgress.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Selection states
    val genres = remember {
        mutableStateListOf(
            GenreSelection("Electronic"),
            GenreSelection("Hip Hop"),
            GenreSelection("Classical"),
            GenreSelection("Pop"),
            GenreSelection("Rock"),
            GenreSelection("Jazz"),
            GenreSelection("R&B"),
            GenreSelection("Lo-Fi")
        )
    }
    
    val moods = remember {
        mutableStateListOf(
            MoodSelection("Energetic"),
            MoodSelection("Relaxing"),
            MoodSelection("Melancholic"),
            MoodSelection("Happy"),
            MoodSelection("Dark"),
            MoodSelection("Uplifting")
        )
    }
    
    val instruments = remember {
        mutableStateListOf(
            InstrumentSelection("Synth"),
            InstrumentSelection("Drums"),
            InstrumentSelection("Piano"),
            InstrumentSelection("Guitar"),
            InstrumentSelection("Bass"),
            InstrumentSelection("Strings")
        )
    }
    
    var isGenerating by remember { mutableStateOf(false) }
    var customPrompt by remember { mutableStateOf("") }
    var songTitle by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("OctaInitium") }
    var isVocalModeActive by remember { mutableStateOf(false) }
    var vocalInput by remember { mutableStateOf("") }
    
    // Outer Box with gradient background like library page
    Box(
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        // Gradient background - same as library page
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
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    onProfileClick = onNavigateToProfile
                )
            },
            bottomBar = {
                com.ui.components.BottomNavBar(
                    navController = navController,
                    currentRoute = "music_creator"
                )
            }
        ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Section
                Spacer(modifier = Modifier.height(20.dp))
                
                // Animated gradient for title when generating
                if (isGenerating) {
                    val infiniteTransition = rememberInfiniteTransition(label = "title_gradient")
                    val gradientOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "gradient_offset"
                    )
                    
                    Text(
                        text = "Create Your Sound",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFBC06F9), // Electric Purple  
                                    Color(0xFFFF00DE), // Hot Pink
                                    Color(0xFF9C27B0), // Medium Purple
                                    Color(0xFFE91E63), // Pink
                                    Color(0xFFBC06F9)  // Back to Electric Purple
                                ),
                                start = Offset(gradientOffset * 1000f, 0f),
                                end = Offset((gradientOffset * 1000f) + 500f, 0f)
                            )
                        )
                    )
                } else {
                    Text(
                        text = "Create Your Sound",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernMusicColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "Tap the black hole to start generating your unique music.",
                    fontSize = 16.sp,
                    color = ModernMusicColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )
                
                // Black Hole Animation with Progress
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BlackHoleGenerator(
                            isGenerating = isGenerating || uiState is MusicUiState.Loading,
                            progress = if (uiState is MusicUiState.Loading) currentProgress else 0f,
                            onClick = {
                                if (!isGenerating && uiState !is MusicUiState.Loading) {
                                    generateMusic(
                                        songTitle = songTitle,
                                        customPrompt = customPrompt,
                                        genres = genres.filter { it.isSelected },
                                        moods = moods.filter { it.isSelected },
                                        instruments = instruments.filter { it.isSelected },
                                        isVocalMode = isVocalModeActive,
                                        vocalLyrics = vocalInput,
                                        musicViewModel = musicViewModel,
                                        onStartGeneration = { isGenerating = true },
                                        onEndGeneration = { isGenerating = false }
                                    )
                                }
                            }
                        )
                        
                        // Progress indicator below black hole
                        when (val state = uiState) {
                            is MusicUiState.Loading -> {
                                Spacer(modifier = Modifier.height(24.dp))
                                ModernProgressIndicator(
                                    progress = currentProgress,
                                    message = state.message
                                )
                            }
                            is MusicUiState.Success -> {
                                Spacer(modifier = Modifier.height(24.dp))
                                SuccessMessage(message = state.message)
                            }
                            is MusicUiState.Error -> {
                                Spacer(modifier = Modifier.height(24.dp))
                                ErrorMessage(message = state.message)
                            }
                            else -> {}
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Song Title Input
                TitleInputField(
                    value = songTitle,
                    onValueChange = { songTitle = it },
                    isEnabled = !isGenerating && uiState !is MusicUiState.Loading
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Model Selection
                ModelSelectionSection(
                    selectedModel = selectedModel,
                    onModelSelect = { model -> selectedModel = model },
                    isEnabled = !isGenerating && uiState !is MusicUiState.Loading
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Custom Prompt Input
                PromptInputField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    isEnabled = !isGenerating && uiState !is MusicUiState.Loading
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Vokal Modu Bölümü
                VocalModeSection(
                    isVocalModeActive = isVocalModeActive,
                    vocalInput = vocalInput,
                    onVocalModeToggle = { isVocalModeActive = !isVocalModeActive },
                    onVocalInputChange = { vocalInput = it },
                    isEnabled = !isGenerating && uiState !is MusicUiState.Loading
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Genre Selection
                SelectionSection(
                    title = "Genre",
                    items = genres,
                    onItemClick = { index ->
                        genres[index] = genres[index].copy(isSelected = !genres[index].isSelected)
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Mood Selection
                SelectionSection(
                    title = "Mood",
                    items = moods,
                    onItemClick = { index ->
                        moods[index] = moods[index].copy(isSelected = !moods[index].isSelected)
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Instruments Selection
                SelectionSection(
                    title = "Instruments",
                    items = instruments,
                    onItemClick = { index ->
                        instruments[index] = instruments[index].copy(isSelected = !instruments[index].isSelected)
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Generate Button
                GenerateMusicButton(
                    isGenerating = isGenerating || uiState is MusicUiState.Loading,
                    onClick = {
                        if (!isGenerating && uiState !is MusicUiState.Loading) {
                            generateMusic(
                                songTitle = songTitle,
                                customPrompt = customPrompt,
                                genres = genres.filter { it.isSelected },
                                moods = moods.filter { it.isSelected },
                                instruments = instruments.filter { it.isSelected },
                                isVocalMode = isVocalModeActive,
                                vocalLyrics = vocalInput,
                                musicViewModel = musicViewModel,
                                onStartGeneration = { isGenerating = true },
                                onEndGeneration = { isGenerating = false }
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    } // End of outer Box with gradient background
}

@Composable
private fun ModernTopBar(
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0x801A0033), // Semi-transparent dark purple like other pages
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Music Generator",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ModernMusicColors.TextPrimary
            )
            
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = ModernMusicColors.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun BlackHoleGenerator(
    isGenerating: Boolean,
    progress: Float = 0f,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blackHole")
    
    // Pulse animation - daha belirgin
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Rotation animation for spiral
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Glow intensity animation
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .size(320.dp)
            .scale(pulseScale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Black hole with purple theme
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2
            
            // Outer purple glow layers
            for (i in 8 downTo 1) {
                val radius = maxRadius * (0.4f + (i * 0.08f))
                val alpha = (0.15f * (9 - i) / 8f) * glowIntensity
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFBC06F9).copy(alpha = alpha * 0.8f),
                            Color(0xFF9D4EDD).copy(alpha = alpha * 0.5f),
                            Color(0xFF6C63FF).copy(alpha = alpha * 0.3f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }
            
            // Accretion disk - rotating rings
            for (ring in 3 downTo 1) {
                val ringRadius = maxRadius * (0.35f + ring * 0.08f)
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFBC06F9).copy(alpha = 0.3f * glowIntensity),
                            Color(0xFF6C63FF).copy(alpha = 0.2f * glowIntensity),
                            Color(0xFFBC06F9).copy(alpha = 0.3f * glowIntensity),
                            Color(0xFF9D4EDD).copy(alpha = 0.25f * glowIntensity),
                            Color(0xFFBC06F9).copy(alpha = 0.3f * glowIntensity)
                        ),
                        center = center
                    ),
                    radius = ringRadius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = (2f + ring * 0.5f).dp.toPx()
                    )
                )
            }
            
            // Event horizon gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black,
                        Color.Black.copy(alpha = 0.95f),
                        Color(0xFF1E1023).copy(alpha = 0.8f),
                        Color(0xFF40214A).copy(alpha = 0.6f),
                        Color(0xFF6C63FF).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 0.5f
                ),
                radius = maxRadius * 0.5f,
                center = center
            )
            
            // Inner black hole
            drawCircle(
                color = Color.Black,
                radius = maxRadius * 0.25f,
                center = center
            )
            
            // Gravitational lensing effect - subtle inner glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFBC06F9).copy(alpha = 0.1f * glowIntensity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 0.22f
                ),
                radius = maxRadius * 0.22f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }
        
        // Rotating spiral overlay when generating
        if (isGenerating) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.minDimension / 2
                
                for (i in 0..5) {
                    rotate(degrees = i * 60f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFBC06F9).copy(alpha = 0.4f),
                                    Color(0xFF6C63FF).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 45f,
                            useCenter = true,
                            topLeft = Offset(center.x - maxRadius * 0.7f, center.y - maxRadius * 0.7f),
                            size = androidx.compose.ui.geometry.Size(maxRadius * 1.4f, maxRadius * 1.4f)
                        )
                    }
                }
            }
        }
        
        // Center play button
        if (!isGenerating) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF40214A).copy(alpha = 0.8f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFBC06F9).copy(alpha = 0.6f),
                                Color(0xFF6C63FF).copy(alpha = 0.4f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Generate",
                    tint = Color(0xFFBC06F9),
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            // Loading spinner when generating
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = Color(0xFFBC06F9),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
private fun <T> SelectionSection(
    title: String,
    items: List<T>,
    onItemClick: (Int) -> Unit
) where T : Any {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ModernMusicColors.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items.size) { index ->
                val item = items[index]
                val isSelected = when (item) {
                    is GenreSelection -> item.isSelected
                    is MoodSelection -> item.isSelected
                    is InstrumentSelection -> item.isSelected
                    else -> false
                }
                val name = when (item) {
                    is GenreSelection -> item.name
                    is MoodSelection -> item.name
                    is InstrumentSelection -> item.name
                    else -> ""
                }
                
                SelectionChip(
                    text = name,
                    isSelected = isSelected,
                    onClick = { onItemClick(index) }
                )
            }
        }
    }
}

@Composable
private fun SelectionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) ModernMusicColors.Primary else ModernMusicColors.Secondary,
        animationSpec = tween(300),
        label = "chipColor"
    )
    
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ModernMusicColors.TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TitleInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color(0xFF00E5FF), // Cyan/Aqua rengi
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Song Title",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ModernMusicColors.TextPrimary
            )
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 100) onValueChange(it) },
            enabled = isEnabled,
            placeholder = {
                Text(
                    text = "Enter your song title...",
                    color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = ModernMusicColors.TextPrimary,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFF00E5FF).copy(alpha = 0.05f),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF00E5FF).copy(alpha = 0.3f),
                disabledBorderColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                cursorColor = Color(0xFF00E5FF)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { /* Focus next field */ }
            )
        )
        
        // Character counter
        Text(
            text = "${value.length}/100",
            fontSize = 12.sp,
            color = if (value.length > 100) Color.Red else Color(0xFF00E5FF).copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromptInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Describe Your Music",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ModernMusicColors.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEnabled,
            placeholder = {
                Text(
                    text = "e.g., Peaceful piano melody with ocean sounds...",
                    color = ModernMusicColors.TextSecondary.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = ModernMusicColors.TextPrimary,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = ModernMusicColors.Secondary.copy(alpha = 0.3f),
                focusedBorderColor = ModernMusicColors.Primary,
                unfocusedBorderColor = ModernMusicColors.Secondary,
                disabledBorderColor = ModernMusicColors.Secondary.copy(alpha = 0.5f),
                cursorColor = ModernMusicColors.Primary
            ),
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { /* Handle done action if needed */ }
            )
        )
        
        // Character counter
        Text(
            text = "${value.length}/500",
            fontSize = 12.sp,
            color = if (value.length > 500) Color.Red else ModernMusicColors.TextSecondary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VocalModeSection(
    isVocalModeActive: Boolean,
    vocalInput: String,
    onVocalModeToggle: () -> Unit,
    onVocalInputChange: (String) -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernMusicColors.Secondary.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    ModernMusicColors.Primary.copy(alpha = 0.5f),
                    Color(0xFF764BA2).copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        tint = if (isVocalModeActive) ModernMusicColors.Primary else ModernMusicColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Vocal Mode",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernMusicColors.TextPrimary
                        )
                        Text(
                            text = if (isVocalModeActive) "Add lyrics to your music" else "Instrumental music",
                            fontSize = 12.sp,
                            color = ModernMusicColors.TextSecondary
                        )
                    }
                }
                
                Switch(
                    checked = isVocalModeActive,
                    onCheckedChange = { onVocalModeToggle() },
                    enabled = isEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ModernMusicColors.Primary,
                        uncheckedThumbColor = ModernMusicColors.TextSecondary,
                        uncheckedTrackColor = ModernMusicColors.Secondary
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
                        onValueChange = onVocalInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = {
                            Text(
                                "Enter your lyrics here...",
                                color = ModernMusicColors.TextSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = ModernMusicColors.TextPrimary,
                            fontSize = 14.sp
                        ),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            cursorColor = ModernMusicColors.Primary,
                            focusedBorderColor = ModernMusicColors.Primary,
                            unfocusedBorderColor = ModernMusicColors.Secondary,
                            containerColor = ModernMusicColors.BackgroundPrimary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 6,
                        enabled = isEnabled
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Şarkı yapısı etiketleri
                    Text(
                        text = "Song Structure Tags:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ModernMusicColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Etiket butonları
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
                                        onVocalInputChange(
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
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    enabled = isEnabled
                                ) {
                                    Text(
                                        text = tag.replace("[", "").replace("]", ""),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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
                                        onVocalInputChange(
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
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    enabled = isEnabled
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
                    
                    // Vokal kayıt butonu (Coming Soon)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { /* Coming soon */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        enabled = false,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ModernMusicColors.TextSecondary,
                            containerColor = ModernMusicColors.Secondary.copy(alpha = 0.1f),
                            disabledContentColor = ModernMusicColors.TextSecondary.copy(alpha = 0.4f),
                            disabledContainerColor = ModernMusicColors.Secondary.copy(alpha = 0.05f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = ModernMusicColors.Secondary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = ModernMusicColors.TextSecondary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Record Vocals (Coming Soon)",
                            fontSize = 14.sp,
                            color = ModernMusicColors.TextSecondary.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerateMusicButton(
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ModernMusicColors.Primary,
            disabledContainerColor = ModernMusicColors.Primary.copy(alpha = 0.5f)
        ),
        enabled = !isGenerating
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = ModernMusicColors.TextPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Generate Music",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ModernMusicColors.TextPrimary
            )
        }
    }
}

@Composable
private fun ModelSelectionSection(
    selectedModel: String,
    onModelSelect: (String) -> Unit,
    isEnabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "AI Model",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ModernMusicColors.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // OctaInitium Model - Default
            ModelCard(
                modifier = Modifier.weight(1f),
                modelName = "OctaInitium",
                modelDescription = "Fast & Reliable",
                isSelected = selectedModel == "OctaInitium",
                isEnabled = isEnabled,
                isAvailable = true,
                badge = "DEFAULT",
                onClick = { onModelSelect("OctaInitium") }
            )
            
            // OctaSupremus Model - Coming Soon
            ModelCard(
                modifier = Modifier.weight(1f),
                modelName = "OctaSupremus",
                modelDescription = "Ultra Quality",
                isSelected = false,
                isEnabled = false,
                isAvailable = false,
                badge = "COMING SOON",
                onClick = { /* Disabled */ }
            )
        }
    }
}

@Composable
private fun ModelCard(
    modifier: Modifier = Modifier,
    modelName: String,
    modelDescription: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    isAvailable: Boolean,
    badge: String,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "modelScale"
    )
    
    Card(
        modifier = modifier
            .height(100.dp)
            .scale(animatedScale)
            .then(
                if (isEnabled && isAvailable) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                ModernMusicColors.Primary.copy(alpha = 0.2f)
            } else if (!isAvailable) {
                ModernMusicColors.Secondary.copy(alpha = 0.1f)
            } else {
                ModernMusicColors.Secondary.copy(alpha = 0.3f)
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                ModernMusicColors.Primary
            } else if (!isAvailable) {
                ModernMusicColors.Secondary.copy(alpha = 0.3f)
            } else {
                ModernMusicColors.Secondary.copy(alpha = 0.5f)
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Model Icon
                Icon(
                    imageVector = if (modelName == "OctaInitium") Icons.Default.Speed else Icons.Default.Stars,
                    contentDescription = modelName,
                    tint = if (isAvailable) {
                        if (isSelected) ModernMusicColors.Primary else ModernMusicColors.TextSecondary
                    } else {
                        ModernMusicColors.TextSecondary.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Model Name
                Text(
                    text = modelName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAvailable) {
                        if (isSelected) ModernMusicColors.TextPrimary else ModernMusicColors.TextPrimary.copy(alpha = 0.8f)
                    } else {
                        ModernMusicColors.TextSecondary.copy(alpha = 0.5f)
                    }
                )
                
                // Model Description
                Text(
                    text = modelDescription,
                    fontSize = 11.sp,
                    color = if (isAvailable) {
                        ModernMusicColors.TextSecondary
                    } else {
                        ModernMusicColors.TextSecondary.copy(alpha = 0.4f)
                    }
                )
            }
            
            // Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (badge == "DEFAULT") {
                            ModernMusicColors.Primary.copy(alpha = 0.8f)
                        } else {
                            ModernMusicColors.Secondary.copy(alpha = 0.6f)
                        }
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
            
            // Lock overlay for unavailable model
            if (!isAvailable) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = ModernMusicColors.TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessMessage(
    message: String
) {
    // Subtle animated colors
    val infiniteTransition = rememberInfiniteTransition(label = "success")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF00E676).copy(alpha = 0.9f),
        targetValue = Color(0xFF00E5FF).copy(alpha = 0.9f),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "successColor"
    )
    
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "successAlpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Glassmorphic checkmark container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
        ) {
            // Glassmorphic background circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                animatedColor.copy(alpha = animatedAlpha),
                                animatedColor.copy(alpha = animatedAlpha * 0.5f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Custom checkmark using Canvas
            Canvas(
                modifier = Modifier.size(50.dp)
            ) {
                val strokeWidth = 4.dp.toPx()
                val checkPath = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.5f)
                    lineTo(size.width * 0.4f, size.height * 0.7f)
                    lineTo(size.width * 0.8f, size.height * 0.3f)
                }
                
                // Glow effect
                drawPath(
                    path = checkPath,
                    color = animatedColor,
                    style = Stroke(
                        width = strokeWidth * 2,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    alpha = 0.3f
                )
                
                // Main checkmark
                drawPath(
                    path = checkPath,
                    color = Color.White,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Modern message text
        Text(
            text = "Music Created Successfully",
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle with file status
        Text(
            text = "Your track has been added to library",
            fontSize = 14.sp,
            fontWeight = FontWeight.Thin,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        )
        
        // Animated progress indicator
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            animatedColor.copy(alpha = animatedAlpha),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Error icon
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = Color(0xFFFF5252),
            modifier = Modifier.size(60.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error message
        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFFF5252),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun ModernProgressIndicator(
    progress: Float,
    message: String = ""
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "progress"
    )
    
    // Renk animasyonu için infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "color")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFBC06F9), // Mor
        targetValue = Color(0xFF00E5FF), // Cyan
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "messageColor"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Glow effect background
            Text(
                text = "${animatedProgress.toInt()}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Thin,
                color = animatedColor,
                modifier = Modifier
                    .blur(15.dp)
                    .alpha(0.4f)
            )
            
            // Main percentage display
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${animatedProgress.toInt()}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = animatedColor.copy(alpha = 0.6f),
                            offset = Offset(0f, 0f),
                            blurRadius = 8f
                        )
                    )
                )
                Text(
                    text = "%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Thin,
                    color = animatedColor,
                    modifier = Modifier.padding(top = 8.dp, start = 2.dp)
                )
            }
        }
        
        // Animated message text
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Message with animated color
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = animatedColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .alpha(0.9f),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = animatedColor.copy(alpha = 0.3f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

// LoadingOverlay artık kullanılmıyor - kaldırıldı
/*
@Composable
private fun LoadingOverlay(
    progress: Float,
    message: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingBlackHole")
    
    // Disk rotation speed based on progress
    val baseSpeed = 8000 - (progress * 60).toInt() // Speed increases as progress increases
    val diskRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(baseSpeed.coerceIn(2000, 8000), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diskRotation"
    )
    
    // Pulsing effect
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Particle pull effect
    val particlePull by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particlePull"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.95f),
                        Color(0xFF1E1023).copy(alpha = 0.9f),
                        Color.Black.copy(alpha = 0.85f)
                    )
                )
            )
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(350.dp),
            contentAlignment = Alignment.Center
        ) {
            // Black hole with accretion disk
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.minDimension / 2
                
                // Background glow
                for (i in 5 downTo 1) {
                    val radius = maxRadius * (0.5f + i * 0.1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFBC06F9).copy(alpha = 0.05f * i),
                                Color(0xFF6C63FF).copy(alpha = 0.03f * i),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
                
                // Event horizon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black,
                            Color(0xFF1E1023).copy(alpha = 0.8f),
                            Color(0xFF40214A).copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = maxRadius * 0.3f
                    ),
                    radius = maxRadius * 0.3f,
                    center = center
                )
                
                // Black hole center
                drawCircle(
                    color = Color.Black,
                    radius = maxRadius * 0.15f,
                    center = center
                )
            }
            
            // Rotating accretion disk
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { 
                        rotationZ = diskRotation * (1f + progress / 100f) // Faster rotation as progress increases
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.minDimension / 2
                
                // Disk rings with progress-based intensity
                for (ring in 1..8) {
                    val ringRadius = maxRadius * (0.25f + ring * 0.08f)
                    val alpha = (progress / 100f) * 0.4f + 0.1f
                    
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFFBC06F9).copy(alpha = alpha),
                                Color(0xFF6C63FF).copy(alpha = alpha * 0.7f),
                                Color(0xFF9D4EDD).copy(alpha = alpha * 0.8f),
                                Color(0xFFBC06F9).copy(alpha = alpha),
                                Color(0xFF6C63FF).copy(alpha = alpha * 0.6f),
                                Color(0xFFBC06F9).copy(alpha = alpha)
                            ),
                            center = center
                        ),
                        radius = ringRadius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = (2f + (progress / 50f)).dp.toPx()
                        )
                    )
                }
                
                // Spiral arms being pulled in
                for (i in 0..5) {
                    rotate(degrees = i * 60f) {
                        val spiralPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(center.x, center.y)
                            for (t in 0..20) {
                                val angle = (t * 0.3f)
                                val radius = (maxRadius * 0.2f) + (t * 10f * (1f - particlePull))
                                val x = center.x + radius * kotlin.math.cos(angle)
                                val y = center.y + radius * kotlin.math.sin(angle)
                                lineTo(x, y)
                            }
                        }
                        
                        drawPath(
                            path = spiralPath,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFBC06F9).copy(alpha = 0.6f * (progress / 100f)),
                                    Color.Transparent
                                )
                            ),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
            
            // Particles being sucked in
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.minDimension / 2
                
                for (i in 0..15) {
                    val angle = (i * 24f) + diskRotation
                    val distance = maxRadius * 0.7f * particlePull
                    val x = center.x + distance * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
                    val y = center.y + distance * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                    
                    drawCircle(
                        color = Color(0xFFBC06F9).copy(alpha = 0.8f * particlePull),
                        radius = (3f + particlePull * 2f).dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
            
            // Modern percentage display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                // Percentage with glow effect
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Glow background
                    Text(
                        text = "${progress.toInt()}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Thin,
                        color = Color(0xFFBC06F9),
                        modifier = Modifier
                            .blur(20.dp)
                            .alpha(0.5f)
                    )
                    
                    // Main percentage text
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${progress.toInt()}",
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Thin,
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFFBC06F9).copy(alpha = 0.8f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 10f
                                )
                            )
                        )
                        Text(
                            text = "%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Thin,
                            color = Color(0xFFBC06F9),
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Message with typewriter effect
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = ModernMusicColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .alpha(0.9f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
*/

// ModernBottomBar and BottomBarItem removed - using com.ui.components.BottomNavBar instead

// Helper function to generate music
private fun generateMusic(
    songTitle: String,
    customPrompt: String,
    genres: List<GenreSelection>,
    moods: List<MoodSelection>,
    instruments: List<InstrumentSelection>,
    isVocalMode: Boolean,
    vocalLyrics: String,
    musicViewModel: MusicViewModel,
    onStartGeneration: () -> Unit,
    onEndGeneration: () -> Unit
) {
    onStartGeneration()
    
    // Build prompt from custom prompt and selections
    val promptParts = mutableListOf<String>()
    
    // Add custom prompt if provided
    if (customPrompt.isNotBlank()) {
        promptParts.add(customPrompt.trim())
    }
    
    // Add genre selections
    if (genres.isNotEmpty()) {
        val genreText = genres.joinToString(", ") { it.name }
        if (customPrompt.isBlank()) {
            promptParts.add(genreText)
        } else {
            promptParts.add("in $genreText style")
        }
    }
    
    // Add mood selections
    if (moods.isNotEmpty()) {
        promptParts.add(moods.joinToString(", ") { it.name.lowercase() } + " mood")
    }
    
    // Add instrument selections
    if (instruments.isNotEmpty()) {
        promptParts.add("with " + instruments.joinToString(", ") { it.name.lowercase() })
    }
    
    // Add vocal mode information
    if (isVocalMode && vocalLyrics.isNotBlank()) {
        promptParts.add("with vocals")
    } else if (!isVocalMode) {
        promptParts.add("instrumental")
    }
    
    // Create final prompt
    val finalPrompt = if (promptParts.isNotEmpty()) {
        val basePrompt = promptParts.joinToString(", ")
        if (isVocalMode && vocalLyrics.isNotBlank()) {
            "$basePrompt\n\nLyrics:\n$vocalLyrics"
        } else {
            basePrompt
        }
    } else {
        "Create a unique piece of music"
    }
    
    // Get primary genre for API
    val genre = genres.firstOrNull()?.name ?: "Electronic"
    
    // Update ViewModel and generate music
    musicViewModel.updatePrompt(finalPrompt)
    musicViewModel.updateSelectedGenre(genre)
    
    // Update song title
    if (songTitle.isNotBlank()) {
        musicViewModel.updateTitle(songTitle)
    } else {
        // Eğer title girilmemişse otomatik bir title oluştur
        val autoTitle = "My ${genre} Song ${System.currentTimeMillis() % 10000}"
        musicViewModel.updateTitle(autoTitle)
    }
    
    // Update vocal mode in ViewModel
    if (isVocalMode != musicViewModel.isVocalModeActive.value) {
        musicViewModel.toggleVocalMode()
    }
    if (isVocalMode) {
        musicViewModel.updateVocalInput(vocalLyrics)
    }
    
    musicViewModel.generateMusic()
}