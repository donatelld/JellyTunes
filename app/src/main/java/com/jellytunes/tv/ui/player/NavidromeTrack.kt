package com.jellytunes.tv.ui.player

data class NavidromeTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumArtUrl: String?,
    val artistImageUrl: String?,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val genre: String? = null
)