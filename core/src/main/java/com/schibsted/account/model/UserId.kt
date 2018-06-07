/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import android.os.Parcel
import android.os.Parcelable
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
        fun fromTokenResponse(token: TokenResponse): UserId {
            val fields = token.idToken?.let { TokenPayload.fromRawToken(it) }

            val subject = fields?.sub
            val legacySubject = fields?.legacy_user_id ?: token.userId

            return UserId(subject ?: legacySubject, legacySubject)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<UserId> = object : Parcelable.Creator<UserId> {
            override fun createFromParcel(source: Parcel): UserId = UserId(source)
            override fun newArray(size: Int): Array<UserId?> = arrayOfNulls(size)
        }
    }
}
