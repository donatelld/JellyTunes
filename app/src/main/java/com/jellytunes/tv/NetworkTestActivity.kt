package com.jellytunes.tv

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class NetworkTestActivity : ComponentActivity() {
    private val client = OkHttpClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var testResult by remember { mutableStateOf("Testing...") }
            
            LaunchedEffect(Unit) {
                testResult = testNetworkConnection()
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
                        text = "Network Test Results",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = testResult,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    
    private suspend fun testNetworkConnection(): String {
        return withContext(Dispatchers.IO) {
            try {
                // Test basic connectivity
                val pingRequest = Request.Builder()
                    .url("http://10.0.2.2:8096/")
                    .build()
                
                client.newCall(pingRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("NetworkTest", "Basic connection successful")
                        
                        // Test authentication
                        val authRequest = Request.Builder()
                            .url("http://10.0.2.2:8096/Users/AuthenticateByName")
                            .post(
                                """{"Username":"alvin","Pw":"25257758Xj.,"}""".toRequestBody(
                                    "application/json".toMediaType()
                                )
                            )
                            .addHeader("X-Emby-Authorization", "MediaBrowser Client=\"Test\", Device=\"Android\", DeviceId=\"test123\", Version=\"1.0\"")
                            .build()
                        
                        client.newCall(authRequest).execute().use { authResponse ->
                            if (authResponse.isSuccessful) {
                                val responseBody = authResponse.body?.string() ?: ""
                                Log.d("NetworkTest", "Authentication successful: $responseBody")
                                "✓ Connection successful!\n✓ Authentication successful!"
                            } else {
                                Log.e("NetworkTest", "Authentication failed: ${authResponse.code}")
                                "✓ Connection successful!\n✗ Authentication failed: ${authResponse.code}"
                            }
                        }
                    } else {
                        Log.e("NetworkTest", "Connection failed: ${response.code}")
                        "✗ Connection failed: ${response.code}"
                    }
                }
            } catch (e: IOException) {
                Log.e("NetworkTest", "Network error: ${e.message}")
                "✗ Network error: ${e.message}"
            }
        }
    }
}