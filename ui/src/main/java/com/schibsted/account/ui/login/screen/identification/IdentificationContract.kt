/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification

import com.schibsted.account.engine.input.Identifier.IdentifierType
import com.schibsted.account.ui.ui.FlowView
import com.schibsted.account.ui.ui.InputField

/**
 * Following the MVP design pattern this interface represent the contract for the view and the presenter responsible for
 * the identification feature.
 *
 * @see com.schibsted.account.ui.login.screen.identification
 */
interface IdentificationContract {

    /**
     * Defines methods implemented by the presenter performing identification business
     *
     * @see com.schibsted.account.ui.login.screen.identification.ui.MobileIdentificationFragment
     *
     * @see com.schibsted.account.ui.login.screen.identification.ui.EmailIdentificationFragment
     */
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
        fun verifyInput(identifier: InputField, identifierType: IdentifierType, allowSignup: Boolean, signUpErrorMessage: String?)

        fun getAccountStatus(input: InputField, allowSignUp: Boolean, signUpErrorMessage: String?)
    }

    /**
     * defines methods implemented by views related to identification process
     */
    interface View : FlowView<Presenter>
}
