/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.VerificationCode
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.OIDCScope
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.PasswordlessToken
import com.schibsted.account.network.response.UserTokenResponse

/**
 * Task to request user credentials and signup with Schibsted account using these
 */
internal class VerifyCodeOperation(
        identifier: Identifier,
        passwordlessToken: PasswordlessToken,
        verificationCode: VerificationCode,
        @OIDCScope scopes: Array<String>,
        failure: (NetworkError) -> Unit,
        success: (UserTokenResponse) -> Unit
) {

    init {
        val callback = object : NetworkCallback<UserTokenResponse>("Validating passwordless token") {
            override fun onError(error: NetworkError) = failure(error)
            override fun onSuccess(result: UserTokenResponse) = success(result)
        }

        ServiceHolder.oAuthService.tokenFromPasswordless(
                ClientConfiguration.get().clientId,
                ClientConfiguration.get().clientSecret,
                identifier.identifier,
                verificationCode.verificationCode,
                passwordlessToken.value,
                *scopes
        ).enqueue(callback)
    }
}
