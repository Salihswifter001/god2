package com.aihackathonkarisacikartim.god2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.io.ByteArrayOutputStream
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import java.io.File
import kotlinx.datetime.Clock
import com.aihackathonkarisacikartim.god2.GeneratedMusicData
import com.aihackathonkarisacikartim.god2.BuildConfig

// Model sınıfları
@Serializable
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val is_completed: Boolean = false,
    val user_id: String? = null
)

@Serializable
data class User(
    val id: String,
    val email: String
)

// Kullanıcı detayları veri sınıfı
@Serializable
data class UserDetails(
    val id: String,
    val username: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val avatar_url: String? = null,
    val gender: String? = null,
    val biography: String? = null,
    val phone_number: String? = null,
    val username_change_count: Int? = 0 // Kullanıcı adı değiştirme sayısı - nullable olarak değiştirildi
)

// Veritabanı işlemleri için yardımcı sınıf
class SupabaseManager {

    // Supabase configuration from BuildConfig (secure)
    private val supabaseUrl = if (BuildConfig.SUPABASE_URL.isNotEmpty()) {
        BuildConfig.SUPABASE_URL
    } else {
        throw IllegalStateException("Supabase URL is not configured. Please add SUPABASE_URL to local.properties")
    }
    
    private val supabaseKey = if (BuildConfig.SUPABASE_ANON_KEY.isNotEmpty()) {
        BuildConfig.SUPABASE_ANON_KEY
    } else {
        throw IllegalStateException("Supabase key is not configured. Please add SUPABASE_ANON_KEY to local.properties")
    }

