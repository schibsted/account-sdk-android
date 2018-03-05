/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import android.os.Build
import com.schibsted.account.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class InfoInterceptor internal constructor(overrideUserAgent: Boolean) : Interceptor {
    private val headerName: String = if (overrideUserAgent) "User-Agent" else "X-Schibsted-Account-User-Agent"

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
                chain.request()
                        .newBuilder()
                        .header(headerName, "AccountSdk/${BuildConfig.VERSION_NAME} " +
                                "(Linux; Android ${Build.VERSION.RELEASE}; API ${Build.VERSION.SDK_INT}; " +
                                "${Build.MANUFACTURER}; ${Build.MODEL})")
                        .build())
    }
}
