/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.tracking

import com.schibsted.account.common.tracking.TrackingData.Engagement
import com.schibsted.account.common.tracking.TrackingData.FlowVariant
import com.schibsted.account.common.tracking.TrackingData.InteractionType
import com.schibsted.account.common.tracking.TrackingData.SpidAction
import com.schibsted.account.common.tracking.TrackingData.UIElement
import com.schibsted.account.common.tracking.TrackingData.UIError
import com.schibsted.account.common.tracking.TrackingData.UserIntent

abstract class UiTracking {
    var flowVariant: FlowVariant? = null
    var intent: UserIntent? = null

    fun resetContext() {
        this.flowVariant = null
        this.intent = null
    }

    abstract fun eventInteraction(interactionType: InteractionType, screen: TrackingData.Screen, custom: Map<String, Any> = mapOf())
    fun eventInteraction(interactionType: InteractionType, screen: TrackingData.Screen) {
        eventInteraction(interactionType, screen)
    }

    abstract fun eventEngagement(engagement: Engagement, uiElement: UIElement, source: TrackingData.Screen?, custom: Map<String, Any> = mapOf())
    fun eventEngagement(engagement: Engagement, uiElement: UIElement, source: TrackingData.Screen?) {
        eventEngagement(engagement, uiElement, source)
    }

    abstract fun eventError(error: UIError, source: TrackingData.Screen?, custom: Map<String, Any> = mapOf())
    fun eventError(error: UIError, source: TrackingData.Screen?) {
        eventError(error, source)
    }

    abstract fun eventActionSuccessful(spidAction: SpidAction, accountId: String? = null, custom: Map<String, Any> = mapOf())
    fun eventActionSuccessful(spidAction: SpidAction) {
        eventActionSuccessful(spidAction)
    }
}
