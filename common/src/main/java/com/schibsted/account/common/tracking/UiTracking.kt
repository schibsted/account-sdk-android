/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.tracking

import com.schibsted.account.common.tracking.TrackingData.Engagement
import com.schibsted.account.common.tracking.TrackingData.FlowVariant
import com.schibsted.account.common.tracking.TrackingData.InteractionType
import com.schibsted.account.common.tracking.TrackingData.UIElement
import com.schibsted.account.common.tracking.TrackingData.UIError
import com.schibsted.account.common.tracking.TrackingData.UserIntent
import kotlin.properties.Delegates

abstract class UiTracking {
    var flowVariant: FlowVariant? = null
    var intent: UserIntent? = null

    var clientId: String? = null
    var loginRealm: String? = null
    var merchantId: Int? = null

    var userId by Delegates.observable<String?>(null) { _, _, newValue ->
        onUserIdChanged?.invoke(newValue)
    }

    var onUserIdChanged: ((String?) -> Unit)? = null

    fun resetContext() {
        this.flowVariant = null
        this.intent = null
    }

    abstract fun eventInteraction(interactionType: InteractionType, screen: TrackingData.Screen, custom: Map<String, Any> = mapOf())

    abstract fun eventEngagement(engagement: Engagement, uiElement: UIElement, source: TrackingData.Screen?, custom: Map<String, Any> = mapOf())

    abstract fun eventError(error: UIError, source: TrackingData.Screen?, custom: Map<String, Any> = mapOf())

    fun eventInteraction(interactionType: InteractionType, screen: TrackingData.Screen) {
        eventInteraction(interactionType, screen, mapOf())
    }

    fun eventEngagement(engagement: Engagement, uiElement: UIElement, source: TrackingData.Screen?) {
        eventEngagement(engagement, uiElement, source, mapOf())
    }

    fun eventError(error: UIError, source: TrackingData.Screen?) {
        eventError(error, source, mapOf())
    }

    protected fun setTrackingIdentifier(id: String?) {
        trackingIdentifier = id
    }

    companion object {
        var trackingIdentifier: String? = null
            private set
    }
}
