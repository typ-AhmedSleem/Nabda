package com.typ.nabda.core.model

/**
 * Defines all actions/alerts supported by the application.
 * Used for filtering and UI display in the History screen.
 */
enum class SupportedAction(val id: String, val titleResName: String) {
    HELP_REQUEST("help_request", "action_help_title"),
    FALL("fall", "action_fall_title");

    companion object {
        fun fromId(id: String): SupportedAction? {
            return entries.find { it.id == id }
        }
    }
}
