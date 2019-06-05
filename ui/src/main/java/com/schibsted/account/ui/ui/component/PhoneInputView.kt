/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiUtil
import com.schibsted.account.ui.ui.rule.MobileValidationRule

/**
 * View use for phone number input
 */
class PhoneInputView
/**
 * Constructor
 *
 * @param context
 * @param attrs
 */
@JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : FieldView(context, attrs) {

    private val prefixView: EditText
    val mobileNumberView: SingleFieldView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.schacc_phone_widget, this)

        errorView = view.findViewById(R.id.mobile_error)
        labelView = view.findViewById(R.id.input_field_widget_label)
        validationRule = MobileValidationRule
        prefixView = view.findViewById(R.id.prefix)
        val digitsContainer = view.findViewById<FrameLayout>(R.id.number)

        mobileNumberView = SingleFieldView.create(context) {
            isCancelable { true }
            isTitleVisible { false }
            ime { EditorInfo.IME_ACTION_DONE }
            inputType { InputType.TYPE_CLASS_PHONE }
            hint { context!!.getString(R.string.schacc_required_field_phone_number) }
        }

        digitsContainer.addView(mobileNumberView)

        prefixView.setOnEditorActionListener { _, ime, _ ->
            if (ime == EditorInfo.IME_ACTION_NEXT) {
                mobileNumberView.inputField.requestFocus()
            }
            false
        }

        val focusListener = OnFocusChangeListener { _, _ -> setBackgroundDependingOnFocus() }
        mobileNumberView.inputField.onFocusChangeListener = focusListener
        prefixView.onFocusChangeListener = focusListener

        mobileNumberView.inputField.setOnClickListener { mobileNumberView.inputField.requestFocus() }

        val internalWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {
                hideErrorView()
            }

            override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
                hideErrorView()
            }

            override fun afterTextChanged(editable: Editable) {
            }
        }
        mobileNumberView.setTextWatcher(internalWatcher)
        prefixView.addTextChangedListener(internalWatcher)
        val prefix = UiUtil.getSimCountry(context!!) ?: 46

        setPhonePrefix(prefix)
        setPhonePrefixHint(prefix)
    }

    private fun setPhonePrefixHint(phonePrefix: Int) {
        val prefixText = resources.getString(R.string.schacc_mobile_prefix_hint)
        prefixView.hint = String.format(prefixText, phonePrefix)
    }

    private fun setPhonePrefix(phonePrefix: Int) {
        val prefixText = resources.getString(R.string.schacc_mobile_prefix_hint)
        prefixView.setText(String.format(prefixText, phonePrefix))
    }

    override fun setImeAction(imeOption: Int, editorActionListener: TextView.OnEditorActionListener) {
        mobileNumberView.inputField.imeOptions = imeOption
        mobileNumberView.inputField.setOnEditorActionListener(editorActionListener)
    }

    fun setPhoneNumber(phoneNumber: String) {
        mobileNumberView.inputField.setText(phoneNumber)
    }

    /**
     * Displays a different background indicating if this view has the focus or not
     */
    private fun setBackgroundDependingOnFocus() {
        if (!isErrorVisible) {
            if (prefixView.hasFocus() || mobileNumberView.hasFocus()) {
                mobileNumberView.inputField.setBackgroundResource(R.drawable.schacc_field_shape_focused)
                prefixView.setBackgroundResource(R.drawable.schacc_field_shape_focused)
            } else {
                prefixView.setBackgroundResource(R.drawable.schacc_field_shape_unfocused)
                mobileNumberView.inputField.setBackgroundResource(R.drawable.schacc_field_shape_unfocused)
            }
        }
    }

    fun reset() {
        mobileNumberView.reset()
        prefixView.setText("")
    }

    /**
     * Provides the input
     *
     * @return The input of the field, without visual formatting
     */
    override fun getInput(): String? {
        return prefixView.text.toString().trim { it <= ' ' } + mobileNumberView.input!!
    }

    /**
     * Verifies if the input is a valid.
     * To be valid the input should not be empty, should only contain digits after the first character,
     * and should not be equal to the default prefix provided by the client.
     *
     * @return true if input is valid false otherwise
     */
    override fun isInputValid(): Boolean {
        return mobileNumberView.isInputValid &&
                prefixView.text.isNotEmpty() &&
                validationRule.isValid(input)
    }

    override fun showErrorView() {
        if (!isErrorVisible) {
            errorView?.visibility = View.VISIBLE
            mobileNumberView.inputField.setBackgroundResource(R.drawable.schacc_input_field_widget_shape_error)
            prefixView.setBackgroundResource(R.drawable.schacc_input_field_widget_shape_error)
        }
    }

    override fun hideErrorView() {
        if (isErrorVisible) {
            mobileNumberView.hideErrorView()
            errorView?.visibility = View.GONE
            setBackgroundDependingOnFocus()
        }
    }

    override fun setTextWatcher(textWatcher: TextWatcher) {
        mobileNumberView.setTextWatcher(textWatcher)
        prefixView.addTextChangedListener(textWatcher)
    }

    override fun setError(message: Int) {
        errorView?.setText(message)
    }

    override fun setError(message: String) {
        errorView?.text = message
    }
}
