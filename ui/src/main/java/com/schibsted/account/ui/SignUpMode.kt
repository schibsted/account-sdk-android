package com.schibsted.account.ui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class SignUpMode : Parcelable {
    @Parcelize
    object Enabled : SignUpMode()

    @Parcelize
    class Disabled(val disabledMessage: String) : SignUpMode()
}
