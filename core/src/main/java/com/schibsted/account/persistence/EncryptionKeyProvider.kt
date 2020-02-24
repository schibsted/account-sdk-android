/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.support.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit
import javax.security.auth.x500.X500Principal

/**
 * Generates, stores and provides RSA keys.
 */
internal interface EncryptionKeyProvider {

    /**
     * [KeyPair] that is currently used by the app.
     */
    val keyPair: KeyPair

    /**
     * Creates new [KeyPair]. On SDKs >= 18 (JELLY_BEAN_MR2) its expiration is set to 365 days,
     * on older SDKs - unlimited.
     */
    fun refreshKeyPair()

    /**
     * Returns true if the current [KeyPair] will expire within 90 days.
     */
    fun isKeyCloseToExpiration(): Boolean

    companion object {

        /**
         * Creates a new [EncryptionKeyProvider] for the current SDK version.
         */
        fun create(context: Context): EncryptionKeyProvider {
            val appContext = context.applicationContext
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ->
                    createApi28(appContext)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    createApi23(appContext)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ->
                    createApi18(appContext)
                else ->
                    createApi14(appContext)
            }
        }

        @VisibleForTesting
        @RequiresApi(Build.VERSION_CODES.P)
        internal fun createApi28(appContext: Context): EncryptionKeyProvider =
                Api28Provider(appContext)

        @VisibleForTesting
        @RequiresApi(Build.VERSION_CODES.M)
        internal fun createApi23(appContext: Context): EncryptionKeyProvider =
                Api23Provider(appContext)

        @VisibleForTesting
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        internal fun createApi18(appContext: Context): EncryptionKeyProvider =
                Api18Provider(appContext)

        @VisibleForTesting
        internal fun createApi14(appContext: Context): EncryptionKeyProvider =
                Api14Provider(appContext)
    }
}

private const val KEY_ALIAS = "identityKeyAlias"
private const val KEY_PRINCIPAL = "CN=$KEY_ALIAS, O=Schibsted Identity"
private const val KEYSTORE_ALGORITHM = "RSA"
private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val PREFS_FILENAME = "IDENTITY_KEYSTORE"
private const val PREFS_PRIVATE_KEY = "IDENTITY_PR_KEY_PAIR"
private const val PREFS_PUBLIC_KEY = "IDENTITY_PU_KEY_PAIR"
private const val PREFS_EXPIRATION = "KEY_PAIR_VALID_UNTIL"
private const val NEVER = -1L

private abstract class BaseProvider(
        protected val appContext: Context
) : EncryptionKeyProvider {

    protected val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
    }

    protected var expiration: Long
        get() = prefs.getLong(PREFS_EXPIRATION, NEVER)
        set(value) = with(prefs.edit()) {
            putLong(PREFS_EXPIRATION, value)
            apply()
        }

    private lateinit var keys: KeyPair

    override val keyPair: KeyPair
        get() {
            if (!::keys.isInitialized) {
                keys = retrieve() ?: generate()
            }
            return keys
        }

    override fun refreshKeyPair() {
        keys = generate()
    }

    override fun isKeyCloseToExpiration(): Boolean {
        val exp = expiration
        val threshold = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(90)
        return when(exp) {
            NEVER -> false
            else -> exp < threshold
        }
    }

    protected abstract fun retrieve(): KeyPair?
    protected abstract fun generate(): KeyPair
}

private class Api14Provider(appContext: Context) : BaseProvider(appContext) {

    override fun retrieve(): KeyPair? = prefs.run {
        val (publicBytes, privateBytes) = readFromPrefs() ?: return null
        createKeyPair(publicBytes, privateBytes)
    }

    override fun generate(): KeyPair {
        return keyGeneratorApi14()
                .genKeys(specApi14())
                .let {
                    createKeyPair(it.public.encoded, it.private.encoded)
                }.also {
                    storeToPrefs(it.public.encoded, it.private.encoded)
                    expiration = NEVER
                }
    }

    private fun storeToPrefs(public: ByteArray, private: ByteArray) = prefs.edit().run {
        putString(PREFS_PUBLIC_KEY, String(public.encodeBase64()))
        putString(PREFS_PRIVATE_KEY, String(private.encodeBase64()))
        apply()
    }

