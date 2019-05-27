/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.response.TokenExchangeResponse
import com.schibsted.account.network.service.session.SessionService

class Auth(val user: User) {
    private val sessionService = SessionService(ClientConfiguration.get().environment, user.authClient)

    internal fun constructSessionUrl(code: String) = "${ClientConfiguration.get().environment}session/$code"

    /**
     * Requests a one-time session URL to be used in for example WebViews for the
     * current user. The code expires after 60 seconds.
     * @param targetClientId The client ID this token is intended for. Note: Will not work for other clients
     * @param redirectUri The desired redirect to receive from /session/<onetimecode> when the
     *                    session represented by the code received is created.
     * @param callback A callback with the one time session URL
     * @see <a href="https://techdocs.login.schibsted.com/endpoints/POST/oauth/exchange/">POST /oauth/exchange</a>
     */
    fun oneTimeSessionUrl(targetClientId: String, redirectUri: String, callback: ResultCallback<String>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        sessionService.oneTimeSessionCode(targetClientId, token, redirectUri).enqueue(
                NetworkCallback.lambda<TokenExchangeResponse>("Requesting one time session code",
                        { callback.onError(it.toClientError()) },
                        { callback.onSuccess(constructSessionUrl(it.code)) })
        )
    }

    /**
     * Requests a one-time authentication code for the current user. The code expires after
     * 30 seconds. This can be used to authenticate from for example a back-end server
     * @param callback Callback containing the one time code
     */
    @Deprecated("Duplicate entry", ReplaceWith("this.oneTimeCode(clientId, callback)"))
    fun oneTimeCode(callback: ResultCallback<String>) {
        oneTimeCode(ClientConfiguration.get().clientId, callback)
    }

    /**
     * Requests a one-time authentication code for the current user. The code expires after
     * 30 seconds. This can be used to authenticate from a back-end server.
     * Uses a supplied server client id to create the code.
     * @param serverClientId The client ID of the server
     * @param callback Callback containing the one time code
     */
    fun oneTimeCode(serverClientId: String, callback: ResultCallback<String>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        sessionService.oneTimeCode(serverClientId, token).enqueue(
                NetworkCallback.lambda<TokenExchangeResponse>("Requesting one time code", { callback.onError(it.toClientError()) }, {
                    callback.onSuccess(it.code)
                })
        )
    }
}
