/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.rule

object NameValidationRule : ValidationRule {
    override fun isValid(input: String?): Boolean {
        input?.let {
            return input.isNotEmpty() && !input.contains("\\d+")
        }
        return false
    }
}
