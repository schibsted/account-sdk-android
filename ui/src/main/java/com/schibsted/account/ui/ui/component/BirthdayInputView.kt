/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import com.schibsted.account.ui.R
import com.schibsted.account.ui.ui.rule.BirthdayValidationRule

class BirthdayInputView : InputFieldView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        setValidationRule(BirthdayValidationRule)
        setTextWatcher(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
            }

            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                sequence?.takeIf { before == 0 && (sequence.length == 4 || sequence.length == 7) }?.let {
                    inputField.setText(sequence.toString().plus("-"))
                    inputField.setSelection(sequence.length + 1)
                }

                sequence?.takeIf { sequence.length == 13 }?.let {
                    inputField.setText(sequence.substring(0, 12))
                    inputField.setSelection(sequence.length)
                }
            }
        })
        inputField.filters = arrayOf(InputFilter.LengthFilter(10))
        inputField.inputType = EditorInfo.TYPE_CLASS_NUMBER
        inputField.hint = context.getString(R.string.schacc_accessibility_input_format,
                context.getString(R.string.schacc_required_field_birthday),
                context.getString(R.string.schacc_required_fields_birthday_hint))

        setTitle(R.string.schacc_required_field_birthday)
        errorView.setText(R.string.schacc_required_fields_birthday_error)
    }
}
