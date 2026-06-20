package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val fileUri: String, // "preset://..." for synthesizer, or file:/// to absolute path
    val durationMs: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val mood: String, // "Peaceful", "Emotional", "Deep", "Focus", "Calm"
    val lyricTimings: String, // "time_seconds:text;time_seconds:text"
    val lastPlayedTimestamp: Long = 0L
) {
    // Parser for easy UI consumption
    fun getParsedLyrics(): List<LyricLine> {
        if (lyricTimings.isEmpty()) return emptyList()
        return try {
            lyricTimings.split(";").mapNotNull {
                val parts = it.split(":", limit = 2)
                if (parts.size == 2) {
                    val seconds = parts[0].toIntOrNull() ?: 0
                    LyricLine(seconds * 1000L, parts[1])
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class LyricLine(
    val timestampMs: Long,
    val text: String
)
