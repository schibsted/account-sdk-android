/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.Context
import com.google.gson.Gson
import com.schibsted.account.util.typeToken
import java.util.*

/**
 * Manages a mapping between randomly generated IDs and data. This allows for referring to data using a secret key
 * while the actual data never leaves the device. Example usage: Redirect URIs
 */
class LocalSecretsProvider(context: Context, private val maxEntries: Int = MAX_ENTRIES) {
    private data class Entry(val time: Long, val key: String, val value: String)

    private val appContext = context.applicationContext

    private val prefs by lazy {
        appContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
    }

    private var secrets: List<Entry>
        get() = prefs.getString(PREFS_KEY, null)?.let {
            GSON.fromJson<List<Entry>>(it, typeToken<List<Entry>>())
        } ?: emptyList()
        set(value) = with(prefs.edit()) {
            putString(PREFS_KEY, GSON.toJson(value))
            apply()
        }

    /**
     * Retrieves previously stored data from a generated key
     * @param secretKey The secret key for the persisted data
     * @return The data previously stored or null if no data is available under that key
     */
    fun get(secretKey: String): String? = secrets.find { it.key == secretKey }?.value

    /**
     * Generates a random key for the value you provide and stores the value. If the value already exists,
     * it will re-use the previously generated id.
     * @param value The value to be persisted
     * @return The randomly generated key for your value
     */
    fun put(value: String): String {
        return secrets.find { it.value == value }?.key
                ?: UUID.randomUUID().toString()
                        .also { uuid ->
                            val now = System.currentTimeMillis()
                            secrets = (secrets.asSequence() + Entry(now, uuid, value))
                                    .sortedByDescending { it.time }.take(maxEntries).toList()
                        }
    }

    companion object {
        // For legacy reasons, both filename and the key are the same:
        private const val PREFS_FILENAME = "AccountSdkLocalSecrets"
        private const val PREFS_KEY = "AccountSdkLocalSecrets"
        private const val MAX_ENTRIES = 10
        private val GSON = Gson()
    }
}
