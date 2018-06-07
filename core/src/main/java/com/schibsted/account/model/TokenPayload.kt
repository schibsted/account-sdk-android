/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import android.util.Base64
import com.google.gson.Gson
import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrDefault

data class TokenPayload(
    val iss: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    val sub: String? = null,
    val user_id: String? = null, // Access token only
    val legacy_user_id: String? = null // ID token only
) {
    companion object {
        private val GSON = Gson()

        fun fromRawToken(token: String): TokenPayload {
            val payload = "\\.(.*?)\\.".toRegex().find(token)
            val decodedPayload = payload?.let {
                val decodedToken = Base64.decode(it.value, 0)
                String(decodedToken, Charsets.ISO_8859_1)
            }

            return Try { GSON.fromJson(decodedPayload, TokenPayload::class.java) }.getOrDefault { TokenPayload() }
        }
    }
}
