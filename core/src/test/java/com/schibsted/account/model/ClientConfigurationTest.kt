/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.util.Logger
import com.schibsted.account.network.Environment
import io.kotlintest.matchers.haveSubstring
import io.kotlintest.shouldBe
import io.kotlintest.shouldHave
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec

class ClientConfigurationTest : WordSpec({
    Logger.loggingEnabled = false

    val testConfig = ClientConfiguration("https://dev-example.com/", "myId", "mySecret")
    val testParams = mapOf("environment" to "https://dev-example.com/", "clientId" to "myId", "clientSecret" to "mySecret")
    val missingParam = mapOf("environment" to "https://dev-example.com/", "clientId" to "myId")

    "Client configuration" should {
        "use the manually specified config" {
            ClientConfiguration.set(testConfig)
            ClientConfiguration.get() shouldBe testConfig
        }
    }

    "fromParams" should {
        "get config from map params" {
            ClientConfiguration.fromParams(testParams) shouldBe testConfig
        }

        "notify about missing parameters" {
            val exception = shouldThrow<IllegalArgumentException> {
                ClientConfiguration.fromParams(missingParam)
            }

            exception.message!! shouldHave haveSubstring("clientSecret")
        }

        "translate environment abbrevations to full URLs" {
            val abbrParams = mapOf("environment" to "DEV", "clientId" to "myId", "clientSecret" to "mySecret")
            val config = ClientConfiguration.fromParams(abbrParams)
            config.environment shouldBe Environment.ENVIRONMENT_DEVELOPMENT
        }
    }
})
