/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import android.os.Build
import com.schibsted.account.AccountService
import com.schibsted.account.BuildConfig
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.tracking.UiTracking
import com.schibsted.account.common.util.existsOnClasspath
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

open class InfoInterceptor : Interceptor {
    private val userAgentHeaderValue: String = "AccountSdk/${BuildConfig.VERSION_NAME} " +
            "(Linux; Android ${Build.VERSION.RELEASE}; API ${Build.VERSION.SDK_INT}; " +
            "${Build.MANUFACTURER}; ${Build.MODEL}) Android (${AccountService.packageName})"

    protected fun getRequestBuilder(userAgentHeaderName: String, chain: Interceptor.Chain): Request.Builder {
        return chain.request()
                .newBuilder()
                .header(userAgentHeaderName, userAgentHeaderValue)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(getRequestBuilder("X-Schibsted-Account-User-Agent", chain).build())
    }
}

class InternalInfoInterceptor : InfoInterceptor() {
    override fun intercept(chain: Interceptor.Chain): Response {
        var builder = getRequestBuilder("User-Agent", chain)
                .header("SDK-Type", "android")
                .header("SDK-Version", BuildConfig.VERSION_NAME)
                .header("SDK-Build-Type", BuildConfig.BUILD_TYPE)
                .header("SDK-Environment", ClientConfiguration.get().environment)
                .header("SDK-UI-Module", if (existsOnClasspath("com.schibsted.account.ui.AccountUi")) "found" else "missing")

        UiTracking.trackingIdentifier?.let {
            builder = builder.header("pulse-jwe", it)
        }

        return chain.proceed(builder.build())
    }
}
