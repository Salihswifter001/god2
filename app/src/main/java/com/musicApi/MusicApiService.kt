package com.musicApi

import android.content.Context
import android.util.Log
import com.aihackathonkarisacikartim.god2.BuildConfig
import com.aihackathonkarisacikartim.god2.GeneratedMusicData
import com.aihackathonkarisacikartim.god2.SupabaseManager
// import com.supabase.SupabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

// API yanıtı için veri sınıfları
@Serializable
data class MusicApiResponse(
    val success: Boolean,
    val data: MusicApiData? = null,
    val error: String? = null
)

@Serializable
data class MusicApiData(
    val music_url: String,
    val cover_url: String,
    val title: String? = null
)

/**
 * Müzik API servisi - Müzik oluşturma API'si ile iletişim kurar
 * ve sonuçları Supabase'e yükler.
 */
class MusicApiService(private val context: Context) {
    // API configuration from BuildConfig (secure)
    private val apiBaseUrl = if (BuildConfig.MUSIC_API_BASE_URL.isNotEmpty()) {
        BuildConfig.MUSIC_API_BASE_URL
    } else {
        "https://apibox.erweima.ai/api/v1" // Fallback URL
    }
    
    // API key from BuildConfig (secure)
    private val apiKey = if (BuildConfig.MUSIC_API_KEY.isNotEmpty()) {
        "Bearer ${BuildConfig.MUSIC_API_KEY}"
    } else {
        throw IllegalStateException("Music API key is not configured. Please add MUSIC_API_KEY to local.properties")
    }
    
    // Supabase manager instance
    private val supabaseManager = SupabaseManager()
    
