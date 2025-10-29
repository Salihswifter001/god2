package com

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OctaForgotPasswordScreen(
    onBackClick: () -> Unit = {},
    onPasswordReset: (String) -> Unit = { _ -> }
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var email by remember { mutableStateOf("") }
    var isEmailSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showScreen by remember { mutableStateOf(false) }
    
    // Animasyon değerleri
    val infiniteTransition = rememberInfiniteTransition(label = "animations")
    
    val hueShift by infiniteTransition.animateFloat(
        initialValue = 200f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hueShift"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(), 
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val loadingAnimValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading"
    )
    
    // Parçacık efektleri için değerler
    val particles = remember {
        List(25) {
            Triple(
                Math.random().toFloat(),
                Math.random().toFloat(),
                (Math.random() * 5 + 5).toFloat()
            )
        }
    }
    
    // Ekranı gösterme
    LaunchedEffect(Unit) {
        delay(100)
        showScreen = true
        delay(300)
        focusRequester.requestFocus()
    }
    
    // Ana container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Arka plan dalgalı efektler
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dönen arka plan ışıltılar
            rotate(rotation) {
                // Ana parlayan daire
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                            Color.hsv(hueShift, 0.8f, 0.3f).copy(alpha = 0.15f * glowIntensity),
                        Color.Transparent
                        ),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = center
                )
                
                // Küçük parlamalar
                repeat(5) { index ->
                    val angle = index * 72f + rotation * 0.5f
                    val distance = size.minDimension * 0.3f
                    val x = center.x + cos(angle * (PI.toFloat() / 180f)) * distance
                    val y = center.y + sin(angle * (PI.toFloat() / 180f)) * distance
                    val spotSize = size.minDimension * 0.05f * (0.7f + 0.3f * glowIntensity)
                    
            drawCircle(
                        color = Color.hsv(
                            (hueShift + index * 20) % 360, 
                            0.9f, 
                            0.7f
                        ).copy(alpha = 0.15f * glowIntensity),
                        radius = spotSize,
                        center = Offset(x, y)
                    )
                }
            }
            
            // Alt dalgalı çizgiler
            val amplitude = size.height * 0.02f
            val waveWidth = size.width * 1.5f
            val baseY = size.height * 0.9f
            
            repeat(3) { layer ->
                val layerAlpha = 0.07f - layer * 0.02f
                val layerPhase = wavePhase + layer * 0.5f
                val baseHeight = baseY - layer * size.height * 0.05f
                val path = Path()
                
                path.moveTo(0f, baseHeight)
                
                for (x in 0..size.width.toInt() step 5) {
                    val xFloat = x.toFloat()
                    val phaseShift = xFloat / waveWidth * 2f * PI.toFloat()
                    val yOffset = sin(phaseShift + layerPhase) * amplitude * (layer + 1)
                    path.lineTo(xFloat, baseHeight + yOffset)
                }
                
                path.lineTo(size.width, size.height)
                path.lineTo(0f, size.height)
                path.close()
                
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                    colors = listOf(
                            Color.hsv(hueShift, 0.8f, 0.6f).copy(alpha = layerAlpha),
                            Color.hsv(hueShift, 0.9f, 0.2f).copy(alpha = 0f)
                        )
                    )
                )
            }
        }
        
        // Parçacık efektleri
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            particles.forEach { (xPercent, yPercent, size) ->
                val x = xPercent * this.size.width
                val y = yPercent * this.size.height * 0.8f + this.size.height * 0.1f
                val radius = size * (0.7f + 0.3f * sin(wavePhase + xPercent * 5f))
                
                drawCircle(
                    color = Color.hsv(
                        (hueShift + xPercent * 30) % 360,
                        0.8f,
                        0.9f
                    ).copy(alpha = 0.2f * glowIntensity),
                    radius = radius,
                    center = Offset(
                        x + cos(wavePhase + yPercent * 10) * 15,
                        y + sin(wavePhase + xPercent * 10) * 15
                    ),
                    blendMode = BlendMode.Plus
                )
            }
        }
        
        // Geri butonu
        AnimatedVisibility(
            visible = showScreen,
            enter = fadeIn() + expandIn(
                expandFrom = Alignment.TopStart,
                animationSpec = tween(500, easing = EaseOutQuad)
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color.hsv(hueShift, 0.9f, 0.8f).copy(alpha = 0.5f)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1A1A1A),
                                Color(0xFF101010)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.hsv(hueShift, 0.7f, 0.7f).copy(alpha = 0.5f),
                                Color.hsv(hueShift, 0.5f, 0.4f).copy(alpha = 0.2f)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.hsv(hueShift, 0.5f, 0.9f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Ana içerik
        AnimatedVisibility(
            visible = showScreen,
            enter = fadeIn(tween(700)) + expandVertically(
                expandFrom = Alignment.CenterVertically,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
        Column(
            modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Logo/ikon
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dış ışıltı
                    Canvas(
                        modifier = Modifier
                            .size(140.dp)
                            .alpha(glowIntensity * 0.4f)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.hsv(hueShift, 0.8f, 0.5f).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2f
                        )
                    }
                    
                    // Logo container
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(pulseScale)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                spotColor = Color.hsv(hueShift, 0.9f, 0.8f).copy(alpha = 0.7f)
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF202020),
                                        Color(0xFF101010)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.5f.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.hsv(hueShift, 0.9f, 0.7f).copy(alpha = 0.8f),
                                        Color.hsv(hueShift + 30, 0.7f, 0.5f).copy(alpha = 0.5f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.hsv(hueShift, 0.9f, 0.8f),
                                            Color.hsv(hueShift + 20, 0.7f, 0.6f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(8.dp)
                        ) {
                            if (isEmailSent) {
                        Icon(
                            imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.LockReset,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                
                // Başlık ve açıklama
                AnimatedContent(
                    targetState = isEmailSent,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith
                        fadeOut(animationSpec = tween(500))
                    },
                    label = "HeaderContent"
                ) { sent ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                            text = if (sent) "Email Sent" else "Forgot Password",
                        fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = 0.5.sp,
                            color = Color.White,
                        textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                    Text(
                            text = if (sent) 
                                "A password reset link has been sent to your email address. Please check your email." 
                            else 
                                "Enter your registered email address to reset your password.",
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Form içeriği
            AnimatedVisibility(
                    visible = !isEmailSent,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                            Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                // E-posta giriş alanı
                        OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .height(60.dp)
                                        .shadow(
                                    elevation = 8.dp,
                                    spotColor = Color.hsv(hueShift, 0.9f, 0.7f).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.hsv(hueShift, 0.5f, 0.9f)
                                )
                            },
                            placeholder = {
                                        Text(
                                    text = "Enter your email address",
                                    color = Color.White.copy(alpha = 0.5f)
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                                    ),
                            colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                cursorColor = Color.hsv(hueShift, 0.9f, 0.9f),
                                focusedBorderColor = Color.hsv(hueShift, 0.9f, 0.8f),
                                unfocusedBorderColor = Color.hsv(hueShift, 0.5f, 0.5f).copy(alpha = 0.5f),
                                focusedLeadingIconColor = Color.hsv(hueShift, 0.9f, 0.9f),
                                unfocusedLeadingIconColor = Color.hsv(hueShift, 0.5f, 0.9f),
                                focusedLabelColor = Color.hsv(hueShift, 0.9f, 0.9f),
                                unfocusedLabelColor = Color.hsv(hueShift, 0.5f, 0.9f),
                                focusedContainerColor = Color(0xFF151515),
                                unfocusedContainerColor = Color(0xFF151515)
                            )
                        )
                        
                        // Şifre sıfırlama butonu
                        Button(
                            onClick = {
                                if (email.isNotEmpty()) {
                                    focusManager.clearFocus()
                                    coroutineScope.launch {
                                        isLoading = true
                                        delay(1500)
                                        isEmailSent = true
                                        isLoading = false
                                        onPasswordReset(email)
                                    }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                .height(56.dp)
                                        .shadow(
                                    elevation = 10.dp,
                                    spotColor = Color.hsv(hueShift, 0.9f, 0.8f).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(28.dp)
                                ),
                            shape = RoundedCornerShape(28.dp),
                            enabled = email.isNotEmpty() && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.hsv(hueShift, 0.9f, 0.3f),
                                disabledContainerColor = Color.hsv(hueShift, 0.3f, 0.2f)
                            )
                        ) {
                            Box(
                                    modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Reset Password",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.5.sp
                                    )
                }
            }
        }
    }
}

                // Başarı ekranı için buton
                AnimatedVisibility(
                    visible = isEmailSent,
                    enter = fadeIn(tween(500, delayMillis = 300)) + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
    ) {
        Button(
                        onClick = onBackClick,
            modifier = Modifier
                            .fillMaxWidth()
                .height(56.dp)
                .shadow(
                                elevation = 10.dp,
                                spotColor = Color.hsv(hueShift, 0.9f, 0.8f).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                            containerColor = Color.hsv(hueShift, 0.9f, 0.3f)
                        )
                    ) {
                    Text(
                            text = "Return to Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun OctaForgotPasswordScreenPreview() {
    OctaForgotPasswordScreen()
} 