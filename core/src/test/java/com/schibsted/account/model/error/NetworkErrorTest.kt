/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.model.error

import com.schibsted.account.common.util.Logger
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class NetworkErrorTest {
    init {
        Logger.loggingEnabled = false
    }

    @Test
    fun shouldHandleBadUserCredentialsError() {
        val resp = Response.error<String>(400, ResponseBody.create(MediaType.parse("application/json"), "{\n" +
            "    \"error\": \"invalid_user_credentials\",\n" +
            "    \"error_code\": \"400 Bad Request\",\n" +
            "    \"type\": \"OAuthException\"\n" +
            "}"))

        val err = NetworkError.fromResponse(resp)
        assertEquals("invalid_user_credentials", err.type)
        assertEquals(400, err.code)
    }

    @Test
    fun shouldHandleInvalidRequestError() {
        val resp = Response.error<String>(400, ResponseBody.create(MediaType.parse("application/json"), "{\n" +
            "    \"error\": \"invalid_request\",\n" +
            "    \"error_code\": \"400 Bad Request\",\n" +
            "    \"type\": \"OAuthException\",\n" +
            "    \"error_description\": \"Invalid grant_type parameter or parameter missing\"\n" +
            "}"))

        val err = NetworkError.fromResponse(resp)
        assertEquals("invalid_request", err.type)
        assertEquals(400, err.code)
        assertEquals("Invalid grant_type parameter or parameter missing", err.description)
    }
}
