package com.jellytunes.tv.data.config

object AppConfig {
    const val JELLYFIN_SERVER_URL = "http://192.168.0.6:8096"
    const val JELLYFIN_USERNAME = "alvin"
    const val JELLYFIN_PASSWORD = "25257758Xj.," // 注意：实际应用中应该加密存储
    
    // 播放设置
    const val AUTO_PLAY_NEXT = true
    const val SHUFFLE_MODE = true
    
    // 网络设置
    const val CONNECT_TIMEOUT_SECONDS = 30
    const val READ_TIMEOUT_SECONDS = 30
}