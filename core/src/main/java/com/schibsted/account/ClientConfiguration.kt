/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.VisibleForTesting
import com.schibsted.account.common.util.Logger
import com.schibsted.account.network.Environment
import com.schibsted.account.util.ConfigurationUtils

/**
 * This contains the client configuration as found in the self service pages in SPiD
 * @param environment The SPiD [Environment] to use
 * @param clientId The ID of the SPiD client to use. Can be found in Self-Service
 * @param clientSecret The client's secret. Can be found in Self-Service
 * @see <a href="https://selfservice.login.schibsted.com/login">Self-Service PRO</a>
 * @see <a href="https://selfservice.identity-pre.schibsted.com">Self-Service PRE</a>
 */
data class ClientConfiguration(
        @Environment val environment: String,
        val clientId: String,
        val clientSecret: String
) : Parcelable {

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(environment)
        writeString(clientId)
        writeString(clientSecret)
    }

    companion object {
        const val DEFAULT_KEY = "DEFAULT_KEY"
        private const val KEY_ID = "clientId"
        private const val KEY_SECRET = "clientSecret"
        private const val KEY_ENVIRONMENT = "environment"
        private var currentConfig: ClientConfiguration? = null
        private var configId: String = DEFAULT_KEY

        @JvmStatic
        fun get(): ClientConfiguration = currentConfig ?: loadConf()

        @JvmStatic
        fun setConfigurationId(configurationId: String) {
            this.configId = configurationId
            currentConfig = loadConf()
        }

        @JvmStatic
        fun set(clientConfiguration: ClientConfiguration) {
            currentConfig = clientConfiguration
        }

        private fun loadConf(): ClientConfiguration {
            val assetConf = ConfigurationUtils.paramsFromAssets()

            if (assetConf.size > 1 && configId == DEFAULT_KEY) {
                throw IllegalArgumentException("You have multiple configuration you need to specify the configuration ID")
            }

            if (assetConf.containsKey(configId)) {
                return fromParams(assetConf[configId]!!)
            } else {
                throw IllegalArgumentException("$configId wasn't found in the configuration file")
            }

        }

        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        internal fun fromParams(params: Map<String, Any>): ClientConfiguration {
            val clientId = requireNotNull(params[KEY_ID], { "Field $KEY_ID is required in the configuration" }) as String
            val clientSecret = requireNotNull(params[KEY_SECRET], { "Field $KEY_SECRET is required in the configuration" }) as String
            val rawEnv = requireNotNull(params[KEY_ENVIRONMENT], { "Field $KEY_ENVIRONMENT is required in the configuration" }) as String

            val environment = when (rawEnv) {
                "DEV" -> Environment.ENVIRONMENT_DEVELOPMENT
                "PRE" -> Environment.ENVIRONMENT_PREPRODUCTION
                "PRO" -> Environment.ENVIRONMENT_PRODUCTION
                "PRO_NORWAY" -> Environment.ENVIRONMENT_PRODUCTION_NORWAY
                else -> rawEnv
            }.trim('/') + "/"

            Logger.verbose(Logger.DEFAULT_TAG + "-CONF", {
                "Read config file:\n" +
                        "Environment:\t$environment \n" +
                        "Client ID:\t${clientId.take(3)}..........${clientId.takeLast(3)} \n" +
                        "Client Secret:\t${clientSecret.take(3)}..........${clientSecret.takeLast(3)} \n"
            })

            return ClientConfiguration(environment, clientId, clientSecret)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<ClientConfiguration> = object : Parcelable.Creator<ClientConfiguration> {
            override fun createFromParcel(source: Parcel): ClientConfiguration = ClientConfiguration(source)
            override fun newArray(size: Int): Array<ClientConfiguration?> = arrayOfNulls(size)
        }
    }
}
