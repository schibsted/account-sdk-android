/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.ProfileData
import java.net.URI

/**
 * Signs up a new user
 */
internal class SignUpOperation(
        email: String,
        redirectUri: URI,
        params: Map<String, Any>,
        failure: (NetworkError) -> Unit,
        success: (ProfileData) -> Unit
) {

    init {
        ClientTokenOperation(
                { failure(it) },
                {
                    val callback = object : NetworkCallback<ApiContainer<ProfileData>>("Signing up user") {
                        override fun onSuccess(result: ApiContainer<ProfileData>) = success(result.data)
                        override fun onError(error: NetworkError) = failure(error)
                    }

                    ServiceHolder.clientService.signUp(
                            bearerAuthHeader = "Bearer ${it.serializedAccessToken}",
                            email = email,
                            redirectUri = redirectUri.toString(),
                            inputParams = params
                    ).enqueue(callback)
                }
        )
    }
}
