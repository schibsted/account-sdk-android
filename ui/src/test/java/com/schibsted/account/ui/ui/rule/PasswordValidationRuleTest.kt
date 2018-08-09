/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.rule

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class PasswordValidationRuleTest : WordSpec({
    "input" should {
        "not be valid if empty" {
            PasswordValidationRule.isValid("") shouldBe false
        }

        "not be valid if null" {
            PasswordValidationRule.isValid(null) shouldBe false
        }

        "not be valid if length < 8" {
            PasswordValidationRule.isValid("3s3") shouldBe false
        }

        "be valid if length >= 8" {
            PasswordValidationRule.isValid("32sdfdfl3") shouldBe true
            PasswordValidationRule.isValid("32sdfdfl234lsdfhasdfh3") shouldBe true
        }
    }
})