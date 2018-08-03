/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import com.schibsted.account.ui.R

/**
 * Defines a custom checkbox error resources and attach a text to this checkbox
 * If you have a [android.text.style.ClickableSpan] inside your text you should use
 * this class rather than [CheckBox] in order to avoid to give a focus to the [android.text.style.ClickableSpan]
 * when clicking on the [CheckBox]
 */
class CheckBoxView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SchibstedView(context, attrs) {

    /**
     * An usual [CheckBox]
     */
    private val checkbox: CheckBox

    /**
     * @return `true` if the [.checkbox] is checked
     */
    var isChecked: Boolean
        get() = checkbox.isChecked
        set(checked) {
            checkbox.isChecked = checked
        }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.schacc_chexbox_widget, this)
        checkbox = view.findViewById(R.id.checkbox)
        errorView = view.findViewById(R.id.checkbox_error)
        labelView = view.findViewById(R.id.checkbox_text)

        checkbox.setOnClickListener {
            if (isErrorVisible) {
                hideErrorView()
            }
        }

        if (isInEditMode) {
            val attrRes = attrs!!.getAttributeResourceValue(null, "text", 0)
            if (attrRes > 0) {
                labelView.text = context.getString(attrRes, "Schibsted Account", "My Client")
            } else {
                val previewText = attrs.getAttributeValue(null, "text")
                labelView.text = previewText
            }
        }
    }

    /**
     * Sets [.errorView] visibility to `VISIBLE`
     * Changes the button drawable of [.checkbox] to display the error behavior
     */
    override fun showErrorView() {
        if (!isErrorVisible) {
            errorView?.visibility = View.VISIBLE
            checkbox.setButtonDrawable(R.drawable.schacc_ic_checkbox_error)
        }
    }

    /**
     * Sets [.errorView] visibility to `GONE`
     * Changes the button drawable of [.checkbox] to display the normal behavior
     */
    override fun hideErrorView() {
        if (isErrorVisible) {
            errorView?.visibility = View.GONE
            checkbox.setButtonDrawable(R.drawable.schacc_checkbox_shape)
        }
    }
}
