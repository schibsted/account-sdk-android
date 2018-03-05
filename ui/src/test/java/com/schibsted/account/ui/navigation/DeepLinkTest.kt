/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.navigation

import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.encodeBase64
import com.schibsted.account.util.DeepLink
import io.kotlintest.matchers.haveSubstring
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.WordSpec
import java.net.URI
import java.net.URLEncoder

class DeepLinkTest : WordSpec({
    Logger.loggingEnabled = false

    val redir = URI.create("spid-myapp://login")

    fun String.urlEnc() = URLEncoder.encode(this, "utf-8")

    "ValidateAccount" should {
        "be created correctly" {
            val result = DeepLink.ValidateAccount.create(redir, true)
            result should startWith(redir.toString().urlEnc())
            result should haveSubstring("act=validate-account".urlEnc())
        }

        "not allow non alphanumeric characters" {
            val uri = URI.create(redir.toString() + "?act=validate-account&code=ab.c12.3!")
            DeepLink.ValidateAccount(uri)?.code shouldBe "abc123"
        }
    }

    "IdentifierProvided" should {
        "be created correctly" {
            val result = DeepLink.IdentifierProvided.create(redir, "me@email.com")
            result should startWith(redir.toString().urlEnc())
            result should haveSubstring("act=identifier-provided".urlEnc())
            result should haveSubstring("id=${encodeBase64("me@email.com")}".urlEnc())
        }

        "be parsed correctly" {
            val uri = URI.create(redir.toString() + "?act=identifier-provided&id=${encodeBase64("me@email.com")}")
            val res = DeepLink.IdentifierProvided(uri)
            res shouldNotBe null
            res?.identifier shouldBe "me@email.com"
        }

        "not allow invalid emails" {
            val uri = URI.create(redir.toString() + "?act=identifier-provided&id=${encodeBase64("meemail.com")}")
            DeepLink.IdentifierProvided(uri)?.identifier shouldBe null
        }
    }
})
