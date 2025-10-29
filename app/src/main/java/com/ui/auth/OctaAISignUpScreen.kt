package com.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.utils.ValidationUtils
import com.utils.PasswordStrength
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.ui.components.BlackHoleBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OctaAISignUpScreen(
    onSignUpSuccess: (String, String) -> Unit = { _, _ -> },
    onLoginClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var passwordStrength by remember { mutableStateOf(PasswordStrength.VERY_WEAK) }
    var showPasswordHints by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Animation for button scale
    var isButtonHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isButtonHovered) 1.05f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "button_scale"
    )
    
    // Password strength color
    val passwordStrengthColor = when (passwordStrength) {
        PasswordStrength.VERY_WEAK -> Color(0xFFEF4444)
        PasswordStrength.WEAK -> Color(0xFFF97316)
        PasswordStrength.MEDIUM -> Color(0xFFEAB308)
        PasswordStrength.STRONG -> Color(0xFF22C55E)
        PasswordStrength.VERY_STRONG -> Color(0xFF10B981)
    }
    
    // Validate password on change
    LaunchedEffect(password) {
        if (password.isNotEmpty()) {
            val result = ValidationUtils.isValidPassword(password)
            passwordStrength = result.strength
            showPasswordHints = !result.isValid
        } else {
            showPasswordHints = false
        }
    }
    
    // Sign up function with validation
    fun performSignUp() {
        coroutineScope.launch {
            // Clear previous messages
            errorMessage = null
            successMessage = null
            
            // Validate inputs
            when {
                fullName.isBlank() -> {
                    errorMessage = "Please enter your name"
                    return@launch
                }
                fullName.length < 2 -> {
                    errorMessage = "Name must be at least 2 characters"
                    return@launch
                }
                !ValidationUtils.isValidEmail(email) -> {
                    errorMessage = "Please enter a valid email address"
                    return@launch
                }
                password.isEmpty() -> {
                    errorMessage = "Please enter a password"
                    return@launch
                }
                password != confirmPassword -> {
                    errorMessage = "Passwords do not match"
                    return@launch
                }
            }
            
            // Validate password strength
            val passwordResult = ValidationUtils.isValidPassword(password)
            if (!passwordResult.isValid) {
                errorMessage = passwordResult.errors.firstOrNull() ?: "Invalid password"
                return@launch
            }
            
            // Validate username from fullName
            val username = fullName.replace(" ", "").lowercase()
            val usernameResult = ValidationUtils.isValidUsername(username)
            if (!usernameResult.isValid && username.length >= 3) {
                // Generate a valid username
                val generatedUsername = fullName.filter { it.isLetterOrDigit() }.take(20).lowercase()
                if (generatedUsername.length < 3) {
                    errorMessage = "Could not create a valid username"
                    return@launch
                }
            }
            
            isLoading = true
            
            try {
                // Supabase registration with enhanced security
                val supabaseManager = SupabaseManager()
                val sanitizedEmail = ValidationUtils.sanitizeInput(email)
                val sanitizedName = ValidationUtils.sanitizeInput(fullName)
                
                val result = supabaseManager.register(
                    email = sanitizedEmail,
                    username = sanitizedName,
                    password = password
                )
                
                if (result.isSuccess) {
                    successMessage = "Registration successful! Please check your email."
                    delay(2000)
                    onSignUpSuccess(email, password)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Registration failed"
                    errorMessage = when {
                        error.contains("already", ignoreCase = true) -> 
                            "This email address is already in use"
                        error.contains("invalid", ignoreCase = true) -> 
                            "Invalid email address or password"
                        error.contains("weak", ignoreCase = true) -> 
                            "Password is too weak, please choose a stronger password"
                        error.contains("network", ignoreCase = true) -> 
                            "Please check your internet connection"
                        else -> "An error occurred during registration"
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Logo and title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "OctaAI",
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-1.5).sp
                    )
                )
                Text(
                    text = "Register to start your music journey",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Success message
            if (successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.5f),
                                Color(0xFF10B981).copy(alpha = 0.3f)
                            )
                        )
                    )
                ) {
                    Text(
                        text = successMessage!!,
                        color = Color(0xFF10B981),
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFEF4444).copy(alpha = 0.5f),
                                Color(0xFFEF4444).copy(alpha = 0.3f)
                            )
                        )
                    )
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF4444),
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Input fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Full Name Input
                NeonTextFieldSignUp(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Full Name",
                    leadingIcon = Icons.Outlined.Person,
                    keyboardType = KeyboardType.Text,
                    focusColor = Color(0xFFE100FF),
                    enabled = !isLoading
                )
                
                // Email Input
                NeonTextFieldSignUp(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    focusColor = Color(0xFFE100FF),
                    enabled = !isLoading
                )
                
                // Password Input with visibility toggle
                NeonTextFieldSignUp(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (passwordVisible) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    focusColor = Color(0xFF007BFF),
                    enabled = !isLoading,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) 
                                    Icons.Outlined.VisibilityOff 
                                else 
                                    Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) 
                                    "Hide password" 
                                else 
                                    "Show password",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }
                )
                
                // Password strength indicator
                if (password.isNotEmpty()) {
                    Column {
                        LinearProgressIndicator(
                            progress = when (passwordStrength) {
                                PasswordStrength.VERY_WEAK -> 0.2f
                                PasswordStrength.WEAK -> 0.4f
                                PasswordStrength.MEDIUM -> 0.6f
                                PasswordStrength.STRONG -> 0.8f
                                PasswordStrength.VERY_STRONG -> 1.0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = passwordStrengthColor,
                            trackColor = Color(0xFF374151)
                        )
                        Text(
                            text = when (passwordStrength) {
                                PasswordStrength.VERY_WEAK -> "Very Weak"
                                PasswordStrength.WEAK -> "Weak"
                                PasswordStrength.MEDIUM -> "Medium"
                                PasswordStrength.STRONG -> "Strong"
                                PasswordStrength.VERY_STRONG -> "Very Strong"
                            },
                            color = passwordStrengthColor,
                            style = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Password hints
                if (showPasswordHints) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF374151).copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Password requirements:",
                                color = Color(0xFF9CA3AF),
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            )
                            val requirements = listOf(
                                "At least 8 characters",
                                "At least one uppercase letter",
                                "At least one lowercase letter", 
                                "At least one number",
                                "At least one special character (@#$%^&+=!?*)"
                            )
                            requirements.forEach { req ->
                                Text(
                                    text = "• $req",
                                    color = Color(0xFF9CA3AF),
                                    style = TextStyle(fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
                
                // Confirm Password Input
                NeonTextFieldSignUp(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm Password",
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (confirmPasswordVisible) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    focusColor = Color(0xFF007BFF),
                    enabled = !isLoading,
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) 
                                    Icons.Outlined.VisibilityOff 
                                else 
                                    Icons.Outlined.Visibility,
                                contentDescription = if (confirmPasswordVisible) 
                                    "Hide password" 
                                else 
                                    "Show password",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }
                )
            }
            
            // Sign Up Button
            Button(
                onClick = { performSignUp() },
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
                enabled = !isLoading && fullName.isNotBlank() && 
                         email.isNotBlank() && password.isNotBlank() && 
                         confirmPassword.isNotBlank()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (!isLoading) listOf(
                                    Color(0xFFE100FF),
                                    Color(0xFF007BFF)
                                ) else listOf(
                                    Color(0xFF6B7280),
                                    Color(0xFF4B5563)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 0f)
                            )
                        )
                        .clip(CircleShape),
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
            
            // Login Link
            Row(
                modifier = Modifier.padding(top = 24.dp, bottom = 48.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                )
                TextButton(
                    onClick = { if (!isLoading) onNavigateToLogin() },
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Sign In",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE100FF),
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SignUpBackgroundGradients() {
    // Left top gradient (Pink)
    Box(
        modifier = Modifier
            .offset(x = (-100).dp, y = (-200).dp)
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
    
    // Right bottom gradient (Blue)
    Box(
        modifier = Modifier
            .offset(x = 200.dp, y = 600.dp)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeonTextFieldSignUp(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusColor: Color,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
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
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = trailingIcon,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF111827).copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = focusColor,
            unfocusedBorderColor = Color.Transparent,
            containerColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = focusColor,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = Color(0xFF6B7280)
        ),
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        singleLine = true,
        enabled = enabled
    )
}

