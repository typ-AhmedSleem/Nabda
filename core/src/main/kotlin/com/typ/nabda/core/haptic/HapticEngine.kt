package com.typ.nabda.core.haptic

import com.typ.nabda.core.model.HapticEnginePattern

/**
 * Interface for performing haptic feedback using [HapticEnginePattern].
 */
interface HapticEngine {
    fun performHaptic(pattern: HapticEnginePattern)
}
