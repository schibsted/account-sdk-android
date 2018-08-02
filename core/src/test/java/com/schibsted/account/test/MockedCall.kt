/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.test

import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class MockedCall<T>(private val response: Response<T>) : Call<T> {
    override fun enqueue(callback: Callback<T>?) {
        callback?.onResponse(this, response)
    }

    override fun execute(): Response<T> = response

    override fun isExecuted(): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun clone(): Call<T> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun isCanceled(): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun cancel() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun request(): Request {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
