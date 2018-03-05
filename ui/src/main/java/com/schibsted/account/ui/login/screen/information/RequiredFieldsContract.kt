/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.information

import com.schibsted.account.ui.ui.FlowView
import com.schibsted.account.ui.ui.InputField

interface RequiredFieldsContract {
    interface View : FlowView<Presenter> {
        fun hideErrors()
    }

    interface Presenter {
        fun updateMissingFields(fields: Map<String, InputField>)
    }
}
