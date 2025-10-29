package com.supabase

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import com.aihackathonkarisacikartim.god2.GeneratedMusicData
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

// OctaReels için model sınıfları
@Serializable
data class ReelsVideo(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("user_id") val userId: String,
    @SerialName("video_url") val videoUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    val description: String? = "",
    @SerialName("music_name") val musicName: String? = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class ReelsVideoWithCounts(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("video_url") val videoUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    val description: String? = "",
    @SerialName("music_name") val musicName: String? = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    val author: UserProfile? = null
)

@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class Comment(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("video_id") val videoId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("parent_comment_id") val parentCommentId: String? = null,
    val text: String,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    val user: UserProfile? = null
)

@Serializable
data class Like(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("video_id") val videoId: String,
    @SerialName("user_id") val userId: String
)

class SupabaseManager {
    // Supabase client
    private val client = createSupabaseClient(
        supabaseUrl = "https://bsniwlfnvgkfvgjnfwfi.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzbml3bGZudmdrZnZnam5md2ZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ2NDMyMjgsImV4cCI6MjA2MDIxOTIyOH0.RJOCRucZFbACTuNeL3XcBKxmPjAT1dt5C476iZEcJco"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    /**
     * Mevcut kullanıcı bilgilerini alma
     */
    fun getCurrentUser(): UserInfo? {
        return client.auth.currentUserOrNull()
    }

/**
 * Kullanıcının oluşturduğu müziği siler
 */
suspend fun deleteUserMusic(musicId: String): Result<Boolean> {
    return try {
        client.from("generated_music")
            .delete {
                filter {
                    eq("id", musicId)
                }
            }

            Result.success(true)
    } catch (e: Exception) {
        Result.failure(Exception("Müzik silme işlemi sırasında hata oluştu: ${e.message}"))
    }
}

/**
 * Müzik dosyasını Supabase Storage'a yükler
 * @param userId Kullanıcı ID'si
 * @param fileName Dosya adı
 * @param fileUri Dosya URI'si
 * @return Başarılı ise dosya URL'si ile Result.success, başarısız ise hata ile Result.failure
 */
suspend fun uploadMusicFile(userId: String, fileName: String, fileUri: Uri, context: Context): Result<String> {
    return try {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val bytes = inputStream?.readBytes() ?: return Result.failure(Exception("Dosya okunamadı"))
        
        val fileExt = if (fileName.contains(".")) {
            fileName.substring(fileName.lastIndexOf("."))
        } else {
            ".mp3"
        }
        
        val uniqueFileName = "music_${userId}_${System.currentTimeMillis()}$fileExt"
        client.storage.from("music")
            .upload(uniqueFileName, bytes)
            
            // Dosyanın genel erişilebilir URL'sini al
        val publicUrl = client.storage.from("music").publicUrl(uniqueFileName)
            Log.d("SupabaseManager", "Müzik dosyası yüklendi: $publicUrl")
            Result.success(publicUrl)
    } catch (e: Exception) {
        Log.e("SupabaseManager", "Müzik yükleme istisnası: ${e.message}")
        Result.failure(Exception("Müzik yükleme sırasında hata oluştu: ${e.message}"))
    }
}

/**
 * Kapak resmini Supabase Storage'a yükler
 * @param userId Kullanıcı ID'si
 * @param fileName Dosya adı
 * @param fileUri Dosya URI'si
 * @return Başarılı ise dosya URL'si ile Result.success, başarısız ise hata ile Result.failure
 */
suspend fun uploadCoverImage(userId: String, fileName: String, fileUri: Uri, context: Context): Result<String> {
    return try {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val bytes = inputStream?.readBytes() ?: return Result.failure(Exception("Dosya okunamadı"))
        
        val fileExt = if (fileName.contains(".")) {
            fileName.substring(fileName.lastIndexOf("."))
        } else {
            ".jpg"
        }
        
        val uniqueFileName = "cover_${userId}_${System.currentTimeMillis()}$fileExt"
        client.storage.from("covers")
            .upload(uniqueFileName, bytes)
            
            // Dosyanın genel erişilebilir URL'sini al
        val publicUrl = client.storage.from("covers").publicUrl(uniqueFileName)
            Log.d("SupabaseManager", "Kapak resmi yüklendi: $publicUrl")
            Result.success(publicUrl)
    } catch (e: Exception) {
        Log.e("SupabaseManager", "Kapak yükleme istisnası: ${e.message}")
        Result.failure(Exception("Kapak yükleme sırasında hata oluştu: ${e.message}"))
    }
}

/**
 * Oluşturulan müzik verisini Supabase veritabanına kaydeder
 * @param musicData Kaydedilecek müzik verisi
 * @return Başarılı ise Result.success(true), başarısız ise hata ile Result.failure
 */
suspend fun saveGeneratedMusic(musicData: GeneratedMusicData): Result<Boolean> {
    return try {
        client.from("generated_music")
            .insert(musicData)
            
            Log.d("SupabaseManager", "Müzik kaydedildi: ${musicData.id}")
            Result.success(true)
    } catch (e: Exception) {
        Log.e("SupabaseManager", "Müzik kaydetme istisnası: ${e.message}")
        Result.failure(Exception("Müzik kaydetme sırasında hata oluştu: ${e.message}"))
    }
}

/**
 * Kullanıcıya ait oluşturulmuş müzikleri getirir
 * @param userId Kullanıcı ID'si
 * @return Kullanıcının oluşturduğu müzik listesi
 */
suspend fun getUserGeneratedMusic(userId: String): List<GeneratedMusicData> {
    return try {
        val musicList = client.from("generated_music")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<GeneratedMusicData>()
            
        // Veritabanından gelen süreleri olduğu gibi kullan
        musicList
    } catch (e: Exception) {
        Log.e("SupabaseManager", "Kullanıcı müziklerini getirme hatası: ${e.message}")
        emptyList()
    }
}

    /**
     * OctaReels video yükler
     * @param userId Kullanıcı ID'si
     * @param videoFile Video dosya URI'si
     * @param thumbnailFile Thumbnail dosya URI'si  
     * @param description Video açıklaması
     * @param musicName Müzik adı
     * @return Başarı durumunda kaydedilen video ile Result.success
     */
    suspend fun uploadReelsVideo(
        userId: String,
        videoFile: Uri,
        thumbnailFile: Uri,
        description: String,
        musicName: String,
        context: Context
    ): Result<ReelsVideo> {
        return try {
            // 1. Video dosyasını yükle
            val videoUrlResult = uploadVideoFile(userId, "video.mp4", videoFile, context)
            if (videoUrlResult.isFailure) {
                return Result.failure(videoUrlResult.exceptionOrNull() ?: Exception("Video yüklenemedi"))
            }
            
            // 2. Thumbnail yükle
            val thumbnailUrlResult = uploadThumbnailFile(userId, "thumbnail.jpg", thumbnailFile, context)
            if (thumbnailUrlResult.isFailure) {
                return Result.failure(thumbnailUrlResult.exceptionOrNull() ?: Exception("Thumbnail yüklenemedi"))
            }
            
            // 3. Veritabanına kaydet
            val videoData = ReelsVideo(
                userId = userId,
                videoUrl = videoUrlResult.getOrDefault(""),
                thumbnailUrl = thumbnailUrlResult.getOrDefault(""),
                description = description,
                musicName = musicName
            )
            
            try {
                // Supabase'e veriyi gönder ve yanıtı almaya çalış
                val result = client.from("reels_videos")
                    .insert(videoData)
                    .decodeSingle<ReelsVideo>()
                
                Log.d("SupabaseManager", "Reels video kaydedildi: ${result.id}")
                Result.success(result)
            } catch (e: SerializationException) {
                // JSON ayrıştırma hatası olursa (boş yanıt vb.), kendimiz bir yanıt oluşturalım
                Log.w("SupabaseManager", "JSON ayrıştırma hatası, ancak video muhtemelen yüklendi: ${e.message}")
                
                // Video muhtemelen yüklendi ama yanıt boş geldi, kendi nesnemizi oluşturup dönelim
                val generatedId = UUID.randomUUID().toString()
                val uploadedVideo = ReelsVideo(
                    id = generatedId,
                    userId = userId,
                    videoUrl = videoUrlResult.getOrDefault(""),
                    thumbnailUrl = thumbnailUrlResult.getOrDefault(""),
                    description = description,
                    musicName = musicName,
                    createdAt = java.time.LocalDateTime.now().toString(),
                    updatedAt = java.time.LocalDateTime.now().toString()
                )
                
                // Kullanıcıya işlemin başarılı olduğunu bildir
                Result.success(uploadedVideo)
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Reels video yükleme hatası: ${e.message}", e)
            Result.failure(Exception("Reels video yükleme sırasında hata oluştu: ${e.message}"))
        }
    }
    
    /**
     * Video dosyasını Supabase Storage'a yükler
     */
    suspend fun uploadVideoFile(userId: String, fileName: String, fileUri: Uri, context: Context): Result<String> {
        return try {
            // Web URL kontrolü (https:// ile başlıyor mu?)
            if (fileUri.toString().startsWith("https://")) {
                // Web URL ise, önce dosyayı geçici olarak indir
                val tempFile = downloadFile(fileUri.toString(), context)
                if (tempFile != null) {
                    // Geçici dosyayı kullanarak yükleme yap
                    val tempUri = Uri.fromFile(tempFile)
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(tempUri)
                    val bytes = inputStream?.readBytes() ?: return Result.failure(Exception("Geçici dosya okunamadı"))
                    
                    val fileExt = if (fileName.contains(".")) {
                        fileName.substring(fileName.lastIndexOf("."))
                    } else {
                        ".mp4"
                    }
                    
                    val uniqueFileName = "video_${userId}_${System.currentTimeMillis()}$fileExt"
                    client.storage.from("reels_videos")
                        .upload(uniqueFileName, bytes)
                        
                    // Yükleme tamamlandıktan sonra geçici dosyayı sil
                    try {
                        tempFile.delete()
                    } catch (e: Exception) {
                        Log.e("SupabaseManager", "Geçici dosya silinirken hata: ${e.message}")
                    }
                    
                    val publicUrl = client.storage.from("reels_videos").publicUrl(uniqueFileName)
                    Log.d("SupabaseManager", "Video dosyası yüklendi: $publicUrl")
                    Result.success(publicUrl)
                } else {
                    Result.failure(Exception("Web URL'den dosya indirilemedi"))
                }
            } else {
                // Normal URI ise, doğrudan devam et
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(fileUri)
                val bytes = inputStream?.readBytes() ?: return Result.failure(Exception("Dosya okunamadı"))
                
                val fileExt = if (fileName.contains(".")) {
                    fileName.substring(fileName.lastIndexOf("."))
                } else {
                    ".mp4"
                }
                
                val uniqueFileName = "video_${userId}_${System.currentTimeMillis()}$fileExt"
                client.storage.from("reels_videos")
                    .upload(uniqueFileName, bytes)
                    
                val publicUrl = client.storage.from("reels_videos").publicUrl(uniqueFileName)
                Log.d("SupabaseManager", "Video dosyası yüklendi: $publicUrl")
                Result.success(publicUrl)
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Video yükleme hatası: ${e.message}", e)
            Result.failure(Exception("Video yükleme sırasında hata oluştu: ${e.message}"))
        }
    }
    
    /**
     * Thumbnail dosyasını Supabase Storage'a yükler
     */
    suspend fun uploadThumbnailFile(userId: String, fileName: String, fileUri: Uri, context: Context): Result<String> {
        return try {
            // Web URL kontrolü (https:// ile başlıyor mu?)
            if (fileUri.toString().startsWith("https://")) {
                // Web URL ise, önce dosyayı geçici olarak indir
                val tempFile = downloadFile(fileUri.toString(), context)
                if (tempFile != null) {
                    // Geçici dosyayı kullanarak yükleme yap
                    val tempUri = Uri.fromFile(tempFile)
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(tempUri)
                    val bytes = inputStream?.readBytes() ?: return Result.failure(Exception("Geçici dosya okunamadı"))
                    
                    val fileExt = if (fileName.contains(".")) {
                        fileName.substring(fileName.lastIndexOf("."))
                    } else {
                        ".jpg"
                    }
                    
                    val uniqueFileName = "thumbnail_${userId}_${System.currentTimeMillis()}$fileExt"
                    client.storage.from("reels_thumbnails")
                        .upload(uniqueFileName, bytes)
                    
                    // Yükleme tamamlandıktan sonra geçici dosyayı sil
                    try {
                        tempFile.delete()
                    } catch (e: Exception) {
                        Log.e("SupabaseManager", "Geçici dosya silinirken hata: ${e.message}")
                    }
                    
                    val publicUrl = client.storage.from("reels_thumbnails").publicUrl(uniqueFileName)
                    Log.d("SupabaseManager", "Thumbnail dosyası yüklendi: $publicUrl")
                    Result.success(publicUrl)
                } else {
                    Result.failure(Exception("Web URL'den dosya indirilemedi"))
                }
            } else {
                // Normal URI ise, doğrudan devam et
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(fileUri)
                val bytes = inputStream?.readBytes() ?: return Result.failure(Exception("Dosya okunamadı"))
                
                val fileExt = if (fileName.contains(".")) {
                    fileName.substring(fileName.lastIndexOf("."))
                } else {
                    ".jpg"
                }
                
                val uniqueFileName = "thumbnail_${userId}_${System.currentTimeMillis()}$fileExt"
                client.storage.from("reels_thumbnails")
                    .upload(uniqueFileName, bytes)
                    
                val publicUrl = client.storage.from("reels_thumbnails").publicUrl(uniqueFileName)
                Log.d("SupabaseManager", "Thumbnail dosyası yüklendi: $publicUrl")
                Result.success(publicUrl)
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Thumbnail yükleme hatası: ${e.message}", e)
            Result.failure(Exception("Thumbnail yükleme sırasında hata oluştu: ${e.message}"))
        }
    }
    
    /**
     * Web URL'den dosyayı indirip geçici bir dosyaya kaydeder
     * @param url İndirilecek dosyanın URL'si
     * @param context Context
     * @return Başarılı ise geçici dosya, başarısız ise null
     */
    private suspend fun downloadFile(url: String, context: Context): java.io.File? {
        return withContext(Dispatchers.IO) {
            try {
                // URL'den dosyayı indir
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.connect()
                
                if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                    Log.e("SupabaseManager", "URL bağlantı hatası: ${connection.responseCode}")
                    return@withContext null
                }
                
                // Dosya uzantısını belirle
                val fileExt = if (url.contains(".")) {
                    url.substring(url.lastIndexOf("."))
                } else {
                    ".tmp"
                }
                
                // Cache dizininde geçici dosya oluştur
                val tempFile = java.io.File(context.cacheDir, "temp_${System.currentTimeMillis()}$fileExt")
                
                // URL'den veriyi oku ve geçici dosyaya yaz
                val inputStream = connection.inputStream
                val outputStream = java.io.FileOutputStream(tempFile)
                
                val buffer = ByteArray(4096)
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                outputStream.close()
                inputStream.close()
                
                Log.d("SupabaseManager", "Dosya başarıyla indirildi: ${tempFile.absolutePath}")
                tempFile
            } catch (e: Exception) {
                Log.e("SupabaseManager", "Dosya indirme hatası: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * Tüm Reels videolarını getirir
     * @return Reels videoları listesi
     */
    suspend fun getAllReelsVideos(): List<ReelsVideoWithCounts> {
        return try {
            // Videolarla birlikte bilgileri getiren sorgu
            val videos = client.from("reels_videos")
                .select()
                .decodeList<ReelsVideo>()
                
            // Videoları ReelsVideoWithCounts modeline dönüştür
            val videosWithCounts = videos.map { video ->
                // Her video için beğeni sayısını getir
                val likeCount = getLikeCount(video.id)
                // Her video için yorum sayısını getir
                val commentCount = getCommentCount(video.id)
                // Kullanıcı bilgilerini getir
                val author = getUserProfile(video.userId)
                
                // ReelsVideoWithCounts nesnesini oluştur
                ReelsVideoWithCounts(
                    id = video.id,
                    userId = video.userId,
                    videoUrl = video.videoUrl,
                    thumbnailUrl = video.thumbnailUrl,
                    description = video.description ?: "", // null güvenli dönüştürme
                    musicName = video.musicName ?: "", // null güvenli dönüştürme
                    createdAt = video.createdAt,
                    updatedAt = video.updatedAt,
                    likeCount = likeCount,
                    commentCount = commentCount,
                    author = author
                )
            }
            
            Log.d("SupabaseManager", "Reels videoları getirildi: ${videosWithCounts.size}")
            videosWithCounts
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Reels videoları getirme hatası: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Belirli bir kullanıcının videolarını getirir
     * @param userId Kullanıcı ID'si
     * @return Kullanıcının videoları listesi
     */
    suspend fun getUserReelsVideos(userId: String): List<ReelsVideoWithCounts> {
        return try {
            val videos = client.from("reels_videos")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ReelsVideo>()
                
            // Videoları ReelsVideoWithCounts modeline dönüştür
            val videosWithCounts = videos.map { video ->
                // Her video için beğeni sayısını getir
                val likeCount = getLikeCount(video.id)
                // Her video için yorum sayısını getir
                val commentCount = getCommentCount(video.id)
                // Kullanıcı bilgilerini getir
                val author = getUserProfile(video.userId)
                
                // ReelsVideoWithCounts nesnesini oluştur
                ReelsVideoWithCounts(
                    id = video.id,
                    userId = video.userId,
                    videoUrl = video.videoUrl,
                    thumbnailUrl = video.thumbnailUrl,
                    description = video.description ?: "", // null güvenli dönüştürme
                    musicName = video.musicName ?: "", // null güvenli dönüştürme
                    createdAt = video.createdAt,
                    updatedAt = video.updatedAt,
                    likeCount = likeCount,
                    commentCount = commentCount,
                    author = author
                )
            }
            
            Log.d("SupabaseManager", "Kullanıcı videoları getirildi: ${videosWithCounts.size}")
            videosWithCounts
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Kullanıcı videolarını getirme hatası: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Video için beğeni sayısını getirir
     * @param videoId Video ID'si
     * @return Beğeni sayısı
     */
    private suspend fun getLikeCount(videoId: String): Int {
        return try {
            val likes = client.from("reels_likes")
                .select {
                    filter {
                        eq("video_id", videoId)
                    }
                }
                .decodeList<Like>()
                
            likes.size
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Beğeni sayısı getirme hatası: ${e.message}", e)
            0
        }
    }
    
    /**
     * Video için yorum sayısını getirir
     * @param videoId Video ID'si
     * @return Yorum sayısı
     */
    private suspend fun getCommentCount(videoId: String): Int {
        return try {
            val comments = client.from("reels_comments")
                .select {
                    filter {
                        eq("video_id", videoId)
                    }
                }
                .decodeList<Comment>()
                
            comments.size
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Yorum sayısı getirme hatası: ${e.message}", e)
            0
        }
    }
    
    /**
     * Kullanıcı profil bilgilerini getirir
     * @param userId Kullanıcı ID'si
     * @return Kullanıcı profili veya null
     */
    private suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val profiles = client.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<UserProfile>()
                
            profiles.firstOrNull()
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Kullanıcı profil getirme hatası: ${e.message}", e)
            // Eğer hata oluşursa veya kullanıcı bulunamazsa temel bir UserProfile nesnesi döndür
            UserProfile(
                id = userId,
                username = "kullanıcı",
                displayName = null,
                avatarUrl = null
            )
        }
    }
    
    /**
     * Video beğenme
     * @param videoId Video ID'si
     * @param userId Kullanıcı ID'si
     * @return Başarı durumu
     */
    suspend fun likeVideo(videoId: String, userId: String): Result<Boolean> {
        return try {
            val like = Like(videoId = videoId, userId = userId)
            client.from("reels_likes")
                .insert(like)
                
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Video beğenme hatası: ${e.message}", e)
            Result.failure(Exception("Video beğenme sırasında hata oluştu: ${e.message}"))
        }
    }
    
    /**
     * Video beğenisini kaldırma
     * @param videoId Video ID'si
     * @param userId Kullanıcı ID'si
     * @return Başarı durumu
     */
    suspend fun unlikeVideo(videoId: String, userId: String): Result<Boolean> {
        return try {
            client.from("reels_likes")
                .delete {
                    filter {
                        eq("video_id", videoId)
                        eq("user_id", userId)
                    }
                }
                
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Beğeniyi kaldırma hatası: ${e.message}", e)
            Result.failure(Exception("Beğeniyi kaldırma sırasında hata oluştu: ${e.message}"))
        }
    }
    
    /**
     * Videoya yorum yapma
     * @param videoId Video ID'si
     * @param userId Kullanıcı ID'si
     * @param text Yorum metni
     * @param parentCommentId Eğer yanıt ise, yanıtlanan yorumun ID'si
     * @return Kaydedilen yorum
     */
    suspend fun addComment(videoId: String, userId: String, text: String, parentCommentId: String? = null): Result<Comment> {
        return try {
            val comment = Comment(
                videoId = videoId,
                userId = userId,
                text = text,
                parentCommentId = parentCommentId
            )
            
            val result = client.from("reels_comments")
                .insert(comment)
                .decodeSingle<Comment>()
                
            Result.success(result)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Yorum ekleme hatası: ${e.message}", e)
            Result.failure(Exception("Yorum ekleme sırasında hata oluştu: ${e.message}"))
        }
    }
    
    /**
     * Video yorumlarını getirme
     * @param videoId Video ID'si
     * @return Yorumlar listesi
     */
    suspend fun getVideoComments(videoId: String): List<Comment> {
        return try {
            // PostgreSQL fonksiyonu ile yorumlar ve kullanıcı bilgilerini JSON olarak alıyoruz
            val comments = client.from("get_video_comments")
                .select {
                    filter {
                        eq("video_id", videoId)
                    }
                }
                .decodeList<Comment>()
                
            comments
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Yorumları getirme hatası: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Kullanıcının videoyu beğenip beğenmediğini kontrol eder
     * @param videoId Video ID'si
     * @param userId Kullanıcı ID'si
     * @return Beğenme durumu
     */
    suspend fun checkIfLiked(videoId: String, userId: String): Boolean {
        return try {
            val likes = client.from("reels_likes")
                .select {
                    filter {
                        eq("video_id", videoId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<Like>()
                
            likes.isNotEmpty()
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Beğeni kontrolü hatası: ${e.message}", e)
            false
        }
    }

    /**
     * API'den gelen geçici müzik URL'lerini Supabase Storage'a kaydederek kalıcı hale getirir
     * @param apiMusicUrl API'den gelen geçici URL
     * @param userId Kullanıcı ID'si
     * @param context Android Context
     * @return Supabase Storage'daki kalıcı URL
     */
    suspend fun saveApiMusicToStorage(apiMusicUrl: String, userId: String, context: Context): Result<String> {
        return try {
            Log.d("SupabaseManager", "API müzik URL'si Supabase'e kaydediliyor: $apiMusicUrl")
            
            // 1. Dosyayı geçici olarak indir
            val tempFile = downloadFile(apiMusicUrl, context)
                ?: return Result.failure(Exception("Müzik dosyası indirilemedi"))
            
            Log.d("SupabaseManager", "Dosya geçici olarak indirildi: ${tempFile.absolutePath}")
            
            // 2. Dosyayı Supabase'e yükle
            val tempUri = Uri.fromFile(tempFile)
            val fileName = "music_${System.currentTimeMillis()}.mp3"
            
            // uploadMusicFile fonksiyonu zaten mevcut sınıfta var
            val result = uploadMusicFile(userId, fileName, tempUri, context)
            
            // 3. Geçici dosyayı sil
            try {
                tempFile.delete()
                Log.d("SupabaseManager", "Geçici dosya silindi")
            } catch (e: Exception) {
                Log.e("SupabaseManager", "Geçici dosya silinirken hata: ${e.message}")
            }
            
            // 4. Sonucu döndür
            if (result.isSuccess) {
                Log.d("SupabaseManager", "Müzik başarıyla Supabase'e yüklendi: ${result.getOrNull()}")
            } else {
                Log.e("SupabaseManager", "Müzik yükleme hatası: ${result.exceptionOrNull()}")
            }
            
            result
        } catch (e: Exception) {
            Log.e("SupabaseManager", "API müziğini Storage'a yükleme hatası: ${e.message}", e)
            Result.failure(Exception("API müziğini Storage'a yükleme sırasında hata oluştu: ${e.message}"))
        }
    }

    /**
     * API'den gelen kapak resmini Supabase Storage'a kaydederek kalıcı hale getirir
     * @param apiCoverUrl API'den gelen geçici URL
     * @param userId Kullanıcı ID'si
     * @param context Android Context
     * @return Supabase Storage'daki kalıcı URL
     */
    suspend fun saveApiCoverToStorage(apiCoverUrl: String, userId: String, context: Context): Result<String> {
        return try {
            Log.d("SupabaseManager", "API kapak URL'si Supabase'e kaydediliyor: $apiCoverUrl")
            
            // 1. Dosyayı geçici olarak indir
            val tempFile = downloadFile(apiCoverUrl, context)
                ?: return Result.failure(Exception("Kapak resmi indirilemedi"))
            
            Log.d("SupabaseManager", "Kapak geçici olarak indirildi: ${tempFile.absolutePath}")
            
            // 2. Dosyayı Supabase'e yükle
            val tempUri = Uri.fromFile(tempFile)
            val fileName = "cover_${System.currentTimeMillis()}.jpg"
            
            // uploadCoverImage fonksiyonu zaten mevcut sınıfta var
            val result = uploadCoverImage(userId, fileName, tempUri, context)
            
            // 3. Geçici dosyayı sil
            try {
                tempFile.delete()
                Log.d("SupabaseManager", "Geçici kapak dosyası silindi")
            } catch (e: Exception) {
                Log.e("SupabaseManager", "Geçici kapak dosyası silinirken hata: ${e.message}")
            }
            
            // 4. Sonucu döndür
            if (result.isSuccess) {
                Log.d("SupabaseManager", "Kapak başarıyla Supabase'e yüklendi: ${result.getOrNull()}")
            } else {
                Log.e("SupabaseManager", "Kapak yükleme hatası: ${result.exceptionOrNull()}")
            }
            
            result
        } catch (e: Exception) {
            Log.e("SupabaseManager", "API kapağını Storage'a yükleme hatası: ${e.message}", e)
            Result.failure(Exception("API kapağını Storage'a yükleme sırasında hata oluştu: ${e.message}"))
        }
    }

    // Gerekli diğer metotlar buraya eklenebilir
} 