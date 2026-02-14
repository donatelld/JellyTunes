package com.jellytunes.tv.data.model

import com.google.gson.annotations.SerializedName

data class NavidromeLyricsResponse(
    @SerializedName("subsonic-response")
    val subsonicResponse: LyricsSubsonicResponse
)

data class LyricsSubsonicResponse(
    val status: String,
    val version: String,
    val type: String,
    @SerializedName("serverVersion")
    val serverVersion: String,
    @SerializedName("openSubsonic")
    val openSubsonic: Boolean,
    @SerializedName("lyricsList")
    val lyricsList: LyricsList?
)

data class LyricsList(
    @SerializedName("structuredLyrics")
    val structuredLyrics: List<StructuredLyrics>?
)

data class StructuredLyrics(
    @SerializedName("displayArtist")
    val displayArtist: String,
    @SerializedName("displayTitle")
    val displayTitle: String,
    val lang: String,
    val offset: Int,
    val synced: Boolean,
    val line: List<LyricsLine>
)

data class LyricsLine(
    val start: Long,
    val value: String
)