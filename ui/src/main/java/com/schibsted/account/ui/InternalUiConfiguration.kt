/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.app.Application
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.smartlock.SmartlockMode
import java.net.URI
import java.util.Locale

data class InternalUiConfiguration(
    val clientName: String,
    val redirectUri: URI,
    val locale: Locale = Locale.getDefault(),
    val identifierType: Identifier.IdentifierType = Identifier.IdentifierType.EMAIL,
    val identifier: String? = null,
    val signUpEnabled: Boolean = true,
    val smartlockMode: SmartlockMode = SmartlockMode.DISABLED,
    @DrawableRes val clientLogo: Int = 0,
    val teaserText: String? = null,
    val signUpNotAllowedErrorMessage: String? = null,
    val isClosingAllowed: Boolean = true
) : Parcelable {

    init {
        if (!signUpEnabled && signUpNotAllowedErrorMessage.isNullOrEmpty()) {
            throw IllegalArgumentException("The property signUpNotAllowedErrorMessage must be specified if signUpEnabled is set to false")
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readSerializable() as URI,
            source.readSerializable() as Locale,
            source.readSerializable() as Identifier.IdentifierType,
            source.readString(),
            source.readInt() == 1,
            source.readSerializable() as SmartlockMode,
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readInt() == 1
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(clientName)
        writeSerializable(redirectUri)
        writeSerializable(locale)
        writeSerializable(identifierType)
        writeString(identifier)
        writeInt(if (signUpEnabled) 1 else 0)
        writeSerializable(smartlockMode)
        writeInt(clientLogo)
        writeString(teaserText)
        writeString(signUpNotAllowedErrorMessage)
        writeInt(if (isClosingAllowed) 1 else 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<InternalUiConfiguration> = object : Parcelable.Creator<InternalUiConfiguration> {
            override fun createFromParcel(source: Parcel): InternalUiConfiguration = InternalUiConfiguration(source)
            override fun newArray(size: Int): Array<InternalUiConfiguration?> = arrayOfNulls(size)
        }

        @JvmStatic
        fun resolve(application: Application): InternalUiConfiguration {
            val requiredConfig = ManifestConfiguration.readFromManifest(application.applicationContext)
            val optionalConfig = UiConfig.getMerged(application)

            // TODO: Prefilled identifier and teaser text should be arguments instead
            return InternalUiConfiguration(
                    requiredConfig.clientName,
                    requiredConfig.redirectUri,
                    optionalConfig.locale,
                    Identifier.IdentifierType.EMAIL, // TODO: Remove
                    null, // TODO: Remove
                    optionalConfig.signUpEnabled == UiConfig.SignUpMode.Enabled,
                    optionalConfig.smartLockMode,
                    optionalConfig.clientLogo,
                    null, // TODO: Remove
                    (optionalConfig.signUpEnabled as? UiConfig.SignUpMode.Disabled)?.disabledMessage,
                    optionalConfig.isCancellable
            )
        }
    }
}
