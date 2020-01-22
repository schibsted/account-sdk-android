/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.util

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.net.URI

class UtilTest : StringSpec() {
    init {
        "existsOnClassPath(\"java.util.Date\") should be true" {
            existsOnClasspath("java.util.Date") shouldBe true
        }

        "existsOnClassPath(\"not.existing.Class\") should be false" {
            existsOnClasspath("not.existing.Class") shouldBe false
        }

        val url1 = "http://www.example.com/path"
        "\"$url1\".safeUrl() should not contain hidden params" {
            url1.safeUrl() shouldBe "http://www.example.com/path"
        }

        val url2 = "http://www.example.com/path?"
        "\"$url2\".safeUrl() should hide params" {
            url2.safeUrl() shouldBe "http://www.example.com/path?<hidden>"
        }

        val url3 = "http://www.example.com/path?query=123"
        "\"$url3\".safeUrl() should hide params" {
            url3.safeUrl() shouldBe "http://www.example.com/path?<hidden>"
        }

        "URI.getQueryParam() should return value when param and its value are present" {
            val uri = URI.create("?first=value1&second=value2&third=value3")
            uri.getQueryParam("first") shouldBe "value1"
            uri.getQueryParam("second") shouldBe "value2"
            uri.getQueryParam("third") shouldBe "value3"
        }

        "URI.getQueryParam() should return empty string when value is not present" {
            val uri = URI.create("?empty=")
            uri.getQueryParam("empty") shouldBe ""
        }

        "URI.getQueryParam() should return null when param is not present" {
            val uri = URI.create("?param=value")
            uri.getQueryParam("not-existing-param") shouldBe null
        }

        "URI.getQueryParam() should return null when params are not defined at all" {
            val uri = URI.create("http://www.example.com")
            uri.getQueryParam("not-existing-param") shouldBe null
        }
    }
}
