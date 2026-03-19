package com.typ.nabda.core.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.typ.nabda.core.model.HapticEnginePattern

/**
 * Android implementation of [HapticEngine] using the system [Vibrator].
 */
class AndroidHapticEngine(private val context: Context) : HapticEngine {

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
        if (!vibrator.hasVibrator()) return

        val effect = VibrationEffect.createWaveform(
            pattern.durations,
            pattern.amplitudes,
            -1 // No repeat
        )

        // Handle repeats if specified in pattern (though createWaveform repeat index is for looping)
        // For simple patterns we just play it once as per the pattern definition.
        vibrator.vibrate(effect)
    }
}
