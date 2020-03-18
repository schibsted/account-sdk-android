/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.schibsted.account.common.util.Logger
import com.schibsted.account.persistence.UserPersistence.Session
import java.lang.reflect.Type
import java.security.InvalidKeyException
import javax.crypto.SecretKey
import kotlin.reflect.KProperty

internal class SessionStorageDelegate(
        context: Context,
        filename: String,
        private val encryptionKeyProvider: EncryptionKeyProvider = EncryptionKeyProvider.create(context),
        private val encryptionUtils: EncryptionUtils = EncryptionUtils.INSTANCE
) {

    companion object {
        private const val TAG = "SessionStorageDelegate"
        private const val PREF_KEY_DATA = "com.schibsted.account.persistence.SessionStorageDelegate.sessions"
        private const val PREF_KEY_AES = "com.schibsted.account.persistence.SessionStorageDelegate.aeskey"
        private val GSON = Gson()
    }

    private val appContext = context.applicationContext

    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(filename, Context.MODE_PRIVATE)
    }

    private lateinit var sessions: List<Session>

    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<Session> {
        if (!::sessions.isInitialized) {
            sessions = retrieveMigratedLegacyData() ?: retrieveSessions()
        }
        return sessions
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: List<Session>) {
        clearLegacyData()
        sessions = value
        storeSessions(value)
    }

    private fun retrieveSessions() = runCatching {
        readStorage()
    }.onFailure {
        Logger.error(TAG, "Failed to read storage. Attempting to repair...", it)
        repairUnreadableStorage(it)
    }.onSuccess {
        if (encryptionKeyProvider.isKeyCloseToExpiration()) {
            storeSessions(it)
        }
    }.getOrDefault(emptyList())

    private fun storeSessions(list: List<Session>) {
        runCatching {
            if (encryptionKeyProvider.isKeyCloseToExpiration()) {
                removeDataAndKey()
                encryptionKeyProvider.refreshKeyPair()
            }
            writeStorage(list)
        }.onFailure {
            Logger.error(TAG, "Failed to write storage. Attempting to repair...", it)
            repairUnwritableStorage(list, it)
        }
    }

    /**
     * Retrieves sessions from [SharedPreferences]. If reading or decryption fails for any reason,
     * removes existing data and key from [SharedPreferences].
     */
    private fun readStorage(): List<Session> {
        val data = retrieveStoredData() ?: return emptyList()
        val json = String(data)
        val typeToken: Type = object : TypeToken<List<Session>>() {}.type
        return GSON.fromJson(json, typeToken)
    }

    /**
     * Stores sessions to [SharedPreferences]. If storing or encryption fails for any reason,
     * removes existing data and key from [SharedPreferences].
     */
    private fun writeStorage(items: List<Session>) {
        val (secretKey, encryptedKey) = retrieveStoredKey() ?: generateSecretKey()
        val json = GSON.toJson(items).toByteArray()
        val encryptedData = encryptionUtils.aesEncrypt(json, secretKey)
        storeDataAndKey(encryptedData to encryptedKey)
    }

    /**
     * Reads [SharedPreferences], decrypts data (if it exists) and returns the result.
     * If decryption fails, removes stored data.
     */
    private fun retrieveStoredData(): ByteArray? {
        val (encryptedData, encryptedKey) = getDataAndKey() ?: return null
        val secretKey = recreateSecretKey(encryptedKey)
        return encryptionUtils.aesDecrypt(encryptedData, secretKey)
    }

    /**
     * Returns [SecretKey] and its encrypted [ByteArray] representation from [SharedPreferences],
     * or null, if it doesn't exist.
     */
    private fun retrieveStoredKey(): Pair<SecretKey, ByteArray>? {
        val (_, key) = getDataAndKey() ?: return null
        return recreateSecretKey(key) to key
    }

    /**
     * Builds a [SecretKey] from its encrypted [ByteArray] representation.
     */
    private fun recreateSecretKey(bytes: ByteArray): SecretKey {
        val privateRsaKey = encryptionKeyProvider.keyPair.private
        val decryptedAesKey = encryptionUtils.rsaDecrypt(bytes, privateRsaKey)
        return encryptionUtils.recreateAesKey(decryptedAesKey)
    }

    /**
     * Generates new [SecretKey] and its encrypted [ByteArray] representation.
     */
    private fun generateSecretKey(): Pair<SecretKey, ByteArray> {
        val secretKey = encryptionUtils.generateAesKey()
        val publicRsaKey = encryptionKeyProvider.keyPair.public
        val encodedKey = encryptionUtils.rsaEncrypt(secretKey.encoded, publicRsaKey)
        return secretKey to encodedKey
    }

    /**
     * Returns encrypted data and related AES key from [SharedPreferences].
     */
    private fun getDataAndKey(): Pair<ByteArray, ByteArray>? = prefs.run {
        val data = getBytes(PREF_KEY_DATA) ?: return null
        val key = getBytes(PREF_KEY_AES) ?: return null
        return data to key
    }

    /**
     * Stores encrypted data and related AES key (in its encrypted [ByteArray] representation)
     * to [SharedPreferences].
     */
    private fun storeDataAndKey(dataAndKey: Pair<ByteArray, ByteArray>) = prefs.edit().run {
        putBytes(PREF_KEY_DATA, dataAndKey.first)
        putBytes(PREF_KEY_AES, dataAndKey.second)
        apply()
    }

    private fun removeDataAndKey() = prefs.edit().run {
        remove(PREF_KEY_DATA)
        remove(PREF_KEY_AES)
        apply()
    }

    private fun SharedPreferences.Editor.putBytes(key: String, value: ByteArray) =
            putString(key, String(value.encodeBase64()))

    private fun SharedPreferences.getBytes(key: String): ByteArray? =
            getString(key, null)?.toByteArray()?.decodeBase64()

    private fun repairUnreadableStorage(throwable: Throwable) {
        removeDataAndKey()
        if (throwable is InvalidKeyException) {
            try {
                encryptionKeyProvider.refreshKeyPair()
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to refresh RSA KeyPair", e)
            }
        }
    }

    private fun repairUnwritableStorage(list: List<Session>, throwable: Throwable) {
        removeDataAndKey()
        if (throwable is InvalidKeyException) {
            try {
                encryptionKeyProvider.refreshKeyPair()
                writeStorage(list)
            } catch (e: Exception) {
                removeDataAndKey()
                Logger.error(TAG, "Failed to write storage with new RSA keys", e)
            }
        }
    }

    /**
     * Old versions of the SDK contained a couple of bugs in encryption/decryption utils.
     * Sessions encrypted in the old way cannot be decrypted using fixed encryption utils.
     * Therefore, we had to move the old decryption code to SessionsStorageLegacy.
     * Sessions encrypted in the new way are stored in a new location in SharedPreferences.
     * This lets us migrate the existing session list and not cause unintended "sign-outs".
     */
    private val legacy = SessionStorageLegacy(appContext, encryptionKeyProvider, encryptionUtils)
    private fun clearLegacyData() = legacy.clear()
    private fun retrieveMigratedLegacyData(): List<Session>? = legacy.retrieve()?.also {
        legacy.clear()
        if (it.isNotEmpty()) {
            storeSessions(it)
        }
    }
}
