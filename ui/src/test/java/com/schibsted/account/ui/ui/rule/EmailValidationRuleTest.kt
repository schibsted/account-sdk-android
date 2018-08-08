/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.rule

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec

class EmailValidationRuleTest : WordSpec({
    "input" should {
        "not be valid if empty" {
            EmailValidationRule.isValid("") shouldBe false
        }

        "not be valid if null" {
            EmailValidationRule.isValid(null) shouldBe false
        }

        "not be valid if @ is missing" {
            EmailValidationRule.isValid("sdkgmail.com") shouldBe false
        }

        "not be valid if @ is found more than once" {
            EmailValidationRule.isValid("me@workd@gmail.com") shouldBe false
        }

        "not be valid if . is missing" {
            EmailValidationRule.isValid("me_work@gmailcom") shouldBe false
        }

        "not be valid if . is missing after the @" {
            EmailValidationRule.isValid("me.work@gmailcom") shouldBe false
        }

        "be valid if . is found more than once after the @" {
            EmailValidationRule.isValid("mework@gmai.l.com") shouldBe true
        }

        "be valid if . is found before and after the @" {
            EmailValidationRule.isValid("me.work@gmail.com") shouldBe true
        }

        "be valid if alphanumeric" {
            EmailValidationRule.isValid("mework43@gmail.com") shouldBe true
        }

        "not be valid if nothing is found after the last ." {
            EmailValidationRule.isValid("me_work@gmail.") shouldBe false
        }

        "not be valid if nothing is found before the last ." {
            EmailValidationRule.isValid("me_work@.com") shouldBe false
        }

        "not be valid if it contains special chars" {
            EmailValidationRule.isValid("me#work@gmail.") shouldBe false
            EmailValidationRule.isValid("me)work@gmail.") shouldBe false
            EmailValidationRule.isValid("me?work@gmail.") shouldBe false
            EmailValidationRule.isValid("me!work@gmail.") shouldBe false
            EmailValidationRule.isValid("me^work@gmail.") shouldBe false
            EmailValidationRule.isValid("me&work@gmail.") shouldBe false
            EmailValidationRule.isValid("me*work@gmail.") shouldBe false
            EmailValidationRule.isValid("me(work@gmail.") shouldBe false
            EmailValidationRule.isValid("me[work@gmail.") shouldBe false
            EmailValidationRule.isValid("me]work@gmail.") shouldBe false
            EmailValidationRule.isValid("mework@gmail.") shouldBe false
        }
    }
})