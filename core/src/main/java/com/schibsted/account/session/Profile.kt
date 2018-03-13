/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import com.schibsted.account.Routes
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.ResultCallbackData
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.ProfileData
import com.schibsted.account.network.response.RequiredFieldsResponse
import java.net.URI

class Profile(val user: User) {
    /**
     * Returns a link to the account summary pages
     */
    @Deprecated("Deprecated since 0.8.0, in favor of the Routes class", ReplaceWith("Routes.accountSummaryUrl(redirectURI)", "com.schibsted.account.Routes"))
    fun getAccountSummaryLink(redirectURI: String? = null) = Routes.accountSummaryUrl(URI.create(redirectURI))

    fun get(callback: ResultCallbackData<ProfileData>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        ServiceHolder.userService(user).getUserProfile(user.userId.id, token)
                .enqueue(object : NetworkCallback<ApiContainer<ProfileData>>("Fetching profile data") {
                    override fun onError(error: NetworkError) {
                        callback.onError(error.toClientError())
                    }

                    override fun onSuccess(result: ApiContainer<ProfileData>) {
                        callback.onSuccess(result.data)
                    }
                })
    }

    fun update(data: Map<String, Any>, callback: ResultCallback? = null) {
        val token = user.token
        if (token == null) {
            callback?.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        ServiceHolder.userService(user).updateUserProfile(user.userId.id, token, data)
                .enqueue(object : NetworkCallback<Unit>("Updating profile") {
                    override fun onError(error: NetworkError) {
                        callback?.onError(error.toClientError())
                    }

                    override fun onSuccess(result: Unit) {
                        callback?.onSuccess()
                    }
                })
    }

    fun getMissingFields(callback: ResultCallbackData<Set<String>>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        ServiceHolder.userService(user).getMissingRequiredFields(user.userId.id, token)
                .enqueue(object : NetworkCallback<ApiContainer<RequiredFieldsResponse>>("Fetching required fields") {
                    override fun onError(error: NetworkError) {
                        callback.onError(error.toClientError())
                    }

                    override fun onSuccess(result: ApiContainer<RequiredFieldsResponse>) {
                        callback.onSuccess(result.data.fields)
                    }
                })
    }
}
