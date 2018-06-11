/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.haveSubstring
import io.kotlintest.matchers.should
import io.kotlintest.specs.StringSpec
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response

class AuthResponseCheckTest : StringSpec({
    val mockOriginalRequest: Request = mock {
        on { url() } doReturn HttpUrl.parse("https://example.com")
    }

    val mockResponse: Response = mock {
        on { request() } doReturn mockOriginalRequest
    }

    "Default implementation should fail on whitelist mismatch" {
        val result = AuthResponseCheck.default(listOf("https://whitelist-example.com")).responseCheck(mockOriginalRequest, mockResponse)
        result should beInstanceOf(AuthCheckResult.Failed::class)
        (result as AuthCheckResult.Failed).reason should haveSubstring("URL not whitelisted")
    }

    "Default implementation should not fail on whitelist match" {
        val result = AuthResponseCheck.default(listOf("https://example.com")).responseCheck(mockOriginalRequest, mockResponse)
        result should beInstanceOf(AuthCheckResult.Passed::class)
    }

    "Default implementation should fail on redirects" {
        val mockRedirectRequest: Request = mock {
            on { url() } doReturn HttpUrl.parse("https://another-example.com")
        }

        val mockRedirectResponse: Response = mock {
            on { request() } doReturn mockRedirectRequest
        }

        val result = AuthResponseCheck.default(listOf("https://example.com")).responseCheck(mockOriginalRequest, mockRedirectResponse)
        result should beInstanceOf(AuthCheckResult.Failed::class)
        (result as AuthCheckResult.Failed).reason should haveSubstring("URL not whitelisted")
    }

    "Default implementation should not fail on matching mockOriginalRequest and response url" {
        val result = AuthResponseCheck.default(listOf("https://example.com")).responseCheck(mockOriginalRequest, mockResponse)
        result should beInstanceOf(AuthCheckResult.Passed::class)
    }
})
