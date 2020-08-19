/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.util

import androidx.annotation.VisibleForTesting
import java.io.InputStream

/**
 * Loads configuration from "assets/schibsted_account.conf". This file may contain
 * (1) key-value pairs separated by a single colon ":" character, or comments
 * that start with "#" character.
 */
object ConfigurationUtils {
    private const val CONFIG_FILE_PATH = "assets/schibsted_account.conf"
    private const val SEPARATOR = ':'
    private const val COMMENT_MARKER = '#'

    /**
     * Loads configuration from "assets/schibsted_account.conf" as a map of keys and values.
     */
    fun paramsFromAssets(): Map<String, String> {
        val stream = getConfigResourceStream(CONFIG_FILE_PATH)
        stream.use {
            val lines = it.reader(Charsets.UTF_8).readLines()
            return parseConfigFile(lines)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getConfigResourceStream(path: String): InputStream {
        val stream = ConfigurationUtils::class.java.classLoader?.getResourceAsStream(path)
        return checkNotNull(stream) { "Missing configuration asset: $path" }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun parseConfigFile(lines: List<String>): Map<String, String> {
        return lines
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .filterNot { it.startsWith(COMMENT_MARKER) }
                .map {
                    val parts = it.split(SEPARATOR, limit = 2)
                    check(parts.size == 2) { "Invalid config file format. Should be <key: value>" }
                    parts[0].trim() to parts[1].trim()
                }.toMap()
    }
}
