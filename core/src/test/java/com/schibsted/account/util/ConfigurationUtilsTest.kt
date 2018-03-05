/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.util

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.WordSpec

class ConfigurationUtilsTest : WordSpec({
    "getConfigResourceStream" should {
        "require that the asset exists" {
            shouldThrow<IllegalArgumentException> {
                ConfigurationUtils.getConfigResourceStream("some/missing/path/file.conf")
            }
        }

        "return the stream for an existing file" {
            val stream = ConfigurationUtils.getConfigResourceStream("example_config.conf")
            stream shouldNotBe null
            stream.close()
        }
    }

    "parseConfigFile" should {
        "parse the correctly content with the correct syntax" {
            val lines = listOf("key1: value1")
            val pairs = ConfigurationUtils.parseConfigFile(lines)

            pairs.size shouldBe 1
            pairs["key1"] shouldBe "value1"
        }

        "parse URLs" {
            val lines = listOf("key1: http://www.example.com")
            val pairs = ConfigurationUtils.parseConfigFile(lines)

            pairs["key1"] shouldBe "http://www.example.com"
        }

        "trim blank space and lines" {
            val lines = listOf("key1: value1", "key2:value2", " key3 :   value3  ", " ")
            val pairs = ConfigurationUtils.parseConfigFile(lines)

            pairs.size shouldBe 3
            pairs["key1"] shouldBe "value1"
            pairs["key2"] shouldBe "value2"
            pairs["key3"] shouldBe "value3"
        }

        "throw an exception if the syntax is wrong" {
            val lines = listOf("key -- value")
            shouldThrow<IllegalArgumentException> { ConfigurationUtils.parseConfigFile(lines) }
        }
    }
})
