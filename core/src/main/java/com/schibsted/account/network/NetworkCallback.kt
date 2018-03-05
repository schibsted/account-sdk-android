/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import com.schibsted.account.common.util.Logger
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.common.util.safeUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal abstract class NetworkCallback<T>(val intent: String) : Callback<T> {
    init {
        Logger.verbose(Logger.DEFAULT_TAG + "-NET_REQ", { intent })
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        Logger.error(Logger.DEFAULT_TAG, { "A network error occurred: ${t.message ?: "Unknown"}" }, t)
        onError(NetworkError(-1, "network_error", t.message ?: "Unknown", call.request().url().toString().safeUrl()))
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        val body = response.body()
        if (response.isSuccessful) {
            if (response is ApiContainer<*> && response.data == null) {
                onError(NetworkError(-1, "parse_error", "Content of SPiD API container was null", call.request().url().toString().safeUrl()))
            } else {
                if (body == null) {
                    onSuccess(Unit as T)
                } else {
                    onSuccess(body)
                }
            }
        } else {
            onError(NetworkError.fromResponse(response))
        }
    }

    abstract fun onSuccess(result: T)
    abstract fun onError(error: NetworkError)

    companion object {
        @JvmStatic
        fun <T> lambda(intent: String, errorFun: (NetworkError) -> Unit, successFun: (T) -> Unit): NetworkCallback<T> {
            return object : NetworkCallback<T>(intent) {
                override fun onSuccess(result: T) {
                    successFun(result)
                }

                override fun onError(error: NetworkError) {
                    errorFun(error)
                }
            }
        }
    }
}
