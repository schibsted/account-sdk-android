/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.schibsted.account.common.util.Logger
import java.security.KeyPair
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KProperty

internal const val AES_KEY = "IDENTITY_AES_PREF_KEY"

internal class SessionStorageDelegate(
    private val appContext: Context,
    private val preferenceFilename: String,
    private val preferenceKey: String,
    private val encryption: PersistenceEncryption = PersistenceEncryption(),
    private val rsaKeyPair: KeyPair = EncryptionKeyProvider(appContext).keyPair
) {

    private var data: List<UserPersistence.Session> = readDataFromPersistence() ?: listOf()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<UserPersistence.Session> = data

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: List<UserPersistence.Session>) {
        this.data = value
        val prefs = appContext.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        val dataToPersist = GSON.toJson(this.data)
        var aesKey: SecretKey? = null

        // Try to get the encrypted aes key from storage
        var encryptedAesKey = prefs.getString(AES_KEY, null)

        if (encryptedAesKey == null) {
            generateAesKeyData()?.let {
                aesKey = it.first
                encryptedAesKey = Base64.encodeToString(it.second, Base64.DEFAULT)
            }
        } else {
            // if the encrypted aes key is found, we decrypt it and recreate the aes key from it
            aesKey = getOriginalAesKey(Base64.decode(encryptedAesKey, Base64.DEFAULT))
        }

        if (dataToPersist.isNullOrEmpty()) {
            Logger.info(TAG, { "No session to persist, not writing to shared preferences." })
        } else if (aesKey == null || encryptedAesKey.isNullOrEmpty()) {
            Logger.info(TAG, { "No encryption key found, not writing to shared preferences." })
        } else {
            val encryptedData = Base64.encodeToString(encryption.aesEncrypt(dataToPersist.toByteArray(), aesKey), Base64.DEFAULT)
            persistData(prefs, encryptedData, encryptedAesKey)
        }
    }

    private fun readDataFromPersistence(): List<UserPersistence.Session>? {
        val prefs = appContext.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        val persistedData = prefs.getString(preferenceKey, null)
        val persistedAesKey = prefs.getString(AES_KEY, null)

        val encryptedData = decodeData(persistedData)
        val encryptedAesKey = decodeData(persistedAesKey)

        val isDataReadable = encryptedAesKey != null && encryptedAesKey.isNotEmpty() && encryptedData != null && encryptedData.isNotEmpty()
        val aesKey = if (isDataReadable) getOriginalAesKey(encryptedAesKey!!) else null

        if (aesKey != null) {
            val data: String? = encryption.aesDecrypt(encryptedData!!, aesKey)
            if (data.isNullOrEmpty() || data.equals(EMPTY_JSON_ARRAY)) {
                Logger.info(TAG, { "Decrypted sessions from persistence returned an empty set." })
                removePersistedData(prefs)
                return null
            }

            val typeToken = object : TypeToken<List<UserPersistence.Session>>() {}.type

            return try {
                val res: List<UserPersistence.Session> = GSON.fromJson(data, typeToken)
                res
            } catch (e: JsonParseException) {
                null
            } catch (_: JsonSyntaxException) {
                null
            }
        } else {
            Logger.info(TAG, { "Unable to retrieve AES key for decryption, returning empty set." })
            removePersistedData(prefs)
            return null
        }
    }

    /**
     * If data are not null, decode the data using [Base64]
     * @param data the [Base64] encoded [String]
     * @return the decoded data as a [ByteArray] or null if the decoding failed
     */
    private fun decodeData(data: String?): ByteArray? {
        if (data != null && data.isNotEmpty()) {
            return try {
                Base64.decode(data, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                Logger.error(TAG, { "Unable to decode session data." }, e)
                null
            }
        }
        return null
    }

    /**
     * If the data are not null, persists data and the encrypted [SecretKey] to [SharedPreferences]
     * @param sharedPreferences : [SharedPreferences] file where information are stored
     * @param encryptedData : a [String] representing the encrypted data to store
     * @param encryptedAesKey : a [String] representing the encrypted [SecretKey] to store
     *
     */
    private fun persistData(sharedPreferences: SharedPreferences, encryptedData: String?, encryptedAesKey: String) {
        encryptedData?.let {
            val editor = sharedPreferences.edit()
            editor.putString(preferenceKey, encryptedData)
            editor.putString(AES_KEY, encryptedAesKey)
            editor.apply()
        } ?: let {
            Logger.error(TAG, { "Unable to write to shared preferences. Not persisting session" })
        }
    }

    /**
     * Builds a [SecretKey] with a given [ByteArray] representing the encrypted [SecretKey]
     * @param encryptedAesKey the representation of the encrypted [SecretKey]
     * @return the [SecretKey] if the decryption was successful, null otherwise
     */
    private fun getOriginalAesKey(encryptedAesKey: ByteArray): SecretKey? {
        val aesEncodedKey = encryption.rsaDecrypt(encryptedAesKey, rsaKeyPair.private)
        if (aesEncodedKey != null && aesEncodedKey.isNotEmpty()) {
            return SecretKeySpec(aesEncodedKey, 0, aesEncodedKey.size, AES_ALG)
        }
        return null
    }

    /**
     * Generates an AES key and its rsa encrypted form
     * @return a [SecretKey] representing the aes key and a [ByteArray] representing
     * the encrypted form of the [SecretKey] or null if encryption failed
     */
    private fun generateAesKeyData(): Pair<SecretKey, ByteArray>? {
        val aesKey = encryption.generateAesKey()
        // we encrypt the newly created aes key
        val byteEncryptedAesKey = encryption.rsaEncrypt(aesKey.encoded, rsaKeyPair.public)
        return byteEncryptedAesKey?.let { Pair(aesKey, byteEncryptedAesKey) }
    }

    /**
     * Removes data from the [SharedPreferences] file, it removes the persisted [SecretKey] as well
     * @param prefs : [SharedPreferences] file where data are stored
     */
    private fun removePersistedData(prefs: SharedPreferences) {
        Logger.info(TAG, { "Clearing the contents of shared preferences" })
        val editor = prefs.edit()
        editor.remove(preferenceKey)
        editor.remove(AES_KEY)
        editor.apply()
    }

    companion object {
        private val TAG = Logger.DEFAULT_TAG + "-SessionStorageDelegate"
        private val EMPTY_JSON_ARRAY = "[]"
        private val GSON = Gson()
    }
}
