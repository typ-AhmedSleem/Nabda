package com.typ.nabda.core.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.typ.nabda.core.model.HapticEnginePattern

/**
 * Android implementation of [HapticEngine] using the system [Vibrator].
 */
class AndroidHapticEngine(private val context: Context) : HapticEngine {

    private val vibrator: Vibrator by lazy {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }).also {
            Log.d("NABDA_HapticEngine", "Retrieved vibrator: hasVibrator=${it.hasVibrator()}, hasAmplitudeControl=${it.hasAmplitudeControl()}")
        }
    }

    override fun performHaptic(pattern: HapticEnginePattern) {
        Log.i("NABDA_HapticEngine", "Cancelled current vibrations.")
        val amplitudeControlAvailable = vibrator.hasAmplitudeControl()
        if (!vibrator.hasVibrator()) {
            Log.w("NABDA_HapticEngine", "No vibrator available")
            return
        }
        if (!amplitudeControlAvailable) {
            Log.w("NABDA_HapticEngine", "Amplitude control not available. Fallback to one-shot vibration.")
        }

        if (amplitudeControlAvailable) {
            val effect = VibrationEffect.createWaveform(
                pattern.durations,
                pattern.amplitudes,
                pattern.repeats
            )
            vibrator.vibrate(effect)
            Log.d("NABDA_HapticEngine", "Performed waveform haptic pattern: $pattern")
        } else {
            val onShotPattern = pattern.durations
                .zip(pattern.amplitudes.map { it.coerceIn(1, 255).toLong() })

            onShotPattern.forEach { (dur, amp) ->
                VibrationEffect.createOneShot(
                    dur,
                    amp.toInt()
                )
            }
            Log.d("NABDA_HapticEngine", "Performed ${onShotPattern.size}-rounds oneshot haptic pattern: '$pattern'.")
        }
    }
}
