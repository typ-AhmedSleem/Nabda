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
