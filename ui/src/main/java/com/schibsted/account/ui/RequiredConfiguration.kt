package com.schibsted.account.ui

import android.content.Context
import android.os.Parcelable
import com.schibsted.account.common.util.Logger
import kotlinx.android.parcel.Parcelize
import java.net.URI

@Parcelize
data class RequiredConfiguration(val redirectUri: URI, val clientName: String) : Parcelable {
    companion object {
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun fromResources(applicationContext: Context): RequiredConfiguration {
            val clientName = applicationContext.getString(R.string.schacc_conf_client_name)
            val redirectScheme = applicationContext.getString(R.string.schacc_conf_redirect_scheme)
            var redirectHost = applicationContext.getString(R.string.schacc_conf_redirect_host)

            if (clientName.isNullOrBlank()) throw IllegalArgumentException("A value for the string <schacc_conf_client_name> must be provided in strings.xml")
            if (redirectScheme.isNullOrBlank()) throw IllegalArgumentException("A value for the string <schacc_conf_redirect_scheme> must be provided in strings.xml")
            if (redirectHost.isNullOrBlank()) throw IllegalArgumentException("A value for the string <schacc_conf_redirect_host> must be provided in strings.xml")

            if (!redirectHost.contains(":/")) {
                Logger.warn(Logger.DEFAULT_TAG + "-RequiredConfiguration", "Your host is not correct, deep links might fail, please provide a complete host")
                redirectHost = "://$redirectHost"
            }
            return RequiredConfiguration(URI.create("$redirectScheme$redirectHost"), clientName!!)
        }
    }
}
