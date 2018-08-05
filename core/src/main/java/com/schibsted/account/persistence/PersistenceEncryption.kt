/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.util.Base64
import com.schibsted.account.common.util.Logger
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

private val TAG = PersistenceEncryption::class.java.simpleName
private const val RSA_TRANSFORM = "RSA/ECB/PKCS1Padding"
private const val AES_TRANSFORM = "AES/CBC/PKCS5Padding"
const val AES_ALG = "AES"

class PersistenceEncryption {

    fun generateAesKey(): SecretKey = KeyGenerator.getInstance(AES_ALG).apply { init(128) }.generateKey()

    fun rsaEncrypt(subjectToEncrypt: ByteArray?, publicRsaKey: PublicKey): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(RSA_TRANSFORM)
            cipher.init(Cipher.ENCRYPT_MODE, publicRsaKey)
            cipher.doFinal(subjectToEncrypt)
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to encrypt content", e)
            null
        }
    }

    fun rsaDecrypt(subjectToDecrypt: ByteArray, privateRsaKey: PrivateKey): ByteArray? {
        val cipher = Cipher.getInstance(RSA_TRANSFORM)
        return try {
            cipher.init(Cipher.DECRYPT_MODE, privateRsaKey)
            return cipher.doFinal(subjectToDecrypt)
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to decrypt content", e)
            null
        }
    }

    fun aesEncrypt(subjectToEncrypt: ByteArray, aesKey: SecretKey?): ByteArray? {
        val aesCipher = Cipher.getInstance(AES_TRANSFORM)
        return try {
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(ByteArray(16)))
            return aesCipher.doFinal(encode(subjectToEncrypt))
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to encrypt content", e)
            null
        }
    }

    fun aesDecrypt(subject: ByteArray, aesKey: SecretKey): String? {
        val aesCipher = Cipher.getInstance(AES_TRANSFORM)
        return try {
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(ByteArray(16)))
            String(decode(aesCipher.doFinal(subject)))
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to decrypt content", e)
            null
        }
    }

    private fun encode(subject: ByteArray?) = Base64.encode(subject, Base64.DEFAULT)
    private fun decode(subject: ByteArray) = Base64.decode(subject, Base64.DEFAULT)
}
