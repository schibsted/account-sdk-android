/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.AgreementsResponse
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.session.User

internal class AgreementsCheckOperation(
        user: User,
        failure: (NetworkError) -> Unit,
        success: (AgreementsResponse.Agreements) -> Unit) {

    init {
        val token = requireNotNull(user.token, { "Cannot get agreements status for logged out user" })
        ServiceHolder.userService(user).getUserAgreements(user.userId.id, token)
                .enqueue(
                        object : NetworkCallback<ApiContainer<AgreementsResponse>>("Fetching user agreements state") {
                            override fun onSuccess(result: ApiContainer<AgreementsResponse>) {
                                success(result.data.agreements)
                            }

                            override fun onError(error: NetworkError) {
                                failure(error)
                            }
                        })
    }
}
