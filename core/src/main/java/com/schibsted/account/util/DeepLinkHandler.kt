/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.util

import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrElse
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.getQueryParam
import com.schibsted.account.common.util.safeUrl
import java.net.URI

object DeepLinkHandler {
    private val TAG = Logger.DEFAULT_TAG + "-DLH"
    const val PARAM_ACTION = "act"

    fun resolveDeepLink(dataString: String?): DeepLink? {
        if (dataString == null) {
            Logger.info(TAG, { "Received null value deep link. Not performing any actions" })
            return null
        } else {
            Logger.info(TAG, { "Received deep link: ${dataString.safeUrl()}" })
        }

        val uri: URI? = Try { URI.create(dataString) }.getOrElse {
            Logger.info(TAG, { "Unable to parse deep link: ${dataString.safeUrl()}" }, it)
            null
        }?.takeIf { it ->
            it.host == null && it.path.startsWith("/login") ||
                    it.host == "spid" && it.path.startsWith("/login") ||
                    it.host == "login"
        }

        if (uri == null) {
            Logger.info(TAG, { "Deep link did not match the login path or host" })
            return null
        }

        return when (uri.getQueryParam(PARAM_ACTION)) {
            DeepLink.Action.VALIDATE_ACCOUNT.value -> {
                Logger.info(TAG, { "Deep link recognized as ValidateAccount" })
                DeepLink.ValidateAccount(uri)
            }
            DeepLink.Action.IDENTIFIER_PROVIDED.value -> {
                Logger.info(TAG, { "Deep link recognized as IdentifierProvided" })
                DeepLink.IdentifierProvided(uri)
            }
            else -> {
                Logger.info(TAG, { "Deep link with action <${uri.getQueryParam(PARAM_ACTION)}> not recognized" })
                null
            }
        }
    }
}
