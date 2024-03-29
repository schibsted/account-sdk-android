/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.flow.password

import com.schibsted.account.engine.input.Identifier

interface FlowSelectionListener {
    enum class FlowType {
        LOGIN, SIGN_UP, ONE_STEP_LOGIN, ONE_STEP_SIGNUP
    }

    fun onFlowSelected(flowType: FlowType, identifier: Identifier)
    fun onFlowSelected(flowType: FlowType)
}
