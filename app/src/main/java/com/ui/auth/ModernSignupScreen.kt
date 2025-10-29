package com.ui.auth
import com.settings.SessionManager

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.utils.ValidationUtils
import com.utils.PasswordStrength
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun ModernSignupScreen(
    onSignupSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    // State management
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) } // 1: Basic info, 2: Password
    
    // Password strength
    var passwordStrength by remember { mutableStateOf(PasswordStrength.VERY_WEAK) }
    var passwordErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Animation states
    var contentVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Focus management
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    
    // Context
    val context = LocalContext.current
    // OctaApplication kaldırıldı - artık gerekli değil
    
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Wave animation for background
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAnimation"
    )
    
    // Color transition
    val colorTransition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorAnimation"
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }
    
    // Check password strength
    LaunchedEffect(password) {
        if (password.isNotEmpty()) {
            val result = ValidationUtils.isValidPassword(password)
            passwordStrength = result.strength
            passwordErrors = result.errors
        }
    }
    
    // Signup function
    fun performSignup() {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            // Validation
            val usernameValidation = ValidationUtils.isValidUsername(username)
            if (!usernameValidation.isValid) {
                errorMessage = usernameValidation.errors.firstOrNull()
                isLoading = false
                return@launch
            }
            
            if (!ValidationUtils.isValidEmail(email)) {
                errorMessage = "Please enter a valid email address"
                isLoading = false
                return@launch
            }
            
            val passwordValidation = ValidationUtils.isValidPassword(password)
            if (!passwordValidation.isValid) {
                errorMessage = passwordValidation.errors.firstOrNull()
                isLoading = false
                return@launch
            }
            
            if (password != confirmPassword) {
                errorMessage = "Passwords do not match"
                isLoading = false
                return@launch
            }
            
            if (!agreedToTerms) {
                errorMessage = "You must accept the terms and conditions"
                isLoading = false
                return@launch
            }
            
            try {
                val supabaseManager = SupabaseManager()
                val result = supabaseManager.register(email, username, password)
                
                if (result.isSuccess) {
                    successMessage = "Registration successful! You can now sign in."
                    delay(2000)
                    onSignupSuccess()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                }
            } catch (e: Exception) {
                errorMessage = "Bağlantı hatası: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Animated wave background
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            
            // Gradient background with color transition
            val topColor = lerp(
                Color(0xFF1E3A5F),
                Color(0xFF432371),
                colorTransition
            )
            val bottomColor = lerp(
                Color(0xFF432371),
                Color(0xFF0A0E27),
                colorTransition
            )
            
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
            
            // Animated waves
            val waveHeight = 150f
            val waveCount = 3
            
            for (i in 0 until waveCount) {
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(0f, height * 0.6f)
                
                for (x in 0..width.toInt() step 10) {
                    val y = height * 0.6f + waveHeight * sin(
                        (x / width) * 4 * PI + waveOffset + i * PI / 3
                    ).toFloat()
                    path.lineTo(x.toFloat(), y)
                }
                
                path.lineTo(width, height)
                path.lineTo(0f, height)
                path.close()
                
                drawPath(
                    path = path,
                    color = Color(0xFF6366F1).copy(
                        alpha = 0.1f - i * 0.02f
                    )
                )
            }
            
            // Floating particles
            val particleCount = 20
            for (i in 0 until particleCount) {
                val x = (width * (i.toFloat() / particleCount)) + 
                       50f * cos(waveOffset + i * PI / 10).toFloat()
                val y = height * 0.2f + 
                       100f * sin(waveOffset * 2 + i * PI / 5).toFloat()
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = 3f + 2f * sin(waveOffset + i).toFloat(),
                    center = Offset(x, y)
                )
            }
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(1000)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Text(
                        text = "Create Account",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                    
                    Text(
                        text = "Begin your music creation journey",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Progress indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(2) { index ->
                            val step = index + 1
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (step <= currentStep) Color(0xFF6366F1)
                                        else Color.White.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = step.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (index < 1) {
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(60.dp)
                                        .align(Alignment.CenterVertically)
                                        .background(
                                            if (currentStep > 1) Color(0xFF6366F1)
                                            else Color.White.copy(alpha = 0.2f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Form card with glassmorphism
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(1200, delayMillis = 200)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(1200, delayMillis = 200)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        AnimatedContent(
                            targetState = currentStep,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                } else {
                                    slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                                }.using(SizeTransform(clip = false))
                            }
                        ) { step ->
                            when (step) {
                                1 -> {
                                    // Step 1: Basic Information
                                    Column {
                                        // Username field
                                        OutlinedTextField(
                                            value = username,
                                            onValueChange = { 
                                                username = it
                                                errorMessage = null
                                            },
                                            label = { Text("Username", color = Color.White.copy(alpha = 0.8f)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.Person,
                                                    contentDescription = "Username",
                                                    tint = Color.White.copy(alpha = 0.7f)
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(usernameFocusRequester),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF6366F1),
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                                cursorColor = Color(0xFF6366F1),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White.copy(alpha = 0.9f)
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onNext = { emailFocusRequester.requestFocus() }
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true,
                                            supportingText = {
                                                Text(
                                                    "3-30 characters, letters and numbers",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        // Email field
                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = { 
                                                email = it
                                                errorMessage = null
                                            },
                                            label = { Text("Email", color = Color.White.copy(alpha = 0.8f)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.Email,
                                                    contentDescription = "Email",
                                                    tint = Color.White.copy(alpha = 0.7f)
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(emailFocusRequester),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF6366F1),
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                                cursorColor = Color(0xFF6366F1),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White.copy(alpha = 0.9f)
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Email,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onDone = { 
                                                    focusManager.clearFocus()
                                                    if (username.isNotEmpty() && email.isNotEmpty()) {
                                                        currentStep = 2
                                                    }
                                                }
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true
                                        )
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        // Next button
                                        Button(
                                            onClick = { currentStep = 2 },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            enabled = username.isNotEmpty() && email.isNotEmpty(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF6366F1),
                                                disabledContainerColor = Color(0xFF6366F1).copy(alpha = 0.5f)
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                text = "Continue",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                Icons.Default.ArrowForward,
                                                contentDescription = "Next",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                                
                                2 -> {
                                    // Step 2: Password Setup
                                    Column {
                                        // Password field
                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { 
                                                password = it
                                                errorMessage = null
                                            },
                                            label = { Text("Password", color = Color.White.copy(alpha = 0.8f)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.Lock,
                                                    contentDescription = "Password",
                                                    tint = Color.White.copy(alpha = 0.7f)
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                    Icon(
                                                        if (passwordVisible) Icons.Outlined.Visibility 
                                                        else Icons.Outlined.VisibilityOff,
                                                        contentDescription = "Toggle visibility",
                                                        tint = Color.White.copy(alpha = 0.5f)
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(passwordFocusRequester),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF6366F1),
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                                cursorColor = Color(0xFF6366F1),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White.copy(alpha = 0.9f)
                                            ),
                                            visualTransformation = if (passwordVisible) 
                                                VisualTransformation.None 
                                            else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Password,
                                                imeAction = ImeAction.Next
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onNext = { confirmPasswordFocusRequester.requestFocus() }
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true
                                        )
                                        
                                        // Password strength indicator
                                        if (password.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            PasswordStrengthIndicator(passwordStrength)
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        // Confirm password field
                                        OutlinedTextField(
                                            value = confirmPassword,
                                            onValueChange = { 
                                                confirmPassword = it
                                                errorMessage = null
                                            },
                                            label = { Text("Confirm Password", color = Color.White.copy(alpha = 0.8f)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.Lock,
                                                    contentDescription = "Confirm Password",
                                                    tint = Color.White.copy(alpha = 0.7f)
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                                    Icon(
                                                        if (confirmPasswordVisible) Icons.Outlined.Visibility 
                                                        else Icons.Outlined.VisibilityOff,
                                                        contentDescription = "Toggle visibility",
                                                        tint = Color.White.copy(alpha = 0.5f)
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(confirmPasswordFocusRequester),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF6366F1),
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                                cursorColor = Color(0xFF6366F1),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                                                errorBorderColor = Color(0xFFEF4444)
                                            ),
                                            visualTransformation = if (confirmPasswordVisible) 
                                                VisualTransformation.None 
                                            else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Password,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onDone = { focusManager.clearFocus() }
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true,
                                            isError = confirmPassword.isNotEmpty() && password != confirmPassword
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        // Terms and conditions
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = agreedToTerms,
                                                onCheckedChange = { agreedToTerms = it },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = Color(0xFF6366F1),
                                                    uncheckedColor = Color.White.copy(alpha = 0.5f),
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                            Text(
                                                text = "I accept the terms and conditions and privacy policy",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Back button
                                            OutlinedButton(
                                                onClick = { currentStep = 1 },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(56.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color.White
                                                ),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowBack,
                                                    contentDescription = "Back",
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Back",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            
                                            // Sign up button
                                            Button(
                                                onClick = { performSignup() },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(56.dp),
                                                enabled = password.isNotEmpty() && 
                                                         password == confirmPassword && 
                                                         agreedToTerms && 
                                                         !isLoading,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF6366F1),
                                                    disabledContainerColor = Color(0xFF6366F1).copy(alpha = 0.5f)
                                                ),
                                                shape = RoundedCornerShape(16.dp)
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
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Error/Success messages
                        AnimatedVisibility(
                            visible = errorMessage != null || successMessage != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (successMessage != null) 
                                        Color(0xFF10B981).copy(alpha = 0.2f)
                                    else Color(0xFFEF4444).copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(
                                    1.dp, 
                                    if (successMessage != null) 
                                        Color(0xFF10B981).copy(alpha = 0.5f)
                                    else Color(0xFFEF4444).copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (successMessage != null) Icons.Default.CheckCircle
                                        else Icons.Default.Error,
                                        contentDescription = "Status",
                                        tint = if (successMessage != null) 
                                            Color(0xFF10B981) 
                                        else Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = successMessage ?: errorMessage ?: "",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login prompt
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(1400, delayMillis = 400))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = onBackToLogin
                    ) {
                        Text(
                            text = "Sign In",
                            color = Color(0xFF818CF8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val color = when (strength) {
        PasswordStrength.VERY_WEAK -> Color(0xFFEF4444)
        PasswordStrength.WEAK -> Color(0xFFF59E0B)
        PasswordStrength.MEDIUM -> Color(0xFFFBBF24)
        PasswordStrength.STRONG -> Color(0xFF84CC16)
        PasswordStrength.VERY_STRONG -> Color(0xFF10B981)
    }
    
    val text = when (strength) {
        PasswordStrength.VERY_WEAK -> "Very Weak"
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
        PasswordStrength.VERY_STRONG -> "Very Strong"
    }
    
    val progress = when (strength) {
        PasswordStrength.VERY_WEAK -> 0.2f
        PasswordStrength.WEAK -> 0.4f
        PasswordStrength.MEDIUM -> 0.6f
        PasswordStrength.STRONG -> 0.8f
        PasswordStrength.VERY_STRONG -> 1f
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Password Strength:",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

// Lerp function for color interpolation
fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}