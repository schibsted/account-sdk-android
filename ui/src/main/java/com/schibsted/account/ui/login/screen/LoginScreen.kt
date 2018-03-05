/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen

enum class LoginScreen constructor(val value: String, val canBeEnded: Boolean = false) {
    /**
     * Represents the screen used to ask the phone number or the email address
     *
     * @see com.schibsted.account.ui.passwordless.identification.ui.AbstractIdentificationFragment
     *
     * @see com.schibsted.account.ui.passwordless.identification.ui.EmailIdentificationFragment
     *
     * @see com.schibsted.account.ui.passwordless.identification.ui.MobileIdentificationFragment
     */
    IDENTIFICATION_SCREEN("IDENTIFICATION_SCREEN", true),

    /**
     * Represents the screen used to perform the code verification
     *
     * @see VerificationFragment
     */
    VERIFICATION_SCREEN("VERIFICATION_SCREEN"),
    /**
     * Represents the screen used to perform the code verification
     *
     * @see com.schibsted.account.ui.passwordless.verification.views.VerificationFragment
     */
    PASSWORD_SCREEN("PASSWORD_SCREEN"),

    /**
     * Represents the screen used to show and accept terms and conditions
     *
     * @see com.schibsted.account.ui.passwordless.terms.TermsFragment
     */
    TC_SCREEN("TC_SCREEN"),

    /**
     * Represents the screen used to ask required information to the user
     *
     * @see com.schibsted.account.ui.passwordless.information.RequiredFieldsFragment
     */
    REQUIRED_FIELDS_SCREEN("REQUIRED_FIELDS_SCREEN", true),

    CHECK_INBOX_SCREEN("CHECK_INBOX_SCREEN", true),

    WEB_TC_SCREEN("WEB_TC_SCREEN"),

    WEB_NEED_HELP_SCREEN("WEB_NEED_HELP_SCREEN"),

    WEB_FORGOT_PASSWORD_SCREEN("WEB_FORGOT_PASSWORD_SCREEN");

    companion object {
        @JvmStatic
        fun isWebView(string: String?): Boolean {
            return when (string) {
                WEB_TC_SCREEN.value,
                WEB_NEED_HELP_SCREEN.value,
                WEB_FORGOT_PASSWORD_SCREEN.value -> true
                else -> false
            }
        }
    }
}
