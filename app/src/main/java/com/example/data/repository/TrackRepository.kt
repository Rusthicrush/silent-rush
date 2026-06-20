package com.example.data.repository

import com.example.data.local.PlaylistDao
import com.example.data.local.TrackDao
import com.example.data.model.Playlist
import com.example.data.model.PlaylistTrackCrossRef
import com.example.data.model.Track
import kotlinx.coroutines.flow.Flow

class TrackRepository(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao
) {
    val allTracks: Flow<List<Track>> = trackDao.getAllTracks()
    val favoriteTracks: Flow<List<Track>> = trackDao.getFavoriteTracks()
    val recentlyPlayed: Flow<List<Track>> = trackDao.getRecentlyPlayedTracks()
    val mostListened: Flow<List<Track>> = trackDao.getMostListenedTracks()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getTracksByMood(mood: String): Flow<List<Track>> = trackDao.getTracksByMood(mood)
    
    fun searchTracks(query: String): Flow<List<Track>> = trackDao.searchTracks(query)

    suspend fun getTrackById(id: Int): Track? = trackDao.getTrackById(id)

    suspend fun insertTrack(track: Track): Long = trackDao.insertTrack(track)

    suspend fun insertTracks(tracks: List<Track>) = trackDao.insertTracks(tracks)

    suspend fun updateTrack(track: Track) = trackDao.updateTrack(track)

    suspend fun deleteTrack(track: Track) = trackDao.deleteTrack(track)

    suspend fun incrementPlayCountAndUpdatedLastPlayed(trackId: Int, timestamp: Long) =
        trackDao.incrementPlayCountAndUpdatedLastPlayed(trackId, timestamp)

    // Playlists
    suspend fun createPlaylist(name: String, description: String = "", imagePresetId: Int = 0): Long {
        val playlist = Playlist(name = name, description = description, imagePresetId = imagePresetId)
        return playlistDao.insertPlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylistRelations(playlist.id)
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Int, trackId: Int) {
        playlistDao.insertTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Int, trackId: Int) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: Int): Flow<List<Track>> =
        playlistDao.getTracksForPlaylist(playlistId)

    fun getTrackCountForPlaylist(playlistId: Int): Flow<Int> =
        playlistDao.getTrackCountForPlaylist(playlistId)
}
