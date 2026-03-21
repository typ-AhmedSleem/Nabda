package com.typ.nabda.feature.caregiver.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.ui.graphics.Color
import com.typ.nabda.core.model.CaregiverAction
import com.typ.nabda.feature.caregiver.R
import com.typ.nabda.feature.caregiver.models.QuickActionItem

object CaregiverQuickActions {

    val allActions = listOf(
        QuickActionItem(
            action = CaregiverAction.FOOD_READY,
            titleResId = R.string.action_food_ready_title,
            descResId = R.string.action_food_ready_desc,
            icon = Icons.Default.Fastfood,
            iconBg = Color(0xFFFFE0B2),
            iconColor = Color(0xFFE65100)
        ),
        QuickActionItem(
            action = CaregiverAction.COME_CLOSER,
            titleResId = R.string.action_come_closer_title,
            descResId = R.string.action_come_closer_desc,
            icon = Icons.Default.SupervisorAccount,
            iconBg = Color(0xFFE3F2FD),
            iconColor = Color(0xFF1976D2)
        ),
        QuickActionItem(
            action = CaregiverAction.SLEEP_TIME,
            titleResId = R.string.action_im_coming_title,
            descResId = R.string.action_im_coming_desc,
            icon = Icons.Filled.Bedtime,
            iconBg = Color(0xFFE8F5E9),
            iconColor = Color(0xFF388E3C)
        ),
        QuickActionItem(
            action = CaregiverAction.DO_WANT_THIS,
            titleResId = R.string.action_do_you_want_this_title,
            descResId = R.string.action_do_you_want_this_desc,
            icon = Icons.AutoMirrored.Filled.Help,
            iconBg = Color(0xFFF3E5F5),
            iconColor = Color(0xFF7B1FA2)
        ),
        QuickActionItem(
            action = CaregiverAction.ARE_YOU_SICK,
            titleResId = R.string.action_are_you_sick_title,
            descResId = R.string.action_are_you_sick_desc,
            icon = Icons.Default.MedicalServices,
            iconBg = Color(0xFFFFEBEE),
            iconColor = Color(0xFFD32F2F)
        ),
        QuickActionItem(
            action = CaregiverAction.IM_COMING,
            titleResId = R.string.action_im_coming_title,
            descResId = R.string.action_im_coming_desc,
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            iconBg = Color(0xFFE8F5E9),
            iconColor = Color(0xFF388E3C)
        ),
    )

}