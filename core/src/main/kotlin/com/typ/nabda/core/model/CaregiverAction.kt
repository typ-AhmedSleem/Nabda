package com.typ.nabda.core.model

/**
 * Defines all actions/alerts supported by the caregiver app.
 * Used for sending actions to deafblind app.
 */
enum class CaregiverAction {
    FOOD_READY,
    COME_CLOSER,
    SLEEP_TIME,
    ARE_YOU_SICK,
    DO_WANT_THIS,
    IM_COMING,

    // todo: remove actions below this
    HELP_REQUEST,
    FALL,
}
