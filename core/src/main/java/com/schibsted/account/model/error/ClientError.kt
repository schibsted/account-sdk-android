/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model.error

import com.schibsted.account.common.util.Logger
import com.schibsted.account.model.error.ClientError.ErrorType

/**
 * A human readable error, produced by the Account SDK. The creation of these errors will be logged
 * when running a debug build. This can be overwritten by setting [Logger.loggingEnabled]
 * @param errorType The [ErrorType] of the error
 * @param message A human readable message of the error
 */
open class ClientError(val errorType: ErrorType, val message: String) {
    init {
        Logger.verbose(Logger.DEFAULT_TAG, { "IdentityError: ${this.errorType} ${this.message}" })
    }

    enum class ErrorType {
        INVALID_USER_CREDENTIALS,
        INVALID_CLIENT_CREDENTIALS,
        UNAUTHORIZED,
        FORBIDDEN,
        AGREEMENTS_NOT_ACCEPTED,
        SESSION_NOT_FOUND,

        ALREADY_REGISTERED,
        ACCOUNT_NOT_VERIFIED,
        INVALID_PHONE_NUMBER,
        INVALID_EMAIL,
        INVALID_GRANT,
        INVALID_CODE,
        MISSING_FIELDS,
        SIGNUP_FORBIDDEN,

        TOO_MANY_REQUESTS,
        CONNECTION_TIMED_OUT,
        UNKNOWN_SPID_ERROR,

        NETWORK_ERROR,
        GENERIC_ERROR,
        UNKNOWN_ERROR,

        INVALID_STATE,
        INVALID_INPUT,
        INVALID_DISPLAY_NAME
    }

    companion object {
        val USER_LOGGED_OUT_ERROR = ClientError(ClientError.ErrorType.INVALID_STATE, "User logged out, cannot continue")
    }
}
