/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.ClientTokenResponse

/**
 * A task to get client credentials for a Schibsted account client
 */
internal class ClientTokenOperation(
        failure: (error: NetworkError) -> Unit,
        success: (clientTokenResponse: ClientTokenResponse) -> Unit
) {

    init {
        val callback = object : NetworkCallback<ClientTokenResponse>("Initializing client session") {
            override fun onError(error: NetworkError) = failure(error)
            override fun onSuccess(result: ClientTokenResponse) = success(result)
        }

        ServiceHolder.oAuthService.tokenFromClientCredentials(
                clientId = ClientConfiguration.get().clientId,
                clientSecret = ClientConfiguration.get().clientSecret
        ).enqueue(callback)
    }
}
