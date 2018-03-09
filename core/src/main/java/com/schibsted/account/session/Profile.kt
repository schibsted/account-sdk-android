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
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.ProfileData
import com.schibsted.account.network.response.RequiredFieldsResponse

class Profile(val user: User) {
    /**
     * Returns a link to the account summary pages
     */
    fun getAccountSummaryLink(redirectURI: String? = null): String {
        val queryParam = redirectURI?.let { "&redirect_uri=$it" } ?: ""
        return "${ClientConfiguration.get().environment}account/summary?client_id=${ClientConfiguration.get().clientId}$queryParam"
    }

    fun get(callback: ResultCallback<ProfileData>) {
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

    fun update(data: Map<String, Any>, callback: ResultCallback<Void?>? = null) {
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
                        callback?.onSuccess(null)
                    }
                })
    }

    fun getMissingFields(callback: ResultCallback<Set<String>>) {
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
