/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.common.tracking

@Suppress("unused")
/**
 * This contains all constants and data types used for tracking. Please see
 * [https://pulse.schibsted.io/docs/tagging_plans%2Fidentity.md] for details
 */
object TrackingData {

    /**
     * Describes a screen
     */
    enum class Screen {
        IDENTIFICATION, PASSWORD, VERIFICATION_CODE, AGREEMENTS, REQUIRED_FIELDS, ACCOUNT_VERIFICATION;
    }

    /**
     * Describes a UI element
     */
    enum class UIElement {
        HELP, CHANGE_IDENTIFIER, AGREEMENTS_SUMMARY, AGREEMENTS_SPID, AGREEMENTS_CLIENT, PRIVACY_SPID, PRIVACY_CLIENT,
        RESEND_VERIFICATION_CODE, FORGOT_PASSWORD
    }

    /**
     * Describes an interaction with a screen
     */
    enum class InteractionType(val value: String) {
        VIEW("View"), CLOSE("Close"), SEND("Send");

        override fun toString(): String = this.value
    }

    /**
     * Describes a user engagement
     */
    enum class Engagement(val value: String) {
        CLICK("Click");

        override fun toString() = value
    }

    enum class IdentifierType(val value: String) {
        EMAIL("Email"), PHONE("Phone");

        override fun toString() = value
    }

    /**
     * What the user is trying to perform
     */
    enum class UserIntent(val value: String) {
        LOGIN("Login"), CREATE("Create");

        override fun toString() = value
    }

    /**
     * The variant of the login flow
     */
    enum class FlowVariant(val value: String) {
        PASSWORDLESS_CODE("PasswordlessCode"), PASSWORDLESS_LINK("PasswordlessLink"), PASSWORD("Password");

        override fun toString() = value
    }

    /**
     * Describes a UI error
     */
    sealed class UIError(val errorType: ErrorType, val cause: String) {
        enum class ErrorType(val value: String) {
            VALIDATION("Validation error"), NETWORK("Network error"), GENERIC("Generic error");

            override fun toString() = value
        }

        object InvalidCredentials : UIError(ErrorType.GENERIC, "Invalid user credentials")
        object InvalidVerificationCode : UIError(ErrorType.GENERIC, "The verification code is incorrect")
        object NetworkError : UIError(ErrorType.NETWORK, "A network error occurred")
        class MissingRequiredField(message: String) : UIError(ErrorType.VALIDATION, "Missing required fields. $message")
        object AgreementsNotAccepted : UIError(ErrorType.VALIDATION, "All agreements must be accepted")
        object InvalidPhone : UIError(ErrorType.VALIDATION, "The provided phone number is not valid")
        object InvalidEmail : UIError(ErrorType.VALIDATION, "The provided email is not valid")
        object InvalidPassword : UIError(ErrorType.VALIDATION, "The provided password is not valid")
        class InvalidRequiredField(field: String) : UIError(ErrorType.VALIDATION, "Required field has invalid format: $field")

        override fun toString() = cause
    }

    /**
     * Global actions tracked
     */
    enum class SpidAction {
        LOGIN_COMPLETED, ACCOUNT_CREATED, VERIFICATION_CODE_SENT, AGREEMENTS_ACCEPTED, REQUIRED_FIELDS_PROVIDED, ACCOUNT_VERIFIED
    }
}
