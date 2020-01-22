/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.util

import android.os.Parcel
import android.provider.Settings
import android.util.Base64
import java.io.UnsupportedEncodingException
import java.net.URI
import java.security.GeneralSecurityException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

/**
 * Returns string value to be used for basic auth header ("Basic " + base64-encoded string
 * that contains clientId and clientSecret separated by a colon).
 */
fun createBasicAuthHeader(clientId: String, clientSecret: String): String =
        "Basic ${encodeBase64("$clientId:$clientSecret")}"

/**
 * Encodes given string as Base64.
 */
fun encodeBase64(str: String): String = Base64.encodeToString(str.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

/**
 * Decodes given Base64 string.
 */
fun decodeBase64(str: String): String = Base64.decode(str, Base64.NO_WRAP).toString(Charsets.UTF_8)

/**
 * Returns true if a class with the given name exists in the classpath.
 */
fun existsOnClasspath(className: String): Boolean {
    return try {
        Class.forName(className)
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

object SecurityUtil {
    private const val CYPHER_METHOD = "PBEWithMD5AndDES"

    @JvmStatic
    @Throws(GeneralSecurityException::class, UnsupportedEncodingException::class)
    fun decryptString(encryptionKey: String, value: String?): String? {
        return value?.let {
            val bytes = Base64.decode(it, Base64.DEFAULT)
            val keyFactory = SecretKeyFactory.getInstance(CYPHER_METHOD)
            val key = keyFactory.generateSecret(PBEKeySpec(encryptionKey.toCharArray()))
            val pbeCipher = Cipher.getInstance(CYPHER_METHOD)
            pbeCipher.init(Cipher.DECRYPT_MODE, key, PBEParameterSpec(Settings.Secure.ANDROID_ID.toByteArray(Charsets.UTF_8), 20))
            String(pbeCipher.doFinal(bytes), Charsets.UTF_8)
        }
    }
}

/**
 * Replaces everything after '?' with the word "<hidden>" and returns the result.
 */
fun String.safeUrl(): String {
    val parts = this.split('?', limit = 2)
    val domain = parts[0]
    val hiddenParams = if (parts.size > 1) "?<hidden>" else ""
    return domain + hiddenParams
}

fun <T> Parcel.readStack(loader: ClassLoader): Stack<T> {
    val items = mutableListOf<T>()
    this.readList(items, loader)
    return Stack<T>().apply { addAll(items) }
}

/**
 * Returns value of a query param with the given name, or null, if it doesn't exist.
 */
fun URI.getQueryParam(p: String): String? = this.query?.split('&')
        ?.map { it.split('=') }
        ?.map { it.first() to it.getOrElse(1) { "" } }
        ?.toMap()
        ?.get(p)
