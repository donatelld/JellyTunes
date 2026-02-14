package com.jellytunes.tv.data.metadata

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

data class AudioMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val genre: String?,
    val year: String?,
    val trackNumber: String?,
    val durationMs: Long,
    val albumArtData: ByteArray?,
    val embeddedLyrics: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioMetadata
        return title == other.title && artist == other.artist && album == other.album
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        return result
    }
}

object AudioMetadataExtractor {
    
    init {
        // Suppress JAudioTagger verbose logging
        Logger.getLogger("org.jaudiotagger").level = Level.OFF
    }
    
    fun extractFromFile(file: File): AudioMetadata? {
        return try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            val header = audioFile.audioHeader
            
            val albumArt = tag?.firstArtwork?.binaryData
            val lyrics = tag?.getFirst(FieldKey.LYRICS)
            
            AudioMetadata(
                title = tag?.getFirst(FieldKey.TITLE)?.takeIf { it.isNotBlank() },
                artist = tag?.getFirst(FieldKey.ARTIST)?.takeIf { it.isNotBlank() },
                album = tag?.getFirst(FieldKey.ALBUM)?.takeIf { it.isNotBlank() },
                albumArtist = tag?.getFirst(FieldKey.ALBUM_ARTIST)?.takeIf { it.isNotBlank() },
                genre = tag?.getFirst(FieldKey.GENRE)?.takeIf { it.isNotBlank() },
                year = tag?.getFirst(FieldKey.YEAR)?.takeIf { it.isNotBlank() },
                trackNumber = tag?.getFirst(FieldKey.TRACK)?.takeIf { it.isNotBlank() },
                durationMs = (header.trackLength * 1000).toLong(),
                albumArtData = albumArt,
                embeddedLyrics = lyrics?.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
