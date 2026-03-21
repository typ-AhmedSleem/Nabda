package com.typ.nabda.feature.caregiver.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.typ.nabda.core.model.CaregiverAction

@Immutable
data class QuickActionItem(
    val action: CaregiverAction,
    val titleResId: Int,
    val descResId: Int,
    val icon: ImageVector,
    val iconBg: Color,
    val iconColor: Color,
)