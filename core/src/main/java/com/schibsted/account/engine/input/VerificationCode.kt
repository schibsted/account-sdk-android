/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.input

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.InputProvider

data class VerificationCode(val verificationCode: String, val keepLoggedIn: Boolean) : Parcelable {
    constructor(source: Parcel) : this(source.readString(), source.readInt() != 0)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(verificationCode)
        writeInt(if (keepLoggedIn) 1 else 0)
    }

    interface Provider {
        /**
         * Called when a verification code is required
         */
        fun onVerificationCodeRequested(verificationCodeProvider: InputProvider<VerificationCode>, identifier: Identifier)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VerificationCode> = object : Parcelable.Creator<VerificationCode> {
            override fun createFromParcel(source: Parcel): VerificationCode = VerificationCode(source)
            override fun newArray(size: Int): Array<VerificationCode?> = arrayOfNulls(size)
        }

        internal fun request(provider: Provider, identifier: Identifier, onProvided: (VerificationCode, ResultCallback) -> Unit) {
            provider.onVerificationCodeRequested(InputProvider(onProvided), identifier)
        }
    }
}
