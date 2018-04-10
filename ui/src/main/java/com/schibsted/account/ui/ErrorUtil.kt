/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.support.annotation.StringRes
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.login.screen.LoginScreen

/**
 * Util class used to provide error related resources depending on [ClientError.ErrorType]
 */
object ErrorUtil {
    /**
     * Provides the string resource matching with the error type
     *
     * @param errorType the type of error
     * @return a string resource to display as error
     */
    @StringRes
    fun getErrorMessageRes(errorType: ClientError.ErrorType, loginScreen: LoginScreen): Int {
        return when (errorType) {
            ClientError.ErrorType.ALREADY_REGISTERED,
            ClientError.ErrorType.UNKNOWN_SPID_ERROR,
            ClientError.ErrorType.UNKNOWN_ERROR,
            ClientError.ErrorType.NETWORK_ERROR -> R.string.schacc_generic_server_error_message
            ClientError.ErrorType.CONNECTION_TIMED_OUT -> R.string.schacc_generic_time_out_error_message
            ClientError.ErrorType.TOO_MANY_REQUESTS -> {
                when (loginScreen) {
                    LoginScreen.PASSWORD_SCREEN, LoginScreen.VERIFICATION_SCREEN ->
                        R.string.schacc_generic_too_many_login_request_error_message
                    LoginScreen.CHECK_INBOX_SCREEN -> R.string.schacc_generic_too_many_inbox_request_error_message
                    else -> R.string.schacc_generic_too_many_code_request_error_message
                }
            }
            else -> R.string.schacc_generic_internet_error
        }
    }

    fun isServerError(errorType: ClientError.ErrorType): Boolean {
        return (errorType == ClientError.ErrorType.UNKNOWN_SPID_ERROR
                || errorType == ClientError.ErrorType.NETWORK_ERROR
                || errorType == ClientError.ErrorType.CONNECTION_TIMED_OUT
                || errorType == ClientError.ErrorType.GENERIC_ERROR
                || errorType == ClientError.ErrorType.UNAUTHORIZED
                || errorType == ClientError.ErrorType.INVALID_CLIENT_CREDENTIALS
                || errorType == ClientError.ErrorType.FORBIDDEN
                || errorType == ClientError.ErrorType.UNKNOWN_ERROR)
                || errorType == ClientError.ErrorType.ALREADY_REGISTERED
    }
}
