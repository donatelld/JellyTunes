package com.jellytunes.tv.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.jellytunes.tv.data.model.BaseItemDto
import com.jellytunes.tv.data.repository.JellyfinRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.jellytunes.tv.data.config.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AudioPlaybackService(private val context: Context) {
    private val jellyfinRepo = JellyfinRepository()
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    
    private val _currentTrack = MutableStateFlow<BaseItemDto?>(null)
    val currentTrack: StateFlow<BaseItemDto?> = _currentTrack
    
    private var audioQueue = mutableListOf<BaseItemDto>()
    private var currentIndex = 0
    
    init {
        initializePlayer()
    }
    
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
                
                override fun onPositionDiscontinuity(
                    oldPosition: androidx.media3.common.Player.PositionInfo,
                    newPosition: androidx.media3.common.Player.PositionInfo,
                    reason: Int
                ) {
                    _currentPosition.value = currentPosition
                    _duration.value = duration
                }
                
                override fun onEvents(
                    player: androidx.media3.common.Player,
                    events: androidx.media3.common.Player.Events
                ) {
                    _currentPosition.value = player.currentPosition
                    _duration.value = player.duration
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        androidx.media3.common.Player.STATE_READY -> {
                            println("✅ Player ready")
                        }
                        androidx.media3.common.Player.STATE_BUFFERING -> {
                            println("⏳ Buffering...")
                        }
                        androidx.media3.common.Player.STATE_ENDED -> {
                            println("⏹️ Track ended")
                            if (AppConfig.AUTO_PLAY_NEXT) {
                                playNext()
                            }
                        }
                        androidx.media3.common.Player.STATE_IDLE -> {
                            println("⏸️ Player idle")
                        }
                    }
                }
                
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    println("❌ Player error: ${error.errorCodeName} - ${error.message}")
                    // Try next track on error
                    if (AppConfig.AUTO_PLAY_NEXT) {
                        playNext()
                    }
                }
            })
        }
        
        // Add periodic position updates for smooth progress bar
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(1000) // Update every second
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition
                        _duration.value = player.duration
                    }
                }
            }
        }
        
        mediaSession = MediaSession.Builder(context, exoPlayer!!)
            .setId("JellyTunesSession")
            .build()
    }
    
    fun connectToJellyfin(username: String, password: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            println("=== Jellyfin Connection Debug ===")
            println("Attempting to connect to Jellyfin at: http://10.0.2.2:8096/")
            println("Username: $username")
            
            val result = jellyfinRepo.authenticate(username, password)
            
            if (result.isSuccess) {
                val authResult = result.getOrNull()!!
                println("✅ Authentication SUCCESS!")
                println("User ID: ${authResult.user.id}")
                println("Access Token: ${authResult.accessToken.substring(0, 10)}...")
                
                loadRandomAudioItems()
                onResult(true)
            } else {
                val error = result.exceptionOrNull()
                println("❌ Authentication FAILED!")
                println("Error: ${error?.message}")
                println("Error type: ${error?.javaClass?.simpleName}")
                onResult(false)
            }
            println("================================")
        }
    }
    
    private fun loadRandomAudioItems() {
        CoroutineScope(Dispatchers.IO).launch {
            println("Loading random audio items from Jellyfin...")
            val result = jellyfinRepo.getRandomAudioItems(50)
            
            if (result.isSuccess) {
                val items = result.getOrNull() ?: emptyList()
                println("✅ Successfully loaded ${items.size} audio items")
                
                if (items.isNotEmpty()) {
                    println("First item: ${items[0].name} by ${items[0].artistItems?.firstOrNull()?.name}")
                    audioQueue.clear()
                    audioQueue.addAll(items)
                    currentIndex = 0
                    playCurrentTrack()
                } else {
                    println("⚠️ No audio items found in library")
                }
            } else {
                val error = result.exceptionOrNull()
                println("❌ Failed to load audio items!")
                println("Error: ${error?.message}")
            }
        }
    }
    
    private fun playCurrentTrack() {
        val track = audioQueue.getOrNull(currentIndex) ?: return
        _currentTrack.value = track
        
        val streamUrl = jellyfinRepo.getAudioStreamUrl(track.id)
        if (streamUrl != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Create media item with proper headers
                    val mediaItem = MediaItem.Builder()
                        .setUri(streamUrl)
                        .setMimeType("audio/*")
                        .build()
                    
                    exoPlayer?.setMediaItem(mediaItem)
                    exoPlayer?.prepare()
                    exoPlayer?.play()
                    
                    println("▶️ Playing: ${track.name} by ${track.artistItems?.firstOrNull()?.name}")
                } catch (e: Exception) {
                    println("❌ Error playing track: ${e.message}")
                    // Try next track if current fails
                    if (AppConfig.AUTO_PLAY_NEXT) {
                        playNext()
                    }
                }
            }
        } else {
            println("❌ Failed to get stream URL for track: ${track.name}")
            // Skip to next track if stream URL generation fails
            if (AppConfig.AUTO_PLAY_NEXT) {
                playNext()
            }
        }
    }
    
    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }
    
    fun playNext() {
        currentIndex = (currentIndex + 1) % audioQueue.size
        playCurrentTrack()
    }
    
    fun playPrevious() {
        currentIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            audioQueue.size - 1
        }
        playCurrentTrack()
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    fun release() {
        mediaSession?.release()
        exoPlayer?.release()
    }
}