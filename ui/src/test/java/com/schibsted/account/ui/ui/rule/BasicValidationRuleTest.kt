/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.rule

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec

class BasicValidationRuleTest : WordSpec( {
    "the input" should {
        "not be valid if empty "{
            BasicValidationRule.isValid("") shouldBe false
        }

        "not be valid if null "{
            BasicValidationRule.isValid(null) shouldBe false
        }

        "be valid if alphanumeric "{
            BasicValidationRule.isValid("sd23") shouldBe true
        }

        "be valid if numeric "{
            BasicValidationRule.isValid("23") shouldBe true
        }

        "be valid if alphabetic "{
            BasicValidationRule.isValid("abdc") shouldBe true
        }

    }
})