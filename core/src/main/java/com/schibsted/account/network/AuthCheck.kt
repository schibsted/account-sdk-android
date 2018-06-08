/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import okhttp3.Request

interface AuthCheck {
    sealed class AuthCheckResult {
        object Passed : AuthCheckResult()
        class Failed(val reason: String) : AuthCheckResult()
    }

    fun validate(request: Request): AuthCheckResult

    companion object {
        operator fun invoke(check: (Request) -> AuthCheckResult): AuthCheck {
            return object : AuthCheck {
                override fun validate(request: Request): AuthCheckResult = check(request)
            }
        }
    }
}
