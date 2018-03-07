/*
 * `Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.`
 */

package com.schibsted.account.smartlock

import com.schibsted.account.engine.input.Credentials

interface SmartLockCallback{

    fun onCredentialRetrieved(credential: Credentials)
    fun onHintRetrieved(id: String)
    fun onCredentialDeleted()
    fun onFailure()
}
