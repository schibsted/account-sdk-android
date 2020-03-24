package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Old versions of the SDK contained a couple of bugs in encryption/decryption utils.
 * Sessions encrypted in the old way cannot be decrypted using fixed encryption utils.
 * This class provides access to legacy storage used by those old versions.
 */
internal class SessionStorageLegacy(context: Context,
                                    private val encryptionKeyProvider: EncryptionKeyProvider,
                                    private val encryptionUtils: EncryptionUtils) {
    companion object {
        private const val FILENAME = "IDENTITY_PREFERENCES"
        private const val KEY_AES = "IDENTITY_AES_PREF_KEY"
        private const val KEY_DATA = "IDENTITY_SESSIONS"
        private val GSON = Gson()
    }

    /**
     * Removes session data and its encryption key from legacy storage.
     */
    fun clear() {
        if (isEmpty) return

        prefs.edit().run {
            remove(KEY_AES)
            remove(KEY_DATA)
            apply()
        }
    }

    /**
     * Retrieves session data from legacy storage.
     */
    fun retrieve(): List<UserPersistence.Session>? {
        if (isEmpty) return null

        return runCatching { fetchSessions() }
                .getOrNull()
                .also {
                    if (it == null) {
                        // Storage is not empty, yet the retrieved value is null.
                        // Storage is not readable because of inconsistent state.
                        clear()
                    }
                }
    }

    private val appContext: Context = context.applicationContext

    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
    }

    private val isEmpty: Boolean
        get() = !prefs.contains(KEY_AES) && !prefs.contains(KEY_DATA)

    private fun fetchSessions(): List<UserPersistence.Session>? {
        val encryptedKey = prefs.getBytes(KEY_AES) ?: return null
        val privateRsaKey = encryptionKeyProvider.keyPair.private
        val decryptedKey = encryptionUtils.rsaDecrypt(encryptedKey, privateRsaKey)
        val secretKey = encryptionUtils.recreateAesKey(decryptedKey)

        val encryptedData = prefs.getBytes(KEY_DATA) ?: return null
        // Old SDK versions used to encrypt data using empty ByteArray:
        val iv = ByteArray(16)
        val decryptedData = encryptionUtils.aesDecrypt(encryptedData, secretKey, iv)
        // Old SDK versions used to Base64-encode data twice:
        val decodedData = decryptedData.decodeBase64()
        val json = String(decodedData)
        val typeToken: Type = object : TypeToken<List<UserPersistence.Session>>() {}.type
        return GSON.fromJson(json, typeToken)
    }

    private fun SharedPreferences.getBytes(key: String): ByteArray? =
            getString(key, null)?.toByteArray()?.decodeBase64()
}
