/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.app.Application
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import com.schibsted.account.engine.input.Identifier
import java.net.URI
import java.util.Locale

data class InternalUiConfiguration(
    val clientName: String,
    val redirectUri: URI,
    val locale: Locale = AccountUi.Params.DEFAULT_LOCALE,
    val identifierType: Identifier.IdentifierType = Identifier.IdentifierType.EMAIL,
    val identifier: String? = AccountUi.Params.DEFAULT_PREFILLED_IDENTIFIER,
    val signUpEnabled: Boolean = true,
    @DrawableRes val clientLogo: Int = AccountUi.Params.DEFAULT_CLIENT_LOGO,
    val teaserText: String? = AccountUi.Params.DEFAULT_TEASER,
    val signUpNotAllowedErrorMessage: String? = null,
    val isClosingAllowed: Boolean = AccountUi.Params.DEFAULT_IS_CANCELLABLE,
    val showRememberMeEnabled: Boolean = AccountUi.Params.DEFAULT_SHOW_REMEMBER_ME
) : Parcelable {

    init {
        if (!signUpEnabled && signUpNotAllowedErrorMessage.isNullOrEmpty()) {
            throw IllegalArgumentException("The property signUpNotAllowedErrorMessage must be specified if signUpMode is set to false")
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readSerializable() as URI,
            source.readSerializable() as Locale,
            source.readSerializable() as Identifier.IdentifierType,
            source.readString(),
            source.readInt() == 1,
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
        fun resolve(application: Application, uiParams: AccountUi.Params, idType: Identifier.IdentifierType): InternalUiConfiguration {
            val requiredConfig = RequiredConfiguration.fromResources(application.applicationContext)
            val optionalConfig = OptionalConfiguration.fromManifest(application)

            val signupEnabled = when {
                uiParams.signUpMode != AccountUi.Params.DEFAULT_SIGNUP_MODE -> uiParams.signUpMode == SignUpMode.Enabled // if overridden programmatically
                optionalConfig.signUpMode != null -> optionalConfig.signUpMode == SignUpMode.Enabled // if defined in manifest
                else -> uiParams.signUpMode == SignUpMode.Enabled // if not use fallback
            }

            val locale = when {
                uiParams.locale != null -> uiParams.locale
                else -> optionalConfig.locale ?: AccountUi.Params.DEFAULT_LOCALE
            }

            val clientLogo = when {
                uiParams.clientLogo != AccountUi.Params.DEFAULT_CLIENT_LOGO -> uiParams.clientLogo
                else -> optionalConfig.clientLogo ?: uiParams.clientLogo
            }
            val isCancellable = when {
                uiParams.isCancellable != AccountUi.Params.DEFAULT_IS_CANCELLABLE -> uiParams.isCancellable
                else -> optionalConfig.isCancellable ?: uiParams.isCancellable
            }

            val disabledMessage = (uiParams.signUpMode as? SignUpMode.Disabled)?.disabledMessage
                    ?: (optionalConfig.signUpMode as? SignUpMode.Disabled)?.disabledMessage

            val showRememberMe = when {
                uiParams.showRememberMeOption != AccountUi.Params.DEFAULT_SHOW_REMEMBER_ME -> uiParams.showRememberMeOption
                else -> optionalConfig.showRememberMeOption ?: uiParams.showRememberMeOption
            }

            return InternalUiConfiguration(
                    requiredConfig.clientName,
                    requiredConfig.redirectUri,
                    locale,
                    idType,
                    uiParams.preFilledIdentifier,
                    signupEnabled,
                    clientLogo,
                    uiParams.teaserText,
                    disabledMessage,
                    isCancellable,
                    showRememberMe
            )
        }
    }
}
