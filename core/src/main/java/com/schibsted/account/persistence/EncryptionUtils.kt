/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal interface EncryptionUtils {

    companion object {

        val INSTANCE: EncryptionUtils by lazy { object : EncryptionUtils {} }

        private const val RSA_TRANSFORM = "RSA/ECB/PKCS1Padding"
        private const val AES_TRANSFORM = "AES/CBC/PKCS5Padding"
        private const val AES_ALG = "AES"
    }

    /**
     * Generates new AES key.
     */
    fun generateAesKey(): SecretKey = with(KeyGenerator.getInstance(AES_ALG)) {
        init(128)
        generateKey()
    } ?: throw RuntimeException("Failed to generate AES key")

    /**
     * Creates an AES key from provided byte array.
     */
    fun recreateAesKey(bytes: ByteArray): SecretKey =
            SecretKeySpec(bytes, 0, bytes.size, AES_ALG)

    /**
     * Encrypts data using AES secret key. Uses random initialization vector (16 bytes long)
     * that is prepended to the produced cipher text.
     */
    fun aesEncrypt(subjectToEncrypt: ByteArray, aesKey: SecretKey): ByteArray =
            with(Cipher.getInstance(AES_TRANSFORM)) {
                val vector = ByteArray(16).also {
                    SecureRandom().nextBytes(it)
                }
                init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(vector))
                val cipherText = doFinal(subjectToEncrypt) ?: return@with null
                vector + cipherText
            } ?: throw RuntimeException("Failed to encrypt data with AES key")

    /**
     * Decrypts data using AES secret key. The first 16 bytes of cipher text are assumed
     * to be initialization vector.
     *
     * @param subjectToDecrypt cipher text with prepended initialization vector
     */
    fun aesDecrypt(subjectToDecrypt: ByteArray, aesKey: SecretKey): ByteArray = aesDecrypt(
            subjectToDecrypt.copyOfRange(16, subjectToDecrypt.size),
            aesKey,
            iv = subjectToDecrypt.copyOfRange(0, 16))

    /**
     * Decrypts data using AES secret key.
     *
     * @param subjectToDecrypt cipher text without initialization vector
     */
    fun aesDecrypt(subjectToDecrypt: ByteArray, aesKey: SecretKey, iv: ByteArray): ByteArray =
            with(Cipher.getInstance(AES_TRANSFORM)) {
                require(iv.size == 16) { "Initialization vector must be 16 bytes long." }
                init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(iv))
                doFinal(subjectToDecrypt)
            } ?: throw RuntimeException("Failed to decrypt data with AES key")

    /**
     * Encrypts data using RSA public key.
     */
    fun rsaEncrypt(subjectToEncrypt: ByteArray, publicRsaKey: PublicKey): ByteArray =
            with(Cipher.getInstance(RSA_TRANSFORM)) {
                init(Cipher.ENCRYPT_MODE, publicRsaKey)
                doFinal(subjectToEncrypt)
            } ?: throw RuntimeException("Failed to encrypt data with RSA key")

    /**
     * Decrypts data using RSA private key.
     */
    fun rsaDecrypt(subjectToDecrypt: ByteArray, privateRsaKey: PrivateKey): ByteArray =
            with(Cipher.getInstance(RSA_TRANSFORM)) {
                init(Cipher.DECRYPT_MODE, privateRsaKey)
                doFinal(subjectToDecrypt)
            } ?: throw RuntimeException("Failed to decrypt data with RSA key")

}
