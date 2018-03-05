/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.rule

import android.util.Patterns

object EmailValidationRule : ValidationRule {
    /**
     * Verifies if the input is a valid, to be valid the input should contain
     * a `@` and at least a `.` after
     *
     * @return true if input is valid false otherwise
     */
    override fun isValid(input: String?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }
}
