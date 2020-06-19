/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrDefault
import com.schibsted.account.network.response.UserTokenResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserToken(
    @SerializedName("id_token") val idToken: String?,
    @SerializedName("user_id") val userId: String,
    @SerializedName("access_token") val serializedAccessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("scope") val scope: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
) : Parcelable {

    constructor(userTokenResponse: UserTokenResponse) : this(
            idToken = userTokenResponse.idToken,
            userId = userTokenResponse.userId,
            serializedAccessToken = userTokenResponse.serializedAccessToken,
            refreshToken = userTokenResponse.refreshToken,
            scope = userTokenResponse.scope,
            tokenType = userTokenResponse.tokenType,
            expiresIn = userTokenResponse.expiresIn
    )

    // This needs to be fail safe form Kotlin's null checks, as parsing done in Java can disregard null checks
    fun isValidToken(): Boolean = Try {
        serializedAccessToken.isNotBlank() &&
                !refreshToken.isNullOrBlank() &&
                (idToken?.isNotBlank() == true || userId.isNotBlank())
    }.getOrDefault { false }

}
