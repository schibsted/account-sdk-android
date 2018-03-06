/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.step

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.network.response.AgreementLinksResponse

data class StepSignUpCredentials(val credentials: Credentials, val clientReqFields: Set<String>, val agreementsLink: AgreementLinksResponse) : Step() {
    constructor(source: Parcel) : this(
        source.readParcelable<Credentials>(Credentials::class.java.classLoader),
        source.readStringSet(),
        source.readParcelable<AgreementLinksResponse>(AgreementLinksResponse::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(credentials, 0)
        writeStringSet(clientReqFields)
        writeParcelable(agreementsLink, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StepSignUpCredentials> = object : Parcelable.Creator<StepSignUpCredentials> {
            override fun createFromParcel(source: Parcel): StepSignUpCredentials = StepSignUpCredentials(source)
            override fun newArray(size: Int): Array<StepSignUpCredentials?> = arrayOfNulls(size)
        }
    }
}
