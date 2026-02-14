package com.jellytunes.tv.data.model

import com.google.gson.annotations.SerializedName

// Navidrome认证响应
data class NavidromeLoginResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("token") val token: String,
    @SerializedName("isAdmin") val isAdmin: Boolean
)

// Navidrome歌曲模型
data class NavidromeSong(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String? = null,
    @SerializedName("album") val album: String? = null,
    @SerializedName("trackNumber") val trackNumber: Int? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("duration") val duration: Int? = null, // 秒
    @SerializedName("bitRate") val bitRate: Int? = null,
    @SerializedName("contentType") val contentType: String? = null,
    @SerializedName("suffix") val suffix: String? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("albumId") val albumId: String? = null,
    @SerializedName("artistId") val artistId: String? = null,
    @SerializedName("coverArt") val coverArt: String? = null,
    @SerializedName("size") val size: Long? = null,
    @SerializedName("discNumber") val discNumber: Int? = null,
    @SerializedName("created") val created: String? = null,
    @SerializedName("albumArtist") val albumArtist: String? = null
)

// Navidrome歌曲列表响应
data class NavidromeSongsResponse(
    @SerializedName("song") val songs: List<NavidromeSong>
)

// Navidrome随机歌曲请求参数
data class NavidromeRandomSongsRequest(
    @SerializedName("size") val size: Int = 50,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("fromYear") val fromYear: Int? = null,
    @SerializedName("toYear") val toYear: Int? = null,
    @SerializedName("musicFolderId") val musicFolderId: String? = null
)