    private fun readFromPrefs(): Pair<ByteArray, ByteArray>? = prefs.run {
        val public = getString(PREFS_PUBLIC_KEY, null) ?: return null
        val private = getString(PREFS_PRIVATE_KEY, null) ?: return null
        public.toByteArray().decodeBase64() to private.toByteArray().decodeBase64()
    }

    private fun createKeyPair(public: ByteArray, private: ByteArray): KeyPair =
            KeyFactory.getInstance(KEYSTORE_ALGORITHM).run {
                KeyPair(
                        generatePublic(X509EncodedKeySpec(public)),
                        generatePrivate(PKCS8EncodedKeySpec(private))
                )
            }
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
private class Api18Provider(appContext: Context) : BaseProvider(appContext) {

    override fun retrieve(): KeyPair? = loadKeyStore().fetchKeyPairApi18()

    override fun generate(): KeyPair {
        val (from, till) = validityDates()
        return keyGeneratorApi18()
                .genKeys(specApi18(appContext, from, till))
                .also { expiration = till.time }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private class Api23Provider(appContext: Context) : BaseProvider(appContext) {

    override fun retrieve(): KeyPair? = loadKeyStore().fetchKeyPairApi18()

    override fun generate(): KeyPair {
        val (from, till) = validityDates()
        return keyGeneratorApi18()
                .genKeys(specApi23(from, till))
                .also { expiration = till.time }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
private class Api28Provider(appContext: Context) : BaseProvider(appContext) {

    override fun retrieve(): KeyPair? = loadKeyStore().fetchKeyPairApi28()

    override fun generate(): KeyPair {
        val (from, till) = validityDates()
        return keyGeneratorApi18()
                .genKeys(specApi23(from, till))
                .also { expiration = till.time }
    }
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
private fun loadKeyStore(): KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
    load(null)
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
private fun KeyStore.fetchKeyPairApi18(): KeyPair? = runCatching {
    getEntry(KEY_ALIAS, null)?.let {
        it as KeyStore.PrivateKeyEntry
        KeyPair(it.certificate.publicKey, it.privateKey)
    }
}.onFailure {
    deleteEntry(KEY_ALIAS)
}.getOrNull()

@RequiresApi(Build.VERSION_CODES.P)
private fun KeyStore.fetchKeyPairApi28(): KeyPair? = runCatching {
    val privateKey = getKey(KEY_ALIAS, null) as? PrivateKey ?: return null
    val publicKey = getCertificate(KEY_ALIAS)?.publicKey ?: return null
    KeyPair(publicKey, privateKey)
}.onFailure {
    deleteEntry(KEY_ALIAS)
}.getOrNull()

private fun validityDates(): Pair<Date, Date> = System.currentTimeMillis().let {
    Date(it - TimeUnit.DAYS.toMillis(1)) to Date(it + TimeUnit.DAYS.toMillis(365))
}

private fun specApi14(): AlgorithmParameterSpec =
        RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4)

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
private fun specApi18(context: Context, from: Date, till: Date): AlgorithmParameterSpec =
        KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(X500Principal(KEY_PRINCIPAL))
                .setSerialNumber(BigInteger.valueOf(1447))
                .setStartDate(from)
                .setEndDate(till)
                .build()

@RequiresApi(Build.VERSION_CODES.M)
private fun specApi23(from: Date, till: Date): AlgorithmParameterSpec =
        KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .setCertificateSubject(X500Principal(KEY_PRINCIPAL))
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeyValidityStart(from)
                .setKeyValidityEnd(till)
                .build()

private fun keyGeneratorApi14(): KeyPairGenerator =
        KeyPairGenerator.getInstance(KEYSTORE_ALGORITHM)

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
private fun keyGeneratorApi18(): KeyPairGenerator =
        KeyPairGenerator.getInstance(KEYSTORE_ALGORITHM, KEYSTORE_PROVIDER)

private fun KeyPairGenerator.genKeys(spec: AlgorithmParameterSpec): KeyPair = run {
    initialize(spec)
    genKeyPair()
}
