package com.jellytunes.tv.test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthTestActivity : ComponentActivity() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var testStatus by remember { mutableStateOf("Testing authentication...") }
            
            LaunchedEffect(Unit) {
                testStatus = performAuthTest()
            }
            
            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Jellyfin Authentication Test",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = testStatus,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    
    private suspend fun performAuthTest(): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthTest", "=== Starting Authentication Test ===")
                Log.d("AuthTest", "Target URL: http://192.168.0.6:8096/Users/AuthenticateByName")
                
                // Test different authentication approaches
                val results = mutableListOf<String>()
                
                // Approach 1: Standard authentication
                results.add("Approach 1 - Standard:")
                val authResult1 = testAuthentication("alvin", "25257758Xj.,", "Pw")
                results.add(if (authResult1.first) "✓ Success" else "✗ Failed: ${authResult1.second}")
                
                // Approach 2: Password field
                results.add("\nApproach 2 - Password field:")
                val authResult2 = testAuthentication("alvin", "25257758Xj.,", "Password")
                results.add(if (authResult2.first) "✓ Success" else "✗ Failed: ${authResult2.second}")
                
                // Approach 3: Try without special characters
                results.add("\nApproach 3 - Simplified password:")
                val authResult3 = testAuthentication("alvin", "25257758Xj.,", "Password")
                results.add(if (authResult3.first) "✓ Success" else "✗ Failed: ${authResult3.second}")
                
                Log.d("AuthTest", "=== Test Complete ===")
                
                results.joinToString("\n")
            } catch (e: Exception) {
                Log.e("AuthTest", "Test error: ${e.message}", e)
                "Error: ${e.message}"
            }
        }
    }
    
    private fun testAuthentication(username: String, password: String, passwordField: String): Pair<Boolean, String> {
        return try {
            val authHeader = "MediaBrowser Client=\"AuthTest\", Device=\"Android\", DeviceId=\"test123\", Version=\"1.0\""
            
            val json = """
                {
                    "Username": "$username",
                    "$passwordField": "$password"
                }
            """.trimIndent()
            
            Log.d("AuthTest", "Request JSON: $json")
            Log.d("AuthTest", "Auth Header: $authHeader")
            
            val body = json.toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("http://192.168.0.6:8096/Users/AuthenticateByName")
                .post(body)
                .addHeader("X-Emby-Authorization", authHeader)
                .build()
            
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("AuthTest", "Response Code: ${response.code}")
                Log.d("AuthTest", "Response Headers: ${response.headers}")
                Log.d("AuthTest", "Response Body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d("AuthTest", "✓ Authentication SUCCESS!")
                    Pair(true, "Success - Token: ${gson.fromJson(responseBody, Map::class.java)["AccessToken"]?.toString()?.take(10)}...")
                } else {
                    Log.d("AuthTest", "✗ Authentication FAILED!")
                    Pair(false, "HTTP ${response.code}: $responseBody")
                }
            }
        } catch (e: IOException) {
            Log.e("AuthTest", "Network error: ${e.message}", e)
            Pair(false, "Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("AuthTest", "General error: ${e.message}", e)
            Pair(false, "Error: ${e.message}")
        }
    }
}