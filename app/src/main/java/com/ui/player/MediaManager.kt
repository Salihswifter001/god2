package com.ui.player

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.ui.SongItem
import java.util.concurrent.TimeUnit

/**
 * Cihaz üzerindeki medya dosyalarını yönetmek için sınıf
 */
class MediaManager(private val context: Context) {
    
    /**
     * Cihaz üzerindeki tüm ses dosyalarını getiren fonksiyon
     */
    fun getAllAudioFiles(): List<SongItem> {
        val audioList = mutableListOf<SongItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        
        // Yalnızca müzik dosyaları 
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    // Albüm kapağının URI'sini oluştur
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )
                    
                    // Süreyi formatlama
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - 
                            TimeUnit.MINUTES.toSeconds(minutes)
                    val durationText = String.format("%d:%02d", minutes, seconds)
                    
                    val song = SongItem(
                        id = id.toString(),
                        title = title ?: "Bilinmeyen",
                        artist = artist ?: "Bilinmeyen",
                        albumArt = albumArtUri.toString(),
                        duration = durationText,
                        mediaUri = contentUri.toString()
                    )
                    
                    audioList.add(song)
                }
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "Müzik dosyaları alınırken hata: ${e.message}")
        }
        
        return audioList
    }
    
    /**
     * Albüm kapak resmini getir
     */
    fun getAlbumArtBitmap(albumArtUri: Uri): Any? {
        // Albüm kapak resmini getirme işlemi (uygulamanın görsel kütüphanesine göre)
        try {
            return context.contentResolver.openInputStream(albumArtUri)
        } catch (e: Exception) {
            Log.e("MediaManager", "Albüm kapağı alınamadı: ${e.message}")
            return null
        }
    }
    
    /**
     * Raw klasöründeki ses dosyalarını getir
     */
    fun getSongsFromRaw(resourceIds: List<Int>): List<SongItem> {
        return resourceIds.mapIndexed { index, resourceId ->
            SongItem(
                id = "raw_$index",
                title = "Raw Dosya $index",
                artist = "Uygulama",
                albumArt = "",
                duration = "0:30",
                mediaResourceId = resourceId
            )
        }
    }
    
    /**
     * Asset dosyaları için yardımcı fonksiyon
     */
    fun getAssetFileUri(fileName: String): Uri {
        return Uri.parse("file:///android_asset/$fileName")
    }
} 