/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component

import android.content.Context
import android.graphics.Rect
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import com.schibsted.account.ui.R
import com.schibsted.account.ui.ui.rule.PasswordValidationRule

class PasswordView : SingleFieldView {
    private val passwordTransformation = PasswordTransformationMethod.getInstance()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        validationRule = PasswordValidationRule
        isCancelable = false
        inputField.transformationMethod = passwordTransformation
        inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.schacc_ic_eye_on), null)
        inputField.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        inputField.hint = context.getString(R.string.schacc_accessibility_password)

        val internalWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
                updateIconVisibility(sequence, count)
            }

            override fun afterTextChanged(editable: Editable) {
            }
        }

        inputField.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val rect = Rect()
                inputField.getGlobalVisibleRect(rect)
                // if we click on the drawable attached to the editText
                if (motionEvent.rawX >= rect.right - inputField.totalPaddingRight) {
                    updateViewState()
                }
            }
            false
        }

        inputField.addTextChangedListener(internalWatcher)
    }

    private fun updateViewState() {
        if (inputField.transformationMethod == passwordTransformation) {
            inputField.transformationMethod = HideReturnsTransformationMethod.getInstance()
            inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.schacc_ic_eye_off), null)
        } else {
            inputField.transformationMethod = passwordTransformation
            inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.schacc_ic_eye_on), null)
        }
    }

    private fun updateIconVisibility(sequence: CharSequence, count: Int) {
        if (TextUtils.isEmpty(sequence)) {
            inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            inputField.transformationMethod = passwordTransformation
        } else if (count == 1) {
            inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.schacc_ic_eye_on), null)
        }
    }
}
