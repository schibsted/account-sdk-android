/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.model.ClientToken
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.TokenResponse

/**
 * A task to get client credentials for a Schibsted account client
 */
internal class ClientTokenOperation internal constructor(
    private val failure: (error: NetworkError) -> Unit,
    private val success: (token: ClientToken) -> Unit
) {

    init {
        ServiceHolder.oAuthService.tokenFromClientCredentials(ClientConfiguration.get().clientId, ClientConfiguration.get().clientSecret)
                .enqueue(object : NetworkCallback<TokenResponse>("Initializing client session") {
                    override fun onError(error: NetworkError) {
                        failure(error)
                    }

                    override fun onSuccess(result: TokenResponse) {
                        success(result)
                    }
                })
    }
}
