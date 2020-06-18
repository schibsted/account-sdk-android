/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.response.ProductAccess
import com.schibsted.account.network.response.ProfileData
import com.schibsted.account.network.response.Subscription
import com.schibsted.account.network.service.user.UserService

class Profile(val user: User, private val userService: UserService = UserService(ClientConfiguration.get().environment, user.authClient)) {

    fun get(callback: ResultCallback<ProfileData>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        userService.getUserProfile(
                userId = user.userId.id,
                bearerAuthHeader = "Bearer ${token.serializedAccessToken}"
        ).enqueue(NetworkCallback.lambda(
                intent = "Fetching profile data",
                errorFun = { callback.onError(it.toClientError()) },
                successFun = { callback.onSuccess(it.data) }
        ))
    }

    fun update(data: Map<String, Any>, callback: ResultCallback<NoValue>? = null) {
        val token = user.token
        if (token == null) {
            callback?.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        userService.updateUserProfile(
                userId = user.userId.id,
                bearerAuthHeader = "Bearer ${token.serializedAccessToken}",
                profileData = data
        ).enqueue(NetworkCallback.lambda(
                intent = "Updating profile",
                errorFun = { callback?.onError(it.toClientError()) },
                successFun = { callback?.onSuccess(NoValue) }
        ))
    }

    fun getMissingFields(callback: ResultCallback<Set<String>>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        userService.getMissingRequiredFields(
                userId = user.userId.id,
                bearerAuthHeader = "Bearer ${token.serializedAccessToken}"
        ).enqueue(NetworkCallback.lambda(
                intent = "Fetching required fields",
                errorFun = { callback.onError(it.toClientError()) },
                successFun = { callback.onSuccess(it.data.fields) }
        ))
    }

    fun getSubscriptions(callback: ResultCallback<List<Subscription>>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }
        userService.getSubscriptions(
                bearerAuthHeader = "Bearer ${token.serializedAccessToken}",
                userId = user.userId.id
        ).enqueue(NetworkCallback.lambda(
                intent = "Fetching user subscriptions",
                errorFun = { callback.onError(it.toClientError()) },
                successFun = { callback.onSuccess(it.value) }
        ))
    }

    fun getProductAccess(productId: String, callback: ResultCallback<ProductAccess>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }
        userService.getProductAccess(
                bearerAuthHeader = "Bearer ${token.serializedAccessToken}",
                userId = user.userId.id,
                productId = productId
        ).enqueue(NetworkCallback.lambda(
                intent = "Fetching product access",
                errorFun = {
                    if (it.code == 404) {
                        /* spid-platform returns 404 Not Found
                         * when the user doesn't have access to the product
                         */
                        callback.onSuccess(ProductAccess(productId, false))
                    } else {
                        callback.onError(it.toClientError())
                    }
                },
                successFun = { callback.onSuccess(it.data) }
        ))
    }
}
