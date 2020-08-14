/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.password

import androidx.annotation.StringRes
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.ui.ErrorField
import com.schibsted.account.ui.ui.FlowView
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.util.KeyValueStore

interface PasswordContract {

    interface View : FlowView<Presenter> {
        /**
         * Shows a contextual error related to the user's actions
         */
        fun showError(errorField: ErrorField, @StringRes errorMes: Int) {
            errorField.setError(errorMes)
            errorField.showErrorView()
        }
    }

    interface Presenter {
        fun sign(inputField: InputField, identifier: Identifier?, keepUserLoggedIn: Boolean, keyValueStore: KeyValueStore?)
    }
}
