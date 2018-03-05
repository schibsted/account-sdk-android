/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.test

import com.schibsted.account.common.util.Logger

object TestUtil {
    fun readResource(path: String) = javaClass.classLoader.getResource(path).readText()

    val testLogger = object : Logger.LogWorker {
        override fun log(level: Logger.Level, tag: String, message: String, throwable: Throwable?) {
            println("$level <$tag>: $message")
        }
    }

    class CaptureLogger : Logger.LogWorker {
        val messages = mutableListOf<String>()

        override fun log(level: Logger.Level, tag: String, message: String, throwable: Throwable?) {
            println("Capturing message: " + message)
            messages.add(message)
        }

        fun reset() {
            messages.clear()
        }
    }
}
