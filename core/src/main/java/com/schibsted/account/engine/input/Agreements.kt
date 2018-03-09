/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.input

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.network.response.AgreementLinksResponse

data class Agreements(val acceptAgreements: Boolean) : Parcelable {
    constructor(source: Parcel) : this(
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt((if (acceptAgreements) 1 else 0))
    }

    interface Provider {
        /**
         * Called when a user has not agreed to the SPiD or platform agreements
         */
        fun onAgreementsRequested(agreementsProvider: InputProvider<Agreements>, agreementLinks: AgreementLinksResponse)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Agreements> = object : Parcelable.Creator<Agreements> {
            override fun createFromParcel(source: Parcel): Agreements = Agreements(source)
            override fun newArray(size: Int): Array<Agreements?> = arrayOfNulls(size)
        }

        internal fun request(provider: Provider, onProvided: (Agreements, ResultCallback<Void?>) -> Unit, agreementLinks: AgreementLinksResponse) {
            provider.onAgreementsRequested(InputProvider(onProvided, { validation ->
                if (!validation.acceptAgreements) "Agreements must be accepted" else null
            }), agreementLinks)
        }
    }
}
