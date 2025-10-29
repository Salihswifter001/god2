package com

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.aihackathonkarisacikartim.god2.UserDetails
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.sin
import io.github.jan.supabase.gotrue.user.UserInfo

/**
 * Kullanıcının profil fotoğrafını görüntülemek, yüklemek ve yönetmek için bileşen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoManager() {
    // State değişkenleri
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isPhotoLoading by remember { mutableStateOf(false) }
    var isPhotoUploading by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var selectedSource by remember { mutableStateOf<ImageSource?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showElements by remember { mutableStateOf(false) }
    
    // Kullanıcı ve Supabase yöneticisi
    val context = LocalContext.current
    val supabaseManager = remember { SupabaseManager() }
    val coroutineScope = rememberCoroutineScope()
    
    // Kullanıcı bilgileri ve profil fotoğrafı
    var currentUser by remember { mutableStateOf<UserInfo?>(null) }
    var userDetails by remember { mutableStateOf<UserDetails?>(null) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Animasyon değişkenleri
    val infiniteTransition = rememberInfiniteTransition(label = "photoEffects")
    val hue by infiniteTransition.animateFloat(
        initialValue = 200f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
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
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Glow faktörü
    val getGlowFactor = { offset: Float ->
        val factor = (sin((PI * 2 * (glowIntensity + offset) % 1).toDouble()).toFloat() + 1f) / 2f * 0.6f + 0.4f
        factor.coerceIn(0.0f, 1.0f)
    }
    
    // Orbitron font tanımı
    val orbitronFont = FontFamily.SansSerif
    
    // Dosya oluşturmak için gerekli fonksiyon
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir("images")
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    // Galeriden fotoğraf seçme
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                photoUri = it
                selectedSource = ImageSource.GALLERY
                showOptions = false
            }
        }
    )
    
    // Kameradan fotoğraf çekme
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                cameraUri?.let {
                    photoUri = it
                    selectedSource = ImageSource.CAMERA
                    showOptions = false
                }
            }
        }
    )
    
    // Kullanıcı bilgilerini yükle
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val user = supabaseManager.getCurrentUser()
                currentUser = user
                if (user != null) {
                    val details = supabaseManager.getUserDetails(user.id)
                    userDetails = details
                    avatarUrl = details?.avatar_url
                    println("DEBUG: Avatar URL: $avatarUrl")
                } else {
                    errorMessage = "Kullanıcı bulunamadı."
                }
            } catch (e: Exception) {
                errorMessage = "Kullanıcı bilgileri alınırken hata oluştu: ${e.message}"
                Log.e("UserProfilePic", "Error fetching user data", e)
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
        
        // UI elementlerini göster
        delay(300)
        showElements = true
    }
    
    // Fotoğraf yükleme fonksiyonu
    suspend fun uploadPhoto(uri: Uri): String? {
        return try {
            isPhotoUploading = true
            uploadProgress = 0f
            
            // Fotoğrafı yüklenebilir boyuta getir
            val compressedPhotoBytes = withContext(Dispatchers.IO) {
                val photoInputStream = context.contentResolver.openInputStream(uri)
                compressPhoto(photoInputStream, 800)
            }
            
            uploadProgress = 0.3f
            
            // Supabase Storage objesini al
            val client = supabaseManager.getSupabaseClient()
            
            // Benzersiz dosya adı oluştur
            val userId = currentUser?.id ?: return null
            val fileName = "avatar_${userId}_${System.currentTimeMillis()}.jpg"
            
            // "avatars" adlı bucket'a yükle
            val bucketName = "avatars"
            
            // Bucket'ın varlığını kontrol et ve gerekirse oluştur
            try {
                // Supabase 1.4.1'de bucket işlemleri için sadece bilgi amaçlı
                println("DEBUG: Using bucket: $bucketName")
            } catch (e: Exception) {
                println("DEBUG: Creating bucket error: ${e.message}")
            }
            
            uploadProgress = 0.5f
            
            // Dosyayı yükle
            val storageClient = client.storage
            storageClient.from(bucketName).upload(path = fileName, data = compressedPhotoBytes, upsert = true)
            
            uploadProgress = 0.7f
            
            // Dosyanın genel URL'sini al
            val publicUrl = storageClient.from(bucketName).publicUrl(path = fileName)
            
            // Kullanıcı profilini güncelle
            val updates = mapOf(
                "id" to userId,
                "avatar_url" to publicUrl,
                "updated_at" to java.time.OffsetDateTime.now().toString()
            )
            
            uploadProgress = 0.9f
            
            // Profil güncelleme
            client.from("profiles").upsert(updates)
            
            // Mevcut kullanıcı ayrıntılarını güncelle
            userDetails = userDetails?.copy(avatar_url = publicUrl)
            avatarUrl = publicUrl
            
            uploadProgress = 1.0f
            delay(300) // Tamamlanma efekti için kısa bir gecikme
            
            println("DEBUG: Photo uploaded successfully: $publicUrl")
            publicUrl
        } catch (e: Exception) {
            println("DEBUG: Error uploading photo: ${e.message}")
            e.printStackTrace()
            errorMessage = "Fotoğraf yüklenirken hata oluştu: ${e.message}"
            null
        } finally {
            isPhotoUploading = false
        }
    }
    
    // Yüklenirken veya hata durumunda gösterilecekler
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return@ProfilePhotoManager
    }

    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = errorMessage!!, color = Color.Red)
        }
        return@ProfilePhotoManager
    }

    // Profil resmi URL'sini al
    val profilePicUrl = userDetails?.avatar_url

    // Ana container
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Arkaplan efektleri
        ModernBlurredBackground()
        
        // Ana içerik
        AnimatedVisibility(
            visible = showElements,
            enter = fadeIn(tween(700)) + expandVertically(tween(700)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık
                Text(
                    text = "PROFİL FOTOĞRAFI",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = orbitronFont,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color.hsv(hue % 360f, 0.5f, 0.9f)
                            )
                        )
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Profil fotoğrafı görüntüleme/yükleme alanı
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Arka plan parlama efekti
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .blur(25.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.hsv(hue % 360f, 0.8f, 0.7f).copy(alpha = 0.3f * getGlowFactor(0f)),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    
                    // Fotoğraf çerçevesi
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer { 
                                scaleX = pulseScale
                                scaleY = pulseScale
                            }
                            .shadow(
                                elevation = 10.dp,
                                spotColor = Color.hsv(hue % 360f, 1f, 0.7f).copy(alpha = getGlowFactor(0f) * 0.5f),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.hsv(hue % 360f, 0.2f, 0.1f),
                                        Color.Black
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.hsv(hue % 360f, 1f, 0.7f).copy(alpha = getGlowFactor(0f) * 0.8f),
                                        Color.hsv((hue + 60) % 360f, 1f, 0.6f).copy(alpha = getGlowFactor(0.3f) * 0.6f),
                                        Color.hsv((hue - 30 + 360) % 360f, 1f, 0.7f).copy(alpha = getGlowFactor(0.6f) * 0.7f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = !isPhotoLoading && !isPhotoUploading,
                                onClick = { showOptions = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Yükleme durumu
                        if (isPhotoLoading || isPhotoUploading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Yükleme çemberi
                                CircularProgressIndicator(
                                    progress = if (isPhotoUploading) uploadProgress else 0f,
                                    color = Color.hsv(hue % 360f, 0.8f, 0.8f),
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        } 
                        // Profil fotoğrafı
                        else if (photoUri != null || avatarUrl != null) {
                            // Varsayılan profil resmi kullan
                            AsyncImage(
                                model = profilePicUrl,
                                contentDescription = "Profil Fotoğrafı",
                                modifier = Modifier.size(100.dp)
                            )
                        } 
                        // Fotoğraf yok - ekle ikonu
                        else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Fotoğraf Ekle",
                                    tint = Color.hsv(hue % 360f, 0.8f, 0.8f),
                                    modifier = Modifier.size(60.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Fotoğraf Ekle",
                                    color = Color.hsv(hue % 360f, 0.5f, 0.9f),
                                    fontFamily = orbitronFont,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Butonlar (sadece fotoğraf seçildiyse görünür)
                AnimatedVisibility(visible = photoUri != null && !isPhotoUploading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    ) {
                        // İptal Butonu
                        OutlinedButton(
                            onClick = { photoUri = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.hsv(0f, 0.7f, 0.9f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color.hsv(0f, 0.7f, 0.9f).copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "İptal",
                                fontFamily = orbitronFont
                            )
                        }
                        
                        // Kaydet Butonu
                        com.NeonGlowButton(
                            onClick = {
                                photoUri?.let { uri ->
                                    coroutineScope.launch {
                                        uploadPhoto(uri)
                                    }
                                }
                            },
                            text = "Kaydet",
                            modifier = Modifier.weight(1f),
                            fontFamily = orbitronFont
                        )
                    }
                }
                
                // Kullanıcı adı ve e-posta gösterimi (opsiyonel)
                currentUser?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userDetails?.username ?: it.email ?: "Kullanıcı",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = orbitronFont
                    )
                }
            }
        }
        
        // Fotoğraf seçim diyaloğu
        if (showOptions) {
            AlertDialog(
                onDismissRequest = { showOptions = false },
                title = {
                    Text(
                        text = "Fotoğraf Seç",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = orbitronFont
                        )
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Kameradan Çek
                        FilledTonalButton(
                            onClick = {
                                val file = createImageFile(context)
                                cameraUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                cameraLauncher.launch(cameraUri)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.hsv(hue % 360f, 0.3f, 0.2f),
                                contentColor = Color.hsv(hue % 360f, 0.5f, 0.9f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Kameradan Çek",
                                fontFamily = orbitronFont
                            )
                        }
                        
                        // Galeriden Seç
                        FilledTonalButton(
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.hsv(hue % 360f, 0.3f, 0.2f),
                                contentColor = Color.hsv(hue % 360f, 0.5f, 0.9f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Galeriden Seç",
                                fontFamily = orbitronFont
                            )
                        }
                        
                        // Fotoğrafı Kaldır (eğer mevcut fotoğraf varsa)
                        if (avatarUrl != null) {
                            FilledTonalButton(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            // Kullanıcı profilinden fotoğrafı kaldır
                                            val userId = currentUser?.id ?: return@launch
                                            val updates = mapOf(
                                                "id" to userId,
                                                "avatar_url" to null,
                                                "updated_at" to java.time.OffsetDateTime.now().toString()
                                            )
                                            
                                            val client = supabaseManager.getSupabaseClient()
                                            client.from("profiles").upsert(updates)
                                            
                                            // Mevcut kullanıcı ayrıntılarını güncelle
                                            userDetails = userDetails?.copy(avatar_url = null)
                                            avatarUrl = null
                                            photoUri = null
                                            showOptions = false
                                            
                                            Toast.makeText(context, "Profil fotoğrafı kaldırıldı", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            errorMessage = "Fotoğraf kaldırılamadı: ${e.message}"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF331111),
                                    contentColor = Color(0xFFFF6666)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Fotoğrafı Kaldır",
                                    fontFamily = orbitronFont
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { showOptions = false }
                    ) {
                        Text(
                            text = "İptal",
                            fontFamily = orbitronFont
                        )
                    }
                }
            )
        }
    }
}

/**
 * Fotoğrafı sıkıştırma fonksiyonu
 */
fun compressPhoto(photoInputStream: InputStream?, maxWidth: Int): ByteArray {
    if (photoInputStream == null) {
        throw IllegalArgumentException("Resim bulunamadı")
    }
    
    val originalBitmap = BitmapFactory.decodeStream(photoInputStream)
    val width = originalBitmap.width
    val height = originalBitmap.height
    
    var scaledBitmap = originalBitmap
    
    // Eğer fotoğraf çok büyükse boyutlandır
    if (width > maxWidth) {
        val scaleFactor = maxWidth.toFloat() / width
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()
        scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        
        // Orijinal bitmap'i serbest bırak
        if (scaledBitmap != originalBitmap) {
            originalBitmap.recycle()
        }
    }
    
    // Bitmap'i JPEG formatında sıkıştır
    val outputStream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    
    // Kullanılan bitmap'i serbest bırak
    scaledBitmap.recycle()
    
    return outputStream.toByteArray()
}

/**
 * Resim kaynağı enum'u
 */
enum class ImageSource {
    CAMERA, GALLERY
} 