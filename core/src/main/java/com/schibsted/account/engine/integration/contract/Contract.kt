/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.integration.contract

import com.schibsted.account.engine.integration.CallbackProvider

interface Contract<in T> {
    /**
     * Called when the flow is ready to be completed. This is the last step of a flow and will
     * return the result of the flow in the provided callback
     */
    fun onFlowReady(callbackProvider: CallbackProvider<T>)
}
