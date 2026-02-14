package com.jellytunes.tv.ui.player

import com.jellytunes.tv.data.model.LyricsLine

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumArtUrl: String? = null,
    val artistImageUrl: String? = null
)

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentLyrics: List<LyricsLine> = emptyList(),
    val currentLyricIndex: Int? = null
)
