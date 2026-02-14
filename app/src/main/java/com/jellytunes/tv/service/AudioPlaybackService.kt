package com.jellytunes.tv.service

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.jellytunes.tv.data.metadata.Lyrics
import com.jellytunes.tv.data.repository.SmbMusicRepository
import com.jellytunes.tv.ui.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AudioPlaybackService(private val context: Context) {
    private val smbRepository = SmbMusicRepository(context)
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack
    
    private val _currentLyrics = MutableStateFlow<Lyrics?>(null)
    val currentLyrics: StateFlow<Lyrics?> = _currentLyrics
    
    private var audioQueue = mutableListOf<Track>()
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
                            println("Player ready")
                        }
                        androidx.media3.common.Player.STATE_BUFFERING -> {
                            println("Buffering...")
                        }
                        androidx.media3.common.Player.STATE_ENDED -> {
                            println("Track ended")
                            playNext()
                        }
                        androidx.media3.common.Player.STATE_IDLE -> {
                            println("Player idle")
                        }
                    }
                }
                
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    println("Player error: ${error.errorCodeName} - ${error.message}")
                    playNext()
                }
            })
        }
        
        // Position updates every 500ms for smooth lyrics sync
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(500)
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
    
    fun connectToSmb(onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            println("Starting SMB connection to NAS...")
            val result = smbRepository.connect()
            
            if (result) {
                println("Loading music from NAS...")
                loadMusicLibrary()
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(true)
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(false)
                }
            }
        }
    }
    
    private suspend fun loadMusicLibrary() {
        val tracks = smbRepository.loadMusicLibrary(100)
        println("Successfully loaded ${tracks.size} tracks")
        
        if (tracks.isNotEmpty()) {
            println("First track: ${tracks[0].title} by ${tracks[0].artist}")
            audioQueue.clear()
            audioQueue.addAll(tracks.shuffled())
            currentIndex = 0
            CoroutineScope(Dispatchers.Main).launch {
                playCurrentTrack()
            }
        } else {
            println("No audio files found in NAS")
        }
    }
    
    private fun playCurrentTrack() {
        val track = audioQueue.getOrNull(currentIndex) ?: return
        _currentTrack.value = track
        _currentLyrics.value = null
        
        CoroutineScope(Dispatchers.IO).launch {
            val audioFile = smbRepository.getAudioFile(track)
            if (audioFile != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val mediaItem = MediaItem.Builder()
                            .setUri(audioFile.toUri())
                            .build()
                        
                        exoPlayer?.setMediaItem(mediaItem)
                        exoPlayer?.prepare()
                        exoPlayer?.play()
                        
                        println("Playing: ${track.title} by ${track.artist}")
                    } catch (e: Exception) {
                        println("Error playing track: ${e.message}")
                        playNext()
                    }
                }
                
                // Load lyrics in background
                loadLyrics(track)
            } else {
                println("Failed to get audio file for: ${track.title}")
                CoroutineScope(Dispatchers.Main).launch {
                    playNext()
                }
            }
        }
    }
    
    private suspend fun loadLyrics(track: Track) {
        val lyrics = smbRepository.getLyrics(track)
        _currentLyrics.value = lyrics
        if (lyrics != null) {
            println("Loaded lyrics: ${lyrics.lines.size} lines")
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
