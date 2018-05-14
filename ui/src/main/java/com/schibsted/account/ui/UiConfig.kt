package com.schibsted.account.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import com.schibsted.account.ui.smartlock.SmartlockMode
import java.util.Locale

data class UiConfig(
    val locale: Locale,
    val signUpEnabled: SignUpMode,
    val isCancellable: Boolean,
    val smartLockMode: SmartlockMode,
    @DrawableRes val clientLogo: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(Locale(parcel.readString()),
            parcel.readString()?.let { SignUpMode.Disabled(it) } ?: SignUpMode.Enabled,
            parcel.readInt() == 1,
            parcel.readSerializable() as SmartlockMode,
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(locale.toString())
        parcel.writeString(when (signUpEnabled) {
            is SignUpMode.Enabled -> null
            is SignUpMode.Disabled -> signUpEnabled.disabledMessage
        })
        parcel.writeInt(if (isCancellable) 1 else 0)
        parcel.writeSerializable(smartLockMode)
        parcel.writeInt(clientLogo)
    }

    override fun describeContents() = 0

    fun newBuilder(): Builder = UiConfig.Builder()
            .locale(locale)
            .signUpEnabled(signUpEnabled)
            .isCancellable(isCancellable)
            .smartLockMode(smartLockMode)
            .clientLogo(clientLogo)

    sealed class SignUpMode {
        object Enabled : SignUpMode()
        class Disabled(val disabledMessage: String) : SignUpMode()
    }

    class Builder {
        private var uiConfig = UiConfig.DEFAULT

        fun locale(locale: Locale) = apply { uiConfig = uiConfig.copy(locale = locale) }
        fun signUpEnabled(signUpEnabled: SignUpMode) = apply { uiConfig = uiConfig.copy(signUpEnabled = signUpEnabled) }
        fun isCancellable(isCancellable: Boolean) = apply { uiConfig = uiConfig.copy(isCancellable = isCancellable) }
        fun smartLockMode(smartLockMode: SmartlockMode) = apply { uiConfig = uiConfig.copy(smartLockMode = smartLockMode) }
        fun clientLogo(@DrawableRes clientLogo: Int) = apply { uiConfig = uiConfig.copy(clientLogo = clientLogo) }

        fun build() = uiConfig
    }

    interface UiConfigProvider {
        fun getUiConfig(): UiConfig
    }

    companion object {
        @JvmField
        val DEFAULT = UiConfig(Locale.getDefault(), SignUpMode.Enabled, true, SmartlockMode.DISABLED, 0)

        @JvmStatic
        fun fromManifest(appContext: Context): UiConfig {
            val appInfo = appContext.packageManager.getApplicationInfo(appContext.packageName, PackageManager.GET_META_DATA)

            val keyLocale = appContext.getString(R.string.schacc_conf_locale)
            val keySignUpEnabled = appContext.getString(R.string.schacc_conf_signup_enabled)
            val keySignUpDisabledMessage = appContext.getString(R.string.schacc_conf_signup_disabled_message)
            val keyIsCancellable = appContext.getString(R.string.schacc_conf_cancellable)
            val keySmartLockMode = appContext.getString(R.string.schacc_conf_smartlock_mode)
            val keyClientLogo = appContext.getString(R.string.schacc_conf_client_logo)

            val locale: Locale? = appInfo.metaData.getString(keyLocale)?.let { Locale(it) }
            val signUpEnabled: SignUpMode? = {
                val enabled = appInfo.metaData.getString(keySignUpEnabled)?.toBoolean()
                when (enabled) {
                    true -> SignUpMode.Enabled
                    false -> {
                        val disabledMessage = requireNotNull(appInfo.metaData.getString(keySignUpDisabledMessage), { "When sign-up is disabled, you need to specify a reason why" })
                        SignUpMode.Disabled(disabledMessage)
                    }
                    null -> null
                }
            }()
            val isCancellable: Boolean? = appInfo.metaData.getString(keyIsCancellable)?.toBoolean()
            val smartLockMode: SmartlockMode? = appInfo.metaData.getString(keySmartLockMode)?.let { SmartlockMode.valueOf(it.toUpperCase().trim()) }
            val clientLogo: Int? = appInfo.metaData.getString(keyClientLogo)?.toInt()

            return UiConfig(
                    locale ?: DEFAULT.locale,
                    signUpEnabled ?: DEFAULT.signUpEnabled,
                    isCancellable ?: DEFAULT.isCancellable,
                    smartLockMode ?: DEFAULT.smartLockMode,
                    clientLogo ?: DEFAULT.clientLogo)
        }

        @JvmStatic
        fun fromUiProvider(uiConfigProvider: UiConfigProvider) = uiConfigProvider.getUiConfig()

        @JvmStatic
        fun resolve(application: Application): UiConfig {
            val providerConfig = (application as? UiConfigProvider)?.let { fromUiProvider(application) }
            val manifestConfig = fromManifest(application.applicationContext)

            return providerConfig ?: manifestConfig
        }

        @JvmField
        val CREATOR: Parcelable.Creator<UiConfig> = object : Parcelable.Creator<UiConfig> {
            override fun createFromParcel(source: Parcel): UiConfig = UiConfig(source)
            override fun newArray(size: Int): Array<UiConfig?> = arrayOfNulls(size)
        }
    }
}
