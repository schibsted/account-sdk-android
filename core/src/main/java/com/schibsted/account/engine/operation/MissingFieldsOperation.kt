/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.RequiredFieldsResponse
import com.schibsted.account.session.User

internal class MissingFieldsOperation(
        user: User,
        resError: (NetworkError) -> Unit,
        resSuccess: (Set<String>) -> Unit) {

    init {
        val token = requireNotNull(user.token, { "Cannot get missing fields for logged out user" })
        ServiceHolder.userService(user).getMissingRequiredFields(user.userId.id, token)
                .enqueue(object : NetworkCallback<ApiContainer<RequiredFieldsResponse>>("Fetching required fields") {
                    override fun onError(error: NetworkError) {
                        resError(error)
                    }

                    override fun onSuccess(result: ApiContainer<RequiredFieldsResponse>) {
                        resSuccess(result.data.fields)
                    }
                })
    }
}
