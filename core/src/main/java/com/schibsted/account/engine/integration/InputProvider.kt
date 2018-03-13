/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.integration

import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError

class InputProvider<in T>(private val onProvided: (T, callback: ResultCallback<NoValue>) -> Unit, private val validation: (T) -> String?) {
    constructor(onProvided: (T, callback: ResultCallback<NoValue>) -> Unit) : this(onProvided, { null })

    fun provide(input: T, callback: ResultCallback<NoValue>) {
        val validationResult = validation(input)

        if (validationResult == null) {
            onProvided(input, callback)
        } else {
            callback.onError(ClientError(ClientError.ErrorType.INVALID_INPUT, validationResult))
        }
    }
}
