package com.typ.nabda.core.actions

import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.core.model.GestureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RequestedActionMapperTest {

    @Test
    fun getActionForGesture_twoFingerSwipeUp_returnsFoodRequest() {
        val action = RequestedActionMapper.getActionForGesture(GestureType.TWO_FINGER_SWIPE_UP)
        assertNotNull(action)
        assertEquals("FOOD_REQUEST", action?.id)
        assertEquals(ActionPriority.NORMAL, action?.priority)
    }

    @Test
    fun getActionForGesture_threeFingerSwipeLeft_returnsEmergency() {
        val action = RequestedActionMapper.getActionForGesture(GestureType.THREE_FINGER_SWIPE_LEFT)
        assertNotNull(action)
        assertEquals("EMERGENCY_ALERT", action?.id)
        assertEquals(ActionPriority.EMERGENCY, action?.priority)
    }

    @Test
    fun getAllActions_returnsNotEmptyList() {
        val actions = RequestedActionMapper.getAllActions()
        assert(actions.isNotEmpty())
    }
}
