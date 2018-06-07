/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrDefault

data class TokenResponse(
    @SerializedName("id_token") val idToken: String? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("access_token") val serializedAccessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("scope") val scope: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
) : Parcelable {

    fun bearerAuthHeader(): String = "Bearer " + serializedAccessToken

    // This needs to be fail safe form Kotlin's null checks, as parsing done in Java can disregard null checks
    fun isValidToken(): Boolean = Try {
        serializedAccessToken.isNotBlank() &&
                !refreshToken.isNullOrBlank() &&
                (idToken?.isNotBlank() == true || userId.isNotBlank())
    }.getOrDefault { false }

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idToken)
        parcel.writeString(userId)
        parcel.writeString(serializedAccessToken)
        parcel.writeString(refreshToken)
        parcel.writeString(scope)
        parcel.writeString(tokenType)
        parcel.writeInt(expiresIn)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<TokenResponse> {
            override fun createFromParcel(parcel: Parcel): TokenResponse = TokenResponse(parcel)

            override fun newArray(size: Int): Array<TokenResponse?> = arrayOfNulls(size)
        }
    }
}
