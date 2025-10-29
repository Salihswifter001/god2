package com.ui.auth
import com.settings.SessionManager

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.utils.ValidationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ui.components.BlackHoleBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OctaAILoginScreen(
    onLoginSuccess: (email: String, password: String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    // State management
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isButtonHovered by remember { mutableStateOf(false) }
    
    // Focus management
    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    
    // Context for Supabase
    val context = LocalContext.current
    // OctaApplication kaldırıldı - artık gerekli değil
    val scope = rememberCoroutineScope()
    
    // Animation for button scale
    val scale by animateFloatAsState(
        targetValue = if (isButtonHovered || isLoading) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )
    
    // Login function with validation
    fun performLogin() {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            // Validation
            if (!ValidationUtils.isValidEmail(email)) {
                errorMessage = "Please enter a valid email address"
                isLoading = false
                return@launch
            }
            
            if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters"
                isLoading = false
                return@launch
            }
            
            try {
                // Supabase login
                val supabaseManager = SupabaseManager()
                val result = supabaseManager.login(email, password, SessionManager(context))
                
                if (result.isSuccess) {
                    delay(300) // Success animation delay
                    onLoginSuccess(email, password)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Login failed"
                    errorMessage = when {
                        error.contains("Invalid login") -> "Invalid email or password"
                        error.contains("Email not confirmed") -> "Please verify your email"
                        error.contains("Network") -> "İnternet bağlantınızı kontrol edin"
                        else -> error
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Beklenmeyen bir hata oluştu: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Black hole background animation
        BlackHoleBackground(
            modifier = Modifier.fillMaxSize()
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo and title section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                // Animated logo
                val infiniteTransition = rememberInfiniteTransition(label = "logo")
                val logoScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "logoScale"
                )
                
                Text(
                    text = "OctaAI",
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-1.5).sp
                    ),
                    modifier = Modifier.scale(logoScale)
                )
                
                Text(
                    text = "Enter the future of music",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Input fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Email Input
                NeonTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        errorMessage = null
                    },
                    placeholder = "Email",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onNext = { passwordFocusRequester.requestFocus() },
                    focusColor = Color(0xFFE100FF), // Pink
                    focusRequester = emailFocusRequester,
                    isError = errorMessage != null && email.isNotEmpty()
                )
                
                // Password Input
                NeonTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null
                    },
                    placeholder = "Password",
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onDone = { 
                        focusManager.clearFocus()
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            performLogin()
                        }
                    },
                    visualTransformation = if (passwordVisible) 
                        VisualTransformation.None 
                    else PasswordVisualTransformation(),
                    focusColor = Color(0xFF007BFF), // Blue
                    focusRequester = passwordFocusRequester,
                    isError = errorMessage != null && password.isNotEmpty(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) 
                                    Icons.Outlined.Visibility
                                else Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }
                )
            }
            
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Forgot Password
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Forgot Password",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
                )
            }
            
            // Login Button
            Button(
                onClick = { 
                    if (!isLoading) {
                        performLogin()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .height(56.dp)
                    .scale(scale)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(),
                enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isLoading) {
                                    listOf(
                                        Color(0xFFE100FF).copy(alpha = 0.6f),
                                        Color(0xFF007BFF).copy(alpha = 0.6f)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFFE100FF),
                                        Color(0xFF007BFF)
                                    )
                                },
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 0f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
            
            // Sign Up Section
            Column(
                modifier = Modifier.padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Don't have an account?",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
                )
                
                // Sign Up Button - Similar to Login but different color
                Button(
                    onClick = onSignUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1A1A1A), // Siyah
                                        Color(0xFF9D00FF), // Neon Mor
                                        Color(0xFFE100FF)  // Parlak Neon Mor
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedBackgroundGradients() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Animated offset for gradients
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    // Top-left gradient (Pink)
    Box(
        modifier = Modifier
            .offset(x = (-100 + offsetX).dp, y = (-200 + offsetY).dp)
            .size(400.dp)
            .blur(150.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE100FF).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
    
    // Bottom-right gradient (Blue)
    Box(
        modifier = Modifier
            .offset(x = (200 - offsetX).dp, y = (600 - offsetY).dp)
            .size(400.dp)
            .blur(150.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF007BFF).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
    
    // Center gradient (Purple)
    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = (300 + offsetY).dp)
            .size(300.dp)
            .blur(120.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onNext: () -> Unit = {},
    onDone: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusColor: Color,
    focusRequester: FocusRequester? = null,
    isError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF6B7280)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isFocused) focusColor else Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = trailingIcon,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .background(
                color = if (isError) {
                    Color(0xFFEF4444).copy(alpha = 0.05f)
                } else {
                    Color(0xFF111827).copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(12.dp)
            ),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = if (isError) Color(0xFFEF4444) else focusColor,
            unfocusedBorderColor = if (isError) {
                Color(0xFFEF4444).copy(alpha = 0.3f)
            } else Color.Transparent,
            containerColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = focusColor
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() },
            onDone = { onDone() }
        ),
        visualTransformation = visualTransformation,
        singleLine = true
    )
}