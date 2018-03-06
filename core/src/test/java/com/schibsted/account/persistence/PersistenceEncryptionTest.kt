/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import com.schibsted.account.common.util.Logger
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldHave
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.matchers.substring
import io.kotlintest.specs.WordSpec
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.RSAKeyGenParameterSpec
import java.util.Arrays

class PersistenceEncryptionTest : WordSpec({
    Logger.loggingEnabled = false
    val encryption = PersistenceEncryption()
    val encTarget = "I want to encrypt this! I'm a very long text, because I want to test encryption on a very long test, is that long enough ? she said yes of course".toByteArray()
    "using a valid aes key" should {
        val aesKey = encryption.generateAesKey()
        "correctly encrypt a string" {
            encryption.aesEncrypt(encTarget, aesKey) shouldNotBe null
        }

        "correctly decrypt the same content" {
            val encRes = encryption.aesEncrypt(encTarget, aesKey)
            encRes shouldNotBe null
            encryption.aesDecrypt(encRes!!, aesKey) shouldBe String(encTarget)
        }
    }

    "using a valid rsa key" should {
        val aesKey = encryption.generateAesKey()
        val rsa = generateKey()
        "correctly encrypt a string" {
            encryption.rsaEncrypt(aesKey.encoded, rsa.public) shouldNotBe null
        }

        "correctly decrypt the same content" {
            val encRes = encryption.rsaEncrypt(aesKey.encoded, rsa.public)
            val aesEncodedKey = encryption.rsaDecrypt(encRes!!, rsa.private)
            assert(Arrays.equals(aesEncodedKey, aesKey.encoded))
        }
    }

    "decrypting using an invalid aes key" should {
        "return null as a result" {
            val aesKey1 = encryption.generateAesKey()
            val aesKey2 = encryption.generateAesKey()
            val encRes = encryption.aesEncrypt(encTarget, aesKey1)
            encRes shouldNotBe null

            val decRes = encryption.aesDecrypt(encRes!!, aesKey2)
            decRes shouldBe null
        }

        "log the exception, but not crash" {
            lateinit var logMessage: String
            lateinit var logException: Throwable

            Logger.loggingEnabled = true
            Logger.logWorker = object : Logger.LogWorker {
                override fun log(level: Logger.Level, tag: String, message: String, throwable: Throwable?) {
                    logMessage = message
                    logException = throwable!!
                }
            }
            val aesKey1 = encryption.generateAesKey()
            val aesKey2 = encryption.generateAesKey()

            val encRes = encryption.aesEncrypt(encTarget, aesKey1)
            encRes shouldNotBe null

            val decRes = encryption.aesDecrypt(encRes!!, aesKey2)
            decRes shouldBe null

            logMessage shouldHave substring("Failed to decrypt")
            logException should beInstanceOf(GeneralSecurityException::class)

            Logger.logWorker = Logger.DEFAULT_LOG_WORKER
            Logger.loggingEnabled = false
        }
    }
}) {
    companion object {
        fun generateKey(): KeyPair {
            val spec = RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4)
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(spec)
            return keyGen.genKeyPair()
        }
    }
}
