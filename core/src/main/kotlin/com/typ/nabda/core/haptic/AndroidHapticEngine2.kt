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
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun performHaptic(pattern: HapticEnginePattern) {
        if (!vibrator.hasVibrator()) {
            Log.w(TAG, "No vibrator available on this device.")
            return
        }

        if (pattern.durations.isEmpty() || pattern.durations.size != pattern.amplitudes.size) {
            Log.e(TAG, "Invalid haptic pattern: size mismatch or empty arrays.")
            return
        }

        try {
            // Note: We removed vibrator.cancel() to prevent killing patterns that are 
            // triggered in rapid succession.

            // Sanitize amplitudes if device doesn't support variable intensity
            val finalAmplitudes = if (vibrator.hasAmplitudeControl()) {
                pattern.amplitudes
            } else {
                pattern.amplitudes.map { if (it > 0) 255 else 0 }.toIntArray()
            }

            val effect = VibrationEffect.createWaveform(
                pattern.durations,
                finalAmplitudes,
                pattern.repeats
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val attributes = VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_COMMUNICATION_REQUEST)
                    .setFlags(VibrationAttributes.FLAG_BYPASS_INTERRUPTION_POLICY, VibrationAttributes.FLAG_BYPASS_INTERRUPTION_POLICY)
                    .build()
                vibrator.vibrate(effect, attributes)
            } else {
                @Suppress("DEPRECATION")
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()
                vibrator.vibrate(effect, audioAttributes)
            }

            Log.d(TAG, "Performed haptic: ${pattern.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform haptic pattern", e)
        }
    }
}
