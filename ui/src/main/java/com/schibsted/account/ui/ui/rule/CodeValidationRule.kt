package com.schibsted.account.ui.ui.rule

import android.text.TextUtils
import com.schibsted.account.ui.ui.component.CodeInputView

object CodeValidationRule : ValidationRule {
    override fun isValid(input: String?): Boolean {
        val code = input?.trim()
        return code?.let {
            code.length == CodeInputView.EXPECTED_LENGTH && TextUtils.isDigitsOnly(code)
        } ?: false
    }
}