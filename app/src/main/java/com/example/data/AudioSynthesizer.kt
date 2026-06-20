package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.model.Track
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin
import kotlin.random.Random

object AudioSynthesizer {
    private const val TAG = "AudioSynthesizer"
    private const val SAMPLE_RATE = 11025 // Lightweight but perfectly clear for atmospheric pads
    private const val BITS_PER_SAMPLE = 16
    private const val CHANNELS = 1

    /**
     * Synthesizes default ambient files into app storage if they don't already exist.
     * Returns the list of Track metadata to seed the database.
     */
    fun synthesizePresetsIfNeeded(context: Context): List<Track> {
        val outputDir = File(context.filesDir, "silent_rush")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val presets = listOf(
            PresetData(
                filename = "silent_rush.wav",
                title = "Silent Rush",
                artist = "Sôla",
                album = "Quiet Stars",
                mood = "Peaceful",
                durationSec = 45,
                lyrics = "0:Instrumental;5:Some songs are not heard, they are felt...;13:Close your eyes, let the slate-colored starlight descend.;23:In this peaceful midnight, we realize how mentally strong we are.;35:Simple on the outside, deep on the inside.;42:Feel the silent rush of calm sweep your soul.",
                synthType = SynthType.CHIMES
            ),
            PresetData(
                filename = "caelum_whispers.wav",
                title = "Caelum Whispers",
                artist = "Lune",
                album = "Aether",
                mood = "Deep",
                durationSec = 45,
                lyrics = "0:Cosmic Frequency;6:A mature heart welcomes the night.;14:No flashy colors... just deep stars and space.;23:You are carrying silent strength within you.;31:Caelum is the constellation of engraving.;39:Let your worries fade into the silent velvet sky.",
                synthType = SynthType.SPACE
            ),
            PresetData(
                filename = "rain_kyoto.wav",
                title = "Rain over Kyoto",
                artist = "Niwatorii",
                album = "Solitude Logs",
                mood = "Calm",
                durationSec = 45,
                lyrics = "0:Rainfall on temple grounds;8:Raindrops wash the busy noise away...;18:Kyoto wind-bells chiming in the quiet mist.;26:They whisper that solitude is not loneliness.;34:It is deep awareness and self-contentment.;41:Breathe the cool air, let the droplets calm your pulse.",
                synthType = SynthType.RAIN
            ),
            PresetData(
                filename = "cosmic_slumber.wav",
                title = "Cosmic Slumber",
                artist = "Sôla",
                album = "Midnight Sleep",
                mood = "Sleep",
                durationSec = 45,
                lyrics = "0:Deep Sleep Binaural Waves;9:Sinking safely into the soft twilight...;19:The moon holds your secrets tonight.;29:You are protected, secure, and entirely okay.;39:Rest your eyes and drift into peaceful silence.",
                synthType = SynthType.SLUMBER
            ),
            PresetData(
                filename = "celestial_aura.wav",
                title = "Celestial Aura",
                artist = "Aetherius",
                album = "Warm Glows",
                mood = "Focus",
                durationSec = 45,
                lyrics = "0:Warm Celestial Aura Swells;7:Rising like a quiet stargazing tide.;16:A simple soul is a serene fortress.;25:Focusing inward, finding center.;35:Let the soft glowing aura guide your clarity.",
                synthType = SynthType.AURA
            )
        )

        return presets.map { preset ->
            val file = File(outputDir, preset.filename)
            if (!file.exists() || file.length() < 1000) {
                Log.d(TAG, "Synthesizing track: ${preset.title} to path: ${file.absolutePath}")
                try {
                    writeWavFile(file, preset.durationSec, preset.synthType)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed synthethizing ${preset.title}", e)
                }
            }
            Track(
                title = preset.title,
                artist = preset.artist,
                album = preset.album,
                fileUri = file.absolutePath,
                durationMs = preset.durationSec * 1000L,
                mood = preset.mood,
                lyricTimings = preset.lyrics,
                isFavorite = false,
                playCount = 0
            )
        }
    }

    private data class PresetData(
        val filename: String,
        val title: String,
        val artist: String,
        val album: String,
        val mood: String,
        val durationSec: Int,
        val lyrics: String,
        val synthType: SynthType
    )

    private enum class SynthType {
        CHIMES, SPACE, RAIN, SLUMBER, AURA
    }

