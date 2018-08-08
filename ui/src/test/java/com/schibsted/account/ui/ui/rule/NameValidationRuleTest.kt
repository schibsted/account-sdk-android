/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.rule

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec

class NameValidationRuleTest : WordSpec({
    "input " should {
        "not be valid if empty" {
            NameValidationRule.isValid("") shouldBe false
        }

        "not be valid if null" {
            NameValidationRule.isValid(null) shouldBe false
        }

        "not be valid if contains digits" {
            NameValidationRule.isValid("mynameis39") shouldBe false
        }
    }
})