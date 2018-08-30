/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import java.util.Locale

data class OptionalConfiguration private constructor(
    val locale: Locale?,
    val signUpMode: SignUpMode?,
    val isCancellable: Boolean?,
    @DrawableRes val clientLogo: Int?,
    val showRememberMeOption: Boolean?
) : Parcelable {

    constructor(parcel: Parcel) : this(Locale(parcel.readString()),
            parcel.readString()?.let { SignUpMode.Disabled(it) } ?: SignUpMode.Enabled,
            parcel.readInt() == 1,
            parcel.readClientLogo(parcel.readInt()),
            parcel.readInt() == 1)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(locale.toString())
        parcel.writeString(when (signUpMode) {
            is SignUpMode.Disabled -> signUpMode.disabledMessage
            else -> null
        })
        parcel.writeInt(if (isCancellable == true) 1 else 0)
        parcel.writeInt(clientLogo ?: 0)
        parcel.writeInt(if (showRememberMeOption == true) 1 else 0)
    }

    override fun describeContents() = 0

    companion object {
        @JvmStatic
        fun fromManifest(appContext: Context): OptionalConfiguration {
            val appInfo = appContext.packageManager.getApplicationInfo(appContext.packageName, PackageManager.GET_META_DATA)

            val keyLocale = appContext.getString(R.string.schacc_conf_locale)
            val keySignUpEnabled = appContext.getString(R.string.schacc_conf_signup_enabled)
            val keySignUpDisabledMessage = appContext.getString(R.string.schacc_conf_signup_disabled_message)
            val keyIsCancellable = appContext.getString(R.string.schacc_conf_cancellable)
            val keyClientLogo = appContext.getString(R.string.schacc_conf_client_logo)
            val keyShowRememberMe = appContext.getString(R.string.schacc_conf_remember_me)

            val locale: Locale? = appInfo.metaData.getString(keyLocale)?.let {
                UiUtil.getLocaleFromLocaleTag(it)
                        ?: throw IllegalArgumentException("The locale format is wrong, you need to use language_country format. For example en_EN")
            }
            val signUpMode: SignUpMode? = {
                val enabled = appInfo.metaData.get(keySignUpEnabled) as? Boolean
                when (enabled) {
                    true -> SignUpMode.Enabled
                    false -> {
                        val disabledMessage = requireNotNull(appInfo.metaData.getString(keySignUpDisabledMessage)) { "When sign-up is disabled, you need to specify a reason why" }
                        SignUpMode.Disabled(disabledMessage)
                    }
                    null -> null
                }
            }()
            val isCancellable: Boolean? = appInfo.metaData.get(keyIsCancellable) as? Boolean
            val showRememberMe: Boolean? = appInfo.metaData.get(keyShowRememberMe) as? Boolean
            val clientLogo: Int? = appInfo.metaData.get(keyClientLogo) as? Int

            return OptionalConfiguration(
                    locale,
                    signUpMode,
                    isCancellable,
                    clientLogo,
                    showRememberMe
            )
        }

        @JvmField
        val CREATOR: Parcelable.Creator<OptionalConfiguration> = object : Parcelable.Creator<OptionalConfiguration> {
            override fun createFromParcel(source: Parcel): OptionalConfiguration = OptionalConfiguration(source)
            override fun newArray(size: Int): Array<OptionalConfiguration?> = arrayOfNulls(size)
        }
    }
}

private fun Parcel.readClientLogo(logoRes: Int): Int? = if (logoRes == 0) null else logoRes