package com.jellytunes.tv.data.repository

import com.google.gson.Gson
import com.jellytunes.tv.data.model.AuthenticationResult
import com.jellytunes.tv.data.model.BaseItemDto
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.jellytunes.tv.data.config.AppConfig
import java.io.IOException
import java.util.concurrent.TimeUnit

class JellyfinRepository {
    private val baseUrl = "${AppConfig.NAVIDROME_SERVER_URL}/"
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(AppConfig.CONNECT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .readTimeout(AppConfig.READ_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .build()
    private var accessToken: String? = null
    private var userId: String? = null
    
    suspend fun authenticate(username: String, password: String): Result<AuthenticationResult> {
        return try {
            val authHeader = "MediaBrowser Client=\"JellyTunes\", Device=\"Android TV\", DeviceId=\"JellyTunes-TV\", Version=\"1.0.0\""
            
            val json = """
                {
                    "Username": "$username",
                    "Pw": "$password"
                }
            """.trimIndent()
            
            val body = json.toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("${baseUrl}Users/AuthenticateByName")
                .post(body)
                .addHeader("X-Emby-Authorization", authHeader)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val responseBody = response.body!!.string()
                    println("Raw response: $responseBody")
                    val authResult = gson.fromJson(responseBody, AuthenticationResult::class.java)
                    accessToken = authResult.accessToken
                    userId = authResult.user.id
                    Result.success(authResult)
                } else {
                    val errorBody = response.body?.string() ?: "No error body"
                    println("Auth failed - Code: ${response.code}, Body: $errorBody")
                    Result.failure(Exception("Authentication failed: ${response.code} - $errorBody"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    
    suspend fun getRandomAudioItems(limit: Int = 50): Result<List<BaseItemDto>> {
        return try {
            if (accessToken == null || userId == null) {
                return Result.failure(Exception("Not authenticated"))
            }
            
            val request = Request.Builder()
                .url("${baseUrl}Users/${userId}/Items?IncludeItemTypes=Audio&Recursive=true&SortBy=Random&Limit=$limit")
                .get()
                .addHeader("X-MediaBrowser-Token", accessToken!!)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val responseBody = response.body!!.string()
                    val result = gson.fromJson(responseBody, com.jellytunes.tv.data.model.BaseItemDtoQueryResult::class.java)
                    Result.success(result.items)
                } else {
                    Result.failure(Exception("Failed to fetch audio items: ${response.code}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    
    fun getAudioStreamUrl(itemId: String): String? {
        return if (accessToken != null && userId != null) {
            "${baseUrl}Audio/$itemId/stream?static=true&userId=$userId"
        } else null
    }
    
    fun getImageUrl(itemId: String, imageTag: String?): String {
        return if (imageTag != null) {
            "${baseUrl}Items/$itemId/Images/Primary?tag=$imageTag&quality=90"
        } else {
            "${baseUrl}Items/$itemId/Images/Primary?quality=90"
        }
    }
    
    fun isAuthenticated(): Boolean = accessToken != null && userId != null
}