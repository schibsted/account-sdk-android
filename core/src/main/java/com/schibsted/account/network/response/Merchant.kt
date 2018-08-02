package com.schibsted.account.network.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Merchant(
    val name: String,
    val locale: String,
    val providerId: String,
    val logo: String,
    val type: String
) : Parcelable {
    companion object Type {
        const val INTERNAL = "internal"
        const val EXTERNAL = "external"
    }
}
