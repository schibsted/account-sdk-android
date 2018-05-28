/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

import java.net.URI
import java.net.URLEncoder

object Routes {
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
    fun forgotPasswordUrl(redirectUri: URI? = null): URI = urlFromPath("flow/password", redirectUri)

    @JvmStatic
    fun accountSummaryUrl(redirectUri: URI? = null): URI = urlFromPath("account/summary", redirectUri, mapOf("response_type" to "code"))
}
