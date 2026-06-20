package com.example.playback

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.util.Log
import com.example.data.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class SilentRushPlayer(private val context: Context) {
    private val TAG = "SilentRushPlayer"

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null

    // Track Queue
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    // Playback States
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0L)
    val playbackProgress: StateFlow<Long> = _playbackProgress.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE) // NONE, ONE, ALL
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Sleep Timer States
    private val _sleepTimerRemainingSec = MutableStateFlow(0)
    val sleepTimerRemainingSec: StateFlow<Int> = _sleepTimerRemainingSec.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var progressTrackingJob: Job? = null
    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Equalizer States
    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _eqBands = MutableStateFlow<List<EqBand>>(emptyList())
    val eqBands: StateFlow<List<EqBand>> = _eqBands.asStateFlow()

    // Listeners
    var onTrackCompleted: ((Track) -> Unit)? = null

    init {
        initializeMediaPlayer()
    }

    private fun initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener {
                    handleCompletion()
                }
                setOnPreparedListener {
                    startPlayback()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    // Reset and attempt recovery
                    mp.reset()
                    true
                }
            }
        }
    }

    private fun releaseEqualizer() {
        try {
            equalizer?.release()
            equalizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release Equalizer", e)
        }
    }

    private fun setupEqualizer(audioSessionId: Int) {
        releaseEqualizer()
        if (audioSessionId == 0) return
        try {
            val eq = Equalizer(0, audioSessionId)
            if (eq.numberOfBands > 0) {
                eq.enabled = _eqEnabled.value
                val bandsList = mutableListOf<EqBand>()
                
                // Fetch bands and frequencies
                val numBands = eq.numberOfBands.toInt()
                val minLevel = eq.bandLevelRange[0] // in millibels
                val maxLevel = eq.bandLevelRange[1]
                val range = maxLevel - minLevel

                for (i in 0 until numBands) {
                    val bandFreqHz = eq.getCenterFreq(i.toShort()) / 1000 // Convert mHz to Hz
                    val label = when {
                        bandFreqHz < 100 -> "${bandFreqHz}Hz"
                        bandFreqHz < 1000 -> "${bandFreqHz}Hz"
                        else -> "${bandFreqHz / 1000}kHz"
                    }
                    val currentVal = eq.getBandLevel(i.toShort())
                    
                    bandsList.add(
                        EqBand(
                            id = i,
                            label = label,
                            centerFreqHz = bandFreqHz,
                            minLevelMilliBels = minLevel.toInt(),
                            maxLevelMilliBels = maxLevel.toInt(),
                            currentLevelMilliBels = currentVal.toInt()
                        )
                    )
                }
                _eqBands.value = bandsList
                equalizer = eq
            }
        } catch (e: Exception) {
            Log.e(TAG, "Equalizer initialization failed gracefully.", e)
            // Fallback for emulator (create mock frequencies to show custom visual sliders that are beautiful)
            setupMockEqualizer()
        }
    }

    private fun setupMockEqualizer() {
        val mockBands = listOf(
            EqBand(0, "60Hz", 60, -1500, 1500, 0),
            EqBand(1, "230Hz", 230, -1500, 1500, 100),
            EqBand(2, "910Hz", 910, -1500, 1500, -300),
            EqBand(3, "4kHz", 4000, -1500, 1500, 400),
            EqBand(4, "14kHz", 14000, -1500, 1500, 200)
        )
        _eqBands.value = mockBands
    }

    fun setQueue(tracks: List<Track>, playIndex: Int = 0) {
        if (tracks.isEmpty()) return
        
        _queue.value = if (_shuffleEnabled.value) {
            val current = tracks[playIndex]
            val shuffled = (tracks - current).shuffled().toMutableList()
            shuffled.add(0, current)
            shuffled
        } else {
            tracks
        }

        val idx = if (_shuffleEnabled.value) 0 else playIndex
        playTrackIndex(idx)
    }

    fun playTrackIndex(index: Int) {
        val tracks = _queue.value
        if (tracks.isEmpty() || index < 0 || index >= tracks.size) return
        
        _currentIndex.value = index
        val track = tracks[index]
        _currentTrack.value = track
        
        playTrack(track)
    }

    private fun playTrack(track: Track) {
        try {
            initializeMediaPlayer()
            mediaPlayer?.apply {
                reset()
                
                val file = File(track.fileUri)
                if (file.exists()) {
                    setDataSource(file.absolutePath)
                } else {
                    // Fallback to presets if file is missing, or content-resolver
                    Log.e(TAG, "File does not exist: ${track.fileUri}")
                    return
                }
                
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing track: ${track.title}", e)
        }
    }

    private fun startPlayback() {
        mediaPlayer?.let { mp ->
            mp.start()
            _isPlaying.value = true
            setupEqualizer(mp.audioSessionId)
            startProgressTracking()
            _currentTrack.value?.let { track ->
                onTrackCompleted?.invoke(track) // Inform ViewModel to update stats/recent played
            }
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            _isPlaying.value = false
            stopProgressTracking()
        } else {
            if (_currentIndex.value == -1 && _queue.value.isNotEmpty()) {
                setQueue(_queue.value, 0)
            } else {
                mp.start()
                _isPlaying.value = true
                startProgressTracking()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(positionMs.toInt())
                _playbackProgress.value = positionMs
            } catch (e: Exception) {
                Log.e(TAG, "Seek failed", e)
            }
        }
    }

    fun playNext() {
        val index = _currentIndex.value
        val tracks = _queue.value
        if (tracks.isEmpty()) return

        when {
            _repeatMode.value == RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracking()
            }
            index < tracks.size - 1 -> {
                playTrackIndex(index + 1)
            }
            _repeatMode.value == RepeatMode.ALL -> {
                playTrackIndex(0) // wrap around
            }
            else -> {
                // stop playback at end of queue
                _isPlaying.value = false
                mediaPlayer?.pause()
                seekTo(0)
                stopProgressTracking()
            }
        }
    }

    fun playPrevious() {
        val index = _currentIndex.value
        val tracks = _queue.value
        if (tracks.isEmpty()) return

        if (playbackProgress.value > 3000L) {
            seekTo(0)
        } else if (index > 0) {
            playTrackIndex(index - 1)
        } else if (_repeatMode.value == RepeatMode.ALL) {
            playTrackIndex(tracks.size - 1) // Wrap to last
        } else {
            seekTo(0)
        }
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
        val current = _currentTrack.value
        val list = _queue.value
        if (list.isNotEmpty() && current != null) {
            if (_shuffleEnabled.value) {
                // Shuffle keeping current at front
                val remaining = (list - current).shuffled()
                _queue.value = listOf(current) + remaining
                _currentIndex.value = 0
            } else {
                // Return to some sorted/original order if possible or just keep current order
                // Let's just keep queue as is to avoid UI jumping, but flag state
            }
        }
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
    }

    private fun handleCompletion() {
        _playbackProgress.value = 0
        playNext()
    }

    private fun startProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = playerScope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _playbackProgress.value = mp.currentPosition.toLong()
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTracking() {
        progressTrackingJob?.cancel()
    }

    // --- SLEEP TIMER ---
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemainingSec.value = 0
            setVolume(1.0f)
            return
        }

        _sleepTimerRemainingSec.value = minutes * 60
        sleepTimerJob = playerScope.launch {
            var remaining = minutes * 60
            while (remaining > 0) {
                delay(1000)
                remaining--
                _sleepTimerRemainingSec.value = remaining

                // Soft fade out in the last 15 seconds
                if (remaining <= 15) {
                    val progress = remaining.toFloat() / 15.0f // 1.0 down to 0.0
                    setVolume(progress)
                }
            }

            // Time's up! Stop everything and reset volume
            stopTimerAndFadeOutQuietly()
        }
    }

    private fun stopTimerAndFadeOutQuietly() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
            }
        }
        stopProgressTracking()
        _sleepTimerRemainingSec.value = 0
        setVolume(1.0f) // reset volume back to normal for next launch
        sleepTimerJob = null
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSec.value = 0
        setVolume(1.0f)
    }

    private fun setVolume(volume: Float) {
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            Log.e(TAG, "setVolume failed", e)
        }
    }

    // --- EQUALIZER MODULATOR ---
    fun toggleEqualizer() {
        _eqEnabled.value = !_eqEnabled.value
        try {
            equalizer?.enabled = _eqEnabled.value
        } catch (e: Exception) {
            Log.e(TAG, "Failed toggling equalizer effect.", e)
        }
    }

    fun setEqualizerBandLevel(bandId: Int, levelMilliBels: Int) {
        // Update model structure
        val currentList = _eqBands.value
        _eqBands.value = currentList.map { band ->
            if (band.id == bandId) {
                band.copy(currentLevelMilliBels = levelMilliBels)
            } else band
        }

        // Apply to physical equalizer session
        try {
            equalizer?.let { eq ->
                if (eq.enabled) {
                    eq.setBandLevel(bandId.toShort(), levelMilliBels.toShort())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed applying band level change.", e)
        }
    }

    fun release() {
        playerScope.cancel()
        releaseEqualizer()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

enum class RepeatMode {
    NONE, ALL, ONE
}

data class EqBand(
    val id: Int,
    val label: String,
    val centerFreqHz: Int,
    val minLevelMilliBels: Int,
    val maxLevelMilliBels: Int,
    val currentLevelMilliBels: Int
)
