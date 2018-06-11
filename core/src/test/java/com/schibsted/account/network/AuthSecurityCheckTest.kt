/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import okhttp3.HttpUrl
import okhttp3.Request

class AuthSecurityCheckTest : StringSpec({
    fun mockRequest(url: String): Request = mock {
        on { url() } doReturn HttpUrl.parse(url)
    }

    "Should not allow HTTP requests when HTTPS only is set" {
        val res = AuthSecurityCheck.default(listOf("http://example.com")).securityCheck(mockRequest("http://example.com"))
        res should beInstanceOf(AuthCheckResult.Failed::class)
        (res as AuthCheckResult.Failed).reason shouldBe "Authenticated requests can only be done over HTTPS unless specifically allowed"
    }

    "Should allow HTTP when check is disabled" {
        val res = AuthSecurityCheck.default(listOf("http://example.com"), allowNonHttps = true).securityCheck(mockRequest("http://example.com"))
        res should beInstanceOf(AuthCheckResult.Passed::class)
    }

    "Should not allow non-whitelisted domains by default" {
        val res = AuthSecurityCheck.default(listOf("https://example.com")).securityCheck(mockRequest("https://other-example.com"))
        res should beInstanceOf(AuthCheckResult.Failed::class)
        (res as AuthCheckResult.Failed).reason shouldBe "Requests can only be done to whitelisted urls, unless this check is specifically disabled"
    }

    "Should allow whitelisted domains when specified" {
        val res = AuthSecurityCheck.default(listOf("https://example.com"), allowNonWhitelistedDomains = true).securityCheck(mockRequest("https://other-example.com"))
        res should beInstanceOf(AuthCheckResult.Passed::class)
    }
})
