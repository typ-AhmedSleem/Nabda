package com.typ.nabda.core.model

sealed class HapticEnginePattern(
    val amplitudes: LongArray,
    val durations: LongArray,
    val repeats: Int = 1,
) {
    data object NotConnected : HapticEnginePattern(
        amplitudes = longArrayOf(0, 150, 0, 150),
        durations = longArrayOf(100, 200, 100, 200)
    )

    data object ConfirmActionAgain : HapticEnginePattern(
        amplitudes = longArrayOf(0, 200),
        durations = longArrayOf(100, 300)
    )

    data object ActionSent : HapticEnginePattern(
        amplitudes = longArrayOf(0, 255, 0, 255),
        durations = longArrayOf(50, 100, 50, 100)
    )

    data object ActionNotConfirmed : HapticEnginePattern(
        amplitudes = longArrayOf(0, 100, 0, 100, 0, 100),
        durations = longArrayOf(50, 150, 50, 150, 50, 150)
    )

    data object HelpRequest : HapticEnginePattern(
        amplitudes = longArrayOf(0, 200, 0, 200, 0, 200),
        durations = longArrayOf(100, 200, 100, 200, 100, 200)
    )

    data object FallAlert : HapticEnginePattern(
        amplitudes = longArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
        durations = longArrayOf(50, 300, 50, 300, 50, 300, 50, 300)
    )
}
