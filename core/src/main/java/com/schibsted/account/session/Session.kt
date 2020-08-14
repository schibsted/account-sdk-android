/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import androidx.annotation.WorkerThread
import com.schibsted.account.network.response.TokenResponse
import okhttp3.OkHttpClient

abstract class Session {
    internal abstract fun token(): TokenResponse

    @WorkerThread
    internal abstract fun refreshToken(): Boolean

    abstract fun bind(builder: OkHttpClient.Builder, urls: List<String>, allowInsecure: Boolean): OkHttpClient.Builder
}
