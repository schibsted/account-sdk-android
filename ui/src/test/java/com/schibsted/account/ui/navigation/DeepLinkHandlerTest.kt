/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.navigation

import android.content.SharedPreferences
import android.test.mock.MockContext
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.util.Logger
import com.schibsted.account.util.DeepLink
import com.schibsted.account.util.DeepLinkHandler
import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import org.mockito.ArgumentMatchers
import java.net.URI

class DeepLinkHandlerTest : WordSpec({
    Logger.loggingEnabled = false
    ClientConfiguration.set(ClientConfiguration("https://env.com/", "myClientId", "myClientSecret"))

    "upon receiving a deep link it" should {
        fun mockContext(): MockContext = mock {}

        "correctly handle both known formats (<scheme>://login/?state=... and <scheme>:///login/?state=...)" {
            val variantA = "spid-myClientId://login/?act=${DeepLink.Action.VALIDATE_ACCOUNT.value}&code=123456"
            val variantB = "spid-myClientId:///login?act=${DeepLink.Action.VALIDATE_ACCOUNT.value}&code=123456"

            val mock: MockContext = mock {}
            DeepLinkHandler.resolveDeepLink(mock, variantA) shouldBe instanceOf(DeepLink.ValidateAccount::class)
            DeepLinkHandler.resolveDeepLink(mockContext(), variantB) shouldBe instanceOf(DeepLink.ValidateAccount::class)
        }

        "correctly handle the validate account action" {
            val uri = DeepLink.ValidateAccount.createDeepLinkUri(URI.create("spid-myClientId://login"), true).toString() + "&code=12345"
            val res = DeepLinkHandler.resolveDeepLink(mockContext(), uri)

            res shouldBe instanceOf(DeepLink.ValidateAccount::class)
            val cRes = res as DeepLink.ValidateAccount
            cRes.code shouldBe "12345"
            cRes.isPersistable shouldBe true
        }

        "correctly handle the identifier provided action" {
            val uri = DeepLink.IdentifierProvided.createDeepLinkUri(URI.create("spid-myClientId://login"), "myid@mail.com")
            val res = DeepLinkHandler.resolveDeepLink(mockContext(), uri.toString())

            res shouldBe instanceOf(DeepLink.IdentifierProvided::class)
            (res as DeepLink.IdentifierProvided).identifier shouldBe "myid@mail.com"
        }

        "correctly handle the web flow login" {
            val sharedPrefsEditor: SharedPreferences.Editor = mock {
                on { remove(ArgumentMatchers.anyString()) } doReturn it
            }
            val sharedPrefs: SharedPreferences = mock {
                on { getString("OAUTH_STATE", null) } doReturn "test-state"
                on { getString("CODE_VERIFIER", null) } doReturn "code-verifier"
                on { getBoolean("PERSIST_USER", true) } doReturn false
                on { edit() } doReturn sharedPrefsEditor
            }
            val context: MockContext = mock {
                on { applicationContext } doReturn it
                on { getSharedPreferences(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()) } doReturn sharedPrefs
            }

            val uri = "spid-myClientId://login?code=123456&state=test-state"
            val res = DeepLinkHandler.resolveDeepLink(context, uri)

            val webFlowRes = res as DeepLink.WebFlowLogin
            webFlowRes.code shouldBe "123456"
            webFlowRes.codeVerifier shouldBe "code-verifier"
            webFlowRes.isPersistable shouldBe false
        }

        "not call anything when not matching the scheme" {
            DeepLinkHandler.resolveDeepLink(mockContext(), "spid-myClientId://login?act=noaction") shouldBe null
        }

        "not call anything when not matching the path" {
            val uri = "spid-myClientId:///otherpath?act=${DeepLink.Action.VALIDATE_ACCOUNT}&code=123456"
            DeepLinkHandler.resolveDeepLink(mockContext(), uri) shouldBe null
        }

        "not call anything when not matching the host" {
            val uri = "spid-myClientId://otherpath/?act=${DeepLink.Action.VALIDATE_ACCOUNT}&code=123456"
            DeepLinkHandler.resolveDeepLink(mockContext(), uri) shouldBe null
        }
    }
})
