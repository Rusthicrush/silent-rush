package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val imagePresetId: Int = 0, // Reference to pre-designed gradient background or icon
    val createdAt: Long = System.currentTimeMillis()
)
