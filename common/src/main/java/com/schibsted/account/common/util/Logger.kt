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
        fun log(level: Level, tag: String, message: String, throwable: Throwable?)
    }

    @JvmStatic
    val DEFAULT_LOG_WORKER = object : LogWorker {
        override fun log(level: Level, tag: String, message: String, throwable: Throwable?) {
            when (level) {
                Level.VERBOSE -> Log.v(tag, message, throwable)
                Level.DEBUG -> Log.d(tag, message, throwable)
                Level.INFO -> Log.i(tag, message, throwable)
                Level.WARNING -> Log.w(tag, message, throwable)
                Level.ERROR -> Log.e(tag, message, throwable)
            }
        }
    }

    @JvmStatic
    val DEFAULT_TAG = "IDSDK"

    @JvmStatic
    var loggingEnabled: Boolean = BuildConfig.DEBUG

    @JvmStatic
    var logWorker: LogWorker = DEFAULT_LOG_WORKER

    private fun (() -> Any?).safeString(): String {
        return try {
            invoke().toString()
        } catch (e: Exception) {
            "Log message invocation failed: $e"
        }
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        error(tag, { message }, throwable)
    }

    fun error(tag: String, message: () -> Any?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.ERROR, tag, message.safeString(), throwable)
    }

    fun warn(tag: String, message: String, throwable: Throwable? = null) {
        warn(tag, { message }, throwable)
    }

    fun warn(tag: String, message: () -> Any?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.WARNING, tag, message.safeString(), throwable)
    }

    fun info(tag: String, message: String, throwable: Throwable? = null) {
        info(tag, { message }, throwable)
    }

    fun info(tag: String, message: () -> Any?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.INFO, tag, message.safeString(), throwable)
    }

    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        debug(tag, { message }, throwable)
    }

    fun debug(tag: String, message: () -> Any?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.DEBUG, tag, message.safeString(), throwable)
    }

    fun verbose(tag: String, message: String, throwable: Throwable? = null) {
        verbose(tag, { message }, throwable)
    }

    fun verbose(tag: String, message: () -> Any?, throwable: Throwable? = null) {
        if (loggingEnabled) logWorker.log(Level.VERBOSE, tag, message.safeString(), throwable)
    }
}
