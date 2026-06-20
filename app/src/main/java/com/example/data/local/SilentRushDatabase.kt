package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Playlist
import com.example.data.model.PlaylistTrackCrossRef
import com.example.data.model.Track

@Database(
    entities = [Track::class, Playlist::class, PlaylistTrackCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class SilentRushDatabase : RoomDatabase() {
    abstract val trackDao: TrackDao
    abstract val playlistDao: PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: SilentRushDatabase? = null

        fun getDatabase(context: Context): SilentRushDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SilentRushDatabase::class.java,
                    "silent_rush_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
