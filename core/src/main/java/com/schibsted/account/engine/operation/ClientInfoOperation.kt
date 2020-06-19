/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.ClientInfo

/**
 * A task to get client credentials for a Schibsted account client
 */
class ClientInfoOperation(
        failure: (error: NetworkError) -> Unit,
        success: (clientInfo: ClientInfo) -> Unit
) {

    init {
        ClientTokenOperation(
                { failure(it) },
                {
                    val callback = object : NetworkCallback<ApiContainer<ClientInfo>>("Retrieving client information") {
                        override fun onSuccess(result: ApiContainer<ClientInfo>) = success(result.data)
                        override fun onError(error: NetworkError) = failure(error)
                    }

                    ServiceHolder.clientService.getClientInfo(
                            bearerAuthHeader = "Bearer ${it.serializedAccessToken}",
                            clientId = ClientConfiguration.get().clientId
                    ).enqueue(callback)
                }
        )
    }
}
