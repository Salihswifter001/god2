package com.musicApi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.*
import com.ui.components.MusicLoadingAnimation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale

/**
 * Music creation screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicCreatorScreen(
    navController: NavController,
    onNavigateToMyMusic: () -> Unit = {},
    viewModel: MusicViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val prompt by viewModel.prompt
    val selectedGenre by viewModel.selectedGenre
    val isVocalModeActive by viewModel.isVocalModeActive
    val vocalInput by viewModel.vocalInput
    val remainingCredits by viewModel.remainingCredits
    
    val coroutineScope = rememberCoroutineScope()
    
    // Animasyon değişkenleri
    val infiniteTransition = rememberInfiniteTransition(label = "transition")
    val hue by infiniteTransition.animateFloat(
        initialValue = 220f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hue"
    )
    
    // Ana ekran
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Arkaplan efekti
        ModernBlurredBackground()
        
        // Sidebar
        var currentRoute by remember { mutableStateOf("music") }
        
        val sidebarItems = listOf(
            SidebarItem(Icons.Filled.Person, "Profile", "profile"),
            SidebarItem(Icons.Filled.MusicNote, "Create Music", "music"),
            SidebarItem(Icons.Filled.LibraryMusic, "My Music", "my_music"),
            SidebarItem(Icons.Filled.CardMembership, "Membership", "membership"),
            SidebarItem(Icons.Filled.VideoLibrary, "OctaReels", "octareels"),
            SidebarItem(Icons.Filled.Info, "About", "about")
        )
        
        var isExpanded by remember { mutableStateOf(false) }
        
        NeonSidebar(
            items = sidebarItems,
            selectedRoute = "music",
            onItemClick = { route -> 
                when (route) {
                    "profile" -> navController.navigate("profile")
                    "my_music" -> navController.navigate("my_music")
                    "membership" -> navController.navigate("membership")
                    "octareels" -> navController.navigate("octareels")
                    "about" -> navController.navigate("about")
                }
            },
            isExpanded = isExpanded,
            onToggleExpand = { isExpanded = !isExpanded },
            modifier = Modifier.align(Alignment.CenterStart)
        )
        
        // İçeriğin padding değeri için animasyon
        val paddingTransition = updateTransition(
            targetState = isExpanded,
            label = "paddingTransition"
        )
        
        val startPadding by paddingTransition.animateDp(
            transitionSpec = {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            },
            label = "paddingAnimation"
        ) { expanded -> if (expanded) 200.dp else 65.dp }
        
        // Ana içerik
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = startPadding, end = 16.dp, top = 16.dp, bottom = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Başlık ve kredi göstergesi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = "Create Music",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White,
                                Color.hsv(hue, 0.7f, 0.9f)
                            )
                        )
                    )
                )
                
                // Kredi göstergesi
                Surface(
                    color = Color(0f, 0.05f, 0.1f, 0.6f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = "Credits",
                            tint = Color.hsv(hue, 0.7f, 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$remainingCredits Credits",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Loading state check
            if (uiState is MusicUiState.Loading) {
                // Loading animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MusicLoadingAnimation(
                        message = (uiState as MusicUiState.Loading).message,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            } else {            
                // Music creation area (not shown during loading)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    // Prompt input field
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { viewModel.updatePrompt(it) },
                        placeholder = { Text("What kind of music would you like to create?") },
                        label = { Text("Music description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.hsv(hue, 0.7f, 0.9f),
                            focusedLabelColor = Color.hsv(hue, 0.7f, 0.9f),
                            containerColor = Color(0f, 0.05f, 0.1f, 0.5f)
                        ),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        singleLine = false,
                        maxLines = 3
                    )
                    
                    // Genre selection
                    Text(
                        text = "Music Genre",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(vertical = 8.dp)
                    )
                    
                    val genres = listOf("Pop", "Rock", "Hip Hop", "Elektronik", "Classical", "Jazz", "R&B", "Country", "Folk", "Reggae")
                    
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(genres.size) { index ->
                            val genre = genres[index]
                            val isSelected = selectedGenre == genre
                            val genreHue = (hue + index * 20) % 360
                            
                            Surface(
                                onClick = {
                                    viewModel.updateSelectedGenre(genre)
                                    viewModel.setRandomPromptForGenre(genre)
                                },
                                color = if (isSelected) Color.hsv(genreHue, 0.7f, 0.3f) else Color(0f, 0.05f, 0.1f, 0.5f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color.hsv(genreHue, 0.7f, 0.9f) else Color(0.3f, 0.3f, 0.3f, 0.5f)
                                )
                            ) {
                                Text(
                                    text = genre,
                                    color = if (isSelected) Color.hsv(genreHue, 0.7f, 0.9f) else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    
                    // New vocal mode view - taken from a separate file
                    VocalModeView(
                        isVocalModeActive = isVocalModeActive,
                        vocalInput = vocalInput,
                        onVocalModeToggle = { viewModel.toggleVocalMode() },
                        onVocalInputChange = { viewModel.updateVocalInput(it) }
                    )
                    
                    // Create button
                    Button(
                        onClick = { viewModel.generateMusic() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.hsv(hue, 0.7f, 0.6f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = prompt.isNotBlank() && 
                                 (if (isVocalModeActive) vocalInput.isNotBlank() else true)
                    ) {
                        Text(
                            text = "Create Music",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Tips
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0f, 0.05f, 0.1f, 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Tips",
                                color = Color.hsv(hue, 0.7f, 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "• Use detailed descriptions. For example: 'Energetic electronic dance music with deep bass and spatial synthesizer sounds'",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            
                            Text(
                                text = "• Lyrics should be short and concise, written like telling a story.",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            
                            Text(
                                text = "• Each music creation operation uses 10 credits.",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Notifications for success and error states
                val currentState = uiState
                when (currentState) {
                    is MusicUiState.Success -> {
                        Snackbar(
                            modifier = Modifier.padding(16.dp),
                            containerColor = Color(0f, 0.3f, 0.1f),
                            contentColor = Color.White,
                            action = {
                                TextButton(onClick = { 
                                    onNavigateToMyMusic() 
                                }) {
                                    Text("Go to My Music", color = Color.White)
                                }
                            }
                        ) {
                            Text(currentState.message)
                        }
                    }
                    is MusicUiState.Error -> {
                        Snackbar(
                            modifier = Modifier.padding(16.dp),
                            containerColor = Color(0.5f, 0f, 0f),
                            contentColor = Color.White
                        ) {
                            Text(currentState.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

/**
 * Custom button for adding song sections
 */
@Composable
private fun SongSectionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.8f),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        modifier = Modifier
            .padding(4.dp)
            .height(48.dp)
            .border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
} 