    private fun writeWavFile(file: File, durationSec: Int, type: SynthType) {
        val totalSamples = SAMPLE_RATE * durationSec
        val dataSize = totalSamples * (BITS_PER_SAMPLE / 8) * CHANNELS
        val totalFileSize = dataSize + 36 // size of header remaining

        FileOutputStream(file).use { out ->
            // 1. WAV HEADER
            out.write("RIFF".toByteArray()) // RIFF marker
            out.write(intToByteArray(totalFileSize)) // Size of entire file - 8 bytes
            out.write("WAVE".toByteArray()) // WAVE pattern
            out.write("fmt ".toByteArray()) // format chunk marker
            out.write(intToByteArray(16)) // subchunk size (16 for PCM)
            out.write(shortToByteArray(1)) // Audio format (1 = PCM)
            out.write(shortToByteArray(CHANNELS.toShort())) // 1 Channel (Mono)
            out.write(intToByteArray(SAMPLE_RATE)) // Sample rate
            out.write(intToByteArray(SAMPLE_RATE * CHANNELS * (BITS_PER_SAMPLE / 8))) // Byte rate
            out.write(shortToByteArray((CHANNELS * (BITS_PER_SAMPLE / 8)).toShort())) // Block align
            out.write(shortToByteArray(BITS_PER_SAMPLE.toShort())) // Bits per sample
            out.write("data".toByteArray()) // data chunk header
            out.write(intToByteArray(dataSize)) // data size

            // 2. SYNTHESIZE AUDIO SAMPLES
            val buffer = ByteBuffer.allocate(4096)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            val notes = listOf(261.63, 329.63, 392.00, 493.88, 523.25) // C4, E4, G4, B4, C5
            var timeIndex = 0

            // Audio synthesis loops
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sampleVal = 0.0

                when (type) {
                    SynthType.CHIMES -> {
                        // Soft wind-chime bell tones decaying exponentially.
                        // Ring a distinct note every 4 seconds
                        val period = 4.0
                        val localT = t % period
                        val noteIdx = ((t / period).toInt()) % notes.size
                        val frequency = notes[noteIdx]
                        
                        // Exp decay: e^(-2 * t)
                        val decay = kotlin.math.exp(-1.5 * localT)
                        
                        // Main tone + 1st & 2nd harmonics for a rich chime bell metallic sound
                        if (localT > 0) {
                            sampleVal = (sin(2 * Math.PI * frequency * localT) +
                                    0.5 * sin(2 * Math.PI * frequency * 2 * localT) +
                                    0.25 * sin(2 * Math.PI * frequency * 3 * localT)) * decay
                        }
                        
                        // Add a very warm, quiet, ultra-low background pad (60Hz)
                        sampleVal += 0.15 * sin(2 * Math.PI * 65.0 * t)
                    }

                    SynthType.SPACE -> {
                        // Deep space drifting drone: oscillating low carriers
                        // 75 Hz base swept with a very slow 0.1Hz LFO
                        val lfo = sin(2 * Math.PI * 0.05 * t)
                        val freq = 75.0 + lfo * 8.0
                        sampleVal = sin(2 * Math.PI * freq * t)
                        
                        // Add some soft upper harmony (150Hz and 225Hz) with phase sweeps
                        sampleVal += 0.3 * sin(2 * Math.PI * (freq * 2) * t + sin(0.2 * t))
                        sampleVal += 0.1 * sin(2 * Math.PI * (freq * 3) * t)
                        
                        // Cozy resonance
                        sampleVal *= 0.7
                    }

                    SynthType.RAIN -> {
                        // Soft rain hiss (white noise) + soft drips
                        // Low pass white noise by smoothing samples
                        val whiteNoise = Random.nextFloat() * 2.0 - 1.0
                        sampleVal = whiteNoise * 0.25

                        // Occasional drop (like random rain clicks) with high resonance
                        if (Random.nextInt(1000) < 3) {
                            // Synthesize a quick high click
                            val dripFreq = 1200.0 + Random.nextInt(300)
                            // We can simulate an impulse decaying extremely fast
                            sampleVal += 0.6 * sin(2 * Math.PI * dripFreq * t) * kotlin.math.exp(-40.0 * (t % 0.1))
                        }
                        
                        // Deep soothing thunder rumble (low frequency bursts)
                        val rumbler = sin(2 * Math.PI * 30.0 * t) * (sin(0.12 * t) * 0.5 + 0.5)
                        sampleVal += rumbler * 0.15
                    }

                    SynthType.SLUMBER -> {
                        // Binaural beating waves: Low carrier frequencies detuned by 4Hz
                        val leftCarrier = sin(2 * Math.PI * 90.0 * t)
                        val rightCarrier = sin(2 * Math.PI * 94.0 * t) // Detuned for theta alpha brainwave sync!
                        
                        // Mixed into mono as an elegant wavy volume swell
                        sampleVal = (leftCarrier + rightCarrier) * 0.45
                        
                        // Add an extremely quiet comforting warm blanket frequency at 48Hz
                        sampleVal += 0.2 * sin(2 * Math.PI * 48.0 * t)
                    }

                    SynthType.AURA -> {
                        // Warm organic pad synthesizer (slow phase shifts)
                        val baseFreq = 110.0 // A2 chord pad
                        // Triad notes: A2 (110.0), C#3 (138.59), E3 (164.81)
                        val lfo1 = sin(2 * Math.PI * 0.1 * t) * 0.4 + 0.6
                        val lfo2 = sin(2 * Math.PI * 0.15 * t + 1.0) * 0.4 + 0.6
                        
                        val note1 = sin(2 * Math.PI * baseFreq * t) * lfo1
                        val note2 = sin(2 * Math.PI * 138.59 * t) * lfo2
                        val note3 = sin(2 * Math.PI * 164.81 * t) * 0.5
                        
                        sampleVal = (note1 + note2 + note3) * 0.35
                    }
                }

                // Clip control to avoid distortion
                if (sampleVal > 1.0) sampleVal = 1.0
                if (sampleVal < -1.0) sampleVal = -1.0

                // Convert to short (16-bit PCM)
                val shortVal = (sampleVal * 32767.0).toInt().toShort()

                if (!buffer.hasRemaining()) {
                    out.write(buffer.array())
                    buffer.clear()
                }
                buffer.putShort(shortVal)
                timeIndex++
            }

            // Flush remaining buffer
            if (buffer.position() > 0) {
                out.write(buffer.array(), 0, buffer.position())
            }
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        val b = ByteArray(4)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        b[2] = (value shr 16 and 0xff).toByte()
        b[3] = (value shr 24 and 0xff).toByte()
        return b
    }

    private fun shortToByteArray(value: Short): ByteArray {
        val b = ByteArray(2)
        b[0] = (value.toInt() and 0xff).toByte()
        b[1] = (value.toInt() shr 8 and 0xff).toByte()
        return b
    }
}
