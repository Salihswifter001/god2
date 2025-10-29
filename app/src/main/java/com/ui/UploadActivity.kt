package com.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.aihackathonkarisacikartim.god2.R
import com.aihackathonkarisacikartim.god2.SupabaseManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import com.musicApi.MusicApiService
import kotlinx.coroutines.delay
import java.util.*

class UploadActivity : ComponentActivity() {
    private lateinit var supabaseManager: SupabaseManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        supabaseManager = SupabaseManager()
        
        setContent {
            MusicUploadScreen(supabaseManager)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicUploadScreen(supabaseManager: SupabaseManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as? ComponentActivity
    
    // MusicApiService instance oluştur
    val musicApiService = remember { MusicApiService(context) }
    
    var title by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var musicUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isVocalModeEnabled by remember { mutableStateOf(false) }
    var vocalLyrics by remember { mutableStateOf("") }
    var isAIGenerateMode by remember { mutableStateOf(false) }
    var generationProgress by remember { mutableStateOf(0f) }
    var generationStatus by remember { mutableStateOf("") }
    var currentTaskId by remember { mutableStateOf<String?>(null) }
    
    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        musicUri = uri
    }
    
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        coverUri = uri
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "İzin verildi", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "İzin reddedildi", Toast.LENGTH_SHORT).show()
        }
    }
    
    // İzinleri kontrol et
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Yeni Müzik Yükle",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FFFF),
                modifier = Modifier.padding(top = 20.dp, bottom = 32.dp)
            )
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Şarkı Adı", color = Color(0xFF00FFFF)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color(0xFF00FFFF),
                    focusedBorderColor = Color(0xFF00FFFF),
                    unfocusedBorderColor = Color(0xFF006060)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = { Text("Tür", color = Color(0xFF00FFFF)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color(0xFF00FFFF),
                    focusedBorderColor = Color(0xFF00FFFF),
                    unfocusedBorderColor = Color(0xFF006060)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Vokal Modu Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vokal Modu",
                    color = Color(0xFF00FFFF),
                    fontSize = 16.sp
                )
                Switch(
                    checked = isVocalModeEnabled,
                    onCheckedChange = { isVocalModeEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00FFFF),
                        checkedTrackColor = Color(0xFF006060),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }
            
            // AI ile Müzik Üretme Modu Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "AI ile Müzik Üret",
                    color = Color(0xFF00FFFF),
                    fontSize = 16.sp
                )
                Switch(
                    checked = isAIGenerateMode,
                    onCheckedChange = { isAIGenerateMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00FFFF),
                        checkedTrackColor = Color(0xFF006060),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }
            
            // Vokal Modu açıksa metin girişi göster
            if (isVocalModeEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF001A1A)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Şarkı Sözleri",
                            color = Color(0xFF00FFFF),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = vocalLyrics,
                            onValueChange = { vocalLyrics = it },
                            placeholder = { Text("Şarkı sözlerinizi buraya yazın...", color = Color.Gray) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                cursorColor = Color(0xFF00FFFF),
                                focusedBorderColor = Color(0xFF00FFFF),
                                unfocusedBorderColor = Color(0xFF006060)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = Color.White
                            ),
                            maxLines = 8
                        )
                    }
                }
            }
            
            // Müzik Dosyası Seçme
            if (!isVocalModeEnabled && !isAIGenerateMode) {
                Button(
                    onClick = { musicPickerLauncher.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Müzik Dosyası Seç")
                }
            }

            // Albüm kapağı seçme butonu
            Button(
                onClick = { coverPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Albüm Kapağı Seç")
            }
            
            // Seçilen dosya bilgileri
            if (musicUri != null && !isVocalModeEnabled && !isAIGenerateMode) {
                Text(
                    text = "Seçilen müzik: ${musicUri?.lastPathSegment}",
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            if (coverUri != null) {
                Text(
                    text = "Seçilen kapak: ${coverUri?.lastPathSegment}",
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                coverUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Kapak Önizleme",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // AI Üretim durumu gösterimi
            if (isAIGenerateMode && currentTaskId != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF003030)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Müzik Üretiliyor", 
                            color = Color(0xFF00FFFF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LinearProgressIndicator(
                            progress = generationProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00FFFF),
                            trackColor = Color(0xFF006060)
                        )
                        
                        Text(
                            text = generationStatus,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Yükleme butonu
            Button(
                onClick = {
                    if (title.isBlank() || genre.isBlank() || 
                       (!isAIGenerateMode && !isVocalModeEnabled && musicUri == null) || 
                       (isVocalModeEnabled && vocalLyrics.isBlank()) || 
                       (!isAIGenerateMode && coverUri == null)) {
                        Toast.makeText(context, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // genre boş ise varsayılan değeri yüklemeden önce kullanma
                    val actualGenre = if (genre.isNotBlank()) genre else ""
                    
                    isUploading = true
                    
                    scope.launch {
                        try {
                            // API ile müzik üretme modu
                            if (isAIGenerateMode) {
                                generationStatus = "Müzik üretme isteği gönderiliyor..."
                                generationProgress = 0.1f
                                
                                val result = if (isVocalModeEnabled) {
                                    // Vokal ile müzik üretme
                                    musicApiService.generateMusicWithLyrics(title, actualGenre, vocalLyrics)
                                } else {
                                    // Enstrümantal müzik üretme (task_id dönecek)
                                    musicApiService.generateMusic(title, actualGenre)
                                }
                                
                                if (result.isSuccess) {
                                    if (isVocalModeEnabled) {
                                        // Vokal modu doğrudan sonuç döndürür
                                        val musicData = result.getOrNull()
                                        Toast.makeText(context, "Müzik başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                                        // Temizle
                                        title = ""
                                        genre = ""
                                        vocalLyrics = ""
                                        isUploading = false
                                    } else {
                                        // Enstrümantal mod task_id döndürür
                                        val taskId = result.getOrNull() as? String
                                        currentTaskId = taskId
                                        
                                        // Durumu düzenli olarak kontrol et
                                        generationStatus = "Müzik üretiliyor, lütfen bekleyin..."
                                        generationProgress = 0.3f
                                        
                                        var isCompleted = false
                                        var attempts = 0
                                        
                                        while (!isCompleted && attempts < 60) { // 5 dakikadan 10 dakikaya çıkardık
                                            try {
                                                delay(10000) // 10 saniye bekle
                                                attempts++
                                                
                                                generationProgress = 0.3f + (attempts.toFloat() / 60f * 0.7f) // 60 denemeye göre ayarladık
                                                generationStatus = "Müzik üretiliyor... (${attempts * 10} saniye geçti)"
                                                
                                                Log.d("UploadActivity", "Müzik durumu kontrol ediliyor, deneme: $attempts")
                                                
                                                // TaskId kontrolü
                                                if (taskId.isNullOrBlank()) {
                                                    generationStatus = "Geçersiz task ID: Müzik üretim isteği başarısız olmuş olabilir"
                                                    Toast.makeText(context, "Geçersiz task ID", Toast.LENGTH_SHORT).show()
                                                    isUploading = false
                                                    break
                                                }
                                                
                                                val statusResult = musicApiService.checkMusicGenerationStatus(taskId ?: "")
                                                Log.d("UploadActivity", "Durum kontrolü sonucu: ${statusResult.isSuccess}")
                                                
                                                if (statusResult.isSuccess) {
                                                    val musicData = statusResult.getOrNull()
                                                    if (musicData != null) {
                                                        // Üretim tamamlandı
                                                        isCompleted = true
                                                        generationProgress = 1.0f
                                                        generationStatus = "Müzik başarıyla oluşturuldu!"
                                                        
                                                        Toast.makeText(context, "Müzik başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                                                        // Temizle
                                                        title = ""
                                                        genre = ""
                                                        currentTaskId = null
                                                        break
                                                    } else {
                                                        // Hala işleniyor, devam et
                                                        Log.d("UploadActivity", "Müzik hala işleniyor, devam ediliyor...")
                                                    }
                                                } else {
                                                    // Hata varsa loglayalım ama döngüden çıkmayalım
                                                    val error = statusResult.exceptionOrNull()?.message ?: "Bilinmeyen hata"
                                                    Log.e("UploadActivity", "Durum kontrolü hatası: $error. Yeniden denenecek.")
                                                    generationStatus = "Hata: $error. Yeniden deneniyor..."
                                                    // Hata olsa bile devam et, döngüden çıkmayalım
                                                }
                                            } catch (e: Exception) {
                                                Log.e("UploadActivity", "Durum kontrolü sırasında hata: ${e.message}")
                                                generationStatus = "Kontrol sırasında hata: ${e.message}. Yeniden deneniyor..."
                                                // Hata olsa bile devam et
                                            }
                                        }
                                        
                                        if (!isCompleted) {
                                            generationStatus = "Müzik üretimi zaman aşımına uğradı, lütfen daha sonra tekrar deneyin."
                                            Toast.makeText(context, "Müzik üretimi zaman aşımına uğradı", Toast.LENGTH_SHORT).show()
                                        }
                                        
                                        isUploading = false
                                    }
                                } else {
                                    generationStatus = "Hata: ${result.exceptionOrNull()?.message}"
                                    Toast.makeText(context, "API hatası: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    isUploading = false
                                }
                            } else {
                                // Normal dosya yükleme modu
                                // URI'den bytesları al
                                val musicBytes = if (!isVocalModeEnabled) {
                                    getBytesFromUri(context, musicUri!!)
                                } else {
                                    // Vokal modu açıksa şarkı sözleri text olarak kullanılacak
                                    vocalLyrics.toByteArray(Charsets.UTF_8)
                                }
                                
                                val coverBytes = getBytesFromUri(context, coverUri!!)
                                
                                if (musicBytes == null || coverBytes == null) {
                                    Toast.makeText(context, "Dosya okunamadı", Toast.LENGTH_SHORT).show()
                                    isUploading = false
                                    return@launch
                                }
                                
                                // Supabase SupabaseManager'ı kullan - doğru metodları çağır
                                val userId = supabaseManager.getCurrentUser()?.id
                                if (userId == null) {
                                    Toast.makeText(context, "Oturum açmanız gerekiyor", Toast.LENGTH_SHORT).show()
                                    isUploading = false
                                    return@launch
                                }
                                
                                // Dosyaları yükle
                                val musicFileName = if (!isVocalModeEnabled) {
                                    musicUri!!.lastPathSegment ?: "music.mp3"
                                } else {
                                    "lyrics.txt"
                                }
                                
                                val coverFileName = coverUri!!.lastPathSegment ?: "cover.jpg"
                                
                                // Supabase'e dosyaları yükle
                                val musicUrl = if (!isVocalModeEnabled) {
                                    val supabaseManager = com.supabase.SupabaseManager()
                                    supabaseManager.uploadMusicFile(userId, musicFileName, musicUri!!, context)
                                } else {
                                    // Vokal modu için sözleri text dosyası olarak yükle
                                    val lyricsUri = writeTextToTempFile(context, vocalLyrics, "lyrics.txt")
                                    if (lyricsUri != null) {
                                        val supabaseManager = com.supabase.SupabaseManager()
                                        supabaseManager.uploadMusicFile(userId, "lyrics.txt", lyricsUri, context)
                                    } else {
                                        Result.failure(Exception("Şarkı sözleri dosyası oluşturulamadı"))
                                    }
                                }
                                
                                val supabaseManager = com.supabase.SupabaseManager()
                                val coverUrl = supabaseManager.uploadCoverImage(userId, coverFileName, coverUri!!, context)
                                
                                if (musicUrl.isSuccess && coverUrl.isSuccess) {
                                    // Müzik verilerini kaydet
                                    val musicData = com.aihackathonkarisacikartim.god2.GeneratedMusicData(
                                        id = UUID.randomUUID().toString(),
                                        userId = userId,
                                        title = title,
                                        prompt = if (isVocalModeEnabled) "Vokal modu: $vocalLyrics" else "Manuel yükleme",
                                        genre = genre,
                                        musicUrl = musicUrl.getOrNull()!!,
                                        coverUrl = coverUrl.getOrNull()!!,
                                        duration = 0L
                                    )
                                    
                                    val mainSupabaseManager = com.aihackathonkarisacikartim.god2.SupabaseManager()
                                    val result = mainSupabaseManager.saveGeneratedMusic(musicData)
                                
                                isUploading = false
                                
                                    if (result.isSuccess) {
                                    Toast.makeText(context, "Müzik başarıyla yüklendi!", Toast.LENGTH_SHORT).show()
                                    // Temizle
                                    title = ""
                                    genre = ""
                                    musicUri = null
                                    coverUri = null
                                } else {
                                        Toast.makeText(context, "Yükleme hatası: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Yükleme hatası: ${musicUrl.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            isUploading = false
                            Toast.makeText(context, "Yükleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = (isAIGenerateMode || musicUri != null || (isVocalModeEnabled && vocalLyrics.isNotBlank())) && 
                          (isAIGenerateMode || isVocalModeEnabled || coverUri != null) && title.isNotBlank() && genre.isNotBlank() && !isUploading && currentTaskId == null
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isAIGenerateMode) {
                    Text("AI ile Müzik Üret")
                } else {
                    Text("Yükle")
                }
            }
        }
    }
}

/**
 * URI'den bir geçici dosya oluşturur ve içeriğini kopyalar
 */
private fun createTempFileFromUri(context: Context, uri: Uri, prefix: String): File {
    val tempFile = File.createTempFile(prefix, null, context.cacheDir)
    
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(tempFile).use { outputStream ->
            val buffer = ByteArray(4 * 1024) // 4k buffer
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        }
    }
    
    return tempFile
}

// URI'den dosya uzantısını alma
private fun getFileExtension(contentResolver: android.content.ContentResolver, uri: Uri): String {
    val mimeType = contentResolver.getType(uri)
    return if (mimeType != null) {
        mimeType.substringAfter("/")
    } else {
        // Varsayılan olarak bir değer döndür
        val path = uri.path
        path?.substringAfterLast(".", "")?.takeIf { it.isNotEmpty() } ?: "tmp"
    }
}

private fun getBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len: Int
        while (inputStream?.read(buffer).also { len = it ?: -1 } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        byteBuffer.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Metni geçici dosyaya yazar ve URI'sini döndürür
 */
private fun writeTextToTempFile(context: Context, text: String, filename: String): Uri? {
    return try {
        val tempFile = File(context.cacheDir, filename)
        FileOutputStream(tempFile).use { outputStream ->
            outputStream.write(text.toByteArray(Charsets.UTF_8))
            outputStream.flush()
        }
        Uri.fromFile(tempFile)
    } catch (e: Exception) {
        Log.e("UploadActivity", "Dosya oluşturma hatası: ${e.message}")
        null
    }
} 