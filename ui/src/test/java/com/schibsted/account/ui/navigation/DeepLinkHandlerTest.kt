/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.navigation

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.util.Logger
import com.schibsted.account.util.DeepLink
import com.schibsted.account.util.DeepLinkHandler
import io.kotlintest.matchers.instanceOf
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.WordSpec
import java.net.URI

class DeepLinkHandlerTest : WordSpec({
    Logger.loggingEnabled = false
    ClientConfiguration.set(ClientConfiguration("https://env.com/", "myClientId", "myClientSecret"))

    "upon receiving a deep link it" should {
        "correctly handle both known formats (<scheme>://login/?state=... and <scheme>:///login/?state=...)" {
            val variantA = "spid-myClientId://login/?act=${DeepLink.Action.VALIDATE_ACCOUNT.value}&code=123456"
            val variantB = "spid-myClientId:///login?act=${DeepLink.Action.VALIDATE_ACCOUNT.value}&code=123456"

            DeepLinkHandler.resolveDeepLink(variantA) shouldBe instanceOf(DeepLink.ValidateAccount::class)
            DeepLinkHandler.resolveDeepLink(variantB) shouldBe instanceOf(DeepLink.ValidateAccount::class)
        }

        "correctly handle the validate account action" {
            val uri = DeepLink.ValidateAccount.create(URI.create("spid-myClientId://login"), true).toString() + "&code=12345"
            val res = DeepLinkHandler.resolveDeepLink(uri)

            res shouldBe instanceOf(DeepLink.ValidateAccount::class)
            val cRes = res as DeepLink.ValidateAccount
            cRes.code shouldEqual "12345"
            cRes.isPersistable shouldEqual true
        }

        "correctly handle the identifier provided action" {
            val uri = DeepLink.IdentifierProvided.create(URI.create("spid-myClientId://login"), "myid@mail.com")
            val res = DeepLinkHandler.resolveDeepLink(uri.toString())

            res shouldBe instanceOf(DeepLink.IdentifierProvided::class)
            (res as DeepLink.IdentifierProvided).identifier shouldEqual "myid@mail.com"
        }

        "not call anything when not matching the scheme" {
            DeepLinkHandler.resolveDeepLink("spid-myClientId://login?act=noaction") shouldBe null
        }

        "not call anything when not matching the path" {
            val uri = "spid-myClientId:///otherpath?action=${DeepLink.Action.VALIDATE_ACCOUNT}&code=123456"
            DeepLinkHandler.resolveDeepLink(uri) shouldBe null
        }

        "not call anything when not matching the host" {
            val uri = "spid-myClientId://otherpath/?action=${DeepLink.Action.VALIDATE_ACCOUNT}&code=123456"
            DeepLinkHandler.resolveDeepLink(uri) shouldBe null
        }
    }
})
