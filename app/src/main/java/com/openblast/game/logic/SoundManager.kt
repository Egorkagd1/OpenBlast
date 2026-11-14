package com.openblast.game.logic

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val prefs = context.getSharedPreferences("openblast_settings", Context.MODE_PRIVATE)

    var isSoundEnabled: Boolean = prefs.getBoolean("sound_enabled", true)
        set(value) {
            field = value
            prefs.edit().putBoolean("sound_enabled", value).apply()
        }

    var isHapticsEnabled: Boolean = prefs.getBoolean("haptics_enabled", true)
        set(value) {
            field = value
            prefs.edit().putBoolean("haptics_enabled", value).apply()
        }

    fun playPickUp() {
        if (!isSoundEnabled) return
        coroutineScope.launch {
            val audio = generateSweep(500.0, 750.0, 50)
            playPcm(audio)
        }
    }

    fun playPlace() {
        if (isHapticsEnabled) {
            vibrate(30, 80)
        }
        if (!isSoundEnabled) return
        coroutineScope.launch {
            val audio = generatePop(180.0, 60)
            playPcm(audio)
        }
    }

    fun playBlast(comboCount: Int) {
        if (isHapticsEnabled) {
            val strength = (100 + comboCount * 25).coerceAtMost(255)
            vibrate(80, strength)
        }
        if (!isSoundEnabled) return
        coroutineScope.launch {
            val notes = listOf(
                523.25, // C5
                659.25, // E5
                783.99, // G5
                1046.50, // C6
                1318.51, // E6
                1567.98  // G6
            )
            val noteIndex = (comboCount - 1).coerceIn(0, notes.size - 1)
            val baseFreq = notes[noteIndex]
            val audio = generateBlastChord(baseFreq, 140)
            playPcm(audio)
        }
    }

    fun playGameOver() {
        if (isHapticsEnabled) {
            vibrate(150, 150)
        }
        if (!isSoundEnabled) return
        coroutineScope.launch {
            val audio = generateSweep(400.0, 120.0, 300)
            playPcm(audio)
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int = 100) {
        vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, clampedAmp)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun generateSweep(startFreq: Double, endFreq: Double, durationMs: Int): ShortArray {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val envelope = (1.0 - progress) // Linear fade out
            phase += 2.0 * PI * currentFreq / sampleRate

            val sample = (sin(phase) * envelope * 24000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generatePop(freq: Double, durationMs: Int): ShortArray {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val expDecay = Math.exp(-progress * 5.0)
            phase += 2.0 * PI * (freq * (1.0 - progress * 0.4)) / sampleRate

            val sample = (sin(phase) * expDecay * 28000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateBlastChord(baseFreq: Double, durationMs: Int): ShortArray {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)

        val fifthFreq = baseFreq * 1.498307 // Perfect fifth
        val octaveFreq = baseFreq * 2.0

        var phase1 = 0.0
        var phase2 = 0.0
        var phase3 = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val envelope = Math.exp(-progress * 3.5)

            phase1 += 2.0 * PI * baseFreq / sampleRate
            phase2 += 2.0 * PI * fifthFreq / sampleRate
            phase3 += 2.0 * PI * octaveFreq / sampleRate

            val s1 = sin(phase1) * 0.5
            val s2 = sin(phase2) * 0.3
            val s3 = sin(phase3) * 0.2

            val combined = (s1 + s2 + s3) * envelope * 28000
            buffer[i] = combined.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val sampleRate = 44100
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()

            // Release track after it finishes
            coroutineScope.launch {
                val delayMs = (buffer.size.toDouble() / sampleRate * 1000).toLong() + 100
                kotlinx.coroutines.delay(delayMs)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
        }
    }
}
