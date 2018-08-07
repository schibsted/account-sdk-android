package com.schibsted.account.ui.ui.rule

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec

class CodeValidationRuleTest : WordSpec({
    "the input " should {
        "not be valid if empty" {
            CodeValidationRule.isValid("") shouldBe false
        }

        "not be valid if null" {
            CodeValidationRule.isValid(null) shouldBe false
        }

        "not be be valid if it's not a number" {
            CodeValidationRule.isValid("aaadfc") shouldBe false
            CodeValidationRule.isValid("a111e1") shouldBe false
        }

        "not be be valid if the size is too short" {
            CodeValidationRule.isValid("01234") shouldBe false
        }

        "not be be valid if the size is too long" {
            CodeValidationRule.isValid("1234598324") shouldBe false
        }

        "be valid if size is correct and input is only digits" {
            CodeValidationRule.isValid("012345") shouldBe true
        }
    }
})