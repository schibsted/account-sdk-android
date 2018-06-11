/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import okhttp3.Request

interface AuthSecurityCheck {
    fun securityCheck(request: Request): AuthCheckResult

    companion object {
        fun default(whitelist: List<String>, allowNonHttps: Boolean = false, allowNonWhitelistedDomains: Boolean = false): AuthSecurityCheck {
            return object : AuthSecurityCheck {
                override fun securityCheck(request: Request): AuthCheckResult {
                    val url = request.url()

                    if (!allowNonHttps && !url.isHttps) {
                        return AuthCheckResult.Failed("Authenticated requests can only be done over HTTPS unless specifically allowed")
                    }

                    if (!allowNonWhitelistedDomains && whitelist.find { url.toString().startsWith(it) } == null) {
                        return AuthCheckResult.Failed("Requests can only be done to whitelisted urls, unless this check is specifically disabled")
                    }

                    return AuthCheckResult.Passed
                }
            }
        }
    }
}
