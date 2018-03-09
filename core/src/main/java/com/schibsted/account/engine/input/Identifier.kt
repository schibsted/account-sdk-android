/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.input

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.operation.AccountStatusOperation
import com.schibsted.account.network.response.AccountStatusResponse

data class Identifier(val identifierType: IdentifierType, val identifier: String) : Parcelable {
    enum class IdentifierType(val value: String) {
        SMS("sms"), EMAIL("email");

        override fun toString() = value
    }

    /**
     * Asks SPiD for the account status of this identifier
     */
    fun getAccountStatus(callbackData: ResultCallback<AccountStatusResponse>) {
        AccountStatusOperation(this, { callbackData.onError(it.toClientError()) }, {
            callbackData.onSuccess(it)
        })
    }

    constructor(source: Parcel) : this(
            IdentifierType.values()[source.readInt()],
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(identifierType.ordinal)
        writeString(identifier)
    }

    interface Provider {
        /**
         * Called when an identifier is required
         */
        fun onIdentifierRequested(provider: InputProvider<Identifier>)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Identifier> = object : Parcelable.Creator<Identifier> {
            override fun createFromParcel(source: Parcel): Identifier = Identifier(source)
            override fun newArray(size: Int): Array<Identifier?> = arrayOfNulls(size)
        }

        internal fun request(provider: Provider, onProvided: (Identifier, ResultCallback<Void?>) -> Unit) {
            provider.onIdentifierRequested(InputProvider(onProvided))
        }
    }
}
