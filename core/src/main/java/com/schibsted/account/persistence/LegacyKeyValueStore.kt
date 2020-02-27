package com.schibsted.account.persistence

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.model.UserToken
import com.schibsted.account.util.KeyValueStore

/**
 * Legacy storage for UserToken. This is no longer in use but we keep it around
 * for migration purposes.
 */
internal class LegacyKeyValueStore(private val keyValueStore: KeyValueStore) {

    fun readToken(): UserToken? {
        val clientSecret = ClientConfiguration.get().clientSecret
        return keyValueStore.readAccessTokenCompat(clientSecret)
    }

    fun clearToken() {
        keyValueStore.clearAccessToken()
    }
}
