package com.jellytunes.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.jellytunes.tv.service.AudioPlaybackService
import com.jellytunes.tv.ui.player.PlayerScreen
import com.jellytunes.tv.ui.player.PlayerState
import com.jellytunes.tv.ui.player.Track
import com.jellytunes.tv.ui.theme.JellyTunesTheme
import com.jellytunes.tv.ui.theme.JellyTunesThemes
import com.jellytunes.tv.data.config.AppConfig
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
            val currentJellyfinTrack by audioService.currentTrack.collectAsState()
            
            // Convert Jellyfin track to UI Track
            val currentTrack = currentJellyfinTrack?.let { track ->
                Track(
                    id = track.id,
                    title = track.name,
                    artist = track.artistItems?.firstOrNull()?.name ?: "Unknown Artist",
                    album = track.album ?: "Unknown Album",
                    durationMs = (track.runTimeTicks ?: 0L) / 10000, // Convert ticks to ms
                    albumArtUrl = getImageUrl(track.albumId ?: track.id, track.albumPrimaryImageTag),
                    artistImageUrl = getImageUrl(track.artistItems?.firstOrNull()?.id ?: "", null)
                )
            }
            
            JellyTunesTheme(colors = currentColors) {
                // Connect to Jellyfin on startup
                LaunchedEffect(Unit) {
                    println("Starting Jellyfin connection...")
                    audioService.connectToJellyfin(AppConfig.JELLYFIN_USERNAME, AppConfig.JELLYFIN_PASSWORD) { success ->
                        if (success) {
                            println("Successfully connected to Jellyfin!")
                        } else {
                            println("Failed to connect to Jellyfin")
                        }
                    }
                }
                
                PlayerScreen(
                    playerState = PlayerState(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPosition,
                        durationMs = duration
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
    
    private fun getImageUrl(itemId: String, imageTag: String?): String? {
        return if (imageTag != null) {
            "${AppConfig.JELLYFIN_SERVER_URL}/Items/$itemId/Images/Primary?tag=$imageTag&quality=90"
        } else {
            "${AppConfig.JELLYFIN_SERVER_URL}/Items/$itemId/Images/Primary?quality=90"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioService.release()
    }
}