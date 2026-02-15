package com.jellytunes.tv.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.jellytunes.tv.data.model.NavidromeSong
import com.jellytunes.tv.data.repository.NavidromeRepository
import com.jellytunes.tv.ui.player.NavidromeTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.jellytunes.tv.data.config.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.jellytunes.tv.data.model.LyricsLine
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AudioPlaybackService(private val context: Context) {
    private val navidromeRepo = NavidromeRepository()
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    
    private val _currentTrack = MutableStateFlow<NavidromeTrack?>(null)
    val currentTrack: StateFlow<NavidromeTrack?> = _currentTrack
    
    private val _currentLyrics = MutableStateFlow<List<LyricsLine>>(emptyList())
    val currentLyrics: StateFlow<List<LyricsLine>> = _currentLyrics
    
    private val _currentLyricIndex = MutableStateFlow<Int?>(null)
    val currentLyricIndex: StateFlow<Int?> = _currentLyricIndex
    
    private var audioQueue = mutableListOf<NavidromeSong>()
    private var currentIndex = 0
    private var lyricUpdateJob: Job? = null
    
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
                    error.printStackTrace()
                    // Try next track on error
                    if (AppConfig.AUTO_PLAY_NEXT) {
                        playNextSafe()
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
    
    fun connectToNavidrome(username: String, password: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            println("=== Navidrome Connection Debug ===")
            println("Attempting to connect to Navidrome at: ${AppConfig.NAVIDROME_SERVER_URL}")
            println("Username: $username")
            
            val result = navidromeRepo.authenticate(username, password)
            
            if (result.isSuccess) {
                val authResult = result.getOrNull()!!
                println("✅ Authentication SUCCESS!")
                println("User ID: ${authResult.id}")
                println("Username: ${authResult.username}")
                
                loadRandomSongs(username, password)
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
    
    private fun loadRandomSongs(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            println("Loading random songs from Navidrome...")
            val result = navidromeRepo.getRandomSongs(username, password, 50)
            
            if (result.isSuccess) {
                val songs = result.getOrNull() ?: emptyList()
                println("✅ Successfully loaded ${songs.size} songs")
                
                if (songs.isNotEmpty()) {
                    println("First song: ${songs[0].title} by ${songs[0].artist}")
                    audioQueue.clear()
                    audioQueue.addAll(songs)
                    currentIndex = 0
                    playCurrentSong(username, password)
                } else {
                    println("⚠️ No songs found in library")
                }
            } else {
                val error = result.exceptionOrNull()
                println("❌ Failed to load songs!")
                println("Error: ${error?.message}")
            }
        }
    }
    
    private fun playCurrentSong(username: String, password: String) {
        // 添加边界检查
        if (audioQueue.isEmpty()) {
            println("❌ Audio queue is empty")
            return
        }
        
        if (currentIndex < 0 || currentIndex >= audioQueue.size) {
            println("❌ Invalid current index: $currentIndex, queue size: ${audioQueue.size}")
            currentIndex = 0 // 重置到开头
        }
        
        val song = audioQueue.getOrNull(currentIndex) ?: run {
            println("❌ Failed to get song at index $currentIndex")
            return
        }
        
        try {
            val track = NavidromeTrack(
                id = song.id,
                title = song.title,
                artist = song.artist ?: "Unknown Artist",
                album = song.album ?: "Unknown Album",
                durationMs = (song.duration ?: 0) * 1000L, // Convert seconds to milliseconds
                albumArtUrl = navidromeRepo.getCoverArtUrl(song.coverArt ?: song.albumId ?: song.id, username, password),
                artistImageUrl = null, // Navidrome doesn't provide separate artist images
                year = song.year,
                trackNumber = song.trackNumber,
                genre = song.genre
            )
            _currentTrack.value = track
            
            // Load lyrics for current song
            loadLyricsForSong(song.id, username, password)
            
            val streamUrl = navidromeRepo.getAudioStreamUrl(song.id, username, password)
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
                        
                        println("▶️ Playing: ${song.title} by ${song.artist}")
                    } catch (e: Exception) {
                        println("❌ Error playing song: ${e.message}")
                        e.printStackTrace()
                        // Try next track if current fails
                        if (AppConfig.AUTO_PLAY_NEXT) {
                            playNextSafe()
                        }
                    }
                }
            } else {
                println("❌ Failed to get stream URL for song: ${song.title}")
                // Skip to next track if stream URL generation fails
                if (AppConfig.AUTO_PLAY_NEXT) {
                    playNextSafe()
                }
            }
        } catch (e: Exception) {
            println("❌ Error creating track: ${e.message}")
            e.printStackTrace()
            if (AppConfig.AUTO_PLAY_NEXT) {
                playNextSafe()
            }
        }
    }
    
    fun togglePlayPause() {
        println("⏯️ togglePlayPause() 被调用")
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                println("⏸️ 当前正在播放，执行暂停")
                player.pause()
            } else {
                println("▶️ 当前已暂停，执行播放")
                player.play()
            }
        } ?: run {
            println("❌ ExoPlayer 未初始化")
        }
    }
    
    fun playNext() {
        println("⏭️ playNext() 被调用，当前索引: $currentIndex")
        playNextSafe()
    }
    
    private fun playNextSafe() {
        try {
            if (audioQueue.isEmpty()) {
                println("❌ Audio queue is empty, cannot play next")
                return
            }
            
            currentIndex = (currentIndex + 1) % audioQueue.size
            println("⏭️ 新索引: $currentIndex")
            // 获取用户名和密码用于构建URL
            val username = AppConfig.NAVIDROME_USERNAME
            val password = AppConfig.NAVIDROME_PASSWORD
            playCurrentSong(username, password)
        } catch (e: Exception) {
            println("❌ Error in playNextSafe: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun playPrevious() {
        println("⏮️ playPrevious() 被调用，当前索引: $currentIndex")
        playPreviousSafe()
    }
    
    private fun playPreviousSafe() {
        try {
            if (audioQueue.isEmpty()) {
                println("❌ Audio queue is empty, cannot play previous")
                return
            }
            
            currentIndex = if (currentIndex > 0) {
                currentIndex - 1
            } else {
                audioQueue.size - 1
            }
            println("⏮️ 新索引: $currentIndex")
            // 获取用户名和密码用于构建URL
            val username = AppConfig.NAVIDROME_USERNAME
            val password = AppConfig.NAVIDROME_PASSWORD
            playCurrentSong(username, password)
        } catch (e: Exception) {
            println("❌ Error in playPreviousSafe: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun loadLyricsForSong(songId: String, username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = navidromeRepo.getLyricsBySongId(songId, username, password)
            
            if (result.isSuccess) {
                val lyrics = result.getOrNull() ?: emptyList()
                _currentLyrics.value = lyrics
                println("✅ Loaded ${lyrics.size} lyric lines for song")
                
                // Start lyric synchronization
                startLyricSynchronization()
            } else {
                val error = result.exceptionOrNull()
                println("❌ Failed to load lyrics: ${error?.message}")
                _currentLyrics.value = emptyList()
                _currentLyricIndex.value = null
            }
        }
    }
    
    private fun startLyricSynchronization() {
        try {
            // Cancel previous job if exists
            lyricUpdateJob?.cancel()
            
            lyricUpdateJob = CoroutineScope(Dispatchers.Main).launch {
                try {
                    while (true) {
                        delay(100) // Check every 100ms for smooth lyric highlighting
                        
                        exoPlayer?.let { player ->
                            if (player.isPlaying) {
                                val currentPosition = player.currentPosition
                                val lyrics = _currentLyrics.value
                                
                                // Find current lyric line with bounds checking
                                var currentIndex: Int? = null
                                for (i in lyrics.indices) {
                                    if (currentPosition >= lyrics[i].start) {
                                        currentIndex = i
                                    } else {
                                        break
                                    }
                                }
                                
                                // Update current lyric index if changed
                                if (_currentLyricIndex.value != currentIndex) {
                                    _currentLyricIndex.value = currentIndex
                                }
                            }
                        }
                        
                        // Stop if no lyrics or player not initialized
                        if (_currentLyrics.value.isEmpty() || exoPlayer == null) {
                            break
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Error in lyric synchronization: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println("❌ Error starting lyric synchronization: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
        // Reset lyric synchronization when seeking
        startLyricSynchronization()
    }
    
    fun release() {
        lyricUpdateJob?.cancel()
        mediaSession?.release()
        exoPlayer?.release()
    }
}