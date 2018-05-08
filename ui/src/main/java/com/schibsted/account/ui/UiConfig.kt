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

            val locale: Locale? = appInfo.metaData.getString("SCHACC_LOCALE")?.let { Locale(it) }
            val signUpEnabled: SignUpMode? = appInfo.metaData.getString("SCHACC_DISABLE_SIGNUP")?.let { SignUpMode.Disabled(it) }
            val isCancellable: Boolean? = appInfo.metaData.getString("SCHACC_IS_CANCELLABLE")?.toBoolean()
            val smartLockMode: SmartlockMode? = appInfo.metaData.getString("SCHACC_SMARTLOCK_MODE")?.let { SmartlockMode.valueOf(it.toUpperCase().trim()) }
            val clientLogo: Int? = appInfo.metaData.getString("SCHACC_CLIENT_LOGO")?.toInt()

            return UiConfig(
                    locale ?: DEFAULT.locale,
                    signUpEnabled ?: DEFAULT.signUpEnabled,
                    isCancellable ?: DEFAULT.isCancellable,
                    smartLockMode ?: DEFAULT.smartLockMode,
                    clientLogo ?: DEFAULT.clientLogo)
        }

        @JvmStatic
        fun fromApplication(application: Application) = (application as? UiConfigProvider)?.getUiConfig()

        @JvmStatic
        fun getMerged(application: Application): UiConfig {
            val appConfig = fromApplication(application)
            val manifestConfig = fromManifest(application.applicationContext)

            return appConfig ?: manifestConfig
        }

        @JvmField
        val CREATOR: Parcelable.Creator<UiConfig> = object : Parcelable.Creator<UiConfig> {
            override fun createFromParcel(source: Parcel): UiConfig = UiConfig(source)
            override fun newArray(size: Int): Array<UiConfig?> = arrayOfNulls(size)
        }
    }
}
