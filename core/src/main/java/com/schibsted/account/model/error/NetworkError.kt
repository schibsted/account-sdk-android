/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model.error

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrElse
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.safeUrl
import retrofit2.Response

data class NetworkError(val code: Int, val type: String, val description: String, val endpoint: String) : InternalError {
    init {
        Logger.debug(Logger.DEFAULT_TAG, { "Request to ${endpoint.safeUrl()} failed with code $code. \nType: <$type> \nDescription: <$description>" })
    }

    override fun toClientError(): ClientError = when {
        code == 302 && type == "ApiException" -> ClientError(ClientError.ErrorType.ALREADY_REGISTERED, "Already registered")
        code == 400 -> when (type) {
            "invalid_user_credentials" -> ClientError(ClientError.ErrorType.INVALID_USER_CREDENTIALS, "Invalid user credentials")
            "invalid_client_credentials" -> ClientError(ClientError.ErrorType.INVALID_CLIENT_CREDENTIALS, "Invalid client credentials")
            "unverified_user" -> ClientError(ClientError.ErrorType.ACCOUNT_NOT_VERIFIED, "The user's account is not verified")
            "invalid_grant" -> when {
                description.startsWith("Invalid code", ignoreCase = true) -> ClientError(ClientError.ErrorType.INVALID_CODE, "Invalid code")
                else -> ClientError(ClientError.ErrorType.INVALID_GRANT, "Invalid grant")
            }
            else -> unknownSpidError()
        }
        code == 401 -> ClientError(ClientError.ErrorType.UNAUTHORIZED, "Invalid access token")
        code == 403 -> ClientError(ClientError.ErrorType.FORBIDDEN, "Client is not authorized to access this API endpoint. Contact SPiD to request access")
        code == 429 && type == "too_many_requests" -> ClientError(ClientError.ErrorType.TOO_MANY_REQUESTS, "Too many requests")
        code == 503 || code == 504 -> ClientError(ClientError.ErrorType.CONNECTION_TIMED_OUT, "The connection has timed out")
        code == -1 -> when (type) {
            "network_error" -> ClientError(ClientError.ErrorType.NETWORK_ERROR, "A network error occurred")
            "parse_error" -> ClientError(ClientError.ErrorType.UNKNOWN_ERROR, "Response from network request could not be parsed")
            "connection_timed_out" -> ClientError(ClientError.ErrorType.CONNECTION_TIMED_OUT, "The connection has timed out")
            else -> unknownSpidError()
        }
        type == "invalid_request" -> when {
            description.contains("phone_number") -> ClientError(ClientError.ErrorType.INVALID_PHONE_NUMBER, "The provided phone number is not valid")
            description.contains("email") -> ClientError(ClientError.ErrorType.INVALID_EMAIL, "The provided email is not valid")
            description.contains("client_id") -> ClientError(ClientError.ErrorType.INVALID_CLIENT_CREDENTIALS, "Invalid client credentials")
            else -> unknownSpidError()
        }
        else -> unknownSpidError()
    }

    private fun unknownSpidError(): ClientError =
            GenericError(
                    { "Unhandled error from SPiD of type <$type>" },
                    {
                        "Request to ${endpoint.safeUrl()} -> $code: <${description.takeIf(String::isNotBlank)
                                ?: "Missing description"}> is not covered in SpidError"
                    })
                    .toClientError()

    companion object {
        private val PARSER = JsonParser()

        fun <T> fromResponse(response: Response<T>): NetworkError {
            require(!response.isSuccessful, { "Cannot parse SPiD error from a successful request" })

            val (type, desc) = Try {
                val body = requireNotNull(response.errorBody(), { "Error body cannot be null" }).string()
                val root = extractErrorRoot(PARSER.parse(body).asJsonObject)
                val type = extractType(root)
                val desc = extractDescription(root)
                Pair(type, desc)
            }.getOrElse {
                Logger.warn(Logger.DEFAULT_TAG + "-" + NetworkError::class.simpleName, { "Parsing SPiD error failed: ${it.message}" }, it)
                Pair("Unknown type", "No description")
            }

            val code = response.code()
            val endpoint = "${response.raw().request().method()} ${response.raw().request().url()}"

            return NetworkError(code, type, desc, endpoint)
        }

        private fun extractErrorRoot(root: JsonObject): JsonObject {
            val err = root.get("error")
            return if (err.isJsonObject) err.asJsonObject else root
        }

        private fun extractType(root: JsonObject) = (root.get("error") ?: root.get("type")).asString

        private fun extractDescription(root: JsonObject): String {
            val field: JsonElement? = (root.get("description") ?: root.get("error_description"))
            return field?.let { f ->
                if (f.isJsonObject) {
                    f.asJsonObject.entrySet().map { "${it.key}: ${it.value.asString}" }.joinToString { ", " }
                } else {
                    f.asString
                }
            } ?: ""
        }
    }
}
