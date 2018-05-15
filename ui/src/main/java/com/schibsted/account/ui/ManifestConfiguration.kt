package com.schibsted.account.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.net.URI

@Parcelize
data class ManifestConfiguration(val redirectUri: URI, val clientName: String) : Parcelable {
    companion object {
        private const val CLIENT_NAME = "schacc_client_name"
        private const val REDIRECT_SCHEME = "schacc_redirect_scheme"
        private const val REDIRECT_HOST = "schacc_redirect_host"

        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun readFromManifest(applicationContext: Context): ManifestConfiguration {
            val appInfo = applicationContext.packageManager.getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)

            val keyClientName = applicationContext.getString(R.string.schacc_conf_client_name)
            val keyRedirectScheme = applicationContext.getString(R.string.schacc_conf_redirect_scheme)
            val keyRedirectHost = applicationContext.getString(R.string.schacc_conf_redirect_host)

            val clientName: String? = appInfo.metaData.getString(keyClientName)
            val redirectScheme: String? = appInfo.metaData.getString(keyRedirectScheme)
            val redirectHost: String? = appInfo.metaData.getString(keyRedirectHost)

            if (clientName.isNullOrEmpty()) throw IllegalArgumentException("The field $keyClientName must be specified in the manifest")
            if (redirectScheme.isNullOrEmpty()) throw IllegalArgumentException("The field $keyRedirectScheme must be specified in the manifest")
            if (redirectHost.isNullOrEmpty()) throw IllegalArgumentException("The field $keyRedirectHost must be specified in the manifest")

            return ManifestConfiguration(URI.create("$redirectScheme://$redirectHost"), clientName!!).also { println("CONFX: $it") }
        }
    }
}
