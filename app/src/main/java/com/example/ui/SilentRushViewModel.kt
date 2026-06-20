package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AudioSynthesizer
import com.example.data.local.SilentRushDatabase
import com.example.data.model.LyricLine
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.data.repository.TrackRepository
import com.example.playback.EqBand
import com.example.playback.RepeatMode
import com.example.playback.SilentRushPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class SilentRushViewModel(application: Application) : AndroidViewModel(application) {
    private const val TAG = "SilentRushViewModel"

    private val repository: TrackRepository
    private val player: SilentRushPlayer

    // UI Tab State
    private val _currentTab = MutableStateFlow(HomeTab.HOME)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    // Database Streams
    val allTracks: StateFlow<List<Track>> = MutableStateFlow<List<Track>>(emptyList())
    val favoriteTracks: StateFlow<List<Track>> = MutableStateFlow<List<Track>>(emptyList())
    val recentlyPlayed: StateFlow<List<Track>> = MutableStateFlow<List<Track>>(emptyList())
    val mostListened: StateFlow<List<Track>> = MutableStateFlow<List<Track>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = MutableStateFlow<List<Playlist>>(emptyList())

    // Filtered lists
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchedTracks: StateFlow<List<Track>> = _searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allTracks
            } else {
                repository.searchTracks(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Mood Filter
    private val _selectedMood = MutableStateFlow("All")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    val moodTracks: StateFlow<List<Track>> = _selectedMood
        .flatMapLatest { mood ->
            if (mood == "All") {
                repository.allTracks
            } else {
                repository.getTracksByMood(mood)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selected Playlist details
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _playlistTracks = MutableStateFlow<List<Track>>(emptyList())
    val playlistTracks: StateFlow<List<Track>> = _playlistTracks.asStateFlow()

    // Player Expose
    val queue: StateFlow<List<Track>>
    val currentIndex: StateFlow<Int>
    val currentTrack: StateFlow<Track?>
    val isPlaying: StateFlow<Boolean>
    val playbackProgress: StateFlow<Long>
    val shuffleEnabled: StateFlow<Boolean>
    val repeatMode: StateFlow<RepeatMode>
    val sleepTimerRemainingSec: StateFlow<Int>
    val eqEnabled: StateFlow<Boolean>
    val eqBands: StateFlow<List<EqBand>>

    // Live Lyrics
    private val _activeLyricIndex = MutableStateFlow(-1)
    val activeLyricIndex: StateFlow<Int> = _activeLyricIndex.asStateFlow()

    // Sheet States
    private val _nowPlayingExpanded = MutableStateFlow(false)
    val nowPlayingExpanded: StateFlow<Boolean> = _nowPlayingExpanded.asStateFlow()

    // Composers / Importers State
    private val _showComposerDialog = MutableStateFlow(false)
    val showComposerDialog: StateFlow<Boolean> = _showComposerDialog.asStateFlow()

    init {
        // Initialize SQLite and repository
        val database = SilentRushDatabase.getDatabase(application)
        repository = TrackRepository(database.trackDao, database.playlistDao)
        player = SilentRushPlayer(application)

        // Sync player states
        queue = player.queue
        currentIndex = player.currentIndex
        currentTrack = player.currentTrack
        isPlaying = player.isPlaying
        playbackProgress = player.playbackProgress
        shuffleEnabled = player.shuffleEnabled
        repeatMode = player.repeatMode
        sleepTimerRemainingSec = player.sleepTimerRemainingSec
        eqEnabled = player.eqEnabled
        eqBands = player.eqBands

        // Tie database observers to ViewModel scope
        allTracks = repository.allTracks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        favoriteTracks = repository.favoriteTracks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        recentlyPlayed = repository.recentlyPlayed.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        mostListened = repository.mostListened.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        playlists = repository.allPlaylists.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        // Load Preset default files and database seeds
        viewModelScope.launch(Dispatchers.IO) {
            val count = database.trackDao.getAllTracks().first().size
            if (count == 0) {
                Log.d(TAG, "Empty Database. Seeding lovely comforting ambient preset files...")
                val seededTracks = AudioSynthesizer.synthesizePresetsIfNeeded(application)
                repository.insertTracks(seededTracks)
                // Seed secondary moody playlists
                repository.createPlaylist("Midnight Slumber", "Deep and serene sounds to help sleep", 1)
                repository.createPlaylist("Focus Sphere", "Minimal ambient tunes for strong minds", 2)
            }
        }

        // Active lyric syncing with playback progress
        viewModelScope.launch {
            combine(currentTrack, playbackProgress) { track, progress ->
                track to progress
            }.collect { (track, progress) ->
                if (track == null || progress <= 0) {
                    _activeLyricIndex.value = -1
                } else {
                    val timings = track.getParsedLyrics()
                    var index = -1
                    for (i in timings.indices) {
                        if (progress >= timings[i].timestampMs) {
                            index = i
                        } else {
                            break
                        }
                    }
                    _activeLyricIndex.value = index
                }
            }
        }

        // Track completed -> trigger Database update
        player.onTrackCompleted = { track ->
            viewModelScope.launch(Dispatchers.IO) {
                repository.incrementPlayCountAndUpdatedLastPlayed(track.id, System.currentTimeMillis())
            }
        }
    }

    fun setTab(tab: HomeTab) {
        _currentTab.value = tab
    }

    fun setMood(mood: String) {
        _selectedMood.value = mood
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            viewModelScope.launch {
                repository.getTracksForPlaylist(playlist.id).collect { tracks ->
                    _playlistTracks.value = tracks
                }
            }
        } else {
            _playlistTracks.value = emptyList()
        }
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _nowPlayingExpanded.value = expanded
    }

    fun setComposerDialogVisible(visible: Boolean) {
        _showComposerDialog.value = visible
    }

    // --- PLAYER ACTION WRAPPERS ---
    fun playTrack(track: Track, customQueue: List<Track> = emptyList()) {
        val activeQueue = if (customQueue.isNotEmpty()) customQueue else allTracks.value
        val playIndex = activeQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.setQueue(activeQueue, playIndex)
    }

    fun playPlaylist(tracks: List<Track>) {
        if (tracks.isNotEmpty()) {
            player.setQueue(tracks, 0)
        }
    }

    fun togglePlayPause() = player.togglePlayPause()
    fun playNext() = player.playNext()
    fun playPrevious() = player.playPrevious()
    fun seekTo(progressMs: Long) = player.seekTo(progressMs)
    fun toggleShuffle() = player.toggleShuffle()
    fun cycleRepeatMode() = player.cycleRepeatMode()

    // --- SLEEP TIMER ---
    fun startSleepTimer(minutes: Int) = player.startSleepTimer(minutes)
    fun cancelSleepTimer() = player.cancelSleepTimer()

    // --- EQUALIZER ---
    fun toggleEqualizer() = player.toggleEqualizer()
    fun setEqualizerBandLevel(eqBand: EqBand, level: Int) = player.setEqualizerBandLevel(eqBand.id, level)

    // --- FAVORITES & COMPOSER WORKFLOWS ---
    fun toggleFavorite(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = track.copy(isFavorite = !track.isFavorite)
            repository.updateTrack(updated)
        }
    }

    fun deleteTrack(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete actual synthesized physical file if it exists in app files
            if (track.fileUri.startsWith(getApplication<Application>().filesDir.absolutePath)) {
                val file = File(track.fileUri)
                if (file.exists()) {
                    file.delete()
                }
            }
            repository.deleteTrack(track)
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createPlaylist(name, description, (1..4).random())
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlaylist(playlist)
            if (_selectedPlaylist.value?.id == playlist.id) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Int, trackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Int, trackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeTrackFromPlaylist(playlistId, trackId)
            // Reload tracks if looking at current playlist
            _selectedPlaylist.value?.let { current ->
                if (current.id == playlistId) {
                    _playlistTracks.value = _playlistTracks.value.filter { it.id != trackId }
                }
            }
        }
    }

    fun importLocalMockSong(title: String, artist: String, album: String, mood: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Generates a fully physical, customized loop inside the devices physical storage
            val outputDir = File(getApplication<Application>().filesDir, "silent_rush")
            val filename = "composed_${System.currentTimeMillis()}.wav"
            val file = File(outputDir, filename)
            
            val durationSec = 30
            val mockLyrics = "0:Instrumental Compose;5:Composer dreaming of $title...;15:An offline moment, simple and serene.;25:Stay mentally strong."
            
            // Choose a random physical sound wave design
            val types = AudioSynthesizer.SynthType.entries
            val synthType = types[(0 until types.size).random()]
            
            try {
                AudioSynthesizer.synthesizePresetsIfNeeded(getApplication())
                // Generates WAV file
                val fileWriterMethod = AudioSynthesizer::class.java.getDeclaredMethod(
                    "writeWavFile",
                    File::class.java,
                    Int::class.java,
                    synthType::class.java
                )
                fileWriterMethod.isAccessible = true
                fileWriterMethod.invoke(AudioSynthesizer, file, durationSec, synthType)
            } catch (e: Exception) {
                Log.e(TAG, "Dynamic writeWavFile reflection fallback.", e)
                // If reflection encounters any platform constraint, copy an existing preset file to act as the imported file!
                val samplePreset = File(outputDir, "silent_rush.wav")
                if (samplePreset.exists()) {
                    samplePreset.copyTo(file, overwrite = true)
                }
            }

            val imported = Track(
                title = title,
                artist = artist,
                album = album,
                fileUri = file.absolutePath,
                durationMs = durationSec * 1000L,
                mood = mood,
                lyricTimings = mockLyrics,
                isFavorite = false,
                playCount = 0
            )

            repository.insertTrack(imported)
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}

class SilentRushViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SilentRushViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SilentRushViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class HomeTab {
    HOME, SEARCH, PLAYLISTS, EQUALIZER
}
