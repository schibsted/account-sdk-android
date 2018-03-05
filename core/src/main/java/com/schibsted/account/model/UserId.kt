/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.VisibleForTesting
import android.util.Base64
import com.google.gson.Gson
import com.schibsted.account.network.response.TokenResponse

data class UserId(val id: String, val legacyId: String?) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(legacyId)
    }

    companion object {
        private val GSON = Gson()

        fun fromTokenResponse(token: TokenResponse): UserId {
            val fields = token.idToken?.let { extractFields(it) }

            val subject = fields?.get("sub") as? String
            val legacySubject = (fields?.get("legacy_user_id") as? String) ?: token.userId

            return UserId(subject ?: legacySubject, legacySubject)
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        internal fun extractFields(idToken: String): Map<String, Any>? {
            val payload = "\\.(.*?)\\.".toRegex().find(idToken)
            val decodedPayload = payload?.let {
                val decodedToken = Base64.decode(it.value, 0)
                String(decodedToken, Charsets.ISO_8859_1)
            }

            return GSON.fromJson(decodedPayload, Map::class.java) as? Map<String, Any>
        }

        @JvmField
        val CREATOR: Parcelable.Creator<UserId> = object : Parcelable.Creator<UserId> {
            override fun createFromParcel(source: Parcel): UserId = UserId(source)
            override fun newArray(size: Int): Array<UserId?> = arrayOfNulls(size)
        }
    }
}
