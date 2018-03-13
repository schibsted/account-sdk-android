/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.net.URI

class UiConfigurationTest : StringSpec({
    "newBuilder should correctly copy the properties of UiConfiguration" {
        val config = UiConfiguration("abc", URI.create("spid-xxxx://login"), 47).copy(teaserText = "Hello")
        val newConfig = config.newBuilder().build()

        newConfig.clientName shouldBe config.clientName
        newConfig.defaultPhonePrefix shouldBe config.defaultPhonePrefix
        newConfig.redirectUri shouldBe config.redirectUri
        newConfig.teaserText shouldBe config.teaserText
        newConfig.headerResource shouldBe config.headerResource
        newConfig.identifier shouldBe config.identifier
        newConfig.identifierType shouldBe config.identifierType
        newConfig.signUpEnabled shouldBe config.signUpEnabled
        newConfig.signUpNotAllowedErrorMessage shouldBe config.signUpNotAllowedErrorMessage
        newConfig.isClosingAllowed shouldBe config.isClosingAllowed
        newConfig.smartlockEnabled shouldBe config.smartlockEnabled
        newConfig.locale shouldBe config.locale
    }
})
