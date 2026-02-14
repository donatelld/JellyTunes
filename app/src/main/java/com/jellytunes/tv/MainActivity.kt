package com.jellytunes.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.jellytunes.tv.service.AudioPlaybackService
import com.jellytunes.tv.ui.player.PlayerScreen
import com.jellytunes.tv.ui.player.PlayerState
import com.jellytunes.tv.ui.theme.JellyTunesTheme
import com.jellytunes.tv.ui.theme.JellyTunesThemes
import com.jellytunes.tv.ui.theme.ThemeType

class MainActivity : ComponentActivity() {
    private lateinit var audioService: AudioPlaybackService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize audio service
        audioService = AudioPlaybackService(this)
        
        setContent {
            // Theme state
            var currentThemeType by remember { mutableStateOf(ThemeType.AMBER) }
            val currentColors = remember(currentThemeType) {
                JellyTunesThemes.getTheme(currentThemeType)
            }
            
            // Playback state from service
            val isPlaying by audioService.isPlaying.collectAsState()
            val currentPosition by audioService.currentPosition.collectAsState()
            val duration by audioService.duration.collectAsState()
            val currentTrack by audioService.currentTrack.collectAsState()
            val currentLyrics by audioService.currentLyrics.collectAsState()
            
            JellyTunesTheme(colors = currentColors) {
                // Connect to NAS SMB on startup
                LaunchedEffect(Unit) {
                    println("Starting SMB connection to NAS...")
                    audioService.connectToSmb { success ->
                        if (success) {
                            println("Successfully connected to NAS!")
                        } else {
                            println("Failed to connect to NAS")
                        }
                    }
                }
                
                PlayerScreen(
                    playerState = PlayerState(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPosition,
                        durationMs = duration,
                        lyrics = currentLyrics,
                        showLyrics = true
                    ),
                    onPlayPauseToggle = {
                        audioService.togglePlayPause()
                    },
                    onNextTrack = {
                        audioService.playNext()
                    },
                    onPreviousTrack = {
                        audioService.playPrevious()
                    },
                    onThemeChange = {
                        currentThemeType = JellyTunesThemes.getNextTheme(currentThemeType)
                    }
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioService.release()
    }
}
