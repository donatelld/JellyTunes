package com.jellytunes.tv.data.api

import com.jellytunes.tv.data.model.NavidromeLoginResponse
import com.jellytunes.tv.data.model.NavidromeSongsResponse
import com.jellytunes.tv.data.model.NavidromeLyricsResponse
import retrofit2.Response
import retrofit2.http.*

interface NavidromeApiService {
    @GET("rest/ping.view")
    suspend fun ping(
        @Query("u") username: String,
        @Query("s") salt: String,
        @Query("t") token: String,
        @Query("f") format: String = "json",
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "JellyTunes"
    ): Response<Unit>
    
    @POST("rest/login.view")
    suspend fun login(
        @Query("u") username: String,
        @Query("p") password: String,
        @Query("f") format: String = "json",
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "JellyTunes"
    ): Response<NavidromeLoginResponse>
    
    @GET("rest/getRandomSongs.view")
    suspend fun getRandomSongs(
        @Query("u") username: String,
        @Query("s") salt: String,
        @Query("t") token: String,
        @Query("f") format: String = "json",
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "JellyTunes",
        @Query("size") size: Int = 50
    ): Response<NavidromeSongsResponse>
    
    @GET("rest/stream.view")
    suspend fun getAudioStream(
        @Query("u") username: String,
        @Query("s") salt: String,
        @Query("t") token: String,
        @Query("f") format: String = "json",
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "JellyTunes",
        @Query("id") songId: String
    ): Response<Unit>
    
    @GET("rest/getCoverArt.view")
    suspend fun getCoverArt(
        @Query("u") username: String,
        @Query("s") salt: String,
        @Query("t") token: String,
        @Query("f") format: String = "json",
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "JellyTunes",
        @Query("id") coverArtId: String,
        @Query("size") size: Int = 300
    ): Response<Unit>

    @GET("rest/getLyricsBySongId.view")
    suspend fun getLyricsBySongId(
        @Query("u") username: String,
        @Query("s") salt: String,
        @Query("t") token: String,
        @Query("f") format: String = "json",
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "JellyTunes",
        @Query("id") songId: String
    ): Response<NavidromeLyricsResponse>
}