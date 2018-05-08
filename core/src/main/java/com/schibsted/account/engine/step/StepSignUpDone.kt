package com.schibsted.account.engine.step

import android.os.Parcel
import android.os.Parcelable

class StepSignUpDone() : Step() {
    constructor(source: Parcel) : this()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {}

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StepSignUpDone> = object : Parcelable.Creator<StepSignUpDone> {
            override fun createFromParcel(source: Parcel): StepSignUpDone = StepSignUpDone(source)
            override fun newArray(size: Int): Array<StepSignUpDone?> = arrayOfNulls(size)
        }
    }
}
