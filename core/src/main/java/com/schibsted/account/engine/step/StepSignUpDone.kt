package com.schibsted.account.engine.step

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.input.Identifier

class StepSignUpDone(val identifier: Identifier) : Step(), Parcelable {
    constructor(source: Parcel) : this(
            source.readParcelable<Identifier>(Identifier::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(identifier, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StepSignUpDone> = object : Parcelable.Creator<StepSignUpDone> {
            override fun createFromParcel(source: Parcel): StepSignUpDone = StepSignUpDone(source)
            override fun newArray(size: Int): Array<StepSignUpDone?> = arrayOfNulls(size)
        }
    }
}
