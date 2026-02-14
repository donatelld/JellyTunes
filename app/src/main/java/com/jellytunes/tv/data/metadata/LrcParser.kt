package com.jellytunes.tv.data.metadata

data class LyricsLine(
    val timeMs: Long,
    val text: String
)

data class Lyrics(
    val lines: List<LyricsLine>,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null
)

object LrcParser {
    
    private val TIME_TAG_REGEX = Regex("""\[(\d{2}):(\d{2})(?:[.:](\d{2,3}))?\]""")
    private val META_TAG_REGEX = Regex("""\[(ti|ar|al|au|by|offset):([^\]]*)\]""")
    
    fun parse(lrcContent: String): Lyrics {
        val lines = mutableListOf<LyricsLine>()
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var offset = 0L
        
        for (line in lrcContent.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            
            // Check for metadata tags
            val metaMatch = META_TAG_REGEX.find(trimmed)
            if (metaMatch != null) {
                val (tag, value) = metaMatch.destructured
                when (tag.lowercase()) {
                    "ti" -> title = value.trim()
                    "ar" -> artist = value.trim()
                    "al" -> album = value.trim()
                    "offset" -> offset = value.trim().toLongOrNull() ?: 0L
                }
                continue
            }
            
            // Parse time tags and lyrics text
            val timeMatches = TIME_TAG_REGEX.findAll(trimmed).toList()
            if (timeMatches.isNotEmpty()) {
                val text = trimmed.replace(TIME_TAG_REGEX, "").trim()
                if (text.isNotEmpty()) {
                    for (match in timeMatches) {
                        val (min, sec, ms) = match.destructured
                        val millis = min.toLong() * 60000 +
                                sec.toLong() * 1000 +
                                (ms.padEnd(3, '0').take(3).toLongOrNull() ?: 0)
                        lines.add(LyricsLine(millis + offset, text))
                    }
                }
            }
        }
        
        // Sort by time
        lines.sortBy { it.timeMs }
        
        return Lyrics(
            lines = lines,
            title = title,
            artist = artist,
            album = album
        )
    }
    
    fun parseFromPlainText(text: String): Lyrics {
        val lines = text.lines()
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                LyricsLine(timeMs = index * 5000L, text = line.trim())
            }
        return Lyrics(lines = lines)
    }
}
