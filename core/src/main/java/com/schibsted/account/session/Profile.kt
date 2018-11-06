/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.response.ProductSubscription
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

        userService.getUserProfile(user.userId.id, token).enqueue(NetworkCallback.lambda("Fetching profile data",
                { callback.onError(it.toClientError()) },
                { callback.onSuccess(it.data) })
        )
    }

    fun update(data: Map<String, Any>, callback: ResultCallback<NoValue>? = null) {
        val token = user.token
        if (token == null) {
            callback?.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        userService.updateUserProfile(user.userId.id, token, data).enqueue(NetworkCallback.lambda("Updating profile",
                { callback?.onError(it.toClientError()) },
                { callback?.onSuccess(NoValue) })
        )
    }

    fun getMissingFields(callback: ResultCallback<Set<String>>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }

        userService.getMissingRequiredFields(user.userId.id, token).enqueue(NetworkCallback.lambda("Fetching required fields",
                { callback.onError(it.toClientError()) },
                { callback.onSuccess(it.data.fields) })
        )
    }

    fun getSubscriptions(callback: ResultCallback<List<Subscription>>) {
        val token = user.token
        if (token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }
        userService.getSubscriptions(token, user.userId.id).enqueue(NetworkCallback.lambda("Fetching user subscriptions",
                { callback.onError(it.toClientError()) },
                {
                    callback.onSuccess(it.value)
                }))
    }

    fun getProductSubscription(productId: String, callback: ResultCallback<ProductSubscription>) {
        val token = user.token
        if(token == null) {
            callback.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }
        userService.getProductSubscription(token, user.userId.id, productId).enqueue(NetworkCallback.lambda("Fetching product info",
            {
                callback.onError(it.toClientError())
            },
            {
               callback.onSuccess(it)
            }
        ))
    }
}
