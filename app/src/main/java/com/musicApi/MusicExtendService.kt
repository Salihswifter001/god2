package com.musicApi

import android.content.Context
import android.util.Log
import com.aihackathonkarisacikartim.god2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Müzik Uzatma API servisi - Mevcut bir müziği uzatmak için API ile iletişim kurar.
 */
class MusicExtendService(private val context: Context) {
    // API URL
    private val apiBaseUrl = "https://apibox.erweima.ai/api/v1"
    
    // API anahtarı
    private val apiKey = if (BuildConfig.MUSIC_API_KEY.isNotEmpty()) {
        "Bearer ${BuildConfig.MUSIC_API_KEY}"
    } else {
        throw IllegalStateException("Music API key not configured")
    }
    
    // API URL'sini döndüren yardımcı fonksiyon
    fun getApiBaseUrl(): String {
        return apiBaseUrl
    }
    
    // API anahtarını döndüren yardımcı fonksiyon
    fun getApiKey(): String {
        return apiKey
    }
    
    // OkHttp client
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            println("OkHttp: ${request.method} isteği gönderiliyor: ${request.url}")
            
            try {
                val response = chain.proceed(request)
                println("OkHttp: ${response.code} yanıtı alındı: ${response.message}")
                response
            } catch (e: Exception) {
                println("OkHttp bağlantı hatası: ${e.message}")
                throw e
            }
        }
        .build()
    
    /**
     * İnternet bağlantısı kontrolü yapar
     */
    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null
        } catch (e: Exception) {
            Log.e("MusicExtendService", "Bağlantı kontrolü hatası: ${e.message}")
            false
        }
    }
    
    /**
     * Mevcut bir müziği uzatmak için API isteği gönderir
     * @param defaultParamFlag true ise özel parametreler kullanılır, false ise kaynak müziğin parametreleri kullanılır
     * @param audioId Uzatılacak kaynak müziğin ID'si
     * @param prompt Müziğin nasıl uzatılacağına dair açıklama (defaultParamFlag true ise gerekli)
     * @param style Müzik stili (defaultParamFlag true ise gerekli)
     * @param title Müzik başlığı (defaultParamFlag true ise gerekli)
     * @param continueAt Uzatmanın başlayacağı zaman noktası (saniye cinsinden, defaultParamFlag true ise gerekli)
     * @param model Kullanılacak model versiyonu (kaynak müzikle tutarlı olmalı)
     * @param negativeTags Oluşturmadan hariç tutulacak müzik stilleri
     * @param callBackUrl Task tamamlandığında bildirim alınacak URL
     * @return İşlem başarılıysa task_id, değilse hata
     */
    suspend fun extendMusic(
        defaultParamFlag: Boolean,
        audioId: String,
        prompt: String = "",
        style: String = "",
        title: String = "",
        continueAt: Int = 0,
        model: String = "V4_5PLUS",
        negativeTags: String = "",
        callBackUrl: String = "https://api.example.com/callback"
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                println("MusicExtendService.extendMusic çağrıldı: audioId=$audioId")
                Log.d("MusicExtendService", "Müzik uzatma API isteği gönderiliyor: audioId=$audioId")
                
                // İnternet bağlantısı kontrolü
                if (!isNetworkAvailable()) {
                    println("İnternet bağlantısı yok!")
                    return@withContext Result.failure(IOException("İnternet bağlantısı yok"))
                }
                
                // İstek gövdesini oluştur
                val requestBody = buildRequestBody(
                    defaultParamFlag, audioId, prompt, style, title, continueAt, model, negativeTags, callBackUrl
                )
                
                println("API istek gövdesi oluşturuldu, istek hazırlanıyor")
                Log.d("MusicExtendService", "API istek gövdesi: $requestBody")
                
                val mediaType = "application/json".toMediaType()
                val body = requestBody.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("$apiBaseUrl/generate/extend")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", apiKey)
                    .build()
                
                try {
                    // İsteği gönder
                    println("API isteği gönderiliyor: ${request.url}")
                    
                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()
                        
                        println("API yanıtı: ${response.code}, yanıt: $responseBody")
                        
                        // Yanıt içeriğini logla
                        Log.d("MusicExtendService", "API yanıtı: ${response.code}, body: $responseBody")
                        
                        if (!response.isSuccessful) {
                            Log.e("MusicExtendService", "API isteği başarısız: ${response.code}, body: $responseBody")
                            throw IOException("API isteği başarısız: ${response.code}")
                        }
                        
                        // Yanıtı işle
                        if (responseBody == null) {
                            Log.e("MusicExtendService", "API yanıtı boş")
                            throw IOException("API yanıtı boş")
                        }
                        
                        // Response JSON'ı parse et
                        val responseJson = JSONObject(responseBody)
                        
                        // code/msg kontrolü yap
                        val code = responseJson.optInt("code", 0)
                        
                        if (code == 200) {
                            val data = responseJson.optJSONObject("data")
                            if (data != null && data.has("task_id")) {
                                val taskId = data.getString("task_id")
                                Log.d("MusicExtendService", "API isteği başarılı, task_id: $taskId")
                                return@withContext Result.success(taskId)
                            } else {
                                // Alternatif olarak taskId alanını kontrol et
                                if (data != null && data.has("taskId")) {
                                    val taskId = data.getString("taskId")
                                    Log.d("MusicExtendService", "API isteği başarılı, taskId: $taskId")
                                    return@withContext Result.success(taskId)
                                } else {
                                    Log.e("MusicExtendService", "API yanıtında task_id/taskId bulunamadı: $responseBody")
                                    throw IOException("API yanıtında task_id bulunamadı")
                                }
                            }
                        } else {
                            val msg = responseJson.optString("msg", "Bilinmeyen hata")
                            Log.e("MusicExtendService", "API yanıtı başarısız: $msg (kod: $code)")
                            return@withContext Result.failure(Exception("API yanıtı başarısız: $msg (kod: $code)"))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MusicExtendService", "API isteği hatası: ${e.message}", e)
                    throw e
                }
            } catch (e: Exception) {
                Log.e("MusicExtendService", "Müzik uzatma hatası: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }
    }
    
    /**
     * API isteği için JSON gövdesini oluşturur
     */
    private fun buildRequestBody(
        defaultParamFlag: Boolean,
        audioId: String,
        prompt: String,
        style: String,
        title: String,
        continueAt: Int,
        model: String,
        negativeTags: String,
        callBackUrl: String
    ): String {
        // JSON objesini oluştur (otomatik escape)
        val jsonObject = JSONObject().apply {
            put("defaultParamFlag", defaultParamFlag)
            put("audioId", audioId)
            
            if (defaultParamFlag) {
                // Özel parametrelerle JSON oluştur
                put("prompt", prompt)
                put("style", style)
                put("title", title)
                put("continueAt", continueAt)
                put("negativeTags", negativeTags)
            }
            
            put("model", model)
            put("callBackUrl", callBackUrl)
        }
        
        return jsonObject.toString()
    }
    
    /**
     * Müzik uzatma durumunu kontrol eder - MusicApiService'deki checkMusicGenerationStatus metoduyla aynı mantıkta çalışır
     * @param taskId Kontrol edilecek görev ID'si
     * @return Durum kontrolü sonucu
     */
    suspend fun checkExtendStatus(taskId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("MusicExtendService", "Müzik uzatma durumu kontrol ediliyor: taskId=$taskId")
                
                // İnternet bağlantısı kontrolü
                if (!isNetworkAvailable()) {
                    Log.e("MusicExtendService", "İnternet bağlantısı yok!")
                    return@withContext Result.failure(IOException("İnternet bağlantısı yok"))
                }
                
                // Durum kontrolü için API isteği
                val request = Request.Builder()
                  .url("$apiBaseUrl/generate/record-info?taskId=$taskId")
                  .get()
                  .addHeader("Accept", "application/json")
                  .addHeader("Authorization", apiKey)
                  .build()
                
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    
                    if (!response.isSuccessful) {
                        Log.e("MusicExtendService", "Durum kontrolü başarısız: ${response.code}")
                        return@withContext Result.failure(IOException("Durum kontrolü başarısız: ${response.code}"))
                    }
                    
                    val responseJson = JSONObject(responseBody ?: "{}")
                    val code = responseJson.optInt("code", 0)
                    val data = responseJson.optJSONObject("data")
                    
                    Log.d("MusicExtendService", "API durum yanıtı: code=$code, data=$data")
                    
                    if (code == 200 && data != null) {
                        // Status alanını kontrol et
                        val status = data.optString("status", "").lowercase()
                        val state = data.optString("state", "").lowercase()
                        
                        // Hem status hem de state alanlarını kontrol ediyoruz (API bazen farklı alanlar kullanabiliyor)
                        val isCompleted = status == "completed" || status == "success" || 
                                          state == "completed" || state == "success"
                        
                        Log.d("MusicExtendService", "Müzik uzatma durumu: status=$status, state=$state, isCompleted=$isCompleted")
                        
                        // Tamamlandıysa true, devam ediyorsa false
                        return@withContext Result.success(isCompleted)
                    } else {
                        // API yanıtı geçersiz veya hata içeriyor
                        val msg = responseJson.optString("msg", "Bilinmeyen hata")
                        Log.e("MusicExtendService", "API durum kontrolü başarısız: $msg (kod: $code)")
                        return@withContext Result.failure(Exception("API durum kontrolü başarısız: $msg (kod: $code)"))
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicExtendService", "Durum kontrolü hatası: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }
    }
} 