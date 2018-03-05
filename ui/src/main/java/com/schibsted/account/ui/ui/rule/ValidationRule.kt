/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.rule

interface ValidationRule {
    /**
     * Checks if rules are respected for the input to be valid
     *
     * @return <code>true</code> if the input matches requirements <code>false</code> otherwise
     */
    fun isValid(input: String?): Boolean
}
