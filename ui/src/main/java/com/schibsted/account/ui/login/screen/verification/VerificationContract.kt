/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.verification

import com.schibsted.account.engine.controller.PasswordlessController
import com.schibsted.account.ui.ui.FlowView
import com.schibsted.account.ui.ui.component.CodeInputView

/**
 * Following the MVP design pattern this interface represent the contract for the view and the presenter responsible
 * for the code verification feature
 *
 * @see com.schibsted.account.ui.login.screen.verification
 */
interface VerificationContract {

    /**
     * defines methods implemented by views related to verification process
     */
    interface View : FlowView<Presenter> {

        /**
         * Shows a pop-up to information the user the code was resent
         */
        fun showResendCodeView()
    }

    /**
     * defines methods implemented by presenters performing verification business
     *
     * @see VerificationFragment
     */
    interface Presenter {
        /**
         * Ask the server side to perform another code sending
         */
        fun resendCode(passwordlessController: PasswordlessController)

        /**
         * Verify the code filled in by the user
         */
        fun verifyCode(codeInputView: CodeInputView, keepMeLoggedIn: Boolean)
    }
}
