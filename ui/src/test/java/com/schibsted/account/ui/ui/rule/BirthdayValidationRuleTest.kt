/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.rule

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import io.kotlintest.tables.row

class BirthdayValidationRuleTest : WordSpec({
    "input" should {
        "not be valid if empty" {
            BirthdayValidationRule.isValid("") shouldBe false
        }

        "not be valid if null" {
            BirthdayValidationRule.isValid(null) shouldBe false
        }

        "not be valid if not fully numeric" {
            BirthdayValidationRule.isValid("1991-dec-13") shouldBe false
        }

        "not be valid if too short" {
            BirthdayValidationRule.isValid("91-12-13") shouldBe false
        }

        "not be valid if separator is not correct" {
            BirthdayValidationRule.isValid("1991/12/13") shouldBe false
        }

        "not be valid if the year doesn't begin with 1 or 2" {
            forall(
                    row("0991-12-13"),
                    row("3991-12-13"),
                    row("4991-12-13"),
                    row("5991-12-13"),
                    row("6991-12-13"),
                    row("7991-12-13"),
                    row("8991-12-13"),
                    row("9991-12-13")
            ) { a -> BirthdayValidationRule.isValid(a) shouldBe false }
        }
        "not be valid if the month is more than 12" {
            BirthdayValidationRule.isValid("1991-13-12") shouldBe false
        }

        "not be valid if the month is less than 01" {
            BirthdayValidationRule.isValid("1991-00-12") shouldBe false
        }

        "not be valid if the day is more than 31" {
            BirthdayValidationRule.isValid("1991-12-32") shouldBe false
        }

        "not be valid if the month is less day 01" {
            BirthdayValidationRule.isValid("1991-12-00") shouldBe false
        }

        "be valid when format is yyyy-mm-dd" {
            BirthdayValidationRule.isValid("1991-12-13") shouldBe true
        }
    }
})