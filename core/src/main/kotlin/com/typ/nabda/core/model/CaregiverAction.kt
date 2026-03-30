package com.typ.nabda.core.model

import androidx.annotation.StringRes
import com.typ.nabda.core.R

/**
 * Defines all actions/alerts supported by the caregiver app.
 * Used for sending actions to deafblind app.
 */
enum class CaregiverAction(@StringRes val nameResId: Int) {
    FOOD_READY(R.string.action_food_ready),
    COME_CLOSER(R.string.action_come_closer),
    SLEEP_TIME(R.string.action_sleep_time),
    ARE_YOU_SICK(R.string.action_are_you_sick),
    DO_WANT_THIS(R.string.action_do_want_this),
    IM_COMING(R.string.action_im_coming),

    // todo: remove actions below this
    HELP_REQUEST(R.string.action_help_request),
    FALL(R.string.action_fall_detected),
}
