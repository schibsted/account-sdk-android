/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.OIDCScope
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.UserTokenResponse

/**
 * Task to request user credentials and signup with Schibsted account using these
 */
internal class LoginOperation(
        credentials: Credentials,
        @OIDCScope scopes: Array<String>,
        failure: (NetworkError) -> Unit,
        success: (UserTokenResponse) -> Unit
) {

    init {
        val callback = object : NetworkCallback<UserTokenResponse>("Identifying with username and password in LoginOperation") {
            override fun onError(error: NetworkError) = failure(error)
            override fun onSuccess(result: UserTokenResponse) = success(result)
        }

        ServiceHolder.oAuthService.tokenFromPassword(
                clientId = ClientConfiguration.get().clientId,
                clientSecret = ClientConfiguration.get().clientSecret,
                username = credentials.identifier.identifier,
                password = credentials.password,
                scopes = *scopes
        ).enqueue(callback)
    }
}
