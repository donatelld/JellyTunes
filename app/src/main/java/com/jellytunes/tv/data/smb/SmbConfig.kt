package com.jellytunes.tv.data.smb

data class SmbConfig(
    val host: String = "192.168.0.6",
    val share: String = "Music",
    val username: String = "music",
    val password: String = "ZmUX7yUg"
) {
    val smbUrl: String
        get() = "smb://$host/$share/"
    
    companion object {
        val SUPPORTED_AUDIO_FORMATS = listOf(
            ".mp3", ".flac", ".m4a", ".ogg", ".wav", ".aac", ".wma"
        )
    }
}
