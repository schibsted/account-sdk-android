/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.step

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.input.VerificationCode
import com.schibsted.account.session.User

data class StepNoPwValidationCode(private val verificationCode: VerificationCode, val user: User, val agreementsAccepted: Boolean, val missingFields: Set<String>) : Step() {
    constructor(source: Parcel) : this(
            source.readParcelable<VerificationCode>(VerificationCode::class.java.classLoader),
            source.readParcelable<User>(User::class.java.classLoader),
            1 == source.readInt(),
            source.readStringSet()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(verificationCode, 0)
        writeParcelable(user, 0)
        writeInt((if (agreementsAccepted) 1 else 0))
        writeStringSet(missingFields)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StepNoPwValidationCode> = object : Parcelable.Creator<StepNoPwValidationCode> {
            override fun createFromParcel(source: Parcel): StepNoPwValidationCode = StepNoPwValidationCode(source)
            override fun newArray(size: Int): Array<StepNoPwValidationCode?> = arrayOfNulls(size)
        }
    }
}
