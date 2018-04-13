/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.PasswordlessToken
import java.util.Locale

/**
 * Task to request user credentials and signup with SPiD using these
 */
internal class SendValidationCodeOperation(
    identifier: Identifier,
    locale: Locale,
    resError: (NetworkError) -> Unit,
    resSuccess: (PasswordlessToken) -> Unit
) {

    init {
        ServiceHolder.passwordlessService.sendValidationCode(ClientConfiguration.get().clientId, identifier.identifier,
                identifier.identifierType.value, locale)
                .enqueue(
                        object : NetworkCallback<PasswordlessToken>("Logging in passwordless") {
                            override fun onError(error: NetworkError) {
                                resError(error)
                            }

                            override fun onSuccess(result: PasswordlessToken) {
                                resSuccess(result)
                            }
                        })
    }
}
