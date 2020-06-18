/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ClientTokenResponse(
    @SerializedName("id_token") val idToken: String?,
    @SerializedName("user_id") val userId: String,
    @SerializedName("access_token") val serializedAccessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("scope") val scope: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
) : Parcelable
