package com.jellytunes.tv.data.repository

import com.google.gson.Gson
import com.jellytunes.tv.data.model.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.jellytunes.tv.data.config.AppConfig
import java.io.IOException
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class NavidromeRepository {
    private val baseUrl = "${AppConfig.NAVIDROME_SERVER_URL}/"
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(AppConfig.CONNECT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .readTimeout(AppConfig.READ_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .build()
    
    private var authToken: String? = null
    private var salt: String? = null
    
    // 生成Subsonic API需要的认证令牌
    private fun generateToken(password: String, salt: String): String {
        val md = MessageDigest.getInstance("MD5")
        val saltedPassword = password + salt
        val hash = md.digest(saltedPassword.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    suspend fun authenticate(username: String, password: String): Result<NavidromeLoginResponse> {
        return try {
            // 使用Subsonic API标准的密码编码方式
            val encodedPassword = "enc:" + password.toByteArray().joinToString("") { "%02x".format(it) }
            
            val url = "${baseUrl}rest/ping.view?" +
                    "u=${username}&" +
                    "p=${encodedPassword}&" +
                    "f=json&" +
                    "v=1.16.1&" +
                    "c=JellyTunes"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val responseBody = response.body!!.string()
                    println("Navidrome login response: $responseBody")
                    
                    // 解析Subsonic API响应格式
                    val responseObject = gson.fromJson(responseBody, Map::class.java)
                    val subsonicResponse = responseObject["subsonic-response"] as? Map<String, Any>
                    val status = subsonicResponse?.get("status") as? String
                    
                    if (status == "ok") {
                        // Subsonic API的login字段可能不存在，我们直接创建响应
                        val navidromeResponse = NavidromeLoginResponse(
                            id = username, // 使用用户名作为ID
                            username = username,
                            token = "", // Subsonic不需要token用于后续请求
                            isAdmin = false
                        )
                        
                        // 对于Subsonic API，我们不需要保存authToken和salt
                        // 后续请求直接使用用户名和密码
                        Result.success(navidromeResponse)
                    } else {
                        val error = subsonicResponse?.get("error") as? Map<String, Any>
                        val errorMessage = error?.get("message") as? String ?: "Unknown error"
                        Result.failure(Exception("Subsonic API error: $errorMessage"))
                    }
                } else {
                    val errorBody = response.body?.string() ?: "No error body"
                    println("Navidrome auth failed - Code: ${response.code}, Body: $errorBody")
                    Result.failure(Exception("Authentication failed: ${response.code} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRandomSongs(username: String, password: String, limit: Int = 50): Result<List<NavidromeSong>> {
        return try {
            // Subsonic API使用用户名和密码进行认证
            val encodedPassword = "enc:" + password.toByteArray().joinToString("") { "%02x".format(it) }
            
            val url = "${baseUrl}rest/getRandomSongs.view?" +
                    "u=${username}&" +
                    "p=${encodedPassword}&" +
                    "f=json&" +
                    "v=1.16.1&" +
                    "c=JellyTunes&" +
                    "size=$limit"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val responseBody = response.body!!.string()
                    println("Navidrome random songs response: $responseBody")
                    
                    // 解析Subsonic API响应格式
                    val responseObject = gson.fromJson(responseBody, Map::class.java)
                    val subsonicResponse = responseObject["subsonic-response"] as? Map<String, Any>
                    val status = subsonicResponse?.get("status") as? String
                    
                    if (status == "ok") {
                        val randomSongsResponse = subsonicResponse["randomSongs"] as? Map<String, Any>
                        val songList = randomSongsResponse?.get("song") as? List<Map<String, Any>>
                        
                        if (songList != null) {
                            val songs = songList.map { songMap ->
                                NavidromeSong(
                                    id = songMap["id"] as? String ?: "",
                                    title = songMap["title"] as? String ?: "",
                                    artist = songMap["artist"] as? String,
                                    album = songMap["album"] as? String,
                                    trackNumber = (songMap["track"] as? Double)?.toInt(),
                                    year = (songMap["year"] as? Double)?.toInt(),
                                    genre = songMap["genre"] as? String,
                                    duration = (songMap["duration"] as? Double)?.toInt(),
                                    bitRate = (songMap["bitRate"] as? Double)?.toInt(),
                                    contentType = songMap["contentType"] as? String,
                                    suffix = songMap["suffix"] as? String,
                                    path = songMap["path"] as? String,
                                    albumId = songMap["albumId"] as? String,
                                    artistId = songMap["artistId"] as? String,
                                    coverArt = songMap["coverArt"] as? String,
                                    size = (songMap["size"] as? Double)?.toLong(),
                                    discNumber = (songMap["discNumber"] as? Double)?.toInt(),
                                    created = songMap["created"] as? String,
                                    albumArtist = songMap["albumArtist"] as? String
                                )
                            }
                            Result.success(songs)
                        } else {
                            Result.success(emptyList())
                        }
                    } else {
                        val error = subsonicResponse?.get("error") as? Map<String, Any>
                        val errorMessage = error?.get("message") as? String ?: "Unknown error"
                        Result.failure(Exception("Subsonic API error: $errorMessage"))
                    }
                } else {
                    Result.failure(Exception("Failed to fetch random songs: ${response.code}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    
    fun getAudioStreamUrl(songId: String, username: String, password: String): String? {
        val encodedPassword = "enc:" + password.toByteArray().joinToString("") { "%02x".format(it) }
        return "${baseUrl}rest/stream.view?" +
                "u=${username}&" +
                "p=${encodedPassword}&" +
                "f=json&" +
                "v=1.16.1&" +
                "c=JellyTunes&" +
                "id=${songId}"
    }
    
    fun getCoverArtUrl(coverArtId: String, username: String, password: String): String? {
        if (coverArtId.isEmpty()) return null
        
        val encodedPassword = "enc:" + password.toByteArray().joinToString("") { "%02x".format(it) }
        return "${baseUrl}rest/getCoverArt.view?" +
                "u=${username}&" +
                "p=${encodedPassword}&" +
                "f=json&" +
                "v=1.16.1&" +
                "c=JellyTunes&" +
                "id=${coverArtId}&" +
                "size=300"
    }
    
    fun isAuthenticated(): Boolean = authToken != null && salt != null
    
    suspend fun getLyricsBySongId(songId: String, username: String, password: String): Result<List<LyricsLine>> {
        return try {
            val encodedPassword = "enc:" + password.toByteArray().joinToString("") { "%02x".format(it) }
            
            val url = "${baseUrl}rest/getLyricsBySongId.view?" +
                    "u=${username}&" +
                    "p=${encodedPassword}&" +
                    "f=json&" +
                    "v=1.16.1&" +
                    "c=JellyTunes&" +
                    "id=${songId}"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val responseBody = response.body!!.string()
                    println("Navidrome lyrics response: $responseBody")
                    
                    // 解析Subsonic API响应格式
                    val responseObject = gson.fromJson(responseBody, Map::class.java)
                    val subsonicResponse = responseObject["subsonic-response"] as? Map<String, Any>
                    val status = subsonicResponse?.get("status") as? String
                    
                    if (status == "ok") {
                        val lyricsList = subsonicResponse["lyricsList"] as? Map<String, Any>
                        val structuredLyrics = lyricsList?.get("structuredLyrics") as? List<Map<String, Any>>
                        
                        if (!structuredLyrics.isNullOrEmpty()) {
                            // 获取第一个歌词对象（通常是最匹配的）
                            val firstLyrics = structuredLyrics[0]
                            val lines = firstLyrics["line"] as? List<Map<String, Any>>
                            
                            if (!lines.isNullOrEmpty()) {
                                val lyricsLines = lines.map { lineMap ->
                                    // 在Subsonic API中，歌词行的内容通常作为值存储
                                    val content = lineMap.entries.firstOrNull { it.key != "start" }?.value as? String ?: ""
                                    LyricsLine(
                                        start = (lineMap["start"] as? Number)?.toLong() ?: 0L,
                                        value = content
                                    )
                                }
                                Result.success(lyricsLines)
                            } else {
                                Result.success(emptyList())
                            }
                        } else {
                            Result.success(emptyList())
                        }
                    } else {
                        val error = subsonicResponse?.get("error") as? Map<String, Any>
                        val errorMessage = error?.get("message") as? String ?: "Unknown error"
                        Result.failure(Exception("Subsonic API error: $errorMessage"))
                    }
                } else {
                    Result.failure(Exception("Failed to fetch lyrics: ${response.code}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}