package com.schibsted.account.persistence

import android.content.Context
import com.google.gson.Gson
import com.schibsted.account.util.typeToken
import java.util.UUID

/**
 * Manages a mapping between randomly generated IDs and data. This allows for referring to data using a secret key
 * while the actual data never leaves the device. Example usage: Redirect URIs
 */
class LocalSecretsProvider(appContext: Context, private val maxEntries: Int = MAX_ENTRIES) {
    private val prefs = appContext.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)

    private data class Entry(val time: Long, val key: String, val value: String)

    /**
     * Retrieves previously stored data from a generated key
     * @param secretKey The secret key for the persisted data
     * @return The data previously stored or null if no data is available under that key
     */
    fun get(secretKey: String): String? {
        val secrets = prefs.getString(SHARED_PREFS_KEY, "[]")?.let {
            GSON.fromJson<List<Entry>>(it, typeToken<List<Entry>>())
        } ?: listOf()

        return secrets.find { it.key == secretKey }?.value
    }

    /**
     * Generates a random key for the value you provide and stores the value. If the value already exists,
     * it will re-use the previously generated id.
     * @param value The value to be persisted
     * @return The randomly generated key for your value
     */
    fun put(value: String): String {
        val secrets = prefs.getString(SHARED_PREFS_KEY, "[]")?.let {
            GSON.fromJson<List<Entry>>(it, typeToken<List<Entry>>())
        } ?: listOf()

        val previousResult = secrets.find { it.value == value }?.key
        if (previousResult != null) {
            return previousResult
        }

        val secretKey = UUID.randomUUID().toString()
        val updatedSecrets = (secrets + Entry(System.currentTimeMillis(), secretKey, value))
                .sortedByDescending { it.time }.take(maxEntries)

        with(prefs.edit()) {
            putString(SHARED_PREFS_KEY, GSON.toJson(updatedSecrets))
            apply()
        }

        return secretKey
    }

    companion object {
        private const val SHARED_PREFS_KEY = "AccountSdkLocalSecrets"
        private const val MAX_ENTRIES = 10

        private val GSON = Gson()
    }
}
