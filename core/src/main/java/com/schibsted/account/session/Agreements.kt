/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.AcceptAgreementResponse
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.network.response.AgreementsResponse
import com.schibsted.account.network.response.ApiContainer

class Agreements(private val user: User) {
    /**
     * Gets the agreements status for the current user
     */
    fun getAgreementsStatus(callback: ResultCallback<AgreementsResponse.Agreements>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        ServiceHolder.userService(user).getUserAgreements(user.userId.id, token)
            .enqueue(
                object : NetworkCallback<ApiContainer<AgreementsResponse>>("Fetching user agreements state") {
                    override fun onSuccess(result: ApiContainer<AgreementsResponse>) {
                        callback.onSuccess(result.data.agreements)
                    }

                    override fun onError(error: NetworkError) {
                        callback.onError(error.toClientError())
                    }
                })
    }

    /**
     * Accept the agreements of the user.
     * @param callback Provide this callback to get the result of the action
     */
    fun acceptAgreements(callback: ResultCallback<Void?>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        ServiceHolder.userService(user).acceptUserAgreements(user.userId.id, token)
            .enqueue(
                object : NetworkCallback<ApiContainer<AcceptAgreementResponse>>("Accepting terms for user") {
                    override fun onSuccess(result: ApiContainer<AcceptAgreementResponse>) {
                        callback.onSuccess(null)
                    }

                    override fun onError(error: NetworkError) {
                        callback.onError(error.toClientError())
                    }
                })
    }

    companion object {
        /**
         * Gets the agreements links for the current client
         */
        @JvmStatic
        fun getAgreementLinks(callback: ResultCallback<AgreementLinksResponse>) {
            ServiceHolder.clientService().getClientAgreementsUrls(ClientConfiguration.get().clientId)
                .enqueue(object : NetworkCallback<ApiContainer<AgreementLinksResponse>>("Fetching agreements links") {
                    override fun onSuccess(result: ApiContainer<AgreementLinksResponse>) {
                        callback.onSuccess(result.data)
                    }

                    override fun onError(error: NetworkError) {
                        callback.onError(error.toClientError())
                    }
                })
        }
    }
}
