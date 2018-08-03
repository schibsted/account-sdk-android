/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.util

import android.util.Log
import com.schibsted.account.common.BuildConfig

object Logger {
    enum class Level {
        VERBOSE, DEBUG, INFO, WARNING, ERROR
    }

    interface LogWorker {
        fun log(level: Level, tag: String, message: String?, throwable: Throwable?)
    }

    @JvmField
    val DEFAULT_LOG_WORKER = object : LogWorker {
        override fun log(level: Level, tag: String, message: String?, throwable: Throwable?) {
            val description = message ?: "<Missing Description>"
            when (level) {
                Level.VERBOSE -> Log.v(DEFAULT_TAG + tag, description, throwable)
                Level.DEBUG -> Log.d(DEFAULT_TAG + tag, description, throwable)
                Level.INFO -> Log.i(DEFAULT_TAG + tag, description, throwable)
                Level.WARNING -> Log.w(DEFAULT_TAG + tag, description, throwable)
                Level.ERROR -> Log.e(DEFAULT_TAG + tag, description, throwable)
            }
        }
    }

    private const val DEFAULT_TAG = "SCHACC-"

    @JvmStatic
    var loggingEnabled: Boolean = BuildConfig.DEBUG

    @JvmStatic
    var logWorker: LogWorker = DEFAULT_LOG_WORKER

    @JvmStatic
    @JvmOverloads
    fun error(message: String?, throwable: Throwable? = null) = error("ERROR", message, throwable)

    fun error(tag: String, message: String?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.ERROR, tag, message, throwable)
    }

    @JvmStatic
    @JvmOverloads
    fun warn(tag: String, message: String?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.WARNING, tag, message, throwable)
    }

    fun warn(message: String?, throwable: Throwable? = null) = warn("WARN", message, throwable)

    @JvmStatic
    @JvmOverloads
    fun info(message: String?, throwable: Throwable? = null) = info("INFO", message, throwable)

    fun info(tag: String, message: String?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.INFO, tag, message, throwable)
    }

    @JvmStatic
    @JvmOverloads
    fun debug(message: String?, throwable: Throwable? = null) = debug("DEBUG", message, throwable)

    fun debug(tag: String, message: String?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.DEBUG, tag, message, throwable)
    }

    @JvmStatic
    @JvmOverloads
    fun verbose(message: String?, throwable: Throwable? = null) = verbose("VERBOSE", message, throwable)

    fun verbose(tag: String, message: String?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.VERBOSE, tag, message, throwable)
    }
}
