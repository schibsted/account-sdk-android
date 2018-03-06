/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response

data class AgreementsResponse(val agreements: Agreements) {

    data class Agreements(private val platform: Boolean, private val client: Boolean) {
        fun allAccepted(): Boolean = platform && client
    }
}
