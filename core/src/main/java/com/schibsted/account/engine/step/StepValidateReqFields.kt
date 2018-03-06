/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.step

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.input.RequiredFields

data class StepValidateReqFields(val requiredFields: RequiredFields) : Step(), Parcelable {
    constructor(source: Parcel) : this(
        source.readParcelable<RequiredFields>(RequiredFields::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(requiredFields, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StepValidateReqFields> = object : Parcelable.Creator<StepValidateReqFields> {
            override fun createFromParcel(source: Parcel): StepValidateReqFields = StepValidateReqFields(source)
            override fun newArray(size: Int): Array<StepValidateReqFields?> = arrayOfNulls(size)
        }
    }
}
