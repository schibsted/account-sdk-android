/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.rule

import android.text.TextUtils

private const val PLUS_PREFIX = "+"

object MobileValidationRule : ValidationRule {

    /**
     * Verifies if the input is a valid.
     * To be valid the input should not be empty, should only contain digits after the first character,
     * and should not be equal to the default prefix provided by the client.
     *
     * @return true if input is valid false otherwise
     */
    override fun isValid(input: String?): Boolean {
        input?.let {
            if (input.length >= 2) {
                val prefix = input.substring(0, 1)
                val number = input.substring(1)
                return !TextUtils.isEmpty(number) && TextUtils.isDigitsOnly(number) && prefix == PLUS_PREFIX
            }
        }
        return false
    }
}
