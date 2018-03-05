/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import android.support.annotation.WorkerThread
import com.schibsted.account.network.response.TokenResponse
import okhttp3.OkHttpClient

abstract class Session {
    internal abstract fun token(): TokenResponse

    @WorkerThread
    internal abstract fun refreshToken(): Boolean

    abstract fun bind(builder: OkHttpClient.Builder, urls: List<String>, allowInsecure: Boolean): OkHttpClient.Builder
}
