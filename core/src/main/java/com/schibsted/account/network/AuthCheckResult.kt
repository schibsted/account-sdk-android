/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

sealed class AuthCheckResult {
    object Passed : AuthCheckResult()
    class Failed(val reason: String) : AuthCheckResult()
}
