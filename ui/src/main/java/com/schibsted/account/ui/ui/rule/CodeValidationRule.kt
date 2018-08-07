package com.schibsted.account.ui.ui.rule

import com.schibsted.account.ui.ui.component.CodeInputView

object CodeValidationRule : ValidationRule {
    override fun isValid(input: String?): Boolean {
        val code = input?.trim()
        return code?.let {
            code.length == CodeInputView.EXPECTED_LENGTH && code.toIntOrNull() != null
        } ?: false
    }
}