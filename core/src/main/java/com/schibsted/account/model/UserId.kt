/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import android.os.Parcelable
import android.util.Base64
import com.google.gson.JsonParser
import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrNull
import com.schibsted.account.network.response.UserTokenResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserId(val id: String, val legacyId: String?) : Parcelable {
    companion object {
        private val parser = JsonParser()

        fun fromUserToken(token: UserToken): UserId {
            val idTokenResult = token.idToken?.let { extractPayload(it) }?.let { extractFields(it) }

            val idTokenSub = idTokenResult?.first
            val idTokenLegacyUserId = idTokenResult?.second

            return UserId(idTokenSub ?: idTokenLegacyUserId ?: token.userId, idTokenLegacyUserId ?: token.userId)
        }

        fun fromUserTokenResponse(token: UserTokenResponse): UserId {
            val idTokenResult = token.idToken?.let { extractPayload(it) }?.let { extractFields(it) }

            val idTokenSub = idTokenResult?.first
            val idTokenLegacyUserId = idTokenResult?.second

            return UserId(idTokenSub ?: idTokenLegacyUserId ?: token.userId, idTokenLegacyUserId ?: token.userId)
        }

        internal fun extractFields(payload: String): Pair<String?, String?>? = Try {
            parser.parse(payload).takeIf { it.isJsonObject }?.asJsonObject?.let { obj ->
                val subject = obj["sub"].takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
                val legacyUserId = obj["legacy_user_id"].takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
                Pair(subject, legacyUserId)
            }
        }.getOrNull()

        internal fun extractPayload(token: String): String? = Try {
            val payload = "\\.(.*?)\\.".toRegex().find(token)
            payload?.let {
                val decodedToken = Base64.decode(it.value, 0)
                String(decodedToken, Charsets.ISO_8859_1)
            }
        }.getOrNull()
    }
}
