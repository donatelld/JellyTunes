package com.jellytunes.tv.ui.player

import com.jellytunes.tv.data.metadata.Lyrics

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long = 0L,
    val albumArtUrl: String? = null,
    val artistImageUrl: String? = null,
    val filePath: String? = null,
    val albumArtData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Track
        return id == other.id
    }
    
    override fun hashCode(): Int = id.hashCode()
}

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lyrics: Lyrics? = null,
    val showLyrics: Boolean = true
)
