/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.onesteplogin

import android.arch.lifecycle.LifecycleOwner
import android.support.annotation.StringRes
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.ui.ErrorField
import com.schibsted.account.ui.ui.FlowView
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.util.KeyValueStore

interface OneStepLoginContract {

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
        /**
         * Use this method to verify the input provided by the view.
         * This method should request a navigation to the next step in case of success
         * and should ask the view to show an error if any failure occurs
         *
         * @param identifier the identifier use to signup the user, it could be a phone number or
         * an email address
         * @param identifierType the type of verification to perform, this is used to make a call to
         * the server side
         * @param allowSignup a flag used to know if the signup option is allowed
         * @param signUpErrorMessage an optional error message to show if the user want to signup but it's not allowed
         */
        fun verifyInput(identifier: InputField, identifierType: Identifier.IdentifierType, allowSignup: Boolean, signUpErrorMessage: String?)

        fun getAccountStatus(input: InputField, allowSignUp: Boolean, signUpErrorMessage: String?)
        fun startSignin()
        fun signIn(identifier: InputField, credInputField: InputField, keepUserLoggedIn: Boolean, lifecycleOwner: LifecycleOwner, keyValueStore: KeyValueStore?)
        fun startSignup()
        fun signup(identifier: InputField, credInputField: InputField, keepUserLoggedIn: Boolean, lifecycleOwner: LifecycleOwner)
    }
}
