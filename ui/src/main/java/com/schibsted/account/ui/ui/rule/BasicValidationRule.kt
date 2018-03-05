/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.rule

object BasicValidationRule : ValidationRule {
    override fun isValid(input: String?): Boolean {
        return !input.isNullOrEmpty()
    }
}
