/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.accessibility.AccessibilityManager
import android.widget.EditText
import android.widget.TextView
import com.schibsted.account.ui.R
import com.schibsted.account.ui.ui.rule.CodeValidationRule
import java.util.ArrayList

/**
 * Widget made to allow the user to enter a 6 digits code
 */
class CodeInputView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FieldView(context, attrs), View.OnClickListener, CustomEditText.KeyEventListener, View.OnLongClickListener {
    /**
     * List of [EditText] used to put in the verification digits
     */
    internal var inputViews: List<CustomEditText> = listOf()

    /**
     * The index of the focused [EditText] inside [.inputViews]
     */
    private var focusedViewPosition: Int = 0

    /**
     * The [EditText] used to enter the first digit
     */
    private lateinit var digit1: CustomEditText

    /**
     * Concatenates input of each [EditText] of [.inputViews] and return the result
     *
     * @return [String] representing the concatenated
     */
    private val fullInput: String
        get() {
            val finalInputBuilder = StringBuilder()
            inputViews.forEach { finalInputBuilder.append(it.text.toString()) }
            return finalInputBuilder.toString()
        }

    init {
        init()
    }

    /**
     * Initializes views and listeners
     */
    private fun init() {
        val context = context
        val view = LayoutInflater.from(context).inflate(R.layout.schacc_code_verification_widget, this)
        errorView = view.findViewById(R.id.input_error_view)
        inputViews = initializeInputViews(view)
        validationRule = CodeValidationRule
        val focusChangeListener = OnFocusChangeListener { view, _ ->
            if (errorView!!.visibility == View.VISIBLE) {
                errorView!!.visibility = View.INVISIBLE
            }
            applyEditTextLayout(inputViews)
            val length = (view as EditText).text.length
            view.setSelection(length)
            focusedViewPosition = inputViews.indexOf(view)
        }

        val internalTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
                if (sequence.isNotEmpty() && before < count) {
                    setFocusOnNextInputView(inputViews)
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        }

        setDeleteKeyListener(this, inputViews)
        setInputViewsFocusListener(focusChangeListener, inputViews)
        setInputViewsTextWatcher(internalTextWatcher, inputViews)
        setOnClickListener(this, inputViews)
        setOnLongClickListener(this, inputViews)
    }

    private fun setDeleteKeyListener(eventListener: CustomEditText.KeyEventListener, inputViews: List<CustomEditText>) {
        inputViews.forEach { it.keyEventListener = eventListener }
    }

    /**
     * Called in [.init] this method populates the [<] with views defined in
     * the xml file
     *
     * @param view the inflated view
     * @return a [<] to assign to [.inputViews]
     */
    private fun initializeInputViews(view: View): List<CustomEditText> {
        val inputViews = ArrayList<CustomEditText>()
        digit1 = view.findViewById(R.id.input_1)
        val digit2 = view.findViewById<CustomEditText>(R.id.input_2)
        val digit3 = view.findViewById<CustomEditText>(R.id.input_3)
        val digit4 = view.findViewById<CustomEditText>(R.id.input_4)
        val digit5 = view.findViewById<CustomEditText>(R.id.input_5)
        val digit6 = view.findViewById<CustomEditText>(R.id.input_6)

        digit1.contentDescription = context.getString(R.string.schacc_accessibility_verification_code_no, 1, EXPECTED_LENGTH)
        digit2.contentDescription = context.getString(R.string.schacc_accessibility_verification_code_no, 2, EXPECTED_LENGTH)
        digit3.contentDescription = context.getString(R.string.schacc_accessibility_verification_code_no, 3, EXPECTED_LENGTH)
        digit4.contentDescription = context.getString(R.string.schacc_accessibility_verification_code_no, 4, EXPECTED_LENGTH)
        digit5.contentDescription = context.getString(R.string.schacc_accessibility_verification_code_no, 5, EXPECTED_LENGTH)
        digit6.contentDescription = context.getString(R.string.schacc_accessibility_verification_code_no, 6, EXPECTED_LENGTH)

        inputViews.add(digit1)
        inputViews.add(digit2)
        inputViews.add(digit3)
        inputViews.add(digit4)
        inputViews.add(digit5)
        inputViews.add(digit6)

        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!am.isEnabled) {
            digit1.isFocusableInTouchMode = true
            digit1.requestFocus()
        }

