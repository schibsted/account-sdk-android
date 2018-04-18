/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.network.service.authentication.OAuthService
import com.schibsted.account.network.service.client.ClientService
import com.schibsted.account.network.service.passwordless.PasswordlessService
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ServiceHolder {
    const val TIMEOUT_MS = 10_000L

    val defaultClient: OkHttpClient = OkHttpClient.Builder()
            .writeTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .addInterceptor(InfoInterceptor(true))
            .build()

    internal var oAuthService = OAuthService(ClientConfiguration.get().environment, defaultClient)

    internal var clientService = ClientService(ClientConfiguration.get().environment, defaultClient)

    internal var passwordlessService = PasswordlessService(ClientConfiguration.get().environment, defaultClient)
}
