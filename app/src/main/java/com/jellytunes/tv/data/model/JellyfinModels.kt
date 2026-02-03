package com.jellytunes.tv.data.model

import com.google.gson.annotations.SerializedName

data class AuthenticationResult(
    @SerializedName("User") val user: User,
    @SerializedName("AccessToken") val accessToken: String,
    @SerializedName("ServerId") val serverId: String
)

data class User(
    @SerializedName("Id") val id: String,
    @SerializedName("Name") val name: String
)

data class BaseItemDto(
    @SerializedName("Id") val id: String,
    @SerializedName("Name") val name: String,
    @SerializedName("Overview") val overview: String? = null,
    @SerializedName("Type") val type: String,
    @SerializedName("MediaType") val mediaType: String? = null,
    @SerializedName("RunTimeTicks") val runTimeTicks: Long? = null,
    @SerializedName("ProductionYear") val productionYear: Int? = null,
    @SerializedName("IndexNumber") val indexNumber: Int? = null,
    @SerializedName("ParentIndexNumber") val parentIndexNumber: Int? = null,
    @SerializedName("AlbumId") val albumId: String? = null,
    @SerializedName("Album") val album: String? = null,
    @SerializedName("ArtistItems") val artistItems: List<NameIdPair>? = null,
    @SerializedName("AlbumPrimaryImageTag") val albumPrimaryImageTag: String? = null,
    @SerializedName("BackdropImageTags") val backdropImageTags: List<String>? = null,
    @SerializedName("ImageTags") val imageTags: Map<String, String>? = null
)

data class NameIdPair(
    @SerializedName("Name") val name: String,
    @SerializedName("Id") val id: String
)

data class BaseItemDtoQueryResult(
    @SerializedName("Items") val items: List<BaseItemDto>,
    @SerializedName("TotalRecordCount") val totalRecordCount: Int,
    @SerializedName("StartIndex") val startIndex: Int
)