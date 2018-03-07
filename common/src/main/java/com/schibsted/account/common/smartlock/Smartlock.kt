/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.smartlock

interface Smartlock {
    fun requestCredentials()
    fun saveCredential(username: String, password: String)
    fun deleteCredential()
}
