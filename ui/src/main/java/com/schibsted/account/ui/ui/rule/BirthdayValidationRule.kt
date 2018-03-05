/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.rule

object BirthdayValidationRule : ValidationRule {
    override fun isValid(input: String?): Boolean {
        return input?.let {
            "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))".toRegex().matchEntire(input) != null
        } == true
    }
}
