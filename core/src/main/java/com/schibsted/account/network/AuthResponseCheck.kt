/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import com.schibsted.account.common.util.safeUrl
import okhttp3.Request
import okhttp3.Response

interface AuthResponseCheck {
    fun responseCheck(originalRequest: Request, response: Response): AuthCheckResult

    companion object {

        fun default(whitelist: List<String>): AuthResponseCheck {
            return object : AuthResponseCheck {
                override fun responseCheck(originalRequest: Request, response: Response): AuthCheckResult {
                    val originalUrl = originalRequest.url()
                    val responseUrl = response.request().url()

                    if (whitelist.find { responseUrl.toString().startsWith(it) } == null) {
                        return AuthCheckResult.Failed("URL not whitelisted")
                    }

                    if (responseUrl != originalUrl) {
                        return AuthCheckResult.Failed("Current URL(${response.request().url().toString().safeUrl()}) does not match the original ${originalUrl.toString().safeUrl()}")
                    }

                    return AuthCheckResult.Passed
                }
            }
        }
    }
}
