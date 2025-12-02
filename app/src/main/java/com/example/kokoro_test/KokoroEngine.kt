package com.example.kokoro_test

import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.k2fsa.sherpa.onnx.*

class KokoroEngine(private val assetManager: AssetManager) {
    private val tag = "KokoroEngine"
    private val tts: OfflineTts

    init {
        Log.i(tag, "Initializing Kokoro TTS engine...")

        // 1. Log assets, we saw from your last log this is all good.
        logAssets(assetManager)

        // 2. Build Kokoro-only model config
        val kokoro = OfflineTtsKokoroModelConfig(
            model = "kokoro-en-v0_19/model.onnx",
            voices = "kokoro-en-v0_19/voices.bin",
            // tokens is optional for Kokoro; can stay empty
            tokens = "",
            lexicon = "",
            dataDir = "kokoro-en-v0_19/espeak-ng-data",
            lengthScale = 1.0f,
            lang = "en"
        )

        // 3. Build the model config, explicitly using only kokoro
        val modelConfig = OfflineTtsModelConfig(
            // leave all others at default
            kokoro = kokoro,
            numThreads = 2,
            debug = true,
            provider = "cpu"
        )

        val config = OfflineTtsConfig(
            model = modelConfig,
            maxNumSentences = 1
        )

        Log.i(tag, "Final TTS config: $config")

        // 4. Create the TTS engine
        try {
            tts = OfflineTts(
                assetManager = assetManager,
                config = config
            )
            Log.i(tag, "Kokoro TTS engine initialized successfully")
            Log.i(tag, "Sample rate: ${tts.sampleRate()}")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize TTS engine", e)
            throw e
        }
    }

    private fun logAssets(assetManager: AssetManager) {
        try {
            Log.i(tag, "=== Verifying asset files ===")

            val root = assetManager.list("") ?: emptyArray()
            Log.i(tag, "Root assets: ${root.joinToString()}")

            val kokoro = assetManager.list("kokoro-en-v0_19") ?: emptyArray()
            Log.i(tag, "Files in kokoro-en-v0_19/: ${kokoro.joinToString()}")

            val espeak = assetManager.list("kokoro-en-v0_19/espeak-ng-data") ?: emptyArray()
            Log.i(tag, "Files in espeak-ng-data/: ${espeak.joinToString()}")

            val requiredFiles = listOf(
                "kokoro-en-v0_19/model.onnx",
                "kokoro-en-v0_19/voices.bin"
            )

            for (file in requiredFiles) {
                try {
                    assetManager.open(file).use {
                        Log.i(tag, "✓ Found: $file")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "✗ Missing: $file", e)
                }
            }

            Log.i(tag, "=== End asset verification ===")

        } catch (e: Exception) {
            Log.e(tag, "Error verifying assets", e)
        }
    }

    fun speak(text: String) {
        try {
            Log.i(tag, "Generating audio for: $text")

            val audio = tts.generate(
                text = text,
                sid = 0,
                speed = 1.0f
            )

            val samples = audio.samples
            val sampleRate = tts.sampleRate()

            Log.i(tag, "Generated ${samples.size} samples at $sampleRate Hz")

            playAudio(samples, sampleRate)

        } catch (e: Exception) {
            Log.e(tag, "Error during TTS: ${e.message}", e)
        }
    }

    private fun playAudio(samples: FloatArray, sampleRate: Int) {
        try {
            val pcmData = ShortArray(samples.size)
            for (i in samples.indices) {
                val sample = (samples[i] * 32767).toInt().coerceIn(-32768, 32767)
                pcmData[i] = sample.toShort()
            }

            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack.play()
            audioTrack.write(pcmData, 0, pcmData.size)
            audioTrack.stop()
            audioTrack.release()

            Log.i(tag, "Audio playback completed")

        } catch (e: Exception) {
            Log.e(tag, "Error playing audio: ${e.message}", e)
        }
    }

    fun close() {
        try {
            tts.release()
            Log.i(tag, "Kokoro TTS engine released")
        } catch (e: Exception) {
            Log.e(tag, "Error releasing TTS: ${e.message}", e)
        }
    }
}