package com.typ.nabda.core.haptic

import android.content.Context
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
                    .setUsage(VibrationAttributes.USAGE_NOTIFICATION)
                    .build()
                vibrator.vibrate(effect, attributes)
            } else {
                vibrator.vibrate(effect)
            }

            Log.d(TAG, "Performed haptic: ${pattern.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform haptic pattern", e)
        }
    }
}

