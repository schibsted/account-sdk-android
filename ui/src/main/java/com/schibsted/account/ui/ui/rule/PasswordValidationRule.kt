/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.rule

object PasswordValidationRule : ValidationRule {
    override fun isValid(input: String?): Boolean {
        input?.let {
            return input.length >= 8
        }
        return false
    }
}
