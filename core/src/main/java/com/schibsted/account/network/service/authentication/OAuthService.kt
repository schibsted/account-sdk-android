/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.authentication

import com.schibsted.account.common.util.createBasicAuthHeader
import com.schibsted.account.network.Environment
import com.schibsted.account.network.response.TokenResponse
import com.schibsted.account.network.service.BaseNetworkService
import okhttp3.OkHttpClient
import retrofit2.Call

class OAuthService(@Environment environment: String, okHttpClient: OkHttpClient) : BaseNetworkService(environment, okHttpClient) {

    private val oauthContract: OAuthContract = createService(OAuthContract::class.java)

    /**
     * Requests a token by providing an authentication code
     * @param clientId The client id of the app.
     * @param clientSecret The client secret of the app.
     * @param authCode The auth code used for authentication.
     * @param redirectUri The uri the redirect targeted.
     * @param scopes The scopes requested for the session.
     */
    fun tokenFromAuthCode(
        clientId: String,
        clientSecret: String,
        authCode: String,
        redirectUri: String,
        scopes: Array<String>?
    ): Call<TokenResponse> {

        val params = mutableMapOf(
                PARAM_GRANT_TYPE to GRANT_TYPE_AUTHORIZATION_CODE,
                PARAM_CODE to authCode,
                PARAM_REDIRECT_URI_UNDERSCORE to redirectUri)
        scopes?.let { params.put(PARAM_SCOPE, scopes.joinToString { " " }) }

        return this.oauthContract.token(createBasicAuthHeader(clientId, clientSecret), params, OIDC_VERSION)
    }

    /**
     * Requests client credentials that can be used to request client-authenticated requests.
     * @param clientId The client id to use.
     * @param clientSecret The client secret corresponding to the given id.
     */
    fun tokenFromClientCredentials(clientId: String, clientSecret: String): Call<TokenResponse> {
        val params = mapOf(PARAM_GRANT_TYPE to GRANT_TYPE_CLIENT_CREDENTIALS)

        return this.oauthContract.token(createBasicAuthHeader(clientId, clientSecret), params, OIDC_VERSION)
    }

    /**
     * Requests a token by providing an identifier and the auth code sent to it, plus the
     * passwordless token received as a result of the request to have the auth code sent.
     * @param code The code sent to the identifier.
     * @param passwordlessToken The passwordless token corresponding to the last request for sending
     * (or resending) an auth code.
     * @param scopes The scopes requested for the session.
     */
    fun tokenFromPasswordless(
        clientId: String,
        clientSecret: String,
        identifier: String,
        code: String,
        passwordlessToken: String,
        vararg scopes: String
    ): Call<TokenResponse> {

        val params = mapOf(PARAM_GRANT_TYPE to PARAM_PASSWORDLESS,
                PARAM_SCOPE to scopes.joinToString(" "),
                PARAM_IDENTIFIER to identifier,
                PARAM_CODE to code,
                PARAM_PASSWORDLESS_TOKEN to passwordlessToken)

        return this.oauthContract.resourceOwner(createBasicAuthHeader(clientId, clientSecret), params)
    }

    /**
     * Log in using identifier and password
     * @param clientId The client id of the app.
     * @param clientSecret The client secret of the app.
     * @param username The identifier to log in
     * @param password The password of the user
     */
    fun tokenFromPassword(clientId: String, clientSecret: String, username: String, password: String, vararg scopes: String): Call<TokenResponse> {
        val params = mapOf(PARAM_GRANT_TYPE to "password",
                PARAM_USERNAME to username,
                PARAM_PASSWORD to password,
                PARAM_SCOPE to scopes.joinToString(" "))

        return this.oauthContract.token(createBasicAuthHeader(clientId, clientSecret), params, null)
    }

    /**
     * Prepares and commands a synchronous request to refresh an access token.
     * @param refreshToken The refresh token to use during the refresh.
     * successful.
     */
    fun refreshToken(clientId: String, clientSecret: String, refreshToken: String): Call<TokenResponse> {
        val params = mapOf(PARAM_GRANT_TYPE to GRANT_TYPE_REFRESH_TOKEN, GRANT_TYPE_REFRESH_TOKEN to refreshToken)

        return this.oauthContract.token(createBasicAuthHeader(clientId, clientSecret), params, OIDC_VERSION)
    }

    companion object {
        private val PARAM_CODE = "code"
        private val PARAM_IDENTIFIER = "identifier"
        private val PARAM_PASSWORDLESS = "passwordless"
        private val GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"
        private val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"
        private val GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials"
        private val PARAM_REDIRECT_URI_UNDERSCORE = "redirect_uri"
        private val PARAM_SCOPE = "scope"

        private val PARAM_GRANT_TYPE = "grant_type"
        private val PARAM_USERNAME = "username"

        private val OIDC_VERSION = "v1"
    }
}
