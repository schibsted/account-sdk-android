/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

@file:Suppress("DEPRECATION")

package com.schibsted.account.persistence

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.util.Base64
import com.schibsted.account.common.util.Logger
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import javax.security.auth.x500.X500Principal

class EncryptionKeyProvider(private val appContext: Context) {
    internal val keyPair: KeyPair = getStoredEncryptionKey() ?: generateEncryptionKey()

    @SuppressLint("NewApi")
    private fun getStoredEncryptionKey(): KeyPair? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val ks = KeyStore.getInstance(KEYSTORE_PROVIDER)
            ks.load(null)
            val storedKeyPair = getKeyFromKeyStore(ks, KEY_ALIAS)
            if (storedKeyPair == null) {
                deleteStoredKeyPair(ks, KEY_ALIAS)
            }
            storedKeyPair
        } else {
            getKeyFromSharedPreferences()
        }
    }

    private fun getKeyFromSharedPreferences(): KeyPair? {
        val prefs = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SHARED_PREFERENCES_PUBLIC_KEY, null)?.let {
            val public = prefs.getString(SHARED_PREFERENCES_PUBLIC_KEY, null)
            val private = prefs.getString(SHARED_PREFERENCES_PRIVATE_KEY, null)
            val publicByteKey = Base64.decode(public, Base64.DEFAULT)
            val privateByteKey = Base64.decode(private, Base64.DEFAULT)

            val kf = KeyFactory.getInstance(KEY_ALGORITHM_RSA)
            val publicKey = kf.generatePublic(X509EncodedKeySpec(publicByteKey))
            val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(privateByteKey))

            KeyPair(publicKey, privateKey)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun deleteStoredKeyPair(keyStore: KeyStore, alias: String) {
        try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            } else {
                Logger.warn(TAG, { "alias $alias was not found" })
            }
        } catch (e: KeyStoreException) {
            Logger.error(TAG, { "Unable to delete key from keystore" }, e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getKeyFromKeyStore(keyStore: KeyStore, alias: String): KeyPair? {
        var key: KeyPair? = null
        if (keyStore.containsAlias(alias)) {
            try {
                val res = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
                key = KeyPair(res.certificate.publicKey, res.privateKey)
            } catch (e: Exception) {
                Logger.error(TAG, { "An exception occurred when attempting to get key from keystore" }, e)
            }
        } else {
            Logger.warn(TAG, { "alias $alias was not found" })
        }
        return key
    }

    /**
     * This lint rule only makes sense for API 4.3 and older
     * @see https://sites.google.com/a/android.com/tools/tips/lint-checks
     * "TrulyRandom" section
     */
    @SuppressLint("TrulyRandom")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateEncryptionKeyM(): KeyPair? {
        try {
            val ks = KeyStore.getInstance(KEYSTORE_PROVIDER)
            ks.load(null)

            val kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER)

            val startValid = GregorianCalendar().time
            val endValid = GregorianCalendar().apply { add(Calendar.YEAR, 1) }.time

            val paramSpec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getApi23Spec(startValid, endValid) else getApi18Spec(startValid, endValid)
            kpg.initialize(paramSpec)

            return kpg.genKeyPair()
        } catch (ex: Exception) {
            Logger.error(TAG, { "An exception occurred when generating key. Will use fallback." }, ex)
            return null
        }
    }

    private fun generateEncryptionKeyFallback(): KeyPair {
        val spec = RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4)
        val keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA)
        keyGen.initialize(spec)
        val generatedKeys = keyGen.genKeyPair()

        val kf = KeyFactory.getInstance(KEY_ALGORITHM_RSA)
        val publicKey = kf.generatePublic(X509EncodedKeySpec(generatedKeys.public.encoded))
        val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(generatedKeys.private.encoded))

        val prefs = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(SHARED_PREFERENCES_PUBLIC_KEY, Base64.encodeToString(generatedKeys.public.encoded, Base64.DEFAULT))
            putString(SHARED_PREFERENCES_PRIVATE_KEY, Base64.encodeToString(generatedKeys.private.encoded, Base64.DEFAULT))
            apply()
        }
        return KeyPair(publicKey, privateKey)
    }

    @SuppressLint("NewApi")
    private fun generateEncryptionKey(): KeyPair {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            generateEncryptionKeyM() ?: generateEncryptionKeyFallback()
        } else {
            generateEncryptionKeyFallback()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getApi23Spec(validFrom: Date, validTo: Date): AlgorithmParameterSpec {
        return KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .setCertificateSubject(X500Principal(PRINCIPAL))
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeyValidityStart(validFrom)
                .setKeyValidityEnd(validTo)
                .build()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getApi18Spec(validFrom: Date, validTo: Date): AlgorithmParameterSpec {
        return KeyPairGeneratorSpec.Builder(appContext)
                .setAlias(KEY_ALIAS)
                .setSubject(X500Principal(PRINCIPAL))
                .setSerialNumber(BigInteger.valueOf(1447))
                .setStartDate(validFrom)
                .setEndDate(validTo)
                .build()
    }

    companion object {
        private val TAG = Logger.DEFAULT_TAG + "-EncryptionKeyProvider"

        private const val KEY_ALIAS = "identityKeyAlias"
        private const val KEY_ALGORITHM_RSA = "RSA"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"

        private const val SHARED_PREFERENCES_NAME = "IDENTITY_KEYSTORE"
        private const val SHARED_PREFERENCES_PRIVATE_KEY = "IDENTITY_PR_KEY_PAIR"
        private const val SHARED_PREFERENCES_PUBLIC_KEY = "IDENTITY_PU_KEY_PAIR"

        private val PRINCIPAL = "CN=$KEY_ALIAS, O=Schibsted Identity"
    }
}
