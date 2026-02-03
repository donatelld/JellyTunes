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
import com.jellytunes.tv.data.config.AppConfig

class ConfigTestActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var configInfo by remember { mutableStateOf("Loading configuration...") }
            
            LaunchedEffect(Unit) {
                configInfo = loadConfigInfo()
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
                        text = "App Configuration Test",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = configInfo,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    
    private fun loadConfigInfo(): String {
        return buildString {
            appendLine("=== Jellyfin Configuration ===")
            appendLine("Server URL: ${AppConfig.JELLYFIN_SERVER_URL}")
            appendLine("Username: ${AppConfig.JELLYFIN_USERNAME}")
            appendLine("Password: ${"*".repeat(AppConfig.JELLYFIN_PASSWORD.length)}")
            appendLine()
            appendLine("=== Playback Settings ===")
            appendLine("Auto Play Next: ${AppConfig.AUTO_PLAY_NEXT}")
            appendLine("Shuffle Mode: ${AppConfig.SHUFFLE_MODE}")
            appendLine()
            appendLine("=== Network Settings ===")
            appendLine("Connect Timeout: ${AppConfig.CONNECT_TIMEOUT_SECONDS}s")
            appendLine("Read Timeout: ${AppConfig.READ_TIMEOUT_SECONDS}s")
            
            // Log to console as well
            Log.d("ConfigTest", "Server URL: ${AppConfig.JELLYFIN_SERVER_URL}")
            Log.d("ConfigTest", "Username: ${AppConfig.JELLYFIN_USERNAME}")
            Log.d("ConfigTest", "Auto Play Next: ${AppConfig.AUTO_PLAY_NEXT}")
        }
    }
}