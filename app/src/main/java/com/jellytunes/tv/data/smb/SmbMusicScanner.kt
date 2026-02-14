package com.jellytunes.tv.data.smb

import jcifs.smb.SmbFile

class SmbMusicScanner(private val smbClient: SmbClient) {
    
    data class SmbAudioFile(
        val path: String,
        val name: String,
        val size: Long,
        val lrcPath: String? = null
    )
    
    fun scanForAudioFiles(maxFiles: Int = 500): List<SmbAudioFile> {
        val audioFiles = mutableListOf<SmbAudioFile>()
        scanDirectory(smbClient.getRootUrl(), audioFiles, maxFiles)
        return audioFiles
    }
    
    private fun scanDirectory(
        dirPath: String,
        audioFiles: MutableList<SmbAudioFile>,
        maxFiles: Int
    ) {
        if (audioFiles.size >= maxFiles) return
        
        val files = smbClient.listFiles(dirPath)
        
        for (file in files) {
            if (audioFiles.size >= maxFiles) break
            
            try {
                if (file.isDirectory) {
                    if (!file.name.startsWith(".")) {
                        scanDirectory(file.canonicalPath, audioFiles, maxFiles)
                    }
                } else {
                    val fileName = file.name.lowercase()
                    if (SmbConfig.SUPPORTED_AUDIO_FORMATS.any { fileName.endsWith(it) }) {
                        val lrcPath = findLrcFile(file)
                        audioFiles.add(
                            SmbAudioFile(
                                path = file.canonicalPath,
                                name = file.name,
                                size = file.length(),
                                lrcPath = lrcPath
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Skip problematic files
            }
        }
    }
    
    private fun findLrcFile(audioFile: SmbFile): String? {
        val baseName = audioFile.name.substringBeforeLast(".")
        val parentPath = audioFile.parent
        val lrcPath = "$parentPath$baseName.lrc"
        
        return if (smbClient.fileExists(lrcPath)) lrcPath else null
    }
}
