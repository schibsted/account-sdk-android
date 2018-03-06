/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.step

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.input.Agreements

data class StepValidateAgreements(val agreements: Agreements) : Step(), Parcelable {
    constructor(source: Parcel) : this(
        source.readParcelable<Agreements>(Agreements::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(agreements, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StepValidateAgreements> = object : Parcelable.Creator<StepValidateAgreements> {
            override fun createFromParcel(source: Parcel): StepValidateAgreements = StepValidateAgreements(source)
            override fun newArray(size: Int): Array<StepValidateAgreements?> = arrayOfNulls(size)
        }
    }
}
