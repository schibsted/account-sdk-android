/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.integration

class CallbackProvider<out T>(private val onProvided: (callback: ResultCallback<T>) -> Unit) {

    fun provide(callback: ResultCallback<T>) {
        onProvided(callback)
    }
}
