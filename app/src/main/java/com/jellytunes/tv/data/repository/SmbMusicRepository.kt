package com.jellytunes.tv.data.repository

import android.content.Context
import com.jellytunes.tv.data.cache.AudioCacheManager
import com.jellytunes.tv.data.metadata.AudioMetadata
import com.jellytunes.tv.data.metadata.AudioMetadataExtractor
import com.jellytunes.tv.data.metadata.LrcParser
import com.jellytunes.tv.data.metadata.Lyrics
import com.jellytunes.tv.data.smb.SmbClient
import com.jellytunes.tv.data.smb.SmbConfig
import com.jellytunes.tv.data.smb.SmbMusicScanner
import com.jellytunes.tv.ui.player.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SmbMusicRepository(context: Context) {
    
    private val config = SmbConfig()
    private val smbClient = SmbClient(config)
    private val scanner = SmbMusicScanner(smbClient)
    private val cacheManager = AudioCacheManager(context)
    
    private val metadataCache = mutableMapOf<String, AudioMetadata>()
    private val lyricsCache = mutableMapOf<String, Lyrics?>()
    
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        println("=== SMB Connection ===")
        println("Connecting to NAS SMB share...")
        val result = smbClient.connect()
        println(if (result) "SMB Connection SUCCESS!" else "SMB Connection FAILED!")
        result
    }
    
    suspend fun loadMusicLibrary(limit: Int = 100): List<Track> = withContext(Dispatchers.IO) {
        println("Scanning for audio files (limit: $limit)...")
        val audioFiles = scanner.scanForAudioFiles(limit)
        println("Found ${audioFiles.size} audio files")
        
        audioFiles.mapIndexedNotNull { index, audioFile ->
            try {
                println("Processing [${index + 1}/${audioFiles.size}]: ${audioFile.name}")
                extractTrackInfo(audioFile.path, audioFile.name, audioFile.lrcPath)
            } catch (e: Exception) {
                println("Failed to process ${audioFile.name}: ${e.message}")
                null
            }
        }
    }
    
    private suspend fun extractTrackInfo(
        smbPath: String, 
        fileName: String,
        lrcPath: String?
    ): Track? = withContext(Dispatchers.IO) {
        // First cache the file locally
        val cachedFile = cacheManager.getCachedFile(smbPath) ?: run {
            val inputStream = smbClient.openInputStream(smbPath) ?: return@withContext null
            cacheManager.cacheAudioFile(smbPath, inputStream)
        } ?: return@withContext null
        
        // Extract metadata from cached file
        val metadata = AudioMetadataExtractor.extractFromFile(cachedFile)
        if (metadata != null) {
            metadataCache[smbPath] = metadata
        }
        
        // Cache lyrics path for later loading
        if (lrcPath != null) {
            // Mark that lyrics exist but don't load yet
        }
        
        val title = metadata?.title ?: fileName.substringBeforeLast(".")
        val artist = metadata?.artist ?: "Unknown Artist"
        val album = metadata?.album ?: "Unknown Album"
        
        Track(
            id = smbPath.hashCode().toString(),
            title = title,
            artist = artist,
            album = album,
            albumArtUrl = null,
            filePath = smbPath,
            albumArtData = metadata?.albumArtData
        )
    }
    
    suspend fun getAudioFile(track: Track): File? = withContext(Dispatchers.IO) {
        val smbPath = track.filePath ?: return@withContext null
        
        cacheManager.getCachedFile(smbPath) ?: run {
            val inputStream = smbClient.openInputStream(smbPath) ?: return@withContext null
            cacheManager.cacheAudioFile(smbPath, inputStream)
        }
    }
    
    suspend fun getLyrics(track: Track): Lyrics? = withContext(Dispatchers.IO) {
        val smbPath = track.filePath ?: return@withContext null
        
        // Check cache first
        if (lyricsCache.containsKey(smbPath)) {
            return@withContext lyricsCache[smbPath]
        }
        
        // Try embedded lyrics first
        val metadata = metadataCache[smbPath]
        if (metadata?.embeddedLyrics != null) {
            val lyrics = LrcParser.parse(metadata.embeddedLyrics)
            lyricsCache[smbPath] = lyrics
            return@withContext lyrics
        }
        
        // Try external LRC file
        val lrcPath = smbPath.substringBeforeLast(".") + ".lrc"
        if (smbClient.fileExists(lrcPath)) {
            val inputStream = smbClient.openInputStream(lrcPath)
            if (inputStream != null) {
                val lrcContent = inputStream.bufferedReader().use { it.readText() }
                val lyrics = LrcParser.parse(lrcContent)
                lyricsCache[smbPath] = lyrics
                return@withContext lyrics
            }
        }
        
        lyricsCache[smbPath] = null
        null
    }
}
