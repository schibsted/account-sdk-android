/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.PasswordlessToken

/**
 * Task to request user credentials and signup with Schibsted account using these
 */
internal class ResendCodeOperation(
        passwordlessToken: PasswordlessToken,
        failure: (NetworkError) -> Unit,
        success: (PasswordlessToken) -> Unit
) {

    init {
        val callback = object : NetworkCallback<PasswordlessToken>("Resending confirmation code") {
            override fun onError(error: NetworkError) = failure(error)
            override fun onSuccess(result: PasswordlessToken) = success(result)
        }

        ServiceHolder.passwordlessService.resendCode(
                ClientConfiguration.get().clientId,
                passwordlessToken
        ).enqueue(callback)
    }
}
