package com

import com.settings.SessionManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.NeonGlowButton
import com.aihackathonkarisacikartim.god2.SupabaseManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import com.ui.components.BlackHoleBackground

@OptIn(
    ExperimentalTextApi::class, 
    ExperimentalMaterial3Api::class,
    androidx.compose.animation.ExperimentalAnimationApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)
@Composable
fun OctaLoginScreen(
    onLogin: (username: String, password: String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onMusicScreen: () -> Unit = {},
    onSignup: () -> Unit = {}
) {
    // Orbitron font tanımı - Projenize eklenmeli (res/font/orbitron_bold.ttf, res/font/orbitron_regular.ttf)
    val orbitronFont = FontFamily.SansSerif // Geçici fallback

    // Application context'i al - applicationContext kullan
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    // OctaApplication kaldırıldı - artık gerekli değil

    // State değişkenleri
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginInProgress by remember { mutableStateOf(false) }
    var loginSuccess by remember { mutableStateOf(false) }
    var showElements by remember { mutableStateOf(false) }
    var loginButtonEnabled by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Animasyon sequence
    var logoAppeared by remember { mutableStateOf(false) }
    var formAppeared by remember { mutableStateOf(false) }
    var buttonAppeared by remember { mutableStateOf(false) }
    var backgroundParticlesVisible by remember { mutableStateOf(false) }

    // Animasyon kontrolcüleri
    val infiniteTransition = rememberInfiniteTransition(label = "mainTransition")
    
    // Ana renk animasyonu - gece mavisi ile mor arası geçiş
    val hue by infiniteTransition.animateFloat(
        initialValue = 210f, // Gece mavisi
        targetValue = 260f,  // Mor tonları
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hueAnimation"
    )
    
    // Gradient animasyonu
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), 
        label = "gradientShift"
    )
    
    // Parlaklık dalgalanması
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Logo nabız efekti
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )
    
    // Arka plan döngüsü
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // Renk paletleri
    val neonBlue = Color(0xFF00FFFF)   // Parlak siber mavi
    val neonPurple = Color(0xFF9D00FF) // Parlak mor
    val deepBlue = Color(0xFF0A0A2A)   // Koyu mavi
    val darkestBlue = Color(0xFF050520) // Çok koyu mavi
    val spaceBlack = Color(0xFF000010)  // Uzay siyahı
    val accentColor = remember(hue) { Color.hsv(hue, 0.9f, 0.8f) }

    // Parlaklık faktörü
    val getGlowFactor = { offset: Float ->
        val sineWave = sin(PI.toFloat() * 2f * ((glowPulse + offset) % 1f))
        val factor = (sineWave + 1f) / 2f * 0.7f + 0.3f
        factor.coerceIn(0.2f, 1.0f)
    }

    // Animasyon sıralaması
    LaunchedEffect(Unit) {
        delay(200)
        showElements = true
        delay(300)
        backgroundParticlesVisible = true
        delay(300)
        logoAppeared = true
        delay(800)
        formAppeared = true
        delay(400)
        buttonAppeared = true
    }

    // Form kontrolü
    LaunchedEffect(email, password) {
        loginButtonEnabled = email.isNotBlank() && password.isNotBlank()
    }

    // Input focus yönetimi
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val (emailFocusRequester, passwordFocusRequester) = remember { FocusRequester.createRefs() }

    // Login işlevi
    fun performLogin() {
        if (!loginButtonEnabled || loginInProgress) return

        focusManager.clearFocus()
        coroutineScope.launch {
            loginInProgress = true
            errorMessage = null
            
            try {
                println("DEBUG login.kt: Starting login process...")
                println("DEBUG login.kt: Context: $context")
                
                // Supabase ile giriş
                val supabaseManager = SupabaseManager()
                val sessionManager = SessionManager(context)
                
                println("DEBUG login.kt: SessionManager created")
                
                // Login işlemi asenkron olduğu için await kullanmalıyız
                val result = supabaseManager.login(email, password, sessionManager)
                
                if (result.isSuccess) {
                    loginSuccess = true
                    
                    // Session already saved in SupabaseManager.login()
                    println("DEBUG login.kt: Login successful")
                    println("DEBUG login.kt: Checking session - isLoggedIn: ${sessionManager.isLoggedIn()}")
                    println("DEBUG login.kt: Saved email: ${sessionManager.getUserEmail()}")
                    println("DEBUG login.kt: Saved userId: ${sessionManager.getUserId()}")
                    println("DEBUG login.kt: Saved username: ${sessionManager.getUsername()}")
                    
                    delay(800) // Başarılı animasyon için biraz bekle
                    onLogin(email, password)
                } else {
                    loginSuccess = false
                    val errorMsg = result.exceptionOrNull()?.message ?: ""
                    errorMessage = when {
                        errorMsg.contains("Invalid login credentials") -> "E-posta adresi veya şifre hatalı"
                        errorMsg.contains("Email not confirmed") -> "E-posta adresi doğrulanmamış"
                        else -> errorMsg.ifBlank { "Giriş başarısız" }
                    }
                }
            } catch (e: Exception) {
                loginSuccess = false
                errorMessage = "Giriş sırasında hata oluştu: ${e.message}"
                println("DEBUG: Login error: ${e.message}")
            } finally {
                loginInProgress = false
            }
        }
    }

    // Ana ekran
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Kara delik arka plan animasyonu
        BlackHoleBackground(
            modifier = Modifier.fillMaxSize()
        )
        
        // Arka plan partikülleri (overlay olarak) - DEVRE DIŞI BIRAKILDI KARA DELİK GÖRÜNSÜN DİYE
        if (false && backgroundParticlesVisible) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.4f) // Kara delik efektiyle uyumlu görünmesi için
                    .blur(3.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension * 0.45f
                
                // Işın partikülleri
                for (i in 0..30) {
                    val angle = (i * 12f + rotationAngle) % 360
                    val angleRad = angle * PI.toFloat() / 180
                    val length = radius * (0.5f + (sin(angleRad * 3) + 1) / 4)
                    val startRatio = 0.2f + (cos(angleRad * 5) + 1) / 4 * 0.2f
                    
                    val startX = centerX + cos(angleRad) * radius * startRatio
                    val startY = centerY + sin(angleRad) * radius * startRatio
                    
                    val endX = centerX + cos(angleRad) * length
                    val endY = centerY + sin(angleRad) * length
                    
                    val particleColor = if (i % 3 == 0) 
                        neonBlue.copy(alpha = getGlowFactor(i / 30f) * 0.15f)
                    else
                        neonPurple.copy(alpha = getGlowFactor((i + 15) / 30f) * 0.12f)
                    
                    drawLine(
                        color = particleColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 1.dp.toPx() * getGlowFactor(i / 10f)
                    )
                }
                
                // Büyük arka plan ışımaları
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.03f * getGlowFactor(0.1f)),
                            Color.Transparent
                        ),
                        center = Offset(centerX + size.width * 0.2f, centerY - size.height * 0.15f),
                    ),
                    radius = size.minDimension * 0.4f,
                    center = Offset(centerX + size.width * 0.2f, centerY - size.height * 0.15f)
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonBlue.copy(alpha = 0.02f * getGlowFactor(0.5f)),
                            Color.Transparent
                        ),
                        center = Offset(centerX - size.width * 0.3f, centerY + size.height * 0.2f),
                    ),
                    radius = size.minDimension * 0.5f,
                    center = Offset(centerX - size.width * 0.3f, centerY + size.height * 0.2f)
                )
            }
        }
        
        // Orta katman ışımaları
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(12.dp)
        ) {
            // Merkez glow
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.1f * getGlowFactor(0f)),
                            Color.Transparent
                        )
                    ),
                    center = Offset(size.width / 2, size.height * 0.4f),
                    radius = size.minDimension * 0.3f
                )
            }
            
            // Alt köşe glow
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(200.dp)
                    .alpha(0.15f * getGlowFactor(0.7f))
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonBlue,
                            Color.Transparent
                        )
                    ),
                    center = Offset(size.width, size.height),
                    radius = size.width
                )
            }
        }

        // Ana içerik alanı
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo ve başlık
            AnimatedVisibility(
                visible = showElements && logoAppeared,
                enter = fadeIn(tween(1200)) + scaleIn(
                    initialScale = 0.5f,
                    animationSpec = tween(1000, easing = EaseOutBack)
                ),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(if (loginSuccess) 1.3f else logoScale)
                            .drawBehind {
                                // Dış ışıma
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.3f * getGlowFactor(0.1f)),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension * 0.8f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Logo dış halka
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = getGlowFactor(0f)),
                                            accentColor.copy(alpha = getGlowFactor(0.25f) * 0.7f),
                                            accentColor.copy(alpha = getGlowFactor(0.5f) * 0.3f),
                                            accentColor.copy(alpha = getGlowFactor(0.75f) * 0.7f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Logo iç kısım
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF151530),
                                                Color(0xFF050510)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.7f),
                                                accentColor.copy(alpha = 0.5f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Logo text
                                Text(
                                    text = "OCTA",
                                    style = TextStyle(
                                        fontSize = 22.sp,
                                        fontFamily = orbitronFont,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White,
                                                accentColor.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }
                    
                    // Başlık
                    Text(
                        text = "OCTA'ya Hoş Geldiniz",
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontFamily = orbitronFont,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    accentColor.copy(alpha = 0.9f)
                                )
                            ),
                            shadow = Shadow(
                                color = accentColor.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 8f
                            )
                        )
                    )
                    
                    // Alt başlık
                    Text(
                        text = "Müziğin Yeni Boyutu",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = orbitronFont,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            // Form alanı
            AnimatedVisibility(
                visible = showElements && formAppeared,
                enter = fadeIn(tween(800)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(800, easing = EaseOutQuint)
                ),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // E-posta alanı
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
                            .drawBehind {
                                // Özel glow efekti
                                if (email.isNotBlank()) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                accentColor.copy(alpha = 0.2f * getGlowFactor(0.1f)),
                                                Color.Transparent
                                            )
                                        ),
                                        cornerRadius = CornerRadius(12.dp.toPx()),
                                        alpha = 0.5f
                                    )
                                }
                            },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = accentColor.copy(alpha = 0.8f)
                            )
                        },
                        label = {
                            Text(
                                "E-posta Adresi",
                                color = accentColor.copy(alpha = 0.9f),
                                fontFamily = orbitronFont,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontFamily = orbitronFont,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() }
                        ),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.Gray,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = accentColor.copy(alpha = 0.7f),
                            cursorColor = accentColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Şifre alanı
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester)
                            .drawBehind {
                                // Özel glow efekti
                                if (password.isNotBlank()) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                accentColor.copy(alpha = 0.2f * getGlowFactor(0.3f)),
                                                Color.Transparent
                                            )
                                        ),
                                        cornerRadius = CornerRadius(12.dp.toPx()),
                                        alpha = 0.5f
                                    )
                                }
                            },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = accentColor.copy(alpha = 0.8f)
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        label = {
                            Text(
                                "Şifre",
                                color = accentColor.copy(alpha = 0.9f),
                                fontFamily = orbitronFont,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontFamily = orbitronFont,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (loginButtonEnabled) {
                                    performLogin()
                                }
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) 
                                        Icons.Default.Visibility
                                    else 
                                        Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = accentColor.copy(alpha = 0.8f)
                                )
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.Gray,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = accentColor.copy(alpha = 0.7f),
                            cursorColor = accentColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Hata mesajı
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF5252),
                            fontFamily = orbitronFont,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Giriş butonu ve bağlantılar
            AnimatedVisibility(
                visible = showElements && buttonAppeared,
                enter = fadeIn(tween(1000)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(1000)
                ),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Giriş Butonu
                    val buttonText = when {
                        loginSuccess -> "Başarılı!"
                        loginInProgress -> "Giriş Yapılıyor..."
                        else -> "Giriş Yap"
                    }
                    
                    val buttonIcon = when {
                        loginSuccess -> Icons.Default.Check
                        loginInProgress -> null
                        else -> Icons.Default.Login
                    }
                    
                    Button(
                        onClick = { performLogin() },
                        enabled = loginButtonEnabled && !loginInProgress && !loginSuccess,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                            .drawBehind {
                                if (loginButtonEnabled && !loginInProgress) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                accentColor.copy(alpha = 0.3f * getGlowFactor(0.5f)),
                                                Color.Transparent
                                            )
                                        ),
                                        cornerRadius = CornerRadius(50.dp.toPx()),
                                        alpha = 0.7f
                                    )
                                }
                            },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor.copy(alpha = 0.2f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.8f),
                                    accentColor.copy(alpha = 0.5f)
                                )
                            )
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (loginInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else if (buttonIcon != null) {
                                Icon(
                                    imageVector = buttonIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            Text(
                                text = buttonText,
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    // "Şifremi Unuttum" ve "Kayıt Ol" bağlantıları
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "Şifremi Unuttum",
                            style = TextStyle(
                                color = accentColor.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier
                                .clickable(onClick = onForgotPassword)
                                .padding(8.dp)
                        )
                        
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                                .align(Alignment.CenterVertically)
                        )
                        
                        Text(
                            text = "Kayıt Ol",
                            style = TextStyle(
                                color = accentColor.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontFamily = orbitronFont,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier
                                .clickable(onClick = onSignup)
                                .padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Üst köşe ışık efekti - daha zarif ve bütünleşik
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopStart)
                    .offset((-80).dp, (-80).dp)
                    .blur(60.dp)
                    .alpha(0.06f * getGlowFactor(0.3f))
            ) {
                // Büyük, çok yumuşak arka ışıma
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonPurple.copy(alpha = 0.4f),
                            neonPurple.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, size.height * 0.3f)
                    ),
                    center = Offset(size.width * 0.3f, size.height * 0.3f),
                    radius = size.minDimension * 0.7f
                )
                
                // İkinci katman - daha küçük ve odaklanmış
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonPurple.copy(alpha = 0.5f),
                            neonPurple.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.35f, size.height * 0.35f)
                    ),
                    center = Offset(size.width * 0.35f, size.height * 0.35f),
                    radius = size.minDimension * 0.3f
                )
            }
        }
            
        // Alt köşe ışık efekti - daha zarif ve bütünleşik
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomEnd)
                    .offset(80.dp, 80.dp)
                    .blur(70.dp)
                    .alpha(0.07f * getGlowFactor(0.7f))
            ) {
                // Ana parlama
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonBlue.copy(alpha = 0.5f),
                            neonBlue.copy(alpha = 0.2f),
                            neonBlue.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.7f, size.height * 0.7f),
                        radius = size.minDimension * 0.8f
                    ),
                    center = Offset(size.width * 0.7f, size.height * 0.7f),
                    radius = size.minDimension * 0.8f
                )
                
                // İkinci katman efekti
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            neonBlue.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = size.minDimension * 0.4f
                    ),
                    center = Offset(size.width * 0.65f, size.height * 0.65f),
                    radius = size.minDimension * 0.4f
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun OctaLoginScreenPreview() {
    OctaLoginScreen()
}