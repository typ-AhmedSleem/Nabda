package com.typ.nabda.core.model

/**
 * Defines all actions/alerts supported by the caregiver app.
 * Used for sending actions to deafblind app.
 */
enum class CaregiverAction(
    val id: String,
    val titleResName: String,
    val iconResName: Int,
) {
    FOOD_READY(
        id = "food_ready",
        titleResName = "action_food_ready_title",
        iconResName = android.R.drawable.ic_menu_today // Placeholder
    ),
    COME_CLOSER(
        id = "come_closer",
        titleResName = "action_come_closer_title",
        iconResName = android.R.drawable.ic_menu_mylocation // Placeholder
    ),
    ARE_YOU_SICK(
        id = "are_you_sick",
        titleResName = "action_are_you_sick_title",
        iconResName = android.R.drawable.ic_menu_call // Placeholder
    ),
    DO_WANT_THIS(
        id = "do_want_this",
        titleResName = "action_do_you_want_this_title",
        iconResName = android.R.drawable.ic_menu_help // Placeholder
    ),
    IM_COMING(
        id = "im_coming",
        titleResName = "action_im_coming_title",
        iconResName = android.R.drawable.ic_menu_directions // Placeholder
    ),
    HELP_REQUEST(
        id = "help_request",
        titleResName = "action_help_title",
        iconResName = android.R.drawable.ic_menu_help
    ),
    FALL(
        id = "fall",
        titleResName = "action_fall_title",
        iconResName = android.R.drawable.ic_menu_report_image
    );

    companion object {
        fun fromId(id: String): CaregiverAction? {
            return entries.find { it.id == id }
        }
    }
}
