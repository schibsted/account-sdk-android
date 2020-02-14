/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

import android.content.Context
import com.schibsted.account.util.DeepLink
import java.net.URI
import java.net.URLEncoder
import java.util.Locale

object Routes {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private fun urlFromPath(path: String, redirectUri: URI?, params: Map<String, String> = mapOf()): URI {
        val queryParams = mapOf("client_id" to ClientConfiguration.get().clientId) +
                (redirectUri?.let { mapOf("redirect_uri" to it.toString()) } ?: mapOf()) +
                params

        val queryParamsEnc = queryParams.map { it.key + "=" + URLEncoder.encode(it.value, "utf-8") }
                .joinToString("&")

        return URI.create("${ClientConfiguration.get().environment + path}?$queryParamsEnc")
    }

    @JvmStatic
    fun logoutUrl(redirectUri: URI? = null): URI = urlFromPath("logout", redirectUri)

    @JvmStatic
    @JvmOverloads
    fun forgotPasswordUrl(redirectUri: URI? = null, locale: Locale? = null): URI {
        val params = locale?.let { mapOf("locale" to locale.toString()) } ?: mapOf()
        return urlFromPath("flow/password", redirectUri, params)
    }

    @JvmStatic
    @JvmOverloads
    fun accountSummaryUrl(redirectUri: URI? = null, locale: Locale? = null): URI {
        val params = mapOf("response_type" to "code") + (locale?.let { mapOf("locale" to locale.toString()) } ?: mapOf())
        return urlFromPath("account/summary", redirectUri, params)
    }

    @JvmStatic
    @JvmOverloads
    fun loginUrl(context: Context, redirectUri: URI, persistUser: Boolean, scopes: Collection<String>? = null): URI {
        val state = randomString(10)
        DeepLink.WebFlowLogin.storePrefs(context, state, persistUser)

        val scopeStr = scopes?.joinToString(" ") ?: "openid"
        val params = mapOf(
                "response_type" to "code",
                "scope" to scopeStr,
                "state" to state,
                "nonce" to randomString(10),
                "new-flow" to "true"
        )

        return urlFromPath("oauth/authorize", redirectUri, params)
    }

    private fun randomString(length: Int): String {
        return (1..length)
                .map { _ -> kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("");
    }
}
