/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.RequiredFieldsResponse
import com.schibsted.account.session.User

internal class MissingFieldsOperation(
        user: User,
        failure: (NetworkError) -> Unit,
        success: (Set<String>) -> Unit
) {

    init {
        val userToken = requireNotNull(user.token) { "Cannot get missing fields for logged out user" }
        val callback = object : NetworkCallback<ApiContainer<RequiredFieldsResponse>>("Fetching required fields") {
            override fun onSuccess(result: ApiContainer<RequiredFieldsResponse>) = success(result.data.fields)
            override fun onError(error: NetworkError) = failure(error)
        }

        user.userService.getMissingRequiredFields(
                userId = user.userId.id,
                bearerAuthHeader = "Bearer ${userToken.serializedAccessToken}"
        ).enqueue(callback)
    }
}
