/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Md5 {
    private const val MD5_ALGORITHM = "MD5"

    private val messageDigest by lazy {
        try {
            MessageDigest.getInstance(MD5_ALGORITHM)
        } catch (ex: NoSuchAlgorithmException) {
            null
        }
    }

    fun hash(data: String): String? = messageDigest?.digest(data.toByteArray())?.joinToString("") { Integer.toHexString(0xFF and it.toInt()) }
}
