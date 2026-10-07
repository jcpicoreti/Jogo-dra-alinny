package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isSfxEnabled: Boolean = true
    var isHapticEnabled: Boolean = true

    private val sampleRate = 22050 // Optimized 22kHz for fast loading and low memory footprint

    // SoundPool for crash-free, zero-latency audio playback
    private val soundPool: SoundPool? = try {
        SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    } catch (_: Throwable) {
        null
    }

    private val soundIds = ConcurrentHashMap<String, Int>()

    init {
        scope.launch {
            try {
                loadProceduralSound("brush") { generateSweepBuffer(300f, 900f, 0.10f) }
                loadProceduralSound("floss") { generateSweepBuffer(1200f, 400f, 0.08f) }
                loadProceduralSound("laser") { generateLaserBuffer() }
                loadProceduralSound("drill") { generateDrillBuffer() }
                loadProceduralSound("splash") { generateSplashBuffer() }
                loadProceduralSound("hit") { generateHitBuffer() }
                loadProceduralSound("level_up") { generateFanfareBuffer() }
                loadProceduralSound("boss_roar") { generateBossRoarBuffer() }
                loadProceduralSound("chest") { generateChestArpeggioBuffer() }
                loadProceduralSound("game_over") { generateGameOverBuffer() }
                loadProceduralSound("water_jet") { generateWaterJetBuffer() }
                loadProceduralSound("gem") { generateChimeBuffer(659.25f, 0.08f) }
            } catch (_: Throwable) {}
        }
    }

    private fun loadProceduralSound(name: String, generator: () -> ShortArray) {
        val pool = soundPool ?: return
        try {
            val pcm = generator()
            val wavFile = File(context.cacheDir, "sfx_$name.wav")
            writeWavFile(wavFile, pcm, sampleRate)
            val soundId = pool.load(wavFile.absolutePath, 1)
            if (soundId > 0) {
                soundIds[name] = soundId
            }
        } catch (_: Throwable) {}
    }

    fun playBrush() {
        playSound("brush", volume = 0.65f)
    }

    fun playFloss() {
        playSound("floss", volume = 0.7f)
    }

    fun playLaser() {
        playSound("laser", volume = 0.8f)
    }

    fun playDrill() {
        playSound("drill", volume = 0.6f)
    }

    fun playSplash() {
        playSound("splash", volume = 0.65f)
    }

    fun playWaterJet() {
        playSound("water_jet", volume = 0.6f)
    }

    fun playHit() {
        playSound("hit", volume = 0.5f)
        vibrate(25)
    }

    fun playGemPickup(combo: Int = 0) {
        if (!isSfxEnabled) return
        val pool = soundPool ?: return
        val soundId = soundIds["gem"] ?: return
        if (soundId <= 0) return
        try {
            val rate = (1.0f + (combo % 6) * 0.08f).coerceIn(0.5f, 2.0f)
            pool.play(soundId, 0.6f, 0.6f, 1, 0, rate)
        } catch (_: Throwable) {}
    }

    fun playLevelUp() {
        playSound("level_up", volume = 0.9f)
        vibrate(80)
    }

    fun playBossRoar() {
        playSound("boss_roar", volume = 0.95f)
        vibrate(180)
    }

    fun playChestOpen() {
        playSound("chest", volume = 0.9f)
        vibrate(70)
    }

    fun playGameOver() {
        playSound("game_over", volume = 0.85f)
    }

    private fun playSound(key: String, volume: Float = 0.7f) {
        if (!isSfxEnabled) return
        val pool = soundPool ?: return
        val soundId = soundIds[key] ?: return
        if (soundId <= 0) return
        try {
            pool.play(soundId, volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f), 1, 0, 1.0f)
        } catch (_: Throwable) {}
    }

    private fun vibrate(durationMs: Long) {
        if (!isHapticEnabled || vibrator == null) return
        try {
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs.coerceIn(10L, 500L), VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs.coerceIn(10L, 500L))
            }
        } catch (_: Throwable) {}
    }

    fun release() {
        try {
            soundPool?.release()
        } catch (_: Throwable) {}
    }

    // Standard 44-byte WAV header generator
    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val totalAudioLen = pcmData.size * 2
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2

        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(totalDataLen)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16) // Subchunk1Size for PCM
            header.putShort(1.toShort()) // AudioFormat (1 = PCM)
            header.putShort(channels.toShort())
            header.putInt(sampleRate)
            header.putInt(byteRate)
            header.putShort((channels * 2).toShort()) // BlockAlign
            header.putShort(16.toShort()) // BitsPerSample
            header.put("data".toByteArray())
            header.putInt(totalAudioLen)
            out.write(header.array())

            val audioBytes = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmData) {
                audioBytes.putShort(sample)
            }
            out.write(audioBytes.array())
        }
    }

    // Procedural Audio Waveform Generators
    private fun generateSweepBuffer(startFreq: Float, endFreq: Float, durationSec: Float): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / durationSec
            val freq = startFreq + (endFreq - startFreq) * progress
            val env = (1.0f - progress) * (if (progress < 0.1f) progress * 10f else 1.0f)
            val sample = sin(2.0 * PI * freq * t) * env
            buffer[i] = (sample * 16000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateChimeBuffer(freq: Float, durationSec: Float): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = exp((-t * 22f).toDouble()).toFloat()
            val sample = (sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)) * env
            buffer[i] = (sample * 20000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateLaserBuffer(): ShortArray {
        val durationSec = 0.12f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / durationSec
            val freq = 1600f * exp((-progress * 4f).toDouble()).toFloat() + 250f
            val env = 1.0f - progress
            val sample = sin(2.0 * PI * freq * t) * env
            buffer[i] = (sample * 22000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateDrillBuffer(): ShortArray {
        val durationSec = 0.14f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val fm = sin(2.0 * PI * 120.0 * t).toFloat() * 150f
            val freq = 800f + fm
            val env = 0.8f
            val sample = sin(2.0 * PI * freq * t) * env
            buffer[i] = (sample * 15000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSplashBuffer(): ShortArray {
        val durationSec = 0.14f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var noise = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / durationSec
            noise += (Random.nextFloat() * 2f - 1f) * 0.3f
            noise *= 0.85f
            val env = (1.0f - progress)
            val bubble = sin(2.0 * PI * (350f + progress * 200f) * t) * 0.6f
            val sample = (noise + bubble) * env
            buffer[i] = (sample * 18000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateHitBuffer(): ShortArray {
        val durationSec = 0.06f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / durationSec
            val freq = 200f * (1.0f - progress)
            val noise = (Random.nextFloat() * 2f - 1f) * 0.4f
            val env = (1.0f - progress) * (1.0f - progress)
            val sample = (sin(2.0 * PI * freq * t) * 0.7f + noise) * env
            buffer[i] = (sample * 22000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateWaterJetBuffer(): ShortArray {
        val durationSec = 0.15f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var filteredNoise = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val raw = Random.nextFloat() * 2f - 1f
            filteredNoise = filteredNoise * 0.75f + raw * 0.25f
            val progress = t / durationSec
            val env = (1.0f - progress * progress)
            buffer[i] = (filteredNoise * env * 16000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBossRoarBuffer(): ShortArray {
        val durationSec = 0.45f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / durationSec
            val freq = 80f + 25f * sin(2.0 * PI * 18.0 * t).toFloat()
            val distortion = sin(2.0 * PI * freq * t)
            val env = if (progress < 0.2f) progress * 5f else (1.0f - progress) * 1.25f
            val sample = (distortion + (Random.nextFloat() * 0.25f - 0.12f)) * env.coerceIn(0f, 1f)
            buffer[i] = (sample * 24000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateFanfareBuffer(): ShortArray {
        val durationSec = 0.50f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
        val noteDuration = durationSec / notes.size
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIndex = (t / noteDuration).toInt().coerceIn(0, notes.size - 1)
            val noteT = t - noteIndex * noteDuration
            val freq = notes[noteIndex]
            val env = exp((-noteT * 6.0).toDouble()).toFloat()
            val sample = (sin(2.0 * PI * freq * noteT) + 0.25 * sin(4.0 * PI * freq * noteT)) * env
            buffer[i] = (sample * 22000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateChestArpeggioBuffer(): ShortArray {
        val durationSec = 0.40f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val notes = listOf(440f, 554.37f, 659.25f, 880f)
        val noteDur = durationSec / notes.size
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIdx = (t / noteDur).toInt().coerceIn(0, notes.size - 1)
            val localT = t - noteIdx * noteDur
            val freq = notes[noteIdx]
            val env = exp((-localT * 7.0).toDouble()).toFloat()
            val sample = sin(2.0 * PI * freq * localT) * env
            buffer[i] = (sample * 20000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateGameOverBuffer(): ShortArray {
        val durationSec = 0.55f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val notes = listOf(440f, 415.30f, 392.00f, 349.23f)
        val noteDur = durationSec / notes.size
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIdx = (t / noteDur).toInt().coerceIn(0, notes.size - 1)
            val localT = t - noteIdx * noteDur
            val freq = notes[noteIdx]
            val env = (1.0f - localT / noteDur)
            val sample = sin(2.0 * PI * freq * localT) * env
            buffer[i] = (sample * 18000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }
}
