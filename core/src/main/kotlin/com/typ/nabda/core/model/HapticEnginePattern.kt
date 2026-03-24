package com.typ.nabda.core.model

import kotlinx.serialization.Serializable

@Serializable
sealed class HapticEnginePattern(
    val amplitudes: IntArray,
    val durations: LongArray,
    val repeats: Int = -1,
) {
    @Serializable
    data class Custom(
        val customAmplitudes: IntArray,
        val customDurations: LongArray,
    ) : HapticEnginePattern(customAmplitudes, customDurations) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Custom

            if (!customAmplitudes.contentEquals(other.customAmplitudes)) return false
            if (!customDurations.contentEquals(other.customDurations)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = customAmplitudes.contentHashCode()
            result = 31 * result + customDurations.contentHashCode()
            return result
        }
    }

    @Serializable
    data object NotConnected : HapticEnginePattern(
        amplitudes = intArrayOf(0, 150, 0, 150),
        durations = longArrayOf(100, 200, 100, 200)
    )

    @Serializable
    data object ConfirmActionAgain : HapticEnginePattern(
        amplitudes = intArrayOf(0, 200),
        durations = longArrayOf(100, 300)
    )

    @Serializable
    data object ActionSent : HapticEnginePattern(
        amplitudes = intArrayOf(0, 255, 0, 255),
        durations = longArrayOf(50, 100, 50, 100)
    )

    @Serializable
    data object ActionNotConfirmed : HapticEnginePattern(
        amplitudes = intArrayOf(0, 100, 0, 100, 0, 100),
        durations = longArrayOf(50, 150, 50, 150, 50, 150)
    )

    @Serializable
    data object NormalRequest : HapticEnginePattern(
        amplitudes = intArrayOf(0, 150),
        durations = longArrayOf(100, 200)
    )

    @Serializable
    data object AssistanceRequest : HapticEnginePattern(
        amplitudes = intArrayOf(0, 200, 0, 200, 0, 200),
        durations = longArrayOf(100, 200, 100, 200, 100, 200)
    )

    @Serializable
    data object EmergencyRequest : HapticEnginePattern(
        amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
        durations = longArrayOf(50, 300, 50, 300, 50, 300, 50, 300)
    )

}