        return inputViews
    }

    /**
     * Attributes a [TextWatcher] to each [EditText] of a list of it.
     *
     * @param textWatcher the [TextWatcher] to attribute
     * @param editTexts the list of [EditText]
     */
    private fun setInputViewsTextWatcher(textWatcher: TextWatcher, editTexts: List<CustomEditText>) {
        editTexts.forEach { it.addTextChangedListener(textWatcher) }
    }

    /**
     * Attributes a [android.view.View.OnFocusChangeListener] to each [EditText] of a list of it.
     *
     * @param focusChangeListener the [android.view.View.OnFocusChangeListener] to attribute
     * @param editTexts the list of [EditText]
     */
    private fun setInputViewsFocusListener(focusChangeListener: View.OnFocusChangeListener, editTexts: List<CustomEditText>) {
        editTexts.forEach { it.onFocusChangeListener = focusChangeListener }
    }

    /**
     * Attributes a [android.view.View.OnClickListener] to each [EditText] of a list of it.
     *
     * @param clickListener the [android.view.View.OnClickListener] to attribute
     * @param inputViews the list of [EditText]
     */
    private fun setOnClickListener(clickListener: View.OnClickListener, inputViews: List<CustomEditText>) {
        inputViews.forEach { it.setOnClickListener(clickListener) }
    }

    private fun setOnLongClickListener(clickListener: View.OnLongClickListener, inputViews: List<CustomEditText>) {
        inputViews.forEach { it.setOnLongClickListener(clickListener) }
    }

    override fun onLongClick(v: View?): Boolean {
        val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip

        if (clipData.itemCount > 0) {
            val clipBoardData = clipData.getItemAt(0).text.toString()
            if (validationRule.isValid(clipBoardData)) {
                inputViews.forEachIndexed { i, input -> input.setText(clipBoardData[i].toString()) }
            }
            return true
        }
        return false
    }

    /**
     * Request the focus on the next [EditText] contained in the [.inputViews]
     * This method disabled the focusable property of the previous focused view and enable this
     * property for the new focused [EditText]
     */
    private fun setFocusOnPreviousInputView() {
        val previousViewPosition = focusedViewPosition - 1
        if (previousViewPosition >= 0) {
            val view = inputViews[previousViewPosition]
            inputViews[focusedViewPosition].isFocusableInTouchMode = false
            view.isFocusableInTouchMode = true
            view.requestFocus()
        }
    }

    /**
     * Requests focus on the next [EditText] in [.inputViews]
     * This method disabled the focusable property of the previous focused view and enable this
     * property for the new focused [EditText]
     * If the focused view is the last one in [.inputViews] we keep the focus on it.
     *
     * @param inputViews the [<]
     */
    private fun setFocusOnNextInputView(inputViews: List<CustomEditText>) {
        val nextFocusedViewPosition = focusedViewPosition + 1
        if (nextFocusedViewPosition < inputViews.size) {
            // if we haven't reached the last digit yet focus the next one.
            val view = inputViews[nextFocusedViewPosition]
            view.isFocusableInTouchMode = true
            inputViews[focusedViewPosition].isFocusableInTouchMode = false
            view.requestFocus()
        } else {
            /*
            we're one the last digit already, just be sure to place the cursor after the digit
            to ensure it to be erased when the delete key is pressed
             */
            val view = inputViews[focusedViewPosition]
            val length = view.text.length
            view.setSelection(length)
        }
    }

    /**
     * Applies colors on the [EditText] underline depending on the focus and the state of the
     * [EditText]
     * If the view has the focus or is filled in with a digit we apply `R.color.primaryEnabled`
     * `R.color.darkGrey` otherwise
     *
     * @param editTexts the [<] to iterate on
     */
    private fun applyEditTextLayout(editTexts: List<CustomEditText>) {

        for (editText in editTexts) {
            if (editText.hasFocus()) {
                editText.background.setColorFilter(ContextCompat.getColor(context, R.color.schacc_primaryEnabled), PorterDuff.Mode.SRC_ATOP)
            } else {
                val fieldText = editText.text.toString()
                if (fieldText.isEmpty()) {
                    editText.background.setColorFilter(ContextCompat.getColor(context, R.color.schacc_darkGrey), PorterDuff.Mode.SRC_ATOP)
                } else {
                    editText.background.setColorFilter(ContextCompat.getColor(context, R.color.schacc_primaryEnabled), PorterDuff.Mode.SRC_ATOP)
                }
            }
        }
    }

    /**
     * Resets color, input and focus of each [EditText] contained in [.inputViews]
     * This method should be called when an error occurred and the user try to edit the code.
     *
     * @param inputViews a [<]
     */
    private fun resetFields(inputViews: List<CustomEditText>) {
        for (inputView in inputViews) {
            inputView.setText("")
            inputView.isFocusableInTouchMode = false
        }
        digit1.isFocusableInTouchMode = true
        digit1.requestFocus()
        applyEditTextLayout(inputViews)
    }

    /**
     * Return the current input
     *
     * @return result of [.getFullInput]
     */
    override fun getInput(): String? = fullInput

    /**
     * Checks if the code returned by [.getFullInput] matches requirement
     *
     * @return `true` if the code is valid `false` otherwise
     */
    override fun isInputValid(): Boolean = validationRule.isValid(fullInput)

    /**
     * Shows the error message and apply a red color filter to [.inputViews]'s [EditText]
     */
    override fun showErrorView() {
        if (!isErrorVisible) {
            inputViews.forEach {
                it.background.setColorFilter(ContextCompat.getColor(context, R.color.schacc_error), PorterDuff.Mode.SRC_ATOP)
            }
            errorView?.visibility = View.VISIBLE
        }
    }

    /**
     * Hides error message and call [.resetFields]
     */
    override fun hideErrorView() {
        if (isErrorVisible) {
            errorView?.visibility = View.GONE
            resetFields(inputViews)
        }
    }

    /**
     * Sets a [TextWatcher] to [.inputViews]'s [EditText]
     *
     * @param textWatcher the [TextWatcher] used to interact with the input field.
     */
    override fun setTextWatcher(textWatcher: TextWatcher) {
        setInputViewsTextWatcher(textWatcher, inputViews)
    }

    override fun setImeAction(imeOption: Int, editorActionListener: TextView.OnEditorActionListener) {
        val lastDigit = inputViews[inputViews.size - 1]
        lastDigit.imeOptions = imeOption
        lastDigit.setOnEditorActionListener(editorActionListener)
    }

    /**
     * Calls [.hideErrorView] if [.isErrorVisible] return true
     *
     * @param view the view clicked by the user
     */
    override fun onClick(view: View) {
        if (isErrorVisible) {
            hideErrorView()
        }
    }

    override fun onDeleteKeyPressed() {
        setFocusOnPreviousInputView()
        inputViews[focusedViewPosition].setText("")
    }

    companion object {
        const val EXPECTED_LENGTH = 6
    }
}
