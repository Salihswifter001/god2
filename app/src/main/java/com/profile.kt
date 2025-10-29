package com

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.aihackathonkarisacikartim.god2.UserDetails
import com.UserStats
import com.ModernBlurredBackground
import com.NeonGlowButton
import com.NeonSidebar
import com.ProfilePhotoManager
import com.SidebarItem
// import com.formatDateToUserFriendly - removed, will use inline formatting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.sin
import io.github.jan.supabase.gotrue.user.UserInfo
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check

/**
 * Settings field component
 */
@Composable
fun SettingsField(
    label: String,
    value: String,
    isPassword: Boolean = false,
    isEditing: Boolean = false,
    editValue: String = "",
    onEditValueChange: (String) -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isEditing && !isPassword) {
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = onEditValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFAA00FF),
                            unfocusedBorderColor = Color(0xFF555555),
                            cursorColor = Color(0xFFAA00FF)
                        ),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = if (isPassword && !isEditing) "••••••••" else value,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )
                }
            }
            
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = if (isEditing) Icons.Filled.Check else Icons.Filled.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit",
                    tint = Color(0xFFAA00FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Settings switch component
 */
@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFAA00FF),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFF555555)
                )
            )
        }
    }
}

/**
 * User profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateToMusicGen: () -> Unit = {},
    navController: NavController
) {
    // Supabase Manager'dan kullanıcı bilgilerini al
    val supabaseManager = remember { SupabaseManager() }
    var currentUser by remember { mutableStateOf<UserInfo?>(null) }
    var userDetails by remember { mutableStateOf<UserDetails?>(null) }
    var userEmail by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var showElements by remember { mutableStateOf(false) }

    // Hata ayıklama için durum kontrol değişkenleri
    var loadingCompleted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Kullanıcı istatistikleri için state
    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var userStatsLoaded by remember { mutableStateOf(false) }

    // Form states for editing
    var username by remember { mutableStateOf("") }
    var editingUsername by remember { mutableStateOf("") }
    var editingEmail by remember { mutableStateOf("") }
    var editingPassword by remember { mutableStateOf("") }
    var isEditingField by remember { mutableStateOf("") } // which field is being edited

    // Privacy switches
    var privateProfile by remember { mutableStateOf(false) }
    var shareUsageData by remember { mutableStateOf(true) }

    // Notification switches
    var newFeaturesNotif by remember { mutableStateOf(true) }
    var promotionsNotif by remember { mutableStateOf(false) }
    
    // Email change dialog states
    var showEmailChangeDialog by remember { mutableStateOf(false) }
    var emailChangePassword by remember { mutableStateOf("") }
    var newEmailAddress by remember { mutableStateOf("") }

    // Profil güncellendiğinde yeniden yükleme tetikleyicisi
    var profileUpdateTrigger by remember { mutableStateOf(0) }

    // Kullanıcı bilgilerini yükle
    LaunchedEffect(profileUpdateTrigger) {
        coroutineScope.launch {
            try {
                println("DEBUG: UserProfileScreen - Fetching current user")
                val currentUserInfo = supabaseManager.getCurrentUser()
                currentUser = currentUserInfo
                println("DEBUG: UserProfileScreen - Current user: ${currentUserInfo?.id}, email: ${currentUserInfo?.email}")

                if (currentUserInfo != null) {
                    // Kullanıcı giriş yapmış, kullanıcı adını al
                    println("DEBUG: UserProfileScreen - Fetching user details for ID: ${currentUserInfo.id}")
                    userDetails = supabaseManager.getUserDetails(currentUserInfo.id)
                    println("DEBUG: UserProfileScreen - User details received: $userDetails")
                    userEmail = currentUserInfo.email ?: ""
                    println("DEBUG: UserProfileScreen - User email set to: $userEmail")

                    // Kullanıcı istatistiklerini yükle
                    try {
                        // Son giriş tarihini güncelle
                        supabaseManager.updateLastLogin(currentUserInfo.id)

                        // Kullanıcı istatistiklerini yükle
                        val stats = supabaseManager.getUserStats(currentUserInfo.id)
                        userStats = stats
                        userStatsLoaded = true
                        println("DEBUG: UserProfileScreen - User stats loaded: $stats")
                    } catch (e: Exception) {
                        println("DEBUG: UserProfileScreen - Error loading user stats: ${e.message}")
                    }
                } else {
                    println("DEBUG: UserProfileScreen - No logged in user found")
                    errorMessage = "Oturum açmış kullanıcı bulunamadı"
                }
            } catch (e: Exception) {
                println("DEBUG: UserProfileScreen - Error getting user details: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error getting user information: ${e.message}"
            } finally {
                loadingCompleted = true
            }
        }

        // Animasyonlu giriş için delay
        delay(300)
        showElements = true
    }

    // Animasyon değişkenleri
    val infiniteTransition = rememberInfiniteTransition(label = "profileTransition")

    val hue by infiniteTransition.animateFloat(
        initialValue = 200f, // Mavi
        targetValue = 220f,  // Koyu mavi
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hue"
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowIntensity"
    )

    // Glow faktörü
    val getGlowFactor = { offset: Float ->
        // Sin fonksiyonu -1.0 ile 1.0 arasında değer döndürür
        val factor =
            (sin((PI * 2 * (glowIntensity + offset) % 1).toDouble()).toFloat() + 1f) / 2f * 0.6f + 0.4f
        factor.coerceIn(0.0f, 1.0f)
    }

    // Kütüphane sayfasındaki gibi arkaplan yapısı
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // Arkaplan gradient
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
            topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x1AFFFFFF), // Ultra thin white at top
                                Color(0x0DFFFFFF), // Even thinner white at bottom
                            )
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33FFFFFF), // Subtle white border top
                                Color(0x1AFFFFFF)  // Fading border bottom
                            )
                        ),
                        shape = RectangleShape
                    )
            ) {
                // Glass effect overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x0A9C27B0), // Ultra thin purple left
                                    Color(0x0AE91E63), // Ultra thin pink middle
                                    Color(0x0A9C27B0)  // Ultra thin purple right
                                )
                            )
                        )
                )
                
                // Frosted glass texture
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            // Noise texture for frosted glass effect
                            for (i in 0..20) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.01f),
                                    radius = kotlin.random.Random.nextFloat() * 100f,
                                    center = Offset(
                                        kotlin.random.Random.nextFloat() * size.width,
                                        kotlin.random.Random.nextFloat() * size.height
                                    )
                                )
                            }
                        }
                )
                
                // Content
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button with glass effect
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0x1AFFFFFF) // Ultra thin white background
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Title with glow effect
                    Text(
                        text = "Settings",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color(0x809C27B0),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                shadowElevation = 8.dp.toPx()
                            }
                    )
                }
            }
        },
        bottomBar = {
            com.ui.components.BottomNavBar(
                navController = navController,
                currentRoute = "profile"
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {

            // Hata mesajı
            if (errorMessage != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { errorMessage = null },
                    title = { Text("Error", color = Color.White) },
                    text = { Text(errorMessage ?: "", color = Color.White) },
                    confirmButton = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("OK", color = Color(0f, 0.8f, 1f))
                        }
                    },
                    containerColor = Color(0f, 0f, 0.1f, 0.9f),
                    titleContentColor = Color.White,
                    textContentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            // Email değiştirme dialog'u
            if (showEmailChangeDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { 
                        showEmailChangeDialog = false
                        emailChangePassword = ""
                        newEmailAddress = ""
                    },
                    title = { 
                        Text(
                            "Email Adresini Değiştir",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Yeni email: $newEmailAddress",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            
                            Text(
                                "Email adresinizi değiştirmek için mevcut şifrenizi girmeniz gerekmektedir:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            
                            OutlinedTextField(
                                value = emailChangePassword,
                                onValueChange = { emailChangePassword = it },
                                label = { Text("Mevcut Şifre", color = Color.White.copy(alpha = 0.7f)) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color(0xFF2A2A3E),
                                    unfocusedContainerColor = Color(0xFF2A2A3E),
                                    focusedIndicatorColor = Color(0f, 0.8f, 1f),
                                    unfocusedIndicatorColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (emailChangePassword.isNotEmpty()) {
                                    coroutineScope.launch {
                                        try {
                                            val result = supabaseManager.updateEmail(
                                                emailChangePassword,
                                                newEmailAddress
                                            )
                                            
                                            if (result.isSuccess) {
                                                userEmail = newEmailAddress
                                                editingEmail = newEmailAddress
                                                showEmailChangeDialog = false
                                                emailChangePassword = ""
                                                newEmailAddress = ""
                                                errorMessage = "Email adresiniz başarıyla güncellendi. Lütfen yeni email adresinize gelen doğrulama linkine tıklayın."
                                            } else {
                                                errorMessage = result.exceptionOrNull()?.message
                                                    ?: "Email güncelleme başarısız"
                                                showEmailChangeDialog = false
                                                emailChangePassword = ""
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Hata: ${e.message}"
                                            showEmailChangeDialog = false
                                            emailChangePassword = ""
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Değiştir", color = Color(0f, 0.8f, 1f))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showEmailChangeDialog = false
                                emailChangePassword = ""
                                newEmailAddress = ""
                            }
                        ) {
                            Text("İptal", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1A0033).copy(alpha = 0.95f),
                    titleContentColor = Color.White,
                    textContentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Yükleniyor göstergesi
            if (!loadingCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color.hsv(hue % 360f, 0.8f, 0.8f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading profile information...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                // Ana içerik
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Header Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Photo with Edit Button
                        Box(
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 3.dp,
                                        color = Color(0xFFAA00FF),
                                        shape = CircleShape
                                    )
                                    .background(Color(0xFF2A2A3E))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Profile Photo",
                                    tint = Color(0xFF888888),
                                    modifier = Modifier
                                        .size(60.dp)
                                        .align(Alignment.Center)
                                )
                            }

                            // Edit button
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFAA00FF))
                                    .clickable { /* Handle photo edit */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Username
                        Text(
                            text = userDetails?.username ?: userEmail.split("@")[0],
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )

                        // Email
                        Text(
                            text = userEmail,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color(0xFF888888)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Settings Sections
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Personal Information Section
                        Text(
                            text = "Personal Information",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Username Field
                        SettingsField(
                            label = "Username",
                            value = userDetails?.username ?: userEmail.split("@")[0],
                            isEditing = isEditingField == "username",
                            editValue = editingUsername,
                            onEditValueChange = { editingUsername = it },
                            onEditClick = {
                                if (isEditingField == "username") {
                                    // Save username
                                    coroutineScope.launch {
                                        currentUser?.id?.let { userId ->
                                            val result = supabaseManager.updateUserProfile(
                                                userId = userId,
                                                username = editingUsername
                                            )
                                            if (result.isSuccess) {
                                                profileUpdateTrigger++
                                            }
                                        }
                                    }
                                    isEditingField = ""
                                } else {
                                    editingUsername =
                                        userDetails?.username ?: userEmail.split("@")[0]
                                    isEditingField = "username"
                                }
                            }
                        )

                        // Email Field
                        SettingsField(
                            label = "Email",
                            value = userEmail,
                            isEditing = isEditingField == "email",
                            editValue = editingEmail,
                            onEditValueChange = { editingEmail = it },
                            onEditClick = {
                                if (isEditingField == "email") {
                                    // Save email
                                    if (editingEmail != userEmail && editingEmail.isNotEmpty()) {
                                        // Email değiştirme için dialog göster
                                        newEmailAddress = editingEmail
                                        showEmailChangeDialog = true
                                    }
                                    isEditingField = ""
                                } else {
                                    editingEmail = userEmail
                                    isEditingField = "email"
                                }
                            }
                        )

                        // Password Field
                        SettingsField(
                            label = "Password",
                            value = "••••••••",
                            isPassword = true,
                            isEditing = isEditingField == "password",
                            editValue = editingPassword,
                            onEditValueChange = { editingPassword = it },
                            onEditClick = {
                                if (isEditingField == "password") {
                                    // Save password - not implemented for now
                                    isEditingField = ""
                                    editingPassword = ""
                                } else {
                                    isEditingField = "password"
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Privacy Section
                        Text(
                            text = "Privacy",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Private Profile Switch
                        SettingsSwitch(
                            title = "Private Profile",
                            subtitle = "Only you can see your creations",
                            checked = privateProfile,
                            onCheckedChange = { privateProfile = it }
                        )

                        // Share Usage Data Switch
                        SettingsSwitch(
                            title = "Share Usage Data",
                            subtitle = "Help improve our AI models",
                            checked = shareUsageData,
                            onCheckedChange = { shareUsageData = it }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Notifications Section
                        Text(
                            text = "Notifications",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // New Features Switch
                        SettingsSwitch(
                            title = "New Features",
                            subtitle = "Get notified about app updates",
                            checked = newFeaturesNotif,
                            onCheckedChange = { newFeaturesNotif = it }
                        )

                        // Promotions Switch
                        SettingsSwitch(
                            title = "Promotions",
                            subtitle = "Receive special offers and deals",
                            checked = promotionsNotif,
                            onCheckedChange = { promotionsNotif = it }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Subscription Section
                        Text(
                            text = "Subscription",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Current Plan Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A3E)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Current Plan",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = Color(0xFF888888)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${userStats?.membership_type ?: "Free"} - Renews on ${
                                            userStats?.join_date?.let {
                                                try {
                                                    val date =
                                                        LocalDateTime.parse(it.replace(" ", "T"))
                                                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                                } catch (e: Exception) {
                                                    "2024/12/31"
                                                }
                                            } ?: "2024/12/31"
                                        }",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                TextButton(
                                    onClick = { /* Handle manage subscription */ },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color(0xFFAA00FF)
                                    )
                                ) {
                                    Text("Manage")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Log Out Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    // Sign out - navigate to login
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.id) {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFAA00FF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ExitToApp,
                                    contentDescription = "Log Out",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Log Out",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for navigation bar
                    }
                }
            }
        }
    }

    @Composable
    fun TabButton(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        text: String,
        glowColor: Color
    ) {
        val backgroundColor = if (selected) {
            Color(0f, 0.3f, 0.6f, 0.7f)
        } else {
            Color.Transparent
        }

        val contentColor = if (selected) {
            glowColor
        } else {
            Color.White.copy(alpha = 0.6f)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = contentColor,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }

    @Composable
    fun StatsPanel(
        userStats: UserStats?,
        userStatsLoaded: Boolean,
        getGlowFactor: (Float) -> Float,
        hue: Float
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0f, 0.05f, 0.15f, 0.8f),
                            Color(0f, 0.1f, 0.25f, 0.8f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0f, 0.8f, 1f, getGlowFactor(0f) * 0.7f),
                            Color(0f, 0.3f, 0.7f, getGlowFactor(0.5f) * 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Başlık
                Text(
                    text = "Kullanıcı İstatistikleri",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0f, 0.8f, 1f),
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Biyografi Kartı - Eğer varsa göster
                val currentUser = remember { SupabaseManager().getCurrentUser() }
                var userDetails by remember { mutableStateOf<UserDetails?>(null) }

                // Kullanıcı detaylarını yükle
                LaunchedEffect(Unit) {
                    try {
                        currentUser?.id?.let { userId ->
                            val details = SupabaseManager().getUserDetails(userId)
                            userDetails = details
                        }
                    } catch (e: Exception) {
                        println("DEBUG: Error loading user details: ${e.message}")
                    }
                }

                // Eğer biyografi varsa göster
                if (!userDetails?.biography.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0f, 0.1f, 0.2f, 0.6f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Biyografi",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0f, 0.8f, 1f)
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = userDetails?.biography ?: "",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }

                if (userStatsLoaded && userStats != null) {
                    // Veriler yüklendi, istatistikleri göster
                    // Görsel tasarım olarak kartlar içinde gösterelim

                    // Oluşturulan Müzik Sayısı
                    StatisticCard(
                        icon = Icons.Filled.MusicNote,
                        title = "Oluşturulan Müzik",
                        value = userStats.created_musics.toString(),
                        glowColor = Color(0f, 0.8f, 1f),
                        backgroundColor = Color(0f, 0.15f, 0.3f, 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kalan Kredi Sayısı
                    StatisticCard(
                        icon = Icons.Filled.Star,
                        title = "Kalan Kredi",
                        value = userStats.credits.toString(),
                        glowColor = Color(0f, 0.8f, 1f),
                        backgroundColor = Color(0f, 0.15f, 0.3f, 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Favori Tür - Özel başlık kullanımı
                    StatisticCardWithCustomTitle(
                        icon = Icons.Filled.Favorite,
                        regularText = "Favori Tür ",
                        specialText = "(Yakında)",
                        specialTextColor = Color(0.7f, 0.1f, 0.9f),
                        value = userStats.favorite_genre ?: "Henüz yok",
                        glowColor = Color(0f, 0.8f, 1f),
                        backgroundColor = Color(0f, 0.15f, 0.3f, 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Katılım Tarihi
                    StatisticCard(
                        icon = Icons.Filled.CalendarToday,
                        title = "Katılım Tarihi",
                        value = userStats.join_date ?: "Bilinmiyor",
                        glowColor = Color(0f, 0.8f, 1f),
                        backgroundColor = Color(0f, 0.15f, 0.3f, 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Son Giriş
                    userStats.last_login_date?.let {
                        StatisticCard(
                            icon = Icons.Filled.AccessTime,
                            title = "Son Giriş",
                            value = it,
                            glowColor = Color(0f, 0.8f, 1f),
                            backgroundColor = Color(0f, 0.15f, 0.3f, 0.5f)
                        )
                    }
                } else {
                    // Yükleniyor göstergesi
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.hsv(hue % 360f, 0.7f, 0.9f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsPanel(
        supabaseManager: SupabaseManager,
        hue: Float,
        getGlowFactor: (Float) -> Float,
        onProfileUpdated: () -> Unit = {}, // Profil güncellendiğinde çağrılacak callback ekledik
        autoOpenProfileEdit: Boolean = false // Otomatik olarak profil düzenleme bölümünü açmak için parametre
    ) {
        var showPasswordChangeSection by remember { mutableStateOf(false) }
        var showProfileEditSection by remember { mutableStateOf(autoOpenProfileEdit) }

        // Şifre değiştirme
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmNewPassword by remember { mutableStateOf("") }

        // Profil bilgileri
        val currentUser = remember { mutableStateOf(supabaseManager.getCurrentUser()) }
        var userDetails by remember { mutableStateOf<UserDetails?>(null) }
        var username by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("") }
        var biography by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        val genderOptions = listOf("Erkek", "Kadın", "Diğer", "Belirtmek İstemiyorum")

        // İşlem durumu
        var isLoading by remember { mutableStateOf(false) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val coroutineScope = rememberCoroutineScope()

        // Kullanıcı detaylarını yükle
        LaunchedEffect(Unit) {
            currentUser.value?.id?.let { userId ->
                try {
                    val details = supabaseManager.getUserDetails(userId)
                    userDetails = details

                    // Mevcut değerleri ayarla
                    username = details?.username ?: ""
                    gender = details?.gender ?: ""
                    biography = details?.biography ?: ""
                    phoneNumber = details?.phone_number ?: ""
                } catch (e: Exception) {
                    errorMessage = "Profil bilgileri yüklenirken hata oluştu: ${e.message}"
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0f, 0.05f, 0.15f, 0.8f),
                            Color(0f, 0.1f, 0.25f, 0.8f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0f, 0.8f, 1f, getGlowFactor(0f) * 0.7f),
                            Color(0f, 0.3f, 0.7f, getGlowFactor(0.5f) * 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Başlık
                Text(
                    text = "Hesap Ayarları",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0f, 0.8f, 1f),
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Bilgi mesajları
                successMessage?.let {
                    Text(
                        text = it,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0f, 0.8f, 0.2f),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(
                                Color(0f, 0.2f, 0f, 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0.8f, 0.2f, 0.2f),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(
                                Color(0.2f, 0f, 0f, 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                }

                // Profil Bilgileri Düzenleme Bölümü
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0f, 0.1f, 0.2f, 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Başlık ve Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Profil Bilgilerini Düzenle",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )

                            IconButton(
                                onClick = { showProfileEditSection = !showProfileEditSection }
                            ) {
                                Icon(
                                    imageVector = if (showProfileEditSection) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Genişlet/Daralt",
                                    tint = Color(0f, 0.7f, 1f)
                                )
                            }
                        }

                        // Profil düzenleme formu
                        AnimatedVisibility(
                            visible = showProfileEditSection,
                            enter = expandVertically() + fadeIn(),
                            exit = slideOutVertically { height -> -height } + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                // Kullanıcı adı
                                var usernameError by remember { mutableStateOf<String?>(null) }
                                var isUsernameValid by remember { mutableStateOf(true) }

                                OutlinedTextField(
                                    value = username,
                                    onValueChange = {
                                        // Kullanıcı adı boşluk içeremez
                                        val filteredUsername = it.replace(" ", "")

                                        if (filteredUsername != it) {
                                            usernameError = "Kullanıcı adı boşluk içeremez"
                                        } else {
                                            usernameError = null
                                        }

                                        // Karakter kısıtlaması ve validasyon
                                        if (filteredUsername.length > 20) {
                                            usernameError =
                                                "Kullanıcı adı maksimum 20 karakter olabilir"
                                            isUsernameValid = false
                                        } else if (filteredUsername.isNotEmpty() && !filteredUsername.matches(
                                                Regex("^[a-zA-Z0-9_\\.]+$")
                                            )
                                        ) {
                                            usernameError =
                                                "Kullanıcı adı sadece harf, rakam, nokta ve alt çizgi içerebilir"
                                            isUsernameValid = false
                                        } else {
                                            isUsernameValid = true
                                        }

                                        username = filteredUsername
                                    },
                                    label = { Text("Kullanıcı Adı") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isUsernameValid) Color(
                                            0f,
                                            0.7f,
                                            1f
                                        ) else Color.Red,
                                        unfocusedBorderColor = if (isUsernameValid) Color(
                                            0f,
                                            0.5f,
                                            0.8f
                                        ) else Color.Red,
                                        focusedLabelColor = if (isUsernameValid) Color(
                                            0f,
                                            0.7f,
                                            1f
                                        ) else Color.Red,
                                        unfocusedLabelColor = if (isUsernameValid) Color(
                                            0f,
                                            0.5f,
                                            0.8f
                                        ) else Color.Red,
                                        cursorColor = Color(0f, 0.7f, 1f),
                                        errorBorderColor = Color.Red,
                                        errorLabelColor = Color.Red
                                    ),
                                    isError = !isUsernameValid,
                                    supportingText = {
                                        if (usernameError != null) {
                                            Text(
                                                text = usernameError!!,
                                                color = Color.Red,
                                                style = TextStyle(fontSize = 12.sp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                )

                                // Kullanıcı adının değişikliğini izleme
                                LaunchedEffect(username) {
                                    // Kullanıcı adı değiştirilmeye çalışılıyor ve ilk değerden farklı ise kontrol et
                                    if (userDetails != null && userDetails?.username != username &&
                                        (userDetails?.username_change_count ?: 0) >= 3
                                    ) {
                                        usernameError =
                                            "Kullanıcı adı değiştirme hakkınız dolmuştur (maximum 3 kez)."
                                        // Kullanıcı adını orijinal haline geri çevir
                                        username = userDetails?.username ?: username
                                    }
                                }

                                // Kullanıcı adı değiştirme hakkı bilgisi ekleyelim
                                val changeCount = userDetails?.username_change_count ?: 0
                                val remainingChanges = 3 - changeCount

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = if (remainingChanges > 0) Color(
                                            0f,
                                            0.7f,
                                            1f
                                        ) else Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (remainingChanges > 0)
                                            "Kalan kullanıcı adı değiştirme hakkınız: $remainingChanges"
                                        else
                                            "Kullanıcı adı değiştirme hakkınız kalmadı",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (remainingChanges > 0) Color(
                                                0f,
                                                0.7f,
                                                1f
                                            ) else Color.Red
                                        )
                                    )
                                }

                                // Cinsiyet seçimi (Dropdown)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = gender,
                                        onValueChange = { },
                                        label = { Text("Cinsiyet") },
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0f, 0.7f, 1f),
                                            unfocusedBorderColor = Color(0f, 0.5f, 0.8f),
                                            focusedLabelColor = Color(0f, 0.7f, 1f),
                                            unfocusedLabelColor = Color(0f, 0.5f, 0.8f),
                                            cursorColor = Color(0f, 0.7f, 1f)
                                        ),
                                        trailingIcon = {
                                            IconButton(onClick = { expanded = true }) {
                                                Icon(
                                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                                    contentDescription = "Cinsiyet Seç",
                                                    tint = Color(0f, 0.7f, 1f)
                                                )
                                            }
                                        },
                                        interactionSource = remember { MutableInteractionSource() },
                                        enabled = true
                                    )

                                    // TextField'ın üzerine tıklanabilir saydam bir katman ekliyoruz
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { expanded = true }
                                    )

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .background(Color(0f, 0.15f, 0.3f, 0.95f))
                                            .border(
                                                width = 1.dp,
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0f, 0.7f, 1f, 0.7f),
                                                        Color(0f, 0.3f, 0.8f, 0.5f)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        genderOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = option,
                                                        color = Color.White,
                                                        style = TextStyle(
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    )
                                                },
                                                onClick = {
                                                    gender = option
                                                    expanded = false
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(
                                                        if (gender == option)
                                                            Color(0f, 0.4f, 0.8f, 0.5f)
                                                        else
                                                            Color.Transparent
                                                    )
                                            )
                                        }
                                    }
                                }

                                // Biyografi
                                OutlinedTextField(
                                    value = biography,
                                    onValueChange = { biography = it },
                                    label = { Text("Biyografi") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .height(120.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0f, 0.7f, 1f),
                                        unfocusedBorderColor = Color(0f, 0.5f, 0.8f),
                                        focusedLabelColor = Color(0f, 0.7f, 1f),
                                        unfocusedLabelColor = Color(0f, 0.5f, 0.8f),
                                        cursorColor = Color(0f, 0.7f, 1f)
                                    ),
                                    maxLines = 5
                                )

                                // Telefon numarası
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Telefon Numarası") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0f, 0.7f, 1f),
                                        unfocusedBorderColor = Color(0f, 0.5f, 0.8f),
                                        focusedLabelColor = Color(0f, 0.7f, 1f),
                                        unfocusedLabelColor = Color(0f, 0.5f, 0.8f),
                                        cursorColor = Color(0f, 0.7f, 1f)
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone
                                    )
                                )

                                // Profil Bilgilerini Kaydet Butonu
                                Button(
                                    onClick = {
                                        currentUser.value?.id?.let { userId ->
                                            coroutineScope.launch {
                                                isLoading = true
                                                errorMessage = null
                                                successMessage = null

                                                try {
                                                    // Kullanıcı adı validasyonu
                                                    if (username.isNotBlank() && !username.matches(
                                                            Regex("^[a-zA-Z0-9_\\.]+$")
                                                        )
                                                    ) {
                                                        errorMessage =
                                                            "Kullanıcı adı sadece harf, rakam, nokta ve alt çizgi içerebilir"
                                                        isLoading = false
                                                        return@launch
                                                    }

                                                    if (username.length > 20) {
                                                        errorMessage =
                                                            "Kullanıcı adı maksimum 20 karakter olabilir"
                                                        isLoading = false
                                                        return@launch
                                                    }

                                                    val result = supabaseManager.updateUserProfile(
                                                        userId = userId,
                                                        username = username,
                                                        gender = gender,
                                                        biography = biography,
                                                        phone_number = phoneNumber
                                                    )

                                                    if (result.isSuccess) {
                                                        userDetails = result.getOrNull()
                                                        successMessage =
                                                            "Profil bilgileriniz başarıyla güncellendi"
                                                        showProfileEditSection = false
                                                        onProfileUpdated() // Profil güncellendiğinde callback'i çağır
                                                    } else {
                                                        errorMessage =
                                                            result.exceptionOrNull()?.message
                                                                ?: "Profil güncelleme başarısız"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Hata: ${e.message}"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0f, 0.4f, 0.8f),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0f, 0.2f, 0.4f, 0.5f),
                                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    enabled = !isLoading &&
                                            // Kullanıcı adı değiştiriliyor ve kalan hak yoksa butonu devre dışı bırak
                                            !((userDetails?.username != username) &&
                                                    (userDetails?.username_change_count ?: 0) >= 3)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Bilgileri Kaydet")
                                    }
                                }
                            }
                        }
                    }
                }

                // Şifre Değiştirme Bölümü
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0f, 0.1f, 0.2f, 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Başlık ve Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Şifre Değiştir",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )

                            IconButton(
                                onClick = { showPasswordChangeSection = !showPasswordChangeSection }
                            ) {
                                Icon(
                                    imageVector = if (showPasswordChangeSection) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Genişlet/Daralt",
                                    tint = Color(0f, 0.7f, 1f)
                                )
                            }
                        }

                        // Şifre değiştirme formu - AnimatedVisibility ile görünümü değiştir
                        AnimatedVisibility(
                            visible = showPasswordChangeSection,
                            enter = expandVertically() + fadeIn(),
                            exit = slideOutVertically { height -> -height } + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                // Mevcut şifre
                                var currentPasswordVisible by remember { mutableStateOf(false) }

                                OutlinedTextField(
                                    value = currentPassword,
                                    onValueChange = { currentPassword = it },
                                    label = { Text("Mevcut Şifre") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0f, 0.7f, 1f),
                                        unfocusedBorderColor = Color(0f, 0.5f, 0.8f),
                                        focusedLabelColor = Color(0f, 0.7f, 1f),
                                        unfocusedLabelColor = Color(0f, 0.5f, 0.8f),
                                        cursorColor = Color(0f, 0.7f, 1f)
                                    ),
                                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Next
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            currentPasswordVisible = !currentPasswordVisible
                                        }) {
                                            Icon(
                                                imageVector = if (currentPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = "Şifreyi Göster/Gizle",
                                                tint = Color(0f, 0.7f, 1f)
                                            )
                                        }
                                    }
                                )

                                // Yeni şifre
                                var newPasswordVisible by remember { mutableStateOf(false) }

                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Yeni Şifre") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0f, 0.7f, 1f),
                                        unfocusedBorderColor = Color(0f, 0.5f, 0.8f),
                                        focusedLabelColor = Color(0f, 0.7f, 1f),
                                        unfocusedLabelColor = Color(0f, 0.5f, 0.8f),
                                        cursorColor = Color(0f, 0.7f, 1f)
                                    ),
                                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Next
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            newPasswordVisible = !newPasswordVisible
                                        }) {
                                            Icon(
                                                imageVector = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = "Şifreyi Göster/Gizle",
                                                tint = Color(0f, 0.7f, 1f)
                                            )
                                        }
                                    }
                                )

                                // Yeni şifre onayı
                                var confirmPasswordVisible by remember { mutableStateOf(false) }

                                OutlinedTextField(
                                    value = confirmNewPassword,
                                    onValueChange = { confirmNewPassword = it },
                                    label = { Text("Yeni Şifre Tekrar") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0f, 0.7f, 1f),
                                        unfocusedBorderColor = Color(0f, 0.5f, 0.8f),
                                        focusedLabelColor = Color(0f, 0.7f, 1f),
                                        unfocusedLabelColor = Color(0f, 0.5f, 0.8f),
                                        cursorColor = Color(0f, 0.7f, 1f)
                                    ),
                                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            confirmPasswordVisible = !confirmPasswordVisible
                                        }) {
                                            Icon(
                                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = "Şifreyi Göster/Gizle",
                                                tint = Color(0f, 0.7f, 1f)
                                            )
                                        }
                                    }
                                )

                                // Hata kontrolü
                                if (newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword) {
                                    Text(
                                        text = "Şifreler eşleşmiyor!",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }

                                // Şifre değiştirme butonu
                                Button(
                                    onClick = {
                                        // Şifre değiştirme işlemi
                                        if (newPassword != confirmNewPassword) {
                                            errorMessage = "Yeni şifreler eşleşmiyor"
                                            return@Button
                                        }

                                        if (newPassword.length < 6) {
                                            errorMessage = "Şifre en az 6 karakter olmalıdır"
                                            return@Button
                                        }

                                        coroutineScope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            successMessage = null

                                            try {
                                                val result = supabaseManager.updatePassword(
                                                    currentPassword,
                                                    newPassword
                                                )

                                                if (result.isSuccess) {
                                                    successMessage =
                                                        "Şifreniz başarıyla değiştirildi"
                                                    currentPassword = ""
                                                    newPassword = ""
                                                    confirmNewPassword = ""
                                                    showPasswordChangeSection = false
                                                } else {
                                                    errorMessage = result.exceptionOrNull()?.message
                                                        ?: "Şifre değiştirme başarısız"
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "Hata: ${e.message}"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0f, 0.4f, 0.8f),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0f, 0.2f, 0.4f, 0.5f),
                                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    enabled = !isLoading && currentPassword.isNotEmpty() && newPassword.isNotEmpty()
                                            && confirmNewPassword.isNotEmpty() && newPassword == confirmNewPassword
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Şifreyi Değiştir")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    } // Outer Box end
}

@Composable
fun StatisticCard(
        icon: ImageVector,
        title: String,
        value: String,
        glowColor: Color,
        backgroundColor: Color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.2f),
                                glowColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // İçerik
            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }

@Composable
fun StatisticCardWithCustomTitle(
        icon: ImageVector,
        regularText: String,
        specialText: String,
        specialTextColor: Color,
        value: String,
        glowColor: Color,
        backgroundColor: Color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.2f),
                                glowColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // İçerik
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = regularText,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )

                    Text(
                        text = specialText,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = specialTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }