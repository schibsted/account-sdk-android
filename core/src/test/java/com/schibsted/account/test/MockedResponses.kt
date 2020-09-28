/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.test

import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MockedResponses<T>(val responses: List<T>, val rotating: Boolean = false) : Call<T> {
    private var index = 0

    private fun nextResp(): Response<T> {
        val next = responses[index]

        index = if (rotating) {
            (index + 1) % (responses.size)
        } else {
            index + 1
        }

        return Response.success(next)
    }

    override fun enqueue(callback: Callback<T>) {
        callback.onResponse(this, nextResp())
    }

    override fun execute(): Response<T> = nextResp()

    override fun request(): Request = Request.Builder().url("https://some-mocked-url.com/").build()

    override fun cancel() {
        throw IllegalAccessError("Not intended for use")
    }

    override fun isCanceled(): Boolean {
        throw IllegalAccessError("Not intended for use")
    }

    override fun isExecuted(): Boolean {
        throw IllegalAccessError("Not intended for use")
    }

    override fun clone(): Call<T> {
        throw IllegalAccessError("Not intended for use")
    }
}
