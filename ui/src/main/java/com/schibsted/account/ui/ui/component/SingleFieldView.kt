/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.schibsted.account.ui.R
import com.schibsted.account.ui.ui.rule.BasicValidationRule
import com.schibsted.account.ui.ui.rule.ValidationRule

open class SingleFieldView : FieldView {

    var isCancelable: Boolean = false
        set(value) {
            updateCancelProperty(value)
        }

    private var infoView: TextView
    var inputField: EditText

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val view = LayoutInflater.from(context).inflate(R.layout.schacc_input_field_widget, this)
        errorView = view.findViewById(R.id.input_error_view)
        infoView = view.findViewById(R.id.input_extra_info_view)
        labelView = view.findViewById(R.id.input_field_widget_label)
        inputField = view.findViewById(R.id.input)
        validationRule = BasicValidationRule
        getXmlProperties(context, attrs)
        init()
    }

    private fun getXmlProperties(context: Context?, attrs: AttributeSet?) {
        context?.obtainStyledAttributes(attrs, R.styleable.SingleFieldView, 0, 0)?.let { ta ->
            try {
                if (ta.hasValue(R.styleable.SingleFieldView_inputType)) {
                    inputField.inputType = getInputTypeFromAttrs(ta) or InputType.TYPE_CLASS_TEXT
                }
                if (ta.hasValue(R.styleable.SingleFieldView_imeOptions)) {
                    inputField.imeOptions = getImeOptionsFromAttrs(ta)
                }
                if (ta.hasValue(R.styleable.SingleFieldView_cancelable)) {
                    updateCancelProperty(ta.getBoolean(R.styleable.SingleFieldView_cancelable, false))
                }
                if (ta.hasValue(R.styleable.SingleFieldView_errorText)) {
                    errorView?.text = ta.getString(R.styleable.SingleFieldView_errorText)
                }

                if (ta.hasValue(R.styleable.SingleFieldView_titleText)) {
                    labelView.text = ta.getString(R.styleable.SingleFieldView_titleText)
                }

                if (ta.hasValue(R.styleable.SingleFieldView_infoText)) {
                    infoView.text = ta.getString(R.styleable.SingleFieldView_infoText)
                }
            } finally {
                ta.recycle()
            }
        }
    }

    private fun getInputTypeFromAttrs(typedArray: TypedArray): Int {
        val inputType = typedArray.getInt(R.styleable.SingleFieldView_inputType, InputType.TYPE_CLASS_TEXT)
        return when (inputType) {
            0 -> {
                inputField.transformationMethod = PasswordTransformationMethod.getInstance()
                InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            1 -> InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            2 -> InputType.TYPE_CLASS_PHONE
            else -> InputType.TYPE_CLASS_TEXT
        }
    }

    private fun getImeOptionsFromAttrs(typedArray: TypedArray): Int {
        val imeOptions = typedArray.getInt(R.styleable.SingleFieldView_imeOptions, EditorInfo.IME_ACTION_NONE)
        return when (imeOptions) {
            0 -> EditorInfo.IME_ACTION_DONE
            1 -> EditorInfo.IME_ACTION_NEXT
            2 -> EditorInfo.IME_ACTION_GO
            else -> EditorInfo.IME_ACTION_NONE
        }
    }

    /**
     * 1- Add a [android.view.View.OnFocusChangeListener] on [.inputField]
     * in order to show or hide the [.errorView] and call [.setBackgroundDependingOnFocus] )}
     *
     *
     * 2 - Add a [android.view.View.OnTouchListener] on [.inputField]
     * in order to erase the text when the cross is clicked.
     *
     *
     * 3- Add a [TextWatcher] on [.inputField] in order to show or hide the cross if there is
     * or not a text to erase.
     */
    private fun init() {
        val focusChangeListener = OnFocusChangeListener { view, hasFocus ->
            if (errorView?.visibility == View.VISIBLE) {
                errorView?.visibility = View.GONE
            }
            setBackgroundDependingOnFocus(hasFocus, inputField)
            if (isCancelable) {
                if (hasFocus) {
                    updateCancelAction(inputField.text.toString())
                } else {
                    inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
            }
        }
        inputField.onFocusChangeListener = focusChangeListener
        inputField.setSingleLine()

        infoView.visibility = if (TextUtils.isEmpty(infoView.text)) View.GONE else View.VISIBLE
    }

    /**
     * Displays a different background indicating if this view has the focus or not
     *
     * @param hasFocus `true` if the view has the focus `false` otherwise
     */
    private fun setBackgroundDependingOnFocus(hasFocus: Boolean, view: View) {
        if (hasFocus) {
            view.setBackgroundResource(R.drawable.schacc_field_shape_focused)
        } else {
            view.setBackgroundResource(R.drawable.schacc_field_shape_unfocused)
        }
    }

    /**
     * Show an error {@link TextView} and modify the background of this view accordingly.
     *
     * @see #hideErrorView()
     */
    override fun showErrorView() {
        if (!isErrorVisible) {
            errorView?.visibility = View.VISIBLE
            inputField.setBackgroundResource(R.drawable.schacc_input_field_widget_shape_error)
        }
        errorView?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    /**
     * hide the previously displayed error and modify the background of this view accordingly.
     *
     * @see #showErrorView()
     */
    override fun hideErrorView() {
        if (isErrorVisible) {
            errorView?.visibility = View.GONE
            setBackgroundDependingOnFocus(hasFocus(), inputField)
        }
    }

    override fun getInput(): String? = inputField.text.toString().trim()

    override fun isInputValid(): Boolean = validationRule.isValid(input)

    override fun setTextWatcher(textWatcher: TextWatcher?) = inputField.addTextChangedListener(textWatcher)

    override fun setImeAction(imeOption: Int, editorActionListener: TextView.OnEditorActionListener?) {
        inputField.imeOptions = imeOption
        inputField.setOnEditorActionListener(editorActionListener)
    }

    fun setInformationMessage(@NonNull message: String) {
        infoView.text = message
        infoView.visibility = if (TextUtils.isEmpty(message)) View.GONE else View.VISIBLE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateCancelProperty(isCancelable: Boolean) {
        val internalWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {
                hideErrorView()
            }

            override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
                hideErrorView()
                if (isCancelable) {
                    updateCancelAction(sequence)
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        }

        if (isCancelable) {
            inputField.setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    val rect = Rect()
                    inputField.getGlobalVisibleRect(rect)
                    //if we click on the drawable attached to the editText
                    if (motionEvent.rawX >= rect.right - inputField.totalPaddingRight) {
                        inputField.setText("")
                    }
                }
                false
            }
        } else {
            inputField.setOnTouchListener(null)
        }
        inputField.addTextChangedListener(internalWatcher)
    }

    private fun updateCancelAction(sequence: CharSequence) {
        if (TextUtils.isEmpty(sequence)) {
            inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        } else {
            inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.schacc_ic_cancel), null)
        }
    }

    fun reset() = inputField.setText("")

    companion object {
        fun create(context: Context?, init: Builder.() -> Unit) = Builder(context, init).build()
    }

    class Builder private constructor() {
        private lateinit var view: SingleFieldView

        constructor(context: Context?, init: Builder.() -> Unit) : this() {
            view = SingleFieldView(context)
            init()
        }

        fun validationRule(init: Builder.() -> ValidationRule) = apply { view.validationRule = init() }
        fun isCancelable(init: Builder.() -> Boolean) = apply { view.isCancelable = init() }
        fun isTitleVisible(init: Builder.() -> Boolean) = apply { view.setTitleVisible(init()) }
        fun title(init: Builder.() -> String) = apply { view.setTitle(init()) }
        fun error(init: Builder.() -> String) = apply { view.setError(init()) }
        fun hint(init: Builder.() -> String) = apply { view.inputField.hint = init() }
        fun informationMessage(init: Builder.() -> String) = apply { view.setInformationMessage(init()) }
        fun ime(init: Builder.() -> Int) = apply { view.inputField.imeOptions = init() }
        fun inputType(init: Builder.() -> Int) = apply { view.inputField.inputType = init() }

        fun build() = view
    }
}
