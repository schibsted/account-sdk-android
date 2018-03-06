/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.util

import android.support.annotation.VisibleForTesting
import java.io.InputStream

object ConfigurationUtils {
    private const val CONFIG_FILE_PATH = "assets/schibsted_account.conf"

    fun paramsFromAssets(): Map<String, Any> {
        val stream = getConfigResourceStream(CONFIG_FILE_PATH)

        stream.use {
            val lines = it.reader(Charsets.UTF_8).readLines()
            return parseConfigFile(lines)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal fun getConfigResourceStream(path: String): InputStream {
        val stream = ConfigurationUtils::class.java.classLoader.getResourceAsStream(path)
        require(stream != null, { "Missing configuration asset: $path" })
        return stream
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal fun parseConfigFile(lines: List<String>): Map<String, Any> {
        return lines
                .filter { it.trim().isNotEmpty() }
                .map {
                    val parts = it.split(delimiters = *charArrayOf(':'), limit = 2)
                    require(parts.size == 2, { "Invalid config file format. Should be <key: value>" })
                    parts[0].trim() to parts[1].trim()
                }.toMap()
    }
}