    // OkHttp client
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    
    /**
     * Müzik API'sine istek gönderir ve task_id döndürür
     * @param prompt Müzik oluşturmak için metin promptu
     * @param genre Müzik türü
     * @param title Müzik başlığı
     * @return İşlem başarılıysa task_id, değilse hata
     */
    suspend fun generateMusic(prompt: String, genre: String, title: String = ""): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Kullanıcı kontrolü
                val currentUser = supabaseManager.getCurrentUser()
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("Müzik oluşturmak için giriş yapmalısınız."))
                }
                
                Log.d("MusicApiService", "API isteği gönderiliyor: prompt=$prompt, genre=$genre, title=$title")
                
                // İsteği belirtilen formatta oluştur
                val mediaType = "application/json".toMediaType()
                val finalTitle = if (title.isBlank()) prompt.take(50) else title
                
                // JSON objesini oluştur (otomatik escape)
                val jsonObject = JSONObject().apply {
                    put("prompt", "")
                    put("style", prompt)
                    put("title", finalTitle)
                    put("customMode", true)
                    put("instrumental", true)
                    put("model", "V4_5PLUS")
                    put("negativeTags", "")
                    put("callBackUrl", "https://api.example.com/callback")
                }
                val jsonString = jsonObject.toString()
                
                Log.d("MusicApiService", "API istek gövdesi: $jsonString")
                
                val body = jsonString.toRequestBody(mediaType)
                val request = Request.Builder()
                  .url("$apiBaseUrl/generate")
                  .post(body)
                  .addHeader("Content-Type", "application/json")
                  .addHeader("Accept", "application/json")
                  .addHeader("Authorization", apiKey)
                  .build()
                
                try {
                    // İsteği gönder
                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()
                        
                        // Yanıt içeriğini logla
                        Log.d("MusicApiService", "API yanıtı: ${response.code}, body: $responseBody")
                        
                        if (!response.isSuccessful) {
                            Log.e("MusicApiService", "API isteği başarısız: ${response.code}, body: $responseBody")
                            throw IOException("API isteği başarısız: ${response.code}")
                        }
                        
                        // Yanıtı işle
                        if (responseBody == null) {
                            Log.e("MusicApiService", "API yanıtı boş")
                            throw IOException("API yanıtı boş")
                        }
                        
                        // Response JSON'ı parse et
                        val responseJson = JSONObject(responseBody)
                        
                        // Yeni format: code/msg kontrol et
                        val code = responseJson.optInt("code", 0)
                        
                        if (code == 200) {
                            val data = responseJson.getJSONObject("data")
                            // API, taskId döndürüyor (task_id değil)
                            val taskId = data.getString("taskId")
                            
                            Log.d("MusicApiService", "API isteği başarılı, task_id: $taskId")
                            return@withContext Result.success(taskId)
                        } else {
                            val msg = responseJson.optString("msg", "Bilinmeyen hata")
                            Log.e("MusicApiService", "API yanıtı başarısız: $msg (kod: $code)")
                            return@withContext Result.failure(Exception("API yanıtı başarısız: $msg (kod: $code)"))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MusicApiService", "API isteği hatası: ${e.message}", e)
                    throw e
                }
            } catch (e: Exception) {
                Log.e("MusicApiService", "Müzik oluşturma hatası: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }
    }
    
    /**
     * Verilen URL'den dosyayı indirir
     * @param url İndirilecek dosyanın URL'si
     * @return İndirilen dosyanın bayt dizisi
     */
    private suspend fun downloadFile(url: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .build()
                
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Dosya indirme başarısız: ${response.code}")
                }
                
                response.body?.bytes() ?: throw IOException("Boş dosya")
            }
        }
    }
    
    /**
     * Vokal ile müzik oluşturma
     * @param prompt Müzik için prompt
     * @param genre Müzik türü
     * @param lyrics Vokal metni/şarkı sözleri
     * @param title Müzik başlığı
     */
    suspend fun generateMusicWithLyrics(prompt: String, genre: String, lyrics: String, title: String = ""): Result<String> {
        Log.d("MusicApiService", "Şarkı sözleri ile müzik oluşturuluyor: prompt=$prompt, genre=$genre, lyrics=$lyrics, title=$title")
        return withContext(Dispatchers.IO) {
            try {
                // Kullanıcı kontrolü
                val currentUser = supabaseManager.getCurrentUser()
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("Müzik oluşturmak için giriş yapmalısınız."))
                }
                
                Log.d("MusicApiService", "API isteği gönderiliyor: prompt=$prompt, genre=$genre, lyrics=$lyrics, title=$title")
                
                // İsteği belirtilen formatta oluştur
                val mediaType = "application/json".toMediaType()
                val finalTitle = if (title.isBlank()) prompt.take(50) else title
                
                // JSON objesini oluştur (otomatik escape)
                val jsonObject = JSONObject().apply {
                    put("prompt", lyrics)
                    put("style", prompt)
                    put("title", finalTitle)
                    put("customMode", true)
                    put("instrumental", false)
                    put("model", "V4_5PLUS")
                    put("negativeTags", "")
                    put("callBackUrl", "https://api.example.com/callback")
                }
                val jsonString = jsonObject.toString()
                
                Log.d("MusicApiService", "API istek gövdesi: $jsonString")
                
                val body = jsonString.toRequestBody(mediaType)
                val request = Request.Builder()
                  .url("$apiBaseUrl/generate")
                  .post(body)
                  .addHeader("Content-Type", "application/json")
                  .addHeader("Accept", "application/json")
                  .addHeader("Authorization", apiKey)
                  .build()
                
                try {
                    // İsteği gönder
                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()
                        
                        // Yanıt içeriğini logla
                        Log.d("MusicApiService", "API yanıtı: ${response.code}, body: $responseBody")
                        
                        if (!response.isSuccessful) {
                            Log.e("MusicApiService", "API isteği başarısız: ${response.code}, body: $responseBody")
                            throw IOException("API isteği başarısız: ${response.code}")
                        }
                        
                        // Yanıtı işle
                        if (responseBody == null) {
                            Log.e("MusicApiService", "API yanıtı boş")
                            throw IOException("API yanıtı boş")
                        }
                        
                        // Response JSON'ı parse et
                        val responseJson = JSONObject(responseBody)
                        
                        // Yeni format: code/msg kontrol et
                        val code = responseJson.optInt("code", 0)
                        
                        if (code == 200) {
                            val data = responseJson.getJSONObject("data")
                            // API, taskId döndürüyor (task_id değil)
                            val taskId = data.getString("taskId")
                            
                            Log.d("MusicApiService", "API isteği başarılı, task_id: $taskId")
                            return@withContext Result.success(taskId)
                        } else {
                            val msg = responseJson.optString("msg", "Bilinmeyen hata")
                            Log.e("MusicApiService", "API yanıtı başarısız: $msg (kod: $code)")
                            return@withContext Result.failure(Exception("API yanıtı başarısız: $msg (kod: $code)"))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MusicApiService", "API isteği hatası: ${e.message}", e)
                    throw e
                }
            } catch (e: Exception) {
                Log.e("MusicApiService", "Müzik oluşturma hatası: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }
    }
    
    /**
     * Müzik üretim durumunu kontrol eder
     * @param taskId Kontrol edilecek görev ID'si
     * @param userId Kullanıcı ID'si (arka planda çalışma için)
     * @return Tamamlandıysa müzik verisi, işlem devam ediyorsa null, başarısızsa hata
     */
    suspend fun checkMusicGenerationStatus(taskId: String, userId: String? = null): Result<GeneratedMusicData?> {
        return withContext(Dispatchers.IO) {
            try {
                // userId parametresi verilmişse onu kullan, yoksa auth'dan al
                val effectiveUserId = if (!userId.isNullOrEmpty()) {
                    userId
                } else {
                    val currentUser = supabaseManager.getCurrentUser()
                    if (currentUser == null) {
                        return@withContext Result.failure(Exception("Müzik durumunu kontrol etmek için giriş yapmalısınız."))
                    }
                    currentUser.id
                }
                
                Log.d("MusicApiService", "Müzik üretim durumu kontrol ediliyor: taskId=$taskId")
                
                // Durum kontrolü isteği oluştur
                val request = Request.Builder()
                  .url("$apiBaseUrl/generate/record-info?taskId=$taskId")
                  .get()
                  .addHeader("Accept", "application/json")
                  .addHeader("Authorization", apiKey)
                  .build()
                
                // İsteği gönder
                client.newCall(request).execute().use { response ->
                    val responseCode = response.code
                    Log.d("MusicApiService", "API yanıt kodu: $responseCode")
                    
                    if (!response.isSuccessful) {
                        // 404 durumunda da null döndürerek işleme devam etmesini sağlayalım
                        if (responseCode == 404) {
                            Log.d("MusicApiService", "Task bulunamadı (404), işlem devam ediyor olabilir")
                            return@withContext Result.success(null) // İşlem devam ediyor olabilir
                        }
                        
                        Log.e("MusicApiService", "Durum kontrolü başarısız: $responseCode")
                        throw IOException("Durum kontrolü başarısız: $responseCode")
                    }
                    
                    // Yanıtı işle
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        Log.e("MusicApiService", "API yanıtı boş")
                        return@withContext Result.success(null) // İşlem devam ediyor olabilir
                    }
                    
                    // Detaylı loglama
                    Log.d("MusicApiService", "Status API yanıtı: $responseBody")
                    
                    try {
                        val responseJson = JSONObject(responseBody)
                        val code = responseJson.optInt("code", 0)
                        
                        Log.d("MusicApiService", "API yanıt kodu: $code")
                        
                        // API başarılı yanıt verdi mi kontrol et
                        if (code == 200) {
                            val data = responseJson.optJSONObject("data")
                            if (data != null) {
                                Log.d("MusicApiService", "Data objesi: $data")
                                
                                // Tüm anahtarları logla
                                val keys = data.keys()
                                val keyList = mutableListOf<String>()
                                while (keys.hasNext()) {
                                    keyList.add(keys.next())
                                }
                                Log.d("MusicApiService", "Data anahtarları: $keyList")
                                
                                // Status veya durum bilgisi var mı?
                                val status = data.optString("status", "")
                                val recordStatus = data.optString("recordStatus", "")
                                val actualStatus = if (status.isNotEmpty()) status else recordStatus
                                
                                Log.d("MusicApiService", "Status: $actualStatus")
                                
                                // Durum bilgisi success, done veya completed ise işlem tamamlanmış demektir
                                if (actualStatus.equals("success", ignoreCase = true) || 
                                    actualStatus.equals("done", ignoreCase = true) || 
                                    actualStatus.equals("completed", ignoreCase = true)) {
                                    
                                    Log.d("MusicApiService", "Müzik üretimi tamamlandı!")
                                    
                                    // API yanıtını detaylı logla
                                    Log.d("MusicApiService", "Tüm data objesi: $data")
                                    
                                    var musicUrl = ""
                                    var coverUrl = ""
                                    var title = "Oluşturulan Müzik"
                                    var prompt = ""
                                    var tags = ""
                                    var duration = 0.0
                                    
                                    // Önce status kontrolü yap
                                    val status = data.optString("status", "")
                                    if (status.equals("SUCCESS", ignoreCase = true)) {
                                        Log.d("MusicApiService", "API durumu: SUCCESS - Yeni format inceleniyor")
                                        
                                        // Özel veri yapısını incele - response->sunoData dizisi
                                        if (data.has("response") && !data.isNull("response")) {
                                            val response = data.getJSONObject("response")
                                            Log.d("MusicApiService", "Response objesi: $response")
                                            
                                            if (response.has("sunoData") && !response.isNull("sunoData")) {
                                                val sunoData = response.getJSONArray("sunoData")
                                                Log.d("MusicApiService", "SunoData array uzunluğu: ${sunoData.length()}")
                                                
                                                if (sunoData.length() > 0) {
                                                    // İlk sonucu kullan (birden fazla olabilir)
                                                    val result = sunoData.getJSONObject(0)
                                                    Log.d("MusicApiService", "Kullanılan sonuç: $result")
                                                    
                                                    // URL'leri direkt olarak al
                                                    if (result.has("audioUrl") && !result.isNull("audioUrl")) {
                                                        musicUrl = result.getString("audioUrl")
                                                        Log.d("MusicApiService", "Müzik URL'si (audioUrl): $musicUrl")
                                                    } else if (result.has("sourceAudioUrl") && !result.isNull("sourceAudioUrl")) {
                                                        musicUrl = result.getString("sourceAudioUrl")
                                                        Log.d("MusicApiService", "Müzik URL'si (sourceAudioUrl): $musicUrl")
                                                    }
                                                    
                                                    if (result.has("imageUrl") && !result.isNull("imageUrl")) {
                                                        coverUrl = result.getString("imageUrl")
                                                        Log.d("MusicApiService", "Kapak URL'si (imageUrl): $coverUrl")
                                                    } else if (result.has("sourceImageUrl") && !result.isNull("sourceImageUrl")) {
                                                        coverUrl = result.getString("sourceImageUrl")
                                                        Log.d("MusicApiService", "Kapak URL'si (sourceImageUrl): $coverUrl")
                                                    }
                                                    
                                                    // Meta verileri al
                                                    if (result.has("title") && !result.isNull("title")) {
                                                        title = result.getString("title")
                                                    }
                                                    
                                                    if (result.has("prompt") && !result.isNull("prompt")) {
                                                        prompt = result.getString("prompt")
                                                    }
                                                    
                                                    if (result.has("tags") && !result.isNull("tags")) {
                                                        tags = result.getString("tags")
                                                    }
                                                    
                                                    if (result.has("duration") && !result.isNull("duration")) {
                                                        duration = result.getDouble("duration")
                                                    }
                                                }
                                            }
                                        }
                                    } else if (data.has("callbackType") && data.getString("callbackType") == "complete") {
                                        // Eski kodda bulunan callback tipi kontrolleri
                                        Log.d("MusicApiService", "CallbackType: complete - İşleniyor")
                                        
                                        // İç içe data array'ini kontrol et
                                        if (data.has("data") && !data.isNull("data")) {
                                            val dataArray = data.getJSONArray("data")
                                            Log.d("MusicApiService", "Data array uzunluğu: ${dataArray.length()}")
                                            
                                            if (dataArray.length() > 0) {
                                                // İlk sonucu kullan (birden fazla olabilir)
                                                val result = dataArray.getJSONObject(0)
                                                Log.d("MusicApiService", "Kullanılan sonuç: $result")
                                                
                                                // Kaynak URL'leri doğrudan API yanıtından al
                                                if (result.has("source_audio_url") && !result.isNull("source_audio_url")) {
                                                    musicUrl = result.getString("source_audio_url")
                                                    Log.d("MusicApiService", "Müzik URL'si: $musicUrl")
                                                }
                                                
                                                if (result.has("source_image_url") && !result.isNull("source_image_url")) {
                                                    coverUrl = result.getString("source_image_url")
                                                    Log.d("MusicApiService", "Kapak URL'si: $coverUrl")
                                                }
                                                
                                                // Meta verileri al
                                                if (result.has("title") && !result.isNull("title")) {
                                                    title = result.getString("title")
                                                }
                                                
                                                if (result.has("prompt") && !result.isNull("prompt")) {
                                                    prompt = result.getString("prompt")
                                                }
                                                
                                                if (result.has("tags") && !result.isNull("tags")) {
                                                    tags = result.getString("tags")
                                                }
                                                
                                                if (result.has("duration") && !result.isNull("duration")) {
                                                    duration = result.getDouble("duration")
                                                }
                                            }
                                        }
                                    } else {
                                        // Diğer API yanıt formatları
                                        Log.d("MusicApiService", "Standart başarı yanıtı - Alternatif format kullanılıyor")
                                        
                                        // Eğer data içinde result alanı varsa, onu kontrol et
                                        val resultObject = if (data.has("result")) data.getJSONObject("result") else data
                                        Log.d("MusicApiService", "Result objesi: $resultObject")
                                        
                                        // Olası tüm alanlarda müzik URL'sini ara
                                        val musicFields = listOf("musicUrl", "music_url", "sourceAudioUrl", "source_audio_url", 
                                                               "audio", "audioUrl", "audio_url", "url", "mp3Url", "mp3_url")
                                        
                                        for (field in musicFields) {
                                            if (resultObject.has(field) && !resultObject.isNull(field)) {
                                                musicUrl = resultObject.getString(field)
                                                Log.d("MusicApiService", "Müzik URL'si bulundu: $field = $musicUrl")
                                                break
                                            }
                                        }
                                        
                                        // Olası tüm alanlarda kapak URL'sini ara  
                                        val coverFields = listOf("coverUrl", "cover_url", "sourceImageUrl", "source_image_url",
                                                              "cover", "image", "imageUrl", "image_url", "coverImageUrl", "cover_image_url")
                                        
                                        for (field in coverFields) {
                                            if (resultObject.has(field) && !resultObject.isNull(field)) {
                                                coverUrl = resultObject.getString(field)
                                                Log.d("MusicApiService", "Kapak URL'si bulundu: $field = $coverUrl")
                                                break
                                            }
                                        }
                                        
                                        // Başlık, prompt ve diğer meta verileri bul
                                        if (resultObject.has("title") && !resultObject.isNull("title")) {
                                            title = resultObject.getString("title")
                                        } else if (data.has("title") && !data.isNull("title")) {
                                            title = data.getString("title")
                                        }
                                        
                                        if (resultObject.has("prompt") && !resultObject.isNull("prompt")) {
                                            prompt = resultObject.getString("prompt")
                                        } else if (data.has("prompt") && !data.isNull("prompt")) {
                                            prompt = data.getString("prompt")
                                        }
                                        
                                        if (resultObject.has("tags") && !resultObject.isNull("tags")) {
                                            tags = resultObject.getString("tags")
                                        } else if (data.has("tags") && !data.isNull("tags")) {
                                            tags = data.getString("tags")
                                        }
                                        
                                        if (resultObject.has("duration") && !resultObject.isNull("duration")) {
                                            duration = resultObject.getDouble("duration")
                                        } else if (data.has("duration") && !data.isNull("duration")) {
                                            duration = data.getDouble("duration")
                                        }
                                    }
                                    
                                    // URL'leri tekrar logla
                                    Log.d("MusicApiService", "Bulunan son URL'ler: audio=$musicUrl, image=$coverUrl")
                                    
                                    if (musicUrl.isNotEmpty()) {
                                        // Eğer kapak URL'si yoksa, varsayılan bir kapak resmi kullan
                                        if (coverUrl.isEmpty()) {
                                            Log.d("MusicApiService", "Kapak URL'si bulunamadı, varsayılan kullanılacak")
                                            coverUrl = "https://via.placeholder.com/500x500.png?text=Müzik"
                                        }
                                        
                                        Log.d("MusicApiService", "Dosyalar indiriliyor ve veritabanına kaydediliyor...")
                                        
                                        try {
                                            // API URL'lerini Supabase Storage'a kaydet
                                            Log.d("MusicApiService", "API URL'leri Supabase Storage'a yükleniyor")
                                            
                                            // Müzik dosyasını Supabase'e kaydet
                                            val musicUrlResult = supabaseManager.saveApiMusicToStorage(musicUrl, effectiveUserId, context)
                                            if (musicUrlResult.isFailure) {
                                                Log.e("MusicApiService", "Müzik dosyası Supabase'e yüklenemedi: ${musicUrlResult.exceptionOrNull()?.message}")
                                                return@withContext Result.failure(Exception("Müzik dosyası Supabase'e yüklenemedi: ${musicUrlResult.exceptionOrNull()?.message}"))
                                            }
                                            
                                            // Kapak resmini Supabase'e kaydet
                                            val coverUrlResult = supabaseManager.saveApiCoverToStorage(coverUrl, effectiveUserId, context)
                                            if (coverUrlResult.isFailure) {
                                                Log.e("MusicApiService", "Kapak resmi Supabase'e yüklenemedi: ${coverUrlResult.exceptionOrNull()?.message}")
                                                return@withContext Result.failure(Exception("Kapak resmi Supabase'e yüklenemedi: ${coverUrlResult.exceptionOrNull()?.message}"))
                                            }
                                            
                                            // Kalıcı URL'leri al
                                            val permanentMusicUrl = musicUrlResult.getOrNull() ?: ""
                                            val permanentCoverUrl = coverUrlResult.getOrNull() ?: ""
                                            
                                            Log.d("MusicApiService", "Kalıcı URL'ler: müzik=$permanentMusicUrl, kapak=$permanentCoverUrl")
                                            
                                            // Veritabanına kalıcı URL'lerle kaydet
                                            var musicId = "" // Varsayılan boş değer
                                            
                                            // SunoData içinden id'yi almaya çalış
                                            if (data.has("response") && !data.isNull("response")) {
                                                val response = data.getJSONObject("response")
                                                if (response.has("sunoData") && !response.isNull("sunoData")) {
                                                    val sunoData = response.getJSONArray("sunoData")
                                                    if (sunoData.length() > 0) {
                                                        val firstItem = sunoData.getJSONObject(0)
                                                        if (firstItem.has("id") && !firstItem.isNull("id")) {
                                                            musicId = firstItem.getString("id")
                                                            Log.d("MusicApiService", "API'den gelen müzik ID'si: $musicId")
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            val musicData = GeneratedMusicData(
                                                id = UUID.randomUUID().toString(),
                                                userId = effectiveUserId,
                                                title = title,
                                                prompt = prompt,
                                                genre = tags,
                                                musicUrl = permanentMusicUrl, // Kalıcı Supabase URL'si
                                                coverUrl = permanentCoverUrl, // Kalıcı Supabase URL'si
                                                musicId = musicId, // API'den gelen müzik ID'si
                                                duration = duration.toLong()
                                            )
                                            
                                            // Önce bu müziğin zaten kaydedilip kaydedilmediğini kontrol et
                                            val musicAlreadyExists = supabaseManager.checkMusicExistsByMusicId(
                                                musicId,
                                                effectiveUserId
                                            )
                                            
                                            if (musicAlreadyExists) {
                                                Log.d("MusicApiService", "Müzik zaten kaydedilmiş, tekrar eklenmeyecek: $musicId")
                                                // Zaten kayıtlı olan müziği dön
                                                return@withContext Result.success(musicData)
                                            } else {
                                                Log.d("MusicApiService", "Veritabanına kaydetme başlıyor: $musicData")
                                                val saveResult = supabaseManager.saveGeneratedMusic(musicData)
                                                if (saveResult.isSuccess) {
                                                    Log.d("MusicApiService", "Müzik başarıyla kaydedildi: ${musicData.id}")
                                                    return@withContext Result.success(musicData)
                                                } else {
                                                    Log.e("MusicApiService", "Veritabanına kaydetme hatası: ${saveResult.exceptionOrNull()?.message}")
                                                    return@withContext Result.failure(
                                                        Exception("Veritabanına kaydetme hatası: ${saveResult.exceptionOrNull()?.message}")
                                                    )
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("MusicApiService", "Dosya indirme veya yükleme hatası: ${e.message}", e)
                                            Log.e("MusicApiService", "Hata detayı", e)
                                            return@withContext Result.failure(e)
                                        }
                                    } else {
                                        Log.e("MusicApiService", "Müzik URL'si bulunamadı! Tüm data: $data")
                                        return@withContext Result.failure(Exception("Müzik URL'si bulunamadı"))
                                    }
                                } else if (actualStatus.equals("processing", ignoreCase = true) || 
                                          actualStatus.equals("in_progress", ignoreCase = true) ||
                                          actualStatus.isEmpty()) {
                                    // İşlem devam ediyor
                                    Log.d("MusicApiService", "Müzik üretimi devam ediyor: $actualStatus")
                                    return@withContext Result.success(null)
                                } else if (actualStatus.equals("failed", ignoreCase = true) || 
                                          actualStatus.equals("error", ignoreCase = true)) {
                                    // İşlem başarısız oldu
                                    val errorMsg = data.optString("error", "Bilinmeyen hata")
                                    Log.e("MusicApiService", "Müzik üretimi başarısız: $errorMsg")
                                    return@withContext Result.failure(Exception("Müzik üretimi başarısız: $errorMsg"))
                                } else {
                                    // Bilinmeyen durum
                                    Log.d("MusicApiService", "Bilinmeyen durum: $actualStatus, yeniden kontrol edilecek")
                                    return@withContext Result.success(null)
                                }
                            } else {
                                // Data objesi yok ama kod 200
                                Log.d("MusicApiService", "Data objesi bulunamadı ancak kod 200, işlem devam ediyor olabilir")
                                return@withContext Result.success(null)
                            }
                } else {
                            // API başarısız yanıt verdi
                            val msg = responseJson.optString("msg", "Bilinmeyen hata")
                            Log.e("MusicApiService", "API yanıtı başarısız: $msg (kod: $code)")
                            return@withContext Result.failure(Exception("API yanıtı başarısız: $msg (kod: $code)"))
                        }
                    } catch (e: Exception) {
                        Log.e("MusicApiService", "API yanıtı JSON ayrıştırma hatası: ${e.message}", e)
                        Log.e("MusicApiService", "Ham yanıt: $responseBody")
                        return@withContext Result.success(null) // API yanıtı beklenmeyen biçimde, ama yine de devam et
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicApiService", "Durum kontrolü hatası: ${e.message}")
                return@withContext Result.failure(e)
            }
        }
    }
} 