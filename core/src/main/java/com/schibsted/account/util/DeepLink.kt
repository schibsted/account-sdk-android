/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.util

import android.content.Context
import android.content.SharedPreferences
import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrDefault
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.getQueryParam
import com.schibsted.account.network.OIDCScope
import java.net.URI

sealed class DeepLink {
    enum class Action(val value: String) {
        VALIDATE_ACCOUNT("validate-account"),
        IDENTIFIER_PROVIDED("identifier-provided");
    }

    open class ValidateAccount protected constructor(val code: String, val isPersistable: Boolean, @OIDCScope val scopes: Array<String>) : DeepLink() {
        companion object {
            private const val PARAM_PERSISTABLE = "pers"
            private const val PARAM_SCOPES = "sc"

            operator fun invoke(uri: URI): ValidateAccount? {
                if (uri.getQueryParam(DeepLinkHandler.PARAM_ACTION) == Action.VALIDATE_ACCOUNT.value) {
                    val code = uri.getQueryParam(PARAM_CODE)
                            ?.filter(Char::isLetterOrDigit)
                            ?.takeIf(String::isNotEmpty)

                    if (code == null) {
                        Logger.info(TAG, "Action recognized, but code validation failed")
                        return null
                    }

                    val isPersistable = Try { Integer.parseInt(uri.getQueryParam(PARAM_PERSISTABLE)) }.map { it == 1 }.getOrDefault { false }
                    @OIDCScope val scopes = uri.getQueryParam(PARAM_SCOPES)?.split(SCOPE_SEPARATOR)?.map(String::trim)?.toTypedArray()

                    return ValidateAccount(code, isPersistable, scopes
                            ?: arrayOf(OIDCScope.SCOPE_OPENID))
                }
                return null
            }

            /**
             * Code gets injected by the server
             */
            fun createDeepLinkUri(redirectUri: URI, isPersistable: Boolean, @OIDCScope scopes: Array<String> = arrayOf(OIDCScope.SCOPE_OPENID)): URI {
                return URI.create("$redirectUri?" +
                        "${DeepLinkHandler.PARAM_ACTION}=${Action.VALIDATE_ACCOUNT.value}" +
                        "&$PARAM_PERSISTABLE=${if (isPersistable) 1 else 0}" +
                        "&$PARAM_SCOPES=${scopes.joinToString(SCOPE_SEPARATOR)}")
            }
        }
    }

    class WebFlowLogin private constructor(code: String, isPersistable: Boolean, val codeVerifier: String) : ValidateAccount(code, isPersistable, arrayOf(OIDCScope.SCOPE_OPENID)) {
        companion object {
            private const val PARAM_STATE = "state"
            private const val PREFERENCE_FILENAME = "WEB_FLOW_LOGIN"
            private const val SHARED_PREFERENCES_OAUTH_STATE = "OAUTH_STATE"
            private const val SHARED_PREFERENCES_CODE_VERIFIER = "CODE_VERIFIER"
            private const val SHARED_PREFERENCES_PERSIST_USER = "PERSIST_USER"

            data class Data(val oauthState: String, val codeVerifier: String, val persistUser: Boolean)

            operator fun invoke(context: Context, uri: URI): WebFlowLogin? {
                val prefs = context.applicationContext.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE)
                val storedData = getData(prefs)
                val state = uri.getQueryParam(PARAM_STATE)

                if (state == null) {
                    Logger.info(TAG, "WebFlowLogin: state is missing from response")
                    return null
                } else if (state != storedData.oauthState) {
                    Logger.info(TAG, "WebFlowLogin: unexpected state")
                    return null
                }

                clearData(prefs)
                val code = uri.getQueryParam(PARAM_CODE)
                if (code == null) {
                    Logger.info(TAG, "WebFlowLogin: code is missing from response")
                    return null
                }


                return WebFlowLogin(code, storedData.persistUser, storedData.codeVerifier)
            }

            fun storeData(context: Context, data: Data) {
                val prefs = context.applicationContext.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE)
                with(prefs.edit()) {
                    putString(SHARED_PREFERENCES_OAUTH_STATE, data.oauthState)
                    putString(SHARED_PREFERENCES_CODE_VERIFIER, data.codeVerifier)
                    putBoolean(SHARED_PREFERENCES_PERSIST_USER, data.persistUser)
                    apply()
                }
            }

            private fun getData(prefs: SharedPreferences): Data {
                val oauthState = prefs.getString(SHARED_PREFERENCES_OAUTH_STATE, "")
                val codeVerifier = prefs.getString(SHARED_PREFERENCES_CODE_VERIFIER, "")
                val persistUser = prefs.getBoolean(SHARED_PREFERENCES_PERSIST_USER, true)
                return Data(oauthState!!, codeVerifier!!, persistUser)
            }

            private fun clearData(prefs: SharedPreferences) {
                with(prefs.edit()) {
                    remove(SHARED_PREFERENCES_OAUTH_STATE)
                    remove(SHARED_PREFERENCES_CODE_VERIFIER)
                    remove(SHARED_PREFERENCES_PERSIST_USER)
                    apply()
                }
            }
        }
    }

    class IdentifierProvided private constructor(val identifier: String) : DeepLink() {
        companion object {
            private const val PARAM_ID = "id"

            operator fun invoke(uri: URI): IdentifierProvided? {
                if (uri.getQueryParam(DeepLinkHandler.PARAM_ACTION) == Action.IDENTIFIER_PROVIDED.value) {
                    val result = uri.getQueryParam(PARAM_ID)?.let { IdentifierProvided(it) }

                    if (result == null) {
                        Logger.info(TAG, "Action recognized, but param $PARAM_ID was missing")
                    }

                    return result
                }
                return null
            }

            fun createDeepLinkUri(redirectUri: URI, identifier: String): URI {
                return URI.create("$redirectUri?" +
                        "${DeepLinkHandler.PARAM_ACTION}=${Action.IDENTIFIER_PROVIDED.value}" +
                        "&$PARAM_ID=$identifier")
            }
        }
    }

    companion object {
        protected const val TAG = "DeepLink"
        protected const val PARAM_CODE = "code"
        private const val SCOPE_SEPARATOR = "-"
    }
}
