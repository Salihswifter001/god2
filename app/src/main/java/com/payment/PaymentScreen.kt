package com.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ModernBlurredBackground
import com.NeonGlowButton
import com.SidebarItem
import com.NeonSidebar
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.updateMembershipType

/**
 * Screen for processing payments
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PaymentScreen(
    planName: String,
    planPrice: String,
    onPaymentSuccess: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMembership: () -> Unit = {},
    navController: NavController
) {
    // Temel değişkenler
    val poppinsFont = FontFamily.SansSerif
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val supabaseManager = remember { SupabaseManager() }
    
    // Form durumları
    var cardNumber by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }
    var paymentInProgress by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Animasyon durumları
    var showElements by remember { mutableStateOf(false) }
    
    // Animasyon değişkenleri
    val infiniteTransition = rememberInfiniteTransition(label = "paymentTransition")
    
    val hue by infiniteTransition.animateFloat(
        initialValue = 230f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hue"
    )
    
    // UI öğelerini aşamalı gösterme
    LaunchedEffect(Unit) {
        delay(300)
        showElements = true
    }
    
    // Form validasyonu
    LaunchedEffect(cardNumber, cardHolderName, expiryDate, cvv) {
        isFormValid = cardNumber.length >= 16 && 
                     cardHolderName.isNotBlank() && 
                     expiryDate.length == 5 && 
                     cvv.length >= 3
    }
    
    // Ana ekran
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030712),  // Koyu siyah
                        Color(0xFF101530),  // Koyu mavi-mor
                        Color(0xFF202045)   // Orta koyu mor
                    )
                )
            )
    ) {
        // Animasyonlu arkaplan
        ModernBlurredBackground()
        
        // Sidebar
        val sidebarItems = listOf(
            SidebarItem(Icons.Filled.Person, "Profile", "profile"),
            SidebarItem(Icons.Filled.MusicNote, "Create Music", "music"),
            SidebarItem(Icons.Filled.LibraryMusic, "My Music", "my_music"),
            SidebarItem(Icons.Filled.CardMembership, "Membership", "membership"),
            SidebarItem(Icons.Filled.Info, "About", "about")
        )
        
        var sidebarExpanded by remember { mutableStateOf(true) }
        
        NeonSidebar(
            items = sidebarItems,
            selectedRoute = "membership",
            onItemClick = { route -> 
                when (route) {
                    "profile" -> onNavigateToProfile()
                    "music" -> navController.navigate("music_creator")
                    "my_music" -> navController.navigate("my_music")
                    "about" -> navController.navigate("about")
                    "membership" -> onNavigateToMembership()
                    "videos" -> navController.navigate("videos")
                    "home" -> navController.navigate("home")
                }
            },
            modifier = Modifier.align(Alignment.CenterStart),
            isExpanded = sidebarExpanded,
            onToggleExpand = { sidebarExpanded = !sidebarExpanded }
        )
        
        // Ana içerik
        val contentPadding = if (sidebarExpanded) 90.dp else 40.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = contentPadding, end = 24.dp, top = 24.dp, bottom = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst kısım - Başlık ve simge
            AnimatedVisibility(
                visible = showElements,
                enter = fadeIn(tween(1000)) + 
                       slideInVertically(
                           initialOffsetY = { -50 },
                           animationSpec = tween(1000, easing = EaseOutQuad)
                       ),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Kredi kartı ikonu
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = Color.hsv(hue, 0.8f, 0.8f)
                            )
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.hsv(hue, 0.3f, 0.3f),
                                        Color.hsv(hue, 0.2f, 0.1f)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.hsv(hue, 0.8f, 0.8f),
                                        Color.hsv(hue + 30, 0.8f, 0.6f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = "Payment",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Ana başlık
                    Text(
                        text = "PAYMENT SCREEN",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontFamily = poppinsFont,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.hsv(hue, 0.5f, 0.9f)
                                )
                            )
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Plan bilgisi
                    Text(
                        text = "$planName Planı - $planPrice",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = poppinsFont,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Ödeme formu
            AnimatedVisibility(
                visible = showElements,
                enter = fadeIn(tween(1000, delayMillis = 300)) + 
                       expandVertically(tween(800, delayMillis = 300, easing = EaseOutQuad)),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.hsv(hue, 0.2f, 0.1f).copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.hsv(hue, 0.8f, 0.8f).copy(alpha = 0.5f),
                                Color.hsv(hue + 30, 0.8f, 0.6f).copy(alpha = 0.3f)
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Kart numarası
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { 
                                if (it.length <= 16 && it.all { char -> char.isDigit() }) {
                                    cardNumber = it
                                }
                            },
                            label = { Text("Card Number") },
                            placeholder = { Text("XXXX XXXX XXXX XXXX") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CreditCard,
                                    contentDescription = null,
                                    tint = Color.hsv(hue, 0.5f, 0.9f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.hsv(hue, 0.5f, 0.9f),
                                focusedLabelColor = Color.hsv(hue, 0.5f, 0.9f),
                                cursorColor = Color.hsv(hue, 0.5f, 0.9f),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                unfocusedLabelColor = Color.Gray.copy(alpha = 0.7f)
                            ),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )
                        
                        // Kart sahibi adı
                        OutlinedTextField(
                            value = cardHolderName,
                            onValueChange = { cardHolderName = it },
                            label = { Text("Cardholder Name") },
                            placeholder = { Text("Full Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Color.hsv(hue, 0.5f, 0.9f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.hsv(hue, 0.5f, 0.9f),
                                focusedLabelColor = Color.hsv(hue, 0.5f, 0.9f),
                                cursorColor = Color.hsv(hue, 0.5f, 0.9f),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                unfocusedLabelColor = Color.Gray.copy(alpha = 0.7f)
                            ),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )
                        
                        // Son kullanma tarihi ve CVV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Son kullanma tarihi
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { 
                                    // Format: MM/YY
                                    if (it.length <= 5) {
                                        var formattedDate = it.filter { char -> char.isDigit() || char == '/' }
                                        
                                        if (formattedDate.length == 2 && !formattedDate.contains("/") && 
                                            expiryDate.length < formattedDate.length) {
                                            formattedDate = "$formattedDate/"
                                        }
                                        
                                        expiryDate = formattedDate
                                    }
                                },
                                label = { Text("Expiry Date") },
                                placeholder = { Text("MM/YY") },
                                modifier = Modifier
                                    .weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.hsv(hue, 0.5f, 0.9f),
                                    focusedLabelColor = Color.hsv(hue, 0.5f, 0.9f),
                                    cursorColor = Color.hsv(hue, 0.5f, 0.9f),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    unfocusedLabelColor = Color.Gray.copy(alpha = 0.7f)
                                ),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true
                            )
                            
                            // CVV
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { 
                                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                        cvv = it
                                    }
                                },
                                label = { Text("CVV") },
                                placeholder = { Text("XXX") },
                                modifier = Modifier
                                    .weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.hsv(hue, 0.5f, 0.9f),
                                    focusedLabelColor = Color.hsv(hue, 0.5f, 0.9f),
                                    cursorColor = Color.hsv(hue, 0.5f, 0.9f),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    unfocusedLabelColor = Color.Gray.copy(alpha = 0.7f)
                                ),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Ödeme butonu
                        NeonGlowButton(
                            text = if (paymentInProgress) "PROCESSING..." else "PAY NOW",
                            onClick = {
                                if (!paymentInProgress && isFormValid) {
                                    // Ödeme işlemini başlat
                                    paymentInProgress = true
                                    coroutineScope.launch {
                                        // Gerçek bir ödeme işlemi bu kısımda yapılabilir
                                        // Şimdilik bir gecikme ile simüle ediyoruz
                                        delay(2000)
                                        
                                        // Varsayalım ki ödeme başarılı oldu
                                        // Üyelik planını güncelle
                                        val currentUser = supabaseManager.getCurrentUser()
                                        if (currentUser != null) {
                                            supabaseManager.updateMembershipType(
                                                currentUser.id, 
                                                when (planName) {
                                                    "PRO" -> "Pro"
                                                    "MAX" -> "Max"
                                                    else -> "Standard"
                                                }
                                            )
                                        }
                                        
                                        paymentInProgress = false
                                        showSuccessDialog = true
                                    }
                                }
                            },
                            enabled = isFormValid && !paymentInProgress,
                            fontFamily = poppinsFont,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Geri dön butonu
                        TextButton(
                            onClick = { 
                                navController.popBackStack() 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "BACK",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    fontFamily = poppinsFont
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Güvenlik bilgileri
            AnimatedVisibility(
                visible = showElements,
                enter = fadeIn(tween(1000, delayMillis = 600)) + 
                       expandVertically(tween(800, delayMillis = 600, easing = EaseOutQuad)),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GÜVENLI ÖDEME",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = poppinsFont,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.hsv(hue, 0.5f, 0.9f),
                            textAlign = TextAlign.Center
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "All payment transactions are protected with 256-bit SSL encryption. Your card information is stored and processed securely.",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = poppinsFont,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Ödeme yöntemi ikonları
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ödeme yöntemi ikonları (gerçek uygulamada logo resimleri kullanılabilir)
                        Icon(
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = "Visa",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(32.dp)
                                .padding(horizontal = 8.dp)
                        )
                        
                        Icon(
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = "Mastercard",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(32.dp)
                                .padding(horizontal = 8.dp)
                        )
                        
                        Icon(
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = "American Express",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(32.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Başarılı ödeme dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Payment Successful!",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            text = {
                Text(
                    text = "Your $planName plan has been successfully activated. You can start using it right away!",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        onPaymentSuccess()
                        // Genellikle profil sayfasına yönlendirir
                        onNavigateToProfile()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.hsv(hue, 0.7f, 0.7f)
                    )
                ) {
                    Text("TAMAM")
                }
            },
            containerColor = Color(0xFF1A1A2E),
            titleContentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
} 