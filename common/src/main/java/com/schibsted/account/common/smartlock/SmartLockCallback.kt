/*
 * `Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.`
 */

package com.schibsted.account.common.smartlock

interface SmartLockCallback {

    fun onCredentialRetrieved(id: String, password: String, keepMeLoggedIn: Boolean)
    fun onHintRetrieved(id: String)
    fun onCredentialDeleted()
    fun onFailure()
}
