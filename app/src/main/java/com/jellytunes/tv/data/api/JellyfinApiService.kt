package com.jellytunes.tv.data.api

import com.jellytunes.tv.data.model.AuthenticationResult
import com.jellytunes.tv.data.model.BaseItemDtoQueryResult
import retrofit2.Response
import retrofit2.http.*

interface JellyfinApiService {
    @POST("Users/AuthenticateByName")
    suspend fun authenticate(
        @Header("X-Emby-Authorization") authHeader: String,
        @Body credentials: Map<String, String>
    ): Response<AuthenticationResult>
    
    @GET("Users/{userId}/Items")
    suspend fun getAudioItems(
        @Header("X-MediaBrowser-Token") token: String,
        @Path("userId") userId: String,
        @Query("IncludeItemTypes") includeItemTypes: String = "Audio",
        @Query("Recursive") recursive: Boolean = true,
        @Query("SortBy") sortBy: String = "Random",
        @Query("Limit") limit: Int = 100
    ): Response<BaseItemDtoQueryResult>
    
    @GET("Items/{itemId}/Download")
    suspend fun getAudioStreamUrl(
        @Header("X-MediaBrowser-Token") token: String,
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String
    ): Response<Unit>
}