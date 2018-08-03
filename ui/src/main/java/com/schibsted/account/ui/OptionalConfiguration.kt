/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import java.util.Locale

data class OptionalConfiguration(
    val locale: Locale,
    val signUpEnabled: SignUpMode,
    val isCancellable: Boolean,
    @DrawableRes val clientLogo: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(Locale(parcel.readString()),
            parcel.readString()?.let { SignUpMode.Disabled(it) } ?: SignUpMode.Enabled,
            parcel.readInt() == 1,
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(locale.toString())
        parcel.writeString(when (signUpEnabled) {
            is SignUpMode.Enabled -> null
            is SignUpMode.Disabled -> signUpEnabled.disabledMessage
        })
        parcel.writeInt(if (isCancellable) 1 else 0)
        parcel.writeInt(clientLogo)
    }

    override fun describeContents() = 0

    fun newBuilder(): Builder = OptionalConfiguration.Builder()
            .locale(locale)
            .signUpEnabled(signUpEnabled)
            .isCancellable(isCancellable)
            .clientLogo(clientLogo)

    sealed class SignUpMode {
        object Enabled : SignUpMode()
        class Disabled(val disabledMessage: String) : SignUpMode()
    }

    class Builder {
        private var uiConfig = OptionalConfiguration.DEFAULT

        fun locale(locale: Locale) = apply { uiConfig = uiConfig.copy(locale = locale) }
        fun signUpEnabled(signUpEnabled: SignUpMode) = apply { uiConfig = uiConfig.copy(signUpEnabled = signUpEnabled) }
        fun isCancellable(isCancellable: Boolean) = apply { uiConfig = uiConfig.copy(isCancellable = isCancellable) }
        fun clientLogo(@DrawableRes clientLogo: Int) = apply { uiConfig = uiConfig.copy(clientLogo = clientLogo) }

        fun build() = uiConfig
    }

    interface UiConfigProvider {
        fun getUiConfig(): OptionalConfiguration
    }

    companion object {
        @JvmField
        val DEFAULT = OptionalConfiguration(Locale.getDefault(), SignUpMode.Enabled, true, 0)

        @JvmStatic
        fun fromManifest(appContext: Context): OptionalConfiguration {
            val appInfo = appContext.packageManager.getApplicationInfo(appContext.packageName, PackageManager.GET_META_DATA)

            val keyLocale = appContext.getString(R.string.schacc_conf_locale)
            val keySignUpEnabled = appContext.getString(R.string.schacc_conf_signup_enabled)
            val keySignUpDisabledMessage = appContext.getString(R.string.schacc_conf_signup_disabled_message)
            val keyIsCancellable = appContext.getString(R.string.schacc_conf_cancellable)
            val keyClientLogo = appContext.getString(R.string.schacc_conf_client_logo)

            val locale: Locale? = appInfo.metaData.getString(keyLocale)?.let {
                UiUtil.getLocaleFromLocaleTag(it)
                        ?: throw IllegalArgumentException("The locale format is wrong, you need to use language_country format. For example en_EN")
            }
            val signUpEnabled: SignUpMode? = {
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
            val clientLogo: Int? = appInfo.metaData.get(keyClientLogo) as? Int

            return OptionalConfiguration(
                    locale ?: DEFAULT.locale,
                    signUpEnabled ?: DEFAULT.signUpEnabled,
                    isCancellable ?: DEFAULT.isCancellable,
                    clientLogo ?: DEFAULT.clientLogo)
        }

        @JvmStatic
        fun fromUiProvider(uiConfigProvider: UiConfigProvider) = uiConfigProvider.getUiConfig()

        @JvmStatic
        fun resolve(application: Application): OptionalConfiguration {
            val providerConfig = (application as? UiConfigProvider)?.let { fromUiProvider(application) }
            val manifestConfig = fromManifest(application.applicationContext)

            return providerConfig ?: manifestConfig
        }

        @JvmField
        val CREATOR: Parcelable.Creator<OptionalConfiguration> = object : Parcelable.Creator<OptionalConfiguration> {
            override fun createFromParcel(source: Parcel): OptionalConfiguration = OptionalConfiguration(source)
            override fun newArray(size: Int): Array<OptionalConfiguration?> = arrayOfNulls(size)
        }
    }
}
