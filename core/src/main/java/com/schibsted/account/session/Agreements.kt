/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.network.response.AgreementsResponse
import com.schibsted.account.network.service.user.UserService

class Agreements(private val user: User, private val userService: UserService = UserService(ClientConfiguration.get().environment, user.authClient)) {

    /**
     * Gets the agreements status for the current user
     */
    fun getAgreementsStatus(callback: ResultCallback<AgreementsResponse.Agreements>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        userService.getUserAgreements(user.userId.id, token)
                .enqueue(NetworkCallback.lambda("Fetching user agreements state",
                        { callback.onError(it.toClientError()) },
                        { callback.onSuccess(it.data.agreements) })
                )
    }

    /**
     * Verifies that a user has accepted agreements.
     * @param callback Calls onSuccess when the agreements are accepted, calls onError for any errors or if the agreements are not accepted
     */
    internal fun ensureAccepted(callback: ResultCallback<NoValue>) {
        this.getAgreementsStatus(ResultCallback.fromLambda(
                { callback.onError(it) },
                { agreementsStatus ->
                    if (agreementsStatus.allAccepted()) {
                        callback.onSuccess(NoValue)
                    } else {
                        callback.onError(ClientError(ClientError.ErrorType.AGREEMENTS_NOT_ACCEPTED,
                                "User has not accepted agreements, please log in again."))
                    }
                }))
    }

    /**
     * Accept the agreements of the user.
     * @param callback Provide this callback to get the result of the action
     */
    fun acceptAgreements(callback: ResultCallback<NoValue>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        userService.acceptUserAgreements(user.userId.id, token)
                .enqueue(NetworkCallback.lambda("Accepting terms for user",
                        { callback.onError(it.toClientError()) },
                        { callback.onSuccess(NoValue) })
                )
    }

    companion object {
        /**
         * Gets the agreements links for the current client
         */
        @JvmStatic
        fun getAgreementLinks(callback: ResultCallback<AgreementLinksResponse>) {
            ServiceHolder.clientService.getClientAgreementsUrls(ClientConfiguration.get().clientId)
                    .enqueue(NetworkCallback.lambda("Fetching agreements links",
                            { callback.onError(it.toClientError()) },
                            { callback.onSuccess(it.data) }))
        }
    }
}
