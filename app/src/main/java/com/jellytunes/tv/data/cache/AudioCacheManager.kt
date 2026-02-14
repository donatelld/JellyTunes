package com.jellytunes.tv.data.cache

import android.content.Context
import android.util.LruCache
import java.io.File
import java.io.InputStream

class AudioCacheManager(context: Context) {
    
    private val cacheDir = File(context.cacheDir, "audio_cache")
    private val maxCacheSize = 500 * 1024 * 1024L // 500MB
    
    private val albumArtCache = object : LruCache<String, ByteArray>(50 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }
    
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    fun cacheAudioFile(smbPath: String, inputStream: InputStream): File? {
        return try {
            val fileName = smbPath.hashCode().toString() + "_" + 
                smbPath.substringAfterLast("/")
            val cacheFile = File(cacheDir, fileName)
            
            if (cacheFile.exists()) {
                return cacheFile
            }
            
            // Check cache size and cleanup if needed
            cleanupIfNeeded()
            
            cacheFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            
            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getCachedFile(smbPath: String): File? {
        val fileName = smbPath.hashCode().toString() + "_" + 
            smbPath.substringAfterLast("/")
        val cacheFile = File(cacheDir, fileName)
        return if (cacheFile.exists()) cacheFile else null
    }
    
    fun cacheAlbumArt(key: String, data: ByteArray) {
        albumArtCache.put(key, data)
    }
    
    fun getAlbumArt(key: String): ByteArray? {
        return albumArtCache.get(key)
    }
    
    private fun cleanupIfNeeded() {
        val files = cacheDir.listFiles() ?: return
        var totalSize = files.sumOf { it.length() }
        
        if (totalSize > maxCacheSize) {
            // Sort by last modified, oldest first
            val sortedFiles = files.sortedBy { it.lastModified() }
            
            for (file in sortedFiles) {
                if (totalSize <= maxCacheSize * 0.8) break
                totalSize -= file.length()
                file.delete()
            }
        }
    }
    
    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
        albumArtCache.evictAll()
    }
}
