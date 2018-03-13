/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.smartlock.SmartlockImpl
import java.net.URI
import java.util.Locale

data class UiConfiguration(
    val clientName: String,
    val redirectUri: URI,
    val defaultPhonePrefix: Int,
    val locale: Locale = Locale.getDefault(),
    val identifierType: Identifier.IdentifierType = Identifier.IdentifierType.EMAIL,
    val identifier: String? = null,
    val signUpEnabled: Boolean = true,
    val smartlockEnabled: Boolean = false,
    @DrawableRes val headerResource: Int = 0,
    val teaserText: String? = null,
    val signUpNotAllowedErrorMessage: String? = null,
    val isClosingAllowed: Boolean = true
) : Parcelable {

    init {
        if (!signUpEnabled && signUpNotAllowedErrorMessage.isNullOrEmpty()) {
            throw IllegalArgumentException("The property signUpNotAllowedErrorMessage must be specified if signUpEnabled is set to false")
        }
        if (smartlockEnabled && !SmartlockImpl.isSmartlockAvailable()) {
            throw IllegalStateException("Smartlock was enabled, but not found on the classpath. Please verify that the smartlock module is included in your build")
        }
    }

    fun newBuilder(): UiConfiguration.Builder {
        val builder = UiConfiguration.Builder(clientName, redirectUri, defaultPhonePrefix)
            .locale(locale)
            .identifierType(identifierType)
            .identifier(identifier)
            .enableSignUp()
            .disableSmartlock()
            .headerResource(headerResource)
            .teaserText(teaserText)
            .allowClosing(isClosingAllowed)
        signUpNotAllowedErrorMessage?.let { builder.disableSignUp(it) }
        return builder
    }

    constructor(source: Parcel) : this(
        source.readString(),
        source.readSerializable() as URI,
        source.readInt(),
        source.readSerializable() as Locale,
        source.readSerializable() as Identifier.IdentifierType,
        source.readString(),
        source.readInt() == 1,
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
        writeInt(defaultPhonePrefix)
        writeSerializable(locale)
        writeSerializable(identifierType)
        writeString(identifier)
        writeInt(if (signUpEnabled) 1 else 0)
        writeInt(if (smartlockEnabled) 1 else 0)
        writeInt(headerResource)
        writeString(teaserText)
        writeString(signUpNotAllowedErrorMessage)
        writeInt(if (isClosingAllowed) 1 else 0)
    }

    class Builder(clientName: String, redirectUri: URI, defaultPhonePrefix: Int) {
        private var subject = UiConfiguration(clientName, redirectUri, defaultPhonePrefix)

        fun locale(locale: Locale) = apply { this.subject = this.subject.copy(locale = locale) }

        fun identifierType(identifierType: Identifier.IdentifierType) = apply { this.subject = this.subject.copy(identifierType = identifierType) }

        fun identifier(identifier: String?) = apply { this.subject = this.subject.copy(identifier = identifier) }

        fun enableSignUp() = apply { this.subject = this.subject.copy(signUpEnabled = true) }

        fun enableSmartlock() = apply { this.subject = this.subject.copy(smartlockEnabled = true) }

        fun disableSmartlock() = apply { this.subject = this.subject.copy(smartlockEnabled = false) }

        fun disableSignUp(signUpDisabledErrorMessage: String) = apply { this.subject = this.subject.copy(signUpEnabled = false, signUpNotAllowedErrorMessage = signUpDisabledErrorMessage) }

        fun headerResource(@DrawableRes headerResource: Int) = apply { this.subject = this.subject.copy(headerResource = headerResource) }

        fun teaserText(teaserText: String?) = apply { this.subject = this.subject.copy(teaserText = teaserText) }

        fun allowClosing(allowClosing: Boolean) = apply { this.subject = this.subject.copy(isClosingAllowed = allowClosing) }

        fun build() = subject

        companion object {
            @JvmStatic
            fun fromManifest(applicationContext: Context): UiConfiguration.Builder {
                val appInfo = applicationContext.packageManager.getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)

                val clientName = appInfo.metaData.getString(CLIENT_NAME)
                val defaultPhonePrefix = appInfo.metaData.getInt(CLIENT_PHONE_PREFIX)
                val redirectScheme = appInfo.metaData.getString(REDIRECT_SCHEME)
                val redirectHost = appInfo.metaData.getString(REDIRECT_HOST)
                val uriScheme = "$redirectScheme://$redirectHost"

                requireNotNull(clientName, { "The field $CLIENT_NAME must be specified in the manifest" })
                requireNotNull(defaultPhonePrefix, { "The field $CLIENT_PHONE_PREFIX must be specified in the manifest" })
                requireNotNull(redirectScheme, { "The field $REDIRECT_SCHEME must be specified in the manifest" })
                requireNotNull(redirectHost, { "The field $REDIRECT_HOST must be specified in the manifest" })

                return Builder(clientName, URI.create(uriScheme), defaultPhonePrefix)
            }
        }
    }

    companion object {
        private val CLIENT_NAME = "schacc_client_name"

        private val CLIENT_PHONE_PREFIX = "schacc_phone_prefix"

        private val REDIRECT_SCHEME = "schacc_redirect_scheme"

        private val REDIRECT_HOST = "schacc_redirect_host"

        @JvmField
        val CREATOR: Parcelable.Creator<UiConfiguration> = object : Parcelable.Creator<UiConfiguration> {
            override fun createFromParcel(source: Parcel): UiConfiguration = UiConfiguration(source)
            override fun newArray(size: Int): Array<UiConfiguration?> = arrayOfNulls(size)
        }
    }
}
