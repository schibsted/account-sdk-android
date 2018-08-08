/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.rule

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec

class MobileValidationRuleTest : WordSpec({
    "input" should {
        "not be valid if empty" {
            MobileValidationRule.isValid("") shouldBe false
        }

        "not be valid if null" {
            MobileValidationRule.isValid(null) shouldBe false
        }

        "not be valid if prefix identifier is missing" {
            MobileValidationRule.isValid("48398230") shouldBe false
        }
        "not be valid if it's too short" {
            MobileValidationRule.isValid("+") shouldBe false
        }

        "not be valid if it doesn't contain only digits after the prefix" {
            MobileValidationRule.isValid("+7342b23") shouldBe false
        }

        "be valid when the format is correct" {
            MobileValidationRule.isValid("+47523980") shouldBe true
        }
    }
})