/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class AgreementLinksResponse(@SerializedName("accepted") private val accepted: Boolean,
                                  @SerializedName("terms_url") val clientTermsUrl: String,
                                  @SerializedName("privacy_url") val clientPrivacyUrl: String,
                                  @SerializedName("platform_terms_url") val spidTermsUrl: String,
                                  @SerializedName("summary") val summary: String,
                                  @SerializedName("platform_privacy_url") val spidPrivacyUrl: String) : Parcelable {

    constructor(source: Parcel) : this(
        1 == source.readInt(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt((if (accepted) 1 else 0))
        writeString(clientTermsUrl)
        writeString(clientPrivacyUrl)
        writeString(spidTermsUrl)
        writeString(summary)
        writeString(spidPrivacyUrl)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AgreementLinksResponse> = object : Parcelable.Creator<AgreementLinksResponse> {
            override fun createFromParcel(source: Parcel): AgreementLinksResponse = AgreementLinksResponse(source)
            override fun newArray(size: Int): Array<AgreementLinksResponse?> = arrayOfNulls(size)
        }
    }
}
