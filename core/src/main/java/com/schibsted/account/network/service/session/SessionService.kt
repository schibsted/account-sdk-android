/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.session

import com.schibsted.account.network.Environment
import com.schibsted.account.network.response.TokenExchangeResponse
import com.schibsted.account.network.response.TokenResponse
import com.schibsted.account.network.service.BaseNetworkService
import com.schibsted.account.util.Preconditions
import okhttp3.OkHttpClient
import retrofit2.Call
import java.util.HashMap

class SessionService(@Environment environment: String, okHttpClient: OkHttpClient) : BaseNetworkService(environment, okHttpClient) {
    private val sessionContract: SessionContract = createService(SessionContract::class.java)

    /**
     * Requests a one-time code with 30 seconds validity.
     * @param clientId The client id of the app.
     * @param accessToken The access token for the user that the code is requested for.
     */
    fun oneTimeCode(clientId: String, accessToken: TokenResponse): Call<TokenExchangeResponse> {
        Preconditions.checkNotNull(clientId, accessToken)
        val params = HashMap<String, String>()
        params.put(PARAM_CLIENT_ID_NO_UNDERSCORE, clientId)
        params.put(PARAM_TYPE, EXCHANGE_TYPE_CODE)
        return this.sessionContract.exchange(accessToken.bearerAuthHeader(), params)
    }

    /**
     * Requests a one-time session code with 60 seconds validity.
     * @param clientId The client id of the app.
     * @param accessToken The access token for the user that the code is requested for.
     * @param redirectUri The desired redirect to receive from /session/<onetimecode> when the
     * session represented by the code received is created.
    </onetimecode> */
    fun oneTimeSessionCode(clientId: String,
        accessToken: TokenResponse,
        redirectUri: String): Call<TokenExchangeResponse> {
        Preconditions.checkNotNull(clientId, accessToken, redirectUri)
        val params = HashMap<String, String>()
        params.put(PARAM_CLIENT_ID_NO_UNDERSCORE, clientId)
        params.put(PARAM_TYPE, EXCHANGE_TYPE_SESSION)
        params.put(BaseNetworkService.PARAM_REDIRECT_URI_NO_UNDERSCORE, redirectUri)
        return this.sessionContract.exchange(accessToken.bearerAuthHeader(), params)
    }

    companion object {
        private val PARAM_CLIENT_ID_NO_UNDERSCORE = "clientId"
        private val PARAM_TYPE = "type"
        private val EXCHANGE_TYPE_CODE = "code"
        private val EXCHANGE_TYPE_SESSION = "session"
    }
}
