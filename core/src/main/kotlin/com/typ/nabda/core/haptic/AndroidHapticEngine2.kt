package com.typ.nabda.core.haptic

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.typ.nabda.core.model.HapticEnginePattern

/**
 * Android implementation of [HapticEngine] using the system [Vibrator].
 */
class AndroidHapticEngine2(private val context: Context) : HapticEngine {

    private companion object {
        private const val TAG = "NABDA_HapticEngine"
    }

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.vibratorIds.also {
                Log.i(TAG, "Available vibrator ids: ${it.contentToString()}")
            }
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun performHaptic(pattern: HapticEnginePattern) {
        // 1. Early exit if no hardware available
        if (!vibrator.hasVibrator()) {
            Log.w(TAG, "No vibrator available on this device.")
            return
        }

        // 2. Validate pattern consistency
        if (pattern.durations.isEmpty() || pattern.durations.size != pattern.amplitudes.size) {
            Log.e(TAG, "Invalid haptic pattern: size mismatch or empty arrays.")
            return
        }

        try {
            // 3. Cancel existing and prepare new effect
            vibrator.cancel()

            // createWaveform works on all devices (API 26+).
            // Devices without amplitude control automatically fallback to default strength.
            val effect = VibrationEffect.createWaveform(
                pattern.durations,
                pattern.amplitudes,
                pattern.repeats
            )

            // 4. Use attributes to ensure correct delivery priority
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val attributes = VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_ALARM)
                    .build()
                vibrator.vibrate(effect, attributes)
            } else {
                // API 26 to 32 (Legacy attributes mapping)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                vibrator.vibrate(effect, audioAttributes)
            }

            Log.d(TAG, "Performed haptic: ${pattern.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform haptic pattern", e)
        }
    }
}

