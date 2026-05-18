package cl.catchgo.app.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

object NotificationSoundPlayer {
    private const val SAMPLE_RATE = 44100

    private fun generateTone(freq: Double, durationMs: Int, volume: Double = 1.0): ShortArray {
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val sample = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            // Generar onda senoidal (sine wave)
            val angle = 2.0 * Math.PI * i / (SAMPLE_RATE / freq)
            // Ramp down to simulate exponential decay
            val decay = 1.0 - (i.toDouble() / numSamples.toDouble())
            val amplitude = 32767.0 * volume * decay
            sample[i] = (sin(angle) * amplitude).toInt().toShort()
        }
        return sample
    }

    private suspend fun playToneSequence(vararg tones: Pair<ShortArray, Int>) {
        withContext(Dispatchers.IO) {
            try {
                // Calculate total length
                val totalSamples = tones.sumOf { it.first.size + (it.second * SAMPLE_RATE / 1000) }
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(totalSamples * 2) // 16 bit = 2 bytes per sample
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                val combinedBuffer = ShortArray(totalSamples)
                var offset = 0
                for (tone in tones) {
                    val sample = tone.first
                    val delayMs = tone.second
                    System.arraycopy(sample, 0, combinedBuffer, offset, sample.size)
                    offset += sample.size + (delayMs * SAMPLE_RATE / 1000)
                }

                audioTrack.write(combinedBuffer, 0, combinedBuffer.size)
                audioTrack.play()

                // Liberar recursos después de que termine
                Thread.sleep((totalSamples * 1000L / SAMPLE_RATE) + 100)
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun playSuccess() {
        val t1 = generateTone(523.25, 150, 0.5)
        val t2 = generateTone(783.99, 250, 0.5)
        playToneSequence(Pair(t1, 80), Pair(t2, 0))
    }

    suspend fun playError() {
        val t1 = generateTone(293.66, 150, 0.6)
        val t2 = generateTone(261.63, 250, 0.6)
        playToneSequence(Pair(t1, 100), Pair(t2, 0))
    }

    suspend fun playWarning() {
        val t1 = generateTone(440.00, 200, 0.5)
        val t2 = generateTone(392.00, 150, 0.5)
        playToneSequence(Pair(t1, 100), Pair(t2, 0))
    }

    suspend fun playInfo() {
        val t1 = generateTone(659.25, 100, 0.5)
        val t2 = generateTone(783.99, 200, 0.5)
        playToneSequence(Pair(t1, 60), Pair(t2, 0))
    }
}
