/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import com.schibsted.account.common.util.Logger
import com.schibsted.account.network.Environment
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.util.ConfigurationUtils
import kotlinx.android.parcel.Parcelize

/**
 * This contains the client configuration as found in the self service pages in Schibsted account
 * @param environment The Schibsted account [Environment] to use
 * @param clientId The ID of the Schibsted account client to use. Can be found in Self-Service
 * @param clientSecret The client's secret. Can be found in Self-Service
 * @see <a href="https://selfservice.login.schibsted.com/login">Self-Service PRO</a>
 * @see <a href="https://selfservice.identity-pre.schibsted.com">Self-Service PRE</a>
 */
@Parcelize
data class ClientConfiguration(
    @Environment val environment: String,
    val clientId: String,
    val clientSecret: String
) : Parcelable {

    companion object {
        private const val TAG = "ClientConfiguration"
        private const val KEY_ID = "clientId"
        private const val KEY_SECRET = "clientSecret"
        private const val KEY_ENVIRONMENT = "environment"
        private lateinit var currentConfig: ClientConfiguration

        @JvmStatic
        fun get(): ClientConfiguration {
            if (!::currentConfig.isInitialized) {
                currentConfig = fromParams(ConfigurationUtils.paramsFromAssets())
            }

            return currentConfig
        }

        @JvmStatic
        fun set(clientConfiguration: ClientConfiguration) {
            currentConfig = clientConfiguration

            ServiceHolder.reset()
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun fromParams(params: Map<String, String>): ClientConfiguration {
            val clientId = requireNotNull(params[KEY_ID]) { "Field $KEY_ID is required in the configuration" }
            val clientSecret = requireNotNull(params[KEY_SECRET]) { "Field $KEY_SECRET is required in the configuration" }

            val rawEnv = requireNotNull(params[KEY_ENVIRONMENT]) { "Field $KEY_ENVIRONMENT is required in the configuration" }
            val environment = when (rawEnv) {
                "DEV" -> Environment.ENVIRONMENT_DEVELOPMENT
                "PRE" -> Environment.ENVIRONMENT_PREPRODUCTION
                "PRO" -> Environment.ENVIRONMENT_PRODUCTION
                "PRO_NORWAY" -> Environment.ENVIRONMENT_PRODUCTION_NORWAY
                else -> rawEnv
            }.trim('/') + "/"

            Logger.verbose(TAG,
                "Read config file:\n" +
                        "Environment:\t$environment \n" +
                        "Client ID:\t${clientId.take(3)}..........${clientId.takeLast(3)} \n" +
                        "Client Secret:\t${clientSecret.take(3)}..........${clientSecret.takeLast(3)} \n"
            )

            return ClientConfiguration(environment, clientId, clientSecret)
        }
    }
}
