package com.schibsted.account.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcelable
import java.net.URI
import kotlinx.android.parcel.Parcelize

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

            val clientName: String? = appInfo.metaData.getString(CLIENT_NAME)
            val redirectScheme: String? = appInfo.metaData.getString(REDIRECT_SCHEME)
            val redirectHost: String? = appInfo.metaData.getString(REDIRECT_HOST)

            requireNotNull(clientName, { "The field $CLIENT_NAME must be specified in the manifest" })
            requireNotNull(redirectScheme, { "The field $CLIENT_NAME must be specified in the manifest" })
            requireNotNull(redirectHost, { "The field $CLIENT_NAME must be specified in the manifest" })

            if (clientName.isNullOrEmpty()) throw IllegalArgumentException("The field $CLIENT_NAME must be specified in the manifest")
            if (redirectScheme.isNullOrEmpty()) throw IllegalArgumentException("The field $REDIRECT_SCHEME must be specified in the manifest")
            if (redirectHost.isNullOrEmpty()) throw IllegalArgumentException("The field $REDIRECT_HOST must be specified in the manifest")

            return ManifestConfiguration(URI.create("$redirectScheme://$redirectHost"), clientName!!)
        }
    }
}
