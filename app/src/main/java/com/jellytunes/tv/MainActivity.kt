package com.jellytunes.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jellytunes.tv.ui.player.PlayerScreen
import com.jellytunes.tv.ui.player.PlayerState
import com.jellytunes.tv.ui.player.Track
import com.jellytunes.tv.ui.theme.JellyTunesTheme
import com.jellytunes.tv.ui.theme.JellyTunesThemes
import com.jellytunes.tv.ui.theme.ThemeType
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Theme state
            var currentThemeType by remember { mutableStateOf(ThemeType.AMBER) }
            val currentColors = remember(currentThemeType) {
                JellyTunesThemes.getTheme(currentThemeType)
            }

            JellyTunesTheme(colors = currentColors) {
                // Demo tracks for UI testing
                val demoTracks = remember {
                    listOf(
                        Track(
                            id = "1",
                            title = "Starlight Symphony",
                            artist = "Aurora Dreams",
                            album = "Celestial Journeys",
                            durationMs = 245000,
                            albumArtUrl = null,
                            artistImageUrl = null
                        ),
                        Track(
                            id = "2",
                            title = "Ocean Waves",
                            artist = "Calm Waters",
                            album = "Peaceful Shores",
                            durationMs = 312000,
                            albumArtUrl = null,
                            artistImageUrl = null
                        ),
                        Track(
                            id = "3",
                            title = "Midnight Jazz",
                            artist = "Blue Note Quartet",
                            album = "After Hours",
                            durationMs = 198000,
                            albumArtUrl = null,
                            artistImageUrl = null
                        )
                    ).shuffled() // Random shuffle on start
                }

                var currentTrackIndex by remember { mutableStateOf(0) }
                var isPlaying by remember { mutableStateOf(true) }
                var currentPosition by remember { mutableLongStateOf(0L) }

                val currentTrack = demoTracks.getOrNull(currentTrackIndex)

                // Simulate progress update
                LaunchedEffect(isPlaying, currentTrackIndex) {
                    currentPosition = 0L
                    while (isPlaying && currentTrack != null) {
                        delay(1000)
                        currentPosition += 1000
                        if (currentPosition >= currentTrack.durationMs) {
                            currentTrackIndex = (currentTrackIndex + 1) % demoTracks.size
                            currentPosition = 0L
                        }
                    }
                }

                PlayerScreen(
                    playerState = PlayerState(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPosition,
                        durationMs = currentTrack?.durationMs ?: 0L
                    ),
                    onPlayPauseToggle = {
                        isPlaying = !isPlaying
                    },
                    onNextTrack = {
                        currentTrackIndex = (currentTrackIndex + 1) % demoTracks.size
                        currentPosition = 0L
                    },
                    onPreviousTrack = {
                        currentTrackIndex = if (currentTrackIndex > 0) {
                            currentTrackIndex - 1
                        } else {
                            demoTracks.size - 1
                        }
                        currentPosition = 0L
                    },
                    onThemeChange = {
                        currentThemeType = JellyTunesThemes.getNextTheme(currentThemeType)
                    }
                )
            }
        }
    }
}