    private val supabase = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }

    // Supabase istemcisine erişim sağlayan yardımcı fonksiyon
    fun getSupabaseClient(): SupabaseClient {
        return supabase
    }

    // Auth fonksiyonları
    
    /**
     * Kullanıcı giriş işlemi
     */
    suspend fun login(email: String, password: String, sessionManager: com.settings.SessionManager? = null): Result<UserInfo> {
        return try {
            println("DEBUG: SupabaseManager.login called with email: $email")
            
            // Email provider ile giriş yap
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Kullanıcı bilgilerini al
            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo != null) {
                println("DEBUG: Login successful for userId: ${userInfo.id}")
                
                // Kullanıcı detaylarını al (username için)
                val userDetails = getUserDetails(userInfo.id)
                val username = userDetails?.username ?: email.substringBefore("@")
                
                println("DEBUG: Username retrieved: $username")
                
                // Oturum bilgilerini kaydet
                if (sessionManager != null) {
                    sessionManager.saveUserLoginSession(userInfo, username)
                    println("DEBUG: Session saved in SupabaseManager - isLoggedIn: ${sessionManager.isLoggedIn()}")
                }
                
                Result.success(userInfo)
            } else {
                println("DEBUG: Login failed - no user info")
                Result.failure(Exception("Kullanıcı bilgileri alınamadı"))
            }
        } catch (e: Exception) {
            println("DEBUG: Login exception: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Kullanıcı kaydı
     */
    suspend fun register(email: String, password: String): Result<UserInfo> {
        return try {
            // Email provider ile kayıt ol
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Hemen giriş yap
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            } catch (e: Exception) {
                println("Otomatik giriş başarısız: ${e.message}")
            }
            
            // Kullanıcı bilgilerini al
            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo != null) {
                Result.success(userInfo)
            } else {
                Result.failure(Exception("Kayıt başarılı! Lütfen e-posta adresinizi doğrulayıp giriş yapın."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kullanıcı adı ile birlikte kayıt
     */
    suspend fun register(email: String, username: String, password: String): Result<UserInfo> {
        return try {
            // Email provider ile kayıt ol
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Hemen giriş yap
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            } catch (e: Exception) {
                println("Otomatik giriş başarısız: ${e.message}")
            }
            
            // Kullanıcı bilgilerini al
            val userInfo = supabase.auth.currentUserOrNull()
            
            if (userInfo != null) {
                try {
                    // Kullanıcı ID'sini al
                    val userId = userInfo.id
                    
                    // profiles tablosundaki kullanıcı adını güncelle
                    println("DEBUG: Updating profile for user ID: $userId with username: $username")
                    
                    // PostgreSQL'de upsert işlemi (insert veya update) yapalım
                    val response = supabase.from("profiles")
                        .upsert(
                            value = mapOf(
                                "id" to userId,
                                "username" to username,
                                "updated_at" to Clock.System.now().toString()
                            )
                        )
                    
                    println("DEBUG: Profile update response: $response")
                    
                    Result.success(userInfo)
                } catch (e: Exception) {
                    // Profil güncellenemese bile kullanıcı oluşturulduğu için başarılı sayalım
                    println("Profil güncelleme hatası: ${e.message}")
                    e.printStackTrace()
                    Result.success(userInfo)
                }
            } else {
                Result.failure(Exception("Kayıt başarılı! Lütfen e-posta adresinizi doğrulayıp giriş yapın."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Kullanıcı çıkışı
     */
    suspend fun logout(sessionManager: com.settings.SessionManager? = null): Result<Unit> {
        return try {
            supabase.auth.signOut()
            
            // Oturum bilgilerini temizle
            sessionManager?.clearSession()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mevcut kullanıcı bilgilerini alma
     */
    fun getCurrentUser(): UserInfo? {
        return supabase.auth.currentUserOrNull()
    }

    /**
     * Şifre değiştirme
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            // Mevcut kullanıcıyı kontrol et
            val currentUser = getCurrentUser()
            if (currentUser == null) {
                return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))
            }
            
            val email = currentUser.email ?: return Result.failure(Exception("Kullanıcı e-posta adresi bulunamadı"))
            
            // Mevcut şifreyi doğrula
            try {
                // Mevcut şifre ile oturum açmayı dene
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = currentPassword
                }
            } catch (e: Exception) {
                // Giriş başarısızsa, mevcut şifre yanlış demektir
                return Result.failure(Exception("Mevcut şifre doğru değil"))
            }
            
            // Şifreyi güncelle
            supabase.auth.modifyUser {
                password = newPassword
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * E-posta değiştirme
     */
    suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit> {
        return try {
            // Mevcut kullanıcıyı kontrol et
            val currentUser = getCurrentUser()
            if (currentUser == null) {
                return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))
            }
            
            // E-posta adresini güncelle
            supabase.auth.modifyUser {
                email = newEmail
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kullanıcı detaylarını getir
     */
    suspend fun getUserDetails(userId: String): UserDetails? {
        return try {
            val profiles = supabase.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<UserDetails>(   )
                
            if (profiles.isNotEmpty()) {
                profiles.first()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error getting user details: ${e.message}")
            e.printStackTrace()
            
            // Eğer created_at alanından kaynaklı bir hata varsa, özel bir yaklaşım uygula
            if (e.message?.contains("created_at") == true) {
                try {
                    // Metadata ile profil bilgilerini al
                    val userInfo = getCurrentUser()
                    if (userInfo != null) {
                        // Basit bir UserDetails nesnesi döndür
                        return UserDetails(
                            id = userInfo.id,
                            username = userInfo.userMetadata?.get("username") as? String,
                            created_at = null,
                            updated_at = null,
                            avatar_url = null
                        )
                    }
                } catch (innerException: Exception) {
                    Log.e("SupabaseManager", "Fallback error: ${innerException.message}")
                }
            }
            
            null
        }
    }

    /**
     * Kullanıcıya ait todoları getirme
     */
    suspend fun getTodos(): List<TodoItem> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        
        return try {
            supabase.from("todos")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<TodoItem>()
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error getting todos: ${e.message}")
            emptyList()
        }
    }

    /**
     * Todo ekleme
     */
    suspend fun addTodo(name: String): Result<TodoItem> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return Result.failure(Exception("Kullanıcı giriş yapmamış"))
        
        val newTodo = TodoItem(
            name = name,
            user_id = userId
        )

        return try {
            val insertedTodo = supabase.from("todos")
                .insert(newTodo) {
                    select()
                }
                .decodeSingle<TodoItem>()

            Result.success(insertedTodo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Todo güncelleyici
     */
    suspend fun updateTodo(todo: TodoItem): Result<TodoItem> {
        return try {
            val updates = mapOf(
                "id" to todo.id,
                "name" to todo.name,
                "is_completed" to todo.is_completed,
                "user_id" to todo.user_id
            )
            
            val updatedTodo = supabase.from("todos")
                .update(updates) {
                    filter {
                        eq("id", todo.id)
                    }
                }
                .decodeSingle<TodoItem>()

            Result.success(updatedTodo)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error updating todo: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Todo silme
     */
    suspend fun deleteTodo(id: String): Result<Unit> {
        return try {
            supabase.from("todos")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error deleting todo: ${e.message}")
            Result.failure(e)
        }
    }

    // Storage ile ilgili fonksiyonlar
    
    /**
     * API'den gelen müzik dosyasını indirir ve Supabase Storage'a yükler
     */
    suspend fun saveApiMusicToStorage(musicUrl: String, userId: String, context: Context): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SupabaseManager", "Müzik dosyası indiriliyor: $musicUrl")
                
                // URL'den dosyayı indir
                val url = URL(musicUrl)
                val connection = url.openConnection()
                connection.connect()
                
                val inputStream = connection.getInputStream()
                val byteArrayOutputStream = ByteArrayOutputStream()
                
                inputStream.use { input ->
                    byteArrayOutputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val musicBytes = byteArrayOutputStream.toByteArray()
                Log.d("SupabaseManager", "Müzik dosyası indirildi: ${musicBytes.size} bytes")
                
                // Benzersiz dosya adı oluştur
                val fileName = "${userId}_${UUID.randomUUID()}.mp3"
                val bucketName = "music-files"
                
                // Supabase Storage'a yükle
                try {
                    // Bucket yoksa oluştur
                    try {
                        supabase.storage.createBucket(bucketName) {
                            public = true
                        }
                        Log.d("SupabaseManager", "Bucket oluşturuldu: $bucketName")
                    } catch (e: Exception) {
                        // Bucket zaten varsa devam et
                        Log.d("SupabaseManager", "Bucket zaten mevcut: $bucketName")
                    }
                    
                    // Dosyayı yükle
                    val result = supabase.storage.from(bucketName).upload(
                        path = fileName,
                        data = musicBytes,
                        upsert = true
                    )
                    
                    // Public URL oluştur
                    val publicUrl = supabase.storage.from(bucketName).publicUrl(fileName)
                    Log.d("SupabaseManager", "Müzik Supabase'e yüklendi: $publicUrl")
                    
                    Result.success(publicUrl)
                } catch (e: Exception) {
                    Log.e("SupabaseManager", "Supabase Storage yükleme hatası: ${e.message}")
                    // Hata durumunda orijinal URL'yi döndür
                    Result.success(musicUrl)
                }
            } catch (e: Exception) {
                Log.e("SupabaseManager", "Müzik dosyası indirme hatası: ${e.message}")
                // Hata durumunda orijinal URL'yi döndür
                Result.success(musicUrl)
            }
        }
    }
    
    /**
     * API'den gelen kapak resmini indirir ve Supabase Storage'a yükler
     */
    suspend fun saveApiCoverToStorage(coverUrl: String, userId: String, context: Context): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SupabaseManager", "Kapak resmi indiriliyor: $coverUrl")
                
                // URL'den dosyayı indir
                val url = URL(coverUrl)
                val connection = url.openConnection()
                connection.connect()
                
                val inputStream = connection.getInputStream()
                val byteArrayOutputStream = ByteArrayOutputStream()
                
                inputStream.use { input ->
                    byteArrayOutputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val coverBytes = byteArrayOutputStream.toByteArray()
                Log.d("SupabaseManager", "Kapak resmi indirildi: ${coverBytes.size} bytes")
                
                // Benzersiz dosya adı oluştur
                val fileName = "${userId}_${UUID.randomUUID()}.jpg"
                val bucketName = "cover-images"
                
                // Supabase Storage'a yükle
                try {
                    // Bucket yoksa oluştur
                    try {
                        supabase.storage.createBucket(bucketName) {
                            public = true
                        }
                        Log.d("SupabaseManager", "Bucket oluşturuldu: $bucketName")
                    } catch (e: Exception) {
                        // Bucket zaten varsa devam et
                        Log.d("SupabaseManager", "Bucket zaten mevcut: $bucketName")
                    }
                    
                    // Dosyayı yükle
                    val result = supabase.storage.from(bucketName).upload(
                        path = fileName,
                        data = coverBytes,
                        upsert = true
                    )
                    
                    // Public URL oluştur
                    val publicUrl = supabase.storage.from(bucketName).publicUrl(fileName)
                    Log.d("SupabaseManager", "Kapak Supabase'e yüklendi: $publicUrl")
                    
                    Result.success(publicUrl)
                } catch (e: Exception) {
                    Log.e("SupabaseManager", "Supabase Storage yükleme hatası: ${e.message}")
                    // Hata durumunda orijinal URL'yi döndür
                    Result.success(coverUrl)
                }
            } catch (e: Exception) {
                Log.e("SupabaseManager", "Kapak resmi indirme hatası: ${e.message}")
                // Hata durumunda orijinal URL'yi döndür
                Result.success(coverUrl)
            }
        }
    }
    
    /**
     * Oluşturulan müziği veritabanına kaydeder
     */
    suspend fun saveGeneratedMusic(musicData: GeneratedMusicData): Result<String> {
        return try {
            Log.d("SupabaseManager", "Müzik kaydediliyor: $musicData")
            
            // Önce duplicate kontrolü yap
            val alreadyExists = checkMusicExistsByMusicId(musicData.musicId, musicData.userId)
            if (alreadyExists) {
                Log.w("SupabaseManager", "Müzik zaten mevcut, kayıt atlanıyor: musicId=${musicData.musicId}")
                return Result.success(musicData.id) // Başarılı olarak dön ama kaydetme
            }
            
            // Veritabanına ekle
            val response = supabase.from("generated_music")
                .insert(musicData)
            
            Log.d("SupabaseManager", "Müzik başarıyla kaydedildi: ${musicData.id}")
            
            // Kaydedilen müziği kontrol et
            val savedMusic = supabase.from("generated_music")
                .select {
                    filter {
                        eq("id", musicData.id)
                    }
                }
                .decodeSingleOrNull<GeneratedMusicData>()
            
            if (savedMusic != null) {
                Log.d("SupabaseManager", "Kayıt doğrulandı: $savedMusic")
            } else {
                Log.w("SupabaseManager", "Kayıt yapıldı ama doğrulanamadı!")
            }
            
            Result.success(musicData.id)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Müzik kaydetme hatası: ${e.message}")
            Log.e("SupabaseManager", "Hata detayı: ", e)
            Result.failure(e)
        }
    }

    /**
     * Kullanıcıya ait oluşturulmuş müzikleri getirir
     */
    suspend fun getUserGeneratedMusic(userId: String): List<GeneratedMusicData> {
        return try {
            Log.d("SupabaseManager", "Müzikler getiriliyor, userId: $userId")
            
            val musicList = supabase.from("generated_music")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<GeneratedMusicData>()
            
            Log.d("SupabaseManager", "Bulunan müzik sayısı: ${musicList.size}")
            musicList.forEach { music ->
                Log.d("SupabaseManager", "Müzik: ${music.title} - ${music.id}")
            }
            
            musicList
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Kullanıcı müziklerini getirme hatası: ${e.message}")
            Log.e("SupabaseManager", "Hata detayı: ", e)
            emptyList()
        }
    }

    /**
     * Kullanıcının müziklerini siler
     */
    suspend fun deleteUserMusic(userId: String, musicId: String): Result<Unit> {
        return try {
            // Veritabanından kaydı sil
            supabase.from("generated_music")
                .delete {
                    filter {
                        eq("id", musicId)
                        eq("user_id", userId) // Güvenlik için kullanıcı ID'sini de kontrol et
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Müzik silme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Public bir dosya için URL oluşturur
     */
    fun getPublicFileUrl(bucketName: String, path: String): String {
        return "$supabaseUrl/storage/v1/object/public/$bucketName/$path"
    }
    
    /**
     * Private bir dosya için geçici imzalı URL oluşturur
     */
    suspend fun getSignedUrl(bucketName: String, path: String, expiresIn: Int = 3600): String {
        return try {
            // Şu an Supabase Storage desteği eksik, bu nedenle doğrudan public URL dönüyoruz
            getPublicFileUrl(bucketName, path)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Signed URL oluşturma hatası: ${e.message}")
            ""
        }
    }
    
    /**
     * Albüm kapak resmi için tam URL döndürür
     */
    suspend fun getCoverArtUrl(coverPath: String): String {
        return if (coverPath.startsWith("http")) {
            // Zaten tam URL ise doğrudan kullan
            coverPath
        } else {
            // Storage path ise URL oluştur
            getPublicFileUrl("cover_art", coverPath)
        }
    }
    
    /**
     * MP3 dosyası için tam URL döndürür
     */
    suspend fun getMusicFileUrl(musicPath: String): String {
        return if (musicPath.startsWith("http")) {
            // Zaten tam URL ise doğrudan kullan
            musicPath
        } else {
            // Storage path ise URL oluştur
            getPublicFileUrl("music_files", musicPath)
        }
    }
    
    /**
     * Kullanıcı adının benzersiz olup olmadığını kontrol eder
     */
    suspend fun isUsernameUnique(username: String, currentUserId: String): Boolean {
        return try {
            val profiles = getSupabaseClient().from("profiles")
                .select {
                    filter {
                        eq("username", username)
                        neq("id", currentUserId) // Kendi profili hariç
                    }
                }
                .decodeList<UserDetails>()
            
            profiles.isEmpty() // Eğer boşsa, kullanıcı adı benzersizdir
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Kullanıcı adı kontrolü sırasında hata: ${e.message}")
            false // Hata durumunda güvenli seçenek - benzersiz olmadığını varsay
        }
    }

    /**
     * Kullanıcı profil bilgilerini günceller
     */
    suspend fun updateUserProfile(userId: String, username: String? = null, gender: String? = null, 
                                  biography: String? = null, phone_number: String? = null): Result<UserDetails> {
        return try {
            // Mevcut kullanıcı detaylarını al
            val currentUserDetails = getUserDetails(userId)
            
            // Kullanıcı adı değiştiriliyor ve boş değilse kullanıcı adı kontrolü yap
            if (username != null && username.isNotBlank()) {
                // Kullanıcı adı değiştirilmek isteniyorsa ve mevcut kullanıcı adından farklıysa
                if (currentUserDetails != null && currentUserDetails.username != username) {
                    // Kullanıcı adı değiştirme hakkı kontrolü
                    val changeCount = currentUserDetails.username_change_count ?: 0
                    if (changeCount >= 3) {
                        return Result.failure(Exception("Kullanıcı adınızı değiştirme hakkınız dolmuştur (maksimum 3 kez)."))
                    }
                    
                    // Benzersizlik kontrolü
                    val isUnique = isUsernameUnique(username, userId)
                    if (!isUnique) {
                        return Result.failure(Exception("Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin."))
                    }
                }
            }
            
            // Güncellenecek alanları içeren harita oluştur - Any yerine String tipini kullan
            val updatesMap = mutableMapOf<String, String>()
            
            // Null olmayan alanları haritaya ekle
            username?.let { 
                // Eğer kullanıcı adı değiştiriliyorsa, sayacı artır
                if (currentUserDetails != null && currentUserDetails.username != username) {
                    val newCount = (currentUserDetails.username_change_count ?: 0) + 1
                    updatesMap["username_change_count"] = newCount.toString() // Int'i String'e dönüştür
                }
                updatesMap["username"] = it 
            }
            gender?.let { updatesMap["gender"] = it }
            biography?.let { updatesMap["biography"] = it }
            phone_number?.let { updatesMap["phone_number"] = it }
            updatesMap["updated_at"] = Clock.System.now().toString()
            
            try {
                // Güncelleme işlemini gerçekleştir
                supabase.from("profiles")
                    .update(updatesMap) { 
                        filter { 
                            eq("id", userId) 
                        }
                    }

                // Güncelleme işlemi başarılı, şimdi güncellenmiş kullanıcı bilgilerini alalım
                val updatedProfile = getUserDetails(userId)
                
                if (updatedProfile != null) {
                    Result.success(updatedProfile)
                } else {
                    Log.e("SupabaseManager", "Profil güncellendi ancak bilgiler alınamadı")
                    // Profil verileri alınamadıysa, önceki değerlerle yeni bir UserDetails oluştur
                    val newChangeCount = if (currentUserDetails != null && currentUserDetails.username != username)
                        (currentUserDetails.username_change_count ?: 0) + 1
                    else
                        currentUserDetails?.username_change_count ?: 0
                        
                    val fallbackDetails = UserDetails(
                        id = userId,
                        username = username,
                        gender = gender,
                        biography = biography,
                        phone_number = phone_number,
                        updated_at = Clock.System.now().toString(),
                        username_change_count = newChangeCount
                    )
                    Result.success(fallbackDetails)
                }
            } catch (e: Exception) {
                Log.e("SupabaseManager", "JSON parse hatası: ${e.message}")
                e.printStackTrace()
                
                // JSON parse hatası oluşsa bile, işlemi başarılı kabul edelim ve güncellenmiş bilgileri geri alalım
                val updatedProfile = getUserDetails(userId)
                
                if (updatedProfile != null) {
                    Result.success(updatedProfile)
                } else {
                    // Hiçbir şekilde güncel bilgileri alamadıysak, default bir nesne döndürelim
                    Log.e("SupabaseManager", "Alternatif metot da başarısız oldu")
                    val newChangeCount = if (currentUserDetails != null && currentUserDetails.username != username)
                        (currentUserDetails.username_change_count ?: 0) + 1
                    else
                        currentUserDetails?.username_change_count ?: 0
                        
                    val fallbackDetails = UserDetails(
                        id = userId,
                        username = username,
                        gender = gender,
                        biography = biography,
                        phone_number = phone_number,
                        updated_at = Clock.System.now().toString(),
                        username_change_count = newChangeCount
                    )
                    Result.success(fallbackDetails)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Profil güncelleme hatası: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Belirli bir müzik kaydını ID'ye göre getirir
     * @param musicId Müzik ID'si
     * @return Müzik verisi veya null
     */
    suspend fun getGeneratedMusicById(musicId: String): GeneratedMusicData? {
        return try {
            Log.d("SupabaseManager", "Müzik verisi alınıyor: musicId=$musicId")
            
            val musicData = supabase.from("generated_music")
                .select {
                    filter {
                        eq("id", musicId)
                    }
                }
                .decodeSingle<GeneratedMusicData>()
            
            Log.d("SupabaseManager", "Müzik verisi başarıyla alındı: ${musicData.title}")
            musicData
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Müzik verisi getirme hatası: ${e.message}")
            null
        }
    }
    
    /**
     * API'den gelen musicId'ye göre müzik kaydının var olup olmadığını kontrol eder
     * @param musicId API'den gelen müzik ID'si
     * @param userId Kullanıcı ID'si
     * @return Müzik varsa true, yoksa false
     */
    suspend fun checkMusicExistsByMusicId(musicId: String, userId: String): Boolean {
        return try {
            Log.d("SupabaseManager", "Müzik duplicate kontrolü: musicId=$musicId, userId=$userId")
            
            val existingMusic = supabase.from("generated_music")
                .select {
                    filter {
                        eq("music_id", musicId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<GeneratedMusicData>()
            
            val exists = existingMusic.isNotEmpty()
            Log.d("SupabaseManager", "Müzik mevcut mu? $exists (musicId: $musicId)")
            exists
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Müzik duplicate kontrol hatası: ${e.message}")
            false
        }
    }
